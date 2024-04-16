package com.yaary.android.mobility.authorization

import android.content.Context
import com.yaary.android.mobility.data.AuthRepository
import com.yaary.android.mobility.data.PreferenceRepository
import com.yaary.android.mobility.error.Error
import consumer.auth.authV2.Auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

internal class AuthController(
    private val context: Context,
    private val authRepository: AuthRepository,
    private val preferenceRepository: PreferenceRepository
) {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    fun initSdk(
        clientId: String,
        clientSecret: String,
        bundleId: String,
        onSuccess: () -> Unit,
        onFailure: (error: Error) -> Unit
    ) {
        scope.launch(Dispatchers.IO) {
            val result = authRepository.initSdk(
                bundleId, clientId, clientSecret
            )
            scope.launch(Dispatchers.Main) {
                //TODO handle errors
                if (result.isSuccess) {
                    processInitSdkResponse(result.getOrNull())
                    preferenceRepository.saveClientId(clientId)
                    onSuccess()
                } else {
                    onFailure(Error(0, ""))
                }
            }
        }
    }

    private fun processInitSdkResponse(data: Auth.InitSdkResponse?) {
        data?.data?.apiToken?.let {
            preferenceRepository.saveApiToken(it)
        }
    }

    fun authorizeUser(
        phoneNumber: String,
        onSuccess: () -> Unit,
        onFailure: (error: Error) -> Unit
    ) {
        scope.launch(Dispatchers.IO) {
            val result = authRepository.authorizeUser(phoneNumber)
            scope.launch(Dispatchers.Main) {
                //TODO handle errors
                if (result.isSuccess) {
                    processAuthorizeUserRespone(result.getOrNull())
                    onSuccess()
                } else {
                    onFailure(Error(0, ""))
                }
            }
        }
    }

    private fun processAuthorizeUserRespone(data: Auth.AuthorizeUserRespone?) {
        data?.data?.userSessionToken?.let {
            preferenceRepository.saveAccessToken(it)
        }
    }

    fun removeUser() {
        preferenceRepository.resetData()
    }
}