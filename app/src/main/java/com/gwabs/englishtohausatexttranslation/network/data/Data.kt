package com.gwabs.englishtohausatexttranslation.network.data
import android.os.Build
import androidx.annotation.RequiresApi
import com.gwabs.englishtohausatexttranslation.network.TranslationApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
data class TranslationRequestBody(
    val text: String
)

data class TranslationResponseBody(
    val translations: List<Translation>
)

data class Translation(
    val text: String
)



object TranslationRepository {

    @RequiresApi(Build.VERSION_CODES.S)
    suspend fun translateText(apiKey: String, fromLang: String, toLang: String, text: String): String {
        val requestBody = TranslationRequestBody(text)
        //val response = api.
        val response = withContext(Dispatchers.IO) {
            TranslationApiClient.instance.translateText(apiKey,fromLang,toLang,requestBody)
        }
        return response.toString()
    }
}

