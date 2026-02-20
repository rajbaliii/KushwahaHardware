package com.kushwahahardware

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class KushwahaHardwareApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
    
    companion object {
        lateinit var instance: KushwahaHardwareApp
            private set
    }
}