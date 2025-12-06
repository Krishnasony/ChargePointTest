package com.chargepoint.fleet.domain.model

/**
 * Represents a charging station that can charge trucks.
 *
 * @property id Unique identifier for the charger
 * @property rateKW Charging power output in kilowatts
 */
data class Charger(
    val id: String,
    val rateKW: Double
) {
    init {
        require(rateKW > 0) { "Charging rate must be positive" }
    }

    /**
     * Calculates the time required to fully charge a truck.
     *
     * @param truck The truck to charge
     * @return Time in hours needed to fully charge the truck
     */
    fun timeToFullChargeHours(truck: Truck): Double {
        return truck.remainingEnergyKWh() / rateKW
    }
}
