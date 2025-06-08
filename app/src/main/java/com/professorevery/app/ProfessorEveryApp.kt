package com.professorevery.app

import android.app.Application
import com.google.firebase.FirebaseApp

class ProfessorEveryApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
} 