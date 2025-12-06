package com.chargepoint.fleet.domain.usecase

import com.chargepoint.fleet.domain.model.AppError
import com.chargepoint.fleet.domain.model.Charger
import com.chargepoint.fleet.domain.model.Result
import com.chargepoint.fleet.domain.model.ScheduleResult
import com.chargepoint.fleet.domain.model.Truck
import com.chargepoint.fleet.domain.repository.FleetRepository
import com.chargepoint.fleet.domain.scheduler.ChargingScheduler
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GenerateChargingScheduleUseCase.
 */
class GenerateChargingScheduleUseCaseTest {

    private lateinit var useCase: GenerateChargingScheduleUseCase
    private lateinit var repository: FleetRepository
    private lateinit var scheduler: ChargingScheduler

    @Before
    fun setup() {
        repository = mockk()
        scheduler = mockk()
        useCase = GenerateChargingScheduleUseCase(repository, scheduler)
    }

    @Test
    fun `invoke returns success when scheduling succeeds`() = runTest {
        // Given
        val trucks = listOf(Truck("T1", 200.0, 50.0))
        val chargers = listOf(Charger("C1", 50.0))
        val timeHorizon = 8
        val expectedSchedule = mockk<ScheduleResult>()

        coEvery { repository.getTrucks() } returns trucks
        coEvery { repository.getChargers() } returns chargers
        coEvery { repository.getTimeHorizonHours() } returns timeHorizon
        coEvery { scheduler.schedule(trucks, chargers, timeHorizon) } returns expectedSchedule

        // When
        val result = useCase()

        // Then
        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat((result as Result.Success).data).isEqualTo(expectedSchedule)
        coVerify { repository.getTrucks() }
        coVerify { repository.getChargers() }
        coVerify { repository.getTimeHorizonHours() }
        coVerify { scheduler.schedule(trucks, chargers, timeHorizon) }
    }

    @Test
    fun `invoke returns error when no chargers available`() = runTest {
        // Given
        val trucks = listOf(Truck("T1", 200.0, 50.0))
        val chargers = emptyList<Charger>()
        val timeHorizon = 8

        coEvery { repository.getTrucks() } returns trucks
        coEvery { repository.getChargers() } returns chargers
        coEvery { repository.getTimeHorizonHours() } returns timeHorizon

        // When
        val result = useCase()

        // Then
        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).error).isInstanceOf(AppError.DataError::class.java)
    }

    @Test
    fun `invoke returns error when time horizon is invalid`() = runTest {
        // Given
        val trucks = listOf(Truck("T1", 200.0, 50.0))
        val chargers = listOf(Charger("C1", 50.0))
        val timeHorizon = 0

        coEvery { repository.getTrucks() } returns trucks
        coEvery { repository.getChargers() } returns chargers
        coEvery { repository.getTimeHorizonHours() } returns timeHorizon

        // When
        val result = useCase()

        // Then
        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).error).isInstanceOf(AppError.InvalidInput::class.java)
    }

    @Test
    fun `invoke returns error when scheduler throws IllegalArgumentException`() = runTest {
        // Given
        val trucks = listOf(Truck("T1", 200.0, 50.0))
        val chargers = listOf(Charger("C1", 50.0))
        val timeHorizon = 8

        coEvery { repository.getTrucks() } returns trucks
        coEvery { repository.getChargers() } returns chargers
        coEvery { repository.getTimeHorizonHours() } returns timeHorizon
        coEvery { scheduler.schedule(trucks, chargers, timeHorizon) } throws 
            IllegalArgumentException("Invalid input")

        // When
        val result = useCase()

        // Then
        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).error).isInstanceOf(AppError.InvalidInput::class.java)
    }

    @Test
    fun `invoke returns error when repository throws exception`() = runTest {
        // Given
        coEvery { repository.getTrucks() } throws RuntimeException("Database error")

        // When
        val result = useCase()

        // Then
        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).error).isInstanceOf(AppError.CalculationError::class.java)
    }
}
