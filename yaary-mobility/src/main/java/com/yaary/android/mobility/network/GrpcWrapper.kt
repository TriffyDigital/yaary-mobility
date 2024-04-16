package com.yaary.android.mobility.network

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.yaary.android.mobility.data.PreferenceRepository
import com.yaary.android.mobility.utils.DeviceUtil
import com.yaary.android.mobility.utils.NetworkStateChangeObserver
import io.grpc.ClientInterceptor
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.android.AndroidChannelBuilder
import io.grpc.stub.ClientCallStreamObserver
import io.grpc.stub.ClientResponseObserver
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.transform
import java.util.concurrent.TimeUnit


internal class GrpcWrapper(
    private val context: Context,
    private val baseURL: String,
    private val preferenceRepository: PreferenceRepository,
    private val networkStateChangeObserver: NetworkStateChangeObserver,
    private val deviceUtil: DeviceUtil
) {
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO

    val channel: ManagedChannel =
        AndroidChannelBuilder
            .forAddress(baseURL, 443)
            .maxRetryAttempts(5) // Set the maximum number of retry attempts on transient failures
            .enableRetry() // Enable automatic retries on transient failures
            .idleTimeout(30, TimeUnit.SECONDS) // Set the idle timeout for the channel
            .context(context.applicationContext)
            .build()

    fun getAuthInterceptor(): ClientInterceptor {
        return YaaryHeaderAttachingClientInterceptor
            .newAttachHeadersInterceptor(
                preferenceRepository = preferenceRepository,
                versionName = getVersionName(),
                deviceId = getDeviceId()
            )
    }

    suspend fun <Request, Response> makeUnaryCall(
        request: Request,
        callHandler: (Request) -> (Response),
    ): Result<Response> {
        Log.d(TAG, "makeUnaryCall: request - $request callHandler - $callHandler")
        return try {
            val grpcResponse = callHandler(request)
            Log.d(TAG, "makeUnaryCall: result $grpcResponse")
            Result.success(grpcResponse)
        } catch (ex: StatusRuntimeException) {
            Log.d(TAG, "makeUnaryCall: StatusRuntimeException $ex")
            if (ex.status.code == Status.UNAUTHENTICATED.code) {
                Result.failure(ex)
            } else {
                if (ex.status.code == Status.UNAVAILABLE.code) {
                    waitForInternetConnection()
                    makeUnaryCall(request, callHandler)
                } else {
                    Result.failure(ex)
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "makeUnaryCall: Exception $e")
            // Handle gRPC communication errors
            Result.failure(e)
        }
    }

    fun <Request, Response> makeServerStreamCall(
        request: Request,
        callHandler: (Request, StreamObserver<Response>) -> Unit
    ): Flow<Result<Response>> {
        var retryAttempt = 0
        return callbackFlow<Result<Response>> {
            Log.d(
                TAG,
                "makeServerStreamCall: callback init... request - $request callHandler - $callHandler"
            )
            var stream: ClientCallStreamObserver<Request>? = null
            val responseObserver = object : ClientResponseObserver<Request, Response> {

                override fun beforeStart(requestStream: ClientCallStreamObserver<Request>?) {
                    stream = requestStream
                }

                override fun onNext(value: Response?) {
                    value?.let { safeResponse ->
                        Log.d(
                            TAG,
                            "makeServerStreamCall onNext: callHandler $callHandler -> response ${safeResponse}"
                        )
                        trySend(
                            Result.success(safeResponse)
                        )
                        retryAttempt = 0
                    } ?: Log.e(TAG, "makeServerStreamCall onNext response value null")

                }

                override fun onError(throwable: Throwable?) {
                    Log.d(TAG, "makeServerStreamCall callHandler $callHandler onError - $throwable")
                    retryAttempt += 1
                    throwable?.let {
                        cancel(
                            message = it.message ?: it.toString(),
                            cause = it
                        )
                    }
                }

                override fun onCompleted() {
                    Log.d(TAG, "makeServerStreamCall onCompleted: callHandler $callHandler")
                    close(null)
                    channel.close()
                    this@callbackFlow.close()
                }
            }

            callHandler(request, responseObserver)
            awaitClose {
                Log.d(TAG, "makeServerStreamCall Flow awaitClose")
                stream?.cancel("Cancel", Exception())
                responseObserver.onCompleted()
            }
        }.retryWhen { cause, _ ->
            Log.d(
                TAG,
                "makeServerStreamCall retryWhen: attempt -> $retryAttempt | exception -> $cause"
            )
            val exception = cause.cause
            val isIoException = exception is StatusRuntimeException
                    && exception.status.code == Status.UNAVAILABLE.code
                    && (
                    exception.message?.lowercase()?.contains("ioexception".lowercase()) == true
                            || exception.message?.lowercase()
                        ?.contains("unable to resolve".lowercase()) == true
                    )

            val isUnauthenticated = exception is StatusRuntimeException
                    && exception.status.code == Status.UNAUTHENTICATED.code

            when {
                isIoException -> {
                    //waiting till internet is available
                    //this will always wait till internet is available otherwise the statement will be in suspended state
                    networkStateChangeObserver
                        .observe()
                        .transform { connected -> if (connected) emit(true) }
                        .first()
                    Log.d(
                        TAG,
                        "$TAG: Internet is connected, retrying..."
                    )
                    true
                }

                isUnauthenticated -> {
                    Log.d(TAG, "makeServerStreamCall: access token invalid...")
                    //refresh code
                    false
                }

                else -> {
                    if (retryAttempt > MAX_RETRY_COUNT) {
                        Log.d(
                            TAG,
                            "makeServerStreamCall: retrying attempt for ${retryAttempt + 1}..."
                        )
                        false
                    } else {
                        delay(TimeUnit.SECONDS.toMillis(1))
                        Log.d(
                            TAG,
                            "makeServerStreamCall: throwing error when grpc wrapper is not handling the error ${cause}}"
                        )
                        true
                    }
                }
            }
        }.flowOn(defaultDispatcher)
    }


    private suspend fun waitForInternetConnection() {
        networkStateChangeObserver
            .observe()
            .transform { connected -> if (connected) emit(true) }
            .first()
    }

    companion object {
        private const val TAG = "GrpcWrapper"
        const val MAX_RETRY_COUNT = 2
    }

    private fun getVersionName(): String {
        return "1.0"
    }

    private fun getDeviceId(): String {
        return deviceUtil.getDeviceId(context)
    }
}
