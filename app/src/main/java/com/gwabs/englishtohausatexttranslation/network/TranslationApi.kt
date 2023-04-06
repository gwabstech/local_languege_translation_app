package com.gwabs.englishtohausatexttranslation.network

import android.view.translation.TranslationResponse
import com.gwabs.englishtohausatexttranslation.network.data.TranslationRequestBody
import retrofit2.http.*

interface TranslationApi {

    @POST("translate?api-version=3.0")
    suspend fun translateText(
        @Header("Ocp-Apim-Subscription-Key") apiKey: String,
        @Query("from") sourceLanguage: String,
        @Query("to") targetLanguage: String,
        @Body request: TranslationRequestBody
    ): TranslationResponse
}

