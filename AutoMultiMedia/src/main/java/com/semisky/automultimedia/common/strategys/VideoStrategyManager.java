package com.semisky.automultimedia.common.strategys;

import android.content.Context;

import com.semisky.automultimedia.MediaApplication;
import com.semisky.automultimedia.common.sql.SharePreferenceUtil;
import com.semisky.automultimedia.common.utils.LogUtil;

import java.io.File;

/**
 * Created by liuyong on 18-3-30.
 */

public class VideoStrategyManager {
    private static final String TAG = VideoStrategyManager.class.getSimpleName();
    private static VideoStrategyManager INSTANCE;

    // 是否用户选择列表文件播放(注：列表使用)
    private boolean mIsUserSelectListPlay = false;

    public void setUserSelectListPlayState(boolean state) {
        this.mIsUserSelectListPlay = state;
    }

    // 是否用户选择列表文件播放
    public boolean isUserSelectListPlay() {
        return this.mIsUserSelectListPlay;
    }

    // 当为true,恢复时，在onMediaPrepare()函数暂停视频
    // 1.上/下一曲时，重置标记 isUserPause = false
    // 2.播放列表歌曲时，重置标记 isUserPause = false
    private boolean isUserPause = false;


    public void setUserPauseState(String who,boolean state){
        this.isUserPause = state;
        LogUtil.i(TAG,"setUserPauseState() who="+who+" , state="+state);
    }

    public boolean getUserPauseState(){
        return this.isUserPause;
    }


    public static VideoStrategyManager getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new VideoStrategyManager();
        }
        return INSTANCE;
    }

    public interface OnRecoverMemoryPlaybackCallback {
        void onPlayItemResource(String url, int progress, boolean isAuotPlay);
        void onSuccessResult(String url, int progress, boolean isAuotPlay);

        void onFailResult();
    }

    private OnRecoverMemoryPlaybackCallback mOnRecoverMemoryPlaybackCallback = null;

    public void registerOnRecoverMemoryPlaybackCallback(OnRecoverMemoryPlaybackCallback callback) {
        this.mOnRecoverMemoryPlaybackCallback = callback;
    }

    public void unregisterOnRecoverMemoryPlaybackCallback() {
        this.mOnRecoverMemoryPlaybackCallback = null;
    }

    // 播放列表资源
    private void nofityPlayItemResource(String url, int progress, boolean isAuotPlay) {
        if (null != mOnRecoverMemoryPlaybackCallback) {
            mOnRecoverMemoryPlaybackCallback.onPlayItemResource(url, progress, isAuotPlay);
        }
    }

    private void nofitySuccessResult(String url, int progress, boolean isAuotPlay) {
        if (null != mOnRecoverMemoryPlaybackCallback) {
            mOnRecoverMemoryPlaybackCallback.onSuccessResult(url, progress, isAuotPlay);
        }
    }

    private void notifyFailReulst() {
        if (null != mOnRecoverMemoryPlaybackCallback) {
            mOnRecoverMemoryPlaybackCallback.onFailResult();
        }
    }

    private Context getContext() {
        return MediaApplication.getContext();
    }

    // 开启恢复断点记忆播放线程
    public void startRecoveryMemoryPlaybackThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                recoveryMemoryPlayback();
            }
        }).start();
    }


    // 恢复断点记忆播放
    private void recoveryMemoryPlayback() {
        String finallSaveVideoUrl = SharePreferenceUtil.getCurrentPlayingVideoUrl(getContext());
        int finallSaveVideoProgress = SharePreferenceUtil.getCurrentPlayVideoProgress(getContext());

        LogUtil.i(TAG, "===========================");
        LogUtil.i(TAG, "recoveryMemoryPlayback() finallSaveVideoUrl=" + finallSaveVideoUrl);
        LogUtil.i(TAG, "recoveryMemoryPlayback() finallSaveVideoProgress=" + finallSaveVideoProgress);
        LogUtil.i(TAG, "recoveryMemoryPlayback() isUserSelectListPlay=" + mIsUserSelectListPlay);
        LogUtil.i(TAG, "===========================");


        if (mIsUserSelectListPlay) {
            // 列表播放，禁止恢复音乐
            mIsUserSelectListPlay = false;
            return;
        } else if (null != finallSaveVideoUrl && new File(finallSaveVideoUrl).exists()) {
            // 播放断点记忆
            nofitySuccessResult(finallSaveVideoUrl, finallSaveVideoProgress,true);
        } else {
            // 无断点记忆
            notifyFailReulst();
        }
    }

    public void playListItemResource(String url){
        String videoUrl = SharePreferenceUtil.getCurrentPlayingVideoUrl(getContext());
        int progress = 0;

        if(url.equals(videoUrl)){
            progress = SharePreferenceUtil.getCurrentPlayVideoProgress(getContext());
        }
        nofityPlayItemResource(url,progress,true);
    }
}
