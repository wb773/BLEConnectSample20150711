package com.example.wb773.bleconnectsample20150711.BLDevice;

import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wb773.bleconnectsample20150711.R;

import java.util.ArrayList;

//デバイス設定画面
public class BLDeviceScanControlActivity extends AppCompatActivity implements BLDeviceScanCallbackInterface, View.OnClickListener {

    private LeDeviceListAdapter mLeDeviceListAdapter;

    private BLDeviceControl mControl;

    //UIへの参照
    private Button hrSearchButton;
    private TextView hrDeviceName;
    private TextView hrDeviceAddress;

    //Preference
    SharedPreferences mPreference;

    //--------------------------------------------------
    // 標準のイベント
    //--------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //画面の生成
        setContentView(R.layout.activity_bldevice_control);

        try {
            //BLデバイスコントロールの生成
            mControl = new BLDeviceControl(BLDeviceScanControlActivity.this);
        } catch (BLDeviceException e) {
            //BLE対応で無い場合はエラー
            Toast.makeText(
                    BLDeviceScanControlActivity.this,
                    e.getMessage(),
                    Toast.LENGTH_LONG).show();
                    finish();
        }

        //ボタンの取得
        hrSearchButton = (Button)findViewById(R.id.hr_search_button);
        hrSearchButton.setOnClickListener(BLDeviceScanControlActivity.this);

        //HRDevice関連
        hrDeviceName = (TextView)findViewById(R.id.current_hr_device_id);
        hrDeviceAddress = (TextView)findViewById(R.id.current_hr_device_address);

        //プリファレンス
        mPreference = getSharedPreferences("Devices", MODE_PRIVATE);

        //デバイス情報の更新
        refreshDevices();
    }

    //--------------------------------------------------
    // オプションメニュー
    //--------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bldevice_control, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    //--------------------------------------------------
    //  BLDeviceScanCallbackInterfaceイベント
    //--------------------------------------------------

    @Override
    public void onScanFinished(ArrayList<BluetoothDevice> devices) {
        // リストビューの初期化
        mLeDeviceListAdapter = new LeDeviceListAdapter();

        if(devices.isEmpty()){
            Toast.makeText(
                    BLDeviceScanControlActivity.this,
                    "noDeviceScaned...",
                    Toast.LENGTH_LONG).show();
        }else{

            for(BluetoothDevice device : devices){
                mLeDeviceListAdapter.addDevice(device);
            }
        }

        //
        //処理：メッセージダイアログの表示
        //
        AlertDialog.Builder dialog = new AlertDialog.Builder(BLDeviceScanControlActivity.this)
                .setTitle("リストのサンプル")
                .setAdapter(mLeDeviceListAdapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String name =  mLeDeviceListAdapter.getItem(which).getName();
                        String address = mLeDeviceListAdapter.getItem(which).getAddress();

                        //シェアードプリファレンスに設定
                        SharedPreferences.Editor editor = mPreference.edit();
                        editor.putString("hrDeviceName",name);
                        editor.putString("hrDeviceAddress",address);
                        editor.commit();

                        //デバイス情報の更新
                        refreshDevices();

                        //選択したアイテムをトースト表示
                        Toast.makeText(
                                BLDeviceScanControlActivity.this,
                                getString(R.string.debug_selected_item, which, name, address),
                                Toast.LENGTH_LONG).show();
                    }
                });

        dialog.show();
    }



    //--------------------------------------------------
    // 操作
    //--------------------------------------------------
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.hr_search_button:
                //心拍計検索ボタン押下時

                //メッセージを表示
                Toast.makeText(BLDeviceScanControlActivity.this, "SearchDevice...", Toast.LENGTH_LONG).show();

                //デバイスを検索する
                mControl.SearchDevice(BLDeviceScanControlActivity.this);
                break;
        }
    }

    public void refreshDevices(){
        BLDeviceScanControlActivity.this.hrDeviceName.setText(mPreference.getString("hrDeviceName", ""));
        BLDeviceScanControlActivity.this.hrDeviceAddress.setText(mPreference.getString("hrDeviceAddress",""));
    }

    /** ------------------------------------------------------------------
     *  リストアダプタ
     * ------------------------------------------------------------------*/
    //デバイス情報を管理するためのクラス
    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDeviceList;
        private LayoutInflater mInflator;

        //コンストラクタ
        public LeDeviceListAdapter() {
            super();
            mLeDeviceList = new ArrayList<BluetoothDevice>();
            mInflator = BLDeviceScanControlActivity.this.getLayoutInflater();
        }

        //リストに追加する
        public void addDevice(BluetoothDevice device) {
            //存在しない場合追加する
            if(!mLeDeviceList.contains(device)) {
                mLeDeviceList.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDeviceList.get(position);
        }
        public void clear() {
            mLeDeviceList.clear();
        }
        @Override
        public int getCount() {
            return mLeDeviceList.size();
        }
        @Override
        public BluetoothDevice getItem(int i) { return mLeDeviceList.get(i); }
        @Override
        public long getItemId(int i) { return i; }
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDeviceList.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }


}
