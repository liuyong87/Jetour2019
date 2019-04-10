package com.semisky.automultimedia.service;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;

import com.semisky.automultimedia.activity.MusicPlayActivity;
import com.semisky.automultimedia.activity.SwitchActivity;
import com.semisky.automultimedia.common.bean.MusicInfo;
import com.semisky.automultimedia.common.constant.Definition;
import com.semisky.automultimedia.common.data.MusicDataModule;
import com.semisky.automultimedia.common.interfaces.OnFastBackwardListener;
import com.semisky.automultimedia.common.interfaces.OnFastForwardListener;
import com.semisky.automultimedia.common.interfaces.OnVoiceSearchInfoListener;
import com.semisky.automultimedia.common.musicplay.MusicAudioFocusManager;
import com.semisky.automultimedia.common.musicplay.MusicPlayHelper;
import com.semisky.automultimedia.common.musicplay.MusicPlayerManager;
import com.semisky.automultimedia.common.strategys.VoiceManager;
import com.semisky.automultimedia.common.utils.AppUtils;
import com.semisky.automultimedia.common.utils.LogUtil;
import com.semisky.automultimedia.common.utils.USBManager;
import com.semisky.automultimedia.service.adapter.SemiskyBaseAdapter;
import com.semisky.automultimedia.service.datainfo.MusicDataInfo;
import com.semisky.autoservice.aidl.IKeyListener;
import com.semisky.autoservice.manager.KeyManager;
import com.semisky.voicereceive.ResultCode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created on 2017/12/15.
 * Author: xiongjun
 * About:
 */

public class MusicPlayService extends BaseService {
    private static final java.lang.String TAG = MusicPlayService.class.getSimpleName();
    // 播放音乐ACTION
    private MusicPlayHelper mMusicPlayHelper;
    private List<MusicInfo> mMusicList;

    @Override
    public void createService() {
        initMusicServiceThread();

        mMusicPlayHelper = new MusicPlayHelper(this);
        mMusicPlayHelper.checkMediaPlayerIsInitial();
        mMusicPlayHelper.initMediaParser();

        mMyDataInfo = new MusicDataInfo(this);
        mMyDataInfo.onCreate();
        mMyAdapter = new SemiskyBaseAdapter();
        mMyAdapter.onCreate(this);
        mMyAdapter.onStartCommand();
        //获取数据库音乐列表
        mMusicList = new ArrayList<MusicInfo>();

        USBManager.getInstance().registerOnUsbStateChangeListener(onUsbStateChangeListener);
        mMusicPlayHelper.registMusicControlListener(musicControlListener);
        MusicDataModule.getInstance().registerOnLoadDataListener(mOnLoadDataListener);
        MusicDataModule.getInstance().registerOnVoiceSearchInfoListener(onVoiceSearchInfoListener);
        MusicPlayerManager.getInstance().registerOnClientCallback(mOnClientCallback);

        mMusicPlayHelper.registerOnFastBackwardListener(mOnFastBackwardListener);// 注册快退操作监听
        mMusicPlayHelper.registerOnFastForwardListener(mOnFastForwardListener);

    }


    @Override
    public void startService(Intent intent, int flags, int startId) {
        String action = getAction(intent);
        int cmd = getCmd(intent);
        LogUtil.i(TAG, "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        LogUtil.i(TAG, "music startService() action=" + action);
        LogUtil.i(TAG, "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        if ("com.semisky.ACTION_MUSIC_TEST".equals(action)) {
            switch (cmd) {
                case 1:
                    mMusicPlayHelper.getCurrentProgress();
                    break;
                case 2:
                    mMusicPlayHelper.start();
                    break;
                case 3:
                    mMusicPlayHelper.pause();
                    break;
                case 4:
                    mMusicPlayHelper.getDuration();
                    break;
            }
            return;
        }
        AppUtils.sendBroadcastByMusicServiceStateChange(true);
        mMusicPlayHelper.checkMediaPlayerIsInitial();
        if (Definition.ACTION_PLAY_TOGGLE.equals(action) && Definition.CMD_INVALID == cmd) {
            // bug2635 ，此处为防抖处理（MODE与点击界面ICON进入多媒体音乐冲突，造成多媒体会播放异常，会播放下一曲操作）
            mMusicPlayHelper.checkAudioFocus();// 此处提前申请音频焦点，是为让本地音乐应用纳入音频焦点管理。
            mPlayControlHandler.removeCallbacks(mRestorePlayRunnable);
            mPlayControlHandler.postDelayed(mRestorePlayRunnable, 500);
        } else if (Definition.ACTION_PLAY_START.equals(action)) {
            if (!mMusicPlayHelper.isPlaying()) {
                mMusicPlayHelper.start();
            }
        } else if (Definition.ACTION_PLAY_PAUSE.equals(action)) {
            if (mMusicPlayHelper.isPlaying()) {
                mMusicPlayHelper.pause();
            }
        } else if (Definition.ACTION_PLAY_PREV.equals(action)) {
            executePreviousProgramOperation();
        } else if (Definition.ACTION_PLAY_NEXT.equals(action)) {
            executeNextProgramOperation();
        } else if (Definition.ACTION_PLAY_LIST.equals(action)) {
            String selectPlayProgramUrl = intent.getStringExtra("url");
            LogUtil.i(TAG, "selectPlayProgramUrl="
                    + (null != selectPlayProgramUrl ? selectPlayProgramUrl : null));
            if (!TextUtils.isEmpty(selectPlayProgramUrl)) {
                executePlayUriRunnable(selectPlayProgramUrl, 0, true);
            }
        } else if (Definition.ACTION_PLAY_APPOINT.equals(action)) {
            int index = intent.getIntExtra("index", 0);
            mMusicPlayHelper.startIndex(index);
        }
    }

    // 恢复播放音乐
    private Runnable mRestorePlayRunnable = new Runnable() {
        @Override
        public void run() {
            int voiceCmd = mMusicPlayHelper.getCurrentVoiceCommand();
            boolean isUserPause = mMusicPlayHelper.isUserPause();
            boolean hasAudioFocus = MusicAudioFocusManager.getInstance().isHasAudiofocus();
            boolean isPlaying = mMusicPlayHelper.isPlaying();

            LogUtil.i(TAG, "=========RestorePlayRunnable START=========");
            LogUtil.i(TAG, "voiceCmd=" + voiceCmd);
            LogUtil.i(TAG, "isUserPause=" + isUserPause);
            LogUtil.i(TAG, "hasAudioFocus=" + hasAudioFocus);
            LogUtil.i(TAG, "isPlaying=" + mMusicPlayHelper.isPlaying());
            LogUtil.i(TAG, "=========RestorePlayRunnable END=========");

            if (!(VoiceManager.NO_INVLID == voiceCmd)) {
                LogUtil.i(TAG, "VOICE WORKING !!!! " + mMusicPlayHelper.getCurrentVoiceCommand());
            } else if (isUserPause) {
                LogUtil.i(TAG, "USER PAUSE MODE !!!! " + ",isUserPause=" + mMusicPlayHelper.isUserPause());
            } else if (!hasAudioFocus) {
                LogUtil.i(TAG, "NO AUDIO FOCUS , STOP PLAY !!!! ");
            } else if (!isPlaying) {
                LogUtil.i(TAG, "RESUME PLAY !!!! ");
                MusicDataModule.getInstance().registerOnLoadDataListener(mOnLoadDataListener);
                MusicDataModule.getInstance().loadAllSongs();
            }
        }
    };

    private String getAction(Intent intent) {
        if (null != intent) {
            return intent.getAction();
        }
        return null;
    }

    private int getCmd(Intent intent) {
        int cmd = Definition.CMD_INVALID;
        if (null != intent) {
            cmd = intent.getIntExtra(Definition.PARAM_CMD, Definition.CMD_INVALID);
        }
        LogUtil.i(TAG, "getCmd() ..." + cmd);
        return cmd;
    }

    @Override
    public void destroyService() {
        MusicPlayerManager.getInstance().unregisterOnClientCallback();
        USBManager.getInstance().unRegisterOnUsbStateChangeListener(onUsbStateChangeListener);
        mMusicPlayHelper.unRegistMusicControlListener(musicControlListener);
        MusicDataModule.getInstance().unregisterOnLoadDataListener(mOnLoadDataListener);
        MusicDataModule.getInstance().destroy();
        AppUtils.sendBroadcastByMusicServiceStateChange(false);
    }

    @Override
    public void receiveCommand(int apiID, Serializable data) {
    }


    @Override
    public void receiveData(int apiID, Serializable data) {

    }


    USBManager.OnUsbStateChangeListener onUsbStateChangeListener = new USBManager.OnUsbStateChangeListener() {
        @Override
        public void onUsbMounted() {

        }

        @Override
        public void onUsbUnMounted() {
            mMusicPlayHelper.setUsbUnMounted();
            unregisterKeyLinstener();// 注销按键事件
            VoiceManager.getInstance().unregisterOnVoiceControlListener(mOnVoiceControlLinster);// 反注销语音控件监听
            stopSelf();
        }

        @Override
        public void onUsbDeviceAttached() {

        }

        @Override
        public void onUsbDeviceDetached() {
            mMusicPlayHelper.pause();
        }
    };

    // 用于界面控件监听
    MusicPlayerManager.OnClientCallback mOnClientCallback = new MusicPlayerManager.OnClientCallback() {

        @Override
        public void onControlNext() {
            LogUtil.i(TAG, "service onControlNext()...");
            executeNextProgramOperation();
        }

        @Override
        public void onControlPrev() {
            LogUtil.i(TAG, "service onControlPrev()...");
            executePreviousProgramOperation();
        }

        @Override
        public void onControlFastForward() {// 快进操作
            LogUtil.i(TAG, "service onControlFastForward()...");
            executeFastForwardOperation();
        }

        @Override
        public void onControlFastBackward() {// 快退操作
            LogUtil.i(TAG, "service onControlFastBackward()...");
            executeFastBackwardOperation();
        }

        @Override
        public void onControlCancelFastBackward() {// 取消快退操作
            LogUtil.i(TAG, "service onControlCancelFastBackward()...");
            removeAllPlayControlRunnable();
            removeFastBackwardOperation();
        }

        @Override
        public void onControlCancelFastForward() {// 取消快进操作
            LogUtil.i(TAG, "service onControlCancelFastForward()...");
            removeAllPlayControlRunnable();
            removeFastBackwardOperation();
        }

        @Override
        public void onControlPause() {
            mMusicPlayHelper.pause();
        }

        @Override
        public void onControlStart() {
            mMusicPlayHelper.start();
        }

        @Override
        public void onControlPlay(final String url) {
            executePlayUriRunnable(url, 0, true);
        }

        @Override
        public void onControlPlayOrPause(boolean isManual) {
            mMusicPlayHelper.playOrPause(isManual);
        }

        @Override
        public void onControlUpdateProgressEnable(boolean enable) {
            if (enable) {
                resumeUpdateProgeress();
            } else {
                pauseUpdateProgress();
            }
        }

        @Override
        public void onControlChangePlayMode() {
            LogUtil.i(TAG, "onControlChangePlayMode() ...");
            mMusicPlayHelper.changePlayMode();

        }

        @Override
        public void onControlRequestMediaData() {// 请求媒体数据更新到播放器界面
            LogUtil.i(TAG, "onControlRequestMediaData()...");
            MusicPlayerManager.getInstance().onNotifyUpdateMediaInfos(mMusicPlayHelper.getCurrentPlayMusic());//显示歌名、歌手、专辑、专辑图片
            MusicPlayerManager.getInstance().onNotifyReviseTotalTime(mMusicPlayHelper.getDuration());// 显示总进度
            updateCurrentProgress(mMusicPlayHelper.getCurrentProgress());// 设置当前播放进度
            MusicPlayerManager.getInstance().onNotifyUpdateProgramPostion(mMusicPlayHelper.getPostion());// 显示当前播放歌曲列表位置
            MusicPlayerManager.getInstance().onNotifyNextMediaInfo(mMusicPlayHelper.getmNextMusicInfo());// 显示下一个节目歌名
            MusicPlayerManager.getInstance().onNotifyPlayStateChange(mMusicPlayHelper.isPlaying());// 音乐播放状态变化
        }

        @Override
        public void onControlSeekTo(int progress) {
            LogUtil.i(TAG, "onControlSeekTo()..." + progress);
            executeSeekToRunnable(progress);
        }
    };

    // 语音控件监听
    VoiceManager.OnVoiceControlListener mOnVoiceControlLinster = new VoiceManager.OnVoiceControlListener() {
        @Override
        public void onCommandResult(int cmdCode) {
            LogUtil.i(TAG, "onCommandResult() ===============cmdCode : " + cmdCode);
            switch (cmdCode) {
                case VoiceManager.CMD_NEXT:
                    executeNextProgramOperation();
                    mMusicPlayHelper.setCurrentVoiceCommand(cmdCode);
                    break;
                case VoiceManager.CMD_PREV:
                    executePreviousProgramOperation();
                    mMusicPlayHelper.setCurrentVoiceCommand(cmdCode);
                    break;
                case VoiceManager.CMD_START:
                    mMusicPlayHelper.setCurrentVoiceCommand(cmdCode);
                    mMusicPlayHelper.start();
                    break;
                case VoiceManager.CMD_PAUSE:
                    mMusicPlayHelper.setCurrentVoiceCommand(cmdCode);
                    mMusicPlayHelper.pause();
                    break;
                case VoiceManager.CMD_RESUME_PLAY:
                    if (mMusicPlayHelper.hasData()) {
                        if (!mMusicPlayHelper.isPlaying()) {
                            mMusicPlayHelper.start();
                        }
                        AppUtils.startActivityByFlag(MusicPlayActivity.class);
                    }

                    break;
                case VoiceManager.CMD_OPEN_LIST:
                    LogUtil.i(TAG, "onCommandResult() CMD_OPEN_LIST ...");
                    AppUtils.startActivityByFlag(SwitchActivity.class);
                    break;
            }
        }

        @Override
        public void onCommandResult(int cmdCode, Object[] agrs) {
            LogUtil.i(TAG, "#################################\n"
                    + "onCommandResult() cmdCode=" + cmdCode + "\n"
                    + (null != agrs ? Arrays.asList(agrs) : "NULL")
                    + "\n#################################");
            mMusicPlayHelper.setCurrentVoiceCommand(cmdCode);
            //            AppUtils.startActivityByFlag(MusicPlayActivity.class);
            switch (cmdCode) {
                case VoiceManager.CMD_CHANGE_PLAY_MODE:
                    if (null != agrs && agrs.length > 0) {
                        Integer playMode = ((Integer) agrs[0]);

                        if (1 == playMode) {// 循环模式
                            mMusicPlayHelper.setmPlayMode(Definition.MusicPlayModel.MODE_CIRCLE_ALL);
                        } else if (2 == playMode) {// 单曲模式
                            mMusicPlayHelper.setmPlayMode(Definition.MusicPlayModel.MODE_CIRCLE_SINGL);
                        } else if (3 == playMode) {// 随机模式
                            mMusicPlayHelper.setmPlayMode(Definition.MusicPlayModel.MODE_RANDOM);
                        }
                    }
                    break;
                case VoiceManager.CMD_PLAY_APPOINT_ATIST:
                    LogUtil.i(TAG, "CMD_PLAY_APPOINT_ATIST...");
                    if (agrs.length > 0) {
                        MusicDataModule.getInstance().voiceFuzzyMatchArtistUnderSongs((String) agrs[0]);
                    }
                    break;
                case VoiceManager.CMD_PLAY_APPOINT_SONG:
                    LogUtil.i(TAG, "CMD_PLAY_APPOINT_SONG...");
                    if (agrs.length > 0) {
                        MusicDataModule.getInstance().voiceFuzzyMatchSongs((String) agrs[0]);
                    }
                    break;
                case VoiceManager.CMD_PLAY_APPOINT_ATIST_AND_SONG:
                    LogUtil.i(TAG, "CMD_PLAY_APPOINT_ATIST_AND_SONG...");
                    if (agrs.length > 1) {
                        MusicDataModule.getInstance().voiceFuzzyMatchArtistAndSong((String) agrs[0], (String) agrs[1]);
                    }
                    break;
                case VoiceManager.CMD_PLAY_APPOINT_ALBUM:
                    LogUtil.i(TAG, "CMD_PLAY_APPOINT_ALBUM...");
                    if (agrs.length > 0) {
                        MusicDataModule.getInstance().voiceFuzzyMatchAlbumUnderSongs((String) agrs[0]);
                    }
                    break;
            }
        }
    };

    OnVoiceSearchInfoListener onVoiceSearchInfoListener = new OnVoiceSearchInfoListener() {
        @Override
        public void onSearchResult(Object info) {

            LogUtil.i(TAG, "onSearchResult() info=" + (null != info ? ((MusicInfo) info).getDisplayName() : "NULL"));

            if (null != info) {
                AppUtils.startActivityByFlag(MusicPlayActivity.class, VoiceManager.CMD_SONG_SEARCHED);
                VoiceManager.getInstance().returnResult(ResultCode.RESULT_SUCCESS);
                executePlayUriRunnable(((MusicInfo) info).getUrl(), 0, true);
            } else {
                VoiceManager.getInstance().returnResult(ResultCode.RESULT_FAIL);
                // 修改问题：播放本地音乐，切换到收音机,语音播放一首本地没有的歌曲，
                // 这时跳转到酷我音乐搜索并播放网络音乐，mode切换本地音乐不自动播放。
                // 解决方法：本地未搜索到音乐时，需要将语音Command flag 重置为VoiceManager.NO_INVLID
                mMusicPlayHelper.setCurrentVoiceCommand(VoiceManager.NO_INVLID);
            }
        }
    };

    // 媒体数据加载监听
    MusicDataModule.OnLoadDataListener mOnLoadDataListener = new MusicDataModule.OnLoadDataListener() {

        @Override
        public void onAllFolderListResult(List<String> mFolderList) {

        }

        @Override
        public void onAllMusicListResult(List<MusicInfo> musicList) {
            LogUtil.i(TAG, "onAllMusicListResult()->" + (musicList != null ? musicList.size() : 0));
            if (null != musicList) {
                mMusicPlayHelper.setmMusicList(musicList);

                mMusicPlayHelper.playLastOrNew();
                mMusicPlayHelper.setUserPause(false);
            }
        }

        @Override
        public void onFolderUnderFileListResult(List<MusicInfo> musicList) {

        }
    };

    // 监听媒体帮助控制
    MusicPlayHelper.MusicControlListener musicControlListener = new MusicPlayHelper.MusicControlListener() {

        @Override
        public void onMediaPlayCompletion() {// 曲目播放完成回调函数
            pauseUpdateProgress();
            executeNextProgramOperation();
        }

        @Override
        public void updateProgress(int progress) {
            updateCurrentProgress(progress);

        }

        @Override
        public void updateMusicInfo(MusicInfo musicInfo) {
            LogUtil.d(TAG, "service updateMusicInfo====");
            if (null == musicInfo) {
                LogUtil.e(TAG, "updateMusicInfo() MusicInfo is Empty !!!");
                return;
            }
            executeICMRunnable(musicInfo.getDisplayName());
            MusicPlayerManager.getInstance().onNotifyUpdateMediaInfos(musicInfo);
        }

        @Override
        public void updateNextMusicInfo(MusicInfo musicInfo) {
            LogUtil.d(TAG, "service nextMusicInfo====");
            if (null != musicInfo) {
                MusicPlayerManager.getInstance().onNotifyNextMediaInfo(musicInfo);
            }
        }

        @Override
        public void updatePosition(String position) {
            LogUtil.d("service updatePosition====" + position);
            MusicPlayerManager.getInstance().onNotifyUpdateProgramPostion(position);
        }

        @Override
        public void fastBackForWardLossEffect() {
        }

        @Override
        public void lossAudioFocus() {
            unregisterKeyLinstener();
            MusicPlayerManager.getInstance().onNotifyAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS);
        }

        @Override
        public void onAudioFocusChange(int focusChange) {
            LogUtil.i(TAG, "onAudioFocusChange() focusChange=" + focusChange);
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:// 永久音频焦点丢失
                    removeOnlyFastBackwardOperation();
                    removeOnlyFastForwardOperation();
                    removePreviousProgramOperation();
                    removeNextProgramOperation();
                    mMusicPlayHelper.saveCurrentMediaInfoToLocal();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:// 短暂音频焦点丢失
                    removeOnlyFastBackwardOperation();
                    removeOnlyFastForwardOperation();
                    removePreviousProgramOperation();
                    removeNextProgramOperation();
                    break;
            }
        }

        @Override
        public void onRequestAudioFocus() {
            registerKeyLinstener();
            VoiceManager.getInstance().registerOnVoiceControlListener(mOnVoiceControlLinster);
        }

        @Override
        public void onMediaPlayerReset() {
            MusicPlayerManager.getInstance().onNotifyReset();
        }

        @Override
        public void onMediaPlayerStart() {
            AppUtils.setCurrentPlayStatus(true);
            AppUtils.openAndroidStreamVolume();
        }

        @Override
        public void onMediaPlayerPause() {
            // 失去永久音频焦点不设置播放状态
            if (Definition.AudioFocusConstants.FOREVER_LOSS != mMusicPlayHelper.getmAudioFocusState()) {
                AppUtils.setCurrentPlayStatus(false);
            }
        }

        @Override
        public void onNotifyProgressRunnabelEnable(boolean state) {
            LogUtil.i(TAG, "onNotifyProgressRunnabelEnable() state=" + state);
            if (state) {
                resumeUpdateProgeress();
            } else {
                pauseUpdateProgress();
            }
        }

    };


    private IKeyListener mIKeyListener;

    private void registerKeyLinstener() {
        LogUtil.i(TAG, "registerKeyLinstener()...");
        KeyManager.getInstance().setOnKeyListener(mIKeyListener = new IKeyListener.Stub() {
            @Override
            public void onKey(int keyCode, int action) throws RemoteException {
                LogUtil.d(TAG, "IKeyListener->keyCode=" + keyCode
                        + " , action=" + action
                        + ",audiofocus=" + mMusicPlayHelper.getmAudioFocusState());

                // 短暂音频丢失
                if (mMusicPlayHelper.getmAudioFocusState() == Definition.AudioFocusConstants.TRANSIENT_LOSS) {
                    LogUtil.i(TAG, "onKey() MODE -> AUDIOFOCUS_LOSS_TRANSIENT");
                    return;
                } else if (mMusicPlayHelper.getmAudioFocusState() == Definition.AudioFocusConstants.FOREVER_LOSS) {
                    LogUtil.i(TAG, "onKey() MODE -> AUDIOFOCUS_LOSS");
                    return;
                }


                if (KeyManager.ACTION_PRESS == action) {// 单击事件
                    switch (keyCode) {
                        case KeyManager.KEYCODE_CHANNEL_UP:// 下一个
                            LogUtil.d(TAG, "-------SINGLE CLICK NEXT");
                            executeNextProgramOperation();
                            break;
                        case KeyManager.KEYCODE_CHANNEL_DOWN:// 上一个
                            LogUtil.d(TAG, "-------SINGLE CLICK PREV");
                            executePreviousProgramOperation();
                            break;
                    }
                } else if (KeyManager.ACTION_LONG_PRESS == action) {// 长按事件
                    switch (keyCode) {
                        case KeyManager.KEYCODE_CHANNEL_UP:// 节目快进
                            LogUtil.d(TAG, "-------LONG PRESS -- FAST FORWARD START");
                            executeFastForwardOperation();
                            break;
                        case KeyManager.KEYCODE_CHANNEL_DOWN:// 节目快退
                            LogUtil.d(TAG, "-------LONG PRESS -- FAST BACKWARD START");
                            executeFastBackwardOperation();
                            break;
                    }

                } else if (KeyManager.ACTION_RELEASE == action) {// 释放长按事件
                    switch (keyCode) {
                        case KeyManager.KEYCODE_CHANNEL_UP:// 下一个(结束快进)
                            LogUtil.d(TAG, "-------RELEASE LONG PRESS -- FAST FORWARD END ");
                            removeAllPlayControlRunnable();
                            removeFastForwardOperation();
                            break;
                        case KeyManager.KEYCODE_CHANNEL_DOWN:// 上一个(结束快退)
                            LogUtil.d(TAG, "-------RELEASE LONG PRESS -- FAST BACKWARD END");
                            removeAllPlayControlRunnable();
                            removeFastBackwardOperation();
                            break;
                    }
                }
            }
        });
    }

    /*--------------------------------SEND ICM------------------------------------------*/
    private ICMRunnable mICMRunnable;

    private void executeICMRunnable(String songName) {
        if (null == mICMRunnable) {
            mICMRunnable = new ICMRunnable();
        }
        mICMRunnable.setSongName(songName);
        mPlayControlHandler.removeCallbacks(mICMRunnable);
        mPlayControlHandler.post(mICMRunnable);
    }

    private class ICMRunnable implements Runnable {
        private String mSongName;

        public void setSongName(String name) {
            this.mSongName = name;
        }

        @Override
        public void run() {
            LogUtil.i(TAG, "ICMRunnable run() mSongName=" + mSongName);
            AppUtils.setCurrentSourceNameToICM(this.mSongName);
        }
    }

    /*--------------------------------PLAY MUSIC------------------------------------------*/
    private PlayRunnable mPlayRunnable;

    private void executePlayUriRunnable(String uri, int progress, boolean isAutoPlay) {
        LogUtil.i(TAG, "executePlayUriRunnable() uri=" + uri);
        if (null == mPlayRunnable) {
            mPlayRunnable = new PlayRunnable();
        }
        mPlayRunnable.setPlayUri(uri)
                .setProgress(progress)
                .setAutoPlayState(isAutoPlay);
        mPlayControlHandler.removeCallbacks(mPlayRunnable);
        mPlayControlHandler.postDelayed(mPlayRunnable, 350);
    }

    private class PlayRunnable implements Runnable {
        private String mPlayUri;
        private int mProgress = 0;
        private boolean mIsAutoStart = true;

        public PlayRunnable setPlayUri(String playUri) {
            this.mPlayUri = playUri;
            return this;
        }

        public PlayRunnable setProgress(int progress) {
            this.mProgress = progress;
            return this;
        }

        public PlayRunnable setAutoPlayState(boolean isAutoStart) {
            this.mIsAutoStart = isAutoStart;
            return this;
        }

        @Override
        public void run() {
            //            mMusicPlayHelper.play(mPlayUri,mProgress,mIsAutoStart);
            mMusicPlayHelper.playList(mPlayUri);
        }

    }

    /*--------------------------------SEEKTO PROGRESS MUSIC------------------------------------------*/

    private SeekToRunnable mSeekToRunnable = null;

    private void executeSeekToRunnable(int progress) {
        if (null == mSeekToRunnable) {
            mSeekToRunnable = new SeekToRunnable();
        }
        mPlayControlHandler.removeCallbacks(mSeekToRunnable);
        mSeekToRunnable.setProgress(progress);
        mPlayControlHandler.post(mSeekToRunnable);
    }

    private class SeekToRunnable implements Runnable {
        private int mProgress;

        public void setProgress(int progress) {
            this.mProgress = progress;
        }

        @Override
        public void run() {
            mMusicPlayHelper.seekTo(mProgress);
        }
    }

    protected void removeAllPlayControlRunnable() {
        mPlayControlHandler.removeCallbacks(mNextProgramRunnable);
        mPlayControlHandler.removeCallbacks(mPreviousProgramRunnable);
        mPlayControlHandler.removeCallbacks(mFastBackwardRunnable);
        mPlayControlHandler.removeCallbacks(mFastForwardRunnable);
    }


    /*--------------------------------FAST FORWARD MUSIC---------------------------------------------*/
    // 执行快进操作
    protected void executeFastForwardOperation() {
        removeAllPlayControlRunnable();
        pauseUpdateProgress();
        mMusicPlayHelper.muteVolume();
        mPlayControlHandler.post(mFastForwardRunnable);
    }

    protected void removeFastForwardOperation() {
        if (!mMusicPlayHelper.isPlaying()) {
            mMusicPlayHelper.start();
        } else if (mMusicPlayHelper.hasPrepared()) {
            resumeUpdateProgeress();
        }
        mMusicPlayHelper.unMuteVolume();
        mPlayControlHandler.removeCallbacks(mFastForwardRunnable);
    }

    protected void removeOnlyFastForwardOperation() {
        mPlayControlHandler.removeCallbacks(mFastForwardRunnable);
    }

    Runnable mFastForwardRunnable = new Runnable() {
        @Override
        public void run() {
            mMusicPlayHelper.fastForward();
            if (!isStopForwardOperation) {
                mPlayControlHandler.postDelayed(this, 1000);
            }
            isStopForwardOperation = false;
        }
    };

    private boolean isStopForwardOperation = false;
    protected OnFastForwardListener mOnFastForwardListener = new OnFastForwardListener() {
        @Override
        public void onEndOperation() {
            LogUtil.i(TAG, "OnFastForwardListener onEndOperation() ...");
            mPlayControlHandler.removeCallbacks(mFastForwardRunnable);
            mMusicPlayHelper.unMuteVolume();
            mMusicPlayHelper.start();
            isStopForwardOperation = true;
        }
    };

    /*--------------------------------FAST BACKWARD MUSIC----------------------------------------------*/

    // 执行快退操作
    protected void executeFastBackwardOperation() {
        removeAllPlayControlRunnable();
        pauseUpdateProgress();
        mMusicPlayHelper.muteVolume();
        mPlayControlHandler.post(mFastBackwardRunnable);

    }

    protected void removeFastBackwardOperation() {
        if (!mMusicPlayHelper.isPlaying()) {
            mMusicPlayHelper.start();
        } else if (mMusicPlayHelper.hasPrepared()) {
            resumeUpdateProgeress();
        }
        mMusicPlayHelper.unMuteVolume();
        mPlayControlHandler.removeCallbacks(mFastBackwardRunnable);
    }

    protected void removeOnlyFastBackwardOperation() {
        mPlayControlHandler.removeCallbacks(mFastBackwardRunnable);
    }

    Runnable mFastBackwardRunnable = new Runnable() {
        @Override
        public void run() {
            mMusicPlayHelper.fastBackWard();
            if (!isStopBackwardOperation) {
                mPlayControlHandler.postDelayed(this, 1000);
            }
            isStopBackwardOperation = false;
        }
    };

    private boolean isStopBackwardOperation = false;
    protected OnFastBackwardListener mOnFastBackwardListener = new OnFastBackwardListener() {
        @Override
        public void onEndOperation() {
            LogUtil.i(TAG, "OnFastBackwardListener onEndOperation()...");
            mPlayControlHandler.removeCallbacks(mFastBackwardRunnable);
            mMusicPlayHelper.unMuteVolume();
            mMusicPlayHelper.start();
            isStopBackwardOperation = true;
        }
    };

    /*--------------------------------PREVIOUS MUSIC--------------------------------------------------*/
    protected void removePreviousProgramOperation() {
        LogUtil.i(TAG, "removePreviousProgramOperation() ...");
        mPlayControlHandler.removeCallbacks(mPreviousProgramRunnable);
    }

    // 执行上一个节目操作
    protected void executePreviousProgramOperation() {
        mPlayControlHandler.removeCallbacks(mPreviousProgramRunnable);
        mPlayControlHandler.postDelayed(mPreviousProgramRunnable, 350);
    }

    // 上一个节目异步线程
    Runnable mPreviousProgramRunnable = new Runnable() {
        @Override
        public void run() {
            mMusicPlayHelper.previous();
        }
    };

    /*--------------------------------NEXT MUSIC--------------------------------------------------*/
    private void removeNextProgramOperation() {
        LogUtil.i(TAG, "removeNextProgramOperation() ...");
        mPlayControlHandler.removeCallbacks(mNextProgramRunnable);
    }

    // 执行下一个节目操作
    protected void executeNextProgramOperation() {
        mPlayControlHandler.removeCallbacks(mNextProgramRunnable);
        mPlayControlHandler.postDelayed(mNextProgramRunnable, 350);
    }

    // 下一个节目异步线程
    Runnable mNextProgramRunnable = new Runnable() {
        @Override
        public void run() {
            mMusicPlayHelper.next();
        }
    };
    /*--------------------------------UPDATE PROGRESS--------------------------------------------------*/
    private Handler mProgressHandler = new Handler();

    // 开启线程更新播放进度
    public void resumeUpdateProgeress() {
        LogUtil.i(TAG, "resumeUpdateProgeress()...");
        mProgressHandler.removeCallbacks(progressUpdateRunnable);
        mProgressHandler.post(progressUpdateRunnable);
    }

    // 暂停线程更新播放进度
    public void pauseUpdateProgress() {
        LogUtil.i(TAG, "pauseUpdateProgress()...");
        mProgressHandler.removeCallbacks(progressUpdateRunnable);
    }

    // 更新播放进度异步线程
    private Runnable progressUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            int progress = mMusicPlayHelper.getCurrentProgress();
            updateCurrentProgress(progress);
            mMusicPlayHelper.saveCurrentMediaInfoToLocal();

            mProgressHandler.removeCallbacks(progressUpdateRunnable);
            mProgressHandler.postDelayed(progressUpdateRunnable, 500);
        }
    };

    private void unregisterKeyLinstener() {
        LogUtil.i(TAG, "unregisterKeyLinstener()...");
        if (null != mIKeyListener) {
            KeyManager.getInstance().unregisterOnKeyListener(mIKeyListener);
        }
    }

    private class MusicServiceThread extends HandlerThread implements Handler.Callback {
        MusicServiceThread(String name) {
            super(name);
        }

        @Override
        public boolean handleMessage(Message msg) {
            return false;
        }
    }

    private Handler mPlayControlHandler;
    private MusicServiceThread mMusicServiceThread;

    public void initMusicServiceThread() {
        LogUtil.i(TAG, "initMusicServiceThread() ...");
        if (null == mMusicServiceThread) {
            mMusicServiceThread = new MusicServiceThread("MusicServiceThread");
            mMusicServiceThread.start();
        }
        if (null == mPlayControlHandler) {
            mPlayControlHandler = new Handler(mMusicServiceThread.getLooper(), mMusicServiceThread);
        }
    }


    private void updateCurrentProgress(int progress){
        int curProgress = progress <= mMusicPlayHelper.getDuration() ? progress:0;
        MusicPlayerManager.getInstance().onNotifyUpdateProgress(curProgress);// 设置当前播放进度
    }

}
