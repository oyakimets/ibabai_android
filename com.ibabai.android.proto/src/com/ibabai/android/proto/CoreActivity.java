package com.ibabai.android.proto;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class CoreActivity extends FragmentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_core);
        
        ActionBar ab = getActionBar();
        ab.setDisplayShowHomeEnabled(false);
        ab.setCustomView(R.layout.ab_scan);
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
	}	
	
}
