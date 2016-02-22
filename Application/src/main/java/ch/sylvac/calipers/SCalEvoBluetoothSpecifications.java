package ch.sylvac.calipers;

import java.util.HashMap;

/**
 * http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
 * <p/>
 * <p/>
 * 01-17 19:09:17.393 14559-14559/com.example.android.bluetoothlegatt E/BluetoothService: 00001800-0000-1000-8000-00805f9b34fb
 * 01-17 19:09:17.393 14559-14559/com.example.android.bluetoothlegatt E/BluetoothCharacteristic: 00002a00-0000-1000-8000-00805f9b34fb
 * 01-17 19:09:17.393 14559-14559/com.example.android.bluetoothlegatt E/BluetoothCharacteristic: 00002a01-0000-1000-8000-00805f9b34fb
 * 01-17 19:09:17.393 14559-14559/com.example.android.bluetoothlegatt E/BluetoothCharacteristic: 00002a04-0000-1000-8000-00805f9b34fb
 * 01-17 19:09:17.393 14559-14559/com.example.android.bluetoothlegatt E/BluetoothService: 00001801-0000-1000-8000-00805f9b34fb
 * 01-17 19:09:17.393 14559-14559/com.example.android.bluetoothlegatt E/BluetoothService: c1b25000-caaf-6d0e-4c33-7dae30052840
 * 01-17 19:09:17.393 14559-14559/com.example.android.bluetoothlegatt E/BluetoothCharacteristic: c1b25010-caaf-6d0e-4c33-7dae30052840
 * 01-17 19:09:17.393 14559-14559/com.example.android.bluetoothlegatt E/BluetoothCharacteristic: c1b25011-caaf-6d0e-4c33-7dae30052840
 * 01-17 19:09:17.393 14559-14559/com.example.android.bluetoothlegatt E/BluetoothCharacteristic: c1b25012-caaf-6d0e-4c33-7dae30052840
 * 01-17 19:09:17.393 14559-14559/com.example.android.bluetoothlegatt E/BluetoothCharacteristic: c1b25013-caaf-6d0e-4c33-7dae30052840
 */
public class SCalEvoBluetoothSpecifications {
    private static HashMap<String, String> attributes = new HashMap();
    public static String SERVICE_A = "00001800-0000-1000-8000-00805f9b34fb";
    public static String SERVICE_B = "00001801-0000-1000-8000-00805f9b34fb";
    public static String METROLOGY = "c1b25000-caaf-6d0e-4c33-7dae30052840";

    public static String CHARACTERISTIC_M = "00002a00-0000-1000-8000-00805f9b34fb";
    public static String CHARACTERISTIC_N = "00002a01-0000-1000-8000-00805f9b34fb";
    public static String CHARACTERISTIC_O = "00002a04-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    public static String DATA_RECEIVED = "c1b25010-caaf-6d0e-4c33-7dae30052840";
    public static String NOT_USED = "c1b25011-caaf-6d0e-4c33-7dae30052840";
    public static String DATA_REQUEST_OR_COMMAND = "c1b25012-caaf-6d0e-4c33-7dae30052840";
    public static String ANSWER_TO_REQUEST_OR_COMMAND = "c1b25013-caaf-6d0e-4c33-7dae30052840";

    static {
        // Services
        attributes.put(SERVICE_A, "SERVICE_A");
        attributes.put(SERVICE_B, "SERVICE_B");
        attributes.put(METROLOGY, "METROLOGY");

        // SERVICE_A Characteristics
        attributes.put(CHARACTERISTIC_M, "CHARACTERISTIC_M");
        attributes.put(CHARACTERISTIC_N, "CHARACTERISTIC_N");
        attributes.put(CHARACTERISTIC_O, "CHARACTERISTIC_O");

        // SERVICE_B Characteristics

        // METROLOGY Characteristics
        attributes.put(DATA_RECEIVED, "DATA_RECEIVED");
        attributes.put(NOT_USED, "NOT_USED");
        attributes.put(DATA_REQUEST_OR_COMMAND, "DATA_REQUEST_OR_COMMAND");
        attributes.put(ANSWER_TO_REQUEST_OR_COMMAND, "ANSWER_TO_REQUEST_OR_COMMAND");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
