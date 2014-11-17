package com.bluno;

import java.util.ArrayList;

import android.bluetooth.BluetoothDevice;

import com.bluno.BlunoConnection.connectionStateEnum;

public interface BlunoHandler 
{
	
	public void onDataReceived(String strGunId, String strCounter, String strSensorId);
	public void onConnectionStateChange(connectionStateEnum theconnectionStateEnum);
	public void onScanCompleted(ArrayList<BluetoothDevice> deviceList);
}
