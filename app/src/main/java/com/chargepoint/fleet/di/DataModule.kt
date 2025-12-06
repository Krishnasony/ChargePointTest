package com.chargepoint.fleet.di

import com.chargepoint.fleet.data.repository.FleetRepositoryImpl
import com.chargepoint.fleet.data.source.InMemoryFleetDataSource
import com.chargepoint.fleet.data.source.LocalFleetDataSource
import com.chargepoint.fleet.domain.repository.FleetRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing data layer dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindFleetRepository(
        fleetRepositoryImpl: FleetRepositoryImpl
    ): FleetRepository

    @Binds
    @Singleton
    abstract fun bindLocalFleetDataSource(
        inMemoryFleetDataSource: InMemoryFleetDataSource
    ): LocalFleetDataSource
}
