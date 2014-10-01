package com.ibabai.android.proto;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class LogActivity extends FragmentActivity {
	SharedPreferences shared_prefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log); 
        
        ActionBar ab=getActionBar();
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setCustomView(R.layout.ab_log);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayShowTitleEnabled(false);
        
        shared_prefs=getSharedPreferences(IbabaiUtils.PREFERENCES, Context.MODE_PRIVATE);
        String b = shared_prefs.getString(IbabaiUtils.BALANCE, "0");
        TextView tv_balance = (TextView) findViewById(R.id.lb_balance);
        tv_balance.setText(b + " bais");
        
        LogbookFragment f = new LogbookFragment();  
        if (getSupportFragmentManager().findFragmentById(R.id.log_fragment) == null) {
        	getSupportFragmentManager().beginTransaction().add(R.id.log_fragment, f).commit();
        	
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
}
