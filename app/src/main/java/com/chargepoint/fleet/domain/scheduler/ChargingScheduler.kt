package com.chargepoint.fleet.domain.scheduler

import com.chargepoint.fleet.domain.model.Charger
import com.chargepoint.fleet.domain.model.ScheduleResult
import com.chargepoint.fleet.domain.model.Truck

/**
 * Interface for charging scheduling algorithms.
 * Allows multiple scheduling strategies to be plugged in.
 */
interface ChargingScheduler {
    /**
     * Generates a charging schedule for the given trucks, chargers, and time horizon.
     *
     * @param trucks List of trucks to schedule
     * @param chargers List of available chargers
     * @param timeHorizonHours Time horizon in hours for scheduling
     * @return The generated schedule result
     * @throws IllegalArgumentException if inputs are invalid
     */
    suspend fun schedule(
        trucks: List<Truck>,
        chargers: List<Charger>,
        timeHorizonHours: Int
    ): ScheduleResult
}
