package com.ibabai.android.proto;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;

public class DownloadCompleteReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context ctxt, Intent i) {
		File ps_update=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), ConUploadService.CON_EXT);
		if(ps_update.exists()) {
			ctxt.startService(new Intent(ctxt, conInstallService.class));
		}
	}
}
