package com.example.wb773.bleconnectsample20150711.BLDevice;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;

/**
 * Created by wb773 on 15/07/11.
 */
public class BLDeviceControl {

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private Handler mHandler;

    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter;

    private BLDeviceControl(){}

    private ArrayList<BluetoothDevice> mDevices;

    public BLDeviceControl(Context applicationContext){
        this.mContext = applicationContext;
    }

    //Bluetoothが使用出来るか確認する
    private BluetoothAdapter checkBLEEnable(){
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        // デバイスがBLEをサポートしているかチェックする
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
           return null;
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        // APIレベル18以上の時、Bluetoothアダプタの参照を取得出来る
        final BluetoothManager bluetoothManager =
                (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            return null;
        }
        return mBluetoothAdapter;
    }

    //当クラスの初期化
    public boolean initBLE(final Context applicationContext){

        return true;
    }

    //BLEデバイススキャンのコールバック
    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    if(!mDevices.contains(device)){
                        mDevices.add(device);
                    }
                }
            };

    //デバイスを検索する
    public List<DeviceHolder> SearchDevice(final Context applicationContext, final BLDeviceScanCallbackInterface callback){
        mHandler = new Handler();
        mHandler.postDelayed(
                new Runnable() {

                    @Override
                    public void run() {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        callback.onScanFinished(mDevices);
                    }
                },
                SCAN_PERIOD
        );

        return null;
    }

    //デバイス情報を管理するためのクラス
    static class DeviceHolder {
        String deviceName;
        String deviceAddress;
    }

}
