package cn.econtech.www.econapp;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import java.lang.ref.WeakReference;

import bluetoothLib.BlueToothController;
import bluetoothLib.MessageConstants;
import cn.econtech.www.utilslib.FileLogger;
import cn.econtech.www.utilslib.LoggingExceptionHandler;
import cn.econtech.www.utilslib.SearchActivity;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, SearchBluetoothFragment.OnItemSelected {

    private static final String TAG = MainActivity.class.getName();


    BlueToothController blueToothController;

    private FragmentManager fragmentManager;
    private RealTimeFragment realTimeFragment;
    private CurveFragment curveFragment;
    private SearchBluetoothFragment searchBluetoothFragment;

    private int currentFragmentIdx = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        new LoggingExceptionHandler(this);

        fragmentManager = getFragmentManager();

        setContentView(R.layout.activity_main);
        setUpBluetoothActivity();

        findViewById(R.id.searchFragmentTab).setOnClickListener(this);
        findViewById(R.id.pauseButton).setOnClickListener(this);

        findViewById(R.id.realTimeFragmentTab).setOnClickListener(this);
        findViewById(R.id.curveFragmentTab).setOnClickListener(this);
        switchFragment(R.id.realTimeFragmentTab);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    private void setUpBluetoothActivity() {
        BluetoothHandler bluetoothHandler = new BluetoothHandler(this);
        blueToothController = BlueToothController.getInstance();
        blueToothController.setHandler(bluetoothHandler);
    }

    public void onClick(View view) {
            switch (view.getId()) {
                case R.id.pauseButton :
                    blueToothController.pauseReadThread();
                    if (realTimeFragment != null)
                        realTimeFragment.pauseRefresh();
                    BlueToothController.getInstance().scanDevice(this, true);
                    break;
                default :
                    switchFragment(view.getId());
                    break;
            }
        Log.d(TAG, "onClick: " + view);
    }

    private void switchFragment(int idx) {
        Log.d(TAG, "switchFragment: " + idx);
        if (idx == currentFragmentIdx)
            return;
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        hideFragments(fragmentTransaction, currentFragmentIdx);
        currentFragmentIdx = idx;
        switch (idx) {
            case R.id.realTimeFragmentTab:
                if (realTimeFragment == null) {
                    realTimeFragment = new RealTimeFragment();
                    fragmentTransaction.add(R.id.fragmentWrapper, realTimeFragment);
                } else {
                    fragmentTransaction.show(realTimeFragment);
                }
                break;
            case R.id.curveFragmentTab:
                if (curveFragment == null) {
                    curveFragment = new CurveFragment();
                    fragmentTransaction.add(R.id.fragmentWrapper, curveFragment);
                } else {
                    fragmentTransaction.show(curveFragment);
                }
                break;
            case  R.id.searchFragmentTab:
                if (null == searchBluetoothFragment) {
                    searchBluetoothFragment = new SearchBluetoothFragment();
                    fragmentTransaction.add(R.id.fragmentWrapper, searchBluetoothFragment);
                } else {
                    fragmentTransaction.show(searchBluetoothFragment);
                }
                break;
        }
        fragmentTransaction.commit();
    }

    private void hideFragments(FragmentTransaction fragmentTransaction, int idx) {
        switch (idx) {
            case R.id.searchFragmentTab:
                if (null != searchBluetoothFragment)
                    fragmentTransaction.hide(searchBluetoothFragment);
                break;
            case R.id.realTimeFragmentTab :
                if (realTimeFragment != null) {
                    fragmentTransaction.hide(realTimeFragment);
                }
                break;
            case R.id.curveFragmentTab :
                if (curveFragment != null)
                    fragmentTransaction.hide(curveFragment);
                break;
        }


    }



    private void makeToast(String msg, int time) {
        Log.d(TAG, "makeToast: " + msg);
        Toast.makeText(this, msg, time).show();
    }

    @Override
    public void onSelection(String macAddress) {
        BlueToothController.getInstance().setDeviceMacAddress(macAddress);
        BlueToothController.getInstance().connect();
    }

    static class BluetoothHandler extends Handler {
        WeakReference<MainActivity> activityWeakReference;

        BluetoothHandler(MainActivity mainActivity) {
            activityWeakReference = new WeakReference<>(mainActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg != null) {
                byte [] dataBuf;
                switch (msg.what) {
                    case MessageConstants.MESSAGE_TOAST :
                        activityWeakReference.get().makeToast((String) msg.obj, Toast.LENGTH_LONG);
                        break;
                    case MessageConstants.MESSAGE_READ :
                        dataBuf = (byte[]) msg.obj;
                        FileLogger.getInstance().logMsg(dataBuf, "data.txt", true);
                        break;
                    case MessageConstants.MESSAGE_WRITE :
                        Log.d(TAG, "handleMessage: message write " + msg.obj);
                        break;
                    default:
                        Log.e(TAG, "handleMessage: unexpected message");
                        activityWeakReference.get().makeToast("handleMessage: unexpected message", Toast.LENGTH_LONG);
                        break;
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        if (blueToothController != null) {
            blueToothController.destroy();
        }
        super.onDestroy();
    }
}
