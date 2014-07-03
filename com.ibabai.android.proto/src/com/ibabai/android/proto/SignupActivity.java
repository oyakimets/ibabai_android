package com.ibabai.android.proto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
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
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.savagelook.android.UrlJsonAsyncTask;

public class SignupActivity extends Activity {
	private final static String REGISTER_API_ENDPOINT_URL="http://192.168.1.102:3000/api/v1/registrations";
	public static final String PREFERENCES = "MyPrefs";	
	public static final String email = "email";
	public static final String phone = "phone";
	public static final String age = "age";
	public static final String gender="gender";
	public static final String city="city";
	public static final String store_id="store_id";
	public static final String user_id="user_id";	
	private DbLoadTask db_load = null;	
	Location current_loc;
	private String s_email;
	private String s_password;
	private String s_confirmation;
	private String s_phone;
	private String s_gender;
	private String s_age;
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
        
        shared_prefs=getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
 		Editor editor=shared_prefs.edit();
 		editor.putInt(city, 0);
 		editor.putInt(store_id, 0);
 		editor.apply();
        
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
		s_email = email.getText().toString();
		s_phone = phone.getText().toString();
		s_password = password.getText().toString();
		s_confirmation = password_confirmation.getText().toString();
		NumberPicker np_age = (NumberPicker) findViewById(R.id.age_picker);
		String[] ap_str = this.setNumPick();		
		int in_age = np_age.getValue();
		s_age = ap_str[in_age]; 
		RadioGroup genderSelector = (RadioGroup) findViewById(R.id.radioGender);
		int selected_id = genderSelector.getCheckedRadioButtonId();
		
		if ( s_email.length() >= 5 && s_phone.length() == 10 && s_password.length() >= 6 && s_password.equals(s_confirmation) && selected_id != -1 && s_age != "spin") {
			
			if (selected_id == R.id.radioMale) {
				s_gender = "male";
			}
			else {
				s_gender = "female";
			}					
			 
			RegisterTask register = new RegisterTask(this);
			register.setMessageLoading("Creating account...");
			register.execute(REGISTER_API_ENDPOINT_URL);			
			
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
			if (s_age == "spin") {
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
				  		Editor edit=shared_prefs.edit();
				 		edit.putInt(city, city_id);
				 		edit.apply();
				 		break;
				 	 }
				 	 new_city_c.moveToNext();
			 	}
			 }
			 startService(new Intent(SignupActivity.this, StoresUploadService.class));
			 new_city_c.close();
			 dbh.close();			 
		     return;  
		 }
		 private Cursor cityCursor() {
			 String c_query = String.format("SELECT * FROM %s", DatabaseHelper.TABLE_C);
			 return(dbh.getReadableDatabase().rawQuery(c_query, null));
		 }
	 }
	private class RegisterTask extends UrlJsonAsyncTask {
		public RegisterTask(Context ctxt) {
			super(ctxt);
		}
		@Override
		protected JSONObject doInBackground(String... urls) {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(urls[0]);
			JSONObject holder = new JSONObject();
			JSONObject cust_json = new JSONObject();
			String response = null;
			JSONObject json = new JSONObject();
			try {
				try {
					json.put("success", false);
					json.put("info", "Something went wrong. Try again!");
					
					cust_json.put(email, s_email);
					cust_json.put(phone, s_phone);
					cust_json.put("password", s_password);
					cust_json.put("password_confirmation", s_confirmation);
					cust_json.put(age, s_age);
					cust_json.put(gender, s_gender);
					holder.put("customer", cust_json);
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
					shared_prefs = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
					Editor e = shared_prefs.edit();
					e.putString("AuthToken", json.getJSONObject("data").getString("auth_token"));
					e.putString(user_id, Integer.toString(json.getJSONObject("data").getJSONObject("customer").getInt("id")));
					e.putString("email", json.getJSONObject("data").getJSONObject("customer").getString("email"));
					e.putString("phone", json.getJSONObject("data").getJSONObject("customer").getString("phone"));
					e.apply();
					
					Intent i=new Intent(getApplicationContext(), CoreActivity.class);
					startActivity(i);
					finish();
				}
				Toast.makeText(SignupActivity.this, json.getString("info"), Toast.LENGTH_LONG).show();
			}
			catch(Exception e) {
				Toast.makeText(SignupActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
			}
			finally {
				super.onPostExecute(json);
			}
		}
	}
}
