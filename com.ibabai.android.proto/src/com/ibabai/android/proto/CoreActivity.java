package com.ibabai.android.proto;

import java.util.ArrayList;

import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ibabai.slidemenu.adapter.NavDrawerListAdapter;
import com.ibabai.slidemenu.model.NavDrawerItem;

public class CoreActivity extends FragmentActivity {
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private String[] navMenuTitles;
	private TypedArray navMenuIcons;
	private ArrayList<NavDrawerItem> navDrawerItems;
	private NavDrawerListAdapter adapter;
	
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
        
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1), true, "5"));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1), true, "3"));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(5, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[6], navMenuIcons.getResourceId(6, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[7], navMenuIcons.getResourceId(7, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[8], navMenuIcons.getResourceId(8, -1)));
        
        navMenuIcons.recycle();
        
        adapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
        mDrawerList.setAdapter(adapter);
        
        ActionBar ab = getActionBar(); 
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setCustomView(R.layout.ab_balance);
        ab.setDisplayShowHomeEnabled(false);
        ab.setDisplayShowTitleEnabled(false);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);

        
       
        
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.core, menu);
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
}
