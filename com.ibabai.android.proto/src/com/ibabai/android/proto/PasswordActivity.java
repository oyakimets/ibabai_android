package com.ibabai.android.proto;

import java.io.IOException;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.savagelook.android.UrlJsonAsyncTask;

public class PasswordActivity extends Activity {	
	private String pass;
	private String pass_conf;
	private String s_email;
	private String s_phone;
	private String s_age;
	private String s_gender;
	SharedPreferences shared_prefs;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pass_activity);
     
		ActionBar ab=getActionBar();
		ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		ab.setCustomView(R.layout.ab_password);
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setHomeButtonEnabled(true);
		ab.setDisplayShowHomeEnabled(true);
		ab.setDisplayShowTitleEnabled(false);
		
		shared_prefs = getSharedPreferences(IbabaiUtils.PREFERENCES, Context.MODE_PRIVATE);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.core, menu);		
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.action_scan:
			Intent i=new Intent(this, ScanActivity.class);
			startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);
			
		}		
	}
	
	public void PasswordChange(View v) {
		EditText pass_et = (EditText)findViewById(R.id.pass);
		EditText conf_et = (EditText)findViewById(R.id.pass_confirmation);
		pass = pass_et.getText().toString();
		pass_conf = conf_et.getText().toString();
		s_age = shared_prefs.getString(IbabaiUtils.AGE, "");
		s_phone = shared_prefs.getString(IbabaiUtils.PHONE, "");
		s_email = shared_prefs.getString(IbabaiUtils.EMAIL, "");
		s_gender = shared_prefs.getString(IbabaiUtils.GENDER, "");
		
		
		if (pass.length() >= 6 && pass.equals(pass_conf)) {
			
			passChangeFromApi();
			
		}
		else {
			if(pass.length()<6) {
				pass_et.setError("No less than 6 characters.");
			}
			else if (!pass.equals(pass_conf)) {
				conf_et.setError("Inputs do not match");
			}
		}		
	}
	private class PassChangeTask extends UrlJsonAsyncTask {
		public PassChangeTask(Context ctxt) {
			super(ctxt);
		}
		@Override
	    protected JSONObject doInBackground(String... urls) {		
		
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPut put = new HttpPut(urls[0]);			
			JSONObject pass_json = new JSONObject();
			String response = null;
			JSONObject holder = new JSONObject();
			JSONObject json = new JSONObject();					
		
			try {
				try {
					json.put("success", false);
					json.put("info", "Something went wrong. Try again!");									
					
					pass_json.put(IbabaiUtils.PASS, pass);
					pass_json.put(IbabaiUtils.PASS_CONF, pass_conf);
					pass_json.put(IbabaiUtils.EMAIL, s_email);
					pass_json.put(IbabaiUtils.AGE, s_age);
					pass_json.put(IbabaiUtils.PHONE, s_phone);
					pass_json.put(IbabaiUtils.GENDER, s_gender);
					holder.put("customer", pass_json);
					StringEntity se = new StringEntity(holder.toString());
					put.setEntity(se);
					
					put.setHeader("Accept", "application/json");
					put.addHeader("Content-Type", "application/json");
					
					ResponseHandler<String> r_handler = new BasicResponseHandler();
					response = client.execute(put, r_handler);
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
					Editor e = shared_prefs.edit();
					e.putString(IbabaiUtils.AUTH_TOKEN, json.getJSONObject("data").getString("auth_token"));
					e.apply();
					Toast.makeText(PasswordActivity.this, "Password saved!", Toast.LENGTH_LONG).show();
					Intent i = new Intent(PasswordActivity.this, CoreActivity.class);
					startActivity(i);
					Log.v("PASS", "Password change confirmed");
					
				}
				else {
					Log.e("Pass", json.getString("info"));
					Toast.makeText(PasswordActivity.this, "Password change failed. Try again!", Toast.LENGTH_LONG).show();
				}	
			}
			catch(Exception e) {
				Toast.makeText(PasswordActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
			}
			finally {
				super.onPostExecute(json);
			}
		}
	}
	private void passChangeFromApi() {
		PassChangeTask pass_update = new PassChangeTask(this);
		pass_update.setMessageLoading("Saving password...");		
		pass_update.execute(IbabaiUtils.REGISTER_API_ENDPOINT_URL+"?auth_token="+shared_prefs.getString(IbabaiUtils.AUTH_TOKEN, ""));	
	}
}
