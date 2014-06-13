package com.ibabai.android.proto;

import org.json.JSONObject;

public class Vendor {
	JSONObject ven=null;
	
	public Vendor(JSONObject ven) {
		this.ven=ven;
	}
	public int getVendorId() {
		return(ven.optInt("vendor_id"));
	}
	public String getVendorName() {
		return(ven.optString("vendor_name"));
	}	
}
