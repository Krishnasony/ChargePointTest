package com.chargepoint.fleet.data.repository

import com.chargepoint.fleet.data.source.LocalFleetDataSource
import com.chargepoint.fleet.domain.model.Charger
import com.chargepoint.fleet.domain.model.Truck
import com.chargepoint.fleet.domain.repository.FleetRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of FleetRepository.
 * 
 * This repository coordinates data retrieval from various sources.
 * Currently uses only local data source, but can be extended to include:
 * - Remote API calls
 * - Local caching strategies
 * - Data synchronization
 * - Offline support
 */
@Singleton
class FleetRepositoryImpl @Inject constructor(
    private val localDataSource: LocalFleetDataSource
) : FleetRepository {

    override suspend fun getTrucks(): List<Truck> {
        return try {
            localDataSource.getTrucks()
        } catch (e: Exception) {
            throw DataRetrievalException("Failed to retrieve trucks", e)
        }
    }

    override suspend fun getChargers(): List<Charger> {
        return try {
            localDataSource.getChargers()
        } catch (e: Exception) {
            throw DataRetrievalException("Failed to retrieve chargers", e)
        }
    }

    override suspend fun getTimeHorizonHours(): Int {
        return try {
            localDataSource.getTimeHorizonHours()
        } catch (e: Exception) {
            throw DataRetrievalException("Failed to retrieve time horizon", e)
        }
    }
}

/**
 * Exception thrown when data retrieval fails.
 */
class DataRetrievalException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
