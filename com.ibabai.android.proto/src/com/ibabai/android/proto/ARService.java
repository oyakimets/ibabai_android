package com.ibabai.android.proto;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

public class ARService extends Service {	
	IntentFilter broadcast_filter;
	private LocalBroadcastManager broadcast_manager;
	private ARRequester ar_requester;
	private ARRemover ar_remover;
	private ARReceiver ar_receiver;	
	public static final String PREFERENCES = "MyPrefs";
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		broadcast_manager = LocalBroadcastManager.getInstance(this);
		broadcast_filter = new IntentFilter(ARUtils.ACTION_ACTIVITY_RECOGNITION);
		broadcast_filter.addCategory(ARUtils.CATEGORY_AR_SERVICES);
		ar_receiver = new ARReceiver();
		broadcast_manager.registerReceiver(ar_receiver, broadcast_filter);
		ar_requester = new ARRequester(this);
		ar_remover = new ARRemover(this);		
		ar_requester.requestUpdates();
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {		
		Log.i(ARUtils.APPTAG, "Receive start id "+startId+": "+intent);
		return(START_STICKY);
	}
	@Override
	public IBinder onBind(Intent arg0) {
		
		return null;
	}
	@Override
	public void onDestroy() {
		try {
			
			ar_remover.removeUpdates(ar_requester.getRequestPendingIntent());
		}
		catch (UnsupportedOperationException e) {
			Log.e(ARUtils.APPTAG, getString(R.string.remove_ar_already_requested_error));
		}
		broadcast_manager.unregisterReceiver(ar_receiver);
		Intent gf_stop_intent = new Intent(this, gfService.class);
		stopService(gf_stop_intent);
	}
	public class ARReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context ctxt, Intent i) {
			String action = i.getAction();
			if (TextUtils.equals(action, ARUtils.ACTION_CONNECTION_ERROR)) {
				String connection_error = i.getStringExtra(ARUtils.EXTRA_CONNECTION_ERROR_CODE);
				Log.e(ARUtils.APPTAG, connection_error);
				String connection_error_type = i.getStringExtra(ARUtils.EXTRA_CONNECTION_REQUEST_TYPE);
				if (connection_error_type.equals("ADD")) {
					Log.e(ARUtils.APPTAG, "AR connection error on ADD");
					Intent ls_intent = new Intent(ctxt, LocationService.class);
					startService(ls_intent);
					stopSelf();
				}
				else {
					Log.e(ARUtils.APPTAG, "AR connection error on REMOVE");
				}
			}
		}
	}
}
