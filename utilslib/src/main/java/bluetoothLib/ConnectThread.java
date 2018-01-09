package bluetoothLib;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Admin-01 on 2018/1/3.
 * connect to socket
 */

class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private ConnectionEstablishCallback connectCallback;
    private static final  String TAG = "ConnectThread";

    ConnectThread(BluetoothDevice device, String MY_UUID, ConnectionEstablishCallback connectCallback) {
        Log.d(TAG, "ConnectThread: constructor");
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        BluetoothSocket tmp = null;
        this.connectCallback = connectCallback;
        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;
    }

    public void run() {
        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            Log.d(TAG, "run: try to connect");
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
                connectCallback.onFailure(closeException.toString());
                return; //in case of duplicate onFailure called
            }
            connectCallback.onFailure(connectException.toString());
            return;
        }

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        connectCallback.onSuccess(mmSocket);
    }
}
