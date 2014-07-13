package com.ibabai.android.proto;

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

public class ProfileActivity extends Activity {
	public static final String PREFERENCES = "MyPrefs";	
	public static final String username = "email";
	public static final String telephone = "phone";
	public static final String age = "age";
	public static final String gender="gender";	
	SharedPreferences shared_prefs;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        
        ActionBar ab=getActionBar();
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setCustomView(R.layout.ab_profile);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayShowTitleEnabled(false);
        
        shared_prefs = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        TextView tv_mail = (TextView)findViewById(R.id.pr_email);
        String st_mail = shared_prefs.getString(username, "");
        tv_mail.setText(st_mail);
        TextView tv_phone = (TextView)findViewById(R.id.pr_phone);
        String st_phone = shared_prefs.getString(telephone, "");
        tv_phone.setText(st_phone);
        TextView tv_age = (TextView)findViewById(R.id.pr_age);
        String in_age = shared_prefs.getString(age, "");
        tv_age.setText(in_age);
        TextView tv_gender = (TextView)findViewById(R.id.pr_gender);
        String st_gender = shared_prefs.getString(gender, "");
        tv_gender.setText(st_gender);
        
        
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
	public void ChangePassword(View view) {
		Intent changeInt = new Intent(this, PasswordActivity.class);
		startActivity(changeInt);
	}
	public void UpdateProfile(View view) {
		Intent updateInt = new Intent(this, ProfileUpdateActivity.class);
		startActivity(updateInt);
	}
}
