package com.example.opendash.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

data class VinalayanRelease(
    val tagName: String,
    val versionName: String,
    val apkName: String,
    val downloadUrl: String,
    val sha256: String,
)

object AppUpdater {
    private const val RELEASES_API = "https://api.github.com/repos/eng-vmourao/Vinalayan/releases?per_page=20"
    private const val APK_MIME = "application/vnd.android.package-archive"
    private const val MAX_APK_BYTES = 250L * 1024L * 1024L

    suspend fun findUpdate(currentVersionName: String): VinalayanRelease? = withContext(Dispatchers.IO) {
        val json = requestText(RELEASES_API)
        parseLatestRelease(json, currentVersionName)
    }

    suspend fun download(context: Context, release: VinalayanRelease): File = withContext(Dispatchers.IO) {
        val updateDir = File(context.cacheDir, "updates").apply { mkdirs() }
        val safeTag = release.tagName.replace(Regex("[^A-Za-z0-9._-]"), "-")
        val target = File(updateDir, "Vinalayan-$safeTag-universal.apk")
        if (target.isFile && sha256(target) == release.sha256) {
            validatePackage(context, target, release)
            return@withContext target
        }

        val partial = File(updateDir, "${target.name}.part")
        partial.delete()
        val connection = open(release.downloadUrl)
        try {
            val expectedLength = connection.contentLengthLong
            require(expectedLength in 1..MAX_APK_BYTES) { "Tamanho do APK inválido" }
            connection.inputStream.use { input ->
                partial.outputStream().use { output -> input.copyTo(output) }
            }
            require(partial.length() == expectedLength) { "O download do APK ficou incompleto" }
            require(sha256(partial) == release.sha256) { "A verificação de segurança do APK falhou" }
            validatePackage(context, partial, release)
            target.delete()
            require(partial.renameTo(target)) { "Não foi possível preparar o APK para instalação" }
            target
        } finally {
            connection.disconnect()
            partial.takeIf { it.exists() }?.delete()
        }
    }

    fun canRequestInstall(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.O || context.packageManager.canRequestPackageInstalls()

    fun installPermissionIntent(context: Context): Intent =
        Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}"))

    fun installerIntent(context: Context, apk: File): Intent {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apk)
        return Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
            setDataAndType(uri, APK_MIME)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    internal fun parseLatestRelease(json: String, currentVersionName: String): VinalayanRelease? {
        val current = AppVersion.parse(currentVersionName) ?: return null
        val releases = JSONArray(json)
        val candidates = buildList {
            for (index in 0 until releases.length()) {
                val releaseJson = releases.getJSONObject(index)
                if (releaseJson.optBoolean("draft")) continue
                val tag = releaseJson.optString("tag_name")
                val version = AppVersion.parse(tag) ?: continue
                if (version <= current) continue
                val assets = releaseJson.optJSONArray("assets") ?: continue
                for (assetIndex in 0 until assets.length()) {
                    val asset = assets.getJSONObject(assetIndex)
                    val name = asset.optString("name")
                    if (!name.startsWith("Vinalayan-") || !name.endsWith("-universal.apk")) continue
                    val digest = asset.optString("digest").removePrefix("sha256:").lowercase()
                    if (!digest.matches(Regex("[0-9a-f]{64}"))) continue
                    val url = asset.optString("browser_download_url")
                    if (!url.startsWith("https://github.com/eng-vmourao/Vinalayan/releases/download/")) continue
                    add(
                        version to VinalayanRelease(
                            tagName = tag,
                            versionName = tag.removePrefix("v"),
                            apkName = name,
                            downloadUrl = url,
                            sha256 = digest,
                        ),
                    )
                }
            }
        }
        return candidates.maxByOrNull { it.first }?.second
    }

    private fun requestText(url: String): String {
        val connection = open(url)
        return try {
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private fun open(url: String): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            instanceFollowRedirects = true
            connectTimeout = 15_000
            readTimeout = 60_000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            setRequestProperty("User-Agent", "Vinalayan updater")
            require(responseCode == HttpURLConnection.HTTP_OK) { "GitHub respondeu com código $responseCode" }
        }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    @Suppress("DEPRECATION")
    private fun validatePackage(context: Context, apk: File, release: VinalayanRelease) {
        val info = context.packageManager.getPackageArchiveInfo(apk.absolutePath, 0)
            ?: error("O arquivo baixado não é um APK válido")
        require(info.packageName == context.packageName) { "O APK pertence a outro aplicativo" }
        require(info.versionName == release.versionName) { "A versão interna do APK não corresponde à versão publicada" }
    }
}

internal data class AppVersion(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val preview: Boolean,
) : Comparable<AppVersion> {
    override fun compareTo(other: AppVersion): Int =
        compareValuesBy(this, other, AppVersion::major, AppVersion::minor, AppVersion::patch)
            .takeIf { it != 0 }
            ?: compareValuesBy(this, other) { !it.preview }

    companion object {
        private val PATTERN = Regex("^v?(\\d+)\\.(\\d+)\\.(\\d+)(?:[-+].*)?$")

        fun parse(value: String): AppVersion? {
            val match = PATTERN.matchEntire(value.trim()) ?: return null
            return AppVersion(
                major = match.groupValues[1].toIntOrNull() ?: return null,
                minor = match.groupValues[2].toIntOrNull() ?: return null,
                patch = match.groupValues[3].toIntOrNull() ?: return null,
                preview = value.substringAfter('-', "").isNotEmpty(),
            )
        }
    }
}
