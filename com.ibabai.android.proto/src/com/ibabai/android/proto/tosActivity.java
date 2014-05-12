package com.ibabai.android.proto;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;

public class tosActivity extends ActionBarActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (getSupportFragmentManager().findFragmentById(R.id.tos_content)==null) {
			Fragment f=TosContentFragment.newInstance("file:///android_asset/docs/tos.html");
			getSupportFragmentManager().beginTransaction().add(R.id.tos_content, f).commit();
		}		
        
		setContentView(R.layout.tos);
		
		ActionBar ab = getActionBar();
        ab.setDisplayShowHomeEnabled(false);
        ab.setCustomView(R.layout.ab_tos);
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);        
        
	}
	
	public void signUp(View view) {
    	Intent i = new Intent(this, SignupActivity.class);
    	startActivity(i);
    }
	
	public void appExit(View view) {
		Intent i = new Intent(this, MainActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.putExtra("EXIT", true);
		startActivity(i);
		finish();
	}
}
