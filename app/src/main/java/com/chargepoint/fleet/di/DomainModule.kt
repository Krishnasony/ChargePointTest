package com.chargepoint.fleet.di

import com.chargepoint.fleet.domain.scheduler.ChargingScheduler
import com.chargepoint.fleet.domain.scheduler.GreedyShortestJobFirstScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing domain layer dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DomainModule {

    @Binds
    @Singleton
    abstract fun bindChargingScheduler(
        greedyShortestJobFirstScheduler: GreedyShortestJobFirstScheduler
    ): ChargingScheduler
}
