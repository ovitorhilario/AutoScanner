package com.vitorhilarioapps.autoscanner.app

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader

val Context.dataStore : DataStore<Preferences> by preferencesDataStore(name = "settings")

class AppApplication : Application() {

    companion object {
        val PREF_RESOLUTION = intPreferencesKey("pref_resolution")
        const val TAG = "AppApplication"
    }

    suspend fun getResolutionCode() : Int {
        val settings = dataStore.data.first()
        return settings[PREF_RESOLUTION] ?: 0
    }

    suspend fun saveResolutionCode(code: Int) {
        dataStore.edit { settings ->
            settings[PREF_RESOLUTION] = code
        }
    }

    override fun onCreate() {
        super.onCreate()
        initOpenCV()
    }

    private fun initOpenCV() {
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback)
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    private val mLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when(status) {
                LoaderCallbackInterface.SUCCESS -> Log.d(TAG, "OPENCV INITIALIZED")
                else -> super.onManagerConnected(status)
            }
        }
    }
}