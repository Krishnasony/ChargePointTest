package com.chargepoint.fleet

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for the Fleet Charging app.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class FleetChargingApp : Application()
