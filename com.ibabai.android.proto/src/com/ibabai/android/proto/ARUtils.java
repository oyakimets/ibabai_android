package com.ibabai.android.proto;

public final class ARUtils {
	public enum REQUEST_TYPE {ADD, REMOVE}

    public static final String APPTAG = "ARSample";    
    public static final String ACTION_CONNECTION_ERROR =
            "com.example.android.activityrecognition.ACTION_CONNECTION_ERROR";
    public static final String ACTION_ACTIVITY_RECOGNITION =
            "com.example.android.activityrecognition.ACTION_ACTIVITY_RECOGNITION";    
    public static final String CATEGORY_AR_SERVICES =
            "com.example.android.activityrecognition.CATEGORY_AR_SERVICES";
    public static final String EXTRA_CONNECTION_ERROR_CODE =
            "com.example.android.activityrecognition.EXTRA_CONNECTION_ERROR_CODE";
    public static final String EXTRA_CONNECTION_REQUEST_TYPE =
            "com.example.android.activityrecognition.EXTRA_CONNECTION_REQUEST_TYPE";
    
    public static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int SECONDS_PER_MINUTE = 60;
    public static final int DETECTION_INTERVAL_MINUTES = 3;
    public static final int DETECTION_INTERVAL_MILLISECONDS =
            MILLISECONDS_PER_SECOND * SECONDS_PER_MINUTE * DETECTION_INTERVAL_MINUTES;

    public static final String KEY_PREVIOUS_ACTIVITY_TYPE =
            "com.example.android.activityrecognition.KEY_PREVIOUS_ACTIVITY_TYPE";
        
}
