package com.example.tefservicewidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.tefservicewidget.R;

public class TefServiceWidget extends AppWidgetProvider {
	
	/** our widget buttons actions ( registered in the AndroidManifest.xml ) */
	public static String ACTION_WIDGET_START_SERVICE = "ActionReceiverStartService";
	public static String ACTION_WIDGET_STOP_SERVICE = "ActionReceiverStopService";
	public static String ACTION_WIDGET_START_SERVICE_ON_BOOT = "ActionReceiverStartServiceOnBoot";
	// continuing the tests
	public static final String SYNC_CLICKED = "automaticWidgetSyncButtonClick";
	
	/** Old Fcn used (..) */
	/*
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){
		
		RemoteViews remoteViews;
		ComponentName tefServiceWidget;
		
		remoteViews = new RemoteViews(context.getPackageName(), R.layout.main);
		tefServiceWidget = new ComponentName(context, TefServiceWidget.class);
		//remoteViews.setTextViewText(R.id.widget_textview, "Hello World!");
		
		appWidgetManager.updateAppWidget(tefServiceWidget, remoteViews);
		
	}
	*/
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){
		
		Log.i("WIDGET DEBUG WIP", "onUpdate got triggered !!!");
		
		RemoteViews remoteViews;
		ComponentName tefServiceWidget;
		
		remoteViews = new RemoteViews(context.getPackageName(), R.layout.main);
		tefServiceWidget = new ComponentName(context, TefServiceWidget.class);
		//remoteViews.setTextViewText(R.id.widget_textview, "Hello World!");
		
		remoteViews.setOnClickPendingIntent(R.id.sync_button,getPendingSelfIntent(context, SYNC_CLICKED) );
		
		/*
		// setting up the Intents necessary to handle the widget's buttons click events
		// the Start Service button
		//Intent active = new Intent(context, TefServiceWidget.class);
		Intent active = new Intent(context, TefServiceWidget.class);
		active.setAction(ACTION_WIDGET_START_SERVICE);
		PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, active, 0);
		remoteViews.setOnClickPendingIntent(R.id.widget_Button1, actionPendingIntent);
		
		// the Stop Service button
		active = new Intent(context, TefServiceWidget.class);
		active.setAction(ACTION_WIDGET_STOP_SERVICE);
		actionPendingIntent = PendingIntent.getBroadcast(context, 0, active, 0);
		remoteViews.setOnClickPendingIntent(R.id.widget_Button2, actionPendingIntent);
		
		// the Start Service On Boot button ( -> used as a toggle )
		active = new Intent(context, TefServiceWidget.class);
		active.setAction(ACTION_WIDGET_START_SERVICE_ON_BOOT);
		actionPendingIntent = PendingIntent.getBroadcast(context, 0, active, 0);
		remoteViews.setOnClickPendingIntent(R.id.widget_Button4, actionPendingIntent);
		*/
		
		appWidgetManager.updateAppWidget(tefServiceWidget, remoteViews); // don 't know if also [still] works ->to test (..)
		//appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
		
	}
	
	/** Overriden implm of the onReceive Fcn to handle the widget buttons action intents  */
	@Override
	public void onReceive(Context context, Intent intent){
		super.onReceive(context, intent);
		
		// act accordingly depending on the clicked button
		/*
		if (intent.getAction().equalsIgnoreCase(ACTION_WIDGET_START_SERVICE)){
			// start the service if not already running
			Toast.makeText(context, "Start service widget button clicked", Toast.LENGTH_LONG).show();
			Log.i(ACTION_WIDGET_START_SERVICE, "triggered !!!");
		} else if (intent.getAction().equalsIgnoreCase(ACTION_WIDGET_STOP_SERVICE)){
			// stop the service if currently running
			Toast.makeText(context, "Stop service widget button clicked", Toast.LENGTH_LONG).show();
		} else if (intent.getAction().equalsIgnoreCase(ACTION_WIDGET_START_SERVICE_ON_BOOT)){
			// toggle the service auto-start on device boot
			Toast.makeText(context, "Toggle autostart service on boot widget button clicked", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(context, "WTF??!!", Toast.LENGTH_SHORT).show();
			super.onReceive(context, intent);
		}
		*/
		
		if (SYNC_CLICKED.equals(intent.getAction())){
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			
			RemoteViews remoteViews;
			ComponentName tefServiceWidget;
			
			remoteViews = new RemoteViews(context.getPackageName(), R.layout.main);
			tefServiceWidget = new ComponentName(context, TefServiceWidget.class);
			
			// stuff to do -> ex: remoteView.setTextViewText(R.id.sync_button, "TESTING");
			
			Log.i("TEST", "TEST IS WORKING YUP !!!");
			
			appWidgetManager.updateAppWidget(tefServiceWidget, remoteViews);
		}
		
	}
	
	/** debug Fcns */
	/*
	public void executeHello(){
		Log.i("See?", "I may work somehow finally ...");
	}
	 */
	
	protected PendingIntent getPendingSelfIntent(Context context, String action){
		Intent intent = new Intent(context, getClass() );
		intent.setAction(action);
		
		return PendingIntent.getBroadcast(context, 0, intent, 0);
		
	}
	
	
}
