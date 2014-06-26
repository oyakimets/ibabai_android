package com.ibabai.android.proto;

import java.io.File;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;

public class UnblockDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {
	private String client_id;
	private View form=null;
	private AlertDialog ub_dialog=null;
	DatabaseHelper dbh;
	public interface ReloadDataListener {
		public void ReloadData();
	}
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		dbh=DatabaseHelper.getInstance(getActivity().getApplicationContext());
		form = getActivity().getLayoutInflater().inflate(R.layout.dialog_unblock, null);
		AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
		AlertDialog dialog = builder.setView(form).setPositiveButton("OK", this).setNegativeButton("Cancel", null).create(); 
		ub_dialog=dialog;
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface d) {
				Button bp = ub_dialog.getButton(AlertDialog.BUTTON_POSITIVE);
				bp.setTextSize(20);
				bp.setBackgroundResource(R.drawable.d_button);
				Button bn = ub_dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
				bn.setTextSize(20);
				bn.setBackgroundResource(R.drawable.d_button);
			}
		});
		return(ub_dialog);
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case AlertDialog.BUTTON_NEGATIVE:
			dismiss();
			break;
		case AlertDialog.BUTTON_POSITIVE:			
			int position=getArguments().getInt("position");
			File sl_dir = getStopDir(getActivity()); 
			if (sl_dir.exists() && sl_dir.isDirectory()) {
				File[] sl_lst = sl_dir.listFiles();
				File sl_f = sl_lst[position];
				String path = sl_f.getAbsolutePath();
				sl_f.delete();
				int s_ind = path.lastIndexOf("/");
				int f_ind = path.lastIndexOf("_");
				client_id= path.substring(s_ind+1, f_ind);
				dbh.updateStatus(client_id, 0);
				dbh.close();
				ReloadDataListener activity = (ReloadDataListener) getActivity();
				activity.ReloadData();
			}
			int size=getArguments().getInt("size");
			if (size > 1) {				
				/*launch async task: 1) delete file from stoplist folder 2) send data to server
				 *  3)update sl.json file 
				 */
			}
			else {				
				Intent ub_yes=new Intent(getActivity(), CoreActivity.class);				
				startActivity(ub_yes);
				/*launch async task: 1) delete file from stoplist folder 2) send data to server
				 *  3)update sl.json file 
				 */
			}
			break;
		default:
			break;			
		}
			
	}
	static File getStopDir(Context ctxt) {
		 return(new File(ctxt.getFilesDir(), stopListActivity.SL_BASEDIR));
	 }	
}
