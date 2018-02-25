package ylva.chat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.SyncStateContract;
import android.util.Log;

import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothChatService {
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private Handler mHandler;

    public BluetoothAdapter myAdapter;
    String address;
    static final UUID myUUID =java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    public BluetoothChatService(Handler handler){
        myAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandler = handler;
    }
    public synchronized void start(){
        mAcceptThread = new AcceptThread();
        mAcceptThread.start();
    }
    public synchronized void connect(BluetoothDevice device){
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device){
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    public synchronized void stop(){
        if (mConnectThread!=null){
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mAcceptThread!=null){
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        if (mConnectedThread!=null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }

    public void write(byte[] out){
        console("Attempting to write");
        ConnectedThread r;
        synchronized (this){
            r=mConnectedThread;
        }
        console("Syncronised");
        r.write(out);
        console("writing\n"+out.toString());
    }

    private class AcceptThread extends Thread{
        private final BluetoothServerSocket mmServerSocket;
        public AcceptThread(){
            BluetoothServerSocket tmp = null;
            try {
                tmp = myAdapter.listenUsingInsecureRfcommWithServiceRecord("YlvaChat", myUUID);
            } catch (IOException e) {
                error("Socket listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run(){
            BluetoothSocket socket= null;
            while (true){
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    error("Socket's accept() method failed", e);
                }

                if (socket!=null){//A connection was accepted that should mean that the connection is now stored in the socket, closing the server listener
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        error("Server socket's close() method failed", e);
                    }
                    break;
                }

            }
        }

        public void cancel(){
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                error("Server socket's close() method failed", e);
            }
        }
    }

    private class ConnectThread extends Thread{
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;


        public ConnectThread(BluetoothDevice device){
            BluetoothSocket tmp = null;
            mmDevice = device;
            try {
                tmp = device.createRfcommSocketToServiceRecord(myUUID);
            } catch (IOException e) {
                error("Sockets create method failed connectThread", e);
            }
            mmSocket = tmp;

        }

        public void run(){
            myAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
            } catch (IOException e) {
                try {
                    mmSocket.close();
                } catch (IOException e1) {
                    error("Could not close socket", e);
                }
            }

            //Connection succesful, connection should now be in the socket
        }

        public void cancel(){
            try {
                mmSocket.close();
            } catch (IOException e) {
                error("Could not close socket", e);

            }
        }
    }


    private interface MessageConstants{
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE =1;
        public static final int MESSAGE_TOAST = 2;
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                error("Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                error("Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    Message readMsg = mHandler.obtainMessage(MessageConstants.MESSAGE_READ, numBytes, -1,mmBuffer);
                    readMsg.sendToTarget();
                    console("Message recieved:\n"+readMsg.toString());
                } catch (IOException e) {
                    error("Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            console("Going to write something");
            try {
                mmOutStream.write(bytes);

                // Share the sent message with the UI activity.
                Message writtenMsg = mHandler.obtainMessage(
                        MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
                writtenMsg.sendToTarget();
            } catch (IOException e) {
                error("Error occurred when sending data", e);

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
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                error("Could not close the connect socket", e);
            }
        }


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
}