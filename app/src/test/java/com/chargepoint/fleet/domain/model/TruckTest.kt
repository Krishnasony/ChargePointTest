package com.chargepoint.fleet.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for Truck model.
 */
class TruckTest {

    @Test
    fun `remainingEnergyKWh calculates correctly`() {
        // Given
        val truck = Truck("T1", 200.0, 50.0)

        // When
        val remaining = truck.remainingEnergyKWh()

        // Then
        assertThat(remaining).isWithin(0.01).of(100.0)
    }

    @Test
    fun `remainingEnergyKWh returns zero for fully charged truck`() {
        // Given
        val truck = Truck("T1", 200.0, 100.0)

        // When
        val remaining = truck.remainingEnergyKWh()

        // Then
        assertThat(remaining).isWithin(0.01).of(0.0)
    }

    @Test
    fun `remainingEnergyKWh returns full capacity for empty truck`() {
        // Given
        val truck = Truck("T1", 200.0, 0.0)

        // When
        val remaining = truck.remainingEnergyKWh()

        // Then
        assertThat(remaining).isWithin(0.01).of(200.0)
    }

    @Test
    fun `isFullyCharged returns true when at 100 percent`() {
        // Given
        val truck = Truck("T1", 200.0, 100.0)

        // When/Then
        assertThat(truck.isFullyCharged()).isTrue()
    }

    @Test
    fun `isFullyCharged returns false when below 100 percent`() {
        // Given
        val truck = Truck("T1", 200.0, 99.9)

        // When/Then
        assertThat(truck.isFullyCharged()).isFalse()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructor throws exception for negative battery capacity`() {
        Truck("T1", -100.0, 50.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructor throws exception for charge percent below 0`() {
        Truck("T1", 200.0, -1.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructor throws exception for charge percent above 100`() {
        Truck("T1", 200.0, 101.0)
    }
}
