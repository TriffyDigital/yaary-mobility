package com.yaary.android.mobility.ride_hailing

import android.content.Context
import com.yaary.android.mobility.data.PreferenceRepository
import com.yaary.android.mobility.data.RideRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

internal class RideController(
    context: Context,
    rideRepository: RideRepository,
    preferenceRepository: PreferenceRepository
) {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
}