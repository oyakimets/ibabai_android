package com.ibabai.android.proto;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class PaymentDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {
	
	private View form=null;
	private AlertDialog payment_dialog=null;
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
		Intent i = new Intent(getActivity(), MarketActivity.class);
    	startActivity(i);
	}
}
