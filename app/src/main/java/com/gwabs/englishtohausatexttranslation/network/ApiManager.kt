package com.gwabs.englishtohausatexttranslation.network

import android.util.Log
import okhttp3.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ApiManager private constructor() {

    private val client = OkHttpClient()

    companion object {
        private var instance: ApiManager? = null

        fun getInstance(): ApiManager {
            if (instance == null) {
                instance = ApiManager()
            }
            return instance!!
        }
    }

    // work on this this night
    suspend fun sendImageToTextRequest(imageData: ByteArray, language: String = "English") : String{
        var formattedText:String = ""
        var proceesedText:String = ""
        val mediaType = "multipart/form-data; boundary=---011000010111000001101001"
            .toMediaTypeOrNull()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("language", language)
            .addFormDataPart("file", "image", imageData.toRequestBody(mediaType))
            .build()

        val request = Request.Builder()
            .url("https://ocr-image-to-text-multilingual.p.rapidapi.com/imagetotext")
            .post(requestBody)
            .addHeader("content-type", "multipart/form-data; boundary=---011000010111000001101001")
            .addHeader("X-RapidAPI-Key", "f68f84c6f0mshe68bae84199ee04p13ec73jsnb779bc1a1195")
            .addHeader("X-RapidAPI-Host", "ocr-image-to-text-multilingual.p.rapidapi.com")
            .build()

        withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {

                    val responseBody = response.body
                    val responseBodyString = responseBody?.string()
                    // Parse the response body string to a JSONObject
                    val responseData = JSONObject(responseBodyString!!)

                    proceesedText = responseData.getString("text").trimIndent()

                    formattedText = proceesedText.replace(Regex("[^a-zA-Z ]"), "")
                    Log.i("TAG",formatString(formattedText).trimIndent())


                } else {
                    "Request failed with code: ${response.code}"
                }
            } catch (e: IOException) {
                e.printStackTrace()
                "Request failed with exception: ${e.message}"
            }
        }
        return formatString(formattedText)
    }

    fun formatString(text: String): String {
        // Remove all non-text characters
        val nonTextCharacters = "\\W"
        val filteredText = text.replace(nonTextCharacters, "")

        // Return the formatted text
        return filteredText
    }
}
