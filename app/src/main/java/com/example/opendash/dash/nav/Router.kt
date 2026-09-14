package com.example.opendash.dash.nav

import com.example.opendash.util.DebugLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Fetches a road route from the public OSRM demo server. Called at planning time
 * (destination shared) while the phone still has internet — the result is cached
 * so riding can proceed offline. Driving profile suits the Himalayan fine.
 */
object Router {
    private const val TAG = "Router"
    private const val BASE = "https://router.project-osrm.org/route/v1/driving"
    private const val UA = "OpenDash/1.1 (personal motorcycle nav; single user)"

    suspend fun route(from: GeoPoint, to: GeoPoint): Route? = withContext(Dispatchers.IO) {
        val url = "$BASE/${from.lng},${from.lat};${to.lng},${to.lat}" +
                "?overview=full&geometries=polyline&steps=true&annotations=speed,maxspeed"
        try {
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                setRequestProperty("User-Agent", UA)
                connectTimeout = 10_000
                readTimeout = 10_000
            }
            val body = conn.inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
            conn.disconnect()
            parse(body)
        } catch (e: Exception) {
            DebugLog.w(TAG) { "route() failed: ${e.message}" }
            null
        }
    }

    private fun parse(json: String): Route? {
        val root = JSONObject(json)
        if (root.optString("code") != "Ok") {
            DebugLog.w(TAG) { "OSRM code=${root.optString("code")}" }
            return null
        }
        val routes = root.optJSONArray("routes") ?: return null
        if (routes.length() == 0) return null
        val r0 = routes.getJSONObject(0)

        val geometry = PolylineCodec.decode(r0.getString("geometry"))
        if (geometry.size < 2) return null

        // Cumulative distance at each vertex
        val cum = DoubleArray(geometry.size)
        for (i in 1 until geometry.size) {
            cum[i] = cum[i - 1] + GeoPoint.distMeters(geometry[i - 1], geometry[i])
        }

        // Maneuvers and speed limits from steps
        val maneuvers = ArrayList<Maneuver>()
        val speedLimits = ArrayList<SpeedLimitSegment>()
        val legs = r0.optJSONArray("legs")
        if (legs != null) {
            for (li in 0 until legs.length()) {
                val leg = legs.getJSONObject(li)
                // Try to extract per-segment speed annotation from the leg
                val annotation = leg.optJSONObject("annotation")
                val speedArray = annotation?.optJSONArray("speed")

                val steps = leg.optJSONArray("steps") ?: continue
                for (si in 0 until steps.length()) {
                    val step = steps.getJSONObject(si)
                    val man = step.optJSONObject("maneuver") ?: continue
                    val loc = man.optJSONArray("location") ?: continue
                    val p = GeoPoint(loc.getDouble(1), loc.getDouble(0))
                    val type = ManeuverType.fromOsrm(man.optString("type"), man.optString("modifier"))
                    val name = step.optString("name").ifBlank { "road" }

                    // OSRM step-level maxspeed (km/h) if available; otherwise
                    // try to derive from the annotations.speed array (m/s → km/h).
                    val stepMaxspeed = step.optInt("maxspeed", -1).takeIf { it > 0 }
                    val stepCum = nearestCumulative(p, geometry, cum)

                    // Build a speed limit from the step's own speed if available
                    val limitKmh = stepMaxspeed ?: run {
                        // Estimate from the step geometry offset into annotations.speed
                        val geomOffset = step.optInt("geometry_offset", -1)
                        val geomEnd = if (si + 1 < steps.length())
                            steps.getJSONObject(si + 1).optInt("geometry_offset", -1)
                        else -1
                        if (speedArray != null && geomOffset >= 0 && geomEnd > geomOffset) {
                            // Average the segment speeds (m/s) for this step → km/h
                            var sum = 0.0; var count = 0
                            for (ai in geomOffset until minOf(geomEnd, speedArray.length())) {
                                val v = speedArray.optDouble(ai, -1.0)
                                if (v > 0) { sum += v; count++ }
                            }
                            if (count > 0) ((sum / count) * 3.6).toInt() else null
                        } else null
                    }

                    if (limitKmh != null && limitKmh > 0) {
                        speedLimits.add(SpeedLimitSegment(stepCum, limitKmh))
                    }

                    maneuvers.add(
                        Maneuver(
                            type = type,
                            instruction = buildInstruction(type, name),
                            location = p,
                            cumulativeMeters = stepCum,
                            speedLimitKmh = limitKmh,
                        )
                    )
                }
            }
        }

        return Route(
            geometry = geometry,
            maneuvers = maneuvers,
            totalMeters = r0.optDouble("distance", cum.last()),
            totalSeconds = r0.optDouble("duration", 0.0),
            cumulative = cum,
            speedLimits = speedLimits,
        )
    }

    private fun buildInstruction(type: ManeuverType, road: String): String = when (type) {
        ManeuverType.DEPART       -> "Head out on $road"
        ManeuverType.ARRIVE       -> "Arrive at destination"
        ManeuverType.TURN_LEFT    -> "Turn left onto $road"
        ManeuverType.TURN_RIGHT   -> "Turn right onto $road"
        ManeuverType.SLIGHT_LEFT  -> "Slight left onto $road"
        ManeuverType.SLIGHT_RIGHT -> "Slight right onto $road"
        ManeuverType.SHARP_LEFT   -> "Sharp left onto $road"
        ManeuverType.SHARP_RIGHT  -> "Sharp right onto $road"
        ManeuverType.UTURN        -> "Make a U-turn"
        ManeuverType.ROUNDABOUT   -> "At the roundabout, take $road"
        ManeuverType.CONTINUE     -> "Continue on $road"
    }

    /** Cumulative distance of the geometry vertex nearest to a maneuver location. */
    private fun nearestCumulative(p: GeoPoint, geom: List<GeoPoint>, cum: DoubleArray): Double {
        var best = 0.0
        var bestD = Double.MAX_VALUE
        for (i in geom.indices) {
            val d = GeoPoint.distMeters(p, geom[i])
            if (d < bestD) { bestD = d; best = cum[i] }
        }
        return best
    }
}

