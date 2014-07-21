package com.ibabai.android.proto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.location.Location;
import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;


public class DataUpdateService extends com.commonsware.cwac.wakeful.WakefulIntentService {
	private static final String STORE_BASE_URL = "http://ibabai.picrunner.net/city_stores/";
	private static final String PROMO_BASE_URL = "http://ibabai.picrunner.net/promo_users/";
	private static final String SP_BASE_URL = "http://ibabai.picrunner.net/promo_stores/";
	private static final String VEN_BASE_URL = "http://ibabai.picrunner.net/vendors/active_vendors.txt";
	public static final String PREFERENCES = "MyPrefs";
	public static final String city="city";
	public static final String user_id="user_id";
	public static final String PS_BASEDIR="promo_stores";	
	public static final String PS_EXT="ps_ext.zip";	
	public static final String PREF_PS_DIR="pendingPsDir";
	private ArrayList<Integer> current_pa;
	private ArrayList<Integer> update_pa;
	private JSONArray promoacts = null;
	private StringBuilder buf;
	private String PROMO_URL;
	private String u_id;
	private int city_id;
	private int c_id;	
	BufferedReader reader=null;
	DatabaseHelper dbh;
	SharedPreferences shared_prefs;
	Location current_loc;	
	
	
	public DataUpdateService() {
		super("StoresUpdateService");
	}

	@Override
	protected void doWakefulWork(Intent intent) {
		Intent stop_i = new Intent(this, LocationService.class);
		stopService(stop_i);
		dbh=DatabaseHelper.getInstance(getApplicationContext());
		GPSTracker gps = new GPSTracker(this);
	    current_loc = gps.getLocation();
	    shared_prefs=getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
	    c_id=shared_prefs.getInt(city, 0);
	    u_id=shared_prefs.getString(user_id, null);
	    
		
		Cursor new_city_c = cityCursor();
		int city_id_ind=new_city_c.getColumnIndex(DatabaseHelper.C_ID);
	 	int lat_ind=new_city_c.getColumnIndex(DatabaseHelper.LAT);
	 	int lon_ind=new_city_c.getColumnIndex(DatabaseHelper.LON);
	 	int rad_ind=new_city_c.getColumnIndex(DatabaseHelper.RAD);
		if (new_city_c != null && new_city_c.moveToFirst()) {
			while(new_city_c.isAfterLast()!=true) {					 
			 	city_id=new_city_c.getInt(city_id_ind);
			 	double latitude=new_city_c.getDouble(lat_ind);
			 	double longitude=new_city_c.getDouble(lon_ind);
			 	int radius=new_city_c.getInt(rad_ind);
			 	Location location = new Location("city");
			 	location.setLatitude(latitude);
			 	location.setLongitude(longitude);
			 	float distance=current_loc.distanceTo(location);
			 	if (distance <= radius) {
			 		if (c_id == city_id) {
			 			/*scan stores and if any add/delete run update
			 			 * for this functionality city folders are to be created under city_stores
			 			 * on FTP server with two files "all_stores" and "update_stores". Update stores would 
			 			 * contain two arrays "add" and "delete". "update_stores" would hold changes fore the last 24 hours.
			 			 */
			 			new_city_c.close();
			 			break;
			 		}
			 		else {
			 			if (StoresAvailability()) {
			 				dbh.ClearStores();
			 			}
			 			String STORES_URL = STORE_BASE_URL + Integer.toString(city_id) +".txt";
						try {
							URL s_url=new URL(STORES_URL);
							HttpURLConnection con=(HttpURLConnection)s_url.openConnection();
							con.setRequestMethod("GET");
							con.setReadTimeout(15000);
							con.connect();
							
							reader=new BufferedReader(new InputStreamReader(con.getInputStream()));
							buf = new StringBuilder();
							String line = null;
							
							while ((line=reader.readLine()) != null) {
								buf.append(line+"\n");
							}							
							loadStores(buf.toString(), city_id);							
						}
						catch (Exception e) {
							Log.e(getClass().getSimpleName(), "Exception retrieving store data", e);
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
			 			Editor edit=shared_prefs.edit();
			 			edit.putInt(city, city_id);
			 			edit.apply();
			 		break;
			 		}
			 	 }
			 	 new_city_c.moveToNext();
		 	}
		}
		if (c_id != 0) {
			Cursor ps_cursor=promostoreCursor();
			if (ps_cursor != null && ps_cursor.getCount() != 0) {
				dbh.ClearPromoStores();
				ps_cursor.close();
			}
		
			String SP_URL= SP_BASE_URL + Integer.toString(c_id) +".txt";
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
		
		PROMO_URL = PROMO_BASE_URL+u_id+".txt";
		try {
			URL p_url=new URL(PROMO_URL);
			HttpURLConnection con=(HttpURLConnection)p_url.openConnection();
			con.setRequestMethod("GET");
			con.setReadTimeout(15000);
			con.connect();
					
			reader=new BufferedReader(new InputStreamReader(con.getInputStream()));
			buf = new StringBuilder();
			String line = null;
					
			while ((line=reader.readLine()) != null) {
				buf.append(line+"\n");
			}
			loadPromos(buf.toString(), u_id);
			killPromos(buf.toString(), u_id);	
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
		
		dbh.ClearVendors();		
		try {
			URL ven_url=new URL(VEN_BASE_URL);
			HttpURLConnection con=(HttpURLConnection)ven_url.openConnection();
			con.setRequestMethod("GET");
			con.setReadTimeout(15000);
			con.connect();
			
			reader=new BufferedReader(new InputStreamReader(con.getInputStream()));
			StringBuilder buf = new StringBuilder();
			String line = null;
			
			while ((line=reader.readLine()) != null) {
				buf.append(line+"\n");
			}
			loadVendors(buf.toString());
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
		Intent start_i = new Intent(this, LocationService.class);
	    startService(start_i);
		WakefulIntentService.sendWakefulWork(this, ConUpdateService.class);		
	}

	private boolean StoresAvailability() {		
		String s_query = String.format("SELECT * FROM %s", DatabaseHelper.TABLE_S);
		Cursor c = dbh.getWritableDatabase().rawQuery(s_query, null);
		if (c.getCount() != 0) {
			c.close();
			return (true);
		}
		else {
			c.close();
			return (false);
		}
	}
	
	 private Cursor cityCursor() {
		 String c_query = String.format("SELECT * FROM %s", DatabaseHelper.TABLE_C);
		 return(dbh.getReadableDatabase().rawQuery(c_query, null));
	 }
	 private Cursor promoactCursor() {
		 String p_query = String.format("SELECT * FROM %s WHERE promoact_id != 7", DatabaseHelper.TABLE_P);
		 return(dbh.getReadableDatabase().rawQuery(p_query, null));
	 }
	 private Cursor promostoreCursor() {
		 String p_query = String.format("SELECT * FROM %s", DatabaseHelper.TABLE_SP);
		 return(dbh.getReadableDatabase().rawQuery(p_query, null));
	 }
	 private void loadStores(String st, int id) throws JSONException {
		JSONObject jso = new JSONObject(st);
		int c_id = jso.optInt("city_id");
		if (id == c_id) {
			JSONArray stores = jso.optJSONArray("stores");
			if (stores.length()>0) {
				for (int i=0; i<stores.length(); i++) {
					JSONObject store = stores.optJSONObject(i);
					Store s = new Store(store);
					dbh.AddStore(s);
				}
			}			
		}		
	}	
	 private void loadPromos(String str, String us_id ) throws JSONException {
		current_pa = CurrentPromos();
		update_pa = UpdatePromos(str, Integer.parseInt(us_id));
		JSONObject jso = new JSONObject(str);
		promoacts = jso.optJSONArray("promos");
		if (promoacts.length() > 0) {
			for (int i=0; i<update_pa.size(); i++) {
				if (!current_pa.contains(update_pa.get(i))) {
					JSONObject promoact = promoacts.optJSONObject(i);
					Promoact p = new Promoact(promoact);
					dbh.AddPromo(p);
				}
			}
		}		
	 }
	 private void killPromos(String str, String us_id) throws JSONException {
		 current_pa = CurrentPromos();
		 update_pa = UpdatePromos(str, Integer.parseInt(us_id));		
		 if (current_pa.size() > 0) {
			 for (int i=0; i<current_pa.size(); i++) {
				 if (!update_pa.contains(current_pa.get(i))) {
					dbh.deletePromo(current_pa.get(i));
				 }
			 }
		 }		 
	 }
	 private ArrayList<Integer> UpdatePromos(String st, int us_id) throws JSONException {
		ArrayList<Integer> up_lst = new ArrayList<Integer>();
		JSONObject jso = new JSONObject(st);
		int u_id = jso.optInt("user_id");
		if (us_id == u_id) {
			promoacts = jso.optJSONArray("promos");
			if (promoacts.length() > 0) {
				for (int i=0; i<promoacts.length(); i++) {
					JSONObject promoact = promoacts.optJSONObject(i);
					int pa_id = promoact.getInt("promoact_id");
					up_lst.add(pa_id);
				}
			}			
		}
		return up_lst;
	}
	 private ArrayList<Integer> CurrentPromos() {
		 ArrayList<Integer> cur_lst = new ArrayList<Integer>();
		 Cursor pa_c = promoactCursor();
		 if (pa_c != null && pa_c.moveToFirst()) {
			 int id_ind = pa_c.getColumnIndex("promoact_id");
			 while (!pa_c.isAfterLast()) {
				 int pa_id = pa_c.getInt(id_ind);
				 cur_lst.add(pa_id);
				 pa_c.moveToNext();
			 }
			 pa_c.close();
		 }
		 return cur_lst; 
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
	private void loadVendors(String st) throws JSONException {
		JSONArray jsa = new JSONArray(st);		
		if (jsa.length() > 0) {
			for (int i=0; i<jsa.length(); i++) {
				JSONObject ven = jsa.optJSONObject(i);
				Vendor v = new Vendor(ven);
				dbh.AddVendor(v);				
			}			
		}
	}
}