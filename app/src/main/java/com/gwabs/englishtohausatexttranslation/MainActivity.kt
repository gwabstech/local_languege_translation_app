package com.gwabs.englishtohausatexttranslation


import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.gwabs.englishtohausatexttranslation.databinding.ActivityMainBinding
import com.gwabs.englishtohausatexttranslation.network.data.TranslationRepository
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val mainScope = MainScope()
    private lateinit var translatedText: String


    @RequiresApi(Build.VERSION_CODES.S)
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val sourceAdapter =
            ArrayAdapter.createFromResource(this, R.array.source_languages, R.layout.spinner_item)
        sourceAdapter.setDropDownViewResource(R.layout.spinner_item)
        binding.spinnerSourceLanguage.adapter = sourceAdapter

        val targetAdapter =
            ArrayAdapter.createFromResource(this, R.array.target_languages, R.layout.spinner_item)
        targetAdapter.setDropDownViewResource(R.layout.spinner_item)
        binding.spinnerTargetLanguage.adapter = targetAdapter

        binding.buttonTranslate.setOnClickListener {

            try {
                if (validateText(
                        binding.editTextSourceText.text.toString(),
                        getLanguageCode(binding.spinnerSourceLanguage.selectedItem.toString()),
                        getLanguageCode(binding.spinnerTargetLanguage.selectedItem.toString())
                    )
                ) {
                    translateText()
                } else {
                    // Handle the case where the text is empty or contains only whitespace

                    binding.editTextSourceText.error = "Text cant be empty"
                }
            }catch (e:Exception){
                Log.i("TAG",e.toString())
            }


        }

        binding.buttonCopy.setOnClickListener {
            try {

                if (validateText(
                        binding.editTextSourceText.text.toString(),
                        getLanguageCode(binding.spinnerSourceLanguage.selectedItem.toString()),
                        getLanguageCode(binding.spinnerTargetLanguage.selectedItem.toString())
                    )
                ) {
                    copy(translatedText)
                } else {
                    Toast.makeText(this, "enter text to translate", Toast.LENGTH_SHORT).show()
                }

            }catch (e:Exception){

                Log.i("TAG",e.toString())
            }

        }


    }

    // this function validate the inputted text
    private fun validateText(text: String, from: String, to: String): Boolean {
        return text.trim().isNotEmpty() && from.trim().isNotEmpty() && to.trim().isNotEmpty()
    }

    private fun copy(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Translated Text", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this@MainActivity, "Translated text copied to clipboard", Toast.LENGTH_SHORT)
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun translateText() {
        val sourceLanguage = getLanguageCode(binding.spinnerSourceLanguage.selectedItem.toString())

        val targetLanguage = getLanguageCode(binding.spinnerTargetLanguage.selectedItem.toString())

        val sourceText = binding.editTextSourceText.text.toString()
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Translating...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        // Launch a coroutine to perform the translation
        mainScope.launch {
            try {
                // Call the translation API
                val outputText = TranslationRepository.translateText(
                    "",
                    sourceLanguage,
                    targetLanguage,
                    sourceText
                )

                // Update the UI with the translated text
                binding.textViewTranslatedText.text = outputText
                translatedText = outputText

            } catch (e: Exception) {
                // Display an error message if the translation failed

                Log.i("TAG", e.toString())
                Toast.makeText(
                    this@MainActivity,
                    "Translation failed: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                // Hide the progress dialog
                progressDialog.dismiss()
            }
        }
    }

    private fun getLanguageCode(language: String): String {
        return when (language) {
            "English" -> "en"
            "Hausa" -> "ha"
            "Igbo" -> "ig"
            "Yoruba" -> "yo"
            else -> throw IllegalArgumentException("Unsupported language: $language")
        }
    }


}