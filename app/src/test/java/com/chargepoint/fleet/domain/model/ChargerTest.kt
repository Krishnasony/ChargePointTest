package com.chargepoint.fleet.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for Charger model.
 */
class ChargerTest {

    @Test
    fun `timeToFullChargeHours calculates correctly`() {
        // Given
        val charger = Charger("C1", 50.0)
        val truck = Truck("T1", 200.0, 50.0) // 100 kWh remaining

        // When
        val time = charger.timeToFullChargeHours(truck)

        // Then
        assertThat(time).isWithin(0.01).of(2.0) // 100 kWh / 50 kW = 2 hours
    }

    @Test
    fun `timeToFullChargeHours returns zero for fully charged truck`() {
        // Given
        val charger = Charger("C1", 50.0)
        val truck = Truck("T1", 200.0, 100.0)

        // When
        val time = charger.timeToFullChargeHours(truck)

        // Then
        assertThat(time).isWithin(0.01).of(0.0)
    }

    @Test
    fun `faster charger reduces charge time`() {
        // Given
        val slowCharger = Charger("C1", 50.0)
        val fastCharger = Charger("C2", 100.0)
        val truck = Truck("T1", 200.0, 0.0)

        // When
        val slowTime = slowCharger.timeToFullChargeHours(truck)
        val fastTime = fastCharger.timeToFullChargeHours(truck)

        // Then
        assertThat(slowTime).isWithin(0.01).of(4.0) // 200 / 50
        assertThat(fastTime).isWithin(0.01).of(2.0) // 200 / 100
        assertThat(fastTime).isLessThan(slowTime)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructor throws exception for negative rate`() {
        Charger("C1", -50.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructor throws exception for zero rate`() {
        Charger("C1", 0.0)
    }
}
