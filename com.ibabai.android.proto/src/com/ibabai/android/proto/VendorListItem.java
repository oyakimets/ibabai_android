package com.ibabai.android.proto;

public class VendorListItem {
	private int vendor_tag;
	
	public VendorListItem() {};
	
	public VendorListItem(int vendor_tag) {
		this.vendor_tag=vendor_tag;
	}
	
	public int getVendorTag() {
		return this.vendor_tag;		
	}
	
	public void setVendorTag(int vendor_tag) {
		this.vendor_tag=vendor_tag;
	}
}
