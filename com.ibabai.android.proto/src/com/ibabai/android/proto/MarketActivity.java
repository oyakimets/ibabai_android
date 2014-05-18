package com.ibabai.android.proto;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class MarketActivity extends Activity {
	private ListView VendorList;
	private TypedArray VendorTags;
	private ArrayList<VendorListItem> VendorListItems;
	private VendorListAdapter adapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.market);
        
        VendorTags=getResources().obtainTypedArray(R.array.market_tags);
        VendorList = (ListView)findViewById(R.id.vendors);
        VendorListItems = new ArrayList<VendorListItem>();
        
        VendorListItems.add(new VendorListItem(VendorTags.getResourceId(0, -1)));
        VendorListItems.add(new VendorListItem(VendorTags.getResourceId(1, -1)));
        VendorListItems.add(new VendorListItem(VendorTags.getResourceId(2, -1)));
        VendorTags.recycle();
        
        adapter = new VendorListAdapter(getApplicationContext(), VendorListItems);
        VendorList.setAdapter(adapter);
        
        VendorList.setOnItemClickListener(new VendorListClickListener());
        
        ActionBar ab=getActionBar();
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setCustomView(R.layout.ab_market);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayShowTitleEnabled(false);        
        
	}
	
	private class VendorListClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			displayAction(position);
		}		
	}
	private void displayAction(int position) {
		switch (position) {
		case 0:
			Intent paymentIntent = new Intent(this, PaymentActivity.class);
			startActivity(paymentIntent);
			break;
		case 1:
			Intent paymentIntent1 = new Intent(this, PaymentActivity.class);
			startActivity(paymentIntent1);
			break;
		case 2:
			Intent paymentIntent2 = new Intent(this, PaymentActivity.class);
			startActivity(paymentIntent2);
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
