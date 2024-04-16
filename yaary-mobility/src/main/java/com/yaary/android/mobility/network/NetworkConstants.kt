package com.yaary.android.mobility.network

import android.os.Build

/**
 * Created by amutha_c on 10,September,2023
 */
internal object NetworkConstants {
    const val KEY_DEVICE_ID = "X-Device-Id"
    const val KEY_DEVICE_PLATFORM = "X-Device-Platform"
    const val KEY_APP_VERSION = "X-App-Version"
    const val KEY_DEVICE_MODEL = "X-Device-Model"
    const val KEY_REQUEST_ID = "X-Request-Id"
    const val X_API_TOKEN = "X-Api-Token"
    const val AUTHORIZATION = "authorization"
    const val X_CLIENT_ID = "X-Client-Id"

    // Header value
    val deviceModel: String = Build.MODEL
    const val devicePlatform = "android"
    const val HTTP_HEADER_BEARER_AUTH_TOKEN_PREFIX = "Bearer "
}