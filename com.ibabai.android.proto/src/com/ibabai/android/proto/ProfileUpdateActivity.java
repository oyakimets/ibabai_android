package com.ibabai.android.proto;

import java.io.IOException;
import java.util.Arrays;

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
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.savagelook.android.UrlJsonAsyncTask;

public class ProfileUpdateActivity extends Activity {
	public static final String PREFERENCES = "MyPrefs";	
	public static final String email = "email";
	public static final String phone = "phone";
	public static final String age = "age";
	public static final String gender="gender";	
	public static final String balance = "Balance";	
	private static final String pass = "";
	private static final String pass_conf="";
	private String s_email;
	private String s_phone;
	private String s_age;
	private String s_gender;
	SharedPreferences shared_prefs;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_update);
     
		ActionBar ab=getActionBar();
		ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		ab.setCustomView(R.layout.ab_profile);
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setHomeButtonEnabled(true);
		ab.setDisplayShowHomeEnabled(true);
		ab.setDisplayShowTitleEnabled(false);
		
		shared_prefs = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
		
        EditText te_mail = (EditText)findViewById(R.id.email_update);
        String st_mail = shared_prefs.getString(email, "");
        te_mail.setText(st_mail);
        
        EditText te_phone = (EditText)findViewById(R.id.phone_update);
        String st_phone = shared_prefs.getString(phone, "");
        te_phone.setText(st_phone);
        
        NumberPicker agePicker=(NumberPicker) findViewById(R.id.age_picker_update);
        String[] ap_str = this.setNumPick();
        String in_age = shared_prefs.getString("age", "");
        int ind = Arrays.asList(ap_str).indexOf(in_age);
        agePicker.setMaxValue(ap_str.length-1);
        agePicker.setMinValue(0);
        agePicker.setValue(ind); 
        agePicker.setDisplayedValues(ap_str);
        agePicker.setWrapSelectorWheel(false);        
        
        
        RadioGroup rg = (RadioGroup) findViewById(R.id.radioGender_update);
        String st_gender = shared_prefs.getString(gender, "");
        if(st_gender.equals("male")) {
        	rg.check(R.id.radioMale_update);
        }
        else {
        	rg.check(R.id.radioFemale_update);
        }
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
	
	public void SaveUpdate(View view) {
		EditText email=(EditText)findViewById(R.id.email_update);
		EditText phone=(EditText)findViewById(R.id.phone_update);		
		TextView genderLabel = (TextView) findViewById(R.id.gender_label);
		s_email = email.getText().toString();
		s_phone = phone.getText().toString();		
		NumberPicker np_age = (NumberPicker) findViewById(R.id.age_picker_update);
		String[] ap_str = this.setNumPick();
		int in_age = np_age.getValue();
		s_age = ap_str[in_age];
		RadioGroup genderSelector = (RadioGroup) findViewById(R.id.radioGender_update);
		int selected_id = genderSelector.getCheckedRadioButtonId();
		if (selected_id == R.id.radioMale_update) {
			s_gender = "male";
		}
		else  {
			s_gender = "female";
		}
		
		if ( s_email.length() >= 5 && s_phone.length() == 10 && selected_id != -1 ) {			
			
			ProfileUpdateFromApi();			
			
		}
		else {
			if (s_email.length() < 5) {
				email.setError("E-mail field error!");
			}
			if (s_phone.length() != 10) {
				phone.setError("Phone field error!");
			}
			
			if (selected_id == -1) {
				genderLabel.setError("Please select your gender");
			}
		}
	}
	private String[] setNumPick() {
		String[] strArray = new String[45];
		int n=16;
		for(int i=0; i<45; i++) {
			strArray[i] = Integer.toString(n);
			n+=1;
					
		}
		return strArray;
	}
	private class ProfileUpdateTask extends UrlJsonAsyncTask {
		public ProfileUpdateTask(Context ctxt) {
			super(ctxt);
		}
		@Override
	    protected JSONObject doInBackground(String... urls) {		
		
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPut put = new HttpPut(urls[0]);			
			JSONObject profile_json = new JSONObject();
			String response = null;
			JSONObject holder = new JSONObject();
			JSONObject json = new JSONObject();					
		
			try {
				try {
					json.put("success", false);
					json.put("info", "Something went wrong. Try again!");									
					
					profile_json.put(PasswordActivity.PASS, pass);
					profile_json.put(PasswordActivity.PASS_CONF, pass_conf);
					profile_json.put(email, s_email);
					profile_json.put(phone, s_phone);
					profile_json.put(age, s_age);
					profile_json.put(gender, s_gender);
					
					holder.put("customer", profile_json);
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
					e.putString("AuthToken", json.getJSONObject("data").getString("auth_token"));					
					e.putString(email, json.getJSONObject("data").getJSONObject("customer").getString("email"));
					e.putString(phone, json.getJSONObject("data").getJSONObject("customer").getString("phone"));
					e.putString(gender, json.getJSONObject("data").getJSONObject("customer").getString("gender"));
					e.putString(age, Integer.toString(json.getJSONObject("data").getJSONObject("customer").getInt("age")));
					e.apply();
					Toast.makeText(ProfileUpdateActivity.this, "Profile updated!", Toast.LENGTH_LONG).show();
					Intent i = new Intent(ProfileUpdateActivity.this, ProfileActivity.class);
					startActivity(i);
					finish();
					Log.v("PROFILE", "Profile updated!");
					
				}
				else {
					Log.e("Pass", json.getString("info"));
					Toast.makeText(ProfileUpdateActivity.this, "Profile update failed. Try again!", Toast.LENGTH_LONG).show();
				}	
			}
			catch(Exception e) {
				Toast.makeText(ProfileUpdateActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
			}
			finally {
				super.onPostExecute(json);
			}
		}
	}
	private void ProfileUpdateFromApi() {
		ProfileUpdateTask profile_update = new ProfileUpdateTask(this);	
		profile_update.setMessageLoading("Updating profile....");
		profile_update.execute(SignupActivity.REGISTER_API_ENDPOINT_URL+"?auth_token="+shared_prefs.getString("AuthToken", ""));	
	}
}
