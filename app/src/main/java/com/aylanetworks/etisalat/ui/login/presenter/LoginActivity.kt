package com.aylanetworks.etisalat.ui.login.presenter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.aylanetworks.etisalat.R
import com.aylanetworks.etisalat.ui.lights.presenter.LightsActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    val viewModel: LoginPresenter by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel?.newData?.observe(this, Observer {

           // Log.e("getUserAuthProvider ","itstts "+it)
            if(it.equals("Success")) {
                val intent = Intent(this, LightsActivity::class.java)
                startActivity(intent)
            }

        })
    }
}