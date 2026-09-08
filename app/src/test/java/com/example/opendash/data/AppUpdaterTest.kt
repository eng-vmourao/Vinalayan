package com.example.opendash.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdaterTest {
    @Test
    fun semanticVersionsAreComparedNumerically() {
        assertTrue(AppVersion.parse("v0.10.0-preview")!! > AppVersion.parse("0.9.9-preview")!!)
        assertTrue(AppVersion.parse("1.0.0")!! > AppVersion.parse("1.0.0-preview")!!)
    }

    @Test
    fun latestValidUniversalApkIsSelected() {
        val json = """
            [
              {
                "tag_name": "v0.1.3-preview",
                "draft": false,
                "assets": [{
                  "name": "Vinalayan-0.1.3-preview-universal.apk",
                  "browser_download_url": "https://github.com/eng-vmourao/Vinalayan/releases/download/v0.1.3-preview/Vinalayan-0.1.3-preview-universal.apk",
                  "digest": "sha256:${"a".repeat(64)}"
                }]
              },
              {
                "tag_name": "v0.1.2-preview",
                "draft": false,
                "assets": [{
                  "name": "Vinalayan-0.1.2-preview-universal.apk",
                  "browser_download_url": "https://github.com/eng-vmourao/Vinalayan/releases/download/v0.1.2-preview/Vinalayan-0.1.2-preview-universal.apk",
                  "digest": "sha256:${"b".repeat(64)}"
                }]
              }
            ]
        """.trimIndent()

        assertEquals("v0.1.3-preview", AppUpdater.parseLatestRelease(json, "0.1.1-preview")?.tagName)
    }

    @Test
    fun currentVersionIsNotOfferedAgain() {
        val json = """
            [{
              "tag_name": "v0.1.2-preview",
              "draft": false,
              "assets": [{
                "name": "Vinalayan-0.1.2-preview-universal.apk",
                "browser_download_url": "https://github.com/eng-vmourao/Vinalayan/releases/download/v0.1.2-preview/Vinalayan-0.1.2-preview-universal.apk",
                "digest": "sha256:${"c".repeat(64)}"
              }]
            }]
        """.trimIndent()

        assertNull(AppUpdater.parseLatestRelease(json, "0.1.2-preview"))
    }
}
