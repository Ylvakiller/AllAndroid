package ylva.chat;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Bluetooth {
    Activity activity;
    private BluetoothAdapter bluetooth;
    private Set<BluetoothDevice> pairedDevices;
    Button btnPaired;
    ListView deviceList;

    /**
     * Constructer, this class needs the activity it is ran in for both turning on bluetooth and communicating with toasts
     * @param activityGiven The activity it this is ran in
     */
    public Bluetooth(Activity activityGiven){
        activity = activityGiven;
        btnPaired = (Button)activity.findViewById(R.id.button);
        deviceList = (ListView)activity.findViewById(R.id.listView);
    }


    public void TestBluetooth(){
        BluetoothAdapter.getDefaultAdapter();
        if(bluetooth==null){
            Toast.makeText(activity.getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
        }else{
            //Check if bluetooth is enabled
            if (!bluetooth.isEnabled()){
                Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(turnBTon,1);
            }
        }
    }

    public void PairedDevicesList(){
        pairedDevices = bluetooth.getBondedDevices();
        ArrayList list = new ArrayList();
        if (pairedDevices.size()>0){
            for (BluetoothDevice bt : pairedDevices){
                list.add(bt.getName()+"\n"+bt.getAddress());//Store the device name and address
            }
        }else{
            Toast.makeText(activity.getApplicationContext(), "No Paired Bluetooth Devices Found", Toast.LENGTH_LONG).show();
        }
        final ArrayAdapter adapter = new ArrayAdapter(activity,android.R.layout.simple_list_item_1,list);
        deviceList.setAdapter(adapter);
    }
    private void onItemClick(AdapterView av, View v, int arg2, long arg3){
        //get the last 17 digits of the device mac address
        String info = ((TextView)v).getText().toString();
        String address = info.substring(info.length()-17);



    }
}