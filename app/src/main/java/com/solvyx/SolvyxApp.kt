package com.solvyx

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.HiltAndroidApp

val Context.solvyxDataStore by preferencesDataStore(name = "solvyx_prefs")

@HiltAndroidApp
class SolvyxApp : Application()
