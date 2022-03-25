package com.aylanetworks.etisalat.commonframework.login.repository

import android.util.Log
import com.aylanetworks.aylasdk.AylaLoginManager
import com.aylanetworks.aylasdk.auth.UsernameAuthProvider
import com.aylanetworks.aylasdk.error.AuthError
import com.daggerhilt.etisalat.common.Common
import com.daggerhilt.etisalat.common.Resource
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LoginDataSourceImpl @Inject constructor(private val loginManager: AylaLoginManager) : LoginRepository {

    var refToken : String? = null

    override suspend fun getLogin(): Resource<String> = suspendCoroutine{ res ->
        loginManager.signIn(
            getUserAuthProvider(), Common.SESSION_NAME, { result ->
                Log.e("getUserAuthProvider ","is "+result.refreshToken)
                refToken = result.refreshToken
                res.resume(Resource.Success<String>(refToken.toString()))

            },
            { error ->
                Log.e("getUserAuthProvider ","error "+error)

                val authError = error as? AuthError
                val errorMessage = authError?.detailMessage ?: ""
                res.resume(Resource.Error<String>(errorMessage.toString()))
            })

    }

    private fun getUserAuthProvider(): UsernameAuthProvider {
        return UsernameAuthProvider(
            "yuva.sky.raj+test52@gmail.com",
            "Ayla12345"
        )
    }
}