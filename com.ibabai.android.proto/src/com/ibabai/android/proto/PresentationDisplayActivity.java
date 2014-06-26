package com.ibabai.android.proto;

import java.io.File;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class PresentationDisplayActivity extends FragmentActivity {
	
	public static final String EXTRA_POSITION="position";
	public static final String EXTRA_PA="pa_id";
	SharedPreferences shared_prefs;
	public static final String PREFERENCES = "MyPrefs";
	public static final String balance = "Balance";
	public static final String MODEL="promo_model";
	private String pa_folder_path=null;
	private ViewPager pres_pager=null;
	private PromoPresAdapter adapter=null;
	private String bal_value;
	DatabaseHelper dbh;
	
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
        
        shared_prefs=getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        bal_value = shared_prefs.getString(balance, "0");
        viewRewCheck();
        TextView tv_balance = (TextView) findViewById(R.id.balance);
        tv_balance.setText(bal_value + " bais"); 
                
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
		String pa_id=getIntent().getStringExtra(EXTRA_PA);
		File pa_folder = new File(getConDir(this), pa_id);
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
		String pa_id=getIntent().getStringExtra(EXTRA_PA);		
		Intent promo_rules_intent=new Intent(this, PromoRulesActivity.class);
		promo_rules_intent.putExtra(PromoRulesActivity.EXTRA_DIR, pa_id);
		startActivity(promo_rules_intent); 
	}
	public void notInterested(View v) {
		String pa_id=getIntent().getStringExtra(EXTRA_PA);
		Bundle bundle = new Bundle();
    	bundle.putString("promoact", pa_id);
    	NiDialogFragment nidf = new NiDialogFragment();
    	nidf.setArguments(bundle);
    	nidf.show(getSupportFragmentManager(), "ni");
	}
	public void sendToStoplist(View v) {
		String pa_id=getIntent().getStringExtra(EXTRA_PA);
		Bundle bundle = new Bundle();
    	bundle.putString("promoact", pa_id);
    	StopDialogFragment nidf = new StopDialogFragment();
    	nidf.setArguments(bundle);
    	nidf.show(getSupportFragmentManager(), "sl");		
	}
	static File getConDir(Context ctxt) {
		 return(new File(ctxt.getFilesDir(), ConUploadService.CON_BASEDIR));
	 }
	public void viewRewCheck() {
		String pa_id=getIntent().getStringExtra(EXTRA_PA);
		Cursor pa_cursor = getPromoCursor(pa_id);
		if (pa_cursor != null && pa_cursor.moveToFirst()) {
			int view_ind = pa_cursor.getColumnIndex(DatabaseHelper.VIEW);
			int view = pa_cursor.getInt(view_ind);
			if (view == 0) {
				int rew1_ind = pa_cursor.getColumnIndex(DatabaseHelper.REW1);
				int c_name_ind = pa_cursor.getColumnIndex(DatabaseHelper.CL_NAME);
				String c_name = pa_cursor.getString(c_name_ind);
				int rew1 = pa_cursor.getInt(rew1_ind);
				pa_cursor.close();				
				dbh.updateView(pa_id);
				dbh.addLogEntry(c_name, rew1, "C");
				dbh.close();
				int bal_amnt = Integer.parseInt(bal_value);
				int new_amnt = bal_amnt + rew1;
				bal_value = Integer.toString(new_amnt);
				Editor editor = shared_prefs.edit();
				editor.putString(balance, bal_value);
				editor.apply();
				Toast t = Toast.makeText(this, Integer.toString(rew1) + " bais were credited to your balance.", Toast.LENGTH_LONG );
				t.show();
			}
			else {
				pa_cursor.close();
				dbh.close();
			}
		}
	}
	private Cursor getPromoCursor(String id) {
		 String p_query = String.format("SELECT * FROM %s WHERE promoact_id =" + id, DatabaseHelper.TABLE_P);
		 return(dbh.getReadableDatabase().rawQuery(p_query, null));
	 }
}
