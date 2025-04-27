package com.example.dronedroid;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.example.dronedroid.ui.home.CONSTANTS;
import com.example.dronedroid.ui.home.ConnectThread;
import com.example.dronedroid.ui.home.ConnectedThread;
import com.example.dronedroid.ui.home.HomeViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.dronedroid.databinding.ActivityMainBinding;
import android.content.Intent;
import android.widget.Toolbar;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private Button connectButton;
    private BluetoothAdapter bluetoothAdapter;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    private Handler btHandler;
    private ActivityMainBinding binding;
    private HomeViewModel homeViewModel;
    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //ToolBarの設定
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        //ViewModelの取得
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        btHandler = new Handler() {
            @SuppressLint("HandlerLeak")
            public void handleMessage(Message msg) {
                String displayText = "";
                switch (msg.what){
                    case CONSTANTS.MSG_RECV:
                        try {
                            MessageParser((byte[]) msg.obj);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    case CONSTANTS.MSG_SEND:
                        break;
                    case CONSTANTS.DEV_CONN:
                        Log.i("bthadler", "DEV_CONN");
                        displayText = "\nConnection Established";
                        homeViewModel.getText().setValue(displayText);
                        homeViewModel.getIsConnected().setValue(true);
                        setConnState();
                        break;
                    case CONSTANTS.DEV_DISC:
                        displayText = "\nDisconnected";
                        homeViewModel.getText().setValue(displayText);
                        homeViewModel.getIsConnected().setValue(false);
                        setDisconState();
                        break;
                    case CONSTANTS.SEND_ERR:
                        displayText = "\nERR :" + msg.obj.toString();
                        homeViewModel.getText().setValue(displayText);
                        break;
                    default:
                        break;
                }

            }
        };

        connectedThread = new ConnectedThread(btHandler);

        //CT, CU1, CU2, CU3, CU4, BC, EC,
        homeViewModel.getObjective_values().observeForever(new Observer<ArrayList<Float>>(){
            @Override
            public void onChanged(ArrayList<Float> z) {
                Log.i("Obj changed", z.toString());
                byte[] msg = new byte[5];
                msg[0] = homeViewModel.obj_chnged_item;
                for(int k = 0; k < 4; k++){
                    msg[k + 1] = ByteBuffer
                            .allocate(4)
                            .putFloat(z.get(homeViewModel.obj_chnged_item)).array()[k];
                }

                Log.i("Send Command", homeViewModel.obj_chnged_item + " " + z.get(homeViewModel.obj_chnged_item));
                if(isConnected){
                    connectedThread.write(msg);
                }
            }
        });

        homeViewModel.getIsContrlEnable().observeForever(new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isControlEnable) {
                byte[] msg = new byte[2];
                //ダミーの数字
                msg[1] = 10;
                if (isControlEnable) {
                    msg[0] = CONSTANTS.CMD_BC;
                }else{
                    msg[0] = CONSTANTS.CMD_EC;
                }
                Log.i("D", "Send command" + new String(msg));
                if (isConnected)connectedThread.write(msg);
            }
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(this, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ActivityResultLauncher<Intent> mGetContent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        // Handle the returned Uri
                        if (result.getResultCode() == RESULT_OK) {}
                    }
            );
            mGetContent.launch(enableBtIntent);
        }
    }

    //Actionbarのボタンが押されたとき
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbarmenu, menu);
        if(isConnected){
            menu.findItem(R.id.connectButton).setIcon(android.R.drawable.ic_media_pause);
        }else{
            menu.findItem(R.id.connectButton).setIcon(android.R.drawable.ic_media_play);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (isConnected){
            //接続されていたら接続解除する
            connectedThread.cancel();
        }
        else {
            //未接続だったら接続する
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            if (!pairedDevices.isEmpty()) {
                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    Log.i("device List", deviceName);
                    if(deviceName.equals("ESP_THRUST")){
                        connectThread = new ConnectThread(device, bluetoothAdapter, connectedThread);
                        connectThread.start();
                        break;
                    }
                }
            }else {
                Toast.makeText(getApplicationContext(), "No Paired Device", Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    //Bluetoothが接続されたとき
    private void setDisconState(){
        //Toolbarのアイコンを変える
        isConnected = false;
        invalidateOptionsMenu();
    }
    //Bluetoothnの接続が切れたとき
    private void setConnState(){
        //Toolbarのアイコンを買える
        isConnected = true;
        invalidateOptionsMenu();
    }
    private float readFloatEndianCorrected(DataInputStream dis) throws IOException {
        byte[] bytes = new byte[4];
        dis.readFully(bytes); // 4バイト読み取る
        // ビッグエンディアン → リトルエンディアン変換
        int intBits = (bytes[0] & 0xFF) << 24 |
                (bytes[1] & 0xFF) << 16 |
                (bytes[2] & 0xFF) << 8 |
                (bytes[3] & 0xFF);
        return Float.intBitsToFloat(intBits);
    }

    private void MessageParser(byte[] msgText) throws IOException {
        if(Character.valueOf((char)msgText[0]).equals('t')){
            //tから始まる場合は文字列として扱う
            String displayText = "\nRECIEVE :" + msgText;
            homeViewModel.getText().setValue(displayText);
        }
        else {
            //Throttle PRY x v a u
            DataInputStream msgIS = new DataInputStream(new ByteArrayInputStream(msgText));

            ArrayList<Float> msgFloats = new ArrayList<>();
            // ByteBufferをリトルエンディアンで作成（ESP32の場合リトルエンディアンが標準）
            ByteBuffer buffer = ByteBuffer.wrap(msgText).order(java.nio.ByteOrder.LITTLE_ENDIAN);

            // byte配列サイズの4バイト単位でfloat配列を作成
            int floatCount = 18;
            float[] floatArray = new float[floatCount];

            // floatを1つずつ読み込む
            for (int i = 0; i < floatCount; i++) {
                floatArray[i] = buffer.getFloat();
                msgFloats.add(floatArray[i]);
            }

            Log.d("Receive Telem Parser", msgFloats.toString());
            homeViewModel.getThrottleText().setValue(String.valueOf(msgFloats.get(0)));
            homeViewModel.getPitchRollYaw().setValue(new ArrayList<Float>(msgFloats.subList(1, 4)));
            homeViewModel.getPosition().setValue(new ArrayList<Float>(msgFloats.subList(4, 7)));
            homeViewModel.getVelocity().setValue(new ArrayList<Float>(msgFloats.subList(7, 10)));
            homeViewModel.getAccel().setValue(new ArrayList<Float>(msgFloats.subList(10, 13)));
            homeViewModel.getControl().setValue(new ArrayList<Float>(msgFloats.subList(13, 18)));
        }
    }
}