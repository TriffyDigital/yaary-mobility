package com.yaary.android.mobility.data

import com.yaary.android.mobility.network.GrpcWrapper
import consumer.auth.authV2.Auth
import consumer.auth.authV2.AuthServiceGrpc
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class AuthRepository(
    private val grpcWrapper: GrpcWrapper
) {
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO
    private val streamStub =
        AuthServiceGrpc.newStub(grpcWrapper.channel)
    private val blockingStub =
        AuthServiceGrpc.newBlockingStub(grpcWrapper.channel)

    suspend fun initSdk(
        bundleId: String,
        clientId: String,
        clientSecret: String
    ): Result<Auth.InitSdkResponse> {
        return withContext(defaultDispatcher) {
            val request = Auth.InitSdkRequest.newBuilder()
                .setBundleId(bundleId)
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .build()

            try {
                // Make the unary gRPC call
                grpcWrapper.makeUnaryCall(
                    request,
                    blockingStub.withInterceptors(grpcWrapper.getAuthInterceptor())::initSdk
                )
            } catch (e: Exception) {
                // Handle gRPC status exceptions
                Result.failure(e)
            }
        }
    }

    suspend fun authorizeUser(phoneNumber: String): Result<Auth.AuthorizeUserRespone> {
        return withContext(defaultDispatcher) {
            val request = Auth.AuthorizeUserRequest.newBuilder()
                .setPhoneNumber(phoneNumber)
                .build()

            try {
                // Make the unary gRPC call
                grpcWrapper.makeUnaryCall(
                    request,
                    blockingStub.withInterceptors(grpcWrapper.getAuthInterceptor())::authorizeUser
                )
            } catch (e: Exception) {
                // Handle gRPC status exceptions
                Result.failure(e)
            }
        }
    }

}