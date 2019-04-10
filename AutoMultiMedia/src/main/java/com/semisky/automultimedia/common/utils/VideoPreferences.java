package com.semisky.automultimedia.common.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class VideoPreferences {
	private static final String TAG = VideoPreferences.class.getSimpleName();
	private static VideoPreferences instance;

	private static final String VIDEO_SP_NAME = "media_video_peference";

	private VideoPreferences() {

	}

	public static VideoPreferences getInstance() {
		if (instance == null) {
			instance = new VideoPreferences();
		}
		return instance;
	}

	/** 获得偏好对象 */
	private SharedPreferences getSP(Context ctx) {
		return ctx.getSharedPreferences(VIDEO_SP_NAME, Context.MODE_PRIVATE);
	}



	/** 设置当前动态更新目录 */
	public boolean setVideoCurrentDynamicUpdateDirectory(Context ctx, String directory) {
		return getSP(ctx).edit().putString("currentDynamicUpdateDirectory", directory).commit();
	}

	/** 获取当前动态更新目录 */
//	public String getVideoCurrentDynamicUpdateDirectory(Context ctx) {
//		return getSP(ctx).getString("currentDynamicUpdateDirectory", CommonConstants.PATH_USB1);
//	}

}
