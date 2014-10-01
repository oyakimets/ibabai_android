package com.ibabai.android.proto;

import java.io.IOException;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.TextView;


public class TransacDebitService extends IntentService {
	private static final int NOTIFY_ID = 1010;	
	private static final String EXTRA_DC_FLAG = "dc_flag";			
	private String c_name;
	private int c_id;	
	private int amount;
	private String phone;
	private String account;
	TextView tv_balance;
	DatabaseHelper dbh;
	SharedPreferences shared_prefs;
	public TransacDebitService() {
		super("TransacDebitService");
	}
	@Override
	protected void onHandleIntent(Intent intent) {
		dbh = DatabaseHelper.getInstance(getApplicationContext());		
		c_name = (String)intent.getExtras().get("v_name");
		phone = (String)intent.getExtras().get("ph");
		account = (String)intent.getExtras().get("acc");
		c_id = (Integer)intent.getExtras().get("v_id");
		amount = Integer.parseInt((String)intent.getExtras().get("amnt"));
		shared_prefs=getSharedPreferences(IbabaiUtils.PREFERENCES, Context.MODE_PRIVATE);
		DebitRegisterAction(IbabaiUtils.PAYMENT_API_ENDPOINT_URL+"?auth_token="+shared_prefs.getString(IbabaiUtils.AUTH_TOKEN, ""));
	}
	private void DebitRegisterAction(String url) {
			
		DefaultHttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(url);
		JSONObject holder = new JSONObject();
		JSONObject pay_json = new JSONObject();
		String response = null;
		JSONObject json = new JSONObject();			
		try {
			try {
				json.put("success", false);
				json.put("info", "Something went wrong. Try again!");					
										
				pay_json.put(IbabaiUtils.AGENT_ID, c_id);
				pay_json.put(IbabaiUtils.AGENT_NAME, c_name);
				pay_json.put(EXTRA_DC_FLAG, "D");					
				pay_json.put(IbabaiUtils.AMOUNT, amount);
				pay_json.put(IbabaiUtils.PHONE, phone);
				pay_json.put(IbabaiUtils.ACCOUNT, account);
				holder.put("transaction", pay_json);
				StringEntity se = new StringEntity(holder.toString());
				post.setEntity(se);
					
				post.setHeader("Accept", "application/json");
				post.addHeader("Content-Type", "application/json");
					
				ResponseHandler<String> r_handler = new BasicResponseHandler();
				response = client.execute(post, r_handler);
				json = new JSONObject(response);					
			}
			catch (HttpResponseException ex) {
				ex.printStackTrace();
				Log.e("ClientProtocol", ""+ex);
				json.put("info", "Response Error");
			}
			catch (IOException ex) {
				ex.printStackTrace();
				Log.e("IO", ""+ex);
			}
		}
		catch (JSONException ex) {
			ex.printStackTrace();
			Log.e("JSON", ""+ex);
		}
		if (json != null) {
			
			try {
				if (json.getBoolean("success")) {
					String new_balance = Integer.toString(json.getJSONObject("data").getInt("balance")); 
					Editor e = shared_prefs.edit();					 
					e.putString(IbabaiUtils.BALANCE, new_balance);
					e.apply();
					dbh.addLogEntry(c_name, Integer.toString(amount), "D");					
					Log.v("DEBIT", "Account deited");
					
				}
				else {
					raiseNotification(this, null);	
					Log.e("DEBIT", json.getString("info"));
					/* launch "payment failed" sms/email
					 * 
					 */
				}	
			}
			catch(Exception e) {
				Log.e("SCAN", "Post execute exception");
			}			
		}
	}
	private void raiseNotification(Context ctxt, Exception e) {
		NotificationCompat.Builder b=new NotificationCompat.Builder(ctxt);

		b.setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL).setWhen(System.currentTimeMillis());
		Bitmap bm = BitmapFactory.decodeResource(ctxt.getResources(), R.drawable.ic_launcher);
		if (e == null) {
			b.setContentTitle("Error warning!").setContentText("You payment order was not executed. try again!").setSmallIcon(android.R.drawable.ic_menu_info_details).setTicker("ibabai").setLargeIcon(bm);

			Intent outbound=new Intent(ctxt, PaymentActivity.class);			

			b.setContentIntent(PendingIntent.getActivity(ctxt, 0, outbound, 0));
		}
		else {
			b.setContentTitle("Sorry").setContentText(e.getMessage()).setSmallIcon(android.R.drawable.stat_notify_error).setTicker("ibabai");
		}

		NotificationManager mgr=(NotificationManager)ctxt.getSystemService(Context.NOTIFICATION_SERVICE);

		mgr.notify(NOTIFY_ID, b.build());
	}	
}

