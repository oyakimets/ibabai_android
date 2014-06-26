package com.ibabai.android.proto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StopListService extends IntentService {	
	
	public StopListService() {
		super("StopListService");
	}
	@Override
	protected void onHandleIntent(Intent i) {
		String pa_id = (String) i.getExtras().get(CoreActivity.EXTRA_NI);
		String cl_id = (String) i.getExtras().get(stopListActivity.EXTRA_CL);
		File dir_src = new File(getPromoDir(this), pa_id);			
		File src = new File(dir_src, "client.jpg");
		Log.v("CLIENT", src.getAbsolutePath());
		File sl_dir = getStopDir(this);
		if (!sl_dir.exists()) {
			sl_dir.mkdirs();
		}
		String path = cl_id+"_client.jpg";
		File dst= new File(sl_dir, path);		
		Log.v("CLIENT", dst.getAbsolutePath());
		try {
			dst.createNewFile();
			CopyClient(src, dst);
		}
		catch (IOException ex) {
				Log.e("STOP", ex.toString());
				
		}					

	}
	public void CopyClient(File src, File dst) throws IOException {
		FileChannel in = null;
		FileChannel out=null;
		try {
			in = new FileInputStream(src).getChannel();
			out = new FileOutputStream(dst).getChannel();
			out.transferFrom(in, 0, in.size());
			
		} finally {
			in.close();
			out.close();
		}
	}
	
	static File getStopDir(Context ctxt) {
		 return(new File(ctxt.getFilesDir(), stopListActivity.SL_BASEDIR));
	 }
	
	static File getPromoDir(Context ctxt) {
		 return(new File(ctxt.getFilesDir(), ConUpdateService.CON_BASEDIR));
	 }
}
