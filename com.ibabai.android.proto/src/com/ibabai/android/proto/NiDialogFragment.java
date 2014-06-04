package com.ibabai.android.proto;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;

public class NiDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {
	private View form=null;
	private AlertDialog ni_dialog=null;
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		form = getActivity().getLayoutInflater().inflate(R.layout.dialog_ni, null);
		AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
		AlertDialog dialog = builder.setView(form).setPositiveButton("Yes", this).setNegativeButton("No", null).create(); 
		ni_dialog=dialog;
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface d) {
				Button bp = ni_dialog.getButton(AlertDialog.BUTTON_POSITIVE);
				bp.setTextSize(20);
				bp.setBackgroundResource(R.drawable.d_button);
				Button bn = ni_dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
				bn.setTextSize(20);
				bn.setBackgroundResource(R.drawable.d_button);
			}
		});
		return(ni_dialog);
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case AlertDialog.BUTTON_NEGATIVE:
			dismiss();
			break;
		case AlertDialog.BUTTON_POSITIVE:
			int position=getArguments().getInt("position");		
			Intent ni_yes=new Intent(getActivity(), CoreActivity.class);
			ni_yes.putExtra(CoreActivity.EXTRA_NI, position);
			startActivity(ni_yes);
			/* Launch async task: 1)delete promo directory2)update promos.json
			 * 3) send data to server with promo id
			 */
			break;
		default:
			break;			
		}
			
	}
}
