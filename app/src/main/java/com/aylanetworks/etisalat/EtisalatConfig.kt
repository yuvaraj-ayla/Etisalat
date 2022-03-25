package com.aylanetworks.etisalathome

import android.app.Application
import android.content.Context
import com.aylanetworks.aylasdk.AylaNetworks
import com.aylanetworks.aylasdk.AylaSystemSettings
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EtisalatConfig : Application() {
    override fun onCreate() {
        super.onCreate()
        initAylaSDK(applicationContext)
    }

    private fun initAylaSDK(applicationContext: Context?) {
        AylaNetworks.initialize(getAylaSettings(applicationContext))
    }

    private fun getAylaSettings(applicationContext: Context?): AylaSystemSettings {
        val aylaSystemSettings = AylaSystemSettings()
        // TODO, set proper configuration depending on flavors (dev, production)
        aylaSystemSettings.appId = "aura-test-6A-id"
        aylaSystemSettings.appSecret = "aura-test-RFy9Q8X2GdH5_j-Qr0D5kLWb2V8"
        aylaSystemSettings.serviceType = AylaSystemSettings.ServiceType.Field
        aylaSystemSettings.context = applicationContext
        aylaSystemSettings.serviceLocation = AylaSystemSettings.ServiceLocation.USA
        aylaSystemSettings.deviceDetailProvider = EtisalatDeviceDetailProvider(applicationContext)
        aylaSystemSettings.allowDSS = true
        aylaSystemSettings.allowOfflineUse = true
        return aylaSystemSettings
    }
}