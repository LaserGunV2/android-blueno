package com.toaster.laser2.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class StorageController
{
	protected final static String STORAGE_NAME="Laser2Storage";
	protected final static String KEY_SENSORBTADDRESS="sensorBtAddress";
	protected SharedPreferences preference;
	
	public StorageController(Context context)
	{
		this.preference=context.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE);
	}
	
	public void saveSensorAddress(String address)
	{
		Editor editor=preference.edit();
		editor.putString(KEY_SENSORBTADDRESS, address);
		editor.commit();
	}
	
	public String getSensorAddress()
	{
		return preference.getString(StorageController.KEY_SENSORBTADDRESS, null);
	}
}
