package com.ibabai.android.proto;

import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.TextView;

public class LogbookFragment extends ListFragment {
	DatabaseHelper db;	
	@Override
	public void onActivityCreated (Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);		
		setRetainInstance(true);
		
		db = DatabaseHelper.getInstance(getActivity().getApplicationContext());		
		new LoadCursorTask().execute();		
	}
	@Override 
	public void onDestroy() {
		super.onDestroy();
		
		((CursorAdapter)getListAdapter()).getCursor().close();
		db.close();
	}
	private Cursor doQuery() {
		String query = String.format("SELECT _id, %s, %s, %s, %s FROM %s ORDER BY %s DESC", DatabaseHelper.DATE, DatabaseHelper.AGENT, DatabaseHelper.AMOUNT, DatabaseHelper.TYPE, DatabaseHelper.TABLE, DatabaseHelper.DATE);
		return(db.getReadableDatabase().rawQuery(query, null));
	}
	private class LoadCursorTask extends AsyncTask<Void, Void, Void> {
		private Cursor logbookCursor = null;
		
		@Override 
		protected Void doInBackground(Void... params) {
			logbookCursor=doQuery();
			logbookCursor.getCount();
			
			return (null);
		}
		
		@Override
		public void onPostExecute(Void arg0) {
			SimpleCursorAdapter adapter;			
			adapter = new SimpleCursorAdapter(getActivity(), R.layout.log_row, logbookCursor, new String[] {DatabaseHelper.DATE, DatabaseHelper.AGENT, DatabaseHelper.AMOUNT}, new int[] {R.id.lb_date, R.id.lb_agent, R.id.lb_amount}, 0);
			
			adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder(){
				public boolean setViewValue(View v, Cursor c, int i) {
					int type_col=c.getColumnIndex(DatabaseHelper.TYPE);
					int amnt_col=c.getColumnIndex(DatabaseHelper.AMOUNT);
					String type = c.getString(type_col);
					String amnt = c.getString(amnt_col);
					TextView tv = (TextView) v;
					if (type.equals("D")) {
						if (i==amnt_col) {							
							tv.setText("-"+amnt);
							tv.setTextColor(Color.RED);
							return true;
						}
						return false;						
					}
					else {
						if (i==amnt_col) {
							tv.setText(amnt);
							tv.setTextColor(Color.parseColor("#43910f"));
							return true;
						}
						return false;
					}
				}
			});
			setListAdapter(adapter);			
			
		}		
	}
}
