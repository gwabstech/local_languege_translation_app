package com.gwabs.englishtohausatexttranslation

fun getLanguageCode(language: String): String {
    return when (language) {
        "English" -> "en"
        "Hausa" -> "ha"


        else -> throw IllegalArgumentException("Unsupported language: $language")
    }
}
