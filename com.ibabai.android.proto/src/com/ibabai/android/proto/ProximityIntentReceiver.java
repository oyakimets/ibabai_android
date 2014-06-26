package com.ibabai.android.proto;

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class ProximityIntentReceiver extends BroadcastReceiver {
	public static final String PREFERENCES = "MyPrefs";	
	public static final String store_id = "store_id";
	SharedPreferences shared_prefs;
	private static final int NOTIFY_ID = 1000;
	public static ArrayList<String> dbPromos;
	public static ArrayList<String> storePromos;
	private Cursor pa_cursor;
	private Cursor ps_cursor;
	private Cursor pa_update_cursor;
	DatabaseHelper dbh;

	@Override
	public void onReceive(Context ctxt, Intent intent) {
		dbh = DatabaseHelper.getInstance(ctxt);
		String key = LocationManager.KEY_PROXIMITY_ENTERING;
		int s_id = intent.getIntExtra("store_id", 0);
		Boolean entering = intent.getBooleanExtra(key, false);
		shared_prefs = ctxt.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
		Editor editor = shared_prefs.edit();
		if (entering) {			
			editor.putInt(store_id, s_id);
			editor.apply();	
			Log.d(getClass().getSimpleName(), "entering");
			raiseNotification(ctxt, null);
			updateDelivery(s_id);
		}
		else {
			editor.putInt(store_id, 0);
			editor.apply();	
			Log.d(getClass().getSimpleName(), "exiting");
		}
		
	}
	private void raiseNotification(Context ctxt, Exception e) {
		NotificationCompat.Builder b=new NotificationCompat.Builder(ctxt);

		b.setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL).setWhen(System.currentTimeMillis());
		Bitmap bm = BitmapFactory.decodeResource(ctxt.getResources(), R.drawable.ic_launcher);
		if (e == null) {
			b.setContentTitle("Hello!").setContentText("You have offers from IBABAI!").setSmallIcon(android.R.drawable.ic_menu_info_details).setTicker("ibabai").setLargeIcon(bm);

			Intent outbound=new Intent(ctxt, CoreActivity.class);			

			b.setContentIntent(PendingIntent.getActivity(ctxt, 0, outbound, 0));
		}
		else {
			b.setContentTitle("Sorry").setContentText(e.getMessage()).setSmallIcon(android.R.drawable.stat_notify_error).setTicker("ibabai");
		}

		NotificationManager mgr=(NotificationManager)ctxt.getSystemService(Context.NOTIFICATION_SERVICE);

		mgr.notify(NOTIFY_ID, b.build());
	}
	private void updateDelivery(int store) {		
		dbPromos=new ArrayList<String>();
		storePromos=new ArrayList<String>();
		pa_cursor=promoactCursor();
		if (pa_cursor != null && pa_cursor.moveToFirst()) {
			int id_ind = pa_cursor.getColumnIndex("promoact_id");
			while (!pa_cursor.isAfterLast()) {
				String pa_id = Integer.toString(pa_cursor.getInt(id_ind));						
				dbPromos.add(pa_id);
				pa_cursor.moveToNext();
			}
			pa_cursor.close();
		}
		ps_cursor = storePromosCursor(store);
		if(ps_cursor != null && ps_cursor.moveToFirst()) {
			int paid_ind = ps_cursor.getColumnIndex("promoact_id");
			while (!ps_cursor.isAfterLast()) {
				String promoact_id=Integer.toString(ps_cursor.getInt(paid_ind));
				if (dbPromos.contains(promoact_id)) {
					storePromos.add(promoact_id);
				}
				ps_cursor.moveToNext();
			}
			ps_cursor.close();
		}
		for (int i=0; i<storePromos.size(); i++) {
			String pa_id = storePromos.get(i);
			pa_update_cursor=getPromoCursor(pa_id);
			if (pa_update_cursor != null && pa_update_cursor.moveToFirst()) {
				int mult_ind = pa_update_cursor.getColumnIndex(DatabaseHelper.MULT);
				int del_ind = pa_update_cursor.getColumnIndex(DatabaseHelper.DEL);
				int multiple = pa_update_cursor.getInt(mult_ind);
				int delivery = pa_update_cursor.getInt(del_ind);
				pa_update_cursor.close();
				int count = delivery+1;
				/*http patch request to server
				 * 
				 */
				dbh.updateDelivery(pa_id, count);
				if (count == multiple) {
					dbh.paStopUpdate(pa_id, 1);
				}
			}
		}
		dbh.close();
	}
	private Cursor storePromosCursor(int store_id) {		
		String ps_query= "SELECT * FROM promo_stores WHERE store_id="+Integer.toString(store_id);
		return (dbh.getReadableDatabase().rawQuery(ps_query, null));
	}
	private Cursor promoactCursor() {
		 String p_query = String.format("SELECT * FROM %s WHERE stopped=0", DatabaseHelper.TABLE_P);
		 return(dbh.getReadableDatabase().rawQuery(p_query, null));
	 }
	private Cursor getPromoCursor(String id) {
		 String p_query = String.format("SELECT * FROM %s WHERE promoact_id =" + id, DatabaseHelper.TABLE_P);
		 return(dbh.getReadableDatabase().rawQuery(p_query, null));
	 }
}
