package com.bluno;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.bluno.scanner.BlunoScanner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class BlunoConnection implements LeScanCallback{
	//attributes
	protected BlunoHandler bleHandler;
	protected Context mainContext;
	protected int bleBaudrate;	//set the default baud rate to 115200
	protected String blePassword = "AT+PASSWOR=DFRobot\r\n";
	protected String bleBaudrateBuffer = "AT+CURRUART=" + bleBaudrate + "a\r\n";
	protected static final String SerialPortUUID="0000dfb1-0000-1000-8000-00805f9b34fb";
	protected static final String CommandUUID="0000dfb2-0000-1000-8000-00805f9b34fb";
	protected static final String ModelNumberStringUUID="00002a24-0000-1000-8000-00805f9b34fb";
	protected static BluetoothGattCharacteristic 	mSCharacteristic, 
												mModelNumberCharacteristic,
												mSerialPortCharacteristic,
												mCommandCharacteristic;
	protected BLEService mBluetoothLeService;
	protected BluetoothAdapter bleAdapter;
	protected ArrayList<BluetoothDevice> bleDevices;
	protected String deviceName;
	protected String deviceAddress;
    public enum connectionStateEnum{isNull, isScanning, isToScan, isConnecting , isConnected, isDisconnecting};
    protected connectionStateEnum mConnectionState;
	private static final int REQUEST_ENABLE_BT = 1;
	private Handler handler= new Handler();
	public boolean isBLEConnected = false;
	protected boolean isScanning;
	protected BluetoothDevice bleDevice;
	protected final static String TAG = BlunoConnection.class.getSimpleName();
	protected ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics;
	protected Runnable mConnectingOverTimeRunnable=new Runnable(){
		@Override
		public void run() {
        	if(mConnectionState==connectionStateEnum.isConnecting)
        		mConnectionState=connectionStateEnum.isToScan;
			onConnectionStateChange(mConnectionState);
			Log.v(this.getClass().getName(), "state change:"+mConnectionState.toString());
			mBluetoothLeService.close();
		}};
		
	protected Runnable mDisconnectingOverTimeRunnable=new Runnable(){
		@Override
		public void run() {
        	if(mConnectionState==connectionStateEnum.isDisconnecting)
        		mConnectionState=connectionStateEnum.isToScan;
			onConnectionStateChange(mConnectionState);
			mBluetoothLeService.close();
		}};
		
	protected final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            System.out.println("mServiceConnection onServiceConnected");
            
        	mBluetoothLeService = ((BLEService.LocalBinder) service).getService();
        	if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                //((Activity) mainContext).finish();
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        	System.out.println("mServiceConnection onServiceDisconnected");
            mBluetoothLeService = null;
        }
    };
    
    protected final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @SuppressLint("DefaultLocale")
		@Override
        public void onReceive(Context context, Intent intent) {
        	final String action = intent.getAction();
            System.out.println("mGattUpdateReceiver->onReceive->action="+action);
            if (BLEService.ACTION_GATT_CONNECTED.equals(action)) {
                isBLEConnected = true;
            	handler.removeCallbacks(mConnectingOverTimeRunnable);
            } 
            else if (BLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
                isBLEConnected = false;
            	handler.removeCallbacks(mDisconnectingOverTimeRunnable);
            	mConnectionState=connectionStateEnum.isDisconnecting;
            	onConnectionStateChange(mConnectionState);
            	mBluetoothLeService.close();
            } 
            else if (BLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
            	for (BluetoothGattService gattService : mBluetoothLeService.getSupportedGattServices()) {
            		System.out.println("ACTION_GATT_SERVICES_DISCOVERED  "+
            				gattService.getUuid().toString());
            	}
            	getGattServices(mBluetoothLeService.getSupportedGattServices());
            } 
            else if (BLEService.ACTION_DATA_AVAILABLE.equals(action)) {
            	if(mSCharacteristic==mModelNumberCharacteristic)
            	{
            		if (intent.getStringExtra(BLEService.EXTRA_DATA).toUpperCase().startsWith("DF BLUNO")) {
						mBluetoothLeService.setCharacteristicNotification(mSCharacteristic, false);
						mSCharacteristic=mCommandCharacteristic;
						mSCharacteristic.setValue(blePassword);
						mBluetoothLeService.writeCharacteristic(mSCharacteristic);
						mSCharacteristic.setValue(bleBaudrateBuffer);
						mBluetoothLeService.writeCharacteristic(mSCharacteristic);
						mSCharacteristic=mSerialPortCharacteristic;
						mBluetoothLeService.setCharacteristicNotification(mSCharacteristic, true);
						mConnectionState = connectionStateEnum.isConnected;
					}
            		else {
            			Toast.makeText(mainContext, "Please select DFRobot devices",Toast.LENGTH_SHORT).show();
					}
            	}
            	else if (mSCharacteristic==mSerialPortCharacteristic) {
            		onSerialReceived(intent.getStringExtra(BLEService.EXTRA_DATA));
				}
            	
            
            	System.out.println("displayData "+intent.getStringExtra(BLEService.EXTRA_DATA));
            	
//            	mPlainProtocol.mReceivedframe.append(intent.getStringExtra(BluetoothLeService.EXTRA_DATA)) ;
//            	System.out.print("mPlainProtocol.mReceivedframe:");
//            	System.out.println(mPlainProtocol.mReceivedframe.toString());

            	
            }
        }
    };
    //methods
    
    protected BlunoScanner addressScanner;
    
    
    
    
	public BlunoConnection(Context context,BlunoHandler handler)
	{
		mainContext=context;	
		isBLEConnected = false;
		isScanning = false;
		bleBaudrate = 115200;
		bleDevices = new ArrayList<BluetoothDevice>();
		//awal onCreateProcess();
		if(!initiate())
		{
			Toast.makeText(mainContext, "Bluetooth LE is not supported",
					Toast.LENGTH_SHORT).show();
			((Activity) mainContext).finish();
		}
    	Log.i(TAG,"CREATE PROCESS: "+mServiceConnection);
        Intent gattServiceIntent = new Intent(mainContext, BLEService.class);
        mainContext.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
		//akhir dr onCreateProcess
		serialBegin(115200);
		this.bleHandler = handler;
		mConnectionState = connectionStateEnum.isNull;
		
		//awal dr onResume
		mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
		if (!bleAdapter.isEnabled()) {
			if (!bleAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				((Activity) mainContext).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}
	    mainContext.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
	    //akhir dr onResume
	    
	    this.addressScanner=new BlunoScanner(bleAdapter, handler);
	}
	
	public void setBlunoHandler(BlunoHandler handlerIn){
		this.bleHandler = handlerIn;
	}
	
	@Override
	public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
		System.out.println("mLeScanCallback onLeScan run ");
		addDevice(device);
		if(device.getAddress().equalsIgnoreCase(deviceAddress)){
			this.bleDevice = device;
			if (mBluetoothLeService.connect(bleDevice.getAddress())) {
		        Log.d(TAG, "Connect request success");
	        	mConnectionState=connectionStateEnum.isConnected;
	        	onConnectionStateChange(mConnectionState);
	            handler.postDelayed(mConnectingOverTimeRunnable, 10000);
	            
        	}
	        else {
		        Log.d(TAG, "Connect request fail");
	        	mConnectionState=connectionStateEnum.isToScan;
	        	onConnectionStateChange(mConnectionState);
			}
			//scanLeDevice(false);
		}
	}

    public void onCreateProcess(){
    	if(!initiate())
		{
			Toast.makeText(mainContext, "Bluetooth LE is not supported",
					Toast.LENGTH_SHORT).show();
			((Activity) mainContext).finish();
		}
    	Log.i(TAG,"CREATE PROCESS: "+mServiceConnection);
        Intent gattServiceIntent = new Intent(mainContext, BLEService.class);
        mainContext.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }
    
    public Context getContext(){
		return this.mainContext;
	}
    
    public String getDeviceName(){
    	return this.deviceName;
    }
    
    public String getDeviceAddress(){
    	return this.deviceAddress;
    }
	
	public void serialBegin(int baud){
		bleBaudrate=baud;
		bleBaudrateBuffer = "AT+CURRUART="+bleBaudrate+"\r\n";
	}

	public void onConnectionStateChange(connectionStateEnum theconnectionStateEnum){
		Log.v(this.getClass().getName(), "on state changed");
		bleHandler.onConnectionStateChange(theconnectionStateEnum);
	}
	
	void scanLeDevice(final boolean enable) {
		if (enable) {
			// Stops scanning after a pre-defined scan period.
			System.out.println("mBluetoothAdapter.startLeScan");
			if(this.bleDevices != null){
				this.clear();
			}
			
			if(!isScanning){
				isScanning = true;
				bleAdapter.startLeScan(this);
			}
		} else {
			if(isScanning){
				isScanning = false;
				bleAdapter.stopLeScan(this);
			}
		}
	}
	
	private void addDevice(BluetoothDevice device) {
		if (!bleDevices.contains(device)) {
			bleDevices.add(device);
		}
	}
	
	private void clear() {
		bleDevices.clear();
	}
	
	public void onSerialReceived(String dataReceived){
		Log.v(this.getClass().getName(), "data recv!");
		String[] tokens=dataReceived.split("#");
		if ((tokens.length>=3)&&(!tokens[0].equals("H")))
		{
			Log.v(this.getClass().getName(), "data rec:"+Arrays.toString(tokens));
			bleHandler.onDataReceived(tokens[0], tokens[1], tokens[2]);
		}
	}
	
	public void connect(String address){
		Log.i(TAG,"BLE ADAPTER = "+this.bleAdapter.toString());
		deviceAddress = address;
		scanLeDevice(true);
	}
	
	private boolean initiate()
	{
		// Use this check to determine whether BLE is supported on the device.
		// Then you can
		// selectively disable BLE-related features.
		if (!mainContext.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			return false;
		}
		
		// Initializes a Bluetooth adapter. For API level 18 and above, get a
		// reference to
		// BluetoothAdapter through BluetoothManager.
		final BluetoothManager bluetoothManager = (BluetoothManager) mainContext.getSystemService(Context.BLUETOOTH_SERVICE);
		bleAdapter = bluetoothManager.getAdapter();
	
		// Checks if Bluetooth is supported on the device.
		if (bleAdapter == null) {
			return false;
		}
		return true;
	}
	
	public void onPauseProcess() {
    	System.out.println("BLUNOActivity onPause");
		//scanLeDevice(false);
		//mainContext.unregisterReceiver(mGattUpdateReceiver);
		//bleDevices.clear();
    	//mConnectionState=connectionStateEnum.isToScan;
    	//onConnectionStateChange(mConnectionState);
		//if(mBluetoothLeService!=null)
		//{
		//	mBluetoothLeService.disconnect();
         //   handler.postDelayed(mDisconnectingOverTimeRunnable, 10000);

//			mBluetoothLeService.close();
		//}
		//mSCharacteristic=null;

	}
	public void onStopProcess() {
		System.out.println("MiUnoActivity onStop");
		if(mBluetoothLeService!=null)
		{
//			mBluetoothLeService.disconnect();
//            mHandler.postDelayed(mDisonnectingOverTimeRunnable, 10000);
        	handler.removeCallbacks(mDisconnectingOverTimeRunnable);
			mBluetoothLeService.close();
		}
		mSCharacteristic=null;
	}

	public void onDestroyProcess() {
        mainContext.unbindService(mServiceConnection);
        mBluetoothLeService = null;
	}
	
	public void onActivityResultProcess(int requestCode, int resultCode, Intent data) {
		// User chose not to enable Bluetooth.
//		if (requestCode == REQUEST_ENABLE_BT
//				&& resultCode == Activity.RESULT_CANCELED) {
//			((Activity) mainContext).finish();
//			return;
//		}
	}
	public void onResumeProcess() {
    	System.out.println("BlUNOActivity onResume");
		// Ensures Bluetooth is enabled on the device. If Bluetooth is not
		// currently enabled,
		// fire an intent to display a dialog asking the user to grant
		// permission to enable it.
		
	}
	
	private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BLEService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
	
	private void getGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        mModelNumberCharacteristic=null;
        mSerialPortCharacteristic=null;
        mCommandCharacteristic=null;
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            uuid = gattService.getUuid().toString();
            System.out.println("displayGattServices + uuid="+uuid);
            
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                uuid = gattCharacteristic.getUuid().toString();
                if(uuid.equals(ModelNumberStringUUID)){
                	mModelNumberCharacteristic=gattCharacteristic;
                	System.out.println("mModelNumberCharacteristic  "+mModelNumberCharacteristic.getUuid().toString());
                }
                else if(uuid.equals(SerialPortUUID)){
                	mSerialPortCharacteristic = gattCharacteristic;
                	System.out.println("mSerialPortCharacteristic  "+mSerialPortCharacteristic.getUuid().toString());
//                    updateConnectionState(R.string.comm_establish);
                }
                else if(uuid.equals(CommandUUID)){
                	mCommandCharacteristic = gattCharacteristic;
                	System.out.println("mSerialPortCharacteristic  "+mSerialPortCharacteristic.getUuid().toString());
//                    updateConnectionState(R.string.comm_establish);
                }
            }
            mGattCharacteristics.add(charas);
        }
        
        if (mModelNumberCharacteristic==null || mSerialPortCharacteristic==null || mCommandCharacteristic==null) {
			Toast.makeText(mainContext, "Please select DFRobot devices",Toast.LENGTH_SHORT).show();
            mConnectionState = connectionStateEnum.isToScan;
		}
        else {
        	mSCharacteristic=mModelNumberCharacteristic;
        	mBluetoothLeService.setCharacteristicNotification(mSCharacteristic, true);
        	mBluetoothLeService.readCharacteristic(mSCharacteristic);
		}
        
    }
	
	public void scanForBleDevices()
	{
		this.addressScanner.startScan();
		Log.v(this.getClass().getName(), "start scan");
	}
	
	public connectionStateEnum getConnectionState()
	{
		return this.mConnectionState;
	}
}
