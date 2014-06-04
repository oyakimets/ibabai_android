package com.ibabai.android.proto;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.json.JSONObject;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class PromoModelFragment extends Fragment {
	private PromoPresentation presentation=null;
	private PresLoadTask presTask=null;
	private static String pc_folder;	
	
	@Override 
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		setRetainInstance(true);
		deliverModel();
	}
	synchronized private void deliverModel() {
		if (presentation != null) {
			((PresentationDisplayActivity)getActivity()).setupPager(presentation);
		}
		else {
			if(presentation == null && presTask == null) {
				presTask = new PresLoadTask();
				executeAsyncTask(presTask, getActivity().getApplicationContext());
			}
		}
	}
	static public <T> void executeAsyncTask(AsyncTask<T, ?, ?> task, T... params) {
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
	}
	private class PresLoadTask extends AsyncTask<Context, Void, Void> {		
		private PromoPresentation localPresentation=null;		
		private Exception e=null;
		private String path;		
		int position=getActivity().getIntent().getIntExtra(PresentationDisplayActivity.EXTRA_POSITION, -1);
		@Override 
		protected Void doInBackground(Context... ctxt) {
			pc_folder= PresentationDisplayActivity.getPromoDir(position); 
			path="promo_content/"+pc_folder+"/cp.json";
			try {
				StringBuilder buf=new StringBuilder();
				InputStream json=ctxt[0].getAssets().open(path); 	
				BufferedReader in = new BufferedReader(new InputStreamReader(json));
				String str;
				while((str=in.readLine()) != null ) {
					buf.append(str);
				}
				in.close();
				localPresentation=new PromoPresentation(new JSONObject(buf.toString()));
			}
			catch(Exception e) {
				this.e=e;
			}
			return null;
		}
		@Override
		public void onPostExecute(Void a) {
			if (e==null) {
				PromoModelFragment.this.presentation=localPresentation;
				PromoModelFragment.this.presTask=null;
				deliverModel();
			}
			else {
				Log.e(getClass().getSimpleName(), "Exception loading presentation", e);
			}
		}
		
	}
}
