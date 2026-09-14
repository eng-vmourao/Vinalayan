package com.example.opendash.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationParserTest {
    @Test
    fun parse_acceptsGoogleMapsHostWithCoordinates() {
        val location = LocationParser.parse("Meet here https://maps.google.com/?q=12.9716,77.5946")

        assertEquals("https://maps.google.com/?q=12.9716,77.5946", location.url)
        assertEquals(12.9716, location.lat!!, 0.000001)
        assertEquals(77.5946, location.lng!!, 0.000001)
        assertFalse(location.needsExpansion)
    }

    @Test
    fun parse_acceptsGeoUriWithoutNetworkUrl() {
        val location = LocationParser.parse("geo:12.9716,77.5946")

        assertEquals(12.9716, location.lat!!, 0.000001)
        assertEquals(77.5946, location.lng!!, 0.000001)
        assertEquals("geo:12.9716,77.5946", location.url)
        assertFalse(location.needsExpansion)
    }

    @Test
    fun parse_rejectsHttpMapUrlEvenWithCoordinates() {
        val location = LocationParser.parse("http://maps.google.com/?q=12.9716,77.5946")

        assertNull(location.url)
        assertNull(location.lat)
        assertNull(location.lng)
        assertFalse(location.needsExpansion)
    }

    @Test
    fun parse_rejectsUnknownHttpsHost() {
        val location = LocationParser.parse("https://example.com/?q=12.9716,77.5946")

        assertNull(location.url)
        assertNull(location.lat)
        assertNull(location.lng)
        assertFalse(location.needsExpansion)
    }

    @Test
    fun parse_acceptsGoogleShortMapHostForExpansion() {
        val location = LocationParser.parse("https://maps.app.goo.gl/abc123")

        assertEquals("https://maps.app.goo.gl/abc123", location.url)
        assertNull(location.lat)
        assertNull(location.lng)
        assertEquals("Shared location", location.name)
        assertTrue(location.needsExpansion)
    }

    @Test
    fun parse_acceptsWazeUrlWithCoordinates() {
        val location = LocationParser.parse("Confira este local no Waze: https://waze.com/ul?ll=-23.55052,-46.633308&navigate=yes")

        assertEquals("https://waze.com/ul?ll=-23.55052,-46.633308&navigate=yes", location.url)
        assertEquals(-23.55052, location.lat!!, 0.000001)
        assertEquals(-46.633308, location.lng!!, 0.000001)
        assertFalse(location.needsExpansion)
    }

    @Test
    fun parse_acceptsWazeUrlWithEncodedComma() {
        val location = LocationParser.parse("https://www.waze.com/ul?ll=-23.55052%2C-46.633308&navigate=yes")

        assertEquals("https://www.waze.com/ul?ll=-23.55052%2C-46.633308&navigate=yes", location.url)
        assertEquals(-23.55052, location.lat!!, 0.000001)
        assertEquals(-46.633308, location.lng!!, 0.000001)
        assertFalse(location.needsExpansion)
    }

    @Test
    fun parse_acceptsWazeCustomScheme() {
        val location = LocationParser.parse("waze://?ll=-23.55052,-46.633308&navigate=yes")

        assertEquals("waze://?ll=-23.55052,-46.633308&navigate=yes", location.url)
        assertEquals(-23.55052, location.lat!!, 0.000001)
        assertEquals(-46.633308, location.lng!!, 0.000001)
        assertFalse(location.needsExpansion)
    }

    @Test
    fun parse_acceptsWazeShortLinkForExpansion() {
        val location = LocationParser.parse("https://wz.to/xyz789")

        assertEquals("https://wz.to/xyz789", location.url)
        assertNull(location.lat)
        assertNull(location.lng)
        assertTrue(location.needsExpansion)
    }
}
