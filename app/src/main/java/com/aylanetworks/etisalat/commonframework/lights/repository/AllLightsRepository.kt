package com.aylanetworks.etisalat.commonframework.lights.repository

import com.aylanetworks.aylasdk.AylaDevice


interface AllLightsRepository {

    suspend fun getAllLights(): List<AylaDevice>?

}