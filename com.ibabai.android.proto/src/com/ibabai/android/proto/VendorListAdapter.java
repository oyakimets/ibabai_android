package com.ibabai.android.proto;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class VendorListAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<Drawable> vendor_list;
	
	public VendorListAdapter(Context context, ArrayList<Drawable> vendor_list ) {
		this.context=context;
		this.vendor_list=vendor_list;
	}
	@Override
	public int getCount() {
		return vendor_list.size();
	}

	@Override
	public Object getItem(int position) {
		return vendor_list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.vendor_list_item, null);
        }
          
        ImageView tagIcon = (ImageView) convertView.findViewById(R.id.vendor_tag);
                  
        tagIcon.setImageDrawable(vendor_list.get(position));        
        
         
        return convertView;
	}

}
