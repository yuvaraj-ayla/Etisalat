package com.aylanetworks.etisalat.ui.lights.presenter

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aylanetworks.etisalat.commonframework.lights.usecase.AllLightUseCase
import com.aylanetworks.etisalat.commonframework.login.usecase.LoginUseCase
import com.daggerhilt.etisalat.common.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LightsPresenter @Inject constructor(private val allLightUseCase: AllLightUseCase) : ViewModel() {

    var newData: MutableLiveData<String>? = MutableLiveData()


    init {
        getLights()
    }

    private fun getLights() {
        Log.e("getUserAuthProvider ","getLights ")


        viewModelScope.launch {
            delay(2000)
            allLightUseCase().collect {result ->
                when (result) {
                    is Resource.Success -> {
                        Log.e("getUserAuthProvider ","onEach "+ result.data.toString())
                        newData?.value = "Success"
                    }
                    is Resource.Error -> {
                        newData?.value = "Error"
                    }
                    is Resource.Loading -> {
                        newData?.value = "Loading"
                    }
                }
            }
        }


    }
}