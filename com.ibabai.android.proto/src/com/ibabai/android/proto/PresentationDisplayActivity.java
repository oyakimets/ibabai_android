package com.ibabai.android.proto;

import java.io.File;
import java.io.IOException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import com.savagelook.android.UrlJsonAsyncTask;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class PresentationDisplayActivity extends FragmentActivity {
	private TextView tv_balance;
	public static final String PAYMENT_API_ENDPOINT_URL="http://192.168.1.100:3000/api/v1/transactions.json";
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
	private int rew1;
	private String c_name;
	private int c_id;	
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
			AddViewRew();
			Intent i = new Intent(this, ViewRegService.class);
			i.putExtra(EXTRA_PA, promoact_id);
			i.putExtra(ScanActivity.EXTRA_CODE, "v");
			startService(i);
		}
		else {			
			Toast.makeText(this, "Reward was granted already. Check your logbook.", Toast.LENGTH_LONG).show();
			tv_balance = (TextView) findViewById(R.id.balance);
			tv_balance.setText("balance "+bal_value + " bais"); 
		}
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
	public void AddViewRew() {		
		pa_cursor = getPromoCursor(promoact_id);
		if (pa_cursor != null && pa_cursor.moveToFirst()) {
			int view_ind = pa_cursor.getColumnIndex(DatabaseHelper.VIEW);
			view = pa_cursor.getInt(view_ind);
			if (view == 0) {
				int rew1_ind = pa_cursor.getColumnIndex(DatabaseHelper.REW1);
				int c_name_ind = pa_cursor.getColumnIndex(DatabaseHelper.CL_NAME);
				int c_id_ind = pa_cursor.getColumnIndex(DatabaseHelper.CL_ID);
				c_name = pa_cursor.getString(c_name_ind);
				c_id = pa_cursor.getInt(c_id_ind);
				rew1 = pa_cursor.getInt(rew1_ind);
				pa_cursor.close();
				creditFromApi();
				
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
	
	private class CreditRegisterTask extends UrlJsonAsyncTask {
		public CreditRegisterTask(Context ctxt) {
			super(ctxt);
		}
		@Override
	    protected JSONObject doInBackground(String... urls) {		
		
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(urls[0]);
			JSONObject holder = new JSONObject();
			JSONObject pay_json = new JSONObject();
			String response = null;
			JSONObject json = new JSONObject();	
			shared_prefs= getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
			
		
			try {
				try {
					json.put("success", false);
					json.put("info", "Something went wrong. Try again!");					
										
					pay_json.put(AGENT_ID, c_id);
					pay_json.put(AGENT_NAME, c_name);
					pay_json.put(FLAG, "C");					
					pay_json.put(AMOUNT, rew1);
					holder.put("transaction", pay_json);
					StringEntity se = new StringEntity(holder.toString());
					post.setEntity(se);
					
					post.setHeader("Accept", "application/json");
					post.addHeader("Content-Type", "application/json");
					
					ResponseHandler<String> r_handler = new BasicResponseHandler();
					response = client.execute(post, r_handler);
					json = new JSONObject(response);					
				}
				catch (HttpResponseException ex) {
					ex.printStackTrace();
					Log.e("ClientProtocol", ""+ex);
					json.put("info", "Response Error");
				}
				catch (IOException ex) {
					ex.printStackTrace();
					Log.e("IO", ""+ex);
				}
			}
			catch (JSONException ex) {
				ex.printStackTrace();
				Log.e("JSON", ""+ex);
			}			
			
			return json;
		}
		@Override
		protected void onPostExecute(JSONObject json) {
			try {
				if (json.getBoolean("success")) {
					String new_balance = Integer.toString(json.getJSONObject("data").getInt("balance")); 
					Editor e = shared_prefs.edit();					 
					e.putString(balance, new_balance);
					e.apply();
					dbh.addLogEntry(c_name, Integer.toString(rew1), "C");
					dbh.updateView(promoact_id);					
					dbh.close();				
					tv_balance = (TextView) findViewById(R.id.balance);
			        tv_balance.setText("balance "+ new_balance + " bais"); 
					
					SoundEffects.playSound(PresentationDisplayActivity.this, SoundEffects.coin);
					
					Toast t = Toast.makeText(PresentationDisplayActivity.this, Integer.toString(rew1) + " bais were credited to your balance.", Toast.LENGTH_LONG );
					t.show();
					Log.v("CREDIT", "Account credited");
					
				}
				else {
					Log.e("CREDIT", json.getString("info"));
					Toast.makeText(PresentationDisplayActivity.this, json.getString("info"), Toast.LENGTH_LONG).show();
				}	
			}
			catch(Exception e) {
				Toast.makeText(PresentationDisplayActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
			}
			finally {
				super.onPostExecute(json);
			}
		}
	}
	
	private void creditFromApi() {
		CreditRegisterTask payment_task = new CreditRegisterTask(this);
		payment_task.setMessageLoading("Processing reward...");		
		payment_task.execute(PAYMENT_API_ENDPOINT_URL+"?auth_token="+shared_prefs.getString("AuthToken", ""));	
	}
}
