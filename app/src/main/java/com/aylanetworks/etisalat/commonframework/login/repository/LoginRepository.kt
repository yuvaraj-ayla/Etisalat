package com.aylanetworks.etisalat.commonframework.login.repository


import com.daggerhilt.etisalat.common.Resource


interface LoginRepository {
    suspend fun getLogin() : Resource<String>
}