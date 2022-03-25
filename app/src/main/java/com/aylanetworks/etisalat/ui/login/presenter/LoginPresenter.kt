package com.aylanetworks.etisalat.ui.login.presenter

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aylanetworks.etisalat.commonframework.login.usecase.LoginUseCase
import com.daggerhilt.etisalat.common.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginPresenter @Inject constructor(private val loginUseCase: LoginUseCase) : ViewModel() {

    var newData: MutableLiveData<String>? = MutableLiveData()


    init {
         getLogin()
    }

    private fun getLogin() {
        Log.e("getUserAuthProvider ","getLogin ")

        /*loginUseCase().launchIn(scope = viewModelScope).invokeOnCompletion {

            Log.e("getUserAuthProvider ","invokeOnCompletion "+it?.message)
            if(it==null) {
                newData?.value = "Success"

            }

        }*/

        viewModelScope.launch {
            loginUseCase().collect {result ->
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