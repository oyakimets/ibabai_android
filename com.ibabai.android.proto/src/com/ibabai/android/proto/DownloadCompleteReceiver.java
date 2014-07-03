package com.ibabai.android.proto;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class DownloadCompleteReceiver extends BroadcastReceiver {
	public static final String PREFERENCES = "MyPrefs";
	public static final String LOAD_TOGGLE = "load_toggle";
	SharedPreferences shared_prefs;

	@Override
	public void onReceive(Context ctxt, Intent i) {
		shared_prefs = ctxt.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
		String toggle = shared_prefs.getString(LOAD_TOGGLE, "con");
		if (toggle.equals("ven")) {
			File ven_update = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), VenUpdateService.VEN_EXT);
			if (ven_update.exists()) {
				WakefulIntentService.sendWakefulWork(ctxt, venInstallService.class);
			}
		}
		else {
			File con_update=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), ConUpdateService.CON_EXT);
			if(con_update.exists()) {
				WakefulIntentService.sendWakefulWork(ctxt, conInstallService.class);
			}
		}
	}
}
