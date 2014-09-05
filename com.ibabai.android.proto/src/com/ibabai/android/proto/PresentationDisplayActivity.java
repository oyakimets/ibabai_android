package com.ibabai.android.proto;

import java.io.File;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class PresentationDisplayActivity extends FragmentActivity {
	private TextView tv_balance;
	public static final String PAYMENT_API_ENDPOINT_URL=SignupActivity.BASE_API_ENDPOINT_URL+"transactions.json";
	public static final String EXTRA_POSITION="position";
	public static final String FLAG="dc_flag";	
	public static final String AGENT_ID="agent_id";
	public static final String AGENT_NAME="agent_name";
	public static final String AMOUNT="amount";
	public static final String EXTRA_PA="pa_id";	
	SharedPreferences shared_prefs;
	public static final String PREFERENCES = "MyPrefs";
	public static final String balance = "Balance";
	public static final String MODEL="promo_model";
	private String pa_folder_path=null;
	private ViewPager pres_pager=null;
	private PromoPresAdapter adapter=null;
	private int view;	
	private String promoact_id;	
	private String bal_value;
	DatabaseHelper dbh;
	private Cursor pa_cursor = null;
	
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
        
        dbh = DatabaseHelper.getInstance(this);       
        
        promoact_id=getIntent().getStringExtra(EXTRA_PA);        
                
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
	@Override
	protected void onResume() {
		shared_prefs=getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
		bal_value = shared_prefs.getString(balance, "0");		
		if (CheckView()) {			
			Intent i = new Intent(this, ViewRewardService.class);
			i.putExtra(EXTRA_PA, promoact_id);
			i.putExtra(ScanActivity.EXTRA_CODE, "v");
			startService(i);
		}
		else {
			Toast.makeText(this, "Reward was granted already. Check your logbook.", Toast.LENGTH_LONG).show();
			
		}
		tv_balance = (TextView) findViewById(R.id.balance);
		tv_balance.setText("balance "+bal_value + " bais"); 
        super.onResume();		
	}
	void setupPager(PromoPresentation presentation) {		
		File pa_folder = new File(getConDir(this), promoact_id);
		if (pa_folder.exists()) {
			pa_folder_path = pa_folder.getAbsoluteFile()+"/";
		}	
		
		adapter = new PromoPresAdapter(this, presentation, pa_folder_path);
		pres_pager.setAdapter(adapter);
	}
	public static String getPromoDir(int position) {			   
	    return CoreActivity.allDirs.get(position);
	}
	public void showPromoRules(View v) {				
		Intent promo_rules_intent=new Intent(this, PromoRulesActivity.class);
		promo_rules_intent.putExtra(PromoRulesActivity.EXTRA_DIR, promoact_id);
		startActivity(promo_rules_intent); 
	}
	public void notInterested(View v) {		
		Bundle bundle = new Bundle();
    	bundle.putString("promoact", promoact_id);
    	NiDialogFragment nidf = new NiDialogFragment();
    	nidf.setArguments(bundle);
    	nidf.show(getSupportFragmentManager(), "ni");
	}
	public void sendToStoplist(View v) {		
		Bundle bundle = new Bundle();
    	bundle.putString("promoact", promoact_id);
    	StopDialogFragment nidf = new StopDialogFragment();
    	nidf.setArguments(bundle);
    	nidf.show(getSupportFragmentManager(), "sl");		
	}
	static File getConDir(Context ctxt) {
		 return(new File(ctxt.getFilesDir(), ConUpdateService.CON_BASEDIR));
	 }
	private boolean CheckView() {		
		Log.v("PA INT", promoact_id);
		pa_cursor = getPromoCursor(promoact_id);
		if (pa_cursor != null && pa_cursor.moveToFirst()) {
			int view_ind = pa_cursor.getColumnIndex(DatabaseHelper.VIEW);
			view = pa_cursor.getInt(view_ind);
			pa_cursor.close();
		}
		if (view == 0) {
			return true;
		}
		else {
			return false;
		}		
	}
	
	private Cursor getPromoCursor(String id) {
		 String p_query = String.format("SELECT * FROM %s WHERE promoact_id =" + id, DatabaseHelper.TABLE_P);
		 return(dbh.getReadableDatabase().rawQuery(p_query, null));
	}	
}
