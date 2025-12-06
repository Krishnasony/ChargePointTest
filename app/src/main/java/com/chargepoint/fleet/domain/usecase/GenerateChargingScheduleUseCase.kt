package com.chargepoint.fleet.domain.usecase

import com.chargepoint.fleet.domain.model.AppError
import com.chargepoint.fleet.domain.model.Result
import com.chargepoint.fleet.domain.model.ScheduleResult
import com.chargepoint.fleet.domain.repository.FleetRepository
import com.chargepoint.fleet.domain.scheduler.ChargingScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case for generating a charging schedule for the fleet.
 * 
 * This use case orchestrates the data retrieval from the repository
 * and delegates the scheduling logic to the ChargingScheduler implementation.
 * It handles errors and wraps the result in a Result type.
 */
class GenerateChargingScheduleUseCase @Inject constructor(
    private val fleetRepository: FleetRepository,
    private val chargingScheduler: ChargingScheduler
) {
    /**
     * Executes the use case to generate a charging schedule.
     *
     * @return Result containing the schedule or an error
     */
    suspend operator fun invoke(): Result<ScheduleResult> = withContext(Dispatchers.IO) {
        try {
            // Retrieve data from repository
            val trucks = fleetRepository.getTrucks()
            val chargers = fleetRepository.getChargers()
            val timeHorizonHours = fleetRepository.getTimeHorizonHours()

            // Validate inputs
            if (chargers.isEmpty()) {
                return@withContext Result.Error(
                    AppError.DataError("No chargers available")
                )
            }

            if (timeHorizonHours <= 0) {
                return@withContext Result.Error(
                    AppError.InvalidInput("Time horizon must be positive")
                )
            }

            // Generate schedule
            val schedule = chargingScheduler.schedule(
                trucks = trucks,
                chargers = chargers,
                timeHorizonHours = timeHorizonHours
            )

            Result.Success(schedule)
        } catch (e: IllegalArgumentException) {
            Result.Error(AppError.InvalidInput(e.message ?: "Invalid input"))
        } catch (e: Exception) {
            Result.Error(
                AppError.CalculationError(
                    e.message ?: "Failed to generate schedule"
                )
            )
        }
    }
}
