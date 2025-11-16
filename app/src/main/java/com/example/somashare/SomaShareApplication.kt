package com.example.somashare

import android.app.Application

class SomaShareApplication : Application() {

    // Database instance
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

    override fun onCreate() {
        super.onCreate()
        // Any app-wide initialization can go here
    }


}