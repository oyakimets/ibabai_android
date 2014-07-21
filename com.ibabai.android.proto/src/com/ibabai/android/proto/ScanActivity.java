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
import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ScanActivity extends Activity implements OnClickListener {
	
	public static final String PAYMENT_API_ENDPOINT_URL="http://192.168.1.100:3000/api/v1/transactions.json";
	private TextView tv_balance;
	public static final String EXTRA_CODE = "code";
	public static final String EXTRA_PA = "pa_id";
	public static final String AGENT_ID = "agent_id";
	public static final String AGENT_NAME = "agent_name";
	public static final String FLAG = "dc_flag";
	public static final String AMOUNT = "amount";
	public static final String P_ID = "promoact_id";
	public static final String PREFERENCES = "MyPrefs";	
	public static final String store_id = "store_id";
	public static final String last_store = "last_store";
	public static final String user_id = "user_id";
	public static final String balance = "Balance";
	public static final String HP_ID = "7";
	private String bal_value;
	private String scanContent;	
	private Cursor pa_cursor = null;
	private Cursor home_cursor = null;
	private String c_name;
	private int c_id;
	private int pact_id;	
	private int rew2;
	SharedPreferences shared_prefs;
	private int store_prox;	
	private Button scanBtn;
	private TextView formatTxt, contentTxt, scanTitle, scanDesc, scanResult;
	DatabaseHelper dbh;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scanner);
        
        scanTitle = (TextView)findViewById(R.id.scan_title);
        scanDesc=(TextView)findViewById(R.id.scan_desc);       
        scanBtn = (Button)findViewById(R.id.scan_button);
        formatTxt=(TextView)findViewById(R.id.scan_format);
        contentTxt=(TextView)findViewById(R.id.scan_content);
        
        scanBtn.setOnClickListener(this);        
        
        ActionBar ab = getActionBar(); 
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setCustomView(R.layout.ab_balance);
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayShowTitleEnabled(false);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
        
        shared_prefs=getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);       
        tv_balance = (TextView) findViewById(R.id.balance);        
        bal_value = shared_prefs.getString(balance, "0");
        tv_balance.setText("balance "+ bal_value + " bais");
        
        dbh=DatabaseHelper.getInstance(this);
        
        shared_prefs = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        store_prox = shared_prefs.getInt(store_id, 0);       
        if (store_prox != 0) {
        	scanTitle.setText("Test scanning mode");
            scanDesc.setText("For product identification purposes only. You can register purchase only outside of the store.");
            
        }
        else {
        	scanTitle.setText("Product registration mode");
            scanDesc.setText("You can register any product for any active promo.");
        }
        /*Async extract ArrayList of product codes from DB
         * 
         */
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.core, menu);		
		 
		return true;
	}
	public void onClick(View v) {
		Intent intent=new Intent("com.google.zxing.client.android.SCAN");
		intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
		startActivityForResult(intent, 0);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		shared_prefs = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        store_prox = shared_prefs.getInt(store_id, 0);
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				scanResult=(TextView)findViewById(R.id.scan_result);
				scanContent = intent.getStringExtra("SCAN_RESULT");
				String scanFormat = intent.getStringExtra("SCAN_RESULT_FORMAT");
				formatTxt.setText("FORMAT: "+scanFormat);
				contentTxt.setText("CONTENT: "+scanContent);
				
				pa_cursor = getPromoCursor(scanContent); 
				if (pa_cursor != null && pa_cursor.moveToFirst()) {
					int pa_id_ind = pa_cursor.getColumnIndex("promoact_id");
					pact_id = pa_cursor.getInt(pa_id_ind);
					
					if(store_prox != 0) {
						scanResult.setBackgroundResource(R.drawable.scan_res_s);
						scanResult.setText("Success! This product participate in a promo.");						
					}
					else {						
						purchRewCheck(pa_cursor);
						dbh.close();						
					}						
					
				}
				else {
					home_cursor = getHomeCursor();
					if (home_cursor != null && home_cursor.moveToFirst()) {
						handleHomePromo(home_cursor);						
						dbh.close();	
						
					}
				}
			}
			else if (resultCode == RESULT_CANCELED) {
				Toast toast = Toast.makeText(getApplicationContext(), "Scanning error. Try again!", Toast.LENGTH_LONG);
				toast.show();
			}
		}		
	}
	private Cursor getPromoCursor(String b_code) {
		 String p_query = String.format("SELECT * FROM %s WHERE barcode = " + b_code, DatabaseHelper.TABLE_P);
		 return(dbh.getReadableDatabase().rawQuery(p_query, null));
	 }
	private Cursor getHomeCursor() {
		 String p_query = String.format("SELECT * FROM %s WHERE  stopped = 0 AND promoact_id = "+HP_ID, DatabaseHelper.TABLE_P);
		 return(dbh.getReadableDatabase().rawQuery(p_query, null));
	 }
	public void purchRewCheck(Cursor c) {
		int purch_ind = c.getColumnIndex(DatabaseHelper.PURCH);
		int purch = c.getInt(purch_ind);
		if (purch == 0) {
			int id_ind = c.getColumnIndex(DatabaseHelper.P_ID);			
			int rew2_ind = c.getColumnIndex(DatabaseHelper.REW2);
			int c_id_ind = c.getColumnIndex(DatabaseHelper.CL_ID);
			int c_name_ind = c.getColumnIndex(DatabaseHelper.CL_NAME);
			c_name = c.getString(c_name_ind);
			rew2 = c.getInt(rew2_ind);			
			c_id = c.getInt(c_id_ind);
			pact_id = c.getInt(id_ind);
			c.close();			
			creditFromApi();			
			
		}
		else {
			scanResult.setBackgroundResource(R.drawable.scan_res_f);
			scanResult.setText("No credit! This product was registered. Check your logbook.");
			c.close();
			dbh.close();
		}
	}
	public void handleHomePromo(Cursor c) {
		int purch_ind = c.getColumnIndex(DatabaseHelper.PURCH);
		int purch = c.getInt(purch_ind);
		if (purch == 0) {
			int id_ind = c.getColumnIndex(DatabaseHelper.P_ID);			
			int rew2_ind = c.getColumnIndex(DatabaseHelper.REW2);
			int c_name_ind = c.getColumnIndex(DatabaseHelper.CL_NAME);
			int c_id_ind = c.getColumnIndex(DatabaseHelper.CL_ID);
			c_name = c.getString(c_name_ind);
			rew2 = c.getInt(rew2_ind);			
			pact_id = c.getInt(id_ind);
			c_id = c.getInt(c_id_ind);
			c.close();				
			
			creditFromApi();			
			
		}
		else {
			scanResult.setBackgroundResource(R.drawable.scan_res_f);
			scanResult.setText("Wrong product. Check your promos.");
			c.close();
			dbh.close();
		}
	}
	static File getConDir(Context ctxt) {
		 return(new File(ctxt.getFilesDir(), ConUpdateService.CON_BASEDIR));
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
					pay_json.put(AMOUNT, rew2);
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
					dbh.addLogEntry(c_name, Integer.toString(rew2), "C");
					dbh.updatePurch(pact_id);									
					tv_balance = (TextView) findViewById(R.id.balance);
			        tv_balance.setText("balance "+ new_balance + " bais"); 
					
					SoundEffects.playSound(ScanActivity.this, SoundEffects.coin);
					
					scanResult=(TextView)findViewById(R.id.scan_result);
					scanResult.setBackgroundResource(R.drawable.scan_res_s);
					scanResult.setText("Success! "+ Integer.toString(rew2) + " bais were credited to your balance.");
					Log.v("CREDIT", "Account credited");
					if (HP_ID.equals(Integer.toString(pact_id))) {
						dbh.deleteHomePromo();
						File home_dir = new File(getConDir(ScanActivity.this), HP_ID);
						if (home_dir.exists()) {
							home_dir.delete();
						}
					}
					
					Intent i = new Intent(ScanActivity.this, ViewRegService.class);
					i.putExtra(EXTRA_PA, Integer.toString(pact_id));
					i.putExtra(EXTRA_CODE, "s");
					startService(i);
					
				}
				else {
					Log.e("CREDIT", json.getString("info"));
					Toast.makeText(ScanActivity.this, "Error! No registration! Try again!", Toast.LENGTH_LONG).show();
				}	
			}
			catch(Exception e) {
				Log.e("SCAN", "Post execute exception");
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
