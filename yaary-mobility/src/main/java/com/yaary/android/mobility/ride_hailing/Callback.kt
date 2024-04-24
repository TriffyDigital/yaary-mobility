package com.yaary.android.mobility.ride_hailing

import com.yaary.android.mobility.error.Error

interface Callback {
    fun <R> onSuccess(response: R)
    fun onError(error: Error)
}