package com.gwabs.englishtohausatexttranslation


import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.gwabs.englishtohausatexttranslation.databinding.ActivityMainBinding
import com.gwabs.englishtohausatexttranslation.network.HttpClientSingleton
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var translatedText: String


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Supported languege dropdown menu items
        val sourceAdapter =
            ArrayAdapter.createFromResource(this, R.array.source_languages, R.layout.spinner_item)
        sourceAdapter.setDropDownViewResource(R.layout.spinner_item)
        binding.spinnerSourceLanguage.adapter = sourceAdapter

        val targetAdapter =
            ArrayAdapter.createFromResource(this, R.array.target_languages, R.layout.spinner_item)
        targetAdapter.setDropDownViewResource(R.layout.spinner_item)
        binding.spinnerTargetLanguage.adapter = targetAdapter

        // click listener to trigger the translation function after inputs are validated successfully
        binding.buttonTranslate.setOnClickListener {

            try {
                if (validateText(
                        binding.editTextSourceText.text.toString(),
                        getLanguageCode(binding.spinnerSourceLanguage.selectedItem.toString()),
                        getLanguageCode(binding.spinnerTargetLanguage.selectedItem.toString())
                    )
                ) {
                    translateText(this)
                } else {
                    // Handle the case where the text is empty or contains only whitespace

                    binding.editTextSourceText.error = "Text cant be empty"
                }
            }catch (e:Exception){
              //  Log.i("TAG",e.toString())
            }


        }

        // click listener to trigger the copy function if the result is not empty
        binding.buttonCopy.setOnClickListener {
            try {
                if (!TextUtils.isEmpty(translatedText))
                    copy(translatedText)

            }catch (e:Exception){
                //Log.i("TAG",e.toString())
            }

        }

        binding.imageToText.setOnClickListener {
            val startImageToText = Intent(this@MainActivity,imageToText::class.java)
            startActivity(startImageToText)

        }

    }

    // this function validate the inputted text
    // inputs validation function
    private fun validateText(text: String, from: String, to: String): Boolean {
        return text.trim().isNotEmpty() && from.trim().isNotEmpty() && to.trim().isNotEmpty()
    }


    // function to enable copy and pest
    private fun copy(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Translated Text", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this@MainActivity, "Translated text copied to clipboard", Toast.LENGTH_SHORT)
            .show()
    }

    // this function send a translation request to the api and  return the result
    private fun translateText(context: Context) {
        val sourceLanguage = getLanguageCode(binding.spinnerSourceLanguage.selectedItem.toString())

        val targetLanguage = getLanguageCode(binding.spinnerTargetLanguage.selectedItem.toString())

        val sourceText = binding.editTextSourceText.text.toString()
        val progressDialog = ProgressDialog(context)
        progressDialog.setMessage("Translating...")
        progressDialog.setCancelable(false)
        progressDialog.show()


        val client = HttpClientSingleton.getInstance()

        val mediaType = "application/x-www-form-urlencoded".toMediaTypeOrNull()
        val body = RequestBody.create(mediaType, "source_language=${sourceLanguage}&target_language=${targetLanguage}&text=${sourceText}")

        // api request requirements such as auth key and base url

        val request = Request.Builder()
            .url("https://text-translator2.p.rapidapi.com/translate")
            .post(body)
            .addHeader("content-type", "application/x-www-form-urlencoded")
            .addHeader("X-RapidAPI-Key", "replace with your api key")
            .addHeader("X-RapidAPI-Host", "text-translator2.p.rapidapi.com")
            .build()

        val gson = Gson()


        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }

                if (response.isSuccessful) {
                    //Log.i("TAG", response.body.toString())

                    val responseBody = response.body?.string()
                    val jsonObject = gson.fromJson(responseBody, JsonObject::class.java)

                    // here we retrieve  the translated text and we assign it to a variable result
                    val result = jsonObject.getAsJsonObject("data").get("translatedText").toString()
                   // Log.i("TAG", translatedText)
                    translatedText = result
                    // set the value of result to the textview that display the translated text
                    binding.textViewTranslatedText.text = result.replace("\"", "")
                    binding.buttonCopy.visibility = View.VISIBLE
                } else {
                    Toast.makeText(this@MainActivity,response.body.toString(),Toast.LENGTH_SHORT).show()
                   // Log.i("TAG", response.body.toString())
                    binding.buttonCopy.visibility = View.GONE
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity,e.toString(),Toast.LENGTH_SHORT).show()
                binding.buttonCopy.visibility = View.GONE
               // Log.i("TAG", e.toString())
            } finally {
                progressDialog.dismiss()

            }
        }
    }


}

