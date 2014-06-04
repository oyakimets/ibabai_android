package com.ibabai.android.proto;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class StopDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {
	private View form=null;
	private AlertDialog sl_dialog=null;
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		form = getActivity().getLayoutInflater().inflate(R.layout.dialog_sl, null);
		AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
		AlertDialog dialog = builder.setView(form).setPositiveButton("Yes", this).setNegativeButton("No", null).create(); 
		sl_dialog=dialog;
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface d) {
				Button bp = sl_dialog.getButton(AlertDialog.BUTTON_POSITIVE);
				bp.setTextSize(20);
				bp.setBackgroundResource(R.drawable.d_button);
				Button bn = sl_dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
				bn.setTextSize(20);
				bn.setBackgroundResource(R.drawable.d_button);
			}
		});
		return(sl_dialog);
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case AlertDialog.BUTTON_NEGATIVE:
			dismiss();
			break;
		case AlertDialog.BUTTON_POSITIVE:
			int position=getArguments().getInt("position");		
			Intent sl_yes=new Intent(getActivity(), CoreActivity.class);
			sl_yes.putExtra(CoreActivity.EXTRA_NI, position);
			startActivity(sl_yes);
			Toast.makeText(getActivity().getBaseContext(), "All promos of the company are blocked", Toast.LENGTH_LONG).show();
			/*launch async task: 1) copy cl.jpg from promo to sl folder 2) update sl.json file 
			 * 3) update promos.json file 4)delete promo folder 5)send promo id to server 
			 */
			break;
		default:
			break;			
		}
			
	}
}
