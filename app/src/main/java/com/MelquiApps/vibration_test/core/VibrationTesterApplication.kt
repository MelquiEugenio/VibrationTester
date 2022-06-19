package com.MelquiApps.vibration_test.core

import android.os.Handler
import androidx.multidex.MultiDexApplication
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class VibrationTesterApplication : MultiDexApplication() {

    private var appOpenManager: AppOpenManager? = null
    private var handler = Handler()

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()

        GlobalScope.launch {
            MobileAds.initialize(this@VibrationTesterApplication)
            appOpenManager = AppOpenManager(this@VibrationTesterApplication, handler)
        }
    }
}