package com.ibabai.android.proto;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class LocationService extends Service {
	public static final String PREFERENCES = "MyPrefs";	
	public static final String store_id = "store_id";
	private LocationManager locationManager;
	private static final long MIN_TIME = 1000*60*30;
	private static final long MIN_DISTANCE = 5;
	private static final long POINT_RADIUS = 500;
	private static final long PROX_ALERT_EXPIRATION = -1;	
	private static final String PROX_ALERT_INTENT = "com.ibabai.android.proto.ProximityAlert";	
	private Cursor s_cursor;
	private BroadcastReceiver b_rec;
	DatabaseHelper dbh;
	private int n = 0;
	@Override
	public void onCreate() {
		dbh=DatabaseHelper.getInstance(getApplicationContext());
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, new myLocationListener());
		s_cursor=StoresCursor();
		b_rec = new ProximityIntentReceiver();
		if (s_cursor !=null && s_cursor.moveToFirst()) {
			while (s_cursor.isAfterLast()!=true) {
				int id_ind = s_cursor.getColumnIndex("store_id");
				int lat_ind = s_cursor.getColumnIndex("latitude");
				int lon_ind = s_cursor.getColumnIndex("longitude");
				int store_id = s_cursor.getInt(id_ind);
				double target_lat = s_cursor.getDouble(lat_ind);
				double target_lon = s_cursor.getDouble(lon_ind);				
				addProximityAlert(target_lat, target_lon, store_id, n);
				n++;
				s_cursor.moveToNext();
			}
			s_cursor.close();
			dbh.close();
		}
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {		
		Log.i("LocalService", "Receive start id "+startId+": "+intent);
		return(START_STICKY);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	public void addProximityAlert(double latitude, double longitude, int s_id, int ind) {
		String proxy = PROX_ALERT_INTENT+ind;
		IntentFilter filter = new IntentFilter(proxy);
		registerReceiver(b_rec, filter);
		Intent intent = new Intent(proxy);
		intent.putExtra("store_id", s_id);
		PendingIntent proxIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
		locationManager.addProximityAlert(latitude, longitude, POINT_RADIUS, PROX_ALERT_EXPIRATION, proxIntent);
		
	}
	public class myLocationListener implements LocationListener {
		public void onLocationChanged(Location location) {			
		}
		public void onStatusChanged(String s, int i, Bundle b) {
			
		}
		public void onProviderDisabled(String s) {
			
		}
		public void onProviderEnabled(String s) {
			
		}
	}
	private Cursor StoresCursor() {		
		String s_query = String.format("SELECT * FROM %s", DatabaseHelper.TABLE_S);
		return (dbh.getWritableDatabase().rawQuery(s_query, null));
	}
	@Override
	public void onDestroy() {
		try {
			unregisterReceiver(b_rec);
		}
		catch(IllegalArgumentException e) {
			Log.d("receiver", e.toString());
		}
		super.onDestroy();
	}
}
