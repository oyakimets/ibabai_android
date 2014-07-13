package com.ibabai.android.proto;


import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends FragmentActivity {
	private ViewPager pager=null;
	private PresentationAdapter adapter=null;
	public static final String PREFERENCES = "MyPrefs";
	public static final String auth_token = "AuthToken";	
	SharedPreferences shared_prefs;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        	setContentView(R.layout.activity_main);        
        	
                        
        	pager=(ViewPager)findViewById(R.id.pager);
        	adapter=new PresentationAdapter(getSupportFragmentManager());
        	pager.setAdapter(adapter);
        	findViewById(R.id.pager).setVisibility(View.VISIBLE);
        
        	ActionBar ab = getActionBar(); 
        	ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        	ab.setCustomView(R.layout.ab_intro);
        	ab.setDisplayShowHomeEnabled(true);
        	ab.setDisplayShowTitleEnabled(false);        	
        	
        	SoundEffects.initSounds(this);
        	
        	
    }
    @Override
    protected void onResume() {
    	shared_prefs = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    	if( getIntent().getBooleanExtra("EXIT", false)) {
    		finish();
    	}
    	if(shared_prefs.contains(auth_token)) {
    		Intent launchIntent = new Intent(this, CoreActivity.class);
    		startActivity(launchIntent);
    		finish();
    	}
    	super.onResume();
    }
    
    public void showTos(View view) {
    	Intent i = new Intent(this, tosActivity.class);
    	startActivity(i);
    }
    
    public void signUp(View view) {
    	new TosDialogFragment().show(getSupportFragmentManager(), "tos");    	
    	
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
       
        return super.onOptionsItemSelected(item);
    }        
}
