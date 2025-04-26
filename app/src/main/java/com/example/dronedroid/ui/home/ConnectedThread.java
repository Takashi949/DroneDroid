package com.example.dronedroid.ui.home;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ConnectedThread extends Thread {
    private final String TAG = "ConnectedThread";
    private BluetoothSocket mmSocket;
    private InputStream mmInStream;
    private OutputStream mmOutStream;
    private byte[] mmBuffer; // mmBuffer store for the stream
    private  Handler handler;
    public ConnectedThread(Handler handler) {
        this.handler = handler;
    }
    public void connect(BluetoothSocket socket){
        this.mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams; using temp objects because
        // member streams are final.
        try {
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating input stream", e);
        }
        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating output stream", e);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
        Log.i("MyBluetooth", "Connection established");
        Message readMsg = handler.obtainMessage(
                CONSTANTS.DEV_CONN, -1, -1, mmBuffer);
        readMsg.sendToTarget();
    }

    public void run() {
        // Keep listening to the InputStream until an exception occurs.
        while (true) {
            //接続している時だけ読み込む
            if(mmSocket != null && mmSocket.isConnected()){
                try {
                    mmBuffer = new byte[1024];

                    // Read from the InputStream.
                    int numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    Message readMsg = handler.obtainMessage(
                            CONSTANTS.MSG_RECV, numBytes, -1,
                            mmBuffer);
                    readMsg.sendToTarget();
                    Log.i("Recv", new String(mmBuffer));
                } catch (IOException e) {
                    //接続が切れたらDISCメッセイー字を送って寝る
                    Log.d(TAG, "Input stream was disconnected", e);
                    mmSocket = null;
                    handler.sendMessage(handler.obtainMessage(CONSTANTS.DEV_DISC, -1, -1, mmBuffer));
                    try {
                        sleep(100);
                    }catch (InterruptedException ee){
                        throw new RuntimeException(ee);
                    }

                }
            }
            //接続されていないときは寝る
            else {
                try {
                    sleep(300);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    // Call this from the main activity to send data to the remote device.
    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);

            // Share the sent message with the UI activity.
            Message writtenMsg = handler.obtainMessage(
                    CONSTANTS.MSG_SEND, -1, -1, bytes);
            writtenMsg.sendToTarget();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when sending data", e);

            // Send a failure message back to the activity.
            Message writeErrorMsg =
                    handler.obtainMessage(CONSTANTS.SEND_ERR);
            Bundle bundle = new Bundle();
            bundle.putString("toast",
                    "Couldn't send data to the other device");
            writeErrorMsg.setData(bundle);
            handler.sendMessage(writeErrorMsg);
        }
    }

    // Call this method from the main activity to shut down the connection.
    public void cancel() {
        Message discMsg = handler.obtainMessage(
                CONSTANTS.DEV_DISC, -1, -1, mmBuffer);
        handler.sendMessage(discMsg);
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }
}
