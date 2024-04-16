package com.yaary.android.mobility.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException


class NetworkStateChangeObserver(
    private val context: Context
) {


    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun observe(): Flow<Boolean> {
        return callbackFlow {
            val initialState = isNetworkConnected()
            var lastKnownState = initialState
            trySend(isNetworkConnected())
            val networkChangeObserver = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    Log.d(TAG, "network onAvailable: $network")
                    if (!lastKnownState) {
                        lastKnownState = true
                        trySend(true)
                    }
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    Log.d(TAG, "onUnavailable: ")
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    super.onCapabilitiesChanged(network, networkCapabilities)
                    val networkValidated = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                    } else {
                        true
                    }

                    val hasInternet =
                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

                    val hasTransport =
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                                || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)

                    val internetAvailable = networkValidated && hasInternet && hasTransport
                    if (lastKnownState != internetAvailable) {
                        lastKnownState = internetAvailable
                        trySend(internetAvailable)
                    }
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    Log.d(TAG, "network onLost: $network")
                    if(lastKnownState){
                        lastKnownState = false
                        trySend(false)
                    }
                }
            }

            connectivityManager.registerNetworkCallback(
                getNetworkRequest(),
                networkChangeObserver
            )

            awaitClose {
                try {
                    connectivityManager.unregisterNetworkCallback(networkChangeObserver)
                    Log.d(TAG, "observe: closed callback...")
                } catch (e: Exception) {
                    Log.d(TAG, "observe: failed to close callback $e")
                }
            }

        }
            .onEach { Log.d(TAG, "observe: network available -> $it") }

    }

    private fun Flow<Boolean>.retryOnException(exceptions: List<Class<out Exception>>): Flow<Boolean> {
        return retryWhen { cause, _ ->
            emit(false)
            if (exceptions.any { it.isInstance(cause) }) {
                delay(INTERNET_RETRY_DURATION)
                Log.d(TAG, "observe: retrying as internet not available")
                true
            } else {
                false
            }
        }
    }

    private fun Flow<Boolean>.pingGooglePeriodically(): Flow<Boolean> {
        return transform { connected: Boolean ->
            if (connected) {
                emitAll(
                    flow {
                        while (true) {
                            emit(checkInternetAvailabilityByPingingGoogleDns())
                            delay(INTERNET_AVAILABILITY_POLLING_DURATION)
                        }
                    })
            } else {
                emit(false)
            }
        }.retryOnException(listOf(IOException::class.java, SocketTimeoutException::class.java))
    }

    private fun getNetworkRequest() = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) // network is configured to reach the general Internet
        .apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) // bit indicates that the system found actual connectivity to the general Internet the last time it checked
            }
        }
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
        .build()


    private suspend fun checkInternetAvailabilityByPingingGoogleDns(): Boolean {
        val timeoutMs = INTERNET_TIME_OUT
        val socket = Socket()
        val socketAddress = InetSocketAddress("8.8.8.8", 53)
        Log.d(TAG, "isInternetAvailable: pinging google...")
        withContext(Dispatchers.IO) {
            socket.connect(socketAddress, timeoutMs)
            socket.close()
            Log.d(TAG, "isInternetAvailable: ping successful...")
        }
        return true
    }

    private fun isNetworkConnected(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val cap = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                ?: return false
            val hasNetworkTransport = cap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            val hasInternetCapability =
                cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && cap.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_VALIDATED
                )
            Log.d(TAG, "isNetworkConnected: hasNetworkTransport $hasNetworkTransport")
            Log.d(TAG, "isNetworkConnected: hasInternetCapability $hasInternetCapability")
            return hasNetworkTransport && hasInternetCapability
        } else {
            val networks = connectivityManager.allNetworkInfo
            for (nInfo in networks) {
                if (nInfo != null && nInfo.isConnected) {
                    Log.d(TAG, "isNetworkConnected: network connected to internet found $nInfo")
                    return true
                }
            }

        }
        Log.d(TAG, "isNetworkConnected: false")
        return false
    }

    companion object {
        const val TAG = "NetworkObserver"
        const val INTERNET_TIME_OUT = 5000
        const val INTERNET_RETRY_DURATION = 1500L
        const val INTERNET_AVAILABILITY_POLLING_DURATION = 10_000L
    }
}