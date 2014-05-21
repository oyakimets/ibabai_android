package com.ibabai.android.proto;

import java.util.Arrays;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.TextView;

public class ProfileUpdateActivity extends Activity {
	public static final String PREFERENCES = "MyPrefs";	
	public static final String username = "email";
	public static final String telephone = "phone #";
	public static final String age = "age";
	public static final String gender="gender";	
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
        String st_mail = shared_prefs.getString(username, "");
        te_mail.setText(st_mail);
        
        EditText te_phone = (EditText)findViewById(R.id.phone_update);
        String st_phone = shared_prefs.getString(telephone, "");
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
        if(st_gender.equals("Male")) {
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
		String s_email = email.getText().toString();
		String s_phone = phone.getText().toString();		
		NumberPicker np_age = (NumberPicker) findViewById(R.id.age_picker_update);
		String[] ap_str = this.setNumPick();
		int in_age = np_age.getValue();
		String str_age = ap_str[in_age];
		RadioGroup genderSelector = (RadioGroup) findViewById(R.id.radioGender_update);
		int selected_id = genderSelector.getCheckedRadioButtonId();
		
		if ( s_email.length() >= 5 && s_phone.length() == 10 && selected_id != -1 ) {
			shared_prefs = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
			Editor editor = shared_prefs.edit();
			
			editor.putString(username, s_email).apply();			
			editor.putString(telephone, s_phone).apply();			
			editor.putString(age, str_age).apply();			
			
			if (selected_id == R.id.radioMale_update) {
				editor.putString(gender, "Male").apply();
			}
			else  {
				editor.putString(gender, "Female").apply();
			}
			
			Intent i=new Intent(this, ProfileActivity.class);
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
}
