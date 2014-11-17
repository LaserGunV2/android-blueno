package com.bluno.scanner;

import java.util.TimerTask;

import com.bluno.BlunoHandler;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;

public class StopScanTask extends TimerTask
{
	protected BluetoothAdapter adapter;
	protected LeScanCallback callback;
	protected BlunoScanner owner;
	
	public StopScanTask(BlunoScanner owner,BluetoothAdapter adapter,LeScanCallback callback)
	{
		this.adapter=adapter;
		this.callback=callback;
		this.owner=owner;
	}
	
	@Override
	public void run()
	{
		this.adapter.stopLeScan(this.callback);
		this.owner.onScanCompleted();
	}

}
