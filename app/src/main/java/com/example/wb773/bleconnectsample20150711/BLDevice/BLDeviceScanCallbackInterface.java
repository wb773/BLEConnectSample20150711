package com.example.wb773.bleconnectsample20150711.BLDevice;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;

/**
 * Created by wb773 on 15/07/14.
 */
public interface BLDeviceScanCallbackInterface {

    public void onScanFinished(ArrayList<BluetoothDevice> devices);

}
