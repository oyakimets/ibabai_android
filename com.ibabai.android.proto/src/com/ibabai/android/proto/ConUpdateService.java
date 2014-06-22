package com.ibabai.android.proto;

import java.io.File;

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
	public static final String CON_BASE_EXT="con_ext.zip";
	public static final String CON_BASEDIR="promo_content";
	public static final String PREF_CON_DIR="pendingConDir";
	public static String CON_EXT;
	private Cursor pa_cursor;
	DatabaseHelper dbh;
	public ConUpdateService() {
		super("ConUpdateService");
		
		
	}
	@Override
	protected void onHandleIntent(Intent arg0) {
		dbh = DatabaseHelper.getInstance(getApplicationContext());
		pa_cursor=promoactCursor();
		if (pa_cursor != null && pa_cursor.moveToFirst()) {
			int id_ind = pa_cursor.getColumnIndex("promoact_id");			
			while (pa_cursor.isAfterLast() != true) {
				String pa_id=Integer.toString(pa_cursor.getInt(id_ind));
				String pa_url = CON_BASE_URL+pa_id+"/"+pa_id+".zip";
				CON_EXT = pa_id+CON_BASE_EXT;
				psDownloadInfo(pa_url, pa_id, CON_EXT );
				
			}
		}
	}
	private Cursor promoactCursor() {
		 String p_query = String.format("SELECT * FROM %s WHERE stopped=0", DatabaseHelper.TABLE_P);
		 return(dbh.getReadableDatabase().rawQuery(p_query, null));
	 }
	static File getConDir(Context ctxt) {
		 return(new File(ctxt.getFilesDir(), CON_BASEDIR));
	 }
	private void psDownloadInfo(String url, String id, String ext) {
		 File localCopy=new File(getConDir(this), id);
		 
		 if (!localCopy.exists()) {
			 PreferenceManager.getDefaultSharedPreferences(this).edit().putString(PREF_CON_DIR, localCopy.getAbsolutePath()).commit();
			 DownloadManager mgr=(DownloadManager) getSystemService(DOWNLOAD_SERVICE);
			 DownloadManager.Request req= new DownloadManager.Request(Uri.parse(url));
			 Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdirs();
			 req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE).setAllowedOverRoaming(false).setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, ext);
			 mgr.enqueue(req);
		 }
	}
}
