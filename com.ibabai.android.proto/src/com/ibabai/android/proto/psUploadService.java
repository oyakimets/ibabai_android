package com.ibabai.android.proto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class psUploadService extends IntentService {
	private static final String SP_BASE_URL = "http://ibabai.picrunner.net/promo_stores/";
	private static final String PA_URL = "http://ibabai.picrunner.net/promo_users/ibabai_promoacts.txt";
	public static final String PREFERENCES = "MyPrefs";
	public static final String city="city";
	private int city_id;	
	BufferedReader reader=null;
	DatabaseHelper dbh;
	SharedPreferences shared_prefs;
	public psUploadService() {
		super("psUploadService");
	}

	@Override
	protected void onHandleIntent(Intent arg0) {
		dbh=DatabaseHelper.getInstance(getApplicationContext());
		shared_prefs = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
		city_id = shared_prefs.getInt(city, 0);
		try {
			URL s_url=new URL(PA_URL);
			HttpURLConnection con=(HttpURLConnection)s_url.openConnection();
			con.setRequestMethod("GET");
			con.setReadTimeout(15000);
			con.connect();
					
			reader=new BufferedReader(new InputStreamReader(con.getInputStream()));
			StringBuilder buf = new StringBuilder();
			String line = null;
					
			while ((line=reader.readLine()) != null) {
				buf.append(line+"\n");
			}
			loadPromos(buf.toString());							
		}
		catch (Exception e) {
			Log.e(getClass().getSimpleName(), "Exception retrieving promo data", e);
		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				}
				catch (IOException e) {
					Log.e(getClass().getSimpleName(), "Exception closing HUC reader", e);						
				}
			}			 			
		}
		if (city_id != 0) {
			String SP_URL= SP_BASE_URL + Integer.toString(city_id) +".txt";
			try {
				URL sp_url=new URL(SP_URL);
				HttpURLConnection con=(HttpURLConnection)sp_url.openConnection();
				con.setRequestMethod("GET");
				con.setReadTimeout(15000);
				con.connect();
				
				reader=new BufferedReader(new InputStreamReader(con.getInputStream()));
				StringBuilder buf = new StringBuilder();
				String line = null;
				
				while ((line=reader.readLine()) != null) {
					buf.append(line+"\n");
				}
				loadPromoStores(buf.toString());
			}
			catch (Exception e) {
				Log.e(getClass().getSimpleName(), "Exception retrieving promo_store data", e);
			}
			finally {
				if (reader != null) {
					try {
						reader.close();
					}
					catch (IOException e) {
						Log.e(getClass().getSimpleName(), "Exception closing HUC reader", e);
					}
				}
			}				
		}
		Intent con_intent = new Intent(this, ConUpdateService.class);
		startService(con_intent);
		Intent loc_intent = new Intent(this, LocationService.class);
		startService(loc_intent);
	}
	private void loadPromoStores(String st) throws JSONException {
		JSONArray jsa = new JSONArray(st);
		for (int i=0; i<jsa.length(); i++) {
			JSONObject store_item = jsa.optJSONObject(i);
			int store_id = store_item.optInt("store_id");
			JSONArray promo_item = store_item.optJSONArray("promo_ids");
			for (int j=0; j<promo_item.length(); j++) {
				int promoact_id = promo_item.optInt(j);
				dbh.addPromoStores(store_id, promoact_id);
			}
		}
	}
	
	private void loadPromos(String st) throws JSONException {
		JSONObject jso = new JSONObject(st);
		JSONArray promoacts = jso.optJSONArray("promos");
		if (promoacts.length() > 0) {
			for (int i=0; i<promoacts.length(); i++) {
				JSONObject promoact = promoacts.optJSONObject(i);
				Promoact p = new Promoact(promoact);
				dbh.AddPromo(p);				
			}			
		}
	}
}
