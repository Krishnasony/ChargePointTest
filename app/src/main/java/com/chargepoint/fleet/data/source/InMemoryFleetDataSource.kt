package com.chargepoint.fleet.data.source

import com.chargepoint.fleet.domain.model.Charger
import com.chargepoint.fleet.domain.model.Truck
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory implementation of LocalFleetDataSource.
 * Provides hardcoded sample data for demonstration purposes.
 * 
 * In a production app, this would be replaced with:
 * - Room database implementation
 * - SharedPreferences/DataStore
 * - Remote API with local caching
 */
@Singleton
class InMemoryFleetDataSource @Inject constructor() : LocalFleetDataSource {

    private val sampleTrucks = listOf(
        Truck(id = "TRUCK-001", batteryCapacityKWh = 200.0, currentChargePercent = 20.0),
        Truck(id = "TRUCK-002", batteryCapacityKWh = 200.0, currentChargePercent = 50.0),
        Truck(id = "TRUCK-003", batteryCapacityKWh = 200.0, currentChargePercent = 10.0),
        Truck(id = "TRUCK-004", batteryCapacityKWh = 180.0, currentChargePercent = 30.0),
        Truck(id = "TRUCK-005", batteryCapacityKWh = 220.0, currentChargePercent = 40.0),
        Truck(id = "TRUCK-006", batteryCapacityKWh = 200.0, currentChargePercent = 15.0),
        Truck(id = "TRUCK-007", batteryCapacityKWh = 190.0, currentChargePercent = 25.0),
        Truck(id = "TRUCK-008", batteryCapacityKWh = 210.0, currentChargePercent = 35.0),
        Truck(id = "TRUCK-009", batteryCapacityKWh = 200.0, currentChargePercent = 45.0),
        Truck(id = "TRUCK-010", batteryCapacityKWh = 200.0, currentChargePercent = 5.0)
    )

    private val sampleChargers = listOf(
        Charger(id = "CHARGER-A", rateKW = 50.0),
        Charger(id = "CHARGER-B", rateKW = 75.0),
        Charger(id = "CHARGER-C", rateKW = 100.0)
    )

    private val sampleTimeHorizon = 8 // 8 hours overnight

    override suspend fun getTrucks(): List<Truck> {
        // Simulate network/database delay
        delay(100)
        return sampleTrucks
    }

    override suspend fun getChargers(): List<Charger> {
        // Simulate network/database delay
        delay(100)
        return sampleChargers
    }

    override suspend fun getTimeHorizonHours(): Int {
        // Simulate network/database delay
        delay(100)
        return sampleTimeHorizon
    }

    /**
     * Allows updating the data at runtime (for testing purposes).
     * In production, this would be handled through proper data management.
     */
    fun updateData(
        trucks: List<Truck>? = null,
        chargers: List<Charger>? = null,
        timeHorizon: Int? = null
    ) {
        // In a real implementation, this would update the database
        // For now, this is just a placeholder for future extensibility
    }
}
