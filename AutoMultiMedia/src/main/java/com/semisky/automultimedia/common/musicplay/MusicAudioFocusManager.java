package com.semisky.automultimedia.common.musicplay;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;

import com.semisky.automultimedia.MediaApplication;
import com.semisky.automultimedia.common.utils.LogUtil;

/**
 * Created by Anter on 2018/6/26.
 * observer(观察者) Observable
 */

public class MusicAudioFocusManager {
    private static final String TAG = MusicAudioFocusManager.class.getSimpleName();
    private static MusicAudioFocusManager _instance;
    private AudioManager mAudioManager;
    private OnAudioFocusChangeListener mObserverOnAudioFocusChangeListener;// 暂存观察者

    public static final int INVALID = -65535;

    /**
     * 是否有音频焦点
     */
    private boolean mHasAudiofocus = false;
    // 当前音频焦点
    private int mCurAudioFocusType = INVALID;


    private MusicAudioFocusManager() {
        mAudioManager = (AudioManager) MediaApplication.getContext().getSystemService(Context.AUDIO_SERVICE);
    }

    public static MusicAudioFocusManager getInstance() {
        if (null == _instance) {
            _instance = new MusicAudioFocusManager();
        }
        return _instance;
    }

    /**
     * 重置当前音频焦点标识
     */
    public void resetCurAudioFocusType() {
        LogUtil.i(TAG, "resetCurAudioFocusType() ..." + INVALID);
        this.mCurAudioFocusType = INVALID;
    }

    /**
     * 设置当前音频焦点标识
     *
     * @param audioFocusType
     */
    private void setmCurAudioFocusType(int audioFocusType) {
        this.mCurAudioFocusType = audioFocusType;
    }

    /**
     * 注册音频焦点观察者
     *
     * @param observer
     */
    public void registerObserverOnAudioFocusChangeListener(OnAudioFocusChangeListener observer) {
        this.mObserverOnAudioFocusChangeListener = observer;
    }

    /**
     * 反注册音频焦点观察者
     */
    public void unregisterObserverOnAudioFocusChangeListener() {
        this.mObserverOnAudioFocusChangeListener = null;
    }


    /**
     * 被观察者监听
     */
    private OnAudioFocusChangeListener mObservableOnAudioFocusChangeListener = new OnAudioFocusChangeListener() {

        @Override
        public void onAudioFocusChange(int focusChange) {
            LogUtil.i(TAG, "=======>onAudioFocusChange() focusChange=" + focusChange);
            setmCurAudioFocusType(focusChange);
            if (null != mObserverOnAudioFocusChangeListener) {
                mObserverOnAudioFocusChangeListener.onAudioFocusChange(focusChange);
            }

        }
    };

    /**
     * 申请音频焦点
     */
    public void onRequestAudioFocus() {
        if (!this.mHasAudiofocus && null != mAudioManager) {
            this.mHasAudiofocus = true;
            this.mCurAudioFocusType = AudioManager.AUDIOFOCUS_GAIN;
            int result = mAudioManager.requestAudioFocus(mObservableOnAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            LogUtil.i(TAG, "onRequestAudioFocus() ..." + result);
        }
    }

    /**
     * 注销音频焦点
     */
    public void onAbandonAudioFocus() {
        if (null != mAudioManager) {
            this.mHasAudiofocus = false;
            resetCurAudioFocusType();
            mAudioManager.abandonAudioFocus(mObservableOnAudioFocusChangeListener);
            LogUtil.i(TAG, "onAbandonAudioFocus() ...");
        }
    }

    /**
     * 是否有音频焦点
     *
     * @return
     */
    public boolean isHasAudiofocus() {
        return this.mHasAudiofocus;
    }

    /**
     * 是否暂停策略
     *
     * @return
     */
    public boolean isPauseStrategys() {
        switch (mCurAudioFocusType) {
            case AudioManager.AUDIOFOCUS_LOSS:
                LogUtil.i(TAG, "isPauseStrategys() AUDIOFOCUS_LOSS");
                return true;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                LogUtil.i(TAG, "isPauseStrategys() AUDIOFOCUS_LOSS_TRANSIENT");
                return true;
            case INVALID:
                LogUtil.i(TAG, "isPauseStrategys() INVALID ...");
                return true;
            case AudioManager.AUDIOFOCUS_GAIN:
                LogUtil.i(TAG, "isPauseStrategys() AUDIOFOCUS_GAIN ...");
                return false;
            default:
                LogUtil.i(TAG, "isPauseStrategys() NORMAL PLAYBACK ...");
                return false;
        }
    }


}
