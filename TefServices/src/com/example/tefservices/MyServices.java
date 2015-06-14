package com.example.tefservices;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class MyServices extends Service {
//public class MyServices extends IntentService {

	// use the SMSreceiver class defined below the current class file ( declare member variables to make us of them )
	private SMSreceiver mSMSreceiver;
	private IntentFilter mIntentFilter;
	
	// it seems that we need the following for the IntentService ( was not needed to using the Service class )
	/**
	public MyServices(){
		super("MyServices");
	}
	*/
	
	// the IntentService class needs to implement the following ( -> and it is actually very usefull ;p )
	/**
	@Override
	protected void onHandleIntent(Intent intent){
		
		//Toast.makeText(getApplicationContext(), " Tef Service is now running [onCreate]", Toast.LENGTH_LONG).show();
		Toast.makeText(this, " Tef Service is now running [onHandleIntent]", Toast.LENGTH_LONG).show();
		// DO STUFF HERE (..)
	}
	*/
	
	
	// we need to override the onCreate method to be able to implement the SMS receiving part
	@Override
	public void onCreate(){
		super.onCreate(); // always call the superclass
		
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
		
		/** necessary constants */
		public final static String EXTRA_MESSAGE = "com.example.tefservices.MESSAGE";
		
		/** test constants for futur SMS parsing */
		public final static String ADMIN_AUTH_CODE = "@z34a@"; // the Admin Auth code
		public final static String USER_AUTH_CODE = "*789#"; // the User Auth code // UPDATED
		public final static String ADMIN_AUTH_TYPE = "ADMIN_AUTH"; // the Admin Auth type
		public final static String USER_AUTH_TYPE = "USER_AUTH"; // the User Auth type
		/** same as above but the following constants are used for the 'history' part */
		public final static String ADMIN_ACTIVATE_HISTORY_AUTH_CODE = "*2580#"; // the Admin ( set & activate ) History Auth code
		public final static String ADMIN_DESACTIVATE_HISTORY_AUTH_CODE  = "*2580#"; // the Admin ( desactivate ) History Auth code
		
		/** reference to the shared preferences of our app */
		SharedPreferences app_preferences;
		
		
		/** Override the broadcastreceiver constructor method to init the SharedPreference as we'd have done for an Activity */
		public SMSreceiver (){
			///** init the app's preferences */
	        //app_preferences = PreferenceManager.getDefaultSharedPreferences(this); // working from an Activity
	        Context ctx  = getApplicationContext();
	        app_preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
		}
		
		
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
					Toast.makeText(getApplicationContext(), "Sender Number:" + strMsgSrc, Toast.LENGTH_LONG).show();
					Toast.makeText(getApplicationContext(), "Message:" + strMsgBody, Toast.LENGTH_LONG).show();
					
					//  WIP DEBUG -> handle the message received
					handleMessageReceived(strMsgBody, strMsgSrc);
					
				}
			}
			
			// finally, hide all that .. -> to call AFTER handling the message .. 
			//this.abortBroadcast(); // disable broadcasting the "SMS received" 'event' to the system
		}
		
		
		/** Fcn thhat handles any message received while the app is running */
		/** It actually parses the message and initiate sending a message back to where it originated (..) */
		
		private void handleMessageReceived(String theMessageBody, String theMessageOrigin){
			
			/** check if the message is not null ( if we got some text to send actually ) */
	    	if ( theMessageBody.equals("") ){
	    		return; // return to prevent a fatal exception
	    	}
	    	
	    	
	    	/** do a little auth check */
	    	String messageAuthType = getAuthTypeFromMessage(theMessageBody);
	    	String currentAuth = checkAuth(messageAuthType);
	    	
	    	/** act according to the current auth type */
	    	if (currentAuth.equals(ADMIN_AUTH_TYPE)){
	    		Log.d("ADMIN AUTH SESSION BEGAN", " > parsing message for new data ..");
	    		// extract the data from the message
	    		String theNewData = getDataFromMessage(theMessageBody);
	    		Log.d("New data from Admin auth session:", theNewData);
	    		// save it in the appropriate location
	    		setStuffToPrefs("theDataPrefKey", theNewData);
	    		// send back a message with the data freshly set up --> for the moment ( as wip ) just set the text of the next activity accordingly (..)
	    		String theFreshData = getStuffFromPrefs("theDataPrefKey");
	    		String theAdminMessage = "Data updated to: " + theFreshData;
	    		Toast.makeText(getApplicationContext(), "ADMIN AUTHENTICATION", Toast.LENGTH_LONG).show();
	    		//Toast.makeText(getApplicationContext(), "New Data: " + theNewData, Toast.LENGTH_LONG).show();
	    		Toast.makeText(getApplicationContext(), "Message returned to user: " + theAdminMessage, Toast.LENGTH_LONG).show();
	    		
	    		/** send a SMS back to where the 'request' SMS originated */
	        	sendSMS(theMessageOrigin, theAdminMessage);
	    	
	        // TO DO: check for " ADMIN_ACTIVATE_HISTORY_AUTH_CODE " 
	        // TODO: check for " ADMIN_DESACTIVATE_HISTORY_AUTH_CODE "
	        	
	    	} else if (currentAuth.equals(USER_AUTH_TYPE)){
	    		Log.d("USER AUTH SESSION BEGAN", " > parsing message for new data ..");
	    		// get the last known data and sent it back to where the first message originated
	    		String theFreshData = getStuffFromPrefs("theDataPrefKey");
	    		String theUserMessage = "Access granted: " + theFreshData;
	    		Toast.makeText(getApplicationContext(), "USER AUTHENTICATION", Toast.LENGTH_LONG).show();
	    		Toast.makeText(getApplicationContext(), "Message returned to user: " + theUserMessage, Toast.LENGTH_LONG).show();
	    		
	    		/** send a SMS back to where the 'request' SMS originated */
	        	sendSMS(theMessageOrigin, theUserMessage);
	        	
	        	// TO DO: check if the 'history' part is set / should be used:
	        	// String theHistoryNumKey = getStuffFromPrefs("theHistoryNumKey");
	        	// if != "", then also
	        	// String theAdminMessage = "Data sent to: " + theMessageOrigin;
	        	// sendSMS(theHistoryNumKey, theUserMessage);
	    		
	    	} else {
	    		Toast.makeText(getApplicationContext(), "UNAUTHORIZED ACCESS OR UNKNOWN AUTHENTICATION", Toast.LENGTH_LONG).show();
	    	}
	    	
	    	/** send a SMS*/
	    	//sendSMS("+33681382722", message);
	    	
	    	
			
		}
		
		
		/** MESSAGE PARSING FCNS & STUFF */
		
		/** Fcn that extracts the Auth part of the message */
		private String getAuthTypeFromMessage(String theMessage){
			/** verify that the message received is at least as large as the auth types */
			if (theMessage.length() < 6 ){
				Log.d("getAuthTypeFromMessage()", " > message length inferior to 6 characters");
				return "";
			}
			
			/** get the substring of 'theMessage' containing the Auth Type */
			String theAuthType = theMessage.substring(0, 6); // extract the '@z34a@' part ( the actual auth type )
			Log.d("getAuthTypeFromMessage()", " > auth type extracted from message");
			return theAuthType;
		}
		
		/** Fcn that extracts the Data part of the message */
		private String getDataFromMessage(String theMessage){
			/** verify that the message received is at least as large as the auth types */
			if (theMessage.length() < 17 ){
				Log.d("getDataFromMessage()", " > message length inferior to 17 characters");
				return "";
			}
			
			/** get the substring of 'theMessage' containing the Auth Type */
			String theData = theMessage.substring(7, 17); // extract the '@z34a@' part ( the actual auth type )
			Log.d("getDataFromMessage()", " > auth type extracted from message");
			return theData;
		}
		
		/** Fcn that checks whether the message comes from an authorized user, and in that case, if it is a user or the admin */
		private String checkAuth(String theMessage){
			
			/** try to find an auth type that matches the few characters at the beginning of the text message */
			if ( theMessage.equals(ADMIN_AUTH_CODE) ){
				Log.d(ADMIN_AUTH_CODE, "admin auth just occured");
				return ADMIN_AUTH_TYPE;
			} else if ( theMessage.equals(USER_AUTH_CODE) ){
				Log.d(USER_AUTH_CODE, "user auth just occured");
				return USER_AUTH_TYPE;
			} else {
				Log.d("UNKNOWN_AUTH", "failed auth just occured");
				return "";
			}
			
		}
		
		
		
		/** SharedPreference stuff -> get & set the data to be returned by SMS messages */
		
		/** Fcn that set the desired stuff into the shared preferences */
		private void setStuffToPrefs(String theStrKeyName, String theStrValue){
			SharedPreferences.Editor editor = app_preferences.edit();
			editor.putString(theStrKeyName, theStrValue);
			Log.d("Preferences", theStrValue);
			editor.commit();
		}
		
		/** Fcn that get the desired stuff out of the shared preferences */
		private String getStuffFromPrefs(String theStrKeyName){
			String theStrValue = app_preferences.getString(theStrKeyName, "");
			Log.d("Preferences", theStrValue);
			return theStrValue;
		}
		
		
		/** SMS sending stuff -> allows stuff to be sent back to originating adresses of received authenticated SMS requests (..) */
		
		/** Called when the user clicks the send button, this time sending an sms message*/
		
		//private void sendSMS(String phoneNumber, String message){
		//	SmsManager sms = SmsManager.getDefault();
		//	sms.sendTextMessage(phoneNumber, null, message, null, null);
		//}
		
		private void sendSMS(String phoneNumber, String message){
			String SENT = "SMS_SENT";
			String DELIVERED = "SMS_DELIVERED";
			
			// both lines below were working when defined from an Activity
			//PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
			//PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);
			
			Context ctx = getApplicationContext();
			PendingIntent sentPI = PendingIntent.getBroadcast(ctx, 0, new Intent(SENT), 0);
			PendingIntent deliveredPI = PendingIntent.getBroadcast(ctx, 0, new Intent(DELIVERED), 0);
			
			/** stuff to do when the message has been sent */
			registerReceiver(new BroadcastReceiver(){
				@Override
				public void onReceive(Context arg0, Intent arg1){
					switch (getResultCode()){
						case Activity.RESULT_OK:
							Toast.makeText(getBaseContext(), "SMS sent", Toast.LENGTH_SHORT).show();
							break;
						case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
							Toast.makeText(getBaseContext(), "Generic failure", Toast.LENGTH_SHORT).show();
							break;
						case SmsManager.RESULT_ERROR_NO_SERVICE:
							Toast.makeText(getBaseContext(), "No service", Toast.LENGTH_SHORT).show();
							break;
						case SmsManager.RESULT_ERROR_NULL_PDU:
							Toast.makeText(getBaseContext(), "Null PDU", Toast.LENGTH_SHORT).show();
							break;
						case SmsManager.RESULT_ERROR_RADIO_OFF:
							Toast.makeText(getBaseContext(), "Radio off", Toast.LENGTH_SHORT).show();
							break;
					}
				}
			}, new IntentFilter(SENT));
			
			/** stuff to do when the message has been delivered */
			registerReceiver(new BroadcastReceiver(){
				@Override
				public void onReceive(Context arg0, Intent arg1){
					switch (getResultCode()){
						case Activity.RESULT_OK:
							Toast.makeText(getBaseContext(), "SMS delivered", Toast.LENGTH_SHORT).show();
							break;
						case Activity.RESULT_CANCELED:
							Toast.makeText(getBaseContext(), "SMS not delivered", Toast.LENGTH_SHORT).show();
							break;
					}
				}
			}, new IntentFilter(SENT));
			
			SmsManager sms = SmsManager.getDefault();
			//sms.sendTextMessage(phoneNumber, null, message, null, null); // old one -> when used with nob arguments for the pending intents
			sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
		}
		
		
		
	} /** END OF THE SMSreceiver Class */

}
