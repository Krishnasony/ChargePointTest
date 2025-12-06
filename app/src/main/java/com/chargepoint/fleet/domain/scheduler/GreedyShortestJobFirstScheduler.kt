package com.chargepoint.fleet.domain.scheduler

import com.chargepoint.fleet.domain.model.Charger
import com.chargepoint.fleet.domain.model.ChargerSchedule
import com.chargepoint.fleet.domain.model.ScheduleAssignment
import com.chargepoint.fleet.domain.model.ScheduleResult
import com.chargepoint.fleet.domain.model.Truck
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Greedy scheduling algorithm based on Shortest Job First (SJF) approach.
 *
 * Algorithm:
 * 1. For each truck, compute the minimum time to full charge across all chargers
 * 2. Filter out trucks that cannot be fully charged within the time horizon
 * 3. Sort remaining trucks by minimum charge time (shortest first)
 * 4. For each truck in order, assign to the best available charger that can fit it
 *
 * This maximizes the number of trucks fully charged within the time horizon.
 */
class GreedyShortestJobFirstScheduler @Inject constructor() : ChargingScheduler {

    override suspend fun schedule(
        trucks: List<Truck>,
        chargers: List<Charger>,
        timeHorizonHours: Int
    ): ScheduleResult = withContext(Dispatchers.Default) {
        // Validate inputs
        require(timeHorizonHours > 0) { "Time horizon must be positive" }
        require(chargers.isNotEmpty()) { "At least one charger must be available" }

        // Handle edge cases
        if (trucks.isEmpty()) {
            return@withContext createEmptySchedule(chargers, timeHorizonHours)
        }

        // Filter out already fully charged trucks
        val trucksToCharge = trucks.filter { !it.isFullyCharged() }
        val alreadyChargedCount = trucks.size - trucksToCharge.size

        if (trucksToCharge.isEmpty()) {
            return@withContext ScheduleResult(
                chargerSchedules = chargers.associate { 
                    it.id to ChargerSchedule(it.id, emptyList(), 0.0) 
                },
                fullyChargedTrucksCount = trucks.size,
                totalTrucks = trucks.size,
                timeHorizonHours = timeHorizonHours,
                unassignedTrucks = emptyList()
            )
        }

        // Step 1 & 2: Compute best time for each truck and filter by time horizon
        val eligibleTrucks = trucksToCharge.mapNotNull { truck ->
            val bestTime = chargers.minOf { charger -> 
                timeToFullChargeHours(truck, charger) 
            }
            if (bestTime <= timeHorizonHours) {
                TruckWithBestTime(truck, bestTime)
            } else {
                null
            }
        }

        // Step 3: Sort by best time ascending (shortest job first)
        val sortedTrucks = eligibleTrucks.sortedBy { it.bestTime }

        // Track current usage time for each charger
        val chargerUsage = chargers.associate { it.id to 0.0 }.toMutableMap()
        
        // Track assignments for each charger
        val chargerAssignments = chargers.associate { 
            it.id to mutableListOf<ScheduleAssignment>() 
        }.toMutableMap()

        val assignedTrucks = mutableSetOf<String>()
        val unassignedTrucks = mutableListOf<Truck>()

        // Step 4: Assign each truck to best available charger
        for (truckWithTime in sortedTrucks) {
            val truck = truckWithTime.truck
            val assignment = findBestChargerAssignment(
                truck,
                chargers,
                chargerUsage,
                timeHorizonHours
            )

            if (assignment != null) {
                val (charger, scheduleAssignment) = assignment
                chargerAssignments[charger.id]?.add(scheduleAssignment)
                chargerUsage[charger.id] = scheduleAssignment.endTime
                assignedTrucks.add(truck.id)
            } else {
                unassignedTrucks.add(truck)
            }
        }

        // Add unassigned trucks that were filtered out in step 2
        val trucksFilteredByHorizon = trucksToCharge.filter { truck ->
            eligibleTrucks.none { it.truck.id == truck.id }
        }
        unassignedTrucks.addAll(trucksFilteredByHorizon)

        // Build final result
        val chargerSchedules = chargers.associate { charger ->
            val assignments = chargerAssignments[charger.id] ?: emptyList()
            charger.id to ChargerSchedule(
                chargerId = charger.id,
                assignments = assignments,
                totalScheduledTime = chargerUsage[charger.id] ?: 0.0
            )
        }

        ScheduleResult(
            chargerSchedules = chargerSchedules,
            fullyChargedTrucksCount = assignedTrucks.size + alreadyChargedCount,
            totalTrucks = trucks.size,
            timeHorizonHours = timeHorizonHours,
            unassignedTrucks = unassignedTrucks
        )
    }

    /**
     * Finds the best charger to assign a truck to.
     * Returns the charger and schedule assignment, or null if no charger can fit the truck.
     */
    private fun findBestChargerAssignment(
        truck: Truck,
        chargers: List<Charger>,
        chargerUsage: Map<String, Double>,
        timeHorizonHours: Int
    ): Pair<Charger, ScheduleAssignment>? {
        var bestCharger: Charger? = null
        var bestAssignment: ScheduleAssignment? = null
        var bestEndTime = Double.MAX_VALUE

        for (charger in chargers) {
            val currentUsage = chargerUsage[charger.id] ?: 0.0
            val timeToFull = timeToFullChargeHours(truck, charger)
            val endTime = currentUsage + timeToFull

            // Check if this charger can fit the truck within the time horizon
            if (endTime <= timeHorizonHours && endTime < bestEndTime) {
                bestCharger = charger
                bestEndTime = endTime
                bestAssignment = ScheduleAssignment(
                    truck = truck,
                    startTime = currentUsage,
                    endTime = endTime,
                    chargeTimeHours = timeToFull
                )
            }
        }

        return if (bestCharger != null && bestAssignment != null) {
            Pair(bestCharger, bestAssignment)
        } else {
            null
        }
    }

    /**
     * Creates an empty schedule when there are no trucks.
     */
    private fun createEmptySchedule(
        chargers: List<Charger>,
        timeHorizonHours: Int
    ): ScheduleResult {
        return ScheduleResult(
            chargerSchedules = chargers.associate { 
                it.id to ChargerSchedule(it.id, emptyList(), 0.0) 
            },
            fullyChargedTrucksCount = 0,
            totalTrucks = 0,
            timeHorizonHours = timeHorizonHours,
            unassignedTrucks = emptyList()
        )
    }

    /**
     * Calculates the time required to fully charge a truck on a charger.
     */
    private fun timeToFullChargeHours(truck: Truck, charger: Charger): Double {
        return charger.timeToFullChargeHours(truck)
    }

    /**
     * Helper data class to store a truck with its best (minimum) charge time.
     */
    private data class TruckWithBestTime(
        val truck: Truck,
        val bestTime: Double
    )
}
