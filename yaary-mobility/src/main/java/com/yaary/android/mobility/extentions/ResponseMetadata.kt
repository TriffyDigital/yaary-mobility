package com.yaary.android.mobility.extentions

import consumer.common.commonV2.Common

val Common.ResponseMetadata.isSuccess
    get() = statusCode == Common.StatusCode.TF_OK