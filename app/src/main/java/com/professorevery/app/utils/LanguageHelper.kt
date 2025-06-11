package com.professorevery.app.utils

import android.content.Context

class LanguageHelper {
    companion object {
        private const val LANGUAGE_PREFERENCE = "app_preferences"
        private const val IS_KOREAN_KEY = "is_korean"
        
        fun setLanguage(context: Context, isKorean: Boolean) {
            val sharedPref = context.getSharedPreferences(LANGUAGE_PREFERENCE, Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putBoolean(IS_KOREAN_KEY, isKorean)
                apply()
            }
        }
        
        fun isKorean(context: Context): Boolean {
            val sharedPref = context.getSharedPreferences(LANGUAGE_PREFERENCE, Context.MODE_PRIVATE)
            return sharedPref.getBoolean(IS_KOREAN_KEY, false) // Default to English
        }
    }
} 