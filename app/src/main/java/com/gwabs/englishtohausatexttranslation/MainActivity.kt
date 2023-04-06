package com.gwabs.englishtohausatexttranslation


import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import com.gwabs.englishtohausatexttranslation.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding :ActivityMainBinding
    private lateinit var translate: Translate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        val options = TranslateOptions.newBuilder().setApiKey("AIzaSyDpRV2Tq3TahjySHgI7oZJxHPCIwcrlHEc").build()
        translate = options.service


        binding.buttonTranslate.setOnClickListener {

            val text = binding.editTextSourceText.text.toString()
            if (validateText(text)) {
                // Do something with the validated text
                try {
                    binding.textViewTranslatedText.text = translate(text)
                }catch (e:Exception){
                    Toast.makeText(this,e.message,Toast.LENGTH_LONG).show()
                }

            } else {
                // Handle the case where the text is empty or contains only whitespace

                binding.editTextSourceText.error = "Text cant be empty"
            }

        }




    }

    // this function validate the inputted text
    private fun validateText(text: String): Boolean {
        return text.trim().isNotEmpty()
    }

    private fun translate(text:String):String{

            return translate.translate(text, Translate.TranslateOption.targetLanguage("ha")).translatedText


    }
}