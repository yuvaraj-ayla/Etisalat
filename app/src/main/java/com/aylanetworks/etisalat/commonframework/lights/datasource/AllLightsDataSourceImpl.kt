package com.aylanetworks.etisalat.commonframework.lights.datasource

import android.util.Log
import com.aylanetworks.aylasdk.AylaDevice
import com.aylanetworks.aylasdk.AylaDeviceManager
import com.aylanetworks.aylasdk.AylaNetworks
import com.aylanetworks.etisalat.commonframework.lights.repository.AllLightsRepository
import javax.inject.Inject

class AllLightsDataSourceImpl @Inject constructor(private val deviceManager: AylaDeviceManager) :
    AllLightsRepository {
    override suspend fun getAllLights(): List<AylaDevice>? {
        Log.e("getUserAuthProvider All Lights ","is "+ AylaNetworks.sharedInstance().getSessionManager("WiserSession").deviceManager.devices)
        return deviceManager.devices
    }

}