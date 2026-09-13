package com.kynex.ai

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.kynex.ai.di.AppGraph
import java.io.File
import java.util.Date

class KynexApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        AppGraph.init(this)

        // Persist the last crash so it can be shown in-app on next launch.
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            runCatching {
                File(filesDir, "crash_log.txt").writeText(
                    "Time: ${Date()}\n${Log.getStackTraceString(e)}\n"
                )
            }
            previous?.uncaughtException(t, e)
        }
    }
}
