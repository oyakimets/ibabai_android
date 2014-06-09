package com.ibabai.android.proto;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class ProximityIntentReceiver extends BroadcastReceiver {
	public static final String PREFERENCES = "MyPrefs";	
	public static final String store_id = "store_id";
	SharedPreferences shared_prefs;
	private static final int NOTIFY_ID = 1000;

	@Override
	public void onReceive(Context ctxt, Intent intent) {
		
		String key = LocationManager.KEY_PROXIMITY_ENTERING;
		
		Boolean entering = intent.getBooleanExtra(key, false);
		shared_prefs = ctxt.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
		Editor editor = shared_prefs.edit();
		if (entering) {			
			editor.putString(store_id, "1");
			editor.apply();	
			Log.d(getClass().getSimpleName(), "entering");
			raiseNotification(ctxt, null);
		}
		else {
			editor.putString(store_id, "0");
			editor.apply();	
			Log.d(getClass().getSimpleName(), "exiting");
		}
		
	}
	private void raiseNotification(Context ctxt, Exception e) {
		NotificationCompat.Builder b=new NotificationCompat.Builder(ctxt);

		b.setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL).setWhen(System.currentTimeMillis());
		Bitmap bm = BitmapFactory.decodeResource(ctxt.getResources(), R.drawable.ic_launcher);
		if (e == null) {
			b.setContentTitle("Hello!").setContentText("You have offers from IBABAI!").setSmallIcon(android.R.drawable.ic_menu_info_details).setTicker("ibabai").setLargeIcon(bm);

			Intent outbound=new Intent(ctxt, CoreActivity.class);			

			b.setContentIntent(PendingIntent.getActivity(ctxt, 0, outbound, 0));
		}
		else {
			b.setContentTitle("Sorry").setContentText(e.getMessage()).setSmallIcon(android.R.drawable.stat_notify_error).setTicker("ibabai");
		}

		NotificationManager mgr=(NotificationManager)ctxt.getSystemService(Context.NOTIFICATION_SERVICE);

		mgr.notify(NOTIFY_ID, b.build());
	}
}
