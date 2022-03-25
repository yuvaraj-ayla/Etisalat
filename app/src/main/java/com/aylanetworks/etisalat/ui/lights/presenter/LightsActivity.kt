package com.aylanetworks.etisalat.ui.lights.presenter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.aylanetworks.etisalat.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LightsActivity : AppCompatActivity() {

    val viewModel: LightsPresenter by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel?.newData?.observe(this, Observer {

            Log.e("getUserAuthProvider ","presenter "+it)

        })
    }
}