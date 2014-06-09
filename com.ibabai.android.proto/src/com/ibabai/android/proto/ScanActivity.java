package com.ibabai.android.proto;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ScanActivity extends Activity implements OnClickListener {
	public static final String PREFERENCES = "MyPrefs";	
	public static final String store_id = "store_id";
	SharedPreferences shared_prefs;
	private String store_prox;
	private static final String testCode="7622300751661";
	private Button scanBtn;
	private TextView formatTxt, contentTxt, scanTitle, scanDesc, scanResult;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scanner);
        
        scanTitle = (TextView)findViewById(R.id.scan_title);
        scanDesc=(TextView)findViewById(R.id.scan_desc);
        scanTitle.setText("Test Scanning");
        scanDesc.setText("You can scan for testing purposes only. You can register purchase only outside of te store.");
        scanBtn = (Button)findViewById(R.id.scan_button);
        formatTxt=(TextView)findViewById(R.id.scan_format);
        contentTxt=(TextView)findViewById(R.id.scan_content);
        
        scanBtn.setOnClickListener(this);
        
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayShowTitleEnabled(false);
        
        shared_prefs = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        store_prox = shared_prefs.getString(store_id, "0");
        if (!store_prox.equals("0")) {
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
        store_prox = shared_prefs.getString(store_id, "0");
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				scanResult=(TextView)findViewById(R.id.scan_result);
				String scanContent = intent.getStringExtra("SCAN_RESULT");
				String scanFormat = intent.getStringExtra("SCAN_RESULT_FORMAT");
				formatTxt.setText("FORMAT: "+scanFormat);
				contentTxt.setText("CONTENT: "+scanContent);
				/*"for" loop through product codes
				 * 
				 */
				if (scanContent.equals(testCode)) {
					scanResult.setBackgroundResource(R.drawable.scan_res_s);
					if(!store_prox.equals("0")) {						
						scanResult.setText("Success! This product participate in a promo.");
						/*write to database and send action to the server
						 * 
						 */
					}
					else {
						scanResult.setText("Success! Your balance is credited as per promo rules.");
					}
				}
				else {					
					scanResult.setText("Product is not found. Please check promo rules.");
					scanResult.setBackgroundResource(R.drawable.scan_res_f);
				}
			}
			else if (resultCode == RESULT_CANCELED) {
				Toast toast = Toast.makeText(getApplicationContext(), "Scanning error. Try again!", Toast.LENGTH_LONG);
				toast.show();
			}
		}		
	}
}
