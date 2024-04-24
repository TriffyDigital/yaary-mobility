package com.yaary.android.mobility.authorization

import android.content.Context
import com.yaary.android.mobility.data.AuthRepository
import com.yaary.android.mobility.data.PreferenceRepository
import com.yaary.android.mobility.error.Error
import com.yaary.android.mobility.extentions.isSuccess
import consumer.auth.authV2.Auth
import consumer.common.commonV2.Common
import consumer.common.commonV2.Common.StatusCode
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
            if (result.isSuccess) {
                if (result.getOrNull()?.metadata?.isSuccess == true) {
                    processInitSdkResponse(result.getOrNull())
                    preferenceRepository.saveClientId(clientId)
                    launchInMain {
                        onSuccess()
                    }
                } else {
                    launchInMain {
                        onFailure(
                            Error(
                                StatusCode.UNRECOGNIZED.name,
                                result.getOrNull()?.metadata?.statusMessage
                            )
                        )
                    }
                }
            } else {
                launchInMain {
                    onFailure(
                        Error(
                            StatusCode.UNRECOGNIZED.name,
                            result.exceptionOrNull()?.message
                        )
                    )
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
            if (result.isSuccess) {
                if (result.getOrNull()?.metadata?.isSuccess == true) {
                    processAuthorizeUserRespone(result.getOrNull())
                    launchInMain {
                        onSuccess()
                    }
                } else {
                    launchInMain {
                        onFailure(
                            Error(
                                result.getOrNull()?.metadata?.statusCode?.name
                                    ?: StatusCode.UNRECOGNIZED.name,
                                result.getOrNull()?.metadata?.statusMessage
                            )
                        )
                    }
                }
            } else {
                launchInMain {
                    onFailure(
                        Error(
                            StatusCode.UNRECOGNIZED.name,
                            result.exceptionOrNull()?.message
                        )
                    )
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

    private fun launchInMain(fn: () -> Unit) {
        scope.launch(Dispatchers.Main) {
            fn()
        }
    }
}