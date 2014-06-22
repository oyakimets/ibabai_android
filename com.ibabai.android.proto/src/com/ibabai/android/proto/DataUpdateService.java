package com.ibabai.android.proto;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;


public class DataUpdateService extends IntentService {
	private static final String STORE_BASE_URL = "http://ibabai.picrunner.net/city_stores/";
	private static final String PROMO_BASE_URL = "http://ibabai.picrunner.net/promo_users/";
	private static final String PS_BASE_URL = "http://ibabai.picrunner.net/promo_stores/";
	public static final String PREFERENCES = "MyPrefs";
	public static final String city="city";
	public static final String user_id="user_id";
	public static final String PS_BASEDIR="promo_stores";	
	public static final String PS_EXT="ps_ext.zip";	
	public static final String PREF_PS_DIR="pendingPsDir";	
	private StringBuilder buf;
	private String PROMO_URL;
	private int u_id;
	private int c_id;
	private File psDir;
	BufferedReader reader=null;
	DatabaseHelper dbh;
	SharedPreferences shared_prefs;
	Location current_loc;	
	
	
	public DataUpdateService() {
		super("StoresUpdateService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		dbh=DatabaseHelper.getInstance(getApplicationContext());
		GPSTracker gps = new GPSTracker(this);
	    current_loc = gps.getLocation();
	    shared_prefs=getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
	    c_id=shared_prefs.getInt(city, 0);
	    u_id=shared_prefs.getInt(user_id, 0);
	    
		
		Cursor new_city_c = cityCursor();
		int city_id_ind=new_city_c.getColumnIndex(DatabaseHelper.C_ID);
	 	int lat_ind=new_city_c.getColumnIndex(DatabaseHelper.LAT);
	 	int lon_ind=new_city_c.getColumnIndex(DatabaseHelper.LON);
	 	int rad_ind=new_city_c.getColumnIndex(DatabaseHelper.RAD);
		if (new_city_c != null) {
			new_city_c.moveToFirst();
			while(new_city_c.isAfterLast()!=true) {					 
			 	int city_id=new_city_c.getInt(city_id_ind);
			 	double latitude=new_city_c.getDouble(lat_ind);
			 	double longitude=new_city_c.getDouble(lon_ind);
			 	int radius=new_city_c.getInt(rad_ind);
			 	Location location = new Location("city");
			 	location.setLatitude(latitude);
			 	location.setLongitude(longitude);
			 	float distance=current_loc.distanceTo(location);
			 	if (distance <= radius) {
			 		if (c_id == city_id) {
			 			new_city_c.close();
			 			break;
			 		}
			 		else if(c_id != city_id && c_id !=0) {
			 			if (StoresAvailability()) {
			 				dbh.ClearStores();
			 			}
			 			String STORES_URL = STORE_BASE_URL + Integer.toString(city_id) +".json";
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
							ClearStores();
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
		Cursor p_cursor=promoactCursor();
		if ( p_cursor != null && p_cursor.moveToFirst()) {
			if (p_cursor.getCount() == 0) {
				PROMO_URL = PROMO_BASE_URL+u_id+"/all_promoacts.txt";
				try {
					URL s_url=new URL(PROMO_URL);
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
					loadPromos(buf.toString(), u_id);							
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
			}
			else {
				PROMO_URL = PROMO_BASE_URL+u_id+"/update_promoacts.txt";
				try {
					URL s_url=new URL(PROMO_URL);
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
					deletePromos(buf.toString(), u_id);
					loadPromos(buf.toString(), u_id);							
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
			}			
		}
		try {
			String PS_URL = PS_BASE_URL+c_id+".zip";
			psDownloadInfo(PS_URL);
		}
		catch (Exception e) {
			Log.e(getClass().getSimpleName(), "Exception loading promo_stores");
		}
				
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
		 String p_query = String.format("SELECT * FROM %s", DatabaseHelper.TABLE_P);
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
	 public void ClearStores() {
		 if (StoresAvailability()) {
			 SQLiteDatabase sqldb = dbh.getWritableDatabase();
			 if (sqldb != null) {
				 sqldb.delete(DatabaseHelper.TABLE_S, null, null);
				 sqldb.close();
			 }
		}		
	 }
	 private void loadPromos(String st, int us_id) throws JSONException {
		JSONObject jso = new JSONObject(st);
		int u_id = jso.optInt("user_id");
		if (us_id == u_id) {
			JSONArray promoacts = jso.optJSONArray("add");
			if (promoacts.length() > 0) {
				for (int i=0; i<promoacts.length(); i++) {
					JSONObject promoact = promoacts.optJSONObject(i);
					Promoact p = new Promoact(promoact);
					dbh.AddPromo(p);
				}
			}			
		}
	 }
	 private void deletePromos(String st, int us_id) throws JSONException {
		 JSONObject jso = new JSONObject(st);
		 int u_id = jso.optInt("user_id");
		 if (us_id==u_id) {
			 JSONArray del_promoacts = jso.optJSONArray("delete");
			 if(del_promoacts.length() > 0) {
				 for(int i=0; i<del_promoacts.length(); i++) {
					 JSONObject del_promoact = del_promoacts.optJSONObject(i);
					 int promo_id = del_promoact.optInt("promoact_id");
					 dbh.deletePromo(promo_id);
				 }
			 }
		 }
	 }
	 
	 static File getPsDir(Context ctxt) {		 	 	
		return(new File(ctxt.getFilesDir(), PS_BASEDIR));
	}
	 
	 private void psDownloadInfo(String url) {
		 File root = this.getFilesDir();
		 psDir = new File(root, PS_BASEDIR);
		 if (!psDir.exists()) {
			 psDir=getPsDir(this);			 
		 }		
		 PreferenceManager.getDefaultSharedPreferences(this).edit().putString(PREF_PS_DIR, psDir.getAbsolutePath()).commit();
		 DownloadManager mgr=(DownloadManager) getSystemService(DOWNLOAD_SERVICE);
		 DownloadManager.Request req= new DownloadManager.Request(Uri.parse(url));
		 Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdirs();
		 req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE).setAllowedOverRoaming(false).setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, PS_EXT);
		 mgr.enqueue(req);
	}
	 
}