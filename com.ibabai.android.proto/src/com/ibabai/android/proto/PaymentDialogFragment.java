package com.ibabai.android.proto;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class PaymentDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {
	private DatabaseHelper db=null;
	private View form=null;
	private AlertDialog payment_dialog=null;
	public static final String PREFERENCES = "MyPrefs";
	public static final String balance = "Balance";
	SharedPreferences shared_prefs;
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);
		
		db = DatabaseHelper.getInstance(getActivity().getApplicationContext());		
				
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		db.close();
	}
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		form = getActivity().getLayoutInflater().inflate(R.layout.dialog_payment, null);
		TextView tv_acc = (TextView) form.findViewById(R.id.dialog_account);
		TextView tv_phn = (TextView) form.findViewById(R.id.dialog_phone);
		TextView tv_amount = (TextView) form.findViewById(R.id.dialog_amount);
		String st_acc = getArguments().getString("dialog_acc");
		String st_phn = getArguments().getString("dialog_phn");
		String st_amount = getArguments().getString("dialog_amnt");
		
		tv_acc.setText(st_acc);
		tv_phn.setText(st_phn);
		tv_amount.setText(st_amount);
		AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
		AlertDialog dialog = builder.setView(form).setPositiveButton(android.R.string.ok, this).setNegativeButton(android.R.string.cancel, null).create(); 
		payment_dialog=dialog;
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface d) {				
				Button bp = payment_dialog.getButton(AlertDialog.BUTTON_POSITIVE);
				bp.setTextSize(20);
				bp.setBackgroundResource(R.drawable.d_button);
				Button bn = payment_dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
				bn.setTextSize(20);
				bn.setBackgroundResource(R.drawable.d_button);
			}
		});
		return(payment_dialog);
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		
		ContentValues cv = new ContentValues(4);
		AlertDialog dlg = (AlertDialog)dialog;
		TextView tv_amnt=(TextView) dlg.findViewById(R.id.dialog_amount);
		String s_amnt=tv_amnt.getText().toString();
		String s_agent = getArguments().getString("dialog_agent");
		String date = new SimpleDateFormat("dd.MM.yyyy").format(new Date());
		
		cv.put(DatabaseHelper.DATE, date);
		cv.put(DatabaseHelper.AGENT, s_agent);
		cv.put(DatabaseHelper.AMOUNT, s_amnt);
		
		shared_prefs=this.getActivity().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        String b = shared_prefs.getString(balance, "0");
        int int_b = Integer.parseInt(b);
        int int_amnt=Integer.parseInt(s_amnt);		
		
		if (s_agent.equals("MTS") || s_agent.equals("Kievstar") || s_agent.equals("Life")) {
			cv.put(DatabaseHelper.TYPE, "D");	
			
			String new_b = Integer.toString(int_b - int_amnt);
			Editor editor = shared_prefs.edit();
	    	editor.putString(balance, new_b).apply();
	    	
	    	Toast.makeText(getActivity().getBaseContext(), "Payment request accepted", Toast.LENGTH_LONG).show();
		}
		else {
			cv.put(DatabaseHelper.TYPE, "C");
			String new_b = Integer.toString(int_b + int_amnt);
			Editor editor = shared_prefs.edit();
	    	editor.putString(balance, new_b).apply();
	    	
	    	Toast.makeText(getActivity().getBaseContext(), "Your account is credited", Toast.LENGTH_LONG).show();
		}
		
		
    	
		new InsertTask().execute(cv);
		
		Intent i = new Intent(getActivity(), MarketActivity.class);
    	startActivity(i);
	}	
	private class InsertTask extends AsyncTask<ContentValues, Void, Void> {
		
		@Override
		protected Void doInBackground(ContentValues... cv) {
			db.getWritableDatabase().insert(DatabaseHelper.TABLE, DatabaseHelper.AGENT, cv[0]);
			db.close();
			return null;
		}		
	}
}
