package bluetoothLib;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import cn.econtech.www.utilslib.DaemonThread;

/**
 * Created by Admin-01 on 2017/12/25.
 *
 */

class BluetoothConnectedThread{
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private Handler mHandler;
    private DaemonThread readThread;
    private static final String TAG = BluetoothConnectedThread.class.getName();

    BluetoothConnectedThread(BluetoothSocket socket, OnSuccessOrFail onSuccessOrFail) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams; using temp objects because
        // member streams are final.
        try {
            tmpIn = socket.getInputStream();

        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating input stream", e);
            onSuccessOrFail.onFail();
        }
        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating output stream", e);
            onSuccessOrFail.onFail();
        }
        onSuccessOrFail.onSuccess();
        mmInStream = tmpIn;
        mmOutStream = tmpOut;

        readThread = new DaemonThread(new DaemonThread.Process() {
            byte[] mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            @Override
            public void process() {
                // Read from the InputStream.
                try {
                    numBytes = mmInStream.read(mmBuffer);
                } catch (IOException e) {
                    Log.d(TAG, "read from stream failed!", e);
                } catch (NullPointerException e) {
                    Log.e(TAG, "process: null input stream", e);
                }
                if (numBytes > 0) {
                    // Send the obtained bytes to the UI activity.
                    sendToHandler(MessageConstants.MESSAGE_READ, Arrays.copyOf(mmBuffer, numBytes), -1, -1);
                }
            }
        });
        readThread.start();
    }

    void setHandler(Handler handler) {
        mHandler = handler;
    }

    private void sendToHandler(int what, Object obj, int arg1, int arg2) {
        if (mHandler != null) {
            mHandler.obtainMessage(what, arg1, arg2, obj).sendToTarget();
        }
    }



    // Call this from the main activity to send data to the remote device.
    void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);

            // Share the sent message with the UI activity.
            sendToHandler(MessageConstants.MESSAGE_WRITE, MessageConstants.BLUETOOTH_WRITE_SUCCEED, -1, -1);
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when sending data", e);

            // Send a failure message back to the activity.
            Message writeErrorMsg =
                    mHandler.obtainMessage(MessageConstants.MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString("toast",
                    "Couldn't send data to the other device");
            writeErrorMsg.setData(bundle);
            mHandler.sendMessage(writeErrorMsg);
        }
    }

    // Call this method from the main activity to shut down the connection.
    synchronized void cancel() {
        try {
            if (mmSocket != null) {
                readThread.stopThread();
                mmSocket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }

    synchronized void startThread() {
        readThread.startThread();
    }

    synchronized void pauseThread() {
        readThread.pauseThread();
    }


}