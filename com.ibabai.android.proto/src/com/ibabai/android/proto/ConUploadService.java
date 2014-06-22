package com.ibabai.android.proto;

import java.io.File;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;

public class ConUploadService extends IntentService {
	private static final String IBABAI_CON_URL = "http://ibabai.picrunner.net/promo_content/0.zip";
	public static final String CON_EXT="con_ext.zip";
	public static final String CON_BASEDIR="promo_content";
	public static final String PREF_CON_DIR="pendingConDir";
	DatabaseHelper dbh;
	
	public ConUploadService() {
		super("ConUploadService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		conUploadInfo(IBABAI_CON_URL);				
		
		Intent ls_intent = new Intent(this, LocationService.class);
		startService(ls_intent);
	}
	
	static File getConDir(Context ctxt) {
		 return(new File(ctxt.getFilesDir(), CON_BASEDIR));
	 }
	private void conUploadInfo(String url) {
		 File localCopy=new File(getConDir(this), "0");
		 
		 if (!localCopy.exists()) {
			 PreferenceManager.getDefaultSharedPreferences(this).edit().putString(PREF_CON_DIR, localCopy.getAbsolutePath()).commit();
			 DownloadManager mgr=(DownloadManager) getSystemService(DOWNLOAD_SERVICE);
			 DownloadManager.Request req= new DownloadManager.Request(Uri.parse(url));
			 Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdirs();
			 req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE).setAllowedOverRoaming(false).setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, CON_EXT);
			 mgr.enqueue(req);
		 }
	}
}
