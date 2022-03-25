package com.aylanetworks.etisalat.commonframework.login.usecase

import android.util.Log
import com.aylanetworks.aylasdk.AylaLoginManager
import com.aylanetworks.etisalat.commonframework.login.repository.LoginRepository
import com.daggerhilt.etisalat.common.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import java.net.HttpRetryException
import javax.inject.Inject

class LoginUseCase @Inject constructor(private val loginManager: AylaLoginManager, private val loginRepository: LoginRepository) {
    operator fun invoke(): Flow<Resource<String>> = flow {

        try {
            emit(Resource.Loading<String>())

            val token = loginRepository.getLogin()

            Log.e("getUserAuthProvider ","LoginUseCase "+token.data)

            if(token.data!=null)
            emit(token)

        } catch(e: HttpRetryException) {
            emit(Resource.Error<String>(e.localizedMessage ?: "An unexpected error occured"))
        } catch(e: IOException) {
            emit(Resource.Error<String>("Couldn't reach server. Check your internet connection."))
        }
    }

}