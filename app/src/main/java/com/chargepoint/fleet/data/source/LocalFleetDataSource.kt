package com.chargepoint.fleet.data.source

import com.chargepoint.fleet.domain.model.Charger
import com.chargepoint.fleet.domain.model.Truck

/**
 * Local data source interface for fleet data.
 * In a production app, this could be implemented with Room, SharedPreferences, or other local storage.
 */
interface LocalFleetDataSource {
    /**
     * Retrieves trucks from local storage.
     */
    suspend fun getTrucks(): List<Truck>

    /**
     * Retrieves chargers from local storage.
     */
    suspend fun getChargers(): List<Charger>

    /**
     * Retrieves the time horizon from local storage.
     */
    suspend fun getTimeHorizonHours(): Int
}
