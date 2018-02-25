package ylva.chat;

import java.util.Set;
import java.util.ArrayList;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.TextView;
import android.content.Intent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

public class ConnectScreen extends AppCompatActivity {
    private BluetoothAdapter bluetooth;
    private Set<BluetoothDevice> pairedDevices;
    Button btnPaired;
    ListView deviceList;

    public final static String EXTRA_ADDRESS = "Temp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_screen);
        btnPaired = (Button)findViewById(R.id.button);
        deviceList = (ListView)findViewById(R.id.listView);

        TestBluetooth();

        btnPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pairedDevicesList();
            }
        });




    }

    public void TestBluetooth(){
        bluetooth = BluetoothAdapter.getDefaultAdapter();
        if(bluetooth==null){
            //Makes sure that the button is disabled when there is not bluetooth possible on the device
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
            btnPaired.setEnabled(false);
        }else{
            //Check if bluetooth is enabled
            if (!bluetooth.isEnabled()){
                Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnBTon,1);
            }
        }
    }

    public void pairedDevicesList(){
        pairedDevices = bluetooth.getBondedDevices();
        ArrayList list = new ArrayList();
        if (pairedDevices.size()>0){
            for (BluetoothDevice bt : pairedDevices){
                list.add(bt.getName()+"\n"+bt.getAddress());//Store the device name and address
            }
        }else{
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found", Toast.LENGTH_LONG).show();
        }
        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,list);
        deviceList.setAdapter(adapter);
        deviceList.setOnItemClickListener(myClickListener);
    }

    private AdapterView.OnItemClickListener myClickListener = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView av, View v, int arg2, long arg3){
            String info = ((TextView)v).getText().toString();
            String address = info.substring(info.length()-17);

            Intent intent = new Intent(ConnectScreen.this, MainActivity.class);

            intent.putExtra(EXTRA_ADDRESS, address);
            startActivity(intent);
        }
    };

}
