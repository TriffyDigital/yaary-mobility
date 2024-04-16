package com.yaary.android.mobility.data

internal class PreferenceRepository(private val preference: Preference) {
    fun getPhoneNo(): String {
        return preference.getPhoneNo()
    }

    fun savePhoneNo(phoneNo: String?) {
        preference.savePhoneNo(phoneNo)
    }

    fun getAccessToken(): String? {
        return preference.getAccessToken()
    }

    fun saveAccessToken(token: String?) {
        preference.saveAccessToken(token)
    }

    fun getApiToken(): String? {
        return preference.getApiToken()
    }

    fun saveApiToken(token: String?) {
        preference.saveApiToken(token)
    }

    fun getClientId(): String? {
        return preference.getClientId()
    }

    fun saveClientId(token: String?) {
        preference.saveClientId(token)
    }

    fun resetData() {
        preference.clear()
    }
}