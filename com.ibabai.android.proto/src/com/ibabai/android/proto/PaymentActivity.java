package com.ibabai.android.proto;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class PaymentActivity extends FragmentActivity {
	public static final String PREFERENCES = "MyPrefs";
	public static final String balance = "Balance";
	SharedPreferences shared_prefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.action_payment);
		
		ActionBar ab = getActionBar(); 
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setCustomView(R.layout.ab_balance);
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayShowTitleEnabled(false);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.core, menu);
		
		shared_prefs=getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        String b = shared_prefs.getString(balance, "0");
        TextView tv_balance = (TextView) findViewById(R.id.balance);
        tv_balance.setText("balance "+ b + " b");
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		switch (item.getItemId()) {
		case R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.action_scan:
			Intent in=new Intent(this, ScanActivity.class);
			startActivity(in);
			return true;
		default:
			return super.onOptionsItemSelected(item);
			
		}		
		
	}
	
	public void confirmBill(View view) {
		EditText et_account = (EditText) findViewById(R.id.billing_account);
		String account_input = et_account.getText().toString();
		
		EditText et_phone = (EditText) findViewById(R.id.billing_phone);
		String phone_input = et_phone.getText().toString();
		
		EditText et_amount = (EditText) findViewById(R.id.billing_amount);
		String amount_input = et_amount.getText().toString();
		
		
		String s_agent = getIntent().getExtras().getString("d_agent");
			
				
		int int_amount = Integer.parseInt(amount_input);
		shared_prefs=getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        String b = shared_prefs.getString(balance, "0");
        int int_b = Integer.parseInt(b);
        if (int_amount <= int_b) {
        	Bundle bundle = new Bundle();
        	bundle.putString("dialog_acc", account_input);
        	bundle.putString("dialog_phn", phone_input);
        	bundle.putString("dialog_amnt", amount_input);
        	bundle.putString("dialog_agent", s_agent);
        	PaymentDialogFragment pdf = new PaymentDialogFragment();
        	pdf.setArguments(bundle);
        	pdf.show(getSupportFragmentManager(), "payment");
        	
        }
        else {
        	et_amount.setError("Amount excedes balance");
        }
	}
}
