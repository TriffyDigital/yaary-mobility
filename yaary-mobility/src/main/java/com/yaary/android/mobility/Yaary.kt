package com.yaary.android.mobility

import android.content.Context
import com.yaary.android.mobility.enviroment.Environment
import com.yaary.android.mobility.error.Error
import com.yaary.android.mobility.ride_hailing.RideController
import com.yaary.android.mobility.utils.InjectorUtil

class Yaary internal constructor(context: Context, rideController: RideController) {

    companion object {
        fun initialise(
            context: Context,
            clientId: String,
            clientSecret: String,
            bundleId: String,
            environment: Environment,
            listener: OnSdkInitialiseResultListener
        ) {
            attemptSdkInitialization(
                context,
                clientId,
                clientSecret,
                bundleId,
                listener
            )
        }

        private fun attemptSdkInitialization(
            context: Context,
            clientId: String,
            clientSecret: String,
            bundleId: String,
            listener: OnSdkInitialiseResultListener
        ) {
            val authController = InjectorUtil.provideAuthController(context.applicationContext)
            authController.initSdk(
                clientId,
                clientSecret,
                bundleId,
                onSuccess = {
                    listener.onSuccess()
                },
                onFailure = {
                    listener.onFailure(it)
                }
            )
        }

        fun authorize(
            context: Context,
            phoneNumber: String,
            listener: OnAuthorizeUserResultListener
        ) {
            attemptAuthoriseUser(context, phoneNumber, listener)
        }

        private fun attemptAuthoriseUser(
            context: Context,
            phoneNumber: String,
            listener: OnAuthorizeUserResultListener
        ) {
            val authController = InjectorUtil.provideAuthController(context.applicationContext)
            authController.authorizeUser(phoneNumber,
                onSuccess = {
                    listener.onSuccess(InjectorUtil.provideYaaryInstance(context.applicationContext))
                },
                onFailure = {
                    listener.onFailure(it)
                }
            )
        }

        fun removeUser(context: Context) {
            val authController = InjectorUtil.provideAuthController(context.applicationContext)
            authController.removeUser()
        }
    }

    interface OnSdkInitialiseResultListener {
        fun onSuccess()

        fun onFailure(error: Error)
    }

    interface OnAuthorizeUserResultListener {
        fun onSuccess(instance: Yaary)

        fun onFailure(error: Error)
    }
}