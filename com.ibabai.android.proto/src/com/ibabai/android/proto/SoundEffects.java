package com.ibabai.android.proto;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;

public class SoundEffects {
	public static int coin; 
	private static SoundPool soundPool;	
	private static boolean loaded = false;
	
	public static void initSounds(Context ctxt) {
		soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		coin = soundPool.load(ctxt, R.raw.coin1, 1);
		soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
				loaded = true;
			}
		});
		
	}
	public static  void playSound(Context ctxt, int soundID) {
		if(!loaded) {
			initSounds(ctxt);
		}
		soundPool.play(coin, 1, 1, 1, 0, 1f);
	}
}
