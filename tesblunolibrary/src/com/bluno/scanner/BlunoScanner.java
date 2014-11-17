package com.bluno.scanner;

import java.util.ArrayList;
import java.util.Timer;

import com.bluno.BlunoHandler;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;

public class BlunoScanner implements LeScanCallback
{
	protected BluetoothAdapter adapter;
	protected final static int SCAN_PERIOD=5000;
	protected Timer timer;
	protected StopScanTask timerTask;
	protected ArrayList<BluetoothDevice> deviceList;
	protected BlunoHandler handler;
	
	
	public BlunoScanner(BluetoothAdapter adapter,BlunoHandler handler)
	{
		this.adapter=adapter;
		this.timer=new Timer();
		this.timerTask=new StopScanTask(this, adapter, this);
		this.deviceList=new ArrayList<BluetoothDevice>();
		this.handler=handler;
	}
	
	public void startScan()
	{
		this.deviceList.clear();
		this.adapter.startLeScan(this);
		this.timer.schedule(new StopScanTask(this,adapter, this), BlunoScanner.SCAN_PERIOD);
	}

	@Override
	public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord)
	{
		if (!this.deviceList.contains(device))
			deviceList.add(device);
	}
	
	public void onScanCompleted()
	{
		handler.onScanCompleted(deviceList);
	}
	
	
}
