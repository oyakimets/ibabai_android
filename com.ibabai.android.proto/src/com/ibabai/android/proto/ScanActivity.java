package com.ibabai.android.proto;

import java.io.File;
import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ScanActivity extends Activity implements OnClickListener {
	public static final String PREFERENCES = "MyPrefs";	
	public static final String store_id = "store_id";
	public static final String balance = "Balance";
	private String bal_value;
	private String rew_2;
	SharedPreferences shared_prefs;
	private int store_prox;	
	private Button scanBtn;
	private TextView formatTxt, contentTxt, scanTitle, scanDesc, scanResult;
	DatabaseHelper dbh;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scanner);
        
        scanTitle = (TextView)findViewById(R.id.scan_title);
        scanDesc=(TextView)findViewById(R.id.scan_desc);       
        scanBtn = (Button)findViewById(R.id.scan_button);
        formatTxt=(TextView)findViewById(R.id.scan_format);
        contentTxt=(TextView)findViewById(R.id.scan_content);
        
        scanBtn.setOnClickListener(this);
        
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayShowTitleEnabled(false);
        
        dbh=DatabaseHelper.getInstance(this);
        
        shared_prefs = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        store_prox = shared_prefs.getInt(store_id, 0);
        bal_value = shared_prefs.getString(balance, "0");
        if (store_prox != 0) {
        	scanTitle.setText("Test scanning mode");
            scanDesc.setText("For product identification purposes only. You can register purchase only outside of the store.");
            
        }
        else {
        	scanTitle.setText("Product registration mode");
            scanDesc.setText("You can register any product for any active promo.");
        }
        /*Async extract ArrayList of product codes from DB
         * 
         */
	}
	public void onClick(View v) {
		Intent intent=new Intent("com.google.zxing.client.android.SCAN");
		intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
		startActivityForResult(intent, 0);
	}
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		shared_prefs = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        store_prox = shared_prefs.getInt(store_id, 0);
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				scanResult=(TextView)findViewById(R.id.scan_result);
				String scanContent = intent.getStringExtra("SCAN_RESULT");
				String scanFormat = intent.getStringExtra("SCAN_RESULT_FORMAT");
				formatTxt.setText("FORMAT: "+scanFormat);
				contentTxt.setText("CONTENT: "+scanContent);
				
				Cursor pa_cursor = getPromoCursor(scanContent); 
				if (pa_cursor != null && pa_cursor.moveToFirst()) {
					scanResult.setBackgroundResource(R.drawable.scan_res_s);
					if(store_prox != 0) {						
						scanResult.setText("Success! This product participate in a promo.");						
					}
					else {
						purchRewCheck(pa_cursor);
						scanResult.setText("Success! Your balance is credited with "+rew_2+" bais.");
					}
				}
				else {
					Cursor home_c = getHomeCursor();
					if (home_c != null && home_c.moveToFirst()) {
						handleHomePromo(home_c);
						scanResult.setText("Success! Your balance is credited with "+rew_2+" bais.");
					}
					else {
						scanResult.setText("Product is not found. Please check promo rules.");
						scanResult.setBackgroundResource(R.drawable.scan_res_f);
					}
				}
			}
			else if (resultCode == RESULT_CANCELED) {
				Toast toast = Toast.makeText(getApplicationContext(), "Scanning error. Try again!", Toast.LENGTH_LONG);
				toast.show();
			}
		}		
	}
	private Cursor getPromoCursor(String b_code) {
		 String p_query = String.format("SELECT * FROM %s WHERE stopped = 0 AND barcode = " + b_code, DatabaseHelper.TABLE_P);
		 return(dbh.getReadableDatabase().rawQuery(p_query, null));
	 }
	private Cursor getHomeCursor() {
		 String p_query = String.format("SELECT * FROM %s WHERE  promoact_id = 0 AND stopped = 0", DatabaseHelper.TABLE_P);
		 return(dbh.getReadableDatabase().rawQuery(p_query, null));
	 }
	public void purchRewCheck(Cursor c) {
		int purch_ind = c.getColumnIndex(DatabaseHelper.PURCH);
		int purch = c.getInt(purch_ind);
		if (purch == 0) {
			int id_ind = c.getColumnIndex(DatabaseHelper.P_ID);			
			int rew2_ind = c.getColumnIndex(DatabaseHelper.REW2);
			int c_name_ind = c.getColumnIndex(DatabaseHelper.CL_NAME);
			String c_name = c.getString(c_name_ind);
			int rew2 = c.getInt(rew2_ind);
			rew_2 = Integer.toString(rew2);
			int pa_id = c.getInt(id_ind);
			c.close();				
			dbh.updatePurch(pa_id);
			dbh.addLogEntry(c_name, rew2, "C");
			dbh.close();
			int bal_amnt = Integer.parseInt(bal_value);
			int new_amnt = bal_amnt + rew2;
			bal_value = Integer.toString(new_amnt);
			Editor editor = shared_prefs.edit();
			editor.putString(balance, bal_value);
			editor.apply();			
		}
		else {
			c.close();
			dbh.close();
		}
	}
	public void handleHomePromo(Cursor c) {
		int purch_ind = c.getColumnIndex(DatabaseHelper.PURCH);
		int purch = c.getInt(purch_ind);
		if (purch == 0) {
			int id_ind = c.getColumnIndex(DatabaseHelper.P_ID);			
			int rew2_ind = c.getColumnIndex(DatabaseHelper.REW2);
			int c_name_ind = c.getColumnIndex(DatabaseHelper.CL_NAME);
			String c_name = c.getString(c_name_ind);
			int rew2 = c.getInt(rew2_ind);
			rew_2 = Integer.toString(rew2);
			int pa_id = c.getInt(id_ind);
			c.close();				
			dbh.updatePurch(pa_id);
			dbh.addLogEntry(c_name, rew2, "C");
			dbh.deleteHomePromo();
			dbh.close();
			int bal_amnt = Integer.parseInt(bal_value);
			int new_amnt = bal_amnt + rew2;
			bal_value = Integer.toString(new_amnt);
			Editor editor = shared_prefs.edit();
			editor.putString(balance, bal_value);
			editor.apply();
			
			File home_dir = new File(getConDir(this), "0");
			if (home_dir.exists()) {
				home_dir.delete();
			}
			
		}
		else {
			c.close();
			dbh.close();
		}
	}
	static File getConDir(Context ctxt) {
		 return(new File(ctxt.getFilesDir(), ConUpdateService.CON_BASEDIR));
	 }
}
