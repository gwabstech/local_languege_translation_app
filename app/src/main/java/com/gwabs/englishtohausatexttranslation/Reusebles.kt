package com.gwabs.englishtohausatexttranslation

import android.app.Dialog
import android.content.Context
import android.widget.EditText

fun getLanguageCode(language: String): String {
    return when (language) {
        "English" -> "en"
        "Hausa" -> "ha"


        else -> throw IllegalArgumentException("Unsupported language: $language")
    }

}
