package com.ibabai.android.proto;

import java.io.File;
import java.util.ArrayList;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class MarketActivity extends Activity {
	private ListView VendorList;	
	private VendorListAdapter adapter;	
	private ArrayList<Drawable> vendor_list;
	private ArrayList<Integer> id_list;
	private Cursor vendor_c;	
	DatabaseHelper dbh;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.market);      
        
        VendorList = (ListView)findViewById(R.id.vendors);         
        
        ActionBar ab=getActionBar();
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setCustomView(R.layout.ab_market);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayShowTitleEnabled(false);         
               
        id_list=new ArrayList<Integer>(); 
		vendor_list=new ArrayList<Drawable>();
        dbh = DatabaseHelper.getInstance(getApplicationContext());       
        getVendorList();       
        adapter = new VendorListAdapter(getApplicationContext(), vendor_list);
	    VendorList.setAdapter(adapter);        
	    VendorList.setOnItemClickListener(new VendorListClickListener());	
        
	}
	
	private class VendorListClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			displayAction(position);
		}		
	}
	private void displayAction(int position) {		
		int vend_id = id_list.get(position);		
		Cursor vend = getVendor(vend_id);
		int id_ind = vend.getColumnIndex(DatabaseHelper.V_ID);
		if (vend != null && vend.moveToFirst() ) {						
			int v_id = vend.getInt(id_ind);			
			Intent paymentIntent = new Intent(this, PaymentActivity.class);
			paymentIntent.putExtra("v_id", v_id);
			startActivity(paymentIntent);
		}
		vend.close();
		
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
	@Override
	public void onBackPressed() {
		Intent i = new Intent(this, CoreActivity.class);
		startActivity(i);
		super.onBackPressed();
	}
	
	
	private void getVendorList() {	 		 
		vendor_c=vendorCursor();
		int ven_id_ind=vendor_c.getColumnIndex(DatabaseHelper.V_ID);
		if (vendor_c != null) {
			vendor_c.moveToFirst();
			while(vendor_c.isAfterLast()!=true) {
				try {
					int vendor_id=vendor_c.getInt(ven_id_ind);
					id_list.add(vendor_id);
					File tag = new File(getVenDir(this), Integer.toString(vendor_id)+".jpg");
					String tag_path = tag.getAbsolutePath();
					if (tag_path != null) {
						Drawable ven_tag = Drawable.createFromPath(tag_path);
						vendor_list.add(ven_tag);
					}
				}
				catch (Exception ex) {
					ex.printStackTrace();	 
				}
				vendor_c.moveToNext();
			}
			vendor_c.close();
		}
	 }
	private Cursor vendorCursor() {
		 String v_query = String.format("SELECT * FROM %s", DatabaseHelper.TABLE_V);
		 return(dbh.getReadableDatabase().rawQuery(v_query, null));
	}	
	
	private Cursor getVendor(int id) {		
		String vendor = "SELECT * FROM vendors WHERE vendor_id = " + String.valueOf(id);
		return(dbh.getReadableDatabase().rawQuery(vendor, null));
	}	
	
	@Override
	protected void onDestroy() {
		dbh.close();
		super.onDestroy();
	}
	static File getVenDir(Context ctxt) {
		 return(new File(ctxt.getFilesDir(), IbabaiUtils.VEN_BASEDIR));
	 }
}
