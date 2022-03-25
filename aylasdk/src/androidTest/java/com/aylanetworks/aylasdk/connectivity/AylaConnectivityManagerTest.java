package com.aylanetworks.aylasdk.connectivity;

import android.app.Service;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class AylaConnectivityManagerTest {

    private static final String TAG = "AylaWifiConnectivityMan";

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetCurrentConnectedNetwork() {
        Context context = InstrumentationRegistry.getContext();
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Service.CONNECTIVITY_SERVICE);
        Network[] networks = cm.getAllNetworks();
        for (Network n : networks) {
            Log.d(TAG, "network:" + n);
            NetworkInfo info = cm.getNetworkInfo(n);
            Log.d(TAG, "network info:" + info);
        }
    }
}