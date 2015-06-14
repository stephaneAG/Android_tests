package com.example.nashubstealth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
// import the Service class as well ..
import com.example.nashubstealth.BackgroundServiceForSMS;

public class BootCompleteIntentReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent){
		
		// act accordingly if the device has booted completely
		if ( "android.intent.action.BOOT_COMPLETED".equals( intent.getAction() ) ){
			
			// TO DO HERE: check if preferences concerning 'onStartBoot' have been set, and if so, check its status (true|false)
			// if 'onStartBoot' is true, then boot the service like below .. 
			// else, do nothing, as the user will activate the service himself later using an activity of a widget of ours ;p
			
			Intent pushIntent = new Intent(context, BackgroundServiceForSMS.class); // point the intent to our Service class
			context.startService(pushIntent); // start the background service for SMS stuff (..)
			
		}
		
	}
	
}
