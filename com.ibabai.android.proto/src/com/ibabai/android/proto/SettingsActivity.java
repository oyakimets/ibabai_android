package com.ibabai.android.proto;

import java.io.File;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
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
	SharedPreferences shared_prefs;
	DatabaseHelper dbh;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        
        ActionBar ab=getActionBar();
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setCustomView(R.layout.ab_settings);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayShowTitleEnabled(false); 
        
        File i_folder=new File(getConDir(this), "0");       
        File[] f_lst = i_folder.listFiles();
        String path = f_lst[5].getAbsolutePath();
        File f = new File(path);
        TextView tv = (TextView)findViewById(R.id.cl_name);
        if (f.exists()) {        	
        	tv.setText(path);
        }
        else {
        	tv.setText("Fuck");
        }
        
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
	public void ClearPrefs(View view) {
		shared_prefs = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
		Editor editor = shared_prefs.edit();
		editor.clear();
		editor.apply();
		Toast t = Toast.makeText(this, "Done", Toast.LENGTH_SHORT );
		t.show();
	}
	public void ClearDB(View view) {
		dbh = DatabaseHelper.getInstance(getApplicationContext());
		SQLiteDatabase sqldb = dbh.getWritableDatabase();
		if (sqldb != null) {
			sqldb.delete(TABLE, null, null);
			Toast t = Toast.makeText(this, "Done", Toast.LENGTH_SHORT );
			t.show();
		}
		else {
			Toast t = Toast.makeText(this, "Error", Toast.LENGTH_SHORT );
			t.show();
		}
	}
	public void updateData(View view) {
		Intent i = new Intent(this, DataUpdateService.class);
		startService(i);
		
	}
	static File getConDir(Context ctxt) {
		 return(new File(ctxt.getFilesDir(), ConUploadService.CON_BASEDIR));
	 }
}
