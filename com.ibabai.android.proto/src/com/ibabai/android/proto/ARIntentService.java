package com.ibabai.android.proto;

import java.util.ArrayList;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.Geofence;

public class ARIntentService extends IntentService {
	private SharedPreferences shared_prefs;	
	private static final long GEOFENCE_EXPIRATION_TIME  = Geofence.NEVER_EXPIRE;  
	private static final float RADIUS = 100;
	private int previous_type;
	private SimpleGeofence sgf;
	private GeofenceRequester gfr;
	private GeofenceRemover gf_remover;
	ArrayList<Geofence> gf_list;
	DatabaseHelper dbh;		
	
	public ARIntentService() {
		super("ARIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		shared_prefs = getApplicationContext().getSharedPreferences(IbabaiUtils.PREFERENCES, Context.MODE_PRIVATE);
		gfr = new GeofenceRequester(this);
		if (ActivityRecognitionResult.hasResult(intent)) {
			ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
			DetectedActivity most_probable_activity = result.getMostProbableActivity();
			int confidence = most_probable_activity.getConfidence();
			int activity_type = most_probable_activity.getType();
			Editor editor = shared_prefs.edit();
			int store = shared_prefs.getInt(IbabaiUtils.STORE_ID, 0);
			if (!shared_prefs.contains(GeofenceUtils.KEY_PREVIOUS_ACTIVITY_TYPE)) {
				if (!isInCarOrStill(activity_type)) {
					addGeofences();
				}
				editor.putInt(GeofenceUtils.KEY_PREVIOUS_ACTIVITY_TYPE, activity_type);
				editor.apply();
			}
			else {
				previous_type = shared_prefs.getInt(GeofenceUtils.KEY_PREVIOUS_ACTIVITY_TYPE, DetectedActivity.UNKNOWN);
				
				if (!isInCarOrStill(activity_type) && isInCarOrStill(previous_type) && (confidence >= 50)) {
					addGeofences();
					editor.putInt(GeofenceUtils.KEY_PREVIOUS_ACTIVITY_TYPE, activity_type);
					editor.apply();
				}
				else if (isInCarOrStill(activity_type) && !isInCarOrStill(previous_type) && (confidence >= 50) && (store == 0)) {
					removeGeofences();
					editor.putInt(GeofenceUtils.KEY_PREVIOUS_ACTIVITY_TYPE, activity_type);					
					editor.apply();
				}				
			}
		}
	}
	
	private boolean isInCarOrStill(int type) {
		if (type == DetectedActivity.IN_VEHICLE || type == DetectedActivity.STILL) {
			return true;
		}
		else {
			return false;
		}
	}
	public void addGeofences() {		
		int c_id = shared_prefs.getInt(IbabaiUtils.CITY, 0);
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
	private Cursor StoresCursor() {		
		String s_query = String.format("SELECT * FROM %s", DatabaseHelper.TABLE_S);
		return (dbh.getReadableDatabase().rawQuery(s_query, null));
	}
	public void removeGeofences() {
		try {
			gf_remover = new GeofenceRemover(this);		
			gf_remover.removeGeofencesByIntent(gfr.getRequestPendingIntent());
		}
		catch (UnsupportedOperationException e) {
			Log.e(GeofenceUtils.APPTAG, getString(R.string.remove_geofences_already_requested_error));
		}
	}
}
