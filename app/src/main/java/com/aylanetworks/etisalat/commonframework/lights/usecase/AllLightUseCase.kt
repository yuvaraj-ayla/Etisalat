package com.aylanetworks.etisalat.commonframework.lights.usecase

import com.aylanetworks.aylasdk.AylaDevice
import com.aylanetworks.etisalat.commonframework.lights.repository.AllLightsRepository
import com.daggerhilt.etisalat.common.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import java.net.HttpRetryException
import javax.inject.Inject

class AllLightUseCase @Inject constructor(
    private val repository: AllLightsRepository
) {
    operator fun invoke(): Flow<Resource<List<AylaDevice>>> = flow {
        try {
            val lights = repository.getAllLights()
            emit(Resource.Success<List<AylaDevice>>(lights))
        } catch(e: HttpRetryException) {
            emit(Resource.Error<List<AylaDevice>>(e.localizedMessage ?: "An unexpected error occured"))
        } catch(e: IOException) {
            emit(Resource.Error<List<AylaDevice>>("Couldn't reach server. Check your internet connection."))
        }
    }
}