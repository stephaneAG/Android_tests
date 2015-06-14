package com.example.tefservices;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// wip debug
		Toast.makeText(getApplicationContext(), " MainActivity [onCreate]", Toast.LENGTH_LONG).show();
		
		setContentView(R.layout.activity_main);
		
		// add the buttons defined in the .xml
		Button btnStart = (Button) findViewById(R.id.startBtn);
		Button btnStop = (Button) findViewById(R.id.stopBtn);
		
		// add the click liteners to the above declared buttons
		btnStart.setOnClickListener( new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				// actually start the service
				startService( new Intent(getBaseContext(), MyServices.class) );
				
			}
		});
		
		btnStop.setOnClickListener( new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				// actually start the service
				stopService( new Intent(getBaseContext(), MyServices.class) );
				
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
