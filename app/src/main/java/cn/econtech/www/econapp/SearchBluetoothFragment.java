package cn.econtech.www.econapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_FIRST_USER;
import static android.app.Activity.RESULT_OK;

/**
 * Created by Admin-01 on 2017/12/20.
 *
 */

public class SearchBluetoothFragment extends Fragment implements AdapterView.OnItemClickListener {

    private static final String TAG = SearchBluetoothFragment.class.getName();

    private BluetoothAdapter bluetoothAdapter =null;
    private DeviceReceiver myDevice =new DeviceReceiver();
    private List<String> deviceList=new ArrayList<>();
    private Set<String> deviceInListSet;
    private ListView deviceListView;
    private ArrayAdapter<String> adapter;
    private boolean hasRegister =false;
    private TextView btSearch;

    private OnItemSelected onItemSelected;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        deviceInListSet = new HashSet<>();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_search, container, false);
        setBluetooth();
        setView(view);
        return view;
    }

    private void setView(View view){

        deviceListView = view.findViewById(R.id.deviceList);
        adapter=new ArrayAdapter<>(getActivity(), R.layout.search_item, deviceList);
        deviceListView.setAdapter(adapter);
        deviceListView.setOnItemClickListener(this);
        btSearch = view.findViewById(R.id.start_search);
        btSearch.setOnClickListener(new ClickMonitor());
    }

    private class ClickMonitor implements View.OnClickListener{

        @Override
        public void onClick(View v) {
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
    public void onStart() {
        //注册蓝牙接收广播
        if(!hasRegister){
            hasRegister =true;
            IntentFilter filterStart=new IntentFilter(BluetoothDevice.ACTION_FOUND);
            IntentFilter filterEnd=new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            getActivity().registerReceiver(myDevice, filterStart);
            getActivity().registerReceiver(myDevice, filterEnd);
        }
        super.onStart();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        if(bluetoothAdapter !=null&& bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
        }
        if(hasRegister){
            hasRegister =false;
            getActivity().unregisterReceiver(myDevice);
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

            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setTitle("No bluetooth devices");
            dialog.setMessage("Your equipment does not support bluetooth, please change device");

            dialog.setNegativeButton("cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            dialog.show();
        }
    }

    /**
     * Finding Devices
     */
    private void findAvailableDevice(){
        //获取可配对蓝牙设备
        Set<BluetoothDevice> devices= bluetoothAdapter.getBondedDevices();

        if(bluetoothAdapter !=null&& bluetoothAdapter.isDiscovering()){
            deviceList.clear();
            adapter.notifyDataSetChanged();
        }
        if(devices.size()>0){ //存在已经配对过的蓝牙设备
            for (BluetoothDevice device : devices) {
                if (!deviceInListSet.contains(device.getAddress())) {
                    deviceList.add(device.getName() + '\n' + device.getAddress());
                    adapter.notifyDataSetChanged();
                    deviceInListSet.add(device.getAddress());
                }

            }
        }else{  //不存在已经配对过的蓝牙设备
            deviceList.add("无配对项，请先配对");
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

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
            String action =intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){    //搜索到新设备
                BluetoothDevice btd=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG, "onReceive: on device found " + action);
                //搜索没有配过对的蓝牙设备
                if (!deviceInListSet.contains(btd.getAddress())) {
                    deviceList.add(btd.getName()+'\n'+btd.getAddress());
                    deviceInListSet.add(btd.getAddress());
                    adapter.notifyDataSetChanged();
                }
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){   //搜索结束

                if (deviceListView.getCount() == 0) {
                    deviceList.add("None device found");
                    adapter.notifyDataSetChanged();
                }
                btSearch.setText(R.string.searchAgain);
            }
        }
    }

    public interface OnItemSelected {
        void onSelection(String macAddress);
    }

    /*
    * onAttach(Context) is not called on pre API 23 versions of Android and onAttach(Activity) is deprecated
    * Use onAttachToContext instead
    */
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onAttach(Context context) {
        onItemSelected = (OnItemSelected) getActivity();
        super.onAttach(context);
    }

    /*
    * Deprecated on API 23
    * Use onAttachToContext instead
    */
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onItemSelected = (OnItemSelected) activity;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {

        Log.d("msgParent", "Parent= " + arg0);
        Log.d("msgView", "View= "+arg1);
        Log.d("msgChildView", "ChildView= "+arg0.getChildAt(pos-arg0.getFirstVisiblePosition()));

        final String msg = deviceList.get(pos);

        if(bluetoothAdapter !=null&& bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
            btSearch.setText(R.string.searchAgain);
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());// 定义一个弹出框对象
        dialog.setTitle("Confirmed connecting device");
        dialog.setMessage(msg);
        dialog.setPositiveButton("connect",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: msg = " + msg);


                        try {
                            String bluetoothMacAddress = msg.substring(msg.length()-17);
                            onItemSelected.onSelection(bluetoothMacAddress);
                        } catch (NullPointerException e) {
                            Log.e(TAG, "onClick: ", e);
                        } catch (StringIndexOutOfBoundsException e) {
                            Toast.makeText(getActivity().getApplication(), "error address", Toast.LENGTH_LONG).show();
                        }


                    }
                });
        dialog.setNegativeButton("cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //to do
                    }
                });
        dialog.show();
    }
}
