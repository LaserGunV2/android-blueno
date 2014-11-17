package com.tesblunolibrary;

import java.util.ArrayList;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class ThreadedUIHandler extends Handler implements UIHandler
{
	protected static final int MSG_appendDebug=0;
	protected static final int MSG_clearDebug=1;
	protected static final int MSG_setAndroidId=2;
	protected static final int MSG_setUiMode=3;
	protected static final int MSG_setStatus=4;
	protected static final int MSG_setDebugStatus=5;
	protected static final int MSG_setPlayerAliveStatus=6;
	protected static final int MSG_setFoundBTAddresses=7;
	
	protected UIHandler actualUI;
	
	public ThreadedUIHandler(UIHandler actualUI,Looper looper)
	{
		super(looper);
		this.actualUI=actualUI;
	}

	@Override
	public void handleMessage(Message msg) 
	{
		switch (msg.what)
		{
			case MSG_appendDebug:
			{
				String par=(String)msg.obj;
				actualUI.appendDebug(par);
			}
			break;
			case MSG_clearDebug:
			{
				actualUI.clearDebug();
			}
			break;
			case MSG_setAndroidId:
			{
				String par=(String)msg.obj;
				actualUI.setAndroidId(par);
			}
			break;
			case MSG_setUiMode:
			{
				Integer par=(Integer)msg.obj;
				actualUI.setUIMode(par.intValue());
			}
			break;
			case MSG_setStatus:
			{
				String par=(String)msg.obj;
				actualUI.setStatus(par);
			}
			break;
			case MSG_setDebugStatus:
			{
				String par=(String)msg.obj;
				actualUI.setDebugStatus(par);
			}
			break;
			case MSG_setPlayerAliveStatus:
			{
				Boolean par=(Boolean)msg.obj;
				actualUI.setPlayerAliveStatus(par.booleanValue());
			}
			break;
			case MSG_setFoundBTAddresses:
			{
				actualUI.setFoundBTAddresses((ArrayList<BluetoothDevice>)msg.obj);
			}
			break;
		}
	}

	@Override
	public void appendDebug(String s) 
	{
		(this.obtainMessage(MSG_appendDebug, s)).sendToTarget();	
	}

	@Override
	public void clearDebug() 
	{
		(this.obtainMessage(MSG_clearDebug)).sendToTarget();
	}

	@Override
	public void setAndroidId(String androidId) {
		(this.obtainMessage(MSG_setAndroidId, androidId)).sendToTarget();
	}

	@Override
	public void setUIMode(int uiMode) {
		(this.obtainMessage(MSG_setUiMode, new Integer(uiMode))).sendToTarget();
		
	}

	@Override
	public void setStatus(String status) {
		(this.obtainMessage(MSG_setStatus,status)).sendToTarget();
	}

	@Override
	public void setDebugStatus(String debugStatus) {
		(this.obtainMessage(MSG_setDebugStatus,debugStatus)).sendToTarget();
	}

	@Override
	public void setPlayerAliveStatus(boolean isPlayerAlive)
	{
		(this.obtainMessage(MSG_setPlayerAliveStatus,Boolean.valueOf(isPlayerAlive))).sendToTarget();
		
	}

	@Override
	public void setFoundBTAddresses(ArrayList<BluetoothDevice> deviceList)
	{
		(this.obtainMessage(MSG_setFoundBTAddresses,deviceList)).sendToTarget();
		
	}
	
	

}
