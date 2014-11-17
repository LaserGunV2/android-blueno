package com.tesblunolibrary;

import java.util.ArrayList;

import android.bluetooth.BluetoothDevice;

public interface UIHandler 
{
	public void appendDebug(String s);
	public void clearDebug();

	public void setAndroidId(String androidId);
	public void setUIMode(int uiMode);
	public void setStatus(String status);
	public void setDebugStatus(String debugStatus);
	public void setPlayerAliveStatus(boolean isPlayerAlive);
	public void setFoundBTAddresses(ArrayList<BluetoothDevice> deviceList);
}
