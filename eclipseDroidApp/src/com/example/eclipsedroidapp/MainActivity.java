package com.example.eclipsedroidapp;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	/** necessary constants */
	public final static String EXTRA_MESSAGE = "com.example.eclipsedroidapp.MESSAGE";
	
	/** test constants for futur SMS parsing */
	public final static String ADMIN_AUTH_CODE = "@z34a@"; // the Admin Auth code
	public final static String USER_AUTH_CODE = "@b32y@"; // the User Auth code
	public final static String ADMIN_AUTH_TYPE = "ADMIN_AUTH"; // the Admin Auth type
	public final static String USER_AUTH_TYPE = "USER_AUTH"; // the User Auth type
	
	/** reference to the shared preferences of our app */
	SharedPreferences app_preferences;
	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        /** init the app's preferences */
        app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    /** Mthds added manually below */
    /** Called when the user clicks the Send button */
	public void sendMessage(View view) {
    	// Do something in response to a click on the Send button
    	Intent intent = new Intent(this, DisplayMessageActivity.class);
    	EditText editText = (EditText) findViewById(R.id.edit_message);
    	
    	String message = editText.getText().toString();
    	String ok_message = "SMS message sent succefully";
    	
    	/** check if the message is not null ( if we got some text to send actually ) */
    	if ( message.equals("") ){
    		return; // return to prevent a fatal exception
    	}
    	
    	/** do a little auth check */
    	String messageAuthType = getAuthTypeFromMessage(message);
    	String currentAuth = checkAuth(messageAuthType);
    	
    	/** act according to the current auth type */
    	if (currentAuth.equals(ADMIN_AUTH_TYPE)){
    		Log.d("ADMIN AUTH SESSION BEGAN", " > parsing message for new data ..");
    		// extract the data from the message
    		String theNewData = getDataFromMessage(message);
    		Log.d("New data from Admin auth session:", theNewData);
    		// save it in the appropriate location
    		setStuffToPrefs("theDataPrefKey", theNewData);
    		// send back a message with the data freshly set up --> for the moment ( as wip ) just set the text of the next activity accordingly (..)
    		String theFreshData = getStuffFromPrefs("theDataPrefKey");
    		String adminCongratulations = "Congratulations! secure data set to: " + theFreshData;
    		
    		/** start another activity */
        	intent.putExtra(EXTRA_MESSAGE, adminCongratulations);
        	startActivity(intent);
    		
    	} else if (currentAuth.equals(USER_AUTH_TYPE)){
    		Log.d("USER AUTH SESSION BEGAN", " > parsing message for new data ..");
    		// get the last known data and sent it back to where the first message originated
    		String theFreshData = getStuffFromPrefs("theDataPrefKey");
    		String userCongratulations = "Got you back ;p > " + theFreshData;
    		
    		/** start another activity */
        	intent.putExtra(EXTRA_MESSAGE, userCongratulations);
        	startActivity(intent);
    		
    	}
    	
    	/** send a SMS*/
    	sendSMS("+33681382722", message);
    	
    	///** start another activity */
    	//intent.putExtra(EXTRA_MESSAGE, ok_message);
    	//startActivity(intent);
	}
	
	/** Called when the user clicks the send button, this time sending an sms message*/
	//private void sendSMS(String phoneNumber, String message){
	//	SmsManager sms = SmsManager.getDefault();
	//	sms.sendTextMessage(phoneNumber, null, message, null, null);
	//}
	
	private void sendSMS(String phoneNumber, String message){
		String SENT = "SMS_SENT";
		String DELIVERED = "SMS_DELIVERED";
		
		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
		PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);
		
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
	
}
