package com.ibabai.android.proto;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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
	DatabaseHelper dbh;
	
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
        
        dbh = DatabaseHelper.getInstance(getApplicationContext());
               
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
		
		int ven_id = getIntent().getExtras().getInt("v_id");
		
		String s_agent = getVendorName(ven_id);
		
		if (amount_input.length() == 0) {
			et_amount.setError("Please enter amount");
		}
		else {
			int int_amount = Integer.parseInt(amount_input);
			shared_prefs=getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
			String b = shared_prefs.getString(balance, "0");
			int int_b = Integer.parseInt(b);
			
			if ((phone_input.length()==10 || account_input.length() >=6) && int_amount != 0 && int_amount<=int_b) { 
					
				Bundle bundle = new Bundle();
				bundle.putString("dialog_acc", account_input);
				bundle.putString("dialog_phn", phone_input);
				bundle.putString("dialog_amnt", amount_input);
				bundle.putInt("dialog_id", ven_id);
				bundle.putString("dialog_agent", s_agent);
				PaymentDialogFragment pdf = new PaymentDialogFragment();
				pdf.setArguments(bundle);
				pdf.show(getSupportFragmentManager(), "payment");
			}
			else if (phone_input.length()==0 && account_input.length()==0 ) {
				et_phone.setError("Enter your phone or account number");
			}
			else if (phone_input.length() !=10 && phone_input.length() >0 && account_input.length() == 0 ) {
				et_phone.setError("Phone number input error");
			}
			else if (account_input.length() < 6 && account_input.length() > 0 && phone_input.length()==0 ) {
				et_phone.setError("Account number input error");
			}
			else if (int_amount == 0) {
				et_amount.setError("Amount can not be 0");
			}
			else if (int_amount > int_b) {
				et_amount.setError("Error! Amount exceeds balance");
			}
		}
	}
	private String getVendorName(int id) {
		String v_name=null;
		String p_query = String.format("SELECT * FROM %s WHERE vendor_id="+Integer.toString(id), DatabaseHelper.TABLE_V);
		Cursor c = dbh.getReadableDatabase().rawQuery(p_query, null);
		if (c!=null && c.moveToFirst()) {
			int name_ind = c.getColumnIndex("vendor_name");
			v_name = c.getString(name_ind);			
		}
		return v_name;		
	}
}
