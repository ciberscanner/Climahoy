package com.example.climahoy.data.repository

import com.example.climahoy.data.model.CityResponse
import com.example.climahoy.data.network.ApiException
import com.example.climahoy.data.network.WeatherApiService
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import retrofit2.Response

class WeatherRepositoryTest {
    @MockK
    private lateinit var apiService: WeatherApiService
    private val apiKey = "123456"
    private lateinit var repository: WeatherRepository

    @Before
    fun onBefore() {
        MockKAnnotations.init(this)
        repository = WeatherRepository(apiService, apiKey)
    }

    @Test
    fun `getCities should return list of CityResponse when response is successful`() = runTest {
        // Given
        val query = "Man"
        val city = CityResponse(9003016, "Manchester Airport","Manchester","United Kingdom",53.35,-2.27,"manchester-airport-manchester-united-kingdom")
        val cities = listOf(city)

        coEvery {
            apiService.getCities(
                apiKey = apiKey,
                query = query
            )
        } returns Response.success(cities)

        // When
        val result = repository.getCities(query)

        // Then
        assertEquals(cities, result)

        coVerify {
            apiService.getCities(
                apiKey = apiKey,
                query = query
            )
        }
    }

    @Test
    fun `getCities should throw ApiException when API returns error`() = runTest {
        // Given
        val query = "InvalidQuery"
        val errorResponse = """
            {
                "error": {
                    "code": 1006,
                    "message": "No matching location found."
                }
            }
        """.trimIndent()

        coEvery {
            apiService.getCities(
                apiKey = apiKey,
                query = query
            )
        } returns Response.error(404, okhttp3.ResponseBody.create(null, errorResponse))

        // When & Then
        try {
            repository.getCities(query)
            fail("Expected ApiException to be thrown")
        } catch (e: ApiException) {
            assertEquals(404, e.code)
            assertNotNull(e.error)
        }
    }
}