package bluetoothLib;

import android.bluetooth.BluetoothSocket;

/**
 * Created by Admin-01 on 2017/12/12.
 */

public interface ConnectionEstablishCallback {

    void onSuccess(BluetoothSocket socket);
    void onFailure(String des);
}
