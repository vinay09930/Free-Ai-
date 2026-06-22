package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.FirebaseManager

class FreeAiApplication : Application() {
    lateinit var database: AppDatabase
    lateinit var firebaseManager: FirebaseManager

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "freeai-database"
        ).fallbackToDestructiveMigration().build()
        firebaseManager = FirebaseManager()
    }
}
