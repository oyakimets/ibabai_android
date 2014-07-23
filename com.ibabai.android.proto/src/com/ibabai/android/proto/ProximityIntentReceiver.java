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
import android.widget.Toast;

public class ProximityIntentReceiver extends BroadcastReceiver {
	public static final String PREFERENCES = "MyPrefs";	
	public static final String store_id = "store_id";
	public static final String LAST_STORE = "last_store";
	SharedPreferences shared_prefs;	
	private static final int NOTIFY_ID = 1000;	

	@Override
	public void onReceive(Context ctxt, Intent intent) {
		
		String key = LocationManager.KEY_PROXIMITY_ENTERING;
		int st_id = (Integer)intent.getExtras().get("st_id");
		Boolean entering = intent.getBooleanExtra(key, false);
		shared_prefs = ctxt.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
		Editor editor = shared_prefs.edit();
		if (entering) {			
			editor.putInt(store_id, st_id);
			editor.putInt(LAST_STORE, st_id);
			editor.apply();
			Toast.makeText(ctxt, "Entering: "+Integer.toString(st_id), Toast.LENGTH_LONG).show();
			Log.d(getClass().getSimpleName(), "entering");			
			raiseNotification(ctxt, null);	
			Intent i = new Intent(ctxt, DelRegService.class);
			ctxt.startService(i);
		}
		else {
				if (st_id == shared_prefs.getInt(LAST_STORE, 0)) {
					Toast.makeText(ctxt, "Exiting: "+Integer.toString(st_id), Toast.LENGTH_LONG).show();
					editor.putInt(store_id, 0);
					editor.apply();			
					Log.d(getClass().getSimpleName(), "exiting");
			}
		}
		
	}
	private void raiseNotification(Context ctxt, Exception e) {
		NotificationCompat.Builder b=new NotificationCompat.Builder(ctxt);

		b.setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL).setWhen(System.currentTimeMillis());
		Bitmap bm = BitmapFactory.decodeResource(ctxt.getResources(), R.drawable.ic_launcher);
		if (e == null) {
			b.setContentTitle("Hello!").setContentText("You have offers from IBABAI!").setSmallIcon(android.R.drawable.ic_menu_info_details).setTicker("ibabai").setLargeIcon(bm);

			Intent outbound=new Intent(ctxt, CoreActivity.class);			

			b.setContentIntent(PendingIntent.getActivity(ctxt, 0, outbound, Intent.FLAG_ACTIVITY_NEW_TASK));
		}
		else {
			b.setContentTitle("Sorry").setContentText(e.getMessage()).setSmallIcon(android.R.drawable.stat_notify_error).setTicker("ibabai");
		}

		NotificationManager mgr=(NotificationManager)ctxt.getSystemService(Context.NOTIFICATION_SERVICE);

		mgr.notify(NOTIFY_ID, b.build());
	}	
}
