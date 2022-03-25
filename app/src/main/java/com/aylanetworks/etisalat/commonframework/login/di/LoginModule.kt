package com.aylanetworks.etisalat.commonframework.login.di

import com.aylanetworks.aylasdk.AylaLoginManager
import com.aylanetworks.aylasdk.AylaNetworks
import com.aylanetworks.etisalat.commonframework.login.repository.LoginRepository
import com.aylanetworks.etisalat.commonframework.login.repository.LoginDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LoginModule {

    @Singleton
    @Provides
    fun provideLoginManager(): AylaLoginManager = AylaNetworks.sharedInstance().loginManager

    @Provides
    @Singleton
    fun provideLoginRepository(loginManager: AylaLoginManager): LoginRepository {
        return LoginDataSourceImpl(loginManager)
    }

}