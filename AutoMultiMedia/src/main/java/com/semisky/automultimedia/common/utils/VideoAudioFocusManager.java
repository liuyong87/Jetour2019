package com.semisky.automultimedia.common.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;

import com.semisky.automultimedia.common.sql.SharePreferenceUtil;
import com.semisky.automultimedia.common.strategys.VideoStrategyManager;

import static com.semisky.autoservice.manager.AudioManager.STREAM_ANDROID;
import static com.semisky.autoservice.manager.AudioManager.STREAM_ANDROID_VIDEO;

/**
 * 视频音频焦点管理
 * 
 * @author liuyong
 * 
 */
public class VideoAudioFocusManager {
	private static final String TAG = "VideoAudioFocusManager";
	private static VideoAudioFocusManager instance;
	private AudioManager mAudioManager;
	private Context mContext;
	/**
	 * 监听所有音频焦点接口
	 */
	private IAudioFocusChangeListener mCustomAudioFocusChangeListener;
	/**
	 * 只监听永久音频焦点接口
	 */
	private IForeverLossAudiofocusListener mIForeverLossAudiofocusListener;
	/**
	 * 是否有音频焦点
	 */
	private boolean hasAudiofocus = false;
	/**
	 * 视频界面是否是在前台标记
	 */
	private boolean mIsForegroundByVideoWindow = false;

	private VideoAudioFocusManager(Context ctx) {
		this.mContext = ctx;
		LogUtil.i(TAG, "init()...");
		if (mAudioManager == null) {
			mAudioManager = (AudioManager) mContext.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
		}
	}

	public static VideoAudioFocusManager getInstance(Context ctx) {
		if (instance == null) {
			instance = new VideoAudioFocusManager(ctx);
		}
		return instance;
	}

	/**
	 * 视频界面是否在前台
	 * 
	 * @return
	 */
	public boolean isForegroundByVideoWindow() {
		return mIsForegroundByVideoWindow;
	}

	/**
	 * 设置视频窗口状态
	 * 
	 * @param isForegroundByVideoWindow
	 */
	public void setVideoWindowForegroundStatus(boolean isForegroundByVideoWindow) {
		this.mIsForegroundByVideoWindow = isForegroundByVideoWindow;
	}

	/**
	 * 是否有音频焦点
	 * 
	 * @return
	 */
	public boolean isHasAudiofocus() {
		return hasAudiofocus;
	}

	// 音频焦点监听接口
	private OnAudioFocusChangeListener mOnAudioFocusChangeListener = new OnAudioFocusChangeListener() {
		@Override
		public void onAudioFocusChange(int focusChange) {
			LogUtil.i(TAG, "handlerAudioFocusChangeEvent() mIsForegroundByVideoWindow="+mIsForegroundByVideoWindow);
			if (mCustomAudioFocusChangeListener != null && mIsForegroundByVideoWindow) {
				mCustomAudioFocusChangeListener.onAudioFocusChange(focusChange);
			}
			handlerAudioFocusChangeEvent(focusChange);
		}
	};

	// 处理音频焦点事件
	private void handlerAudioFocusChangeEvent(int focusChange) {
		LogUtil.i(TAG,"handlerAudioFocusChangeEvent() focusChange="+focusChange);
		switch (focusChange) {
		case AudioManager.AUDIOFOCUS_LOSS:
			LogUtil.i(TAG, "AUDIOFOCUS_LOSS");

			if (mIForeverLossAudiofocusListener != null) {
				mIForeverLossAudiofocusListener.onAudioFocusChange(focusChange);
			}
			// 记忆最后是播放状态
			SharePreferenceUtil.setVideoFinallyPlaying(mContext.getApplicationContext(), true);
			VideoStrategyManager.getInstance().setUserPauseState("handlerAudioFocusChangeEvent()",false);
			abandonAudioFocus();
			break;
		}
	}

	/**
	 * 申请永久音频焦点
	 */
	@SuppressLint("InlinedApi")
	public void requestAudioFocus() {
		if (!hasAudiofocus) {
			LogUtil.i(TAG, "requestAudioFocus() ...");
			mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener, AudioManager.STREAM_MUSIC,
					AudioManager.AUDIOFOCUS_GAIN);
			AppUtils.openAndroidStreamVolumeByVideo();
			hasAudiofocus = true;
		}
	}




	/**
	 * 注销永久音频焦点
	 */
	public void abandonAudioFocus() {
		if (mAudioManager != null) {
			LogUtil.i(TAG, "abondonAudioFocus() ...");
			mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
			hasAudiofocus = false;
			unregisterForeverLossAudiofocusListener();
		}
	}

	/**
	 * 永久音频焦点监听接口
	 * 
	 * @author liuyong
	 * 
	 */
	public interface IForeverLossAudiofocusListener {
		public void onAudioFocusChange(int focusChange);
	}

	/**
	 * 注册永久音频焦点监听接口
	 * 
	 * @param listener
	 */
	public void registerForeverLossAudiofocusListener(IForeverLossAudiofocusListener listener) {
		LogUtil.i(TAG, "registerForeverLossAudiofocusListener() ...");
		this.mIForeverLossAudiofocusListener = listener;
	}

	/**
	 * 反注册永久音频焦点监听接口
	 */
	public void unregisterForeverLossAudiofocusListener() {
		if (mIForeverLossAudiofocusListener != null) {
			LogUtil.i(TAG, "unregisterForeverLossAudiofocusListener() ...");
			this.mIForeverLossAudiofocusListener = null;
		}
	}

	/** 音频焦点监听接口 */
	public interface IAudioFocusChangeListener {
		public void onAudioFocusChange(int focusChange);
	}

	/**
	 * 注册音频焦点监听接口
	 * 
	 * @param listener
	 */
	public void registerAudioFocusChangeListener(IAudioFocusChangeListener listener) {
		LogUtil.i(TAG, "registerAudioFocusChangeListener() ...");
		this.mCustomAudioFocusChangeListener = listener;
	}

	/**
	 * 反注册音频焦点监听接口
	 */
	public void unregisterAudioFocusChangeListener() {
		LogUtil.i(TAG, "unregisterAudioFocusChangeListener() ...");
		this.mCustomAudioFocusChangeListener = null;
	}

}
