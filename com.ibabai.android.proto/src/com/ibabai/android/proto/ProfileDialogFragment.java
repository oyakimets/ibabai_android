package com.ibabai.android.proto;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class ProfileDialogFragment extends DialogFragment {
	private View form=null;
	private AlertDialog profile_dialog=null;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		form = getActivity().getLayoutInflater().inflate(R.layout.profile_dialog, null);
		OnClickListener positiveClick = new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent i = new Intent(getActivity(), ProfileActivity.class);
				startActivity(i);
			}
		};
		OnClickListener neutralClick = new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Toast.makeText(getActivity().getBaseContext(), "Check your email for instructions", Toast.LENGTH_LONG).show();
			}
		};
		OnClickListener negativeClick = new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent cancelInt = new Intent(getActivity(), CoreActivity.class);
				startActivity(cancelInt);			
			}
		};		
		
		AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
		AlertDialog dialog = builder.setView(form).setPositiveButton("Done", positiveClick).setNeutralButton("Forgot?", neutralClick).setNegativeButton("Cancel", negativeClick).create(); 
		profile_dialog=dialog;
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface d) {				
				Button bp = profile_dialog.getButton(AlertDialog.BUTTON_POSITIVE);
				bp.setTextSize(20);
				bp.setBackgroundResource(R.drawable.d_button);
				Button bn = profile_dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
				bn.setTextSize(20);
				bn.setBackgroundResource(R.drawable.d_button);
				Button bneu = profile_dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
				bneu.setTextSize(20);
				bneu.setBackgroundResource(R.drawable.d_button);
			}
		});
		return(profile_dialog);
	}
	
}
