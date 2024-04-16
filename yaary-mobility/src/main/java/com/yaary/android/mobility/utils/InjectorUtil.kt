package com.yaary.android.mobility.utils

import android.content.Context
import com.yaary.android.mobility.Yaary
import com.yaary.android.mobility.authorization.AuthController
import com.yaary.android.mobility.data.AuthRepository
import com.yaary.android.mobility.data.Preference
import com.yaary.android.mobility.data.PreferenceRepository
import com.yaary.android.mobility.data.RideRepository
import com.yaary.android.mobility.network.GrpcWrapper
import com.yaary.android.mobility.ride_hailing.RideController

internal class InjectorUtil {

    companion object {

        internal fun provideYaaryInstance(
            context: Context,
            rideController: RideController = provideRideController(context.applicationContext)
        ): Yaary {
            return Yaary(context, rideController)
        }

        fun provideAuthController(
            context: Context,
            authRepository: AuthRepository = provideAuthRepository(context.applicationContext),
            preferenceRepository: PreferenceRepository = providePreferenceRepository(context.applicationContext),
        ): AuthController {
            return AuthController(context, authRepository, preferenceRepository)
        }

        private fun provideAuthRepository(
            context: Context,
            grpcWrapper: GrpcWrapper = provideGrpcWrapper(context.applicationContext)
        ): AuthRepository {
            return AuthRepository(grpcWrapper)
        }

        private fun provideGrpcWrapper(
            context: Context,
            baseUrl: String = provideBaseUrl(),
            preferenceRepository: PreferenceRepository = providePreferenceRepository(context.applicationContext),
            networkStateChangeObserver: NetworkStateChangeObserver = provideNetworkStateChangeObserver(
                context.applicationContext
            ),
            deviceUtil: DeviceUtil = provideDeviceUtil()
        ): GrpcWrapper {
            return GrpcWrapper(
                context.applicationContext,
                baseUrl,
                preferenceRepository,
                networkStateChangeObserver,
                deviceUtil
            )
        }

        private fun provideDeviceUtil(): DeviceUtil {
            return DeviceUtil()
        }

        private fun provideNetworkStateChangeObserver(context: Context): NetworkStateChangeObserver {
            return NetworkStateChangeObserver(context.applicationContext)
        }

        private fun providePreferenceRepository(
            context: Context,
            preference: Preference = providePreference(context.applicationContext)
        ): PreferenceRepository {
            return PreferenceRepository(preference)
        }

        private fun providePreference(context: Context): Preference {
            return Preference(context.applicationContext)
        }

        private fun provideBaseUrl(): String {
            return "grpc.preprod.triffy.in"
        }

        fun provideRideController(
            context: Context,
            rideRepository: RideRepository = provideRideRepository(context.applicationContext),
            preferenceRepository: PreferenceRepository = providePreferenceRepository(context.applicationContext)
        ): RideController {
            return RideController(context.applicationContext, rideRepository, preferenceRepository)
        }

        private fun provideRideRepository(
            context: Context,
            grpcWrapper: GrpcWrapper = provideGrpcWrapper(context.applicationContext)
        ): RideRepository {
            return RideRepository(grpcWrapper)
        }
    }
}