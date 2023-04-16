package com.gwabs.englishtohausatexttranslation

fun getLanguageCode(language: String): String {
    return when (language) {
        "English" -> "en"
        "Hausa" -> "ha"
        "Igbo" -> "ig"
        "Yoruba" -> "yo"
        "French" -> "fr"
        "Arabic" -> "ar"

        else -> throw IllegalArgumentException("Unsupported language: $language")
    }
}