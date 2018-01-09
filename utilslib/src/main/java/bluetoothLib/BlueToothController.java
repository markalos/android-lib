package bluetoothLib;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import cn.econtech.www.utilslib.SearchActivity;


/**
 * Created by mark on 2017/12/12.
 *
 */

public class BlueToothController {

    private String MY_UUID;


    private static BluetoothAdapter mBluetoothAdapter;
    private BluetoothConnectedThread bluetoothConnectedThread;

    private static String deviceMacAddress;
    private BLUETOOTH_STATUS status = BLUETOOTH_STATUS.CONNECTION_FREE;
    private static final String TAG = BlueToothController.class.getName();

    static {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    {
        //default uuid
        MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    }


    private Handler mHandler; // handler that gets info from Bluetooth service

    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }

    public boolean setDeviceMacAddress(String macAddress) {
        if (preConnection(macAddress) != MessageConstants.BLUETOOTH_ENVIRONMENT_OKAY) {
            return false;
        }
        if (!macAddress.equals(deviceMacAddress))
            deviceMacAddress = macAddress;
        return true;
    }

    public void pauseReadThread() {
        if (bluetoothConnectedThread == null)
            return;
        bluetoothConnectedThread.pauseThread();
    }



    private BlueToothController() {
        //no instance
    }

    private static class ClassHolder {
        private static final BlueToothController instance = new BlueToothController();
    }

    public static BlueToothController getInstance() {
        BlueToothController instance = ClassHolder.instance;
        if (mBluetoothAdapter == null)
            instance.sendToHandler(MessageConstants.MESSAGE_TOAST, "bluetooth not support");
        return instance;
    }


    /**
     * start scan activity from current context, without connection
     * try to call {@link BlueToothController#connect()} whenever you feel appropriate
     * @param packageContext from where to start search activity
     */
    public void scanDevice(Context packageContext) {
        scanDevice(packageContext, false);
    }

    /**
     *  start scan activity from current context
     * @param packageContext from where to start search activity
     * @param autoConnect to connect after mac address selection or not
     */
    public void scanDevice(Context packageContext, boolean autoConnect) {
        Intent intent = new Intent(packageContext, SearchActivity.class);
        intent.putExtra(MessageConstants.AUTO_CONNECT, autoConnect);
        packageContext.startActivity(intent);
    }

    public void write(byte [] bytes) {
        if (status == BLUETOOTH_STATUS.CONNECTION_ESTABLISHED) {
            bluetoothConnectedThread.write(bytes);
        }
    }

    private void sendToHandler(int what, Object obj) {
        if (mHandler != null) {
            mHandler.obtainMessage(what,obj).sendToTarget();
        }
    }



    private int preConnection(final String deviceAddress) {
        Log.d(TAG, "preConnection: status " + status);
        if (status == BLUETOOTH_STATUS.CONNECTION_ESTABLISHING) {
            Log.d(TAG, "preConnection: establishing");
            sendToHandler(MessageConstants.MESSAGE_TOAST, "is connecting to device : " + deviceMacAddress);
            return MessageConstants.BLUETOOTH_CONNECTING;
        } else if (status == BLUETOOTH_STATUS.CONNECTION_ESTABLISHED) {
            Log.d(TAG, "preConnection: equals = " + deviceMacAddress.equals(deviceAddress));
            if (deviceMacAddress.equals(deviceAddress)) {
                sendToHandler(MessageConstants.MESSAGE_TOAST, "connected to " + deviceMacAddress);
                return MessageConstants.BLUETOOTH_CONNECT_SUCCESS;
            } else {
                disconnect();
                bluetoothConnectedThread = null;
            }
        }

        return MessageConstants.BLUETOOTH_ENVIRONMENT_OKAY;
    }

    private void connectToRemoteDevice(final  BluetoothDevice remoteDevice, final OnSuccessOrFail onSuccessOrFail) {
        final ConnectThread connectThread = new ConnectThread(remoteDevice, MY_UUID, new ConnectionEstablishCallback() {
            @Override
            public void onSuccess(BluetoothSocket socket) {
                Log.d(TAG, "onSuccess: connectToRemoteDevice succeed");
                if (status == BLUETOOTH_STATUS.CONNECTION_FREE) {
                    return;
                }
                bluetoothConnectedThread = new BluetoothConnectedThread(socket, new OnSuccessOrFail() {
                    @Override
                    public void onSuccess() {
                        onSuccessOrFail.onSuccess();
                        status = BLUETOOTH_STATUS.CONNECTION_ESTABLISHED;
                    }
                    @Override
                    public void onFail() {
                        onSuccessOrFail.onFail();
                        status = BLUETOOTH_STATUS.CONNECTION_FREE;
                    }
                });
                bluetoothConnectedThread.setHandler(mHandler);
            }
            @Override
            public void onFailure(String des) {
                status = BLUETOOTH_STATUS.CONNECTION_FREE;
                sendToHandler(MessageConstants.MESSAGE_TOAST, "failed on connecting");
                Log.e(TAG, "onFailure: " + des);
            }
        });
        connectThread.start();
    }

    public void reConnect(final OnSuccessOrFail onSuccessOrFail) {
        if (status == BLUETOOTH_STATUS.CONNECTION_FREE)
            connect(deviceMacAddress, onSuccessOrFail);
    }

    public void connect() {
        connect(new OnSuccessOrFail() {
            @Override
            public void onSuccess() {
                sendToHandler(MessageConstants.MESSAGE_TOAST, "connect succeed");
            }

            @Override
            public void onFail() {
                sendToHandler(MessageConstants.MESSAGE_TOAST, "connect failed");
            }
        });
    }

    public void connect(final OnSuccessOrFail onSuccessOrFail) {
        connect(deviceMacAddress, onSuccessOrFail);
    }

    public void connect(final String deviceAddress, final OnSuccessOrFail onSuccessOrFail) {
        if (!setDeviceMacAddress(deviceAddress)) {
            return;
        }

        BluetoothDevice remoteDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);
        if (null == remoteDevice) {
            Log.e(TAG, "connect: device not found" + deviceAddress);
            sendToHandler(MessageConstants.MESSAGE_TOAST,
                        "connect: device " + deviceAddress + " not found");

        } else {
            status = BLUETOOTH_STATUS.CONNECTION_ESTABLISHING;
            connectToRemoteDevice(remoteDevice, onSuccessOrFail);
        }

    }

    private void disconnect() {
        if (bluetoothConnectedThread != null)
            bluetoothConnectedThread.cancel();
        status = BLUETOOTH_STATUS.CONNECTION_FREE;

    }

    public void destroy() {
        disconnect();
    }

    public void setUUID (String uuidString) {
        MY_UUID = uuidString;
    }




}




