package com.tesblunolibrary;

import com.bluno.BlunoConnection;
import com.bluno.BlunoConnection.connectionStateEnum;
import com.bluno.BlunoHandler;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity implements BlunoHandler,OnClickListener
{
	protected BlunoConnection bluConn;
	protected Button btnConnect;
	protected TextView outText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		bluConn=new BlunoConnection(this,this);
		setContentView(R.layout.activity_main);
		btnConnect=(Button)this.findViewById(R.id.btnConnect);
		outText=(TextView)this.findViewById(R.id.editTextOut);
		btnConnect.setOnClickListener(this);
		
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
		Log.v("OUT", data1+data2+data3);
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
		
	}
}
