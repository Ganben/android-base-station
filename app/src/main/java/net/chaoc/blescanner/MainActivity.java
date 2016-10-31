package net.chaoc.blescanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import net.chaoc.blescanner.models.BleInfo;
import net.chaoc.blescanner.utils.CacheUtil;
import net.chaoc.blescanner.utils.ConfigUtil;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.yunba.android.manager.YunBaManager;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.StartScanButton)
    Button startScanningButton;
    @BindView(R.id.StopScanButton)
    Button stopScanningButton;
    @BindView(R.id.PeripheralTextView)
    TextView peripheralTextView;

    public static final String TAG = "MainActivity";
    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final int PUBLISH_YUNBA_INTERVAL = 5000;          //云巴publish间隔，单位毫秒
    private static final int PUBLISH_YUNBA_INTERVAL_SOS = 1000;      //（紧急呼救）云巴publish间隔，单位毫秒

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;

    private HashMap<String,BleInfo> bleCache = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initView();
        init();
        //registerMessageReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkConfig();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, YunbaSettingActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        peripheralTextView.setMovementMethod(new ScrollingMovementMethod());
        stopScanningButton.setVisibility(View.INVISIBLE);
    }

    private void init() {
        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();

        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

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


        /*YunBaManager.subscribe(this, new String[]{"t1"}, new IMqttActionListener() {

            @Override
            public void onSuccess(IMqttToken arg0) {
                Log.d("main", "Subscribe topic succeed");
            }

            @Override
            public void onFailure(IMqttToken arg0, Throwable arg1) {
                Log.d("main", "Subscribe topic failed");
            }
        });*/
    }

    // 检查云巴 alias & topic是否已经配置
    private void checkConfig() {
        if (TextUtils.isEmpty(CacheUtil.getInstance().getYunbaAlias())
            || TextUtils.isEmpty(CacheUtil.getInstance().getYunbaTopic())) {
            //TODO: need to config alias here
            Log.e("Alias", "Alias or Topic is not configured");
            Intent intent = new Intent(this, YunbaSettingActivity.class);
            startActivity(intent);
        }
    }

    @OnClick(R.id.StartScanButton)
    public void startScanning(View view) {
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

    @OnClick(R.id.StopScanButton)
    public void stopScanning(View view) {
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
            String payload = toHexString(result.getScanRecord().getManufacturerSpecificData().valueAt(0));
            String name = result.getDevice().getName();
            int manufacturer = result.getScanRecord().getManufacturerSpecificData().keyAt(0);
            int rssi = result.getRssi();

            if (manufacturer != Constants.MANUFACTURER_DATA) {
                return ;
            }

            peripheralTextView.append("Name: " + name + " rssi: " + rssi + " md: " + manufacturer + "\n" + payload + "\n");
            // auto scroll for text view
            final int scrollAmount = peripheralTextView.getLayout().getLineTop(peripheralTextView.getLineCount()) - peripheralTextView.getHeight();
            // if there is no need to scroll, scrollAmount will be <=0
            if (scrollAmount > 0)
                peripheralTextView.scrollTo(0, scrollAmount);

            // 将扫描到的蓝牙信息publish到云巴
            // 同一个设备连续扫描到后publish到yunba会间隔5秒（PUBLISH_YUNBA_INTERVAL）再重复发送
            // 如果是紧急呼救状态，间隔为1秒
            if (bleCache.get(payload) != null) {
                if ( (new Date().getTime() - bleCache.get(payload).getTimestamp().getTime()) > (isSOS(payload) ? PUBLISH_YUNBA_INTERVAL_SOS : PUBLISH_YUNBA_INTERVAL) ) {
                    bleCache.get(payload).setTimestamp(new Date());
                    publishToYunba(payload);
                } else {
                    //do nothing
                    Log.i(TAG, "payload already published:"+payload);
                }
            } else {
                bleCache.put(payload,new BleInfo(name,rssi,payload,new Date()));
                publishToYunba(payload);
            }
        }
    };

    private String toHexString(byte[] bytes) {
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

    // 是否未紧急呼救
    private boolean isSOS(String payload) {
        return payload.startsWith(Constants.SOS_SIGNAL);
    }

    // 发送扫描到的蓝牙设备信息到云巴
    private void publishToYunba(final String payload) {
        String alias = CacheUtil.getInstance().getYunbaAlias();
        if(TextUtils.isEmpty(alias)) {
            alias = ConfigUtil.getInstance().getYunAlias();
        }
        Log.i(TAG,"ready to publish :" + alias + ":"+payload);
        YunBaManager.publish2ToAlias(this, alias, payload, null,
                new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.i(TAG, "publish2 to alias succeed : " + payload);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        if (exception instanceof MqttException) {
                            MqttException ex = (MqttException)exception;
                            String msg =  "publish2ToAlias failed with error code : " + ex.getReasonCode();
                            Log.e(TAG, msg);
                        }
                    }
                }
        );
    }

}
