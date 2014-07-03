package com.ibabai.android.proto;

import java.io.File;
import java.util.ArrayList;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;

public class VenUpdateService extends com.commonsware.cwac.wakeful.WakefulIntentService {
	public static final String PREFERENCES = "MyPrefs";
	public static final String LOAD_TOGGLE = "load_toggle";
	private static final String VEN_BASE_URL = "http://ibabai.picrunner.net/vendors/";
	public static final String VEN_EXT="ven_ext.jpg";
	public static final String VEN_BASEDIR="vendors";
	public static final String PREF_VEN_DIR="pendingConDir";	
	private Cursor ven_cursor;
	private File ven_dir;
	private ArrayList<String> tag_lst;
	private ArrayList<String> ven_lst;
	private ArrayList<String> ven_to_load;	
	DatabaseHelper dbh;
	SharedPreferences shared_prefs;
	public VenUpdateService() {
		super("VenUpdateService");
	}
	@Override
	protected void doWakefulWork(Intent intent) {
		dbh = DatabaseHelper.getInstance(getApplicationContext());
		ven_cursor=vendorCursor();
		ven_dir = getVenDir(this);
		tag_lst=new ArrayList<String>();
		ven_lst=new ArrayList<String>();
		ven_to_load = new ArrayList<String>();
		if (ven_dir.exists()) {			
			String[] f_arr = ven_dir.list();
			if (f_arr.length > 0) {
				for (int i=0; i<f_arr.length; i++) {
					tag_lst.add(f_arr[i]);
				}
			}
		}
		else {
			ven_dir.mkdirs();
		}
		
		if (ven_cursor != null && ven_cursor.moveToFirst()) {
			int id_ind = ven_cursor.getColumnIndex("vendor_id");
			while (ven_cursor.isAfterLast() != true) {
				int ven_id = ven_cursor.getInt(id_ind);
				ven_lst.add(Integer.toString(ven_id)+".jpg");
				ven_cursor.moveToNext();
			}
		}
		
		if (tag_lst.size() > 0) {
			for (int j=0; j<tag_lst.size(); j++) {
				if (!ven_lst.contains(tag_lst.get(j))) {					
					File del_tag = new File(ven_dir, tag_lst.get(j));
					if (del_tag.exists()) {
						del_tag.delete();
					}
				}
			}
		}
		
		if (ven_lst.size() > 0) {
			for (int k=0; k<ven_lst.size(); k++) {
				if (!tag_lst.contains(ven_lst.get(k))) {
					ven_to_load.add(ven_lst.get(k));
								
				}
			}
		}
		if (ven_to_load.size() > 0) {			
			String pa_url = VEN_BASE_URL+ven_to_load.get(0);
			File localCopy=new File(ven_dir, ven_to_load.get(0));
			if (!localCopy.exists()) {
				String local_path = localCopy.getAbsolutePath();
				venDownloadInfo(pa_url, local_path);
			}
		}
		else {
			shared_prefs = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
			Editor editor = shared_prefs.edit();
			editor.putString(LOAD_TOGGLE, "con");
			editor.apply();
			stopSelf();
		}
	}
	private Cursor vendorCursor() {
		 String p_query = String.format("SELECT * FROM %s", DatabaseHelper.TABLE_V);
		 return(dbh.getReadableDatabase().rawQuery(p_query, null));
	 }
	static File getVenDir(Context ctxt) {
		 return(new File(ctxt.getFilesDir(), VEN_BASEDIR));
	 }
	private void venDownloadInfo(String url, String path) {	
		PreferenceManager.getDefaultSharedPreferences(this).edit().putString(PREF_VEN_DIR, path).commit();
		DownloadManager mgr=(DownloadManager) getSystemService(DOWNLOAD_SERVICE);
		DownloadManager.Request req= new DownloadManager.Request(Uri.parse(url));
		Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdirs();
		req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE).setAllowedOverRoaming(false).setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, VEN_EXT);
		mgr.enqueue(req);
		 
	}
}
