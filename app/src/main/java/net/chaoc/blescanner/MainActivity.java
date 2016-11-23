package net.chaoc.blescanner;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.io.ByteArrayInputStream;

import io.yunba.android.manager.YunBaManager;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity implements android.view.View.OnClickListener {

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;
    Button startScanningButton;
    Button stopScanningButton;
    TextView peripheralTextView;
    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    public String advDataStr;
    AdvertiseData advData;
    BluetoothLeAdvertiser advertiser;
    AdvertiseSettings advSettings;
    private MessageReceiver mMessageReceiver;
    public final static String MESSAGE_RECEIVED_ACTION = "io.yunba.example.msg_received_action";
    AdvertiseCallback advCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        peripheralTextView = (TextView) findViewById(R.id.PeripheralTextView);
        peripheralTextView.setMovementMethod(new ScrollingMovementMethod());

        startScanningButton = (Button) findViewById(R.id.StartScanButton);
        startScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startScanning();
            }
        });
        stopScanningButton = (Button) findViewById(R.id.StopScanButton);
        stopScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopScanning();
            }
        });
        stopScanningButton.setVisibility(View.INVISIBLE);

        YunBaManager.start(getApplicationContext());
        YunBaManager.subscribe(getApplicationContext(), new String[]{"t1"}, new IMqttActionListener() {

            @Override
            public void onSuccess(IMqttToken arg0) {
                Log.d("main", "Subscribe topic succeed");

            }

            @Override
            public void onFailure(IMqttToken arg0, Throwable arg1) {
                Log.d("main", "Subscribe topic failed" );
            }
        });

        registerMessageReceiver();

    }

    public void registerMessageReceiver(){

        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(YunBaManager.MESSAGE_RECEIVED_ACTION);
        filter.addCategory(getPackageName());
        registerReceiver(mMessageReceiver, filter);

        IntentFilter filterCon = new IntentFilter();
        filterCon.addAction(YunBaManager.MESSAGE_CONNECTED_ACTION);
        filterCon.addCategory(getPackageName());
        registerReceiver(mMessageReceiver, filterCon);

        IntentFilter filterDis = new IntentFilter();
        filterDis.addAction(YunBaManager.MESSAGE_DISCONNECTED_ACTION);
        filterDis.addCategory(getPackageName());
        registerReceiver(mMessageReceiver, filterDis);

        IntentFilter pres = new IntentFilter();
        pres.addAction(YunBaManager.PRESENCE_RECEIVED_ACTION);
        pres.addCategory(getPackageName());
        registerReceiver(mMessageReceiver, pres);

    }
    @Override
    public void onClick(View v) {
//        Toast.makeText(getApplicationContext())
    }

    public void init(){
        btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();
        advertiser = btAdapter.getBluetoothLeAdvertiser();

        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,REQUEST_ENABLE_BT);
        }
// ble advertisers
        advSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .setConnectable( false )
                .build();
        advDataStr = "121383000000";
        ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton2);
        final EditText eText = (EditText) findViewById(R.id.editText4);
        eText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


            }

            @Override
            public void afterTextChanged(Editable s) {
                String text;
                text = s.toString();
                if (text.matches("-?[0-9a-fA-F]+") == true) {
                    advDataStr = s.toString();
                } else {
                    s.clear();
                    s.append("121383000000");
                }
            }
        });

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //enabled
                    //update text value;
                    //validated it; if false then disabled this
                    //start advertising;
                    advertise(toByteArray(advDataStr));

                } else {
                    //disabled
                    //stop advertising;
                    stopAdvertise();
                }
            }
        });

        // Make sure we have access coarse location enabled, if not, prompt the user to enable it
//        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setTitle("This app needs location access");
//            builder.setMessage("Please grant location access so this app can detect peripherals.");
//            builder.setPositiveButton(android.R.string.ok, null);
//            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                @Override
//                public void onDismiss(DialogInterface dialog) {
//                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
//                }
//            });
//            builder.show();
//        }
    }

    public void startScanning() {
        System.out.println("start scanning");
        peripheralTextView.setText("");
        startScanningButton.setVisibility(View.INVISIBLE);
        stopScanningButton.setVisibility(View.VISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.startScan(leScanCallback);
            }
        });
    }

    public void stopScanning() {
        System.out.println("stopping scanning");
        peripheralTextView.append("Stopped Scanning");
        startScanningButton.setVisibility(View.VISIBLE);
        stopScanningButton.setVisibility(View.INVISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.stopScan(leScanCallback);
            }
        });
    }


        // Device scan callback.
        private ScanCallback leScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                peripheralTextView.append("Name: " + result.getDevice().getName() + " rssi: " + result.getRssi() + " : " + result.getScanRecord().getManufacturerSpecificData().keyAt(0) + "\n" + toHexString(result.getScanRecord().getManufacturerSpecificData().valueAt(0))+ "\n");

                // auto scroll for text view
                final int scrollAmount = peripheralTextView.getLayout().getLineTop(peripheralTextView.getLineCount()) - peripheralTextView.getHeight();
                // if there is no need to scroll, scrollAmount will be <=0
                if (scrollAmount > 0)
                    peripheralTextView.scrollTo(0, scrollAmount);
            }
        };

    public String toHexString(byte[] bytes){
        StringBuilder hexString = new StringBuilder();

        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }

    public static byte[] toByteArray(String s){
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }


    public void advertise(byte[] manuData){
        advData = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addManufacturerData(65280, manuData)
                .build();
        advCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
                Toast.makeText(getApplicationContext(), "start advertising", Toast.LENGTH_LONG).show();
            }
        };
        advertiser.startAdvertising(advSettings, advData, advCallback);
    }

    public void stopAdvertise(){
        advertiser.stopAdvertising(advCallback);

    }

    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Action - " + intent.getAction());
            Toast.makeText(getApplicationContext(), "msg received", Toast.LENGTH_SHORT).show();
            }
    }
}


