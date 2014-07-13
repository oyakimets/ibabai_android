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
import android.widget.Toast;
import com.savagelook.android.UrlJsonAsyncTask;

public class ProfileDialogFragment extends DialogFragment {
	public final static String VALID_API_ENDPOINT_URL="http://192.168.1.102:3000/api/v1/validations.json";
	public static final String C_ID = "customer_id";
	private View form=null;
	private AlertDialog profile_dialog=null;
	public static final String EMAIL = "email";
	private static final String PASS = "password";
	private String email;
	private String pass="";
	private String u_id;	
	SharedPreferences shared_prefs;
	public static final String PREFERENCES = "MyPrefs";
	
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		shared_prefs = getActivity().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);		
		form = getActivity().getLayoutInflater().inflate(R.layout.profile_dialog, null);
		
		OnClickListener neutralClick = new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dismiss();
				Toast.makeText(getActivity().getBaseContext(), "Check your email for instructions", Toast.LENGTH_LONG).show();
				
			}
		};
		OnClickListener negativeClick = new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dismiss();
			}
		};		
		
		AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
		AlertDialog dialog = builder.setView(form).setPositiveButton("Done", null).setNeutralButton("Forgot?", neutralClick).setNegativeButton("Cancel", negativeClick).create(); 
		profile_dialog=dialog;
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface d) {				
				Button bp = profile_dialog.getButton(AlertDialog.BUTTON_POSITIVE);
				bp.setTextSize(20);
				bp.setBackgroundResource(R.drawable.d_button);
				Button bn = profile_dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
				bn.setTextSize(20);
				bn.setBackgroundResource(R.drawable.d_button);
				Button bneu = profile_dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
				bneu.setTextSize(20);
				bneu.setBackgroundResource(R.drawable.d_button);
				
				bp.setOnClickListener(new View.OnClickListener() {
										
					@Override
					public void onClick(View v) {
						EditText et = (EditText)form.findViewById(R.id.pr_password);
						pass = et.getText().toString();
						if (pass.length()==0) {
							et.setError("Password can not be empty!");
							
						}
						else {
							/*Intent i = new Intent(getActivity(), ProfileActivity.class);
							 *startActivity(i);
							 *profile_dialog.dismiss();							 
							 */
							 validFromApi();
							 
						}
						
					}
				});
			}
		});
		return(profile_dialog);
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
			email = shared_prefs.getString("email", null);			
		
			try {
				try {
					json.put("success", false);
					json.put("info", "Something went wrong. Try again!");
					
					val_json.put(C_ID, u_id);				
					val_json.put(EMAIL, email);
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
					
					Intent i = new Intent(getActivity(), ProfileActivity.class);
					startActivity(i);
					Log.v("VALIDATION", "Validation confirmed");
					profile_dialog.dismiss();
				}
				else {
					Log.e("VALIDATION", json.getString("info"));
					Toast.makeText(getActivity(), "Password is not valid. Try again!", Toast.LENGTH_LONG).show();
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
