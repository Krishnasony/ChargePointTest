package com.chargepoint.fleet.domain.model

/**
 * Represents a single truck assignment to a charger in the schedule.
 *
 * @property truck The truck being charged
 * @property startTime Time in hours when charging starts (relative to schedule start)
 * @property endTime Time in hours when charging completes (relative to schedule start)
 * @property chargeTimeHours Duration of charging in hours
 */
data class ScheduleAssignment(
    val truck: Truck,
    val startTime: Double,
    val endTime: Double,
    val chargeTimeHours: Double
) {
    init {
        require(startTime >= 0) { "Start time must be non-negative" }
        require(endTime > startTime) { "End time must be after start time" }
        require(chargeTimeHours > 0) { "Charge time must be positive" }
    }
}
