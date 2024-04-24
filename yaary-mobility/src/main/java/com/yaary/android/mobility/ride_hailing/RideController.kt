package com.yaary.android.mobility.ride_hailing

import android.content.Context
import com.yaary.android.mobility.data.PreferenceRepository
import com.yaary.android.mobility.data.RideRepository
import com.yaary.android.mobility.error.Error
import com.yaary.android.mobility.extentions.isSuccess
import consumer.common.commonV2.Common
import consumer.ride.rideV2.Ride
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

internal class RideController(
    context: Context,
    private val rideRepository: RideRepository,
    private val preferenceRepository: PreferenceRepository
) {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    fun initiateSearch(
        request: Ride.GetRideQuotesRequest,
        callback: Callback
    ) {
        scope.launch(Dispatchers.IO) {
            val result = rideRepository.initiateSearch(request)
            if (result.isSuccess) {
                val data = result.getOrNull()
                val metadata = data?.metadata
                if (data?.data != null && metadata != null && metadata.isSuccess) {
                    launchInMain {
                        callback.onSuccess(
                            data.data
                        )
                    }
                } else {
                    launchInMain {
                        callback.onError(
                            Error(
                                Common.StatusCode.UNRECOGNIZED.name,
                                data?.metadata?.statusMessage
                            )
                        )
                    }
                }
            } else {
                launchInMain {
                    callback.onError(
                        Error(
                            Common.StatusCode.UNRECOGNIZED.name,
                            result.exceptionOrNull()?.message
                        )
                    )
                }
            }
        }
    }

    fun getQuoteCatalogStream(
        request: Ride.QuoteCatalogRequest,
        callback: Callback
    ) {
        scope.launch(Dispatchers.IO) {
            val result = rideRepository.getQuoteCatalogStream(request)
            result
                .buffer()
                .catch {
                    callback.onError(
                        Error(
                            Common.StatusCode.UNRECOGNIZED.name,
                            it.message
                        )
                    )
                }.collect {
                    if (it.isSuccess) {
                        val data = it.getOrNull()
                        val metadata = data?.metadata
                        if (data?.data != null && metadata != null && metadata.isSuccess) {
                            launchInMain {
                                callback.onSuccess(
                                    data.data
                                )
                            }
                        } else {
                            launchInMain {
                                callback.onError(
                                    Error(
                                        Common.StatusCode.UNRECOGNIZED.name,
                                        data?.metadata?.statusMessage
                                    )
                                )
                            }
                        }
                    } else {
                        launchInMain {
                            callback.onError(
                                Error(
                                    Common.StatusCode.UNRECOGNIZED.name,
                                    it.exceptionOrNull()?.message
                                )
                            )
                        }
                    }
                }
        }
    }

    fun selectQuote(
        request: Ride.SelectQuoteRequest,
        callback: Callback
    ) {
        scope.launch(Dispatchers.IO) {
            val result = rideRepository.selectQuote(request)
            if (result.isSuccess) {
                val data = result.getOrNull()
                val metadata = result.getOrNull()?.metadata
                if (data?.data != null && metadata != null && metadata.isSuccess) {
                    launchInMain {
                        callback.onSuccess(
                            data.data
                        )
                    }
                } else {
                    launchInMain {
                        callback.onError(
                            Error(
                                Common.StatusCode.UNRECOGNIZED.name,
                                metadata?.statusMessage
                            )
                        )
                    }
                }
            } else {
                launchInMain {
                    callback.onError(
                        Error(
                            Common.StatusCode.UNRECOGNIZED.name,
                            result.exceptionOrNull()?.message
                        )
                    )
                }
            }
        }
    }

    fun confirmCancel(
        request: Ride.ConfirmCancelRequest,
        callback: Callback
    ) {
        scope.launch(Dispatchers.IO) {
            val result = rideRepository.confirmCancel(request)
            if (result.isSuccess) {
                val data = result.getOrNull()
                val metadata = result.getOrNull()?.metadata
                if (data?.data != null && metadata != null && metadata.isSuccess) {
                    launchInMain {
                        callback.onSuccess(
                            data.data
                        )
                    }
                } else {
                    launchInMain {
                        callback.onError(
                            Error(
                                Common.StatusCode.UNRECOGNIZED.name,
                                metadata?.statusMessage
                            )
                        )
                    }
                }
            } else {
                launchInMain {
                    callback.onError(
                        Error(
                            Common.StatusCode.UNRECOGNIZED.name,
                            result.exceptionOrNull()?.message
                        )
                    )
                }
            }
        }
    }

    fun rideUpdateStream(
        request: Ride.RideUpdateRequest,
        callback: Callback
    ) {
        scope.launch(Dispatchers.IO) {
            val result = rideRepository.rideUpdateStream(request)
            result
                .buffer()
                .catch {
                    callback.onError(
                        Error(
                            Common.StatusCode.UNRECOGNIZED.name,
                            it.message
                        )
                    )
                }.collect {
                    if (it.isSuccess) {
                        val data = it.getOrNull()
                        val metadata = data?.metadata
                        if (data?.data != null && metadata != null && metadata.isSuccess) {
                            launchInMain {
                                callback.onSuccess(
                                    data.data
                                )
                            }
                        } else {
                            launchInMain {
                                callback.onError(
                                    Error(
                                        Common.StatusCode.UNRECOGNIZED.name,
                                        data?.metadata?.statusMessage
                                    )
                                )
                            }
                        }
                    } else {
                        launchInMain {
                            callback.onError(
                                Error(
                                    Common.StatusCode.UNRECOGNIZED.name,
                                    it.exceptionOrNull()?.message
                                )
                            )
                        }
                    }
                }
        }
    }

    fun getRideConfirmationStream(
        request: Ride.GetRideConfirmationRequest,
        callback: Callback
    ) {
        scope.launch(Dispatchers.IO) {
            val result = rideRepository.getRideConfirmationStream(request)
            result
                .buffer()
                .catch {
                    callback.onError(
                        Error(
                            Common.StatusCode.UNRECOGNIZED.name,
                            it.message
                        )
                    )
                }.collect {
                    if (it.isSuccess) {
                        val data = it.getOrNull()
                        val metadata = data?.metadata
                        if (data?.data != null && metadata != null && metadata.isSuccess) {
                            launchInMain {
                                callback.onSuccess(
                                    data.data
                                )
                            }
                        } else {
                            launchInMain {
                                callback.onError(
                                    Error(
                                        Common.StatusCode.UNRECOGNIZED.name,
                                        data?.metadata?.statusMessage
                                    )
                                )
                            }
                        }
                    } else {
                        launchInMain {
                            callback.onError(
                                Error(
                                    Common.StatusCode.UNRECOGNIZED.name,
                                    it.exceptionOrNull()?.message
                                )
                            )
                        }
                    }
                }
        }
    }

    private fun launchInMain(fn: () -> Unit) {
        scope.launch(Dispatchers.Main) {
            fn()
        }
    }
}