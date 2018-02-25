package ylva.chat;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    boolean isConnected = false;
    public BluetoothAdapter myAdapter = BluetoothAdapter.getDefaultAdapter();
    String address;
    static final UUID myUUID =java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private ArrayAdapter<String> mConversationArrayAdapter;
    private String mConnectedDeviceName = null;

    private BluetoothChatService mChatService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent newint = getIntent();
        address = newint.getStringExtra(ConnectScreen.EXTRA_ADDRESS);
        console(address);


        Button btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setText("Test");
        EditText textField = (EditText) findViewById(R.id.editText);


        /*
        The following waits for a button click, then gets the message, puts it on the log.
        This will send the message to be send to the Bluetooth connection once that has been mad
         */
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                console("Button was pressed, obtaining string");
                String message;
                EditText textField = (EditText) findViewById(R.id.editText);
                message = textField.getText().toString();
                console( "Message to be send to "+address+" :\n" + message);
                mChatService.write("Attempting to send something".getBytes());
            }
        });

        Button btnConnect  = (Button) findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                console("Connect button was pressed, switching to connect view");
                Intent intent = new Intent(MainActivity.this, ConnectScreen.class);
                startActivity(intent);
            }
        });

        mChatService = new BluetoothChatService(mhandler);
        mChatService.start();
        BluetoothDevice device = myAdapter.getRemoteDevice(address);
        mChatService.connect(device);

        //mChatService.write("This is a test".getBytes());

        console("test send");





    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }





    /**
     * Prints the given message in the console on info level and sends a toast
     * @param s String to print
     */
    private void msg(String s){
        Log.d("Ylva", s);
        //Toast.makeText(MainActivity.this,s,Toast.LENGTH_LONG).show();
    }

    /**
     * Prints a given message only in console on debug level
     * @param s
     */
    private void console(String s){
        Log.d("Ylva",s);
    }
    private void error (String s, Exception e){
        Log.e("Ylva", s,e);
    }


    private final Handler mhandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuffer = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuffer);
                    console("Message gotten somewhere writebuffer\n"+writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuffer = (byte[]) msg.obj;
                    String readMessage = new String(readBuffer);
                    console("Message gotten somewhere readBuffer\n"+readMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    console("Connected to device\n"+mConnectedDeviceName);
            }
        }
    };

}


