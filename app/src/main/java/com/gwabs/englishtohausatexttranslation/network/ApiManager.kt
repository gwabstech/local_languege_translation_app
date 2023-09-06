package com.gwabs.englishtohausatexttranslation.network

import okhttp3.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
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
    suspend fun sendImageToTextRequest(imageData: ByteArray, language: String = "English"): String {
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

        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {

                    response.body?.string() ?: ""



                } else {
                    "Request failed with code: ${response.code}"
                }
            } catch (e: IOException) {
                e.printStackTrace()
                "Request failed with exception: ${e.message}"
            }
        }
    }
}
