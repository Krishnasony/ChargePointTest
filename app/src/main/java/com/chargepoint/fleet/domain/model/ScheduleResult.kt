package com.chargepoint.fleet.domain.model

/**
 * Represents the complete charging schedule for all chargers.
 *
 * @property chargerSchedules Map of charger ID to its list of scheduled truck assignments
 * @property fullyChargedTrucksCount Number of trucks that will be fully charged
 * @property totalTrucks Total number of trucks in the fleet
 * @property timeHorizonHours The time horizon used for scheduling
 * @property unassignedTrucks List of trucks that could not be fully charged within the time horizon
 */
data class ScheduleResult(
    val chargerSchedules: Map<String, ChargerSchedule>,
    val fullyChargedTrucksCount: Int,
    val totalTrucks: Int,
    val timeHorizonHours: Int,
    val unassignedTrucks: List<Truck>
) {
    init {
        require(fullyChargedTrucksCount >= 0) { "Fully charged trucks count must be non-negative" }
        require(totalTrucks >= 0) { "Total trucks must be non-negative" }
        require(timeHorizonHours > 0) { "Time horizon must be positive" }
        require(fullyChargedTrucksCount + unassignedTrucks.size == totalTrucks) {
            "Sum of charged and unassigned trucks must equal total trucks"
        }
    }

    /**
     * Calculates the overall utilization percentage of the scheduling.
     *
     * @return Percentage of trucks that will be fully charged (0-100)
     */
    fun utilizationPercent(): Double {
        return if (totalTrucks > 0) {
            (fullyChargedTrucksCount.toDouble() / totalTrucks) * 100.0
        } else {
            0.0
        }
    }
}

/**
 * Represents the schedule for a single charger.
 *
 * @property chargerId The ID of the charger
 * @property assignments Ordered list of truck assignments for this charger
 * @property totalScheduledTime Total time in hours this charger will be occupied
 */
data class ChargerSchedule(
    val chargerId: String,
    val assignments: List<ScheduleAssignment>,
    val totalScheduledTime: Double
) {
    init {
        require(totalScheduledTime >= 0) { "Total scheduled time must be non-negative" }
    }

    /**
     * Calculates the utilization of this charger within the time horizon.
     *
     * @param timeHorizonHours The total time horizon
     * @return Utilization percentage (0-100)
     */
    fun utilizationPercent(timeHorizonHours: Int): Double {
        return if (timeHorizonHours > 0) {
            (totalScheduledTime / timeHorizonHours) * 100.0
        } else {
            0.0
        }
    }
}
