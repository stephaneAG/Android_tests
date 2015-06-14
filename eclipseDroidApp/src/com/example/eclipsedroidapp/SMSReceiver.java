package com.example.eclipsedroidapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSReceiver extends BroadcastReceiver {
	
	/** seems to be necessary to handle SMS message reception */
	private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	private static final String TAG = "SMSBroadcastReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		
		/** a quick log */
		Log.i(TAG, "Intent received: " + intent.getAction());
		
		/** get the SMS message passed in */
		if (intent.getAction().equals(SMS_RECEIVED)){
			
			/** test/hack -> abort further broadcasting to other apps (..) */
			this.abortBroadcast();
			
			Bundle bundle = intent.getExtras();
			//SmsMessage[] messages = null;
			//String str = "";
			if (bundle != null){
				
				/** retrieve the SMS message received */
				Object[] pdus = (Object[]) bundle.get("pdus");
				final SmsMessage[] messages = new SmsMessage[pdus.length];
				for (int i=0; i<messages.length; i++){
					messages[i] = SmsMessage.createFromPdu( (byte[])pdus[i] );
					//str += "SMS from " + messages[i].getOriginatingAddress(); // I guess the number that sent the first SMS message
					//str += " :";
					//str += messages[i].getMessageBody().toString(); // to string -> check if really necesary & why (..)
					//str += "\n";
				}
				
				/** display the new SMS message */
				//Toast.makeText(context, str, Toast.LENGTH_SHORT).show(); // It seems for unknow yet reason I can't see it ...
				//Log.d("Message received: ", str);
				if (messages.length > -1){
					Log.i(TAG, "The message received: " + messages[0].getMessageBody());
				}
			}
			
		}
		
	}
	
}
