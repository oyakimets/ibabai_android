package com.ibabai.android.proto;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

public class ARIntentService extends IntentService {
	private SharedPreferences shared_prefs;
	public static final String PREFERENCES = "MyPrefs";
	private int previous_type;
	
	public ARIntentService() {
		super("ARIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		shared_prefs = getApplicationContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
		
		if (ActivityRecognitionResult.hasResult(intent)) {
			ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
			DetectedActivity most_probable_activity = result.getMostProbableActivity();
			int confidence = most_probable_activity.getConfidence();
			int activity_type = most_probable_activity.getType();
			Editor editor = shared_prefs.edit();
			if (!shared_prefs.contains(ARUtils.KEY_PREVIOUS_ACTIVITY_TYPE)) {
				if (isOnFoot(activity_type) && (confidence >= 50)) {
					Intent gf_start_intent = new Intent(this, gfService.class);
					startService(gf_start_intent);
				}
				editor.putInt(ARUtils.KEY_PREVIOUS_ACTIVITY_TYPE, activity_type);
				editor.apply();
			}
			else {
				previous_type = shared_prefs.getInt(ARUtils.KEY_PREVIOUS_ACTIVITY_TYPE, DetectedActivity.UNKNOWN);
				if (isOnFoot(activity_type) && !isOnFoot(previous_type) && (confidence >= 50)) {
					Intent gf_start_intent = new Intent(this, gfService.class);
					startService(gf_start_intent);
				}
				else if (!isOnFoot(activity_type) && isOnFoot(previous_type) && (confidence >= 50)) {
					Intent gf_stop_intent = new Intent(this, gfService.class);
					stopService(gf_stop_intent);
				}
				editor.putInt(ARUtils.KEY_PREVIOUS_ACTIVITY_TYPE, activity_type);
				editor.apply();
			}
		}
	}
	private boolean isOnFoot(int type) {
		if (type == DetectedActivity.ON_FOOT) {
			return true;
		}
		else {
			return false;
		}
	}	
}
