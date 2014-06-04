package com.ibabai.android.proto;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class PresentationAdapter extends FragmentStatePagerAdapter {
	private static final String[] presentation = {"slide_0.html", "slide_1.html", "slide_2.html"};
	public PresentationAdapter(FragmentManager mgr) {
		super(mgr);
		
	}
	@Override
	public Fragment getItem(int position) {
		String path=presentation[position];
		return (SimpleContentFragment.newInstance("file:///android_asset/intro/" + path));
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return (3);
	}
}
