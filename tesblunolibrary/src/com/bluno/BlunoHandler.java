package com.bluno;

import com.bluno.BlunoConnection.connectionStateEnum;

public interface BlunoHandler {
	
	public void onDataReceived(String data1, String data2, String data3);
	public void onConnectionStateChange(connectionStateEnum theconnectionStateEnum);
}
