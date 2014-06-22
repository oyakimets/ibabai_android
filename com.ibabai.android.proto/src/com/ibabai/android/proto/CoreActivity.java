package com.ibabai.android.proto;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ibabai.slidemenu.adapter.NavDrawerListAdapter;
import com.ibabai.slidemenu.model.NavDrawerItem;

public class CoreActivity extends FragmentActivity {
	public static final String EXTRA_NI="position";
	private boolean bool=false;
	private String str_sl_size="0";	
	private String ni_position = null;	
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private String[] navMenuTitles;
	private TypedArray navMenuIcons;
	private ArrayList<NavDrawerItem> navDrawerItems;
	private NavDrawerListAdapter adapter;
	public static final String PREFERENCES = "MyPrefs";
	public static final String balance = "Balance";	
	private ListView PromoList;
	private PromoListAdapter pl_adapter;
	private ArrayList<Drawable> PromoListItems;
	private GetPromos get_promos=null;
	public static ArrayList<String> allDirs;
	public static ArrayList<String> dbPromos;
	private static int store_id;	
	private Cursor pa_cursor;
	private Cursor ps_cursor;
	private Cursor home_cursor;
	SharedPreferences shared_prefs;
	DatabaseHelper dbh;
	FileInputStream is;
	BufferedInputStream buf;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.activity_core);       
        
        mTitle = mDrawerTitle = getTitle();
        
        navMenuTitles = getResources().getStringArray(R.array.core_menu);
        navMenuIcons = getResources().obtainTypedArray(R.array.core_menu_icons);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mDrawerList = (ListView)findViewById(R.id.core_left);
        navDrawerItems = new ArrayList<NavDrawerItem>();
        if (stopListActivity.StopListItems == null) {
        	str_sl_size=Integer.toString(0);
        }
        else {
        	str_sl_size=Integer.toString(stopListActivity.StopListItems.size());
        }
        bool = setCountBoolean(str_sl_size);
        
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1), true, "5"));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1), bool, str_sl_size));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(5, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[6], navMenuIcons.getResourceId(6, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[7], navMenuIcons.getResourceId(7, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[8], navMenuIcons.getResourceId(8, -1)));
        
        navMenuIcons.recycle();
        
        adapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
        mDrawerList.setAdapter(adapter);        
               
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, 0, 0) {
        	public void onDrawerClosed(View view) {
        		getActionBar().setTitle(mTitle);
        		invalidateOptionsMenu();
        	}
        	public void onDrawerOpened(View drawerView) {
        		getActionBar().setTitle(mDrawerTitle);
        		invalidateOptionsMenu();
        	}
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        
        if (!mDrawerLayout.isDrawerOpen(mDrawerList)) {
        	ActionBar abar = getActionBar();
            abar.setCustomView(R.layout.ab_balance);
                    	
        }
        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());
        
        ActionBar ab = getActionBar(); 
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setCustomView(R.layout.ab_balance);
        ab.setDisplayShowHomeEnabled(false);
        ab.setDisplayShowTitleEnabled(false);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
        
        dbh=DatabaseHelper.getInstance(getApplicationContext());
        
        shared_prefs=getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        store_id=shared_prefs.getInt("store_id", 0);       
        
		dbPromos=new ArrayList<String>();
        allDirs=new ArrayList<String>();
        PromoList=(ListView) findViewById(R.id.promo_list);
        PromoListItems = new ArrayList<Drawable>();
        get_promos=new GetPromos();
        executeAsyncTask(get_promos, getApplicationContext()); 
        
        DataUpdateReceiver.scheduleAlarm(this);
	}
	private class SlideMenuClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			displayAction(position);
		}
	}
	private void displayAction(int position) {
		switch (position) {
		case 0:
			Intent i=new Intent(this, myPromoActivity.class);
			startActivity(i);
			mDrawerLayout.closeDrawer(mDrawerList);
			break;
		case 1:
			Intent i1=new Intent(this, MarketActivity.class);
			startActivity(i1);
			mDrawerLayout.closeDrawer(mDrawerList);
			break;
		case 2:
			Intent i2=new Intent(this, stopListActivity.class);
			startActivity(i2);
			mDrawerLayout.closeDrawer(mDrawerList);
			break;
		case 3:
			new ProfileDialogFragment().show(getSupportFragmentManager(), "profile");
			mDrawerLayout.closeDrawer(mDrawerList);			
			break;
		case 4:
			Intent i4=new Intent(this, LogActivity.class);
			startActivity(i4);
			mDrawerLayout.closeDrawer(mDrawerList);
			break;
		case 5:
			Intent i5=new Intent(this, SettingsActivity.class);
			startActivity(i5);
			mDrawerLayout.closeDrawer(mDrawerList);
			break;
		case 6:
			Intent emailInt = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "oleg.yakimets@gmail.com", null));
			emailInt.putExtra(Intent.EXTRA_SUBJECT, "User feedback");
			startActivity(Intent.createChooser(emailInt, "Share your feedback"));
			mDrawerLayout.closeDrawer(mDrawerList);
			break;
		case 7:
			Intent i7=new Intent(this, HelpActivity.class);
			startActivity(i7);
			mDrawerLayout.closeDrawer(mDrawerList);
			break;
		case 8:
			Intent sharingIntent=new Intent(Intent.ACTION_SEND);
			sharingIntent.setType("text/plain");
			String shareBody = "www.ibabai.com";
			sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Cool App!");
			sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
			startActivity(Intent.createChooser(sharingIntent, "Share through"));
			mDrawerLayout.closeDrawer(mDrawerList);
			break;
		default:
			break;
		}			
		
	}	
	public static <T> void executeAsyncTask(AsyncTask<T, ?, ?> task, T... params) {
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
	}
	private class GetPromos extends AsyncTask<Context, Void, Void> {
		
		 
		 @Override
		 protected Void doInBackground(Context... ctxt) {
			 
			 try {
				 checkHomePromo();
				 pa_cursor=promoactCursor();
				 if (pa_cursor != null && pa_cursor.moveToFirst()) {
					 int id_ind = pa_cursor.getColumnIndex("promoact_id");
					 while (!pa_cursor.isAfterLast()) {
						 String pa_id = Integer.toString(pa_cursor.getInt(id_ind));						
						 dbPromos.add(pa_id);
						 pa_cursor.moveToNext();
					 }
					 pa_cursor.close();
					 if (store_id == 0) {
						 allDirs=dbPromos;						 
					 }
					 else {
						 ps_cursor = storePromosCursor(store_id);
						 if(ps_cursor != null && ps_cursor.moveToFirst()) {
							 int paid_ind = ps_cursor.getColumnIndex("promoact_id");
							 while (!ps_cursor.isAfterLast()) {
								 String promoact_id=Integer.toString(ps_cursor.getInt(paid_ind));
								 if (dbPromos.contains(promoact_id)) {
									 allDirs.add(promoact_id);
								 }
								 ps_cursor.moveToNext();
							 }
							 ps_cursor.close();
						 }						
					 }
				 }
				 			
				
				if (ni_position != null) {
					allDirs.remove(ni_position);
				}
				allDirs.add("0");
				for (int j=0; j< allDirs.size(); j++) {					
					String dir=allDirs.get(j);
					File pa_folder = new File(getConDir(CoreActivity.this), dir);
					if (pa_folder.exists()) {
						File tag_file = new File(pa_folder, "con_tag.JPG");
						String tag_path = tag_file.getAbsolutePath();												
						Drawable d_promo = Drawable.createFromPath(tag_path);
						PromoListItems.add(d_promo);								
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
		     
			 pl_adapter = new PromoListAdapter(getApplicationContext(), PromoListItems);
			 KillPromo();
		     PromoList.setAdapter(pl_adapter);
		     PromoList.setOnItemClickListener(new PromoListClickListener());		     
			 
		     return;  
		 }
	 }
	@Override
	protected void onResume() {
		GPSTracker gps = new GPSTracker(this);
        if(!gps.canGetLocation()) {
        	LocDialogFragment ldf = new LocDialogFragment();
        	ldf.show(getSupportFragmentManager(), "location");
        }
        else {        	
        	String lat=Double.toString(gps.getLatitude());
        	String lon=Double.toString(gps.getLongitude());
        	Toast t = Toast.makeText(this, "Latitude: "+lat+"/Longitude: "+lon, Toast.LENGTH_LONG);
        	t.show();      	
        	
        }
        super.onResume();		
	}
	@Override
	protected void onDestroy() {
		dbh.close();
		super.onDestroy();
	}
	private class PromoListClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			displayPromoAction(position);
		}		
	}
	private void displayPromoAction(int position) {
		Intent promo_intent=new Intent(this, PresentationDisplayActivity.class);
		promo_intent.putExtra(PresentationDisplayActivity.EXTRA_POSITION, position);
		String pa_id = allDirs.get(position);
		promo_intent.putExtra(PresentationDisplayActivity.EXTRA_PA, pa_id);
		startActivity(promo_intent);		
	}
	
	private void KillPromo() {
		ni_position=getIntent().getStringExtra(EXTRA_NI);
		if (PromoListItems.size() != 0 && ni_position != null) {
			PromoListItems.remove(ni_position);
			pl_adapter.notifyDataSetChanged();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.core, menu);		
		shared_prefs=getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        String b = shared_prefs.getString(balance, "0");
        TextView tv_balance = (TextView) findViewById(R.id.balance);
        tv_balance.setText("balance "+ b + " b");
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		switch (item.getItemId()) {
		case R.id.action_scan:
			Intent in=new Intent(this, ScanActivity.class);
			startActivity(in);
			return true;
		default:
			return super.onOptionsItemSelected(item);
			
		}		
		
	}
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean drawerOpen=mDrawerLayout.isDrawerOpen(mDrawerList);
		menu.findItem(R.id.action_scan).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}
	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}
	@Override 
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}
	@Override
	public void onBackPressed() {
		Intent e_int = new Intent(Intent.ACTION_MAIN);
		e_int.addCategory(Intent.CATEGORY_HOME);
		e_int.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(e_int);
		
	}
	public boolean setCountBoolean(String counter) {
		if (counter.equals("0")) {
			return false;
		}
		else {
			return true;
		}			
	}
	
	private Cursor promoactCursor() {
		 String p_query = String.format("SELECT * FROM %s WHERE stopped=0", DatabaseHelper.TABLE_P);
		 return(dbh.getReadableDatabase().rawQuery(p_query, null));
	 }
	static File getConDir(Context ctxt) {
		 return(new File(ctxt.getFilesDir(), ConUploadService.CON_BASEDIR));
	 }
	private Cursor storePromosCursor(int store_id) {		
		String ps_query= "SELECT * FROM promo_stores WHERE store_id="+Integer.toString(store_id);
		return (dbh.getReadableDatabase().rawQuery(ps_query, null));
	}
	private Cursor homePromoCursor() {
		String p_query = String.format("SELECT * FROM %s WHERE promoact_id=0", DatabaseHelper.TABLE_P);
		return(dbh.getReadableDatabase().rawQuery(p_query, null));
	}
	private void checkHomePromo() {
		home_cursor = homePromoCursor();
		int stop_ind = home_cursor.getColumnIndex("stopped");
		if (home_cursor != null && home_cursor.moveToFirst()) {
			int stopped = home_cursor.getInt(stop_ind);	
			home_cursor.close();
			if (stopped == 1) {			
				File home_folder = new File(getConDir(CoreActivity.this), "0");
				if (home_folder.exists()) {
					home_folder.delete();
				}
			}			
		}
	}
}
