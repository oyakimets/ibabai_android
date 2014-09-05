package com.ibabai.android.proto;

import java.io.IOException;
import java.util.ArrayList;

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
import android.util.Log;

public class ViewRegService extends IntentService {
	public static final String LOGS_API_ENDPOINT_URL=SignupActivity.BASE_API_ENDPOINT_URL+"cust_logs.json";
	public static final String PREFERENCES = "MyPrefs";	
	public static final String PASS = "password";
	public static final String LAST_STORE = "last_store";
	SharedPreferences shared_prefs;
	private String P_CODE; 
	public static final String S_ID = "store_id";
	private int s_id;
	private int pa_id;
	private String code;
	private String promoact_id;
	public static ArrayList<String> dbPromos;
	public static ArrayList<String> storePromos;	
	public ViewRegService() {
		super("ViewRegService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {		
		shared_prefs = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
		s_id = shared_prefs.getInt(LAST_STORE, 0);
		promoact_id = (String) intent.getExtras().get(ScanActivity.EXTRA_PA);
		code = (String) intent.getExtras().get(ScanActivity.EXTRA_CODE);
		if (code.equals("s")) {
			P_CODE = "fc_3";
		}
		else {
			P_CODE = "fc_2";
		}
		pa_id = Integer.parseInt(promoact_id);
		RegisterView(LOGS_API_ENDPOINT_URL+"?auth_token="+shared_prefs.getString("AuthToken", ""));
		
	}
		
	private void RegisterView(String url) {		
		
		DefaultHttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(url);
		JSONObject holder = new JSONObject();
		JSONObject cust_log_json = new JSONObject();
		String response = null;
		JSONObject json = new JSONObject();			
		try {
			try {
				json.put("success", false);
				json.put("info", "Something went wrong. Try again!");
												
				cust_log_json.put(ScanActivity.P_ID, pa_id);
				cust_log_json.put(S_ID, s_id);
				cust_log_json.put(P_CODE, true);					
				holder.put("cust_log", cust_log_json);
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
					Log.v("VIEW", "VIEW registered");
				}
				else {
					Log.e("VIEW", json.getString("info"));
				}
			}
			catch(Exception e) {
				Log.e("VIEW", e.getMessage());
			}
		}		
	}	
}
