package com.yaary.android.mobility.network

import com.yaary.android.mobility.data.PreferenceRepository
import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ForwardingClientCall
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import java.util.UUID

internal class YaaryHeaderAttachingClientInterceptor internal constructor(
    private val extraHeaders: Metadata,
    private val preferenceRepository: PreferenceRepository,
) :
    ClientInterceptor {
    override fun <ReqT, RespT> interceptCall(
        method: MethodDescriptor<ReqT, RespT>, callOptions: CallOptions, next: Channel
    ): ClientCall<ReqT, RespT> {
        return HeaderAttachingClientCall<ReqT, RespT>(next.newCall(method, callOptions))
    }


    private inner class HeaderAttachingClientCall<ReqT, RespT>  // Non private to avoid synthetic class
    internal constructor(call: ClientCall<ReqT, RespT>?) :
        ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(call) {
        override fun start(responseListener: Listener<RespT>, headers: Metadata) {
            headers.merge(extraHeaders)
            if (!preferenceRepository.getAccessToken().isNullOrEmpty()) {
                headers.removeAll(
                    Metadata.Key.of(
                        NetworkConstants.AUTHORIZATION,
                        Metadata.ASCII_STRING_MARSHALLER
                    )
                )
                headers.put(
                    Metadata.Key.of(
                        NetworkConstants.AUTHORIZATION,
                        Metadata.ASCII_STRING_MARSHALLER
                    ),
                    "${NetworkConstants.HTTP_HEADER_BEARER_AUTH_TOKEN_PREFIX}${preferenceRepository.getAccessToken()}"
                )
            }

            if (!preferenceRepository.getApiToken().isNullOrEmpty()) {
                headers.removeAll(
                    Metadata.Key.of(
                        NetworkConstants.X_API_TOKEN,
                        Metadata.ASCII_STRING_MARSHALLER
                    )
                )
                headers.put(
                    Metadata.Key.of(NetworkConstants.X_API_TOKEN, Metadata.ASCII_STRING_MARSHALLER),
                    preferenceRepository.getApiToken()
                )
            }

            headers.removeAll(
                Metadata.Key.of(
                    NetworkConstants.KEY_REQUEST_ID,
                    Metadata.ASCII_STRING_MARSHALLER
                )
            )
            headers.put(
                Metadata.Key.of(
                    NetworkConstants.KEY_REQUEST_ID,
                    Metadata.ASCII_STRING_MARSHALLER
                ), UUID.randomUUID().toString()
            )
            super.start(responseListener, headers)
        }
    }

    companion object {
        private val headers by lazy {
            val header = Metadata()
            header.put(
                Metadata.Key.of(
                    NetworkConstants.KEY_DEVICE_PLATFORM,
                    Metadata.ASCII_STRING_MARSHALLER
                ), NetworkConstants.devicePlatform
            )
            header.put(
                Metadata.Key.of(
                    NetworkConstants.KEY_DEVICE_MODEL,
                    Metadata.ASCII_STRING_MARSHALLER
                ), NetworkConstants.deviceModel
            )
            header
        }

        fun newAttachHeadersInterceptor(
            preferenceRepository: PreferenceRepository,
            versionName: String,
            deviceId: String
        ): ClientInterceptor {
            headers.removeAll(
                Metadata.Key.of(
                    NetworkConstants.KEY_APP_VERSION,
                    Metadata.ASCII_STRING_MARSHALLER
                )
            )
            headers.put(
                Metadata.Key.of(
                    NetworkConstants.KEY_APP_VERSION,
                    Metadata.ASCII_STRING_MARSHALLER
                ), versionName
            )

            if (deviceId.isNotEmpty()) {
                headers.removeAll(
                    Metadata.Key.of(
                        NetworkConstants.KEY_DEVICE_ID,
                        Metadata.ASCII_STRING_MARSHALLER
                    )
                )
                headers.put(
                    Metadata.Key.of(
                        NetworkConstants.KEY_DEVICE_ID,
                        Metadata.ASCII_STRING_MARSHALLER
                    ), deviceId
                )
            }
            headers.removeAll(
                Metadata.Key.of(
                    NetworkConstants.KEY_APP_VERSION,
                    Metadata.ASCII_STRING_MARSHALLER
                )
            )
            headers.put(
                Metadata.Key.of(
                    NetworkConstants.KEY_APP_VERSION,
                    Metadata.ASCII_STRING_MARSHALLER
                ), versionName
            )

            if (!preferenceRepository.getClientId().isNullOrEmpty()) {
                headers.removeAll(
                    Metadata.Key.of(
                        NetworkConstants.X_CLIENT_ID,
                        Metadata.ASCII_STRING_MARSHALLER
                    )
                )
                headers.put(
                    Metadata.Key.of(
                        NetworkConstants.X_CLIENT_ID,
                        Metadata.ASCII_STRING_MARSHALLER
                    ), preferenceRepository.getClientId()
                )
            }

            return YaaryHeaderAttachingClientInterceptor(
                preferenceRepository = preferenceRepository,
                extraHeaders = headers
            )
        }
    }
}
