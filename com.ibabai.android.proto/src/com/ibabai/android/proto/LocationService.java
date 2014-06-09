package com.ibabai.android.proto;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
	private static final long POINT_RADIUS = 100;
	private static final long PROX_ALERT_EXPIRATION = -1;	
	private static final String PROX_ALERT_INTENT = "com.ibabai.android.proto.ProximityAlert";	
	private double target_lat = 50.4367479; 
	private double target_lon = 30.4673234;
	@Override
	public void onCreate() {
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, new myLocationListener());
		addProximityAlert(target_lat, target_lon);
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
	public void addProximityAlert(double latitude, double longitude) {
		Intent intent = new Intent(PROX_ALERT_INTENT);
		PendingIntent proxIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
		locationManager.addProximityAlert(latitude, longitude, POINT_RADIUS, PROX_ALERT_EXPIRATION, proxIntent);
		IntentFilter filter = new IntentFilter(PROX_ALERT_INTENT);
		registerReceiver(new ProximityIntentReceiver(), filter);
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

}
