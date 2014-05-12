package com.ibabai.android.proto;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

public class SignupActivity extends ActionBarActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.signup);
		
		ActionBar ab = getActionBar();
        ab.setDisplayShowHomeEnabled(false);
        ab.setCustomView(R.layout.ab_signup);
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        
        NumberPicker agePicker=(NumberPicker) findViewById(R.id.age_picker);
        agePicker.setMaxValue(60);
        agePicker.setMinValue(16);
        agePicker.setValue(17);
        agePicker.setWrapSelectorWheel(false);
        
	}
	
	public void AccountCreate(View view) {
		EditText email=(EditText)findViewById(R.id.email);
		EditText phone=(EditText)findViewById(R.id.phone);
		EditText password=(EditText)findViewById(R.id.password);
		EditText password_confirmation=(EditText)findViewById(R.id.password_confirmation);
		String s_email = email.getText().toString();
		String s_phone = phone.getText().toString();
		String s_password = password.getText().toString();
		String s_confirmation = password_confirmation.getText().toString();
		
		if ( s_email.length() >= 5 && s_phone.length() == 10 && s_password.length() >= 6 && s_password.equals(s_confirmation)) {
			Intent i=new Intent(this, CoreActivity.class);
			startActivity(i);
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
		}
		
	}
}
