package com.chargepoint.fleet.domain.model

/**
 * Represents an electric truck in the fleet.
 *
 * @property id Unique identifier for the truck
 * @property batteryCapacityKWh Total battery capacity in kilowatt-hours
 * @property currentChargePercent Current state of charge as a percentage (0-100)
 */
data class Truck(
    val id: String,
    val batteryCapacityKWh: Double,
    val currentChargePercent: Double
) {
    init {
        require(batteryCapacityKWh > 0) { "Battery capacity must be positive" }
        require(currentChargePercent in 0.0..100.0) {
            "Current charge percent must be between 0 and 100"
        }
    }

    /**
     * Calculates the remaining energy needed to fully charge this truck.
     *
     * @return Remaining energy in kWh
     */
    fun remainingEnergyKWh(): Double {
        val remainingFraction = 1.0 - (currentChargePercent / 100.0)
        return batteryCapacityKWh * remainingFraction
    }

    /**
     * Checks if the truck is already fully charged.
     */
    fun isFullyCharged(): Boolean = currentChargePercent >= 100.0
}
