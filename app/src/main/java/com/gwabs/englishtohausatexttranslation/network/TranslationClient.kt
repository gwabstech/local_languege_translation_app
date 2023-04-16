package com.gwabs.englishtohausatexttranslation.network

import okhttp3.OkHttpClient
object HttpClientSingleton {
    private val client = OkHttpClient()

    fun getInstance(): OkHttpClient {
        return client
    }
}
