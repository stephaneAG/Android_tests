package com.example.nashubstealth;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class BackgroundServiceForSMS extends Service {
	
	// use the SMSreceiver class defined below the current class file ( declare member variables to make us of them )
	private SMSreceiver mSMSreceiver;
	private IntentFilter mIntentFilter;
	
	
	/** test constants for futur SMS parsing */
	public final static String ADMIN_AUTH_CODE = "@z34a@"; // the Admin Auth code
	public final static String USER_AUTH_CODE = "@b32y@"; // the User Auth code
	public final static String ADMIN_AUTH_TYPE = "ADMIN_AUTH"; // the Admin Auth type
	public final static String USER_AUTH_TYPE = "USER_AUTH"; // the User Auth type
	
	/** reference to the shared preferences of our app */
	SharedPreferences app_preferences;
	
	
	// we need to override the onCreate method to be able to implement the SMS receiving part
	@Override
	public void onCreate(){
		super.onCreate(); // always call the superclass
		
		/** init the app's preferences */
        app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		// init our member variables responsible for receiving SMS messages
		mSMSreceiver = new SMSreceiver();
		mIntentFilter = new IntentFilter();
		
		// setup the intent filter to look for SMS received
		mIntentFilter.addAction("android.provider.Telephony.SMS_RECEIVED"); // nb: not available as choice from menu
		
		// register the receiver
		registerReceiver(mSMSreceiver, mIntentFilter);
		
		// display a fancy message to the user to indicate the service was just launched succefully
		Toast.makeText(getApplicationContext(), " Tef Service is now running [onCreate]", Toast.LENGTH_LONG).show();
		
	}
	
	@Override
	public IBinder onBind(Intent arg0) { // onBind enables to bind an activity to a service
		// TODO Auto-generated method stub
		return null;
	}
	
	// to explicitly start a service when the user hit the startService button, we use the following method
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
	// I guess I forgot to add it for the Service, but it is needed for the IntentService ( and should have been there as well for Service ..)
		super.onStartCommand(intent, flags, startId);
		
		// this service will run until we stop it
		// ( > actually, this service does not do much except showing a Toast, once started ( & only once (..) ) )
		//Toast.makeText(this, " Tef Service Started !", Toast.LENGTH_LONG).show();
		Toast.makeText(getApplicationContext(), " Tef Service is currently running [onStrtCmd]", Toast.LENGTH_LONG).show();
		return START_STICKY; // this is what actually keep the service going until explicitly stopped
		//return START_NOT_STICKY;
		/** -> the above has been commented as: --> actually not needed to simply display a message
											   --> was provoking 'currently running' to appear twice (..)
		Nb: the above problem could be resolved by diggin/applying "Use Toast whenever we want" ;p
		.. and by using "onCreate" to show our initial message ;p ( -> as called right before "onStrtCmd", & once )
		*/
		
	}
	
	// to explicitly stop the service when the user hit the stopService button
	@Override
	public void onDestroy(){
		super.onDestroy(); // always call the superclass
		//Toast.makeText(this, " Tef Service Stopped !", Toast.LENGTH_LONG).show();
		Toast.makeText(getApplicationContext(), " Tef Service is now stopped [onDestroy]", Toast.LENGTH_LONG).show();
		
		// unregister the SMS receiver
		unregisterReceiver(mSMSreceiver);
	}
	
	// "class within a class" -> BroadcastReceiver class to receive the SMS
	private class SMSreceiver extends BroadcastReceiver {
		private final String TAG = this.getClass().getSimpleName();
		
		@Override
		public void onReceive(Context context, Intent intent){
			
			this.abortBroadcast(); // disable broadcasting the "SMS received" 'event' to the system
			
			Bundle extras = intent.getExtras();
			String strMessage = "";
			if (extras != null){
				Object[] smsextras = (Object[]) extras.get("pdus");
				for (int i=0; i<smsextras.length; i++){
					SmsMessage smsmsg = SmsMessage.createFromPdu( (byte[])smsextras[i] );
					String strMsgBody = smsmsg.getMessageBody().toString();
					String strMsgSrc = smsmsg.getOriginatingAddress();
					strMessage += "SMS from " + strMsgSrc +": " + strMsgBody;
					Log.i(TAG, strMessage);
					Toast.makeText(getApplicationContext(), " SMS received !", Toast.LENGTH_LONG).show();
					
					// DEBUG WIP: Toast the stuff from SMS
					Toast.makeText(getApplicationContext(), " SMS received !", Toast.LENGTH_LONG).show();
					Toast.makeText(getApplicationContext(), "Sender number: " + strMsgSrc, Toast.LENGTH_LONG).show();
					Toast.makeText(getApplicationContext(), "Message: " + strMsgBody, Toast.LENGTH_LONG).show();
					
				}
			}
				
			// finally, hide all that .. -> to call AFTER handling the message .. 
			//this.abortBroadcast(); // disable broadcasting the "SMS received" 'event' to the system
		}
			
	}
	
}
