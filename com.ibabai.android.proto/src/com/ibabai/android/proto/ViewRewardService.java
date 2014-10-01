package com.ibabai.android.proto;

import java.io.IOException;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
public class ViewRewardService extends IntentService {	
	SharedPreferences shared_prefs;	
	private int view;
	private int rew1;
	private String c_name;
	private int c_id;	
	private String promoact_id;	
	DatabaseHelper dbh;
	private Cursor pa_cursor = null;
	Handler rewHandler;
	private JSONObject json;
	private String error_info;
	private String e_info;
	
	public ViewRewardService() {
		super("ViewRewardService");
	}
	@Override
	public void onCreate() {
		super.onCreate();
		rewHandler = new Handler();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		dbh = DatabaseHelper.getInstance(this.getApplicationContext()); 
		shared_prefs = getSharedPreferences(IbabaiUtils.PREFERENCES, Context.MODE_PRIVATE);
		promoact_id = (String) intent.getExtras().get(IbabaiUtils.EXTRA_PA);
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
				RegisterReward(IbabaiUtils.PAYMENT_API_ENDPOINT_URL+"?auth_token="+shared_prefs.getString(IbabaiUtils.AUTH_TOKEN, ""));
				
			}
			else {
				pa_cursor.close();
				dbh.close();
			}
		}		
	}
	
	private void RegisterReward(String url) {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(url);
		JSONObject holder = new JSONObject();
		JSONObject pay_json = new JSONObject();
		String response = null;
		json = new JSONObject();	
		shared_prefs= getSharedPreferences(IbabaiUtils.PREFERENCES, Context.MODE_PRIVATE);				
			
		try {
			try {
				json.put("success", false);
				json.put("info", "Something went wrong. Try again!");					
											
				pay_json.put(IbabaiUtils.AGENT_ID, c_id);
				pay_json.put(IbabaiUtils.AGENT_NAME, c_name);
				pay_json.put(IbabaiUtils.FLAG, "C");					
				pay_json.put(IbabaiUtils.AMOUNT, rew1);
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
		
		if (json != null) {
			try {
				if (json.getBoolean("success")) {
					String new_balance = Integer.toString(json.getJSONObject("data").getInt("balance")); 
					Editor e = shared_prefs.edit();					 
					e.putString(IbabaiUtils.BALANCE, new_balance);
					e.apply();
					dbh.addLogEntry(c_name, Integer.toString(rew1), "C");
					dbh.updateView(promoact_id);					
					dbh.close();				
						
					SoundEffects.playSound(this, SoundEffects.coin);
					
					rewHandler.post(new Runnable() {
						@Override
						public void run() {
							Toast t = Toast.makeText(ViewRewardService.this, Integer.toString(rew1) + " bais were credited to your balance.", Toast.LENGTH_LONG );
							t.show();
						}
					});	
					
					Log.v("CREDIT", "Account credited");
					Intent i = new Intent(this, ViewRegService.class);
					i.putExtra(IbabaiUtils.EXTRA_PA, promoact_id);
					i.putExtra(IbabaiUtils.EXTRA_CODE, "v");
					startService(i);						
				}
				else {
					Log.e("CREDIT", json.getString("info"));
					error_info = json.getString("info");
					rewHandler.post(new Runnable() {
						public void run() {					
							Toast.makeText(ViewRewardService.this, error_info, Toast.LENGTH_LONG).show();
						}
					});
				}	
			}
			catch(Exception e) {
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
				e_info =  e.getMessage(); 
				rewHandler.post(new Runnable() {
					public void run() {					
						Toast.makeText(ViewRewardService.this, e_info, Toast.LENGTH_LONG).show();
					}
				});
			}			
		}
	}
	private Cursor getPromoCursor(String id) {
		 String p_query = String.format("SELECT * FROM %s WHERE promoact_id =" + id, DatabaseHelper.TABLE_P);
		 return(dbh.getReadableDatabase().rawQuery(p_query, null));
	 }	
}


