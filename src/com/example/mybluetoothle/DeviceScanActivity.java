package com.example.mybluetoothle;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class DeviceScanActivity extends Activity {

    private BluetoothAdapter mBluetoothAdapter;  // 蓝牙适配器
    private boolean mScanning;  
    private Handler mHandler;
	private ArrayList<String> device_names = new ArrayList<String>();
	private ArrayList<String> device_addresses = new ArrayList<String>();
    private String mBluetoothDeviceAddress;
    private String mBluetoothDeviceAddress2;
    private BluetoothLeService mBluetoothLeService;
    private int mConnectionState = STATE_DISCONNECTED;
    Intent gattServiceIntent = null;
    private BluetoothGattService mBluetoothGattService = null;
    private BluetoothGattCharacteristic mBluetoothGattCharacteristic = null;
    private BluetoothGattService mBluetoothGattService2 = null;
    private BluetoothGattCharacteristic mBluetoothGattCharacteristic2 = null;
    
    private final UUID service_uuid = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    private final UUID characteristic_uuid = UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb");
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";

	private TextView device_name1,device_name2,device_address1,device_address2,device_state1,device_state2,tv_detail1,tv_detail2;
	private Button device_connect1,device_connect2,device_detail1,device_detail2;
	private Context mContext;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    // 设置扫描的时间
    private static final long SCAN_PERIOD = 3000;
    private int i = 0;
    private int j = 0;
    
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e("DeviceScanActivity", "Unable to initialize Bluetooth");
                finish(); 
            }else
            {
                Log.e("DeviceScanActivity", "Sucess initialize Bluetooth");            	
            }
            // Automatically connects to the device upon successful start-up initialization.
            // 自动连接设备
            mBluetoothLeService.connect1(mBluetoothDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_scan);
        getActionBar().setTitle("扫描BLE Device");
        mHandler = new Handler();
        initView();		
        
        // 检测是否具有蓝牙功能
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this,"蓝牙设备不支持", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        // android4.3以上系统，获取一个蓝牙适配器
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "蓝牙设备不支持", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        device_connect1.setOnClickListener(new connectClickListener() );
        device_connect2.setOnClickListener(new connect2ClickListener() );
        device_detail1.setOnClickListener(new detailClickListener());
        device_detail2.setOnClickListener(new detail2ClickListener());
        
        gattServiceIntent = new Intent(this, BluetoothLeService.class);

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
	}

	class connectClickListener implements  View.OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			mBluetoothDeviceAddress = device_address1.getText()+"";
            Toast.makeText(DeviceScanActivity.this,"Bind Service To" + mBluetoothDeviceAddress, Toast.LENGTH_SHORT).show();
	        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
		}
	}

	class connect2ClickListener implements  View.OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			mBluetoothDeviceAddress2 = device_address2.getText()+"";
            Toast.makeText(DeviceScanActivity.this,"bind service to" + mBluetoothDeviceAddress2, Toast.LENGTH_SHORT).show();
            mBluetoothLeService.connect2(mBluetoothDeviceAddress2);
		}
	}

	class detailClickListener implements View.OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			mBluetoothGattService = mBluetoothLeService.getGattService(service_uuid);
			mBluetoothGattCharacteristic = mBluetoothGattService.getCharacteristic(characteristic_uuid);
			if(mBluetoothGattCharacteristic != null){
	            Toast.makeText(DeviceScanActivity.this,"get characteristic sucess!", Toast.LENGTH_SHORT).show();				
			}
            mBluetoothLeService.setCharacteristicNotification(
                    mBluetoothGattCharacteristic, true);
		}
	}

	class detail2ClickListener implements  View.OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			mBluetoothGattService2 = mBluetoothLeService.getGattService2(service_uuid);
			mBluetoothGattCharacteristic2 = mBluetoothGattService2.getCharacteristic(characteristic_uuid);
			if(mBluetoothGattCharacteristic2 != null){
	            Toast.makeText(DeviceScanActivity.this,"get characteristic sucess!", Toast.LENGTH_SHORT).show();				
			}
            mBluetoothLeService.setCharacteristicNotification2(
                    mBluetoothGattCharacteristic2, true);
		}
	}
	
	private void initView() {
		// TODO Auto-generated method stub
		device_name1 = (TextView) findViewById(R.id.device_name1);
		device_name2 = (TextView) findViewById(R.id.device_name2);
		device_address1 = (TextView) findViewById(R.id.device_address1);
		device_address2 = (TextView) findViewById(R.id.device_address2);
		device_state1 = (TextView) findViewById(R.id.device_state1);
		device_state2 = (TextView) findViewById(R.id.device_state2);
		tv_detail1 = (TextView) findViewById(R.id.tv_detail1);
		tv_detail2 = (TextView) findViewById(R.id.tv_detail2);
		
		device_connect1 = (Button) findViewById(R.id.device_connect1);
		device_connect2 = (Button) findViewById(R.id.device_connect2);
		device_detail1 = (Button) findViewById(R.id.device_detail1);
		device_detail2 = (Button) findViewById(R.id.device_detail2);
		
		device_state1.setText("unconnect");
		device_state2.setText("unconnect");
        mContext = getApplicationContext();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
        }
        return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_scan:
            scanLeDevice(true);
            break;
        case R.id.menu_stop:
            scanLeDevice(false);
            break;
        }
    return true;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        scanLeDevice(true);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
        scanLeDevice(false);
        unregisterReceiver(mGattUpdateReceiver);
	}
	
	@Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);  // 解除绑定与service
        mBluetoothLeService = null;
    }
	
    // 扫描蓝牙设备
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
        	// 在一定时间内关闭扫描
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                	updateView();
                    invalidateOptionsMenu();  // 在运行时更改选项菜单
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);  // 开始扫描设备
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);  // 停止扫描设备
        }
        invalidateOptionsMenu();  // 更新菜单
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
        		device_names.add(device.getName());
        		device_addresses.add(device.getAddress());
        }
    };
    
    private void updateView(){
    	for(String d_name : device_names){
    		if(i==0)
    		{
    			device_name1.setText(d_name);
    			i=1;
    		}else if(i==1){
    			device_name2.setText(d_name);
    		}
    	}
    	for(String d_address : device_addresses){
			if(j==0)
			{
				device_address1.setText(d_address);
				j=1;
			}else if(j==1){
				device_address2.setText(d_address);
			}
    	}
    }
    
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final int extra = intent.getIntExtra("extra",1);
            if (ACTION_GATT_CONNECTED.equals(action)) {
            	
            	if(extra == 1)
            	{
            		updateConnectionState("connected");
            	}else if(extra == 2){
                    updateConnectionState2("connected");           		
            	}
            	
            } else if (ACTION_GATT_DISCONNECTED.equals(action)) {
            	
            	if(extra == 1)
            	{
            		updateConnectionState("disconnected");
            	}else if(extra == 2){
                    updateConnectionState2("disconnected");           		
            	}
            	
            } else if(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)){
                Toast.makeText(DeviceScanActivity.this,"discover services sucess!", Toast.LENGTH_SHORT).show();            	
            } else if(BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)){
            	
            	if(extra == 1)
            	{
                	updateDetail(intent.getExtras().getString(
    						BluetoothLeService.EXTRA_DATA));            	
                }else if(extra == 2){
                	updateDetail2(intent.getExtras().getString(
    						BluetoothLeService.EXTRA_DATA));             	
                }
            	
            } 
        }

    };
    
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_GATT_CONNECTED);
        intentFilter.addAction(ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
    
    // 更新连接状态
    private void updateConnectionState(final String state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                device_state1.setText(state);
            }
        });
    }
    
    // 更新连接状态
    private void updateConnectionState2(final String state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                device_state2.setText(state);
            }
        });
    }

	private void updateDetail(final String stringExtra) {
		// TODO Auto-generated method stub	
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (stringExtra != null) {
                    tv_detail1.setText(stringExtra);
                }            
            }
        });
	}
	
	private void updateDetail2(final String stringExtra) {
		// TODO Auto-generated method stub	
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (stringExtra != null) {
                    tv_detail2.setText(stringExtra);
                }            
            }
        });
	}
}