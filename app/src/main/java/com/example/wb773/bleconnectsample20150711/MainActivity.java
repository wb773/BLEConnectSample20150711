package com.example.wb773.bleconnectsample20150711;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.wb773.bleconnectsample20150711.BLDevice.BLDeviceScanControlActivity;


public class MainActivity extends AppCompatActivity {

    public static final int OPTION_MENU_OPEN_DEVICE_CONTROL= 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        menu.add(0, OPTION_MENU_OPEN_DEVICE_CONTROL, 1, getString(R.string.optionmenu_open_devicecontrol));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        switch (item.getItemId()) {
            case OPTION_MENU_OPEN_DEVICE_CONTROL:

                Intent intent = new Intent(this,BLDeviceScanControlActivity.class);
                startActivity(intent);


                break;
        }

        return super.onOptionsItemSelected(item);
    }



}
