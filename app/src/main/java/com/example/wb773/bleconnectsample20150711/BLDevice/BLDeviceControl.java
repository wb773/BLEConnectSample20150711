package com.example.wb773.bleconnectsample20150711.BLDevice;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

import com.example.wb773.bleconnectsample20150711.R;

import java.util.ArrayList;

import java.util.List;
import java.util.UUID;

/**
 * Created by wb773 on 15/07/11.
 */
public class BLDeviceControl {

    private static final String TAG = "BLDeviceControl";

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private Handler mHandler;

    private Activity mContext;
    private BluetoothAdapter mBluetoothAdapter;

    private BLDeviceControl(){}

    private ArrayList<BluetoothDevice> mDevices;

    public static final String HEART_RATE_SERVICE_UUID = "0000180d-0000-1000-8000-00805f9b34fb";
    public static final String HEART_RATE_CHARACTARISTIC_UUID = "00002a37-0000-1000-8000-00805f9b34fb";
    public static final String CLIENT_CHARACTERISTIC_CONFIG_UUID = "00002902-0000-1000-8000-00805f9b34fb";

    //Gatt
    private BluetoothGatt mBluetoothGatt;

    public BLDeviceControl(Activity applicationContext) throws BLDeviceException {
        this.mContext = applicationContext;
        if(!checkBLEEnable()){
            throw new BLDeviceException("Cannot use BLDevise");
        }
        checkBLEEnable();

    }

    //Bluetoothが使用出来るか確認する
    private boolean checkBLEEnable(){
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        // デバイスがBLEをサポートしているかチェックする
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
           return false;
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        // APIレベル18以上の時、Bluetoothアダプタの参照を取得出来る
        final BluetoothManager bluetoothManager =
                (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            return false;
        }
        return true;
    }

    //BLEデバイススキャンのコールバック
    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

                    Log.i(TAG,"onLeScan:" + device.getName());

                    if(!mDevices.contains(device)){
                        mDevices.add(device);
                    }
                }
            };

    //デバイスを検索する
    public List<DeviceHolder> SearchDevice(final BLDeviceScanCallbackInterface callback){
        mDevices = new ArrayList<BluetoothDevice>();
        mHandler = new Handler(); //SCAN_PERIOD後に処理を終了するための装置

        //検索中ダイアログの表示
        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setTitle(mContext.getString(R.string.devicescandialog_title));
        progressDialog.setMessage(mContext.getString(R.string.devicescandialog_message));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        //タイムアウト処理
        mHandler.postDelayed(
                new Runnable() {

                    @Override
                    public void run() {
                        Log.i(TAG,mBluetoothAdapter.toString());
                        Log.i(TAG, mLeScanCallback.toString());
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        progressDialog.dismiss();
                        callback.onScanFinished(mDevices);
                    }
                },
                SCAN_PERIOD
        );
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        if(!mBluetoothAdapter.startLeScan(mLeScanCallback)){
            Log.i(TAG,"Cannot start Le Scan");
        }else{
            Log.i(TAG,"Start Scanning");
        }
        return null;
    }

    //デバイス情報を管理するためのクラス
    static class DeviceHolder {
        String deviceName;
        String deviceAddress;
    }



    /** ------------------------------------------------------------------
     *  GATTへの接続
     * ------------------------------------------------------------------*/

    public void connectGATT(BluetoothDevice device){
        // GATT接続を試みる
        mBluetoothGatt = device.connectGatt(mContext, false, mBluetoothGattCallback);
    }

    private final BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "onConnectionStateChange: " + status + " -> " + newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // GATTへ接続成功
                // サービスを検索する
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // GATT通信から切断された
                mBluetoothGatt = null;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "onServicesDiscovered received: " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(UUID.fromString(HEART_RATE_SERVICE_UUID));
                if (service == null) {

                }else{
                    // サービスを見つけた

                    BluetoothGattCharacteristic characteristic =
                            service.getCharacteristic(UUID.fromString(HEART_RATE_CHARACTARISTIC_UUID));

                    if (characteristic == null) {


                    }else{
                        // キャラクタリスティックを見つけた

                        // Notification を要求する
                        boolean registered = gatt.setCharacteristicNotification(characteristic, true);

                        // Characteristic の Notification 有効化
                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                                UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG_UUID));
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);
                    }
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Log.d(TAG, "onCharacteristicRead: " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "onCharacteristicChanged");
            // Characteristicの値更新通知

            if (HEART_RATE_CHARACTARISTIC_UUID.equals(characteristic.getUuid().toString())) {
                Byte value = characteristic.getValue()[0];
                boolean left = (0 < (value & 0x02));
                boolean right = (0 < (value & 0x01));
            }
        }
    };
}
