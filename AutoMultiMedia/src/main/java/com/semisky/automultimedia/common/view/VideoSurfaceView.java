package com.semisky.automultimedia.common.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;

import com.semisky.automultimedia.common.utils.AppUtils;
import com.semisky.automultimedia.common.utils.OnMediaChangeStateListener;
import com.semisky.automultimedia.common.utils.LogUtil;
import com.semisky.automultimedia.common.utils.VideoAudioFocusManager;

import java.io.IOException;

/**
 * Displays a video file. The VideoView class can load images from various
 * sources (such as resources or content providers), takes care of computing its
 * measurement from the video so that it can be used in any layout manager, and
 * provides various display options such as scaling and tinting.
 */
public class VideoSurfaceView extends SurfaceView /* implements MediaPlayerControl */
{
	private static final boolean DEBUG = true;

	private static String TAG = "VideoView";

	/** 音量最低比例 */
	private static float VOL_MIN_RATIO = 0.0f;// 音量最低比例
	/** 音量最高比例 */
	private static final float VOL_MAX_RATIO = 1.0f;
	/** 每次音量减小比例 */
	private static final float VOL_STEP_DEC = 0.1f;
	/** 每次音量增大比例 */
	private static final float VOL_STEP_INC = 0.1f;
	/** 每次音量减小时间间隔 */
	private static final int FADE_DOWN_DELAY = 50;
	/** 每次音量增大时间间隔 */
	private static final int FADE_UP_DELAY = 100;

	/** 播放状态-无音乐文件 */
	public static final int MPV_NOFILE = -1;
	/** 播放状态-当前音乐文件无效 */
	public static final int MPV_INVALID = 0;
	/** 播放状态-准备就绪 */
	public static final int MPV_PREPARE = 1;
	/** 播放状态-播放中 */
	public static final int MPV_PLAYING = 2;
	/** 播放状态-暂停 */
	public static final int MPV_PAUSE = 3;
	/** 播放状态-停止 */
	public static final int MPV_STOP = 4;
	/** 是否被销毁 */
	public static boolean isDestroy=true;

	private Context mContext;

	// settable by the client
	private Uri mUri;
	// @20170704 liuyong add
	private String mCurPlayPath;
	private int mDuration;

	// All the stuff we need for playing and showing a video
	private SurfaceHolder mSurfaceHolder = null;
	private MediaPlayer mMediaPlayer = null;
	private boolean mIsPrepared;
	private int mVideoWidth;
	private int mVideoHeight;
	private int mSurfaceWidth;
	private int mSurfaceHeight;
	private OnCompletionListener mOnCompletionListener;
	private MediaPlayer.OnPreparedListener mOnPreparedListener;
	private MediaPlayer.OnInfoListener mOnInfoListener;
	private OnMediaChangeStateListener mOnMediaChangeStateListener;
	private int mCurrentBufferPercentage;
	private OnErrorListener mOnErrorListener;
	private boolean mStartWhenPrepared;
	private int mSeekWhenPrepared;

	private AudioManager mAudioManager;

	public boolean isPrepared() {
		return mIsPrepared;
	}

	/**
	 * 视频播放器工作状态
	 */
	private int mVideoPlayeWorkState;

	/**
	 * 获取视频播放器工作状态
	 *
	 * @return
	 */
	public int getmVideoPlayeWorkState() {
		Log.d(TAG, "getmVideoPlayeWorkState(): " + mVideoPlayeWorkState);
		return mVideoPlayeWorkState;
	}

	/**
	 * 设置更新视频播放器工作状态
	 *
	 * @param mVideoPlayeWorkState
	 */
	public void setmVideoPlayeWorkState(int mVideoPlayeWorkState) {
		this.mVideoPlayeWorkState = mVideoPlayeWorkState;
		Log.d(TAG, "setmVideoPlayeWorkState(): " + mVideoPlayeWorkState);
	}


	private MySizeChangeLinstener mMyChangeLinstener;

	public int getVideoWidth() {
		return mVideoWidth;
	}

	public int getVideoHeight() {
		return mVideoHeight;
	}

	public void setVideoScale(int width, int height) {
		LayoutParams lp = getLayoutParams();
		lp.height = height;
		lp.width = width;
		setLayoutParams(lp);
	}

	public interface MySizeChangeLinstener {
		public void doMyThings();
	}

	public void setMySizeChangeLinstener(MySizeChangeLinstener l) {
		mMyChangeLinstener = l;
	}

	public VideoSurfaceView(Context context) {
		super(context);
		mContext = context;
		initVideoView();
	}

	public VideoSurfaceView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		mContext = context;
		initVideoView();
	}

	public VideoSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initVideoView();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
		int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
		setMeasuredDimension(width, height);
	}



	private void initVideoView() {
		mVideoWidth = 0;
		mVideoHeight = 0;
		getHolder().addCallback(mSHCallback);
		getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		setFocusable(true);
		setFocusableInTouchMode(true);
		requestFocus();

		initAudioManager();
	}

	public void setVideoPath(String path) {
		setVideoURI(Uri.parse(path));
		setmVideoPath(path);
	}

	public void setVideoURI(Uri uri) {
		mUri = uri;
		mStartWhenPrepared = false;
		mSeekWhenPrepared = 0;
		openVideo();
		requestLayout();
		invalidate();
	}

	public void setmVideoPath(String url) {
		mCurPlayPath = null;
		if (null != mUri) {
			mCurPlayPath = mUri.toString();
		}
	}

	public String getVideoPath() {
		return mCurPlayPath;
	}

	public void stopPlayback() {
		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
			// vAbandonAudioFocus();
			unregisterAudioFocusChangeListener();
			notifyMediaStop();
		}
	}

	private void openVideo() {
		if (mUri == null || mSurfaceHolder == null) {
			// not ready for playback just yet, will try again later
			return;
		}
		// Tell the music playback service to pause
		// TODO: these constants need to be published somewhere in the
		// framework.
		Intent i = new Intent("com.android.music.musicservicecommand");
		i.putExtra("command", "pause");
		mContext.sendBroadcast(i);

		if (mMediaPlayer != null) {
			mMediaPlayer.reset();
			mMediaPlayer.release();
			mMediaPlayer = null;
			notifyMediaReset();// 通知界面媒体重置
		}

		try {
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setOnPreparedListener(mPreparedListener);
			mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
			mIsPrepared = false;
			mStartWhenPrepared = true;
			Log.v(TAG, "reset duration to -1 in openVideo");
			mDuration = -1;
			mMediaPlayer.setOnCompletionListener(mCompletionListener);
			mMediaPlayer.setOnErrorListener(mErrorListener);
			mMediaPlayer.setOnInfoListener(mInfoListener);
			mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
			mCurrentBufferPercentage = 0;
			mMediaPlayer.setDataSource(mContext, mUri);
			mMediaPlayer.setDisplay(mSurfaceHolder);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mMediaPlayer.setScreenOnWhilePlaying(true);
			mMediaPlayer.prepareAsync();
			notifyMediaPrepare();
			registerAudioFocusChangeListener();
		} catch (IOException ex) {
			Log.w(TAG, "Unable to open content: " + mUri);
			return;
		} catch (IllegalArgumentException ex) {
			Log.w(TAG, "Unable to open content: " + mUri);
			return;
		}
	}

	public void setOnMediaChangeStateListener(OnMediaChangeStateListener listener) {
		this.mOnMediaChangeStateListener = listener;
	}

	MediaPlayer.OnInfoListener mInfoListener = new MediaPlayer.OnInfoListener() {

		@Override
		public boolean onInfo(MediaPlayer mp, int what, int extra) {
			// TODO Auto-generated method stub
			LogUtil.i(TAG,"===========>OnInfoListener.onInfo() what="+what+",extra="+extra);
			notifyMediaInfo(what, extra);// 通知界面媒体指示信息
			if (mOnInfoListener != null) {
				mOnInfoListener.onInfo(mp, what, extra);
			}
			return false;
		}
	};
	MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
		public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
			mVideoWidth = mp.getVideoWidth();
			mVideoHeight = mp.getVideoHeight();

			if (mMyChangeLinstener != null) {
				mMyChangeLinstener.doMyThings();
			}

			if (mVideoWidth != 0 && mVideoHeight != 0) {
				getHolder().setFixedSize(mVideoWidth, mVideoHeight);
			}
		}
	};

	MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
		public void onPrepared(MediaPlayer mp) {
			LogUtil.i(TAG,"================>onPrepared() ...");
			// briefly show the mediacontroller
			mIsPrepared = true;
			notifyMediaPrepareFinish();// 通知界面准备媒体资源完成
			iRequestAudioFocus();
			if (mOnPreparedListener != null) {
				mOnPreparedListener.onPrepared(mMediaPlayer);
			}
			// if (mMediaController != null) {
			// mMediaController.setEnabled(true);
			// }
			mVideoWidth = mp.getVideoWidth();
			mVideoHeight = mp.getVideoHeight();
			if (mVideoWidth != 0 && mVideoHeight != 0) {
				// Log.i("@@@@", "video size: " + mVideoWidth +"/"+
				// mVideoHeight);
				getHolder().setFixedSize(mVideoWidth, mVideoHeight);
//				if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
					// We didn't actually change the size (it was already at the
					// size
					// we need), so we won't get a "surface changed" callback,
					// so
					// start the video here instead of in the callback.
					if (mSeekWhenPrepared != 0) {
						mMediaPlayer.seekTo(mSeekWhenPrepared);
						mSeekWhenPrepared = 0;
					}
					if (mStartWhenPrepared) {
						mMediaPlayer.start();
						mStartWhenPrepared = false;
					}
//				}
			} else {
				// We don't know the video size yet, but should start anyway.
				// The video size might be reported to us later.
				if (mSeekWhenPrepared != 0) {
					mMediaPlayer.seekTo(mSeekWhenPrepared);
					mSeekWhenPrepared = 0;
				}
				if (mStartWhenPrepared) {
					mMediaPlayer.start();
					mStartWhenPrepared = false;
				}
			}
		}
	};

	private OnCompletionListener mCompletionListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mp) {

			notifyMediaPlayCompletion();// 媒体播放完成
			if (mOnCompletionListener != null) {
				mOnCompletionListener.onCompletion(mMediaPlayer);
			}
		}
	};

	private OnErrorListener mErrorListener = new OnErrorListener() {
		public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
			Log.d(TAG, "Error: " + framework_err + "," + impl_err);

			/* If an error handler has been supplied, use it and finish. */
			notifyMediaPrepareFail(framework_err, impl_err);// 通知界面媒体准备失败
			if (mOnErrorListener != null) {
				if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
					return true;
				}
			}

			/*
			 * Otherwise, pop up an error dialog so the user knows that
			 * something bad has happened. Only try and pop up the dialog if
			 * we're attached to a window. When we're going away and no longer
			 * have a window, don't bother showing the user an error.
			 */
			if (getWindowToken() != null) {
				Resources r = mContext.getResources();
				int messageId;

				/*
				 * if (framework_err ==
				 * MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
				 * messageId = com.android.internal.R.string.
				 * VideoView_error_text_invalid_progressive_playback; } else {
				 * messageId =
				 * com.android.internal.R.string.VideoView_error_text_unknown; }
				 * 
				 * new AlertDialog.Builder(mContext)
				 * .setTitle(com.android.internal
				 * .R.string.VideoView_error_title) .setMessage(messageId)
				 * .setPositiveButton
				 * (com.android.internal.R.string.VideoView_error_button, new
				 * DialogInterface.OnClickListener() { public void
				 * onClick(DialogInterface dialog, int whichButton) { If we get
				 * here, there is no onError listener, so at least inform them
				 * that the video is over.
				 * 
				 * if (mOnCompletionListener != null) {
				 * mOnCompletionListener.onCompletion(mMediaPlayer); } } })
				 * .setCancelable(false) .show();
				 */
			}
			return true;
		}
	};

	private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			mCurrentBufferPercentage = percent;
		}
	};

	/**
	 * Register a callback to be invoked when the media file is loaded and ready
	 * to go.
	 * 
	 * @param l
	 *            The callback that will be run
	 */
	public void setOnPreparedListener(MediaPlayer.OnPreparedListener l) {
		mOnPreparedListener = l;
	}

	/**
	 * Register a callback to be invoked when the end of a media file has been
	 * reached during playback.
	 * 
	 * @param l
	 *            The callback that will be run
	 */
	public void setOnCompletionListener(OnCompletionListener l) {
		mOnCompletionListener = l;
	}

	/**
	 * Register a callback to be invoked when an error occurs during playback or
	 * setup. If no listener is specified, or if the listener returned false,
	 * VideoView will inform the user of any errors.
	 * 
	 * @param l
	 *            The callback that will be run
	 */
	public void setOnErrorListener(OnErrorListener l) {
		mOnErrorListener = l;
	}

	public void setOnInfoListener(MediaPlayer.OnInfoListener l) {
		mOnInfoListener = l;
	}

	SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
			mSurfaceWidth = w;
			mSurfaceHeight = h;
			if (mMediaPlayer != null && mIsPrepared && mVideoWidth == w && mVideoHeight == h) {
				if (mSeekWhenPrepared != 0) {
					mMediaPlayer.seekTo(mSeekWhenPrepared);
					mSeekWhenPrepared = 0;
				}

				// mMediaPlayer.start();//modify data:2016-12-09
				// 切换视频缩放比例会触发视频播放，所以暂时注释掉
			}
		}

		public void surfaceCreated(SurfaceHolder holder) {
			Log.i("xiongjun","surfaceCreated");
			mSurfaceHolder = holder;
			openVideo();// 当前Surface不可见时，会销毁当前播放视频，所以这个方法是用来恢复当前播放视频资源
			VideoAudioFocusManager.getInstance(getContext()).setVideoWindowForegroundStatus(true);
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.i("xiongjun","surfaceDestroyed");
			mSurfaceHolder = null;

			if (mMediaPlayer != null ) {
				mUri = null;
//				mSeekWhenPrepared = mMediaPlayer.getCurrentPosition();
				notifyMediaUpdateProgress(mMediaPlayer.getCurrentPosition());
				mMediaPlayer.reset();
				mMediaPlayer.release();
				mMediaPlayer = null;
				// vAbandonAudioFocus();// 注销音频焦点
				unregisterAudioFocusChangeListener();
				notifyMediaStop();
				VideoAudioFocusManager.getInstance(getContext()).setVideoWindowForegroundStatus(false);
			}
		}

	};

	public void start() {
		 LogUtil.d(TAG,"start() mIsPrepared-----------------------------: "+mIsPrepared);
		if (mMediaPlayer != null &&mIsPrepared && !mMediaPlayer.isPlaying()) {
			mCurrentVolume = 0.0f;// 设置渐变音初始值成员变量
			mMediaPlayer.setVolume(0, 0);// 预防声音过大而产生pop音，将播放器音量设置到0,方便渐变上升，
			mMediaplayerHandler.removeMessages(FADEDOWN);// 获得音频焦点移除-渐变下降音量消息
			mMediaplayerHandler.sendEmptyMessage(FADEUP);// 获得音频焦点发送-渐变上升音量消息
			iRequestAudioFocus();// 播放时申请音频焦点
			// checkIsRequestAudiofocus();// 检查音频焦点
			mMediaPlayer.start();
			mStartWhenPrepared = false;
			notifyMediaStart();// 通知界面媒体开始播放
			LogUtil.d(TAG, "MediaPlayer->start() ---------------ok!!!");
		} else {
			if(mIsPrepared && null != mMediaPlayer && mMediaPlayer.isPlaying()){
				notifyMediaStart();// 通知界面媒体开始播放
			}
			mStartWhenPrepared = true;
		}
	}


	public void pause() {
		if (mMediaPlayer != null && mIsPrepared) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.pause();
				notifyMediaPause();// 通知界面媒体暂停
			}
		}
		mStartWhenPrepared = false;
	}

	public int getDuration() {
		if (mMediaPlayer != null && mIsPrepared) {
			if (mDuration > 0) {
				return mDuration;
			}
			mDuration = mMediaPlayer.getDuration();
			return mDuration;
		}
		mDuration = -1;
		return mDuration;
	}

	public int getCurrentPosition() {
		if (mMediaPlayer != null && mIsPrepared) {
			return mMediaPlayer.getCurrentPosition();
		}
		return 0;
	}

	public void seekTo(int msec) {
		LogUtil.d(TAG, "-------------------------------->seekTo() msec: " + msec);
		if (mMediaPlayer != null && mIsPrepared) {
			mMediaPlayer.seekTo(msec);
		} else {
			mSeekWhenPrepared = msec;
		}
	}

	public boolean isPlaying() {
		if (mMediaPlayer != null && mIsPrepared) {
			return mMediaPlayer.isPlaying();
		}
		return false;
	}

	public int getBufferPercentage() {
		if (mMediaPlayer != null) {
			return mCurrentBufferPercentage;
		}
		return 0;
	}

	/** 初始化音频管理器 */
	void initAudioManager() {
		if (DEBUG)
			Log.d(TAG, "AudioManager init ...");
		mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
	}

	/**
	 * 短暂失去音频焦点标记
	 */
	private boolean mPauseByTransientLossOfFocus = false;
	private float mCurrentVolume = 1.0f;

	private static final int FADEDOWN = 5;
	private static final int FADEUP = 6;
	private static final int FOCUSCHANGE = 7;
	private static final int MSG_PAUSE_VIDEO_SERVICE = 8;

	private Handler mMediaplayerHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case FADEDOWN:// 音量渐变降低
				if (mMediaPlayer == null)
					return;

				mCurrentVolume -= VOL_STEP_DEC;
				if (mCurrentVolume > VOL_MIN_RATIO) {
					mMediaplayerHandler.sendEmptyMessageDelayed(FADEDOWN, FADE_DOWN_DELAY);
				} else {
					mCurrentVolume = VOL_MIN_RATIO;
				}
				mMediaPlayer.setVolume(mCurrentVolume, mCurrentVolume);
				if (DEBUG)
					Log.d(TAG, "FADEDOWN : " + mCurrentVolume);
				break;
			case FADEUP:// 音量渐变上升
				if (mMediaPlayer == null) {
					return;
				}
				mCurrentVolume += VOL_STEP_INC;
				if (mCurrentVolume < VOL_MAX_RATIO) {
					mMediaplayerHandler.sendEmptyMessageDelayed(FADEUP, FADE_UP_DELAY);
				} else {
					mCurrentVolume = VOL_MAX_RATIO;
				}
				mMediaPlayer.setVolume(mCurrentVolume, mCurrentVolume);
				if (DEBUG)
					Log.d(TAG, "FADEUP : " + mCurrentVolume);
				break;
			case FOCUSCHANGE:// 音频焦点改变
				handlerPlayVideoByAudioFocusCode(msg.arg1);
				break;
			case MSG_PAUSE_VIDEO_SERVICE:// 暂停视频
				if (mMediaPlayer != null) {
					if (mMediaPlayer.isPlaying()) {
						mPauseByTransientLossOfFocus = true;// 设置短暂失去焦点标记
						pause();// 暂停视频
						if (DEBUG)
							Log.d(TAG, "MSG_PAUSE_VIDEO_SERVICE ....");
					}
				}
				break;
			}
		}
	};

	/**
	 * 根据音频焦点处理播放视频
	 */
	void handlerPlayVideoByAudioFocusCode(int focusChange) {
		switch (focusChange) {
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
//			setNaviMinSoundMixingRatio();// 设置Navi最小混音比例
//			mMediaplayerHandler.removeMessages(FADEUP);// 获得音频焦点移除-渐变下降音量消息
//			mMediaplayerHandler.sendEmptyMessage(FADEDOWN);// 获得音频焦点发送-渐变上升音量消息
			if (DEBUG)
				Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ....");
			break;
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
			mMediaplayerHandler.removeMessages(FADEUP);// 获得音频焦点移除-渐变下降音量消息
			mMediaplayerHandler.sendEmptyMessage(FADEDOWN);// 获得音频焦点发送-渐变上升音量消息
			mMediaplayerHandler.sendEmptyMessageDelayed(MSG_PAUSE_VIDEO_SERVICE, 300);// 延时发送暂停音乐消息，预防pop音
			if (DEBUG)
				Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT ....");
			break;
		case AudioManager.AUDIOFOCUS_LOSS:
			mMediaplayerHandler.removeMessages(FADEUP);// 获得音频焦点移除-渐变下降音量消息
			mMediaplayerHandler.sendEmptyMessage(FADEDOWN);// 获得音频焦点发送-渐变上升音量消息
			mMediaplayerHandler.sendEmptyMessageDelayed(MSG_PAUSE_VIDEO_SERVICE, 500);// 延时发送暂停音乐消息，预防pop音
			if (DEBUG)
				Log.d(TAG, "AUDIOFOCUS_LOSS ....");
			break;
		case AudioManager.AUDIOFOCUS_GAIN:
			if (mMediaPlayer != null) {
				if (DEBUG)
					Log.d(TAG, "AUDIOFOCUS_GAIN isPlaying : " + mMediaPlayer.isPlaying()
							+ " , mPauseByTransientLossOfFocus : " + mPauseByTransientLossOfFocus);
				// 1.播放器没有在播放状态; 2.短暂焦点失去标记为true
				if (!mMediaPlayer.isPlaying() && mPauseByTransientLossOfFocus) {
					mPauseByTransientLossOfFocus = false;// 将短暂失去焦点标记恢复初始值
					mCurrentVolume = 0.0f;// 设置渐变音初始值成员变量
					mMediaPlayer.setVolume(0, 0);// 预防声音过大而产生pop音，将播放器音量设置到0,方便渐变上升，
					mMediaplayerHandler.removeMessages(FADEDOWN);// 获得音频焦点移除-渐变下降音量消息
					mMediaplayerHandler.sendEmptyMessage(FADEUP);// 获得音频焦点发送-渐变上升音量消息
					start();// 恢复播放已暂停视频
				} else {
					mMediaplayerHandler.removeMessages(FADEDOWN);// 获得音频焦点移除-渐变下降音量消息
					mMediaplayerHandler.sendEmptyMessage(FADEUP);// 获得音频焦点发送-渐变上升音量消息
				}
				AppUtils.openAndroidStreamVolumeByVideo();
			}
			if (DEBUG)
				Log.d(TAG, "AUDIOFOCUS_GAIN ....");
			break;
		}
	}

	/**
	 * 申请音频焦点
	 */
	void iRequestAudioFocus() {
		VideoAudioFocusManager.getInstance(mContext).requestAudioFocus();
	}

	VideoAudioFocusManager.IAudioFocusChangeListener mCustomAudioFocusChangeListener = new VideoAudioFocusManager.IAudioFocusChangeListener() {
		@Override
		public void onAudioFocusChange(int focusChange) {
			if (mMediaplayerHandler == null)
				return;
			mMediaplayerHandler.obtainMessage(FOCUSCHANGE, focusChange, 0).sendToTarget();
		}
	};

	private void registerAudioFocusChangeListener() {
		VideoAudioFocusManager.getInstance(mContext).registerAudioFocusChangeListener(mCustomAudioFocusChangeListener);
	}

	private void unregisterAudioFocusChangeListener() {
		VideoAudioFocusManager.getInstance(mContext).unregisterAudioFocusChangeListener();
	}

	// 静音
	public boolean muteVolume() {
		if (mAudioManager != null && mMediaPlayer != null) {
			mMediaPlayer.setVolume(0, 0);
			mCurrentVolume = 0.0f;
			mMediaplayerHandler.removeMessages(FADEUP);
			return true;
		}
		return false;
	}

	// 取消静音
	public boolean unMuteVolume() {
		if (mAudioManager != null && mMediaPlayer != null) {
			mMediaplayerHandler.sendEmptyMessage(FADEUP);
			return true;
		}
		return false;
	}

	/**
	 * 设置Navi最小混音比例
	 */
	private void setNaviMinSoundMixingRatio() {
		int ratio = Settings.System.getInt(mContext.getContentResolver(), "semisky_car_navmixing", 7);
		if (ratio <= 12) {
			VOL_MIN_RATIO = (88 - (ratio - 1) * 8) / 100f;
		} else {
			VOL_MIN_RATIO = (88 - (7 - 1) * 8) / 100f;
		}
		if (VOL_MIN_RATIO > 0 && VOL_MIN_RATIO <= 1) {// 微调最小Navi最小混音比例
			VOL_MIN_RATIO -= 0.2f;
		}
		Log.d(TAG, "setNaviMinSoundMixingRatio() " + VOL_MIN_RATIO + ",ratio: " + ratio);
	}

	public void setStopSpecifyMimeVideo() {
		Log.d(TAG, "setStopSpecifyMimeVideo()");
		if (mOnErrorListener != null && mMediaPlayer != null) {
			if (mOnErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNSUPPORTED,
					MediaPlayer.MEDIA_ERROR_UNSUPPORTED)) {
			}
		}
	}

	/**
	 * 通知界面媒体重置
	 */
	private void notifyMediaReset() {
		if (null != mOnMediaChangeStateListener) {
			mOnMediaChangeStateListener.onMediaReset();
		}
	}

	/**
	 * 通知界面准备媒体资源
	 */
	private void notifyMediaPrepare() {
		if (null != mOnMediaChangeStateListener) {
			mOnMediaChangeStateListener.onMediaPrepare();
		}
	}

	/**
	 * 通知界面准备媒体资源完成
	 */
	private void notifyMediaPrepareFinish() {
		if (null != mOnMediaChangeStateListener) {
			mOnMediaChangeStateListener.onMediaPrepareFinish();
		}
	}

	/**
	 * 通知界面媒体开始播放
	 */
	private void notifyMediaStart() {
		if (null != mOnMediaChangeStateListener) {
			mOnMediaChangeStateListener.onMediaStart();
		}
	}

	/**
	 * 通知界面媒体准备失败
	 */
	private void notifyMediaPrepareFail(int what, int extra) {
		if (null != mOnMediaChangeStateListener) {
			mOnMediaChangeStateListener.onMediaPrepareFail(what, extra);
		}
	}

	/**
	 * 通知界面媒体暂停
	 */
	private void notifyMediaPause() {
		if (null != mOnMediaChangeStateListener) {
			mOnMediaChangeStateListener.onMediaPause();
		}
	}

	/**
	 * 通知界面媒体停止
	 */
	private void notifyMediaStop() {
		if (null != mOnMediaChangeStateListener) {
			mOnMediaChangeStateListener.onMediaStop();
			isDestroy=true;
		}
	}

	/**
	 * 媒体播放完成
	 */
	private void notifyMediaPlayCompletion() {
		if (null != mOnMediaChangeStateListener) {
			mOnMediaChangeStateListener.onMediaPlayCompletion();
		}
	}

	/**
	 * 通知界面媒体指示信息
	 */
	private void notifyMediaInfo(int what, int extra) {
		if (null != mOnMediaChangeStateListener) {
			mOnMediaChangeStateListener.onMediaInfo(what, extra);
		}
	}

	/**
	 * 通知界面更新播放进度
	 */
	private void notifyMediaUpdateProgress(int progress) {
		if (null != mOnMediaChangeStateListener) {
			mOnMediaChangeStateListener.onUpdateProgress(progress);
		}
	}

}
