package cn.econtech.www.utilslib;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import bluetoothLib.BlueToothController;
import bluetoothLib.ListViewAdapter;
import bluetoothLib.MessageConstants;


public class SearchActivity extends Activity implements AdapterView.OnItemClickListener {
    private BluetoothAdapter bluetoothAdapter =null;
    private DeviceReceiver myDevice =new DeviceReceiver();
    private List<String> deviceList=new ArrayList<>();
    private ListView deviceListView;
    private TextView btSearch;
    private ListViewAdapter<String> stringListViewAdapter;
    private boolean hasRegistered =false;
    private Set<String> deviceSet = new HashSet<>();
    private static final  String TAG = "SearchActivity";
    private boolean autoConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        autoConnect = extras != null && extras.getBoolean(MessageConstants.AUTO_CONNECT, false);
        setContentView(R.layout.activity_search);
        setView();
        setBluetooth();

    }

    private void setView(){

        deviceListView = findViewById(R.id.deviceList);
        stringListViewAdapter =new ListViewAdapter<>(this, R.layout.search_item, deviceList);
        deviceListView.setAdapter(stringListViewAdapter);
        deviceListView.setOnItemClickListener(this);
        btSearch = findViewById(R.id.start_search);
        btSearch.setOnClickListener(new ClickMonitor());

    }
    private class ClickMonitor implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick: click monitor ");
            if(bluetoothAdapter.isDiscovering()){
                bluetoothAdapter.cancelDiscovery();
                btSearch.setText("再次搜搜");
            }else{
                findAvailableDevice();
                bluetoothAdapter.startDiscovery();
                btSearch.setText("停止搜索");
            }
        }
    }

    @Override
    protected void onStart() {
        //注册蓝牙接收广播
        if(!hasRegistered){
            hasRegistered =true;
            IntentFilter filterStart=new IntentFilter(BluetoothDevice.ACTION_FOUND);
            IntentFilter filterEnd=new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(myDevice, filterStart);
            registerReceiver(myDevice, filterEnd);
        }
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        if(bluetoothAdapter !=null && bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
        }
        if(hasRegistered){
            hasRegistered =false;
            unregisterReceiver(myDevice);
        }
        super.onDestroy();
    }
    /**
     * Setting Up Bluetooth
     */
    private void setBluetooth(){
        bluetoothAdapter =BluetoothAdapter.getDefaultAdapter();

        if(bluetoothAdapter !=null){  //Device support Bluetooth
            //确认开启蓝牙
            if(!bluetoothAdapter.isEnabled()){
                //请求用户开启
                Intent intent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, RESULT_FIRST_USER);
                //使蓝牙设备可见，方便配对
                Intent in=new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                in.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 200);
                startActivity(in);
                //直接开启，不经过提示
                bluetoothAdapter.enable();
            }
        }
        else{   //Device does not support Bluetooth

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("No bluetooth devices");
            dialog.setMessage("Your equipment does not support bluetooth, please change device");

            dialog.setNegativeButton("cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            finish();
                        }
                    });
            dialog.show();
        }
    }

    /**
     * Finding Devices
     */
    private void findAvailableDevice(){
        Log.e(TAG, "findAvailableDevice: enter");
        //获取可配对蓝牙设备
        Set<BluetoothDevice> device= bluetoothAdapter.getBondedDevices();

        if(bluetoothAdapter !=null&& bluetoothAdapter.isDiscovering()){
            deviceList.clear();
            stringListViewAdapter.notifyDataSetChanged();
        }
        if(device.size()>0){ //存在已经配对过的蓝牙设备
            for (BluetoothDevice btd : device) {
                if (!deviceSet.contains(btd.getAddress())) {
                    deviceList.add(btd.getName()+'\n'+btd.getAddress());
                    deviceSet.add(btd.getAddress());
                    stringListViewAdapter.notifyDataSetChanged();
                }
            }
        }else{  //不存在已经配对过的蓝牙设备
            deviceList.add("无配对项，请先配对");
            stringListViewAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch(resultCode){
            case RESULT_OK:
                findAvailableDevice();
                break;
            case RESULT_CANCELED:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 蓝牙搜索状态广播监听
     * @author Andy
     *
     */
    private class DeviceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: " + intent);
            Log.d(TAG, "onReceive: deviceListView.getSelectedView() " + deviceListView.getSelectedView());
            String action =intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){    //搜索到新设备
                BluetoothDevice btd=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!deviceSet.contains(btd.getAddress())) {
                    Log.d(TAG, "onReceive: new device found :" + btd);
                    //搜索没有配过对的蓝牙设备
                    if (btd.getBondState() != BluetoothDevice.BOND_BONDED) {
                        deviceList.add(btd.getName()+'\n'+btd.getAddress());
                        stringListViewAdapter.notifyDataSetChanged();
                    }
                    deviceSet.add(btd.getAddress());
                }
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){   //搜索结束
                Log.d(TAG, "onReceive: " + action);
                if (deviceListView.getCount() == 0) {
                    deviceList.add("No can be matched to use bluetooth");
                    stringListViewAdapter.notifyDataSetChanged();
                }
                Log.d(TAG, "onReceive: " + deviceListView.getSelectedView());

                btSearch.setText("再次搜索");
            }
        }
    }


    @Override
    public void onItemClick(AdapterView<?> arg0, final View view, int pos, long arg3) {
        if(bluetoothAdapter !=null&& bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
            view.setSelected(true);
            view.setPressed(true);
            view.setActivated(true);
            btSearch.setText("再次搜索");
        }
        final String msg = deviceList.get(pos);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);// 定义一个弹出框对象
        dialog.setTitle("Confirmed connection");
        dialog.setMessage(msg);
        dialog.setPositiveButton("connect",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: connect clicked on dialog");
                        final int MAC_ADDRESS_LENGTH = 17;
                        if (msg.length() < MAC_ADDRESS_LENGTH)
                            Toast.makeText(SearchActivity.this, "invalid mac address", Toast.LENGTH_SHORT).show();
                        String macAddress = msg.substring(msg.length()- MAC_ADDRESS_LENGTH);
                        BlueToothController.getInstance().setDeviceMacAddress(macAddress);
                        if (autoConnect) {
                            BlueToothController.getInstance().connect();
                        }
                        finish();
                    }
                });
        dialog.setNegativeButton("cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: dismiss dialog");
                        dialog.dismiss();
                    }
                });
        Log.d(TAG, "onItemClick: dialog about to show.");

        dialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }



}
