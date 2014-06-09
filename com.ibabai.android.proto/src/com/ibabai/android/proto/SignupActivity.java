package com.ibabai.android.proto;


import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
}
