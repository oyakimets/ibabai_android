package com.ibabai.android.proto;

import android.os.Bundle;

public class TosContentFragment extends AbstractContentFragment {
	private static final String KEY_FILE="file";
	
	protected static TosContentFragment newInstance(String file) {
		TosContentFragment f=new TosContentFragment();
		
		Bundle args=new Bundle();
		args.putString(KEY_FILE, file);
		f.setArguments(args);
		
		return(f);
	}
	@Override
	String getPage() {
		return(getArguments().getString(KEY_FILE));
	}

}
