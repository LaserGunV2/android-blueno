package com.tesblunolibrary;

import java.util.ArrayList;

import com.bluno.BlunoConnection;
import com.bluno.BlunoConnection.connectionStateEnum;
import com.bluno.BlunoHandler;

import android.support.v7.app.ActionBarActivity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity implements BlunoHandler,OnClickListener,UIHandler,OnItemClickListener
{
	protected BlunoConnection bluConn;
	protected Button btnConnect;
	protected Button btnScan;
	protected TextView outText;
	protected ListView listViewAddress;
	protected ArrayAdapter<String> btAddressAdapter;
	protected ThreadedUIHandler threadedUIHandler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		threadedUIHandler=new ThreadedUIHandler(this, this.getMainLooper());
		bluConn=new BlunoConnection(this,this);
		setContentView(R.layout.activity_main);
		btnConnect=(Button)this.findViewById(R.id.btnConnect);
		btnScan=(Button)this.findViewById(R.id.btnScan);
		outText=(TextView)this.findViewById(R.id.textViewOut);
		listViewAddress=(ListView)this.findViewById(R.id.listViewBTAddress);
		btAddressAdapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		listViewAddress.setAdapter(btAddressAdapter);
		//btAddressAdapter.add("abc");
		//btAddressAdapter.add("def");
		btnConnect.setOnClickListener(this);
		btnScan.setOnClickListener(this);
		listViewAddress.setOnItemClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings)
		{
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onDataReceived(String data1, String data2, String data3)
	{
		//Log.v("OUT", data1+data2+data3);
		outText.setText(data1+data2+data3);
	}

	@Override
	public void onConnectionStateChange(connectionStateEnum theconnectionStateEnum)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View v)
	{
		if (v==btnConnect)
		{
			bluConn.connect("88:33:14:D6:B1:84");
		}
		else if (v==btnScan)
		{
			bluConn.scanForBleDevices();
		}
		
	}

	@Override
	public void onScanCompleted(ArrayList<BluetoothDevice> deviceList)
	{
		threadedUIHandler.setFoundBTAddresses(deviceList);
		
		
	}

	@Override
	public void appendDebug(String s)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearDebug()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setAndroidId(String androidId)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setUIMode(int uiMode)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setStatus(String status)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDebugStatus(String debugStatus)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPlayerAliveStatus(boolean isPlayerAlive)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFoundBTAddresses(ArrayList<BluetoothDevice> deviceList)
	{
		btAddressAdapter.clear();
		for (BluetoothDevice device:deviceList)
		{
			btAddressAdapter.add(device.getAddress());
		}
		
	}

	@Override
	public void onItemClick(AdapterView<?> list, View view, int idx, long id)
	{
		//Log.v("TEST", btAddressAdapter.getItem(idx));
		
	}
}
