package com.ibabai.android.proto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class venInstallService extends com.commonsware.cwac.wakeful.WakefulIntentService {
	public venInstallService() {
		super("venInstallService");
	}
	@Override
	protected void doWakefulWork(Intent intent) {
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		String ven_dir_path=prefs.getString(VenUpdateService.PREF_VEN_DIR, null);
		if (ven_dir_path != null) {
			File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			File update=new File(root, VenUpdateService.VEN_EXT);
			try {
				File dest_tag = new File(ven_dir_path);				
				if (!dest_tag.exists()) {
					dest_tag.createNewFile();
				}
				upload(update, dest_tag);
			}
			catch (IOException e) {
				Log.e(getClass().getSimpleName(), "Exception unzipping update");
			}
			update.delete();
			WakefulIntentService.sendWakefulWork(this, VenUpdateService.class);			
			
		}
		else {
			Log.e(getClass().getSimpleName(), "null content dir path");
		}
	}
	private static void upload(File src, File dest) throws IOException {
		InputStream is = null;
		OutputStream out = null;
		try {		
			is = new FileInputStream(src);
			out = new FileOutputStream(dest); 
			byte[] buf = new byte[1024];
			int count;
		    while ((count=is.read(buf)) != -1) {
					out.write(buf, 0, count);
			}
			out.flush();
		}
		finally {
			is.close();
			out.close();
		}			
	}
}
