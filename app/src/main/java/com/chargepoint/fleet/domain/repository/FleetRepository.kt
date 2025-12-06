package com.chargepoint.fleet.domain.repository

import com.chargepoint.fleet.domain.model.Charger
import com.chargepoint.fleet.domain.model.Truck

/**
 * Repository interface for fleet data access.
 * Abstracts the data source from the domain layer.
 */
interface FleetRepository {
    /**
     * Retrieves the list of trucks in the fleet.
     *
     * @return List of trucks
     * @throws Exception if data retrieval fails
     */
    suspend fun getTrucks(): List<Truck>

    /**
     * Retrieves the list of available chargers.
     *
     * @return List of chargers
     * @throws Exception if data retrieval fails
     */
    suspend fun getChargers(): List<Charger>

    /**
     * Retrieves the time horizon for scheduling in hours.
     *
     * @return Time horizon in hours
     * @throws Exception if data retrieval fails
     */
    suspend fun getTimeHorizonHours(): Int
}
