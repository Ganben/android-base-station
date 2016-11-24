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

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.io.ByteArrayInputStream;
import java.net.URISyntaxException;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity implements android.view.View.OnClickListener {

    MqttAndroidClient sampleClient;
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
    //mqtt parameter
    String topic        = "test";
    String content      = "Message from MqttPublishSample";
    int qos             = 1;
    String serverUri       = "tcp://47.88.15.107:1883";
    String clientId     = "AndroidSample";
    long matchCount = 0;
    long otherCount = 0;

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
    }

    @Override
    public void onClick(View v) {
    }

    public void sent(){
//        Toast.makeText(getApplicationContext())
//        mSocket.emit("new message", "123123");
        Log.w(this.getClass().getSimpleName(), "view clicked");
        String s = "this me , this is me!";
        byte[] bs = s.getBytes();
        MqttMessage msg = new MqttMessage();
        msg.setPayload(bs);
        try {
            sampleClient.publish(topic, msg);
        } catch (MqttException me) {
            Log.w("sent error", "me ".concat(me.getMessage().toString()));
        }
        Toast.makeText(this.getApplicationContext(), "clicked", Toast.LENGTH_SHORT);
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


        mqtt_init();


        //use socket io;
//        mSocket.connect();
        //
        //
        Log.w(this.getClass().getSimpleName(), String.valueOf(sampleClient.isConnected()));
    }


    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("tcp://47.88.15.107:1883");
        } catch (URISyntaxException e) {}
    }


    public void mqtt_init(){

//        MemoryPersistence persistence = new MemoryPersistence();
        sampleClient = new MqttAndroidClient(this.getApplicationContext(), serverUri, clientId);

        sampleClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                if (reconnect) {
//                    addToHistory("Reconnected to : " + serverURI);
                    // Because Clean Session is true, we need to re-subscribe
                    subscribeToTopic();
                } else {
//                    addToHistory("Connected to: " + serverURI);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
//                addToHistory("The Connection was lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
//                addToHistory("Incoming message: " + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);

        try {
            //addToHistory("Connecting to " + serverUri);
            sampleClient.connect(mqttConnectOptions, this.getApplicationContext(), new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    sampleClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    addToHistory("Failed to connect to: " + serverUri);
                }
            });


        } catch (MqttException ex){
            ex.printStackTrace();
        }

        try {
            MqttMessage msg = new MqttMessage();
            byte[] bt = new byte[]{ 1, 2, 3, 4, 5};

            msg.setPayload(bt);
            msg.setId(1);
            msg.setQos(1);
            topic = "test";
//            sampleClient.publish(topic, msg);
            sampleClient.getBufferedMessage(1);
        } catch (Exception me) {
            Log.w(this.getClass().getSimpleName(), "-----------publich");
        }

    }

    public void subscribeToTopic(){
        try {
            sampleClient.subscribe("test", 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
//                    addToHistory("Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    addToHistory("Failed to subscribe");
                }
            });

            // THIS DOES NOT WORK!
            sampleClient.subscribe("test", 0, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // message Arrived!
                    System.out.println("Message: " + topic + " : " + new String(message.getPayload()));
                }
            });

        } catch (MqttException ex){
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }

    }

    public void startScanning() {
        System.out.println("start scanning");
        peripheralTextView.setText("");

        sent();
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
                if (result.getScanRecord().getManufacturerSpecificData().keyAt(0) == 65280) {
                    matchCount += 1;
                    try {
//                    sampleClient = new MqttClient(broker, clientId);
//                    sampleClient.connect();
                        MqttMessage msg = new MqttMessage();
                        msg.setQos(1);
                        msg.setPayload(result.getScanRecord().getManufacturerSpecificData().valueAt(0));

                        sampleClient.publish("test", msg);
                    } catch (MqttException me) {
                        Log.d(this.getClass().getSimpleName(), "mqtt sent err");
                    }

                    peripheralTextView.append("$" + matchCount + ":N:" + result.getDevice().getName() + " rssi: " + result.getRssi() + " : " + result.getScanRecord().getManufacturerSpecificData().keyAt(0) + "\n" + toHexString(result.getScanRecord().getManufacturerSpecificData().valueAt(0)) + "\n");

                    // auto scroll for text view
                    final int scrollAmount = peripheralTextView.getLayout().getLineTop(peripheralTextView.getLineCount()) - peripheralTextView.getHeight();
                    // if there is no need to scroll, scrollAmount will be <=0
                    if (scrollAmount > 0)
                        peripheralTextView.scrollTo(0, scrollAmount);
                } else {
                    otherCount += 1;
                }
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


