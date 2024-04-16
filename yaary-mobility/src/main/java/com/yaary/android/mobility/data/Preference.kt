package com.yaary.android.mobility.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

internal class Preference(context: Context) {
    private var preference: SharedPreferences

    init {
        preference =
            context.getSharedPreferences("com.yaary.android.mobility", Context.MODE_PRIVATE)
    }

    fun edit(
        commit: Boolean = false,
        action: SharedPreferences.Editor.() -> Unit
    ) {
        try {
            preference.edit(commit) {
                action()
            }
        } catch (_: Exception) {
            // ignore
        }
    }

    fun getString(key: String): String? {
        return preference.getString(key, null)
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return preference.getBoolean(key, defaultValue)
    }

    fun getLong(key: String, defaultValue: Long = 0): Long {
        return preference.getLong(key, defaultValue)
    }


    fun getInt(key: String, defaultValue: Int = 0): Int {
        return preference.getInt(key, defaultValue)
    }

    fun getStringSet(key: String, defaultValue: Set<String>? = null): Set<String>? {
        return preference.getStringSet(key, defaultValue)
    }

    object Key {
        const val INSTALLATION_ID = "installation_id"
        const val PHONE_NUMBER = "phoneNumber"
        const val ACCESS_TOKEN = "accessToken"
        const val API_TOKEN = "apiToken"
        const val CLIENT_ID = "client_id"
    }
}

internal fun Preference.getInstallationId(): String {
    return getString(Preference.Key.INSTALLATION_ID) ?: ""
}

internal fun Preference.saveInstallationId(phoneNo: String?) {
    edit {
        putString(Preference.Key.INSTALLATION_ID, phoneNo)
    }
}

internal fun Preference.clear() {
    edit {
        clear()
    }
}

internal fun Preference.getPhoneNo(): String {
    return getString(Preference.Key.PHONE_NUMBER) ?: ""
}

internal fun Preference.savePhoneNo(phoneNo: String?) {
    edit {
        putString(Preference.Key.PHONE_NUMBER, phoneNo)
    }
}

internal fun Preference.getAccessToken(): String? {
    return getString(Preference.Key.ACCESS_TOKEN)
}

internal fun Preference.saveAccessToken(token: String?) {
    edit {
        putString(Preference.Key.ACCESS_TOKEN, token)
    }
}

internal fun Preference.getApiToken(): String? {
    return getString(Preference.Key.API_TOKEN)
}

internal fun Preference.saveApiToken(token: String?) {
    edit {
        putString(Preference.Key.API_TOKEN, token)
    }
}

internal fun Preference.getClientId(): String? {
    return getString(Preference.Key.CLIENT_ID)
}

internal fun Preference.saveClientId(token: String?) {
    edit {
        putString(Preference.Key.CLIENT_ID, token)
    }
}