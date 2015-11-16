package com.example.hung.bleadvertise;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.UUID;

public class MainActivity extends Activity {
    private TextView logText;
    private BroadcastReceiver mReceiver;
    private BluetoothLeAdvertiser leAdvertiser;
    private AdvertiseCallback advertiseCallback;
    private StringBuilder txtBuilder = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logText = (TextView) findViewById(R.id.tv_supported);

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Add the name and address to an array adapter to show in a ListView
                    // mArrayAdapter.add(device.getName() + "\n" + device.getAddress());

                    txtBuilder.append("Found! - ").append(device.getName()).append(" : ").append(device.getAddress());
                    logText.setText(txtBuilder.toString());
                }
            }
        };

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        // check if BLE is supported on the device
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            txtBuilder.append("BLE Supported").append("\n");

            logText.setText(txtBuilder.toString());
        }

        BluetoothAdapter adapter = bluetoothManager.getAdapter();
        leAdvertiser = adapter.getBluetoothLeAdvertiser();

        // Ensures Bluetooth is available on the device and it is enabled
        if (adapter == null || !adapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            int REQUEST_ENABLE_BT = 1;
            startActivityForResult(intent, REQUEST_ENABLE_BT);
        }

        startAdvertising();
    }

    private void startAdvertising() {
        txtBuilder.append("Activity : Starting Advertising").append("\n");
        logText.setText(txtBuilder.toString());

        if (advertiseCallback == null) {


            AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
            settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);
            settingsBuilder.setTimeout(0);

            // settings
            AdvertiseSettings settings = settingsBuilder.build();

            ParcelUuid uuid = ParcelUuid.fromString(UUID.randomUUID().toString());
            AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
            dataBuilder.addServiceUuid(uuid);
            dataBuilder.setIncludeDeviceName(true);

            // data
            AdvertiseData data = dataBuilder.build();

            // callback
            advertiseCallback = new SampleAdvertiseCallback();

            if (leAdvertiser != null) {
                leAdvertiser.startAdvertising(settings, data, advertiseCallback);
                txtBuilder.append("UUID : ").append(uuid.toString()).append("\n");
                logText.setText(txtBuilder.toString());
            }
        }
    }

    private void stopAdvertising() {
        txtBuilder.append("Activity : Stop Advertising").append("\n");
        logText.setText(txtBuilder.toString());

        if (leAdvertiser != null) {
            leAdvertiser.stopAdvertising(advertiseCallback);
            advertiseCallback = null;
        }
    }

    protected void onPause() {
        super.onPause();
        txtBuilder.append("onPause").append("\n");
        logText.setText(txtBuilder.toString());
        stopAdvertising();
    }

    protected void onStop() {
        super.onStop();
        txtBuilder.append("onStop").append("\n");
        logText.setText(txtBuilder.toString());
    }

    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private class SampleAdvertiseCallback extends AdvertiseCallback {
        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            txtBuilder.append("Advertising Failed").append("\n");
            logText.setText(txtBuilder.toString());

        }

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            txtBuilder.append("Advertising Successfully started").append("\n");
            logText.setText(txtBuilder.toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
