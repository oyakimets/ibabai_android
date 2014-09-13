package com.ibabai.android.proto;

import java.util.ArrayList;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;

public class gfService extends Service {
	
	public static final String PREFERENCES = "MyPrefs";
	public static final String city = "city";
	private static final long SECONDS_PER_HOUR = 3600;
	private static final long MILLISECONDS_PER_SECOND = 1000;
	private static final long GEOFENCE_EXPIRATION_IN_HOURS = 24;
	private static final long GEOFENCE_EXPIRATION_TIME  = SECONDS_PER_HOUR * MILLISECONDS_PER_SECOND * GEOFENCE_EXPIRATION_IN_HOURS;  
	private static final float RADIUS = 100;	
	private SimpleGeofence sgf;
	private GeofenceRequester gfr;
	private GeofenceRemover gf_remover;
	ArrayList<Geofence> gf_list;
	DatabaseHelper dbh;
	SharedPreferences shared_prefs;
	private GeofenceSampleReceiver broadcast_receiver;
	private IntentFilter intent_filter;
	private LocalBroadcastManager lbm;
	
	@Override
	public void onCreate() {
		super.onCreate();
		broadcast_receiver = new GeofenceSampleReceiver();
		intent_filter = new IntentFilter();
		intent_filter.addAction(GeofenceUtils.ACTION_GEOFENCES_ADDED);
		intent_filter.addAction(GeofenceUtils.ACTION_GEOFENCES_REMOVED);
		intent_filter.addAction(GeofenceUtils.ACTION_GEOFENCE_ERROR);
		intent_filter.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);
		lbm = LocalBroadcastManager.getInstance(this); 
		lbm.registerReceiver(broadcast_receiver, intent_filter);
		gfr = new GeofenceRequester(this);
		shared_prefs = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
		int c_id = shared_prefs.getInt(city, 0);
		if (c_id != 0) {
			dbh=DatabaseHelper.getInstance(getApplicationContext());
			gf_list = new ArrayList<Geofence>();
			Cursor s_cursor=StoresCursor();			
			if (s_cursor !=null && s_cursor.moveToFirst()) {
				while (s_cursor.isAfterLast()!=true) {
					int id_ind = s_cursor.getColumnIndex("store_id");
					int lat_ind = s_cursor.getColumnIndex("latitude");
					int lon_ind = s_cursor.getColumnIndex("longitude");
					int st_id = s_cursor.getInt(id_ind);
					double target_lat = s_cursor.getDouble(lat_ind);
					double target_lon = s_cursor.getDouble(lon_ind);				
					sgf = new SimpleGeofence(Integer.toString(st_id), target_lat, target_lon, RADIUS, GEOFENCE_EXPIRATION_TIME, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);
					gf_list.add(sgf.toGeoFence());
					s_cursor.moveToNext();
				}
				s_cursor.close();
				dbh.close();
			}
		}
		try {
			gfr.addGeofences(gf_list);
			
		}
		catch (UnsupportedOperationException e) {
			Log.e(GeofenceUtils.APPTAG, getString(R.string.add_geofences_already_requested_error));
		}
		
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {		
		Log.i("gfService", "Receive start id "+startId+": "+intent);
		return(START_STICKY);
	}
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public void onDestroy() {
		try {
			gf_remover = new GeofenceRemover(this);		
			gf_remover.removeGeofencesByIntent(gfr.getRequestPendingIntent());
		}
		catch (UnsupportedOperationException e) {
			Log.e(GeofenceUtils.APPTAG, getString(R.string.remove_geofences_already_requested_error));
		}
		lbm.unregisterReceiver(broadcast_receiver);
	}
	
	private Cursor StoresCursor() {		
		String s_query = String.format("SELECT * FROM %s", DatabaseHelper.TABLE_S);
		return (dbh.getReadableDatabase().rawQuery(s_query, null));
	}
	
	public class GeofenceSampleReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context ctxt, Intent i) {
			String action = i.getAction();
			Editor editor = shared_prefs.edit();
			if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_ADDED)) {
				Log.d(GeofenceUtils.APPTAG, "Geofences successfully added");
				editor.putString("geofence", "GF ADDED");
				editor.apply();
			}
			else if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_REMOVED)) {
				Log.d(GeofenceUtils.APPTAG, "Geofences successfully removed");
				stopSelf();
				
			}
			else if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_ERROR)) {
				String status = i.getStringExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS);
				String type = i.getStringExtra(GeofenceUtils.EXTRA_GEOFENCE_TYPE);
				if (TextUtils.equals(type, "ADD")) {
					Log.e(GeofenceUtils.APPTAG, status);
					Intent ls_intent = new Intent(ctxt, LocationService.class);
					ctxt.startService(ls_intent);					
					stopSelf();
				}
				else {
					Log.e(GeofenceUtils.APPTAG, status);
				}
				
			}
			else if (TextUtils.equals(action, GeofenceUtils.ACTION_CONNECTION_ERROR)) {
				String connection_error = i.getStringExtra(GeofenceUtils.EXTRA_CONNECTION_ERROR_CODE);
				Log.e(GeofenceUtils.APPTAG, connection_error);
			}
			else {
				Log.e(GeofenceUtils.APPTAG, ctxt.getString(R.string.invalid_action_detail, action));
			}
		}
	}
	
}


