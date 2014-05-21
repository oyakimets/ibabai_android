package com.ibabai.android.proto;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class PasswordChangeActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.password_change);
     
		ActionBar ab=getActionBar();
		ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		ab.setCustomView(R.layout.ab_profile);
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setHomeButtonEnabled(true);
		ab.setDisplayShowHomeEnabled(true);
		ab.setDisplayShowTitleEnabled(false);
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
		EditText et_pass = (EditText) findViewById(R.id.pass_change);
		String st_pass = et_pass.getText().toString();
		EditText et_pc = (EditText) findViewById(R.id.pass_change_confirm);
		String st_pc = et_pc.getText().toString();
		if (st_pass.length() >= 6 && st_pass.equals(st_pc)) {
			Toast t = Toast.makeText(this, "Password has been changed", Toast.LENGTH_LONG);
			t.show();
			Intent i = new Intent(this, CoreActivity.class);
			startActivity(i);		
		}
		else {
			if (st_pass.length()<6) {
				et_pass.setError("No less than 6 characters");
			}
			if (!st_pass.equals(st_pc)) {
				et_pc.setError("Entries do not match");
			}
		}
		
	}
}
