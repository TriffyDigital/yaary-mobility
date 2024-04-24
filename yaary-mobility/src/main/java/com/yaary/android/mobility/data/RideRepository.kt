package com.yaary.android.mobility.data

import com.yaary.android.mobility.network.GrpcWrapper
import consumer.ride.rideV2.Ride
import consumer.ride.rideV2.RideServiceGrpc
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

internal class RideRepository(private val grpcWrapper: GrpcWrapper) {
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO
    private val streamStub =
        RideServiceGrpc.newStub(grpcWrapper.channel)
    private val blockingStub =
        RideServiceGrpc.newBlockingStub(grpcWrapper.channel)

    suspend fun initiateSearch(request: Ride.GetRideQuotesRequest): Result<Ride.SubmitRideQuoteResponse> {
        return withContext(defaultDispatcher) {
            try {
                // Make the unary gRPC call
                grpcWrapper.makeUnaryCall(
                    request,
                    blockingStub.withInterceptors(grpcWrapper.getAuthInterceptor())::initiateSearch
                )
            } catch (e: Exception) {
                // Handle gRPC status exceptions
                Result.failure(e)
            }
        }
    }

    suspend fun selectQuote(
        request: Ride.SelectQuoteRequest
    ): Result<Ride.SelectQuoteResponse> {
        return withContext(defaultDispatcher) {
            try {
                // Make the unary gRPC call
                grpcWrapper.makeUnaryCall(
                    request,
                    blockingStub.withInterceptors(grpcWrapper.getAuthInterceptor())::selectQuote
                )
            } catch (e: Exception) {
                // Handle gRPC status exceptions
                Result.failure(e)
            }
        }
    }

    suspend fun confirmCancel(
        request: Ride.ConfirmCancelRequest
    ): Result<Ride.ConfirmCancelRespone> {
        return withContext(defaultDispatcher) {
            try {
                // Make the unary gRPC call
                grpcWrapper.makeUnaryCall(
                    request,
                    blockingStub.withInterceptors(grpcWrapper.getAuthInterceptor())::confirmCancel
                )
            } catch (e: Exception) {
                // Handle gRPC status exceptions
                Result.failure(e)
            }
        }
    }

    fun rideUpdateStream(
        request: Ride.RideUpdateRequest
    ): Flow<Result<Ride.RideUpdateResponse>> {
        // Initiating stream.
        return grpcWrapper
            .makeServerStreamCall(
                request,
                streamStub.withInterceptors(grpcWrapper.getAuthInterceptor())::rideUpdateStream
            )
    }

    fun getQuoteCatalogStream(
        request: Ride.QuoteCatalogRequest
    ): Flow<Result<Ride.QuoteCatalogResponse>> {
        // Initiating stream.
        return grpcWrapper
            .makeServerStreamCall(
                request,
                streamStub.withInterceptors(grpcWrapper.getAuthInterceptor())::getQuoteCatalogStream
            )
    }

    fun getRideConfirmationStream(
        request: Ride.GetRideConfirmationRequest
    ): Flow<Result<Ride.GetRideConfirmationResponse>> {
        // Initiating stream.
        return grpcWrapper
            .makeServerStreamCall(
                request,
                streamStub.withInterceptors(grpcWrapper.getAuthInterceptor())::getRideConfirmationStream
            )
    }
}