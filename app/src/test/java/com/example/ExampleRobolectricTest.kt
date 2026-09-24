package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.LocationState
import com.example.data.model.UserLocation
import com.example.data.repository.LocationRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("WeatherGPT", appName)
    }

    @Test
    fun `user location conversion to city location`() {
        val userLocation = UserLocation(
            latitude = 28.6139,
            longitude = 77.2090,
            address = "Connaught Place, New Delhi, Delhi",
            locality = "New Delhi",
            subAdminArea = "New Delhi",
            adminArea = "Delhi",
            postalCode = "110001",
            country = "India"
        )

        val cityLocation = userLocation.toCityLocation()
        assertEquals("New Delhi", cityLocation.name)
        assertEquals("Delhi", cityLocation.state)
        assertEquals(28.6139, cityLocation.latitude, 0.0001)
        assertEquals(77.2090, cityLocation.longitude, 0.0001)
    }

    @Test
    fun `location repository provides indian cities list`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = LocationRepository(context)
        assertTrue(repo.indianCities.isNotEmpty())
        assertNotNull(repo.indianCities.firstOrNull { it.name == "Delhi" })
        assertNotNull(repo.indianCities.firstOrNull { it.name == "Mumbai" })
    }
}
