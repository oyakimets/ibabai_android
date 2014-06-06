package com.ibabai.android.proto;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ScanActivity extends Activity implements OnClickListener {
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
	}
	public void onClick(View v) {
		Intent intent=new Intent("com.google.zxing.client.android.SCAN");
		intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
		startActivityForResult(intent, 0);
	}
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				scanResult=(TextView)findViewById(R.id.scan_result);
				String scanContent = intent.getStringExtra("SCAN_RESULT");
				String scanFormat = intent.getStringExtra("SCAN_RESULT_FORMAT");
				formatTxt.setText("FORMAT: "+scanFormat);
				contentTxt.setText("CONTENT: "+scanContent);
				if (scanContent.equals(testCode)) {
					scanResult.setText("Success! Your balance is credited as per promo rules.");
					scanResult.setBackgroundResource(R.drawable.scan_res_s);
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
