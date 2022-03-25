package com.aylanetworks.etisalat.commonframework.lights.di

import com.aylanetworks.aylasdk.AylaDeviceManager
import com.aylanetworks.aylasdk.AylaNetworks
import com.aylanetworks.etisalat.commonframework.lights.repository.AllLightsRepository
import com.aylanetworks.etisalat.commonframework.lights.datasource.AllLightsDataSourceImpl
import com.daggerhilt.etisalat.common.Common
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LightsModule {

    @Singleton
    @Provides
    fun provideLoginManager(): AylaDeviceManager = AylaNetworks.sharedInstance().getSessionManager(Common.SESSION_NAME).deviceManager

    @Provides
    @Singleton
    fun provideLightsRepository(deviceManager: AylaDeviceManager): AllLightsRepository {
        return AllLightsDataSourceImpl(deviceManager)
    }

}