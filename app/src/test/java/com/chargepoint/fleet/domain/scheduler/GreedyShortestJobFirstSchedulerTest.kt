package com.chargepoint.fleet.domain.scheduler

import com.chargepoint.fleet.domain.model.Charger
import com.chargepoint.fleet.domain.model.Truck
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GreedyShortestJobFirstScheduler.
 */
class GreedyShortestJobFirstSchedulerTest {

    private lateinit var scheduler: GreedyShortestJobFirstScheduler

    @Before
    fun setup() {
        scheduler = GreedyShortestJobFirstScheduler()
    }

    @Test
    fun `schedule with empty trucks list returns empty schedule`() = runTest {
        // Given
        val trucks = emptyList<Truck>()
        val chargers = listOf(Charger("C1", 50.0))
        val timeHorizon = 8

        // When
        val result = scheduler.schedule(trucks, chargers, timeHorizon)

        // Then
        assertThat(result.totalTrucks).isEqualTo(0)
        assertThat(result.fullyChargedTrucksCount).isEqualTo(0)
        assertThat(result.unassignedTrucks).isEmpty()
        assertThat(result.chargerSchedules).hasSize(1)
        assertThat(result.chargerSchedules["C1"]?.assignments).isEmpty()
    }

    @Test
    fun `schedule with single truck and single charger within horizon`() = runTest {
        // Given
        val trucks = listOf(Truck("T1", 100.0, 50.0)) // 50 kWh remaining
        val chargers = listOf(Charger("C1", 50.0)) // 1 hour to charge
        val timeHorizon = 8

        // When
        val result = scheduler.schedule(trucks, chargers, timeHorizon)

        // Then
        assertThat(result.fullyChargedTrucksCount).isEqualTo(1)
        assertThat(result.unassignedTrucks).isEmpty()
        assertThat(result.chargerSchedules["C1"]?.assignments).hasSize(1)
        assertThat(result.chargerSchedules["C1"]?.assignments?.first()?.truck?.id).isEqualTo("T1")
        assertThat(result.chargerSchedules["C1"]?.totalScheduledTime).isEqualTo(1.0)
    }

    @Test
    fun `schedule prioritizes shortest charge time first`() = runTest {
        // Given
        val trucks = listOf(
            Truck("T1", 200.0, 50.0), // 100 kWh remaining = 2 hours at 50kW
            Truck("T2", 100.0, 50.0), // 50 kWh remaining = 1 hour at 50kW
            Truck("T3", 150.0, 50.0)  // 75 kWh remaining = 1.5 hours at 50kW
        )
        val chargers = listOf(Charger("C1", 50.0))
        val timeHorizon = 8

        // When
        val result = scheduler.schedule(trucks, chargers, timeHorizon)

        // Then
        assertThat(result.fullyChargedTrucksCount).isEqualTo(3)
        val assignments = result.chargerSchedules["C1"]?.assignments
        assertThat(assignments).hasSize(3)
        // Should be scheduled in order: T2 (1h), T3 (1.5h), T1 (2h)
        assertThat(assignments?.get(0)?.truck?.id).isEqualTo("T2")
        assertThat(assignments?.get(1)?.truck?.id).isEqualTo("T3")
        assertThat(assignments?.get(2)?.truck?.id).isEqualTo("T1")
    }

    @Test
    fun `schedule distributes trucks across multiple chargers`() = runTest {
        // Given
        val trucks = listOf(
            Truck("T1", 200.0, 0.0),  // 200 kWh = 4 hours at 50kW
            Truck("T2", 200.0, 0.0),  // 200 kWh = 4 hours at 50kW
        )
        val chargers = listOf(
            Charger("C1", 50.0),
            Charger("C2", 50.0)
        )
        val timeHorizon = 8

        // When
        val result = scheduler.schedule(trucks, chargers, timeHorizon)

        // Then
        assertThat(result.fullyChargedTrucksCount).isEqualTo(2)
        assertThat(result.unassignedTrucks).isEmpty()
        // Each charger should have one truck
        assertThat(result.chargerSchedules["C1"]?.assignments).hasSize(1)
        assertThat(result.chargerSchedules["C2"]?.assignments).hasSize(1)
    }

    @Test
    fun `schedule excludes trucks that cannot fit in time horizon`() = runTest {
        // Given
        val trucks = listOf(
            Truck("T1", 200.0, 0.0),  // 200 kWh = 4 hours at 50kW (fits)
            Truck("T2", 500.0, 0.0)   // 500 kWh = 10 hours at 50kW (doesn't fit)
        )
        val chargers = listOf(Charger("C1", 50.0))
        val timeHorizon = 8

        // When
        val result = scheduler.schedule(trucks, chargers, timeHorizon)

        // Then
        assertThat(result.fullyChargedTrucksCount).isEqualTo(1)
        assertThat(result.unassignedTrucks).hasSize(1)
        assertThat(result.unassignedTrucks[0].id).isEqualTo("T2")
        assertThat(result.chargerSchedules["C1"]?.assignments).hasSize(1)
        assertThat(result.chargerSchedules["C1"]?.assignments?.first()?.truck?.id).isEqualTo("T1")
    }

    @Test
    fun `schedule assigns truck to faster charger when available`() = runTest {
        // Given
        val trucks = listOf(Truck("T1", 200.0, 0.0)) // 200 kWh
        val chargers = listOf(
            Charger("C1", 50.0),   // 4 hours
            Charger("C2", 100.0)   // 2 hours (faster)
        )
        val timeHorizon = 8

        // When
        val result = scheduler.schedule(trucks, chargers, timeHorizon)

        // Then
        assertThat(result.fullyChargedTrucksCount).isEqualTo(1)
        // Should prefer faster charger C2
        assertThat(result.chargerSchedules["C2"]?.assignments).hasSize(1)
        assertThat(result.chargerSchedules["C2"]?.assignments?.first()?.truck?.id).isEqualTo("T1")
        assertThat(result.chargerSchedules["C1"]?.assignments).isEmpty()
    }

    @Test
    fun `schedule handles already fully charged trucks`() = runTest {
        // Given
        val trucks = listOf(
            Truck("T1", 200.0, 100.0), // Already fully charged
            Truck("T2", 200.0, 50.0)   // Needs charging
        )
        val chargers = listOf(Charger("C1", 50.0))
        val timeHorizon = 8

        // When
        val result = scheduler.schedule(trucks, chargers, timeHorizon)

        // Then
        assertThat(result.totalTrucks).isEqualTo(2)
        assertThat(result.fullyChargedTrucksCount).isEqualTo(2) // Both are/will be fully charged
        assertThat(result.chargerSchedules["C1"]?.assignments).hasSize(1) // Only T2 needs charging
        assertThat(result.chargerSchedules["C1"]?.assignments?.first()?.truck?.id).isEqualTo("T2")
    }

    @Test
    fun `schedule fills charger sequentially without gaps`() = runTest {
        // Given
        val trucks = listOf(
            Truck("T1", 100.0, 50.0), // 50 kWh = 1 hour
            Truck("T2", 100.0, 50.0), // 50 kWh = 1 hour
            Truck("T3", 100.0, 50.0)  // 50 kWh = 1 hour
        )
        val chargers = listOf(Charger("C1", 50.0))
        val timeHorizon = 8

        // When
        val result = scheduler.schedule(trucks, chargers, timeHorizon)

        // Then
        assertThat(result.fullyChargedTrucksCount).isEqualTo(3)
        val assignments = result.chargerSchedules["C1"]?.assignments
        assertThat(assignments).hasSize(3)
        // Check no gaps: each starts when previous ends
        assertThat(assignments?.get(0)?.startTime).isEqualTo(0.0)
        assertThat(assignments?.get(0)?.endTime).isEqualTo(1.0)
        assertThat(assignments?.get(1)?.startTime).isEqualTo(1.0)
        assertThat(assignments?.get(1)?.endTime).isEqualTo(2.0)
        assertThat(assignments?.get(2)?.startTime).isEqualTo(2.0)
        assertThat(assignments?.get(2)?.endTime).isEqualTo(3.0)
    }

    @Test
    fun `schedule maximizes number of fully charged trucks`() = runTest {
        // Given
        val trucks = listOf(
            Truck("T1", 100.0, 90.0), // 10 kWh = 0.2 hours (very quick)
            Truck("T2", 100.0, 80.0), // 20 kWh = 0.4 hours (quick)
            Truck("T3", 200.0, 0.0),  // 200 kWh = 4 hours (long)
        )
        val chargers = listOf(Charger("C1", 50.0))
        val timeHorizon = 5

        // When
        val result = scheduler.schedule(trucks, chargers, timeHorizon)

        // Then
        assertThat(result.fullyChargedTrucksCount).isEqualTo(3)
        val assignments = result.chargerSchedules["C1"]?.assignments
        // Should prioritize T1, T2 first (shorter), then T3
        assertThat(assignments?.get(0)?.truck?.id).isEqualTo("T1")
        assertThat(assignments?.get(1)?.truck?.id).isEqualTo("T2")
        assertThat(assignments?.get(2)?.truck?.id).isEqualTo("T3")
        assertThat(result.chargerSchedules["C1"]?.totalScheduledTime).isWithin(0.01).of(4.6)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `schedule throws exception for negative time horizon`() = runTest {
        // Given
        val trucks = listOf(Truck("T1", 100.0, 50.0))
        val chargers = listOf(Charger("C1", 50.0))
        val timeHorizon = -1

        // When/Then
        scheduler.schedule(trucks, chargers, timeHorizon)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `schedule throws exception for empty chargers list`() = runTest {
        // Given
        val trucks = listOf(Truck("T1", 100.0, 50.0))
        val chargers = emptyList<Charger>()
        val timeHorizon = 8

        // When/Then
        scheduler.schedule(trucks, chargers, timeHorizon)
    }

    @Test
    fun `schedule calculates correct charge times and assignments`() = runTest {
        // Given
        val truck = Truck("T1", 200.0, 25.0) // 150 kWh remaining
        val charger = Charger("C1", 75.0) // 2 hours to charge
        val trucks = listOf(truck)
        val chargers = listOf(charger)
        val timeHorizon = 8

        // When
        val result = scheduler.schedule(trucks, chargers, timeHorizon)

        // Then
        val assignment = result.chargerSchedules["C1"]?.assignments?.first()
        assertThat(assignment?.chargeTimeHours).isWithin(0.01).of(2.0)
        assertThat(assignment?.startTime).isEqualTo(0.0)
        assertThat(assignment?.endTime).isWithin(0.01).of(2.0)
    }

    @Test
    fun `schedule with complex scenario`() = runTest {
        // Given - realistic overnight charging scenario
        val trucks = listOf(
            Truck("T1", 200.0, 20.0),  // 160 kWh
            Truck("T2", 200.0, 50.0),  // 100 kWh
            Truck("T3", 200.0, 10.0),  // 180 kWh
            Truck("T4", 180.0, 30.0),  // 126 kWh
            Truck("T5", 220.0, 40.0),  // 132 kWh
        )
        val chargers = listOf(
            Charger("C1", 50.0),   // Slow charger
            Charger("C2", 75.0),   // Medium charger
            Charger("C3", 100.0)   // Fast charger
        )
        val timeHorizon = 8 // 8 hours overnight

        // When
        val result = scheduler.schedule(trucks, chargers, timeHorizon)

        // Then
        assertThat(result.totalTrucks).isEqualTo(5)
        assertThat(result.fullyChargedTrucksCount).isAtLeast(3)
        assertThat(result.timeHorizonHours).isEqualTo(8)
        
        // Verify all scheduled times are within horizon
        result.chargerSchedules.values.forEach { schedule ->
            assertThat(schedule.totalScheduledTime).isAtMost(timeHorizon.toDouble())
            schedule.assignments.forEach { assignment ->
                assertThat(assignment.endTime).isAtMost(timeHorizon.toDouble())
            }
        }
    }
}
