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
import android.database.Cursor;
import android.util.Log;

public class DelRegService extends IntentService {	
	SharedPreferences shared_prefs;
	private static final String P_CODE = "fc_1";	
	private int s_id;
	private int pa_id;
	private int multiple;
	private int count;
	public static ArrayList<String> dbPromos;
	public static ArrayList<String> storePromos;
	private Cursor pa_cursor;
	private Cursor ps_cursor;
	private Cursor pa_update_cursor;
	DatabaseHelper dbh;
	public DelRegService() {
		super("DelRegService");
	}

	@Override
	protected void onHandleIntent(Intent i) {
		dbh = DatabaseHelper.getInstance(getApplicationContext());
		shared_prefs = getSharedPreferences(IbabaiUtils.PREFERENCES, Context.MODE_PRIVATE);
		if (shared_prefs.contains(IbabaiUtils.AUTH_TOKEN)) {			
			s_id = shared_prefs.getInt(IbabaiUtils.LAST_STORE, 0);			
			updateDelivery(s_id);
		}
		else {
			stopSelf();
		}
	}
	
	private void updateDelivery(int store) {		
		dbPromos=new ArrayList<String>();
		storePromos=new ArrayList<String>();
		pa_cursor=promoactCursor();
		if (pa_cursor != null && pa_cursor.moveToFirst()) {
			int id_ind = pa_cursor.getColumnIndex("promoact_id");
			while (!pa_cursor.isAfterLast()) {
				String pa_id = Integer.toString(pa_cursor.getInt(id_ind));						
				dbPromos.add(pa_id);
				pa_cursor.moveToNext();
			}
			pa_cursor.close();
		}
		ps_cursor = storePromosCursor(store);
		if(ps_cursor != null && ps_cursor.moveToFirst()) {
			int paid_ind = ps_cursor.getColumnIndex("promoact_id");
			while (!ps_cursor.isAfterLast()) {
				String promoact_id=Integer.toString(ps_cursor.getInt(paid_ind));
				if (dbPromos.contains(promoact_id)) {
					storePromos.add(promoact_id);
				}
				ps_cursor.moveToNext();
			}
			ps_cursor.close();
		}
		for (int i=0; i<storePromos.size(); i++) {
			String promoact_id = storePromos.get(i);
			pa_id = Integer.parseInt(promoact_id);
			pa_update_cursor=getPromoCursor(promoact_id);			
			if (pa_update_cursor != null && pa_update_cursor.moveToFirst()) {
				int mult_ind = pa_update_cursor.getColumnIndex(DatabaseHelper.MULT);
				int del_ind = pa_update_cursor.getColumnIndex(DatabaseHelper.DEL);
				multiple = pa_update_cursor.getInt(mult_ind);
				int delivery = pa_update_cursor.getInt(del_ind);
				pa_update_cursor.close();
				count = delivery+1;				
			}
			RegisterDelivery(IbabaiUtils.LOGS_API_ENDPOINT_URL+"?auth_token="+shared_prefs.getString(IbabaiUtils.AUTH_TOKEN, ""), promoact_id, count, multiple);
		}
		dbh.close();
	}
	private Cursor storePromosCursor(int store_id) {		
		String ps_query= "SELECT * FROM promo_stores WHERE store_id="+Integer.toString(store_id);
		return (dbh.getReadableDatabase().rawQuery(ps_query, null));
	}
	private Cursor promoactCursor() {
		 String p_query = String.format("SELECT * FROM %s WHERE stopped=0", DatabaseHelper.TABLE_P);
		 return(dbh.getReadableDatabase().rawQuery(p_query, null));
	 }
	private Cursor getPromoCursor(String id) {
		 String p_query = String.format("SELECT * FROM %s WHERE promoact_id =" + id, DatabaseHelper.TABLE_P);
		 return(dbh.getReadableDatabase().rawQuery(p_query, null));
	 }
	private void RegisterDelivery(String url, String pa, int cnt, int mlt) {		
		
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
								
				cust_log_json.put(IbabaiUtils.P_ID, pa_id);
				cust_log_json.put(IbabaiUtils.STORE_ID, s_id);
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
					Log.v("DELIVERY", "Delivery registered");
					dbh.updateDeliveryCount(pa, cnt);
					if (cnt == mlt) {
						dbh.paStopUpdate(pa, 1);
					}
				}
				else {
					Log.e("DELIVERY", json.getString("info"));
				}
			}
			catch(Exception e) {
				Log.e("DELIVERY", e.getMessage());
			}
		}		
	}
	@Override
	public void onDestroy() {
		dbh.close();
		super.onDestroy();
	}
}
