/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import ch.sylvac.calipers.SCalEvoBluetoothSpecifications;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRA_DEVICE_ADDRESS = "deviceAddress";
    public static final String EXTRA_METADATA = "metadata";
    public static final String EXTRA_VALUE = "value";

    public static final String PREF_NAME = "calipers";
    public static final String PREF_MAC_ADDRESS = "macAddress";

    public static final int REQUEST_CONNECT_DEVICE = 233;
    public static final int REQUEST_ENABLE_BT = 235;

    private Intent mReturnIntent;
    private TextView mConnectionState;
    private Button mToggleConnectButton;
    private TextView mDataField;
    private TextView mDeviceAddressTextView;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private Ringtone mDataRecievedSound;

    private ExpandableListView mGattServicesList;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    public static BluetoothGattCharacteristic mDataReceivedCharacteristic;
    public static BluetoothGattCharacteristic mDataRequestOrCommandCharacteristic;
    public static BluetoothGattCharacteristic mAnswerToDataRequestOrCommandCharacteristic;

    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                listenToCalipersDataButton();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(EXTRA_VALUE));
            } else {
                Log.e(TAG, "Received something else");
            }
        }
    };

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner = new ExpandableListView.OnChildClickListener() {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                    int childPosition, long id) {
            if (mGattCharacteristics != null) {
                final BluetoothGattCharacteristic characteristic =
                        mGattCharacteristics.get(groupPosition).get(childPosition);
                final int charaProp = characteristic.getProperties();
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                    // If there is an active notification on a characteristic, clear
                    // it first so it doesn't update the data field on the user interface.
                    if (mNotifyCharacteristic != null) {
                        mBluetoothLeService.setCharacteristicNotification(
                                mNotifyCharacteristic, false);
                        mNotifyCharacteristic = null;
                    }
                    mBluetoothLeService.readCharacteristic(characteristic);
                }
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    listenToCaliperMeasurements(characteristic);
                }
                return true;
            }
            return false;
        }
    };

    private void clearUI() {
        mDataField.setText(R.string.no_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);

        // Prepare returned intent
        mReturnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, mReturnIntent);
        mReturnIntent.putExtra(EXTRA_METADATA, "{}");

        // Sets up UI references.
        mDeviceAddressTextView = (TextView) findViewById(R.id.device_address);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mToggleConnectButton = (Button) findViewById(R.id.toggle_connect_button);
        mDataField = (TextView) findViewById(R.id.data_value);

        mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        mGattServicesList.setOnChildClickListener(servicesListClickListner);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mDataRecievedSound = RingtoneManager.getRingtone(getApplicationContext(), notification);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mDataField.setText(getIntent().getStringExtra(EXTRA_VALUE));

        mDeviceAddress = getIntent().getStringExtra(EXTRA_DEVICE_ADDRESS);
        if (mDeviceAddress == null || "".equals(mDeviceAddress)) {
            mDeviceAddress = getSharedPreferences(PREF_NAME, MODE_PRIVATE).getString(PREF_MAC_ADDRESS, "");
            if (!"".equals(mDeviceAddress)) {
                mDeviceAddressTextView.setText(mDeviceAddress);
            } else {
                mDeviceAddress = null;
                Intent serverIntent = new Intent(this, DeviceScanActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            }
        } else {
            mDeviceAddressTextView.setText(mDeviceAddress);
        }
    }

    public void sendMessage(View v) {
        final EditText sendEditText = (EditText) findViewById(R.id.send_value);
        String message = sendEditText.getText().toString();
        if (mDataRequestOrCommandCharacteristic == null) {
            Log.e("BluetoothSend", "Cant send " + message);
            return;
        }
        Log.e("BluetoothSend", "Will send " + message);
        mBluetoothLeService.writeCharacteristic(mDataRequestOrCommandCharacteristic, message);
        listenToCaliperChanges();
    }

    public void readCalipersDataButton(View v) {
        listenToCalipersDataButton();
    }

    public void readCalipersContinuouslyButton(View v) {
        listenToCaliperChanges();
    }

    public void listenToCalipersDataButton() {
        listenToCaliperMeasurements(mDataReceivedCharacteristic);
    }

    public void listenToCaliperChanges() {
        listenToCaliperMeasurements(mAnswerToDataRequestOrCommandCharacteristic);
    }

    public void listenToCaliperMeasurements(final BluetoothGattCharacteristic characteristic) {
        if (characteristic == null) {
            Log.e(TAG, "Haven't discovered this feature yet, please wait until it has connected once, and or try disconnecting and reconnecting.");
            return;
        }

        Log.d(TAG, "Starting to listen to notifications from " + characteristic.getUuid());
        final int charaProp = characteristic.getProperties();
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            // If there is an active notification on a characteristic, clear
            // it first so it doesn't update the data field on the user interface.
            if (mNotifyCharacteristic != null) {
                mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
                mNotifyCharacteristic = null;
            }
            mBluetoothLeService.readCharacteristic(characteristic);
        }
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            mNotifyCharacteristic = characteristic;
            mBluetoothLeService.setCharacteristicNotification(characteristic, true);

            // Set descriptor to enable notifications
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(SCalEvoBluetoothSpecifications.CLIENT_CHARACTERISTIC_CONFIG));
            if (descriptor == null) {
                Log.e(TAG, "descriptor " + SCalEvoBluetoothSpecifications.CLIENT_CHARACTERISTIC_CONFIG + "was null, looking for others.");

                List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
                String uuid;
                for (BluetoothGattDescriptor aDescriptor : descriptors) {
                    uuid = aDescriptor.getUuid().toString();
                    Log.e(TAG, "BluetoothGattDescriptor " + uuid);
                    descriptor = aDescriptor;
                }
                if (descriptor == null) {
                    Log.e(TAG, "Couldn't find any descriptors for this characteristic, cant enable notifications");
                    return;
                } else {
                    Log.e(TAG, "Trying the last descriptor to enable notifications");
                }
            }
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothLeService.writeDescriptor(descriptor);
            Toast.makeText(this, "Ready", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Ready to read from characteristic " + characteristic.getUuid());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mGattUpdateReceiver);
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    public void toggleConnect(View view) {
        if (!mConnected) {
            mBluetoothLeService.connect(mDeviceAddress);
        } else {
            mBluetoothLeService.disconnect();
        }
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
                if (mConnected) {
                    mToggleConnectButton.setText(R.string.menu_disconnect);
                } else {
                    mToggleConnectButton.setText(R.string.menu_connect);
                }
            }
        });
    }

    private void displayData(String data) {
        if (data != null) {
            mDataField.setText(data);
            Log.d(TAG, "displaying: " + data);

            if (mDataRecievedSound != null) {
                mDataRecievedSound.play();
            }

            mReturnIntent.putExtra(EXTRA_VALUE, data);
            setResult(Activity.RESULT_OK, mReturnIntent);
            // onPause();
        }
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        if (mDataReceivedCharacteristic != null) {
            Log.e(TAG, "Dont need to re list the characteristics, we already found them.");
            return;
        }
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            Log.e("BluetoothService", uuid);

            currentServiceData.put(LIST_NAME, SCalEvoBluetoothSpecifications.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                Log.e("BluetoothCharacteristic", uuid);
                currentCharaData.put(LIST_NAME, SCalEvoBluetoothSpecifications.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
                if (SCalEvoBluetoothSpecifications.DATA_RECEIVED.equals(uuid)) {
                    mDataReceivedCharacteristic = gattCharacteristic;
                } else if (SCalEvoBluetoothSpecifications.ANSWER_TO_REQUEST_OR_COMMAND.equals(uuid)) {
                    mAnswerToDataRequestOrCommandCharacteristic = gattCharacteristic;
                } else if (SCalEvoBluetoothSpecifications.DATA_REQUEST_OR_COMMAND.equals(uuid)) {
                    mDataRequestOrCommandCharacteristic = gattCharacteristic;
                }

                List<BluetoothGattDescriptor> descriptors = gattCharacteristic.getDescriptors();
                for (BluetoothGattDescriptor aDescriptor : descriptors) {
                    uuid = aDescriptor.getUuid().toString();
                    Log.e(TAG, "BluetoothGattDescriptor " + uuid);
                }
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2},
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2}
        );
        mGattServicesList.setAdapter(gattServiceAdapter);

        listenToCalipersDataButton();
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    mDeviceAddress = data.getExtras().getString(EXTRA_DEVICE_ADDRESS);
                    if (mDeviceAddress == null || "".equals(mDeviceAddress)) {
                        Log.d(TAG, "Mac address was not returned.");
                        Toast.makeText(this, R.string.bluetooth_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    mDeviceAddressTextView.setText(mDeviceAddress);

                    // Save mac address
                    SharedPreferences.Editor editor = getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
                    editor.putString(PREF_MAC_ADDRESS, mDeviceAddress);
                    editor.commit();
                } else {
                    Toast.makeText(this, R.string.bluetooth_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode != Activity.RESULT_OK) {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bluetooth_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }
}