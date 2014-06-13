package com.ibabai.android.proto;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.TextView;

public class SignupActivity extends Activity {
	public static final String PREFERENCES = "MyPrefs";
	public static final String status = "SignedUp";
	public static final String username = "email";
	public static final String telephone = "phone #";
	public static final String age = "age";
	public static final String gender="gender";
	public static final String city="city";
	private DbLoadTask db_load = null;	
	Location current_loc;
	DatabaseHelper dbh;
	SharedPreferences shared_prefs;		
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.signup);
		
		ActionBar ab = getActionBar(); 
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setCustomView(R.layout.ab_signup);
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayShowTitleEnabled(false);     
        
        
        NumberPicker agePicker=(NumberPicker) findViewById(R.id.age_picker);
        String[] ap_str = this.setNumPick();
        agePicker.setMaxValue(ap_str.length-1);
        agePicker.setMinValue(0);
        agePicker.setValue(0); 
        agePicker.setDisplayedValues(ap_str);
        agePicker.setWrapSelectorWheel(false);
        
        GPSTracker gps = new GPSTracker(this);
        current_loc = gps.getLocation();             
        dbh = DatabaseHelper.getInstance(getApplicationContext());
        db_load=new DbLoadTask();
        db_load.execute();
        
	}
	
	public void AccountCreate(View view) {
		EditText email=(EditText)findViewById(R.id.email);
		EditText phone=(EditText)findViewById(R.id.phone);
		EditText password=(EditText)findViewById(R.id.password);
		EditText password_confirmation=(EditText)findViewById(R.id.password_confirmation);
		TextView genderLabel = (TextView) findViewById(R.id.gender_label);
		TextView ageLabel = (TextView) findViewById(R.id.age_label);
		String s_email = email.getText().toString();
		String s_phone = phone.getText().toString();
		String s_password = password.getText().toString();
		String s_confirmation = password_confirmation.getText().toString();
		NumberPicker np_age = (NumberPicker) findViewById(R.id.age_picker);
		String[] ap_str = this.setNumPick();		
		int in_age = np_age.getValue();
		String str_age = ap_str[in_age]; 
		RadioGroup genderSelector = (RadioGroup) findViewById(R.id.radioGender);
		int selected_id = genderSelector.getCheckedRadioButtonId();
		
		if ( s_email.length() >= 5 && s_phone.length() == 10 && s_password.length() >= 6 && s_password.equals(s_confirmation) && selected_id != -1 && str_age != "spin") {
			shared_prefs = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
			Editor editor = shared_prefs.edit();
			editor.putBoolean(status, true);
			editor.apply();	
			
			editor.putString(username, s_email).apply();			
			editor.putString(telephone, s_phone).apply();			
			editor.putString(age, str_age).apply();
			
			
			if (selected_id == R.id.radioMale) {
				editor.putString(gender, "Male").apply();
			}
			else {
				editor.putString(gender, "Female").apply();
			}
			Intent s_intent = new Intent(this, LocationService.class);
			startService(s_intent);
			Intent i=new Intent(this, CoreActivity.class);
			startActivity(i);
			finish();
		}
		else {
			if (s_email.length() < 5) {
				email.setError("E-mail field error!");
			}
			if (s_phone.length() != 10) {
				phone.setError("Phone field error!");
			}
			if (s_password.length() < 6) {
				password.setError("No less than 6 characters!");
			}
			if (!s_password.equals(s_confirmation)) {
				password_confirmation.setError("Passwords do not match!");
			}			
			if (selected_id == -1) {
				genderLabel.setError("Please select your gender");
			}
			if (str_age == "spin") {
				ageLabel.setError("Please set your age");
			}
		}
		
	}
	private String[] setNumPick() {
		String[] strArray = new String[46];
		int n=16;
		for(int i=0; i<46; i++) {
			if (i==0) {
				strArray[i] = "spin";
			}
			else {
				strArray[i] = Integer.toString(n);
				n+=1;
			}		
		}
		return strArray;
	}
	public static <T> void executeAsyncTask(AsyncTask<T, ?, ?> task, T... params) {
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
	}
	private class DbLoadTask extends AsyncTask<ContentValues, Void, Void> {		
		 
		 @Override
		 protected Void doInBackground(ContentValues... cv) {		 
			 
			 try {
				StringBuilder buf=new StringBuilder();
				InputStream json=getAssets().open("data/cities.json"); 	
				BufferedReader in = new BufferedReader(new InputStreamReader(json));
				String str;
				while((str=in.readLine()) != null ) {
					buf.append(str);
				}				
				in.close();
				
				JSONArray ja=new JSONArray(buf.toString());				
				for(int i=0; i<ja.length(); i++) {					
					JSONObject c_jo = ja.optJSONObject(i);
					City c = new City(c_jo);
					dbh.AddCity(c);						 
				}	
								 
			}				 
			catch(Exception e) {
				e.printStackTrace();
			}
			try {
				StringBuilder v_buf=new StringBuilder();
				InputStream v_json=getAssets().open("data/vendors/vendors.json"); 	
				BufferedReader v_in = new BufferedReader(new InputStreamReader(v_json));
				String v_str;
				while((v_str=v_in.readLine()) != null ) {
					v_buf.append(v_str);
				}				
				v_in.close();				
				JSONArray vja=new JSONArray(v_buf.toString());				
				for(int j=0; j<vja.length(); j++) {					
					JSONObject v_jo = vja.optJSONObject(j);
					Vendor v = new Vendor(v_jo);
					dbh.AddVendor(v);					 
				}					
				 
			}			
			catch(Exception e) {
				e.printStackTrace();
			}			
			 
			 return null;
		 }		 
		 
		 @Override 
		 public void onPostExecute(Void result) {
			 super.onPostExecute(result);			 
			 
			 Cursor new_city_c = cityCursor();
			 int city_id_ind=new_city_c.getColumnIndex(DatabaseHelper.C_ID);
		 	 int lat_ind=new_city_c.getColumnIndex(DatabaseHelper.LAT);
		 	 int lon_ind=new_city_c.getColumnIndex(DatabaseHelper.LON);
		 	 int rad_ind=new_city_c.getColumnIndex(DatabaseHelper.RAD);
			 if (new_city_c != null) {
				 new_city_c.moveToFirst();
				 while(new_city_c.isAfterLast()!=true) {					 
				 	 int city_id=new_city_c.getInt(city_id_ind);
				 	 double latitude=new_city_c.getDouble(lat_ind);
				 	 double longitude=new_city_c.getDouble(lon_ind);
				 	 int radius=new_city_c.getInt(rad_ind);
				 	 Location location = new Location("city");
				 	 location.setLatitude(latitude);
				 	 location.setLongitude(longitude);
				 	 float distance=current_loc.distanceTo(location);
				 	 if (distance <= radius) {
				 		shared_prefs=getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
				 		Editor editor=shared_prefs.edit();
				 		editor.putInt(city, city_id);
				 		editor.apply();
				 		break;
				 	 }
				 	 new_city_c.moveToNext();
			 	}
			 }
			 dbh.close();
			 new_city_c.close();
		     return;  
		 }
		 private Cursor cityCursor() {
			 String c_query = String.format("SELECT * FROM %s", DatabaseHelper.TABLE_C);
			 return(dbh.getReadableDatabase().rawQuery(c_query, null));
		 }
	 }	
}
