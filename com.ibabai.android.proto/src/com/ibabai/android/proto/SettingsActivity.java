package com.ibabai.android.proto;

import java.io.File;

import com.google.android.gms.location.DetectedActivity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {
	public static final String PREFERENCES = "MyPrefs";
	public static final String status = "SignedUp";
	static final String TABLE="logbook";
	public static final String city="city";		
	GPSTracker gps_t;
	SharedPreferences shared_prefs;
	DatabaseHelper dbh;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        dbh = DatabaseHelper.getInstance(getApplicationContext());
        ActionBar ab=getActionBar();
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setCustomView(R.layout.ab_settings);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayShowTitleEnabled(false); 
        
        gps_t = new GPSTracker(this);
        String lat = Double.toString(gps_t.getLatitude());
        String lon = Double.toString(gps_t.getLongitude());
        TextView tv1 = (TextView)findViewById(R.id.tv_1);
        tv1.setText( lat+"/"+lon);
        
     
        
        shared_prefs= getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        
        
       
	}
	@Override
	protected void onResume() {
		
		String s_id = Integer.toString(shared_prefs.getInt("store_id", 0));
		int activity_code = shared_prefs.getInt(GeofenceUtils.KEY_PREVIOUS_ACTIVITY_TYPE, DetectedActivity.UNKNOWN);
        TextView tv2 = (TextView)findViewById(R.id.tv_2);
        tv2.setText(getNameFromType(activity_code));       
        
		String last_s = Integer.toString(shared_prefs.getInt("last_store", 0));
		String gf = shared_prefs.getString("geofence", "????");
	    TextView tv3 = (TextView)findViewById(R.id.tv_3);
	    tv3.setText(gf);
	     
	     super.onResume();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.core, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.action_scan:
			Intent i=new Intent(this, ScanActivity.class);
			startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);
			
		}		
		
	}
	public void RegisterDelivery(View view) {
		Intent i = new Intent(this, DelRegService.class);
		startService(i);
		Toast t = Toast.makeText(this, "Done", Toast.LENGTH_SHORT );
		t.show();
	}	
	
	static File getConDir(Context ctxt) {
		 return(new File(ctxt.getFilesDir(), ConUpdateService.CON_BASEDIR));
	 }
	static File getStopDir(Context ctxt) {
		 return(new File(ctxt.getFilesDir(), stopListActivity.SL_BASEDIR));
	 }
	private String getNameFromType(int activity) {
		switch (activity) {
		 case DetectedActivity.IN_VEHICLE :
			 return "CAR";
		 case DetectedActivity.ON_BICYCLE :
			 return "BIKE";
		 case DetectedActivity.ON_FOOT :
			 return "ON FOOT";
		 case DetectedActivity.STILL :
			 return "STILL";
		 case DetectedActivity.UNKNOWN :
			 return "UNKNOWN";
		 case DetectedActivity.TILTING :
			 return "TILTING";
		}
		return "UNKNOWN";
	}
}
