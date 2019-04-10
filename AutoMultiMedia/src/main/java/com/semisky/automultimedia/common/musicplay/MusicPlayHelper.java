package com.semisky.automultimedia.common.musicplay;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import com.semisky.automultimedia.MediaApplication;
import com.semisky.automultimedia.R;
import com.semisky.automultimedia.common.bean.MusicInfo;
import com.semisky.automultimedia.common.constant.Definition;
import com.semisky.automultimedia.common.data.MusicDataModule;
import com.semisky.automultimedia.common.interfaces.OnFastBackwardListener;
import com.semisky.automultimedia.common.interfaces.OnFastForwardListener;
import com.semisky.automultimedia.common.mediascan.MediaParaser;
import com.semisky.automultimedia.common.sql.SharePreferenceUtil;
import com.semisky.automultimedia.common.strategys.VoiceManager;
import com.semisky.automultimedia.common.utils.AppUtils;
import com.semisky.automultimedia.common.utils.LogUtil;
import com.semisky.automultimedia.common.utils.USBManager;
import com.semisky.autoservice.manager.AutoConstants;
import com.semisky.autoservice.manager.AutoManager;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by 熊俊 on 2018/1/4.
 */

public class MusicPlayHelper {
    public static final String TAG = MusicPlayHelper.class.getSimpleName();
    private Context mContext;
    private MediaPlayer mMediaPlayer;
    private boolean mIsPrepared = false;// 音乐是否准备完成
    //    private List<MusicInfo> mMusicList = new ArrayList<MusicInfo>();
    private int mPlayMode = 1;
    private MediaParaser mMediaParaser;
    private Handler _handler = new Handler(Looper.getMainLooper());

    /**
     * 当前播放歌曲在音乐信息集合中的位置
     */
    private int mCurPlayingMusicPositionInListIndex = -1;
    private int mNextPlayMusicPositionInListIndex = -1;
    private AudioManager mAudioManager;
    //    private boolean hasAudioFocus = false;
    private MusicInfo mCurMusicInfo;
    private MusicInfo mNextMusicInfo;
    private boolean isUserPause = false;

    // 音频焦点状态
    private int mAudioFocusState = Definition.AudioFocusConstants.NO_INVLID;
    private int mCurrentVoiceCmd = VoiceManager.NO_INVLID;

    private MusicPlayList mMusicPlayList;// 服务播放媒体清单

    public MusicPlayHelper(Context mContext) {
        this.mContext = mContext;
        this.mMusicPlayList = new MusicPlayList();
    }

    // 媒体是否已准备
    public boolean hasPrepared() {
        return this.mIsPrepared;
    }

    // 静音媒体声音
    public void muteVolume() {
        if (null != mMediaPlayer && isHasAudioFocus()) {
            mMediaPlayer.setVolume(0, 0);
        }
    }

    // 恢复媒体声音
    public void unMuteVolume() {
        if (null != mMediaPlayer && isHasAudioFocus()) {
            mMediaPlayer.setVolume(1.0f, 1.0f);
        }
    }

    // 获取音频焦点状态
    public int getmAudioFocusState() {
        return this.mAudioFocusState;
    }

    // 设置当前语音命令标识
    public void setCurrentVoiceCommand(int cmd) {
        this.mCurrentVoiceCmd = cmd;
    }

    // 获取当前语音命令标识
    public int getCurrentVoiceCommand() {
        return this.mCurrentVoiceCmd;
    }

    // 保存断点媒体信息
    public void saveCurrentMediaInfoToLocal() {
        if (null != mMediaPlayer) {
            // 保存当前播放进度
            SharePreferenceUtil.setCurrentPlayingMusicProgress(mContext, mMediaPlayer.getCurrentPosition());
        }
    }

    public void setmMusicList(List<MusicInfo> mMusicList) {
        this.mMusicPlayList.addMusicList(mMusicList);// modify1
    }

    /**
     * 是否有媒体数据
     *
     * @return
     */
    public boolean hasData() {
        return mMusicPlayList.hasData();// modify2
    }

    //当前总进度
    public int getDuration() {
        if (mIsPrepared) {
            return (null != mMediaPlayer ? mMediaPlayer.getDuration() : 0);
        }
        return 0;
    }

    // 当前播播放音乐信息
    public MusicInfo getCurrentPlayMusic() {
        return mMusicPlayList.getCurrentMusicInfo();// modify3
    }

    // 下一曲歌曲名
    public MusicInfo getmNextMusicInfo() {
        return mMusicPlayList.getmNextMusicInfo();// modify4
    }

    // 是否用用户暂停
    public void setUserPause(boolean userPause) {
        isUserPause = userPause;
        LogUtil.i(TAG, "setUserPause()" + userPause);
    }

    public boolean isUserPause() {
        return this.isUserPause;
    }

    public String getPostion() {
       /* int totalQty = (mMusicList.size() > 0 ? mMusicList.size() : 0);
        int curProgramIndex = (mCurPlayingMusicPositionInListIndex > -1 ? mCurPlayingMusicPositionInListIndex + 1 : 0);
        return (curProgramIndex + mContext.getString(R.string.sprit) + totalQty);*/
        return mMusicPlayList.getCurIndexAndTotalSizeWithProgram();// modify5
    }


    public void setmPlayMode(int mPlayMode) {
        LogUtil.i(TAG, "setmPlayMode() mPlayMode=" + mPlayMode);
        if (!hasData()) {
            return;
        }
        this.mPlayMode = mPlayMode;
        mMusicPlayList.setmPlayMode(mPlayMode);// modify6
        mMusicPlayList.refreshNextIndexWithPlayMode();
        notifyNextPrepared();

        SharePreferenceUtil.setPlayMode(MediaApplication.getContext(), mPlayMode);
        MusicPlayerManager.getInstance().onNotifyChangePlayMode(mPlayMode);
//        prepareNext();
    }

    // 切换播放模式
    public void changePlayMode() {
        if (!hasData()) {
            return;
        }
        int playMode = SharePreferenceUtil.getPlayMode(MediaApplication.getContext());
        int nextPlayMode = PlayMode.switchNextMode(playMode);
        this.mPlayMode = nextPlayMode;
        mMusicPlayList.setmPlayMode(mPlayMode);// modify6
        mMusicPlayList.refreshNextIndexWithPlayMode();
        notifyNextPrepared();

        SharePreferenceUtil.setPlayMode(MediaApplication.getContext(), nextPlayMode);
        MusicPlayerManager.getInstance().onNotifyChangePlayMode(nextPlayMode);
//        prepareNext();
    }

    /**
     * USB卸载
     * 注：在MusicPlayerService.java 的OnUsbStateChangeListener.onUsbUnMounted回调调用
     */
    public void setUsbUnMounted() {
        LogUtil.i(TAG, "setUsbUnMounted() hasAudioFocus=" + isHasAudioFocus());
        if (isHasAudioFocus()) {// 是否有音频焦点
            AppUtils.launcherRadioApp(mContext.getPackageName(), AutoManager.FOREGROUND_LAUNCH);
        }
        abandonAudioFocus();// 注销音频焦点事件
        notifyAudioFocusLoss();// 通知观察者音频焦点丢失
        stop();// 停止音乐播放
    }

    public void checkMediaPlayerIsInitial() {
        LogUtil.i(TAG, "checkMediaPlayer() ...");
        if (null == mMediaPlayer) {
            initMediaPlayer();
        } else {
            LogUtil.i(TAG, "checkMediaPlayerIsInitial() Initialized !!!");
        }
    }

    /**
     * 初始化媒体播放器
     */
    private void initMediaPlayer() {
        LogUtil.i(TAG, "initMediaPlayer()");
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(mOnPreparedListener);

        mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
        mMediaPlayer.setOnSeekCompleteListener(mOnSeekCompleteListener);
        mMediaPlayer.setOnErrorListener(mOnErrorListener);
        MusicAudioFocusManager.getInstance().registerObserverOnAudioFocusChangeListener(mOnAudioFocusChangeListener);
    }

    public void initMediaParser() {
        if (null == mMediaParaser) {
            mMediaParaser = new MediaParaser();
        }
    }

    public boolean playOrPause(boolean isManual) {
        if (null != mMediaPlayer) {
            if (mMediaPlayer.isPlaying()) {
                pause();
                setUserPause(true);
                return false;
            } else {
                setUserPause(false);
                return start();

            }
        }
        return false;
    }

    public boolean playLastOrNew() {
        // modify8
        if (isPlaying() || !mMusicPlayList.hasData())
            return false;

        boolean isExistsFinallSaveMusicUrl = false;
        boolean isMatch = false;
        String finallSaveMusicUrl = SharePreferenceUtil.getCurrentPlayingMusicUrl(mContext);
        int finallSaveMusicProgress = SharePreferenceUtil.getCurrentPlayingMusicProgress(mContext);
        String curMountedFirstUsbPath = USBManager.getInstance().getCurrentMountedUsbPath();
        int playMode = SharePreferenceUtil.getPlayMode(mContext);
        this.mPlayMode = playMode;
        mMusicPlayList.setmPlayMode(playMode);
        LogUtil.d(TAG, "resumeToRestorePlay() finallSaveMusicUrl: " + finallSaveMusicUrl);
        LogUtil.d(TAG, "resumeToRestorePlay() finallSaveMusicProgress: " + finallSaveMusicProgress);


        // 播放断点记忆音乐
        if (null != finallSaveMusicUrl && null != curMountedFirstUsbPath) {
            isMatch = finallSaveMusicUrl.startsWith(curMountedFirstUsbPath);
            isExistsFinallSaveMusicUrl = new File(finallSaveMusicUrl).exists();
            LogUtil.d(TAG, "resumeToRestorePlay() isMatch: " + isMatch);
            LogUtil.d(TAG, "resumeToRestorePlay() isExistsFinallSaveMusicUrl: " + isExistsFinallSaveMusicUrl);

            if (isMatch && isExistsFinallSaveMusicUrl) {
                return play(finallSaveMusicUrl, finallSaveMusicProgress, true);
            }
        }
        // 没有断点记忆，默认播放第一个音乐
        return play(mMusicPlayList.getMusicList().get(0).getUrl(), 0, true);//modify9
    }


    /**
     * 播放指定路径与指定进度位置音乐文件
     */
    public boolean play(String musicUrl, int progress, boolean autoStart) {
        if (null == mMediaPlayer) {
            return false;
        }
        mIsPrepared = false;
//        mCurPlayingMusicPositionInListIndex = getMusicUrlInListIndex(musicUrl);
        // modify
        mMusicPlayList.setmPlayingMusicUrl(musicUrl);
        mMusicPlayList.refreshCurrentPlayingIndex();

        LogUtil.d(TAG, "service->play() progress: " + progress + ",autoStart: " + autoStart + ",musicUrl: " + musicUrl
                + ",getMusicUrlInListIndex(): " + mCurPlayingMusicPositionInListIndex);

        checkAudioFocus();
        if (mMediaPlayer == null) {
            return false;
        }

        asyncPrepare(musicUrl, autoStart, progress);

        /*if (!prepare(musicUrl)) {
            return false;
        }
        if (progress > 0 && seekTo(progress) && autoStart) {
            start(true);
            return true;
        } else if (autoStart) {// 起始位置播放
            start(true);
            return true;
        }*/

        return false;
    }

    private int getMusicUrlInListIndex(String url) {
        if (!mMusicPlayList.hasData()) {// modify10
            return -1;
        }
        for (int i = 0; i < mMusicPlayList.getMusicList().size(); i++) {
            MusicInfo music = mMusicPlayList.getMusicList().get(i);
            if (music.getUrl().equals(url)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 开始播放音乐
     */
    public boolean start() {
        LogUtil.d(TAG, "mIsPrepared: " + mIsPrepared);
        isUserPause = false;
        mAudioFocusState = Definition.AudioFocusConstants.RESET;
        mCurrentVoiceCmd = VoiceManager.NO_INVLID;
        if (null != mMediaPlayer && mIsPrepared && !MusicAudioFocusManager.getInstance().isPauseStrategys()) {
            mMediaPlayer.setVolume(1.0f, 1.0f);
            mMediaPlayer.start(); // 音乐开始播放
            checkAudioFocus();
            notifyProgressRunnabelEnable(true);
            notifyMediaPlayerStart();
            MusicPlayerManager.getInstance().onNotifyStart();
            LogUtil.d(TAG, "service->start()");

            SharePreferenceUtil.saveCurrentMusicIsPlaying(MediaApplication.context, true);
            return true;
        }
        MusicPlayerManager.getInstance().onNotifyPause();
        return false;
    }

    private boolean start(boolean hasPrepared) {
        isUserPause = false;
        mAudioFocusState = Definition.AudioFocusConstants.RESET;
        mCurrentVoiceCmd = VoiceManager.NO_INVLID;
        if (null != mMediaPlayer && hasPrepared && !MusicAudioFocusManager.getInstance().isPauseStrategys()) {
            mMediaPlayer.setVolume(1.0f, 1.0f);
            mMediaPlayer.start(); // 音乐开始播放
            checkAudioFocus();
            notifyProgressRunnabelEnable(true);
            notifyMediaPlayerStart();
            MusicPlayerManager.getInstance().onNotifyStart();

            LogUtil.d(TAG, "service->start(param)");
            SharePreferenceUtil.saveCurrentMusicIsPlaying(MediaApplication.context, true);
            return true;
        }
        MusicPlayerManager.getInstance().onNotifyPause();
        return false;
    }

    public void updateCurrentMediaInfo() {
        if (null == mMusicPlayList) {
            return;
        }
        LogUtil.i(TAG, "updateCurrentMediaInfo() ...");
        notifyNextPrepared();
        //当前播放位置
        notifyPosition();
        // 准备媒体资源完成
        notifyMediaPrepare(mMusicPlayList.getCurrentMusicInfo());
    }

    private AsyncPrepareRunnable mAsyncPrepareRunnable = null;

    private void asyncPrepare(String url, boolean isAutoPlay, int progress) {
        if (null == mAsyncPrepareRunnable) {
            mAsyncPrepareRunnable = new AsyncPrepareRunnable(this);
            mAsyncPrepareRunnable.registerOnAsyncMediaPrepareListener(new AsyncPrepareRunnable.OnAsyncMediaPrepareListener() {
                @Override
                public void onAsyncMediaPrepareStateChanged(boolean isValidMediaFile, boolean isAutoPlay, int progress) {
                    LogUtil.i(TAG, "onAsyncMediaPrepareStateChanged() isValidMediaFile=" + isValidMediaFile + " , isAutoPlay=" + isAutoPlay + ",progress=" + progress);
                    if (isValidMediaFile) {
                        if (progress > 0) {
                            seekTo(progress);
                        }
                        if (isAutoPlay) {
                            start(true);
                        }
                    } else {
                        updateCurrentMediaInfo();
                    }
                }
            });
        }// end-----(null == mAsyncPrepareRunnable)

        mAsyncPrepareRunnable.setAutoPlayState(isAutoPlay);
        mAsyncPrepareRunnable.setFileUrl(url);
        mAsyncPrepareRunnable.setmProgress(progress);

        if (mAsyncPrepareRunnable.ismIsRunning()) {
            int pos = mMediaPlayer.getCurrentPosition();
            _handler.removeCallbacks(mTimeoutRunTaskRunnable);
            _handler.postDelayed(mTimeoutRunTaskRunnable, 1000);
            LogUtil.e(TAG, "AsyncPrepareRunnable Running !!! " + pos);
            return;
        }
        new Thread(mAsyncPrepareRunnable).start();
    }

    private Runnable mTimeoutRunTaskRunnable = new Runnable() {
        @Override
        public void run() {
            LogUtil.i(TAG, "TimeoutRunTaskRunnable ....");
            mAsyncPrepareRunnable.resetRunState();
            updateCurrentMediaInfo();
            new Thread(mAsyncPrepareRunnable).start();
        }
    };

    private Runnable mTimeoutRunTaskRunnable2 = new Runnable() {
        @Override
        public void run() {
            LogUtil.i(TAG, "*****************mTimeoutRunTaskRunnable2**************");
            mMediaPlayer.getCurrentPosition();
            updateCurrentMediaInfo();
            LogUtil.i(TAG, "mTimeoutRunTaskRunnable2 end....");
        }
    };


    private static class AsyncPrepareRunnable implements Runnable {
        private WeakReference<MusicPlayHelper> mRfr;
        private String mFileUrl;
        private int mProgress;
        private volatile boolean mIsRunning;
        private boolean mIsAutoPlay = false;

        private OnAsyncMediaPrepareListener mOnAsyncMediaPrepareListener;

        public void setmProgress(int progress) {
            this.mProgress = progress;
        }

        public void setAutoPlayState(boolean isAutoPlay) {
            this.mIsAutoPlay = isAutoPlay;
        }

        public void setFileUrl(String fileUrl) {
            this.mFileUrl = fileUrl;
        }

        public boolean ismIsRunning() {
            return this.mIsRunning;
        }

        public void resetRunState() {
            this.mIsRunning = false;
        }

        AsyncPrepareRunnable(MusicPlayHelper helper) {
            mRfr = new WeakReference<>(helper);
        }

        public interface OnAsyncMediaPrepareListener {
            void onAsyncMediaPrepareStateChanged(boolean isValidMediaFile, boolean isAutoPlay, int progress);
        }

        public void registerOnAsyncMediaPrepareListener(OnAsyncMediaPrepareListener l) {
            mOnAsyncMediaPrepareListener = l;
        }

        @Override
        public void run() {

            if (null == mRfr || null == mRfr.get()) {
                LogUtil.e(TAG, "WeakReference<MusicPlayHelper> mRfr == NULL");
                return;
            }

            if (null == mRfr.get().mMediaPlayer) {
                return;
            }
            this.mIsRunning = true;

            //暂停更新进度
            mRfr.get().notifyProgressRunnabelEnable(false);
            // 保存将要播放的歌曲路径（包括无法播放的歌曲路径）
            SharePreferenceUtil.setCurrentPlayingMusicUrl(mRfr.get().mContext, mFileUrl);
            try {
                // 重置
                mRfr.get().mMediaPlayer.reset();
                // 回调函数播放器重置
                mRfr.get().notifyMediaPlayerReset();
                LogUtil.e(TAG, "reset ...");
                mRfr.get()._handler.removeCallbacks(mRfr.get().mTimeoutRunTaskRunnable2);
                mRfr.get()._handler.postDelayed(mRfr.get().mTimeoutRunTaskRunnable2, 650);
                mRfr.get().mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mRfr.get().mMediaPlayer.setDataSource(mFileUrl);
                mRfr.get().mMediaPlayer.prepare();
                if (null != mOnAsyncMediaPrepareListener) {
                    mOnAsyncMediaPrepareListener.onAsyncMediaPrepareStateChanged(true, mIsAutoPlay, mProgress);
                }
                this.mIsRunning = false;
                mRfr.get()._handler.removeCallbacks(mRfr.get().mTimeoutRunTaskRunnable2);
                LogUtil.e(TAG, "prepare done ...");
            } catch (Exception e) {
                LogUtil.e(TAG, "************************************");
                LogUtil.e(TAG, "**************Async prepare Exception" + e.getMessage());
                LogUtil.e(TAG, "************************************");
                mRfr.get().mIsPrepared = false;
                mRfr.get()._handler.removeCallbacks(mRfr.get().mTimeoutRunTaskRunnable2);
                if (null != mOnAsyncMediaPrepareListener) {
                    mOnAsyncMediaPrepareListener.onAsyncMediaPrepareStateChanged(false, mIsAutoPlay, mProgress);
                }
                this.mIsRunning = false;
                e.printStackTrace();
            }
        }
    }

    /**
     * 准备音乐
     *//*
    private synchronized boolean prepare(String musicUrl) {
        if (null == mMediaPlayer) {
            return false;
        }
        if (null == musicUrl) {
            LogUtil.e(TAG, "prepare musicUrl== null !!!");
            return false;
        }
        LogUtil.i(TAG, "prepare " + musicUrl);
        //暂停更新进度
        notifyProgressRunnabelEnable(false);
        // 保存将要播放的歌曲路径（包括无法播放的歌曲路径）
        SharePreferenceUtil.setCurrentPlayingMusicUrl(mContext, musicUrl);
        try {
            // 重置
            mMediaPlayer.reset();
            // 回调函数播放器重置
            notifyMediaPlayerReset();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(musicUrl);
            mMediaPlayer.prepare();
        } catch (Exception e) {
            LogUtil.e(TAG, "prepare Exception" + e);
            e.printStackTrace();
        }
        return true;
    }*/
    private void parseMusic(String url) {
        try {
            // 媒体开始准备
            mCurMusicInfo = mMediaParaser.parseMusic(url);
        } catch (Exception e) {
            LogUtil.e(TAG, "parseMusic() FAIL !!!");
        }
    }

    private void notifyFastBackForWardLossEffect() {
        for (MusicControlListener listener : musicControlListeners) {
            listener.fastBackForWardLossEffect();
        }
    }

    // 当前播放媒体在列表中的位置及总媒体数量
    private void notifyPosition() {
        try {
            for (MusicControlListener listener : musicControlListeners) {
               /* int size = mMusicPlayList.getMusicList().size();// modify11
                int musicUrlInListIndex = getMusicUrlInListIndex(mCurMusicInfo.getUrl()) + 1;*/
                listener.updatePosition(mMusicPlayList.getCurIndexAndTotalSizeWithProgram());// modify11
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "notifyPosition() ERROR!!!");
            e.printStackTrace();
            return;
        }
    }

    private void notifyNextPrepared() {
        try {
            for (MusicControlListener listener : musicControlListeners) {
                listener.updateNextMusicInfo(mMusicPlayList.getmNextMusicInfo());// modify12
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "notifyNextPrepared() >>>>>>>>>>>FAIL !!! >>>>>>>>>>>>");
            e.printStackTrace();
        }
    }

    private void notifyMediaPrepare(MusicInfo mCurMusicInfo) {
        try {
            for (MusicControlListener listener : musicControlListeners) {
                listener.updateMusicInfo(mCurMusicInfo);
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "notifyMediaPrepare() >>>>>>>>>>>FAIL !!! >>>>>>>>>>>>");
            e.printStackTrace();
        }
    }

    private void notifyMediaProgress() {
        for (MusicControlListener listener : musicControlListeners) {
            listener.updateProgress(mMediaPlayer.getCurrentPosition());
        }
    }

    private void notifyAudioFocusLoss() {
        for (MusicControlListener listener : musicControlListeners) {
            listener.lossAudioFocus();
        }
    }

    private void notifyAudioFocusChange(int focusChange) {
        for (MusicControlListener listener : musicControlListeners) {
            listener.onAudioFocusChange(focusChange);
        }
    }

    private void notifyRequestAudioFocus() {
        for (MusicControlListener listener : musicControlListeners) {
            listener.onRequestAudioFocus();
        }
    }

    private void notifyMediaPlayerReset() {
        try {
            for (MusicControlListener listener : musicControlListeners) {
                listener.onMediaPlayerReset();
            }

        } catch (Exception e) {
            LogUtil.e(TAG, "notifyMediaPlayerReset() FAIL !!!");
        }
    }

    private void notifyMediaPlayerStart() {
        for (MusicControlListener listener : musicControlListeners) {
            listener.onMediaPlayerStart();
        }
    }

    private void notifyMediaPlayerPause() {
        for (MusicControlListener listener : musicControlListeners) {
            listener.onMediaPlayerPause();
        }
    }

    // 通知播放进度线程根据状态是否开启或暂停（state : true表示：启动更新进度线程 false表示：暂停播放进度线程）
    private void notifyProgressRunnabelEnable(boolean state) {
        try {

            for (MusicControlListener listener : musicControlListeners) {
                listener.onNotifyProgressRunnabelEnable(state);
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "notifyProgressRunnabelEnable() FAIL !!!");
        }
    }

    private void notifyMediaPlayCompletion() {
        for (MusicControlListener listener : musicControlListeners) {
            listener.onMediaPlayCompletion();
        }
    }

    /**
     * 下一曲
     */
    public void next() {
        LogUtil.i(TAG, "next() ...");
        if (!hasData()) {
            LogUtil.i(TAG, "NO DATA !!!");
            return;
        }

        pause();

//        mCurPlayingMusicPositionInListIndex = mNextPlayMusicPositionInListIndex;
        String nextUrl = mMusicPlayList.getNextProgramUrl();// modify
        LogUtil.i(TAG, "nextUrl=" + nextUrl);
      /*  if (!prepare(nextUrl)) {
            return false;
        }*/

        asyncPrepare(nextUrl, true, 0);

        LogUtil.d(TAG, "next music mCurPlayingMusicPositionInListIndex = " + mCurPlayingMusicPositionInListIndex);
//        return start(true);
    }

    /**
     * 上一曲
     */
    public void previous() {
        if (mMediaPlayer == null || !hasData()) {
            return;
        }

        pause();
        String prevUrl = mMusicPlayList.getPrevProgramUrl();
       /* if (!prepare(prevUrl)) {
            return false;
        }
        return start(true);*/
        asyncPrepare(prevUrl, true, 0);
    }

    private OnFastForwardListener mOnFastForwardListener = null;

    public void registerOnFastForwardListener(OnFastForwardListener listener) {
        this.mOnFastForwardListener = listener;
    }

    private void notifyEndFastForwardOpreation() {
        if (null != mOnFastForwardListener) {
            mOnFastForwardListener.onEndOperation();
        }
    }

    //快进
    public void fastForward() {
        if (mMediaPlayer != null && mIsPrepared) {
            int position = mMediaPlayer.getCurrentPosition() + 10000;
            if (position > mMediaPlayer.getDuration()) {
                //                notifyFastBackForWardLossEffect();
                notifyEndFastForwardOpreation();
                next();
                return;
            }
            notifyMediaProgress();
            mMediaPlayer.seekTo(position);
        }
    }

    private OnFastBackwardListener mOnFastBackwardListener = null;

    public void registerOnFastBackwardListener(OnFastBackwardListener listener) {
        this.mOnFastBackwardListener = listener;
    }

    private void notifyEndFastBackwardOpreation() {
        if (null != mOnFastBackwardListener) {
            mOnFastBackwardListener.onEndOperation();
        }
    }

    //快退
    public void fastBackWard() {
        if (mMediaPlayer != null && mIsPrepared) {
            int position = mMediaPlayer.getCurrentPosition();
            if (position > 10000) {
                position -= 10000;
            } else {
                //                notifyFastBackForWardLossEffect();
                mMediaPlayer.seekTo(0);
                notifyMediaProgress();
                notifyEndFastBackwardOpreation();
                return;
            }
            notifyMediaProgress();
            mMediaPlayer.seekTo(position);
        }
    }

    /**
     * 暂停播放
     */
    public boolean pause() {
        if (isPlaying() && mIsPrepared) {
            // 暂停更新播放进度
            notifyProgressRunnabelEnable(false);
            mMediaPlayer.pause();
            notifyMediaPlayerPause();
            MusicPlayerManager.getInstance().onNotifyPause();
            LogUtil.i(TAG, "service->pause()");
            SharePreferenceUtil.saveCurrentMusicIsPlaying(MediaApplication.context, false);
            return true;
        }
        return false;
    }

    /**
     * 是否在播放
     *
     * @return
     */
    public boolean isPlaying() {
        return (mMediaPlayer != null && mMediaPlayer.isPlaying());
    }

    /**
     * 停止媒体播放器
     */
    private boolean stop() {
        if (mMediaPlayer != null) {
            notifyProgressRunnabelEnable(false);
            MusicPlayerManager.getInstance().onNotifyStop();

            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
//            mMusicList.clear();// modify
            mMusicPlayList.getMusicList().clear();
            LogUtil.d(TAG, "service->stop()");
            return true;
        }
        return false;
    }

    /**
     * 设置媒体播放器进度
     */
    public boolean seekTo(int progress) {
        if (mMediaPlayer == null) {
            return false;
        }
        if (progress >= 0 && progress <= mMediaPlayer.getDuration()) {
            mMediaPlayer.seekTo(progress);
            return true;
        }
        return false;
    }

    public int getCurrentProgress() {
        if (null != mMediaPlayer) {
            if (mMediaPlayer.getCurrentPosition() <= mMediaPlayer.getDuration()) {
                return mMediaPlayer.getCurrentPosition();
            }
        }
        return 0;
    }


    // 检查音乐焦点
    public void checkAudioFocus() {
        LogUtil.d(TAG, ": checkAudioFocus() hasAudioFocus=" + isHasAudioFocus());
        if (!isHasAudioFocus()) {
            requestAudioFocus();
        }
    }

    // 申请音频焦点
    private void requestAudioFocus() {
        //        hasAudioFocus = true;
        MusicAudioFocusManager.getInstance().onRequestAudioFocus();
       /* if (mAudioManager == null) {
            mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        }
        int result = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);*/
        SharePreferenceUtil.saveLastAppFlag(Definition.APP.FLAG_MUSIC);
        AppUtils.openAndroidStreamVolume();
        //        AppUtils.setCurrentSource();
        setAppStatusForRequestAudioFocus();
        notifyRequestAudioFocus();
        LogUtil.d(TAG, "============requestAudioFocus()...");
    }

    // 释放音频焦点
    private void abandonAudioFocus() {
        //        if (mAudioManager != null) {
        MusicAudioFocusManager.getInstance().onAbandonAudioFocus();
        //            mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
        //            hasAudioFocus = false;
        setAppStatus("", AutoConstants.AppStatus.DESTROY);
        LogUtil.d(TAG, "============abandonAudioFocus()...");
        //        }
    }

    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {

        @Override
        public void onAudioFocusChange(int focusChange) {
            notifyAudioFocusChange(focusChange);
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    LogUtil.d(TAG, "AUDIOFOCUS_LOSS");
                    pause();// 暂停播放
                    abandonAudioFocus();// 注销音频焦点
                    notifyAudioFocusLoss();// 通知观察者音频焦点丢失
                    mAudioFocusState = Definition.AudioFocusConstants.FOREVER_LOSS;
                    mCurrentVoiceCmd = VoiceManager.NO_INVLID;
                    isUserPause = false;
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    LogUtil.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT");
                    mAudioFocusState = Definition.AudioFocusConstants.TRANSIENT_LOSS;
                    pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    LogUtil.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                    //                    mMediaPlayer.setVolume(0.2f, 0.2f);// 调低音量(中间件统一做处理)
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    LogUtil.d(TAG, "AUDIOFOCUS_GAIN");
                    LogUtil.d(TAG, "AUDIOFOCUS_GAIN isUserPause=" + isUserPause
                            + "\n,mAudioFocusState=" + mAudioFocusState
                            + "\n,mCurrentVoiceCmd=" + mCurrentVoiceCmd);
                    AppUtils.openAndroidStreamVolume();

                    // bug: 手动暂停音乐，进入屏保时钟，退出屏保时钟，方控上或下一曲无作用
                    if (mAudioFocusState == Definition.AudioFocusConstants.TRANSIENT_LOSS) {
                        LogUtil.d(TAG, "TRANSIENT_LOSS");
                        mAudioFocusState = Definition.AudioFocusConstants.NO_INVLID;
                    }

                    if (isUserPause) {
                        LogUtil.d(TAG, "USER PAUSE");
                        return;
                    }

                    if (mAudioFocusState == Definition.AudioFocusConstants.FOREVER_LOSS) {
                        LogUtil.d(TAG, "FOREVER_LOSS");
                        return;
                    }

                    if (mCurrentVoiceCmd == VoiceManager.CMD_PAUSE
                            || mCurrentVoiceCmd == VoiceManager.CMD_START
                            || mCurrentVoiceCmd == VoiceManager.CMD_PREV
                            || mCurrentVoiceCmd == VoiceManager.CMD_NEXT
                            || mCurrentVoiceCmd == VoiceManager.CMD_PLAY_APPOINT_ATIST
                            || mCurrentVoiceCmd == VoiceManager.CMD_PLAY_APPOINT_SONG
                            || mCurrentVoiceCmd == VoiceManager.CMD_PLAY_APPOINT_ATIST_AND_SONG
                            || mCurrentVoiceCmd == VoiceManager.CMD_PLAY_APPOINT_ALBUM) {
                        LogUtil.d(TAG, "VOICE WORKING MODE !!!");
                        return;
                    }
                    //                    mMediaPlayer.setVolume(1.0f, 1.0f);// 升音量(中间件统一做处理)
                    start();
                    break;
            }
        }
    };


    private MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            LogUtil.i(TAG, "==================> onPrepared! mIsPrepared=" + mIsPrepared);
            mIsPrepared = true;
            MusicPlayerManager.getInstance().onNotifyPrepare();
            MusicPlayerManager.getInstance().onNotifyReviseTotalTime(mp.getDuration());
            notifyProgressRunnabelEnable(true);

            notifyNextPrepared();
            //当前播放位置
            notifyPosition();
            // 准备媒体资源完成
            notifyMediaPrepare(mMusicPlayList.getCurrentMusicInfo());
        }
    };

    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            LogUtil.i(TAG, "==================> onCompletion! hasAudioFocus=" + isHasAudioFocus());
            LogUtil.i(TAG, "==================> onCompletion! mIsPrepared=" + mIsPrepared);
            if (isHasAudioFocus() && mIsPrepared) {
                notifyMediaPlayCompletion();
            }
        }
    };

    private MediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener = new MediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mp) {

        }
    };

    private MediaPlayer.OnErrorListener mOnErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            MusicPlayerManager.getInstance().onNotifyMediaError();
            return false;
        }
    };

    private List<MusicControlListener> musicControlListeners = new ArrayList<MusicControlListener>();

    public void registMusicControlListener(MusicControlListener musicControlListener) {
        musicControlListeners.add(musicControlListener);
    }

    public void unRegistMusicControlListener(MusicControlListener musicControlListener) {
        musicControlListeners.remove(musicControlListener);
    }

    public interface MusicControlListener {
        void updateProgress(int progress);

        void updateMusicInfo(MusicInfo musicInfo);

        void updateNextMusicInfo(MusicInfo musicInfo);

        void updatePosition(String position);

        void fastBackForWardLossEffect();

        void lossAudioFocus();

        void onAudioFocusChange(int focusChange);

        void onRequestAudioFocus();

        void onMediaPlayerReset();

        void onMediaPlayerStart();

        void onMediaPlayerPause();

        void onNotifyProgressRunnabelEnable(boolean state);

        void onMediaPlayCompletion();

    }

    /**
     * 申请音频焦点时设置App状态
     */
    private void setAppStatusForRequestAudioFocus() {
        boolean isTopActivity = AppUtils.isTopActivity(AutoConstants.PackageName.CLASS_MUSIC);
        LogUtil.i(TAG, "isTopActivity=" + isTopActivity);
        int liveCycle = AutoConstants.AppStatus.RUN_FOREGROUND;
        String stateFlag = "";

        if (isTopActivity) {
            liveCycle = AutoConstants.AppStatus.RUN_FOREGROUND;
            stateFlag = mContext.getString(R.string.music);
        } else {
            liveCycle = AutoConstants.AppStatus.RUN_BACKGROUND;
            stateFlag = "background";
        }
        setAppStatus(stateFlag, liveCycle);
    }

    private void setAppStatus(String info, int liveCycleCode) {
        LogUtil.i(TAG, "setAppStatus() title=" + info + " , state=" + liveCycleCode);
        AutoManager.getInstance().setAppStatus(AutoConstants.PackageName.CLASS_MUSIC, info, liveCycleCode);
    }

    public boolean isHasAudioFocus() {
        return MusicAudioFocusManager.getInstance().isHasAudiofocus();
    }

    // 播放列表歌曲
    private String preparePlayUrl = null;

    public void playList(String url) {
        if (null == mMediaPlayer) {
            LogUtil.e(TAG, "playList() null == mMediaPlayer");
            return;
        }

        this.preparePlayUrl = url;
        boolean hasData = hasData();

        LogUtil.i(TAG, "playList() hasData=" + hasData);
        LogUtil.i(TAG, "playList() isHasAudioFocus=" + isHasAudioFocus());

        // 有媒体数据,有音频焦点
        if (hasData && isHasAudioFocus()) {
            if (isCurrentPlayingProgram(preparePlayUrl)) {
                LogUtil.i(TAG, "CURRENT PLAYING PROGRAM , ONLY UPDATE INFO !!!");
                MusicPlayerManager.getInstance().controlRequestMediaData();
                return;
            }
            play(url, 0, true);
        } else {
            MusicDataModule.getInstance().loadAllSongs(new MusicDataModule.OnLoadDataListener() {
                @Override
                public void onAllFolderListResult(List<String> mFolderList) {
                }

                @Override
                public void onFolderUnderFileListResult(List<MusicInfo> musicList) {
                }

                @Override
                public void onAllMusicListResult(List<MusicInfo> musicList) {
                    int progress = 0;

                    LogUtil.i(TAG, "playList()->onAllMusicListResult() =" + (musicList != null ? musicList.size() : 0));
                    if (null != musicList && musicList.size() > 0) {
                        if (!hasData()) {
                            setmMusicList(musicList);
                        }

                        if (isCurrentPlayingProgram(preparePlayUrl)) {
                            progress = SharePreferenceUtil.getCurrentPlayingMusicProgress(MediaApplication.getContext());
                        }
                        play(preparePlayUrl, progress, true);
                    }
                }
            });
        }
    }

    private boolean isCurrentPlayingProgram(String curPlayingProgreamUri) {
        String lastUri = SharePreferenceUtil.getCurrentPlayingMusicUrl(MediaApplication.getContext());
        LogUtil.i(TAG, "isCurrentPlayingProgram() curPlayingProgreamUri=" + curPlayingProgreamUri);
        LogUtil.i(TAG, "isCurrentPlayingProgram() lastUri=" + lastUri);

        if (!TextUtils.isEmpty(curPlayingProgreamUri) && !TextUtils.isEmpty(lastUri)) {
            if (curPlayingProgreamUri.equals(lastUri)) {
                LogUtil.i(TAG, "CURRENT PLAYING PROGRAM !!!!");
                return true;
            }
        }
        LogUtil.i(TAG, "PLAY PROGRAM CHANGE !!!!");
        return false;
    }

    /**
     * 播放某一个位置的音乐
     */
    public boolean startIndex(int index) {
        if (!hasData()) {
            return false;
        }
        if (0 <= index && index <= mMusicPlayList.getMusicList().size() - 1) {
            mCurMusicInfo = mMusicPlayList.getMusicList().get(index);
            mCurPlayingMusicPositionInListIndex = index;
        } else {
            Toast.makeText(mContext, "下标越界", Toast.LENGTH_LONG).show();
            return false;
        }
        pause();

       /* if (!prepare(mCurMusicInfo.getUrl())) {
            return false;
        }

        LogUtil.d(TAG, "index music mCurPlayingMusicPositionInListIndex = " + mCurPlayingMusicPositionInListIndex);
        return start(true);*/
        asyncPrepare(mCurMusicInfo.getUrl(), true, 0);
        return true;
    }


}
