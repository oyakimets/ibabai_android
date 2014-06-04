package com.ibabai.android.proto;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class PresentationDisplayActivity extends FragmentActivity {
	
	public static final String EXTRA_POSITION="position";
	SharedPreferences shared_prefs;
	public static final String PREFERENCES = "MyPrefs";
	public static final String balance = "Balance";
	public static final String MODEL="promo_model";
	private ViewPager pres_pager=null;
	private PromoPresAdapter adapter=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if(getSupportFragmentManager().findFragmentByTag(MODEL) == null) {
        	getSupportFragmentManager().beginTransaction().add(new PromoModelFragment(), MODEL).commit();
        	
        }
        
        setContentView(R.layout.presentation_pager); 
        pres_pager = (ViewPager)findViewById(R.id.presentation_pager);
        
        ActionBar ab=getActionBar();
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setCustomView(R.layout.ab_balance);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayShowTitleEnabled(false);
        
        shared_prefs=getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        String b = shared_prefs.getString(balance, "0");
        TextView tv_balance = (TextView) findViewById(R.id.balance);
        tv_balance.setText(b + " bais"); 
                
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
	void setupPager(PromoPresentation presentation) {		
		int position=getIntent().getIntExtra(EXTRA_POSITION, -1);
		String promo_folder= getPromoDir(position); 
		adapter = new PromoPresAdapter(this, presentation, promo_folder);
		pres_pager.setAdapter(adapter);
	}
	public static String getPromoDir(int position) {			   
	    return CoreActivity.allDirs.get(position);
	}
	public void showPromoRules(View v) {
		int position=getIntent().getIntExtra(EXTRA_POSITION, -1);
		String promo_folder= getPromoDir(position); 
		Intent promo_rules_intent=new Intent(this, PromoRulesActivity.class);
		promo_rules_intent.putExtra(PromoRulesActivity.EXTRA_DIR, promo_folder);
		startActivity(promo_rules_intent); 
	}
	public void notInterested(View v) {
		int position=getIntent().getIntExtra(EXTRA_POSITION, -1);
		Bundle bundle = new Bundle();
    	bundle.putInt("position", position);
    	NiDialogFragment nidf = new NiDialogFragment();
    	nidf.setArguments(bundle);
    	nidf.show(getSupportFragmentManager(), "ni");
	}
	public void sendToStoplist(View v) {
		int position=getIntent().getIntExtra(EXTRA_POSITION, -1);
		Bundle bundle = new Bundle();
    	bundle.putInt("position", position);
    	StopDialogFragment nidf = new StopDialogFragment();
    	nidf.setArguments(bundle);
    	nidf.show(getSupportFragmentManager(), "sl");		
	}
	
}
