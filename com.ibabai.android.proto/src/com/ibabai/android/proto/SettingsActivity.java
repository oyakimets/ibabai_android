package com.ibabai.android.proto;

import java.io.File;

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

import com.commonsware.cwac.wakeful.WakefulIntentService;

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
        TextView tv2 = (TextView)findViewById(R.id.tv_2);
        tv2.setText(s_id);       
        
		String last_s = Integer.toString(shared_prefs.getInt("last_store", 0));
	    TextView tv3 = (TextView)findViewById(R.id.tv_3);
	    tv3.setText(last_s);
	     
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
	public void LoadData(View view) {
		Intent i = new Intent(this, psUploadService.class);
		startService(i);
		Toast t = Toast.makeText(this, "Done", Toast.LENGTH_SHORT );
		t.show();		
	}
	public void updateData(View view) {
		WakefulIntentService.sendWakefulWork(this, DataUpdateService.class);
		
	}
	static File getConDir(Context ctxt) {
		 return(new File(ctxt.getFilesDir(), ConUpdateService.CON_BASEDIR));
	 }
	static File getStopDir(Context ctxt) {
		 return(new File(ctxt.getFilesDir(), stopListActivity.SL_BASEDIR));
	 }		
}
