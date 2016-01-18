package ch.sylvac.calipers;

import java.util.HashMap;

/**
 *
 * http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml

 *
 01-17 19:09:17.393 14559-14559/com.example.android.bluetoothlegatt E/BluetoothService: 00001800-0000-1000-8000-00805f9b34fb
 01-17 19:09:17.393 14559-14559/com.example.android.bluetoothlegatt E/BluetoothCharacteristic: 00002a00-0000-1000-8000-00805f9b34fb
 01-17 19:09:17.393 14559-14559/com.example.android.bluetoothlegatt E/BluetoothCharacteristic: 00002a01-0000-1000-8000-00805f9b34fb
 01-17 19:09:17.393 14559-14559/com.example.android.bluetoothlegatt E/BluetoothCharacteristic: 00002a04-0000-1000-8000-00805f9b34fb
 01-17 19:09:17.393 14559-14559/com.example.android.bluetoothlegatt E/BluetoothService: 00001801-0000-1000-8000-00805f9b34fb
 01-17 19:09:17.393 14559-14559/com.example.android.bluetoothlegatt E/BluetoothService: c1b25000-caaf-6d0e-4c33-7dae30052840
 01-17 19:09:17.393 14559-14559/com.example.android.bluetoothlegatt E/BluetoothCharacteristic: c1b25010-caaf-6d0e-4c33-7dae30052840
 01-17 19:09:17.393 14559-14559/com.example.android.bluetoothlegatt E/BluetoothCharacteristic: c1b25011-caaf-6d0e-4c33-7dae30052840
 01-17 19:09:17.393 14559-14559/com.example.android.bluetoothlegatt E/BluetoothCharacteristic: c1b25012-caaf-6d0e-4c33-7dae30052840
 01-17 19:09:17.393 14559-14559/com.example.android.bluetoothlegatt E/BluetoothCharacteristic: c1b25013-caaf-6d0e-4c33-7dae30052840
 */
public class SCalEvoBluetoothSpecifications {
    private static HashMap<String, String> attributes = new HashMap();
    public static String SERVICE_A = "00001800-0000-1000-8000-00805f9b34fb";
    public static String SERVICE_B = "00001801-0000-1000-8000-00805f9b34fb";
    public static String SERVICE_C = "c1b25000-caaf-6d0e-4c33-7dae30052840";

    public static String CHARACTERISTIC_M = "00002a00-0000-1000-8000-00805f9b34fb";
    public static String CHARACTERISTIC_N = "00002a01-0000-1000-8000-00805f9b34fb";
    public static String CHARACTERISTIC_O = "00002a04-0000-1000-8000-00805f9b34fb";

    public static String CHARACTERISTIC_W = "c1b25010-caaf-6d0e-4c33-7dae30052840";
    public static String CHARACTERISTIC_X = "c1b25011-caaf-6d0e-4c33-7dae30052840";
    public static String CHARACTERISTIC_Y = "c1b25012-caaf-6d0e-4c33-7dae30052840";
    public static String CHARACTERISTIC_Z = "c1b25013-caaf-6d0e-4c33-7dae30052840";

    static {
        // Services
        attributes.put(SERVICE_A, "SERVICE_A");
        attributes.put(SERVICE_B, "SERVICE_B");
        attributes.put(SERVICE_C, "SERVICE_C");

        // SERVICE_A Characteristics
        attributes.put(CHARACTERISTIC_M, "CHARACTERISTIC_M");
        attributes.put(CHARACTERISTIC_N, "CHARACTERISTIC_N");
        attributes.put(CHARACTERISTIC_O, "CHARACTERISTIC_O");

        // SERVICE_B Characteristics

        // SERVICE_C Characteristics
        attributes.put(CHARACTERISTIC_W, "CHARACTERISTIC_W");
        attributes.put(CHARACTERISTIC_X, "CHARACTERISTIC_X");
        attributes.put(CHARACTERISTIC_Y, "CHARACTERISTIC_Y");
        attributes.put(CHARACTERISTIC_Z, "CHARACTERISTIC_Z");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
