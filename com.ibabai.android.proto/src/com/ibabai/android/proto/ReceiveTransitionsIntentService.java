package com.ibabai.android.proto;

import java.util.ArrayList;
import java.util.List;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

public class ReceiveTransitionsIntentService extends IntentService {
	private SharedPreferences shared_prefs;
	public static final String PREFERENCES = "MyPrefs";	
	public static final String store_id = "store_id";
	public static final String LAST_STORE = "last_store";
	public static ArrayList<String> userPromos;
	public static ArrayList<String> storePromos;
	private static final int NOTIFY_ID = 1000;
	private int offers = 0;
	DatabaseHelper dbh;
	
	public ReceiveTransitionsIntentService() {
		super("ReceiveTransitionsIntentService");
	}
	@Override
	protected void onHandleIntent(Intent intent) {
		
		Intent broadcastIntent = new Intent();
		
		broadcastIntent.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);
		
		if(LocationClient.hasError(intent)) {
			int errorCode = LocationClient.getErrorCode(intent);
			String errorMessage = LocationServiceErrorMessages.getErrorString(this, errorCode);
			
			Log.e(GeofenceUtils.APPTAG, getString(R.string.geofence_transition_error_detail, errorMessage));
			
			broadcastIntent.setAction(GeofenceUtils.ACTION_GEOFENCE_ERROR).putExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS, errorMessage).putExtra(GeofenceUtils.EXTRA_GEOFENCE_TYPE, "TRANSITION");
			
			LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
		}
		else {
			int transition = LocationClient.getGeofenceTransition(intent);
			
			if ((transition == Geofence.GEOFENCE_TRANSITION_ENTER) || (transition == Geofence.GEOFENCE_TRANSITION_EXIT)) {
				List<Geofence> geofences = LocationClient.getTriggeringGeofences(intent);
				String geofenceId = geofences.get(0).getRequestId();
				int s_id = Integer.parseInt(geofenceId);
				String transitionType = getTransitionString(transition);
				dbh = DatabaseHelper.getInstance(getApplicationContext());
				shared_prefs = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
				Editor editor = shared_prefs.edit();
				if (transitionType.equals(getString(R.string.geofence_transition_entered)) && hasPromos(s_id)) {
					editor.putInt(store_id, s_id);
					editor.putInt(LAST_STORE, s_id);
					editor.apply();					
					Log.d(getClass().getSimpleName(), "entering");			
					raiseNotification(this, null);	
					Intent i = new Intent(this, DelRegService.class);
					startService(i);
					
				}
				else if (transitionType.equals(getString(R.string.geofence_transition_exited))) {
					if (s_id == shared_prefs.getInt(LAST_STORE, 0)) {						
						editor.putInt(store_id, 0);
						editor.apply();			
						Log.d(getClass().getSimpleName(), "exiting");
					}
				}
				else {
					Log.d(getClass().getSimpleName(), "unknown");
				}
			}
			else {
				Log.e(GeofenceUtils.APPTAG, getString(R.string.geofence_transition_invalid_type, transition));
			}
		}
	}
	private String getTransitionString(int transitionType) {
        switch (transitionType) {

            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);

            default:
                return getString(R.string.geofence_transition_unknown);
        }
    }
	private boolean hasPromos(int store_id) {
		userPromos = new ArrayList<String>();
		storePromos=new ArrayList<String>();
		Cursor pa_cursor=promoactCursor();
		if (pa_cursor != null && pa_cursor.moveToFirst()) {
			int id_ind = pa_cursor.getColumnIndex("promoact_id");
			while (!pa_cursor.isAfterLast()) {
				String pa_id = Integer.toString(pa_cursor.getInt(id_ind));						
				userPromos.add(pa_id);
				pa_cursor.moveToNext();
			}
			pa_cursor.close();
		}
			
		Cursor ps_cursor = storePromosCursor(store_id);
		if(ps_cursor != null && ps_cursor.moveToFirst()) {
			int paid_ind = ps_cursor.getColumnIndex("promoact_id");
			while (!ps_cursor.isAfterLast()) {
				String promoact_id=Integer.toString(ps_cursor.getInt(paid_ind));
				storePromos.add(promoact_id);				
				ps_cursor.moveToNext();
			}
			ps_cursor.close();
		}
		if (userPromos != null && storePromos != null) {
			for (int i=0; i<storePromos.size(); i++) {
				if (userPromos.contains(storePromos.get(i))) {
					offers++;						
				}				
			}
		}
		if (offers > 0) {
			return true;
		}
		else {
			return false;
		}
			
	}
	private Cursor promoactCursor() {
		 String p_query = String.format("SELECT * FROM %s WHERE stopped=0", DatabaseHelper.TABLE_P);
		 return(dbh.getReadableDatabase().rawQuery(p_query, null));
	 }
	private Cursor storePromosCursor(int store_id) {		
		String ps_query= "SELECT * FROM promo_stores WHERE store_id="+Integer.toString(store_id);
		return (dbh.getReadableDatabase().rawQuery(ps_query, null));
	}
	private void raiseNotification(Context ctxt, Exception e) {
		NotificationCompat.Builder b=new NotificationCompat.Builder(ctxt);

		b.setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL).setWhen(System.currentTimeMillis());
		Bitmap bm = BitmapFactory.decodeResource(ctxt.getResources(), R.drawable.ic_launcher);
		if (e == null) {
			b.setContentTitle("Hello!").setContentText("You have "+offers+" offers from IBABAI!").setSmallIcon(android.R.drawable.ic_menu_info_details).setTicker("ibabai").setLargeIcon(bm);

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
