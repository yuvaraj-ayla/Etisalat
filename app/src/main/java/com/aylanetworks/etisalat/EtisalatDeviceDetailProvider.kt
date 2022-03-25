package com.aylanetworks.etisalathome

import android.content.Context
import com.aylanetworks.aylasdk.AylaDevice
import com.aylanetworks.aylasdk.AylaSystemSettings

/**
 * This is a helper class that allows the SDK to ask the application for details about particular
 * devices manged by the SDK. This class MUST be passed as "deviceDetailProvider" parameter to the
 * AylaSDK during its initialization.
 * @see EtisalatConfig class
 */
class EtisalatDeviceDetailProvider(applicationContext: Context?) :
    AylaSystemSettings.DeviceDetailProvider {

    // TODO, add more functionality when the device setup is required
    override fun getManagedPropertyNames(aylaDevice: AylaDevice?): Array<String?> {
        if (aylaDevice?.oemModel?.contains("SQR621U1XXW")!!) {
            val property = mutableListOf<String>()
            property.add("Switch")
            property.add("ClientTimer")
            property.add("Power")
            return property.toTypedArray()
        } else if (aylaDevice.oemModel?.contains("SQR226U1XXW")!!) {
            val property = mutableListOf<String>()
            property.add("Switch")
            property.add("Level")
            property.add("ClientTimer")
            property.add("Power")
            return property.toTypedArray()
        } else if (aylaDevice.oemModel?.contains("SQR141U1XXW")!!) {
            val property = mutableListOf<String>()
            property.add("Switch")
            property.add("ClientTimer")
            property.add("Power")
            return property.toTypedArray()
        } else if (aylaDevice.oemModel?.contains("SQR441U1XXW")!!) {
            val property = mutableListOf<String>()
            property.add("Switch")
            property.add("ClientTimer")
            property.add("Power")
            return property.toTypedArray()
        }
        return emptyArray()

    }
}
