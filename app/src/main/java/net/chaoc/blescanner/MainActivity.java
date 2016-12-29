package net.chaoc.blescanner;

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
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
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

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity implements android.view.View.OnClickListener {

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;
    Button startScanningButton;
    Button stopScanningButton;
    TextView peripheralTextView;
    private final static int REQUEST_ENABLE_BT = 1;
    public String advDataStr;
    AdvertiseData advData;
    BluetoothLeAdvertiser advertiser;
    AdvertiseSettings advSettings;

    AdvertiseCallback advCallback;

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
                //if (result.getScanRecord().getManufacturerSpecificData().keyAt(0) == 65280) {
                if (false) {
                    matchCount += 1;

                    peripheralTextView.append("$" + matchCount + ":N:" + result.getDevice().getName() + " rssi: " + result.getRssi() + " : " + result.getScanRecord().getManufacturerSpecificData().keyAt(0) + "\n" + toHexString(result.getScanRecord().getBytes()) + "\n");

                    // auto scroll for text view
                    final int scrollAmount = peripheralTextView.getLayout().getLineTop(peripheralTextView.getLineCount()) - peripheralTextView.getHeight();
                    // if there is no need to scroll, scrollAmount will be <=0
                    if (scrollAmount > 0)
                        peripheralTextView.scrollTo(0, scrollAmount);
                } else {
                    if (result.getDevice().getName() != null ) {
                       //if (result.getDevice().getName().toString().startsWith("Brac")) {
                        try {
                            String data = toHexString(result.getScanRecord().getManufacturerSpecificData().valueAt(0));
                            String payload = toHexString(result.getScanRecord().getBytes());
                            String name = result.getDevice().getName();
                            int key = result.getScanRecord().getManufacturerSpecificData().keyAt(0);
                            int rssi = result.getRssi();

                            String str = "$:" + name + " rssi" + rssi + " : " + key + "\n"
                                    + data + "\n"
                                    + payload + "\n";
                            if (key == 65281 && data.startsWith("12")) {
                                peripheralTextView.append(Html.fromHtml("<font color=#00ee00>"+str+"</font>"));
                            } else {
                                peripheralTextView.append(str);
                            }
                            peripheralTextView.append("\n");

                            otherCount += 1;
                            Log.w(this.getClass().getSimpleName(), toHexString(result.getScanRecord().getBytes()));
                        } catch (Exception e) {
                            Log.e(this.getClass().getSimpleName(), "get manufacture data error");
                        }
                        // auto scroll for text view
                        final int scrollAmount = peripheralTextView.getLayout().getLineTop(peripheralTextView.getLineCount()) - peripheralTextView.getHeight();
                        // if there is no need to scroll, scrollAmount will be <=0
                        if (scrollAmount > 0)
                            peripheralTextView.scrollTo(0, scrollAmount);
                    //}
                    }
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
                .addManufacturerData(65281, manuData)
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


