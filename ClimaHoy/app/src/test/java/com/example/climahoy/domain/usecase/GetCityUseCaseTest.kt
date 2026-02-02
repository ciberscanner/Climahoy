package com.example.climahoy.domain.usecase

import com.example.climahoy.data.model.CityResponse
import com.example.climahoy.data.network.ApiException
import com.example.climahoy.data.network.ErrorAPI
import com.example.climahoy.data.repository.WeatherRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test


class GetCityUseCaseTest {
    @MockK
    private lateinit var repository: WeatherRepository

    private lateinit var getCityUseCase: GetCityUseCase

    @Before
    fun onBefore() {
        MockKAnnotations.init(this)
        getCityUseCase = GetCityUseCase(repository)
    }

    @Test
    fun `invoke should return list of CityResponse when repository succeeds`() = runTest {
        // Given
        val query = "Man"
        val city = CityResponse(9003016, "Manchester Airport","Manchester","United Kingdom",53.35,-2.27,"manchester-airport-manchester-united-kingdom")
        val cities = listOf(city)

        coEvery { repository.getCities(query) } returns cities

        // When
        val result = getCityUseCase(query)

        // Then
        assertEquals(cities, result)
        coVerify { repository.getCities(query) }
    }

    @Test
    fun `invoke should throw exception when repository throws`() = runTest {
        // Given
        val query = "InvalidQuery"
        val exception = ApiException(code = 404, message = "Not Found", error = ErrorAPI())

        coEvery { repository.getCities(query) } throws exception

        // When & Then
        try {
            getCityUseCase(query)
            fail("Expected ApiException to be thrown")
        } catch (e: ApiException) {
            assertEquals(404, e.code)
            assertEquals("Error 404: Not Found", e.message)
        }

        coVerify { repository.getCities(query) }
    }
}