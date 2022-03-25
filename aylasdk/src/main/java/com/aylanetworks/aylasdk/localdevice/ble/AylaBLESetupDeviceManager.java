package com.aylanetworks.aylasdk.localdevice.ble;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.android.volley.Response;
import com.aylanetworks.aylasdk.AylaAPIRequest;
import com.aylanetworks.aylasdk.error.ErrorListener;
import com.aylanetworks.aylasdk.localdevice.AylaLocalDevice;
import com.aylanetworks.aylasdk.setup.AylaBLEWiFiSetupDevice;
import com.aylanetworks.aylasdk.setup.ble.AylaGenericGattService;
import com.aylanetworks.aylasdk.setup.ble.AylaWiFiConfigGattService;

import java.util.ArrayList;
import java.util.List;

public class AylaBLESetupDeviceManager extends AylaBLEDeviceManager {

    /**
     * AylaBLEDeviceManager constructor.
     *
     * @param context Context for the application
     */
    public AylaBLESetupDeviceManager(Context context) {
        super(context);
    }

    /**
     * Scans for BLE Setup devices, in vicinity, that's broadcasting itself with
     * service UUID {@link AylaGenericGattService#SERVICE_UUID}. These devices
     * can be configured to join the network by providing WiFi SSID and password
     * over BLE.
     *
     * @param scanFilter used to filter the set of returned devices.
     * @param timeoutInMs Maximum time the search should take in ms.
     * @param successListener Listener to be notified with the results.
     * @param errorListener Listener to receive an error should one occur.
     *
     * @return a cancelable AylaAPIRequest
     */
    public AylaAPIRequest findBLESetupDevices(final ScanFilter scanFilter,
                                              final int timeoutInMs,
                                              final Response.Listener<AylaBLEWiFiSetupDevice[]> successListener,
                                              final ErrorListener errorListener) {
        return findLocalDevices(scanFilter, timeoutInMs,
                new Response.Listener<AylaLocalDevice[]>() {
                    @Override
                    public void onResponse(AylaLocalDevice[] response) {
                        List<AylaBLEWiFiSetupDevice> devices = new ArrayList<>();
                        for (AylaLocalDevice device : response) {
                            if (device instanceof AylaBLEWiFiSetupDevice) {
                                devices.add((AylaBLEWiFiSetupDevice) device);
                            }
                        }
                        successListener.onResponse(devices.toArray(
                                new AylaBLEWiFiSetupDevice[devices.size()]));
                    }
                }, errorListener);
    }

    @Override
    protected AylaBLEDevice createLocalDevice(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {

        ScanRecordHelper helper = ScanRecordHelper.parseFromBytes(scanRecord);
        if (helper.containsService(AylaGenericGattService.SERVICE_UUID)
             || helper.containsService(AylaWiFiConfigGattService.SERVICE_UUID)) {
            return new AylaBLEWiFiSetupDevice(bluetoothDevice, rssi, scanRecord);
        }

        return new AylaBLEDevice(bluetoothDevice, rssi, scanRecord);
    }
}
