package com.ibabai.android.proto;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class PaymentActivity extends FragmentActivity {
	
	
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
	
	public void confirmBill(View view) {
		EditText et_account = (EditText) findViewById(R.id.billing_account);
		String account_input = et_account.getText().toString();
		EditText et_phone = (EditText) findViewById(R.id.billing_phone);
		String phone_input = et_phone.getText().toString();
		EditText et_amount = (EditText) findViewById(R.id.billing_amount);
		String amount_input = et_amount.getText().toString();
		Bundle bundle = new Bundle();
		bundle.putString("dialog_acc", account_input);
		bundle.putString("dialog_phn", phone_input);
		bundle.putString("dialog_amnt", amount_input);
		PaymentDialogFragment pdf = new PaymentDialogFragment();
		pdf.setArguments(bundle);
		pdf.show(getSupportFragmentManager(), "payment"); 
	}

}
