package com.yaary.android.mobility.utils

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import java.util.UUID

internal class DeviceUtil {
    @SuppressLint("HardwareIds")
    internal fun getDeviceId(context: Context): String {
        var androidId =
            Settings.Secure.getString(
                context.applicationContext.contentResolver,
                Settings.Secure.ANDROID_ID
            )
        if (androidId.isNullOrEmpty()) {
            androidId = UUID.randomUUID().toString()
        }
        return androidId
    }
}