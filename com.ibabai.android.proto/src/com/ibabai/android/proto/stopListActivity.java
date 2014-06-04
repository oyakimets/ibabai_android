package com.ibabai.android.proto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.ibabai.android.proto.UnblockDialogFragment.UnblockDialogListener;

public class stopListActivity extends FragmentActivity implements UnblockDialogListener  {
	private Bundle bundle;
	public static int sl_size=0;	
	public static final String EXTRA_POS="position";
	public static final String PREFERENCES = "MyPrefs";
	public static final String balance = "Balance";
	private static final String TAG_STOPS="stops";	
	private ListView StopList;
	private StopListAdapter sl_adapter;
	public static ArrayList<Drawable> StopListItems;
	private GetStops get_stops=null;
	public static ArrayList<String> allDirs;	
	JSONArray stopJArr = null;
	SharedPreferences shared_prefs;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stop_list);
        
        ActionBar ab=getActionBar();
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setCustomView(R.layout.ab_stoplist);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayShowTitleEnabled(false);
        
        allDirs=new ArrayList<String>();
        StopList=(ListView) findViewById(R.id.stop_list);
        StopListItems = new ArrayList<Drawable>();
        get_stops=new GetStops();
        executeAsyncTask(get_stops, getApplicationContext());
        
	}
	public static <T> void executeAsyncTask(AsyncTask<T, ?, ?> task, T... params) {
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
	}
	private class GetStops extends AsyncTask<Context, Void, Void> {		 
		 
		 @Override
		 protected Void doInBackground(Context... ctxt) {
			 try {
				 StringBuilder buf=new StringBuilder();
				 InputStream json=ctxt[0].getAssets().open("promo_content/sl.json"); 	
				 BufferedReader in = new BufferedReader(new InputStreamReader(json));
				 String str;
				 while((str=in.readLine()) != null ) {
					buf.append(str);
				}				
				in.close();
				
				JSONArray ja=new JSONArray(buf.toString());				
				JSONObject jo=(JSONObject)ja.getJSONObject(0);
				stopJArr=jo.getJSONArray(TAG_STOPS);				
				for(int i=0; i<stopJArr.length(); i++) {					
					String stop_str=(String)stopJArr.get(i);					
					allDirs.add(stop_str);	
				}
				for (int j=0; j< allDirs.size(); j++) {
				   try { 
				       String file_name=allDirs.get(j); 
				       InputStream is = ctxt[0].getAssets().open("promo_content/stop_list/"+file_name);
				       Drawable d_stop=Drawable.createFromStream(is, null);
				       StopListItems.add(d_stop);
				    }
				    catch (IOException ex) {				        	      	
				    }				        	
				}			
			 }
		 	 catch(Exception e) {
		 		 e.printStackTrace();
		 	 }			 
			 
			 return null;
		 }
		 @Override 
		 public void onPostExecute(Void result) {
			 super.onPostExecute(result);		 
		     
			 sl_adapter = new StopListAdapter(getApplicationContext(), StopListItems);
			 sl_size=sl_adapter.getCount();			 
		     StopList.setAdapter(sl_adapter);
		     bundle = new Bundle();
		     bundle.putInt("size", sl_size);
		     UnblockDialogFragment unbl = new UnblockDialogFragment();
		     unbl.setArguments(bundle);
		     StopList.setOnItemClickListener(new StopListClickListener());
			 
		     return;  
		 }
	 }
	private class StopListClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			displayStopAction(position);
		}		
	}
	private void displayStopAction(int pos) {		
    	bundle.putInt("position", pos);
    	UnblockDialogFragment unbl = new UnblockDialogFragment();
    	unbl.setArguments(bundle);
    	unbl.show(getSupportFragmentManager(), "unblock");		
	}
	
	public void KillBlock(int ub_pos) {		
		if (StopListItems.size() != 0 && ub_pos != -1) {
			StopListItems.remove(ub_pos);
			bundle.putInt("size", StopListItems.size());
	    	UnblockDialogFragment ub_fr = new UnblockDialogFragment();
	    	ub_fr.setArguments(bundle);
			sl_adapter.notifyDataSetChanged();
			
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.core, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.action_scan:
			Intent i=new Intent(this, ScanActivity.class);
			startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);
			
		}		
		
	}

}
