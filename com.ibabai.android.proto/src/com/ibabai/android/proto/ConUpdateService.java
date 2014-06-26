package com.ibabai.android.proto;

import java.io.File;
import java.util.ArrayList;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;

public class ConUpdateService extends IntentService {
	private static final String CON_BASE_URL = "http://ibabai.picrunner.net/promo_content/";
	public static final String CON_EXT="con_ext.zip";
	public static final String CON_BASEDIR="promo_content";
	public static final String PREF_CON_DIR="pendingConDir";	
	private Cursor pa_cursor;
	private ArrayList<Integer> dir_lst;
	private ArrayList<Integer> promo_lst;
	private ArrayList<Integer> con_to_load;
	private File promos_folder;
	DatabaseHelper dbh;
	public ConUpdateService() {
		super("ConUpdateService");
		
		
	}
	@Override
	protected void onHandleIntent(Intent intent) {
		dbh = DatabaseHelper.getInstance(getApplicationContext());
		pa_cursor=promoactCursor();
		promos_folder = getConDir(this);
		dir_lst=new ArrayList<Integer>();
		promo_lst=new ArrayList<Integer>();
		con_to_load = new ArrayList<Integer>();
		if (promos_folder.exists()) {			
			String[] dir_arr = promos_folder.list();
			if (dir_arr.length > 0) {
				for (int i=0; i<dir_arr.length; i++) {
					dir_lst.add(Integer.parseInt(dir_arr[i]));
				}
			}
		}
		else {
			promos_folder.mkdirs();
		}
		
		if (pa_cursor != null && pa_cursor.moveToFirst()) {
			int id_ind = pa_cursor.getColumnIndex("promoact_id");
			while (pa_cursor.isAfterLast() != true) {
				int pa_id = pa_cursor.getInt(id_ind);
				promo_lst.add(pa_id);
				pa_cursor.moveToNext();
			}
		}
		
		if (dir_lst.size() > 0) {
			for (int j=0; j<dir_lst.size(); j++) {
				if (!promo_lst.contains(dir_lst.get(j))) {
					String dir_name = Integer.toString(dir_lst.get(j));
					File promo_dir = new File(promos_folder, dir_name);
					if (promo_dir.exists() && promo_dir.isDirectory()) {
						promo_dir.delete();
					}
				}
			}
		}
		
		if (promo_lst.size() > 0) {
			for (int k=0; k<promo_lst.size(); k++) {
				if (!dir_lst.contains(promo_lst.get(k))) {
					con_to_load.add(promo_lst.get(k));
								
				}
			}
		}
		if (con_to_load.size() > 0) {
			String dir_name=Integer.toString(con_to_load.get(0));
			String pa_url = CON_BASE_URL+dir_name+".zip";
			File localCopy=new File(promos_folder, dir_name);
			if (!localCopy.exists()) {
				String local_path = localCopy.getAbsolutePath();
				psDownloadInfo(pa_url, local_path);
			}
		}
		else {
			stopSelf();
		}
	}
	private Cursor promoactCursor() {
		 String p_query = String.format("SELECT * FROM %s", DatabaseHelper.TABLE_P);
		 return(dbh.getReadableDatabase().rawQuery(p_query, null));
	 }
	static File getConDir(Context ctxt) {
		 return(new File(ctxt.getFilesDir(), CON_BASEDIR));
	 }
	private void psDownloadInfo(String url, String path) {	
		PreferenceManager.getDefaultSharedPreferences(this).edit().putString(PREF_CON_DIR, path).commit();
		DownloadManager mgr=(DownloadManager) getSystemService(DOWNLOAD_SERVICE);
		DownloadManager.Request req= new DownloadManager.Request(Uri.parse(url));
		Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdirs();
		req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE).setAllowedOverRoaming(false).setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, CON_EXT);
		mgr.enqueue(req);
		 
	}
}
