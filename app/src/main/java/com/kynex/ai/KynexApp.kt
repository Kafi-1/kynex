package com.kynex.ai

import android.app.Application
import com.google.firebase.FirebaseApp
import com.kynex.ai.di.AppGraph

class KynexApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        AppGraph.init(this)
    }
}
