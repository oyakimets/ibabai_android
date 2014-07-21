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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.savagelook.android.UrlJsonAsyncTask;


public class PaymentDialogFragment extends DialogFragment {	
	public final static String VALID_API_ENDPOINT_URL="http://192.168.1.100:3000/api/v1/debits.json";
	private View form=null;
	private AlertDialog payment_dialog=null;
	public static final String PREFERENCES = "MyPrefs";
	public static final String balance = "Balance";
	public static final String AGENT_ID = "agent_id";
	public static final String TYPE = "type";
	public static final String AGENT_NAME = "agent_name";
	public static final String AMOUNT = "amount";
	public static final String ACCOUNT = "account";
	public static final String PHONE = "phone";
	private String pass;	
	private String vendor_name;	
	public static final String PASS = "password";
	private String st_acc;
	private String st_phn;
	private String st_amount;
	private int _id;
	SharedPreferences shared_prefs;	
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		form = getActivity().getLayoutInflater().inflate(R.layout.dialog_payment, null);
		TextView tv_acc = (TextView) form.findViewById(R.id.dialog_account);
		TextView tv_phn = (TextView) form.findViewById(R.id.dialog_phone);
		TextView tv_amount = (TextView) form.findViewById(R.id.dialog_amount);
		st_acc = getArguments().getString("dialog_acc");
		st_phn = getArguments().getString("dialog_phn");
		st_amount = getArguments().getString("dialog_amnt");
		_id = getArguments().getInt("dialog_id");
		vendor_name = getArguments().getString("dialog_agent");
		shared_prefs = getActivity().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);		
		OnClickListener negativeClick = new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dismiss();
			}
		};
		
		tv_acc.setText(st_acc);
		tv_phn.setText(st_phn);
		tv_amount.setText(st_amount);
		AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
		AlertDialog dialog = builder.setView(form).setPositiveButton(android.R.string.ok, null).setNegativeButton(android.R.string.cancel, negativeClick).create(); 
		payment_dialog=dialog;
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface d) {				
				Button bp = payment_dialog.getButton(AlertDialog.BUTTON_POSITIVE);
				bp.setTextSize(20);
				bp.setBackgroundResource(R.drawable.d_button);
				Button bn = payment_dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
				bn.setTextSize(20);
				bn.setBackgroundResource(R.drawable.d_button);
				
				bp.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						EditText et = (EditText)payment_dialog.findViewById(R.id.dialog_password);
						pass = et.getText().toString();
						if (pass.length() == 0) {
							et.setError("Enter your password");
						}
						else {
							
							validFromApi();
						}
					}
				});
			}
		});
		return(payment_dialog);
	}
	
	private class PassValidTask extends UrlJsonAsyncTask {
		public PassValidTask(Context ctxt) {
			super(ctxt);
		}
		@Override
	    protected JSONObject doInBackground(String... urls) {		
		
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(urls[0]);
			JSONObject holder = new JSONObject();
			JSONObject val_json = new JSONObject();
			String response = null;
			JSONObject json = new JSONObject();						
		
			try {
				try {
					json.put("success", false);
					json.put("info", "Something went wrong. Try again!");					
								
					val_json.put(AMOUNT, Integer.parseInt(st_amount));
					val_json.put(PASS, pass);					
					holder.put("credentials", val_json);
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
					json.put("info", "Password is invalid. Try again!");
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
			
			return json;
		}
		@Override
		protected void onPostExecute(JSONObject json) {
			try {
				if (json.getBoolean("success")) {
					
					Intent i = new Intent(getActivity(), TransacDebitService.class);
					i.putExtra("acc", st_acc);
					i.putExtra("ph", st_phn);
					i.putExtra("v_id", _id);
					i.putExtra("v_name", vendor_name);
					i.putExtra("amnt", st_amount);
					getActivity().startService(i);
					Intent in = new Intent(getActivity(), CoreActivity.class);
					startActivity(in);
					Log.v("VALIDATION", "Validation confirmed");
					payment_dialog.dismiss();
					Toast.makeText(getActivity().getBaseContext(), "Payment order is accepted!", Toast.LENGTH_LONG).show();
				}				
				else {
					Log.e("VALIDATION", json.getString("info"));
					Toast.makeText(getActivity().getBaseContext(), json.getString("info"), Toast.LENGTH_LONG).show();
				}	
			}
			catch(Exception e) {
				Toast.makeText(getActivity().getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
			}
			finally {
				super.onPostExecute(json);
			}
		}
	}
	private void validFromApi() {
		PassValidTask pass_validation = new PassValidTask(getActivity());
		pass_validation.setMessageLoading("Just a moment...");		
		pass_validation.execute(VALID_API_ENDPOINT_URL+"?auth_token="+shared_prefs.getString("AuthToken", ""));	
	}	
}
