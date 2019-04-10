package com.semisky.automultimedia.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Toast;

import com.semisky.automultimedia.MediaApplication;
import com.semisky.automultimedia.R;
import com.semisky.automultimedia.activity.symbol.VideoSymbol;
import com.semisky.automultimedia.common.bean.VideoInfo;
import com.semisky.automultimedia.common.broadRecevier.FactoryTestReceiver;
import com.semisky.automultimedia.common.broadRecevier.VideoFactoryTestManager;
import com.semisky.automultimedia.common.constant.Definition;
import com.semisky.automultimedia.common.data.VideoDataModule;
import com.semisky.automultimedia.common.mediascan.MediaParaser;
import com.semisky.automultimedia.common.mediascan.MediaParaser.OnParseVideoInfoListener;
import com.semisky.automultimedia.common.sql.SharePreferenceUtil;
import com.semisky.automultimedia.common.strategys.VideoStrategyManager;
import com.semisky.automultimedia.common.utils.AppUtils;
import com.semisky.automultimedia.common.utils.LogUtil;
import com.semisky.automultimedia.common.utils.OnMediaChangeStateListener;
import com.semisky.automultimedia.common.utils.USBManager;
import com.semisky.automultimedia.common.utils.VideoAudioFocusManager;
import com.semisky.automultimedia.databinding.VedioPlayData;
import com.semisky.automultimedia.service.datainfo.VedioDataInfo;
import com.semisky.autoservice.aidl.IKeyListener;
import com.semisky.autoservice.aidl.IVehicleStatusListener;
import com.semisky.autoservice.manager.AutoConstants;
import com.semisky.autoservice.manager.AutoManager;
import com.semisky.autoservice.manager.CarCtrlManager;
import com.semisky.autoservice.manager.KeyManager;
import com.semisky.autoservice.ProtocolConstants.MessageMcuConstants;

import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.semisky.automultimedia.common.constant.Definition.VideoMessge.FAST_BACKWARD;
import static com.semisky.automultimedia.common.constant.Definition.VideoMessge.FAST_FORWARD;
import static com.semisky.automultimedia.common.constant.Definition.VideoMessge.HIDE_PROGRESSBAR;
import static com.semisky.automultimedia.common.constant.Definition.VideoMessge.MSG_NEXT;
import static com.semisky.automultimedia.common.constant.Definition.VideoMessge.MSG_PREV;
import static com.semisky.automultimedia.common.constant.Definition.VideoMessge.SHOW_PROGRESSBAR;
import static com.semisky.automultimedia.common.constant.Definition.VideoMessge.VEDIO_LIST;

/**
 * Created by 熊俊 on 2017/12/27.
 */

public class VideoPlayActivity extends Activity {
    private static final String TAG = "VideoPlayActivity";
    private static boolean DEBUG = false;
    private VideoHandler handler = new VideoHandler(this);
    private List<VideoInfo> videoList = new ArrayList<VideoInfo>();
    private boolean isSurfaceViewDrawComplete = false;
    private VideoSymbol videoSymbol;
    private VedioPlayData vedioDataBind;
    private String mCurrentPlayVideoURL;
    private String intentUrl;
    private OnMediaChangeStateListener mOnMediaChangeStateListener;

    private boolean isEnterList = false;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LogUtil.i(TAG,"onConfigurationChanged() ...");
    }

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.i(TAG,"------------------------VIDEO onCreate");
        VideoAudioFocusManager.getInstance(this).requestAudioFocus();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }

        vedioDataBind = DataBindingUtil.setContentView(this, R.layout.activity_vedio_play);
        videoSymbol = new VideoSymbol();
        vedioDataBind.setVideoSymbol(videoSymbol);
        initListener();
        initPlayStateListener();
        vedioDataBind.videoSurfaceview.getViewTreeObserver().addOnGlobalLayoutListener(new SurfaceViewGlobalLayoutListener());

        VedioDataInfo dataInfo = new VedioDataInfo(this);
//        dataInfo.getVideoList(handler);
        intentUrl = getIntent().getStringExtra("url");
        loadData(false);
        handIntent(super.getIntent());
        //注册广播 home 2018/06/15
        registerReceiver(home,new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        //工厂测试
        VideoFactoryTestManager.getInstance().registerOnFactoryTestCommandListener(onFactoryTestCommandListener);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LogUtil.i(TAG,"------------------------VIDEO onNewIntent");
        VideoAudioFocusManager.getInstance(this).requestAudioFocus();
        handIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppUtils.finishOtherApp();
        registerKeyLinstener();
        SharePreferenceUtil.saveLastAppFlag(Definition.APP.FLAG_VIDEO);
        //如果没有播放，再播放
        if (!vedioDataBind.videoSurfaceview.isPlaying()){
            if (!VideoStrategyManager.getInstance().isUserSelectListPlay()&&vedioDataBind.videoSurfaceview.isDestroy) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(AppUtils.isTopActivity(VideoPlayActivity.class.getName())){
                            recoverVideo();
                            vedioDataBind.videoSurfaceview.isDestroy=false;
                        }
                    }
                },1500);
            }
        }

        VideoStrategyManager.getInstance().setUserSelectListPlayState(false);
        isEnterList = false;
        AutoManager.getInstance().setAppStatus(getClass().getName(), getResources().getString(R.string.vedio), AutoConstants.AppStatus.RUN_FOREGROUND);
        registerVehicleStatusListener();// 注册车辆状态监听
        checkVideoWarningFunction();
        AppUtils.closeUsbConnectDialog();

    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeMessages(FAST_BACKWARD);
        handler.removeMessages(FAST_FORWARD);
        unregisterKeyLinstener();
        unregisterVehicleStatusListener();// 反注册车辆状态监听
        if (!isEnterList) {
            AutoManager.getInstance().setAppStatus(getClass().getName(), "background", AutoConstants.AppStatus.RUN_BACKGROUND);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterUsbStateListener();
        unregisterOnMediaChangeStateListener();
        AutoManager.getInstance().setAppStatus(getClass().getName(), getResources().getString(R.string.vedio), AutoConstants.AppStatus.DESTROY);

        handler.removeCallbacksAndMessages(null);
        handler = null;
        VideoDataModule.getInstance().unregisterOnLoadDataListener(mOnLoadDataListener);
        //add 2018-4-19  插入U盘，清空列表
        VideoDataModule.getInstance().onDestroy();
        //2018/06/15 李超超
        unregisterReceiver(home);
        //工厂测试
        VideoFactoryTestManager.getInstance().unRegisterOnFactoryTestListener();
    }


    private void sendMsgPlayMusic(String url, int progress, boolean isAutoPlay) {
        handler.removeMessages(VideoHandler.MSG_PLAY_MUSIC);
        Message msg = handler.obtainMessage();
        msg.obj = url;
        msg.arg1 = progress;
        msg.arg2 = (isAutoPlay == true ? 1 : 0);
        msg.what = VideoHandler.MSG_PLAY_MUSIC;
        handler.sendMessage(msg);
    }

    private VideoStrategyManager.OnRecoverMemoryPlaybackCallback mOnRecoverMemoryPlaybackCallback = null;

    // 注册恢复记忆播放监听
    private void registerOnRecoverMemoryPlaybackCallback() {
        if (null == mOnRecoverMemoryPlaybackCallback) {
            mOnRecoverMemoryPlaybackCallback = new VideoStrategyManager.OnRecoverMemoryPlaybackCallback() {
                @Override
                public void onPlayItemResource(String url, int progress, boolean isAuotPlay) {
                    if(progress == 0){
                        VideoStrategyManager.getInstance().setUserPauseState("onPlayItemResource()",false);
                    }
                    sendMsgPlayMusic(url, progress, isAuotPlay);
                }

                @Override
                public void onSuccessResult(String url, int progress, boolean isAuotPlay) {
                    LogUtil.i(TAG, "recoverVideo() Has Breakpoint !!!");
                    LogUtil.i(TAG, "recoverVideo() progress=" + progress + " , isAuotPlay=" + isAuotPlay + " , url=" + url);
                    // 只加载数据但不允许播放
                    loadData(false);

                    // 发送恢复断点记忆播放消息
                    sendMsgPlayMusic(url, progress, isAuotPlay);

                }

                @Override
                public void onFailResult() {
                    LogUtil.i(TAG, "recoverVideo() No Breakpoint !!!");
                    // 检查如果有数据，播放默认首个视频
                    loadData(true);
                }
            };
            // 注册恢复断点监听
            VideoStrategyManager.getInstance().registerOnRecoverMemoryPlaybackCallback(mOnRecoverMemoryPlaybackCallback);
        }
    }

    // 恢复断点播放
    private void recoverVideo() {
        VideoStrategyManager.getInstance().startRecoveryMemoryPlaybackThread();
    }


    // 注册数据加载监听
    private VideoDataModule.OnLoadDataListener mOnLoadDataListener = null;
    private void registerOnLoadDataListener() {
        if (null == mOnLoadDataListener) {
            mOnLoadDataListener = new VideoDataModule.OnLoadDataListener() {

                @Override
                public void onAllVideoListResult(int to, List<VideoInfo> videoList, boolean isAllowPlay) {
                    VideoPlayActivity.this.videoList.addAll(videoList);
                    LogUtil.i(TAG, "onAllVideoListResult() to=" + to
                            + " , videoList=" + (null != videoList ? videoList.size() : 0)
                            + " ,isAllowPlay=" + isAllowPlay);

                    if (isAllowPlay) {// 加载完成数据并且允许播放视频
                        if (VideoDataModule.FLAG_PLAYER == to) {
                            if (null != videoList && videoList.size() > 0) {
                                // 播放默认首个视频
                                sendMsgPlayMusic(videoList.get(0).getVideoUrl(), 0, true);
                            }
                        }
                    }
                }
            };
            // 注册视频数据加载监听
            VideoDataModule.getInstance().registerOnLoadDataListener(mOnLoadDataListener);
        }
    }

    // 加载数据
    private void loadData(boolean isAllowPlay) {
        // 加载视频数据
        VideoDataModule.getInstance().loadAllVideoToPlayer(VideoDataModule.FLAG_PLAYER, isAllowPlay);
    }

    private void handIntent(Intent intent) {
        String url = intent.getStringExtra("url");
        if (null != url) {
            VideoStrategyManager.getInstance().playListItemResource(url);
        }
//        play(url, 0, true);
    }

    private void initListener() {
        registerOnLoadDataListener();// 数据加载监听
        registerOnRecoverMemoryPlaybackCallback();// 注册恢复记忆播放监听
        registerUsbStateListener();// 注册USB挂载状态监听
        registerOnParseVideoInfoListener();// 注册解析视频信息监听
        registerOnMediaChangeStateListener();// 注册视频媒体播放状态监听
        vedioDataBind.setListener(new VideoControlListener());
        vedioDataBind.sbVideo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private int mCurrentProgress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mCurrentProgress = progress;
                    videoSymbol.setVideoProgress(progress);

                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                removeMessageUpdateProgress();
                removeMsgHideProgressBar();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                startVideo();
                seekToProgress(mCurrentProgress, true);
                sendMsgHideProgressBar(5000);
            }
        });

        vedioDataBind.imgNextVideo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                handler.sendEmptyMessage(FAST_FORWARD);
                removeMsgHideProgressBar();
                //add 2018-4-17
                setMuteVolume();
                return true;
            }
        });

        vedioDataBind.imgPreVideo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                handler.sendEmptyMessage(FAST_BACKWARD);
                removeMsgHideProgressBar();
                //add 2018-4-17
                setMuteVolume();
                return true;
            }
        });

        vedioDataBind.imgNextVideo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    isStop_Playing();
                    cleanFastOrBackForWard();
                    handler.removeMessages(FAST_FORWARD);
                    sendMsgHideProgressBar(5000);
                }

                return false;
            }
        });

        vedioDataBind.imgPreVideo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    isStop_Playing();
                    cleanFastOrBackForWard();
                    handler.removeMessages(FAST_BACKWARD);
                    sendMsgHideProgressBar(5000);
                }
                return false;
            }
        });

    }


    private IKeyListener mIKeyListener = null;

    private void registerKeyLinstener() {
        LogUtil.i(TAG,"registerKeyLinstener()...");
        KeyManager.getInstance().setOnKeyListener(mIKeyListener = new IKeyListener.Stub() {
            @Override
            public void onKey(int keyCode, int action) throws RemoteException {
                LogUtil.d(TAG, "IKeyListener->keyCode=" + keyCode + " , action=" + action);
                if (KeyManager.ACTION_LONG_PRESS == action) {// 1.长按按下事件
                    switch (keyCode) {
                        case KeyManager.KEYCODE_CHANNEL_UP:// 下一个
                            LogUtil.d(TAG, "======>LONG PRESS NEXT");
                            removeMessageUpdateProgress();
                            handler.sendEmptyMessage(FAST_FORWARD);
                            sendMsgHideProgressBar(5000);
                            //add 2018-4-17
                            setMuteVolume();
                            break;
                        case KeyManager.KEYCODE_CHANNEL_DOWN:// 上一个
                            LogUtil.d(TAG, "======>LONG PRESS PREV");
                            removeMessageUpdateProgress();
                            handler.sendEmptyMessage(FAST_BACKWARD);
                            sendMsgHideProgressBar(5000);
                            //add 2018-4-17
                            setMuteVolume();
                            break;
                    }

                } else if (KeyManager.ACTION_RELEASE == action) {// 2.长按释放事件
                    switch (keyCode) {
                        case KeyManager.KEYCODE_CHANNEL_UP:// 下一个
                            LogUtil.d(TAG, "======>RELEASE LONG PRESS NEXT");
                            isStop_Playing();
                            cleanFastOrBackForWard();
                            handler.removeMessages(FAST_FORWARD);
                            break;
                        case KeyManager.KEYCODE_CHANNEL_DOWN:// 上一个
                            LogUtil.d(TAG, "======>RELEASE LONG PRESS PREV");
                            isStop_Playing();
                            cleanFastOrBackForWard();
                            handler.removeMessages(FAST_BACKWARD);
                            break;
                    }

                } else if (KeyManager.ACTION_PRESS == action) {// 3.单击事件
                    switch (keyCode) {
                        case KeyManager.KEYCODE_CHANNEL_UP:// 下一个
                            LogUtil.d(TAG, "======>SINGLE CLICK NEXT");
                            sendMsgPlayNextVideo();
                            break;
                        case KeyManager.KEYCODE_CHANNEL_DOWN:// 上一个
                            LogUtil.d(TAG, "======>SINGLE CLICK PREV");
                            sendMsgPlayPreVideo();
                            break;
                    }

                }

            }
        });
    }

    private void unregisterKeyLinstener() {
        LogUtil.i(TAG,"unregisterKeyLinstener()...");
        if (null != mIKeyListener) {
            KeyManager.getInstance().unregisterOnKeyListener(mIKeyListener);
        }
    }


    private void initPlayStateListener() {
        vedioDataBind.videoSurfaceview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playNextVideo();

            }
        });
        vedioDataBind.videoSurfaceview.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                videoSymbol.setBlackCanvasVisiable(true);
                videoSymbol.setShowVideoDamageView(true);
                videoSymbol.setVideoPlaying(false);
                return false;
            }
        });
    }

   /* *//**
     * 恢复播放视频
     *//*
    private void restorePlayVideo() {

        if (intentUrl != null) {
            play(intentUrl, 0, true);
            intentUrl = null;
        }

        String finallSaveVideoUrl = SharePreferenceUtil.getCurrentPlayingVideoUrl(this);
        int finallSaveVideoProgress = SharePreferenceUtil.getCurrentPlayVideoProgress(this);
        // boolean isFinallPlaying = isVideoFinallyPlaying();
        boolean isExistsFinallSaveVideoUrl = false;
        String curMountedFirstUsbPath = USBManager.getInstance().getCurrentMountedUsbPath();
        boolean isMatch = false;

        LogUtil.d(TAG, "restorePlayVideo() finallSaveVideoUrl: \n" + finallSaveVideoUrl);
        LogUtil.d(TAG, "restorePlayVideo() finallSaveVideoProgress: " + finallSaveVideoProgress);
        LogUtil.d(TAG, "restorePlayVideo() curMountedFirstUsbPath: " + curMountedFirstUsbPath);

        if (null != finallSaveVideoUrl && null != curMountedFirstUsbPath) {
            isMatch = finallSaveVideoUrl.startsWith(curMountedFirstUsbPath);
            isExistsFinallSaveVideoUrl = new File(finallSaveVideoUrl).exists();
            LogUtil.d(TAG, "restorePlayVideo() isExistsFinallSaveVideoUrl: " + isExistsFinallSaveVideoUrl);
            LogUtil.d(TAG, "restorePlayVideo() isMatch: " + isMatch);

            if (isExistsFinallSaveVideoUrl && isMatch) {
                play(finallSaveVideoUrl, finallSaveVideoProgress, true);
                return;
            }
        }

        // 没有断点记忆，默认播放第一个视频
        if (videoList.size() > 0) {
            String firstVideoUrl = videoList.get(0).getVideoUrl();
            LogUtil.d(TAG, "restorePlayVideo()->firstVideoUrl: " + firstVideoUrl);
            play(firstVideoUrl, 0, true);
        }

    }
*/

    /**
     * 检查视频播放
     */
    private boolean play(String videoUrl, int progress, boolean autoStart) {
        LogUtil.d(TAG, "play() videoUrl: \n" + videoUrl + ", progress: " + progress
                + ", autoStart: " + autoStart);
        if (!prepare(videoUrl)) {
            return false;
        }

        if (progress > 0) {
            if (seekToProgress(progress, false)) {// 定点播放
                if (autoStart) {
                    startVideo();
                    // return startVideo();
                    return true;
                }
            } else {
                // 定点播放fail
            }
        } else {// 起始位置播放
            if (autoStart) {
                startVideo();
                // return startVideo();
                return true;
            }
        }

        return false;
    }

    /**
     * 准备视频资源
     */
    boolean prepare(String videoUrl) {

        if (TextUtils.isEmpty(videoUrl)) {
            return false;
        }
        // 先停止刷新播放进度
        removeMessageUpdateProgress();
//        pauseUpdateProgress();
        // 保存将要播放的视频路径（包括无法播放的视频路径）
        SharePreferenceUtil.setCurrentPlayingVideoUrl(MediaApplication.context, videoUrl);
        SharePreferenceUtil.saveVideoFileLastModified(AppUtils.getFileLastModifiedTime(videoUrl));
        // 重置开关状态
        videoSymbol.setVideoPlaying(false);
        // 显示黑色幕布
        videoSymbol.setBlackCanvasVisiable(true);
        // 设置播放源
        vedioDataBind.videoSurfaceview.setVideoPath(videoUrl);
        vedioDataBind.videoSurfaceview.requestFocus();

        // 刷新列表选中状态
        mCurrentPlayVideoURL = videoUrl;
        // 解析视频文件
        VideoInfo videoInfo = MediaParaser.getInstance().parserMetadataByVideoUrl(videoUrl);
        if (videoInfo != null) {
            videoSymbol.setVideoDuration(videoInfo.getVideoDuration());
            AppUtils.setCurrentSourceNameToICM(videoInfo.getVideoDisplayName());
            LogUtil.d(TAG, "prepare success !!! ");
            return true;
        } else {
            videoSymbol.setShowVideoDamageView(true);
            LogUtil.d(TAG, "prepare fail !!! ");
        }
        return false;
    }

    /**
     * 播放视频
     */
    private boolean startVideo() {


        // 启动播放器
        vedioDataBind.videoSurfaceview.start();
        // 记忆最后是播放状态
        SharePreferenceUtil.setVideoFinallyPlaying(MediaApplication.context, true);
        sendMsgHideProgressBar(5000);
       /* if (vedioDataBind.videoSurfaceview.isPrepared()) {
            // 开始刷新播放进度
            startUpdatePlayProgressThread();
        }*/
        LogUtil.d(TAG + ": startVideo() success !!!");


        return true;
    }

    /**
     * 播放指定位置（有些视频无法做seekTo操作）
     */
    private boolean seekToProgress(int progress, boolean fromUser) {

        if (progress >= 0) {
            vedioDataBind.videoSurfaceview.seekTo(progress);
            if (!fromUser) {// 如果不是用户拖动的，则需要刷新下播放进度
                // 刷新播放进度
                videoSymbol.setVideoProgress(progress);
            }
            // 保存播放进度
            LogUtil.d(TAG + ": seekToProgress() success !!!");
            return true;
        }
        LogUtil.d(TAG + ": seekToProgress() fail !!!");
        return false;
    }


    //UI点击事件
    public class VideoControlListener {

        public void onContainerClick() {
            if (videoSymbol.getVisiable()) {
                sendMsgHideProgressBar(0);
            } else {
                sendMsgShowProgressBar(0);
                sendMsgHideProgressBar(5000);
            }
        }

        public void onMenuClick() {
            isEnterList = true;
            sendMsgHideProgressBar(5000);
            Intent intent = new Intent(VideoPlayActivity.this, SwitchActivity.class);
            intent.putExtra("type", 1);
            intent.putExtra("playingUrl", mCurrentPlayVideoURL);
            VideoDataModule.getInstance().setmPlayingProgramUrl(mCurrentPlayVideoURL);
            VideoPlayActivity.this.startActivity(intent);
            //回到主菜单的时候，移除消息和回调
            handler.removeCallbacksAndMessages(null);
            overridePendingTransition(0, 0);

        }

        public void onPreClick() {
            playPreVideo();
            sendMsgHideProgressBar(5000);
        }

        public void onPlayClick() {
            boolean playing = vedioDataBind.videoSurfaceview.isPlaying();
            if (playing) {
                VideoStrategyManager.getInstance().setUserPauseState("onPlayClick()",true);
                vedioDataBind.videoSurfaceview.pause();
                videoSymbol.setVideoPlaying(false);
            } else {
                VideoStrategyManager.getInstance().setUserPauseState("onPlayClick()",false);
                vedioDataBind.videoSurfaceview.start();
                videoSymbol.setVideoPlaying(true);
            }
            sendMsgHideProgressBar(5000);
        }

        public void onNextClick() {
            playNextVideo();
            sendMsgHideProgressBar(5000);
        }
    }

    // 视频播放异常延时播放下一个节目
    private void sendMsgVideoErrorDelayPlayNext() {
        sendMsgHideProgressBar(5000);
        handler.removeMessages(MSG_NEXT);
        handler.sendEmptyMessageDelayed(MSG_NEXT, 5000);
    }

    private void sendMsgPlayNextVideo() {
        sendMsgHideProgressBar(5000);
        handler.removeMessages(MSG_NEXT);
        handler.sendEmptyMessageDelayed(MSG_NEXT, 500);
    }

    private void playNextVideo() {
        //如果是异常播放，点击下一曲时移除延迟后的消息
        handler.removeMessages(MSG_NEXT);
        VideoStrategyManager.getInstance().setUserPauseState("playNextVideo()",false);
        VideoInfo nextVideo = getNextUrl(mCurrentPlayVideoURL);
        play(nextVideo.getVideoUrl(), 0, true);
    }

    private void sendMsgPlayPreVideo() {
        sendMsgHideProgressBar(5000);
        ////如果是异常播放，点击上一个时移除延迟后的消息（5秒后自动播放下一个）
        handler.removeMessages(MSG_NEXT);
        handler.removeMessages(MSG_PREV);
        handler.sendEmptyMessageDelayed(MSG_PREV, 500);
    }

    private void playPreVideo() {
        ////如果是异常播放，点击上一个时移除延迟后的消息（5秒后自动播放下一个，以前不能播放上一个）
        handler.removeMessages(MSG_NEXT);
        VideoStrategyManager.getInstance().setUserPauseState("playPreVideo()",false);
        VideoInfo preVideo = getPreUrl(mCurrentPlayVideoURL);
        play(preVideo.getVideoUrl(), 0, true);
    }

    private int getCurrentIndex(String url) {
        for (int i = 0; i < videoList.size(); i++) {
            if (videoList.get(i).getVideoUrl().equals(url))
                return i;
        }
        return -1;
    }

    private VideoInfo getPreUrl(String url) {
        int currentIndex = getCurrentIndex(url);
        int i = reviseIndex(currentIndex - 1);
        return videoList.get(i);
    }

    private VideoInfo getNextUrl(String url) {
        int currentIndex = getCurrentIndex(url);
        int i = reviseIndex(currentIndex + 1);
        return videoList.get(i);
    }

    private int reviseIndex(int index) {
        if (index < 0) {
            index = videoList.size() - 1;
        }
        if (index >= videoList.size()) {
            index = 0;
        }
        return index;
    }

    private void sendMsgShowProgressBar(int delayMillis) {
        handler.removeMessages(SHOW_PROGRESSBAR);
        handler.sendEmptyMessageDelayed(SHOW_PROGRESSBAR, delayMillis);
    }

    private void removeMsgHideProgressBar() {
        handler.removeMessages(HIDE_PROGRESSBAR);
    }

    private void sendMsgHideProgressBar(int delayMillis) {
        handler.removeMessages(HIDE_PROGRESSBAR);
        handler.sendEmptyMessageDelayed(HIDE_PROGRESSBAR, delayMillis);
    }

    private OnParseVideoInfoListener mOnParseVideoInfoListener = null;

    // 注册解析视频信息监听
    private void registerOnParseVideoInfoListener() {
        mOnParseVideoInfoListener = new OnParseVideoInfoListener() {

            @Override
            public void onParseVideoInfoStart() {
                LogUtil.i(TAG, "onParseVideoInfoStart()...");
            }

            @Override
            public void onParseVideoInfoComplete(VideoInfo info) {
                LogUtil.i(TAG, "onParseVideoInfoComplete()...");
            }

            @Override
            public void onParseVideoInfoError(String url) {
                LogUtil.i(TAG, "onParseVideoInfoError()..." + url);
                // 视频播放异常延时播放下一个节目
                sendMsgVideoErrorDelayPlayNext();
            }

            @Override
            public void onParseVideoFileNotExist(String url) {
                LogUtil.i(TAG, "onParseVideoFileNotExist()..." + url);
            }
        };
        MediaParaser.getInstance().registerOnParseVideoInfoListener(mOnParseVideoInfoListener);
    }

    private void registerOnMediaChangeStateListener() {
        mOnMediaChangeStateListener = new OnMediaChangeStateListener() {
            @Override
            public void onMediaReset() {
                removeMessageUpdateProgress();
                videoSymbol.setVideoPlaying(false);
                videoSymbol.setVideoProgress(0);
                videoSymbol.setVideoDuration(0);
                videoSymbol.setBlackCanvasVisiable(true);// 隐藏黑色画布（use:防止切换下一个视频时屏闪问题
            }

            @Override
            public void onMediaPrepare() {
                LogUtil.i(TAG,"=================>onMediaPrepare()");

            }

            @Override
            public void onMediaPrepareFinish() {
                LogUtil.i(TAG, "onMediaPrepareFinish()...");
                videoSymbol.setShowVideoDamageView(false);// 隐藏视频损坏提示画布
                if(! VideoStrategyManager.getInstance().getUserPauseState()){
                    videoSymbol.setVideoPlaying(true);
                }
                if(VideoStrategyManager.getInstance().getUserPauseState()){
                    vedioDataBind.videoSurfaceview.pause();
                }
                AppUtils.openAndroidStreamVolumeByVideo();
            }

            @Override
            public void onMediaStart() {
                LogUtil.i(TAG,"onMediaStart()...");
                sendMessageUpdateProgress();
                videoSymbol.setVideoPlaying(true);
                AppUtils.setCurrentPlayStatus(true);
            }

            @Override
            public void onMediaPrepareFail(int what, int extra) {
                videoSymbol.setVideoPlaying(false);
                sendMsgVideoErrorDelayPlayNext();
                AppUtils.setCurrentPlayStatus(false);
            }

            @Override
            public void onUpdateProgress(int progress) {
                LogUtil.i(TAG, "onUpdateProgress() progress=" + progress);
                removeMessageUpdateProgress();
                SharePreferenceUtil.setCurrentPlayVideoProgress(MediaApplication.getContext(), progress);
            }

            @Override
            public void onMediaPause() {
                LogUtil.i(TAG,"onMediaPause()...");
                videoSymbol.setVideoPlaying(false);
                AppUtils.setCurrentPlayStatus(false);
                removeMessageUpdateProgress();

            }

            @Override
            public void onMediaStop() {
                videoSymbol.setVideoPlaying(false);
            }

            @Override
            public void onMediaPlayCompletion() {

            }

            @Override
            public void onAbandonAudiofocus() {

            }

            @Override
            public void onMediaInfo(int what, int extra) {
                LogUtil.i(TAG, "onMediaInfo() ===>what=" + what + " , extra=" + extra);

                switch (what) {
                    case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                        videoSymbol.setBlackCanvasVisiable(false);// 隐藏黑色画布（use:防止切换下一个视频时屏闪问题）
                        videoSymbol.setShowVideoDamageView(false);// 隐藏视频损坏提示画布
                        if(!VideoStrategyManager.getInstance().getUserPauseState()){
                            videoSymbol.setVideoPlaying(true);// 设置播放开关状态
                            sendMessageUpdateProgress();// 开启更新播放进度
                        }
                        break;
                }
            }
        };
        vedioDataBind.videoSurfaceview.setOnMediaChangeStateListener(mOnMediaChangeStateListener);
    }

    private void unregisterOnMediaChangeStateListener() {
        if (null != mOnMediaChangeStateListener) {
            vedioDataBind.videoSurfaceview.setOnMediaChangeStateListener(null);
        }
    }


    private class SurfaceViewGlobalLayoutListener implements ViewTreeObserver.OnGlobalLayoutListener {
        @Override
        public void onGlobalLayout() {
            isSurfaceViewDrawComplete = true;
        }
    }

    private int seconds_Fast_Back_ForWard = 0;//按键计时
    private int times = 2;

    private static class VideoHandler extends Handler {
        private static final int MSG_PLAY_MUSIC = 123;
        WeakReference<VideoPlayActivity> mReference;

        VideoHandler(VideoPlayActivity activity) {
            mReference = new WeakReference<VideoPlayActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_PLAY_MUSIC:
                    LogUtil.i(TAG, "handleMessage()========>MSG_PLAY_MUSIC");
                    mReference.get().play((String) msg.obj, msg.arg1, (msg.arg2 == 1));
                    break;
               /* case VEDIO_LIST:
                    List<VideoInfo> videoInfoList = (List<VideoInfo>) msg.obj;
                    mReference.get().videoList.clear();
                    mReference.get().videoList.addAll(videoInfoList);
                    LogUtil.i("xiongjun isSurfaceViewDrawComplete = " + mReference.get().isSurfaceViewDrawComplete);
                    if (mReference.get().isSurfaceViewDrawComplete) {
                        mReference.get().restorePlayVideo();
                    } else {
                        //若surfaceView未绘制完成，等待0.5s后再次尝试播放
                        sendEmptyMessageDelayed(12, 500);
                    }
                    break;
                case 12:
                    if (mReference.get().isSurfaceViewDrawComplete) {
                        mReference.get().restorePlayVideo();
                    } else {
                        sendEmptyMessageDelayed(12, 500);
                    }
                    break;*/
                case FAST_FORWARD:
                    int position = mReference.get().vedioDataBind.videoSurfaceview.getCurrentPosition() + 10000 * mReference.get().times;
                    if (position > mReference.get().vedioDataBind.videoSurfaceview.getDuration()) {
                        mReference.get().playNextVideo();
                        return;
                    } else {
                        mReference.get().vedioDataBind.videoSurfaceview.seekTo(position);
                        mReference.get().videoSymbol.setVideoProgress(position);
                    }
                    mReference.get().seconds_Fast_Back_ForWard++;
//                    if (mReference.get().seconds_Fast_Back_ForWard == 2 || mReference.get().seconds_Fast_Back_ForWard == 5 || mReference.get().seconds_Fast_Back_ForWard == 8) {
//                        mReference.get().times = mReference.get().times * 2;
//                    }
                    if (mReference.get().seconds_Fast_Back_ForWard == 3 || mReference.get().seconds_Fast_Back_ForWard == 9 || mReference.get().seconds_Fast_Back_ForWard == 6) {
                        mReference.get().times = mReference.get().times * 2;
                    }
                    removeMessages(FAST_FORWARD);
                    sendEmptyMessageDelayed(FAST_FORWARD, 1000);
                    break;
                case FAST_BACKWARD:
                    int back_position = mReference.get().vedioDataBind.videoSurfaceview.getCurrentPosition() - 10000 * mReference.get().times;
                    if (back_position < 0) {
                        removeMessages(FAST_BACKWARD);
                        mReference.get().isStop_Playing();
                        mReference.get().cleanFastOrBackForWard();
                        back_position = 0;
                    } else {
                        removeMessages(FAST_BACKWARD);
                        sendEmptyMessageDelayed(FAST_BACKWARD, 1000);
                    }
                    mReference.get().vedioDataBind.videoSurfaceview.seekTo(back_position);
                    mReference.get().videoSymbol.setVideoProgress(back_position);

                    mReference.get().seconds_Fast_Back_ForWard++;
//                    if (mReference.get().seconds_Fast_Back_ForWard == 2 || mReference.get().seconds_Fast_Back_ForWard == 5 || mReference.get().seconds_Fast_Back_ForWard == 8) {
//                        mReference.get().times = mReference.get().times * 2;
//                    }
                    if (mReference.get().seconds_Fast_Back_ForWard == 3 || mReference.get().seconds_Fast_Back_ForWard == 9 || mReference.get().seconds_Fast_Back_ForWard == 6) {
                        mReference.get().times = mReference.get().times * 2;
                    }


                    break;
                case HIDE_PROGRESSBAR:
                    mReference.get().videoSymbol.setVisiable(false);
                    mReference.get().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    //VideoPlayActivity.this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
                    break;
                case SHOW_PROGRESSBAR:
                    mReference.get().videoSymbol.setVisiable(true);

                    mReference.get().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    //VideoPlayActivity.this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                    break;
                case MSG_NEXT:
                    mReference.get().playNextVideo();
                    break;
                case MSG_PREV:
                    mReference.get().playPreVideo();
                    break;
            }
        }
    }

    private void sendMessageUpdateProgress() {
        handler.removeCallbacks(mUpdateProgressRunnable);
        handler.postDelayed(mUpdateProgressRunnable, 1000);
    }

    private void removeMessageUpdateProgress() {
        handler.removeCallbacks(mUpdateProgressRunnable);
    }

    private Runnable mUpdateProgressRunnable = new Runnable() {
        @Override
        public void run() {
            sendMessageUpdateProgress();
            int progress = vedioDataBind.videoSurfaceview.getCurrentPosition();
            videoSymbol.setVideoProgress(progress);
            SharePreferenceUtil.setCurrentPlayVideoProgress(MediaApplication.getContext(), progress);
        }
    };

    private void checkVideoWarningFunction() {
        // 1.未打开允许观看视频开关 && speed >= 20km/h ：显示警示弹窗
        // 2.未打开允许观看视频开关 && speed < 15km/h ：不显示警示弹窗
        // 3.已打开允许观看视频开关：不显示警示弹窗
        checkVideoWarningFunction(AppUtils.getWatchVideoState(), AppUtils.getSpeed());
    }

    private void checkVideoWarningFunction(int driver_switch_state, int speed) {
        if(DEBUG){
            LogUtil.i(TAG, "checkVideoWarningFunction() driver_switch_state=" + driver_switch_state + " , speed=" + speed);
        }
        // 1.未打开允许观看视频开关 && speed >= 20km/h ：显示警示弹窗
        // 2.未打开允许观看视频开关 && speed < 15km/h ：不显示警示弹窗
        // 3.已打开允许观看视频开关：不显示警示弹窗
        if (driver_switch_state == 0) {// 禁止观看视频
            if (speed >= 20) { // 显示警示弹窗
                videoSymbol.setShowVideoWarrnigView(true);
            } else if (speed < 15) {// 不显示警示弹窗
                videoSymbol.setShowVideoWarrnigView(false);
            }
        }else {
            videoSymbol.setShowVideoWarrnigView(false);
        }
    }

    private IVehicleStatusListener mIVehicleStatusListener = null;

    // 注册车辆状态信息
    private void registerVehicleStatusListener() {

        if (mIVehicleStatusListener != null) {
            return;
        }

        CarCtrlManager.getInstance().registerVehicleStatusListener(mIVehicleStatusListener = new IVehicleStatusListener.Stub() {
            @Override
            public void onVehicleStatusChange(int var, int var1/*车速标记*/, int var2/*车速*/) throws RemoteException {
                if(DEBUG){
                    LogUtil.d(TAG, "==========>onVehicleStatusChange() var=" + var + ",var1=" + var1 + ",var2=" + var2);
                }
                if (MessageMcuConstants.SubID_CanBus.ICM_SpdWarnCfgSt == var1) {// 车速
                    // 1.未打开允许观看视频开关 && speed >= 20km/h ：显示警示弹窗
                    // 2.未打开允许观看视频开关 && speed < 15km/h ：不显示警示弹窗
                    // 3.已打开允许观看视频开关：不显示警示弹窗
                    checkVideoWarningFunction(AppUtils.getWatchVideoState(), var2);
                }
            }
        });
    }

    // 反注册车辆状态信息
    private void unregisterVehicleStatusListener() {
        if (mIVehicleStatusListener != null) {
            CarCtrlManager.getInstance().unregisterVehicleStatusListener(mIVehicleStatusListener);
            mIVehicleStatusListener = null;
            return;
        }
    }

    USBManager.OnUsbStateChangeListener onUsbStateChangeListener = new USBManager.OnUsbStateChangeListener() {

        @Override
        public void onUsbMounted() {

        }

        @Override
        public void onUsbUnMounted() {

        }

        @Override
        public void onUsbDeviceAttached() {

        }

        @Override
        public void onUsbDeviceDetached() {
            LogUtil.i(TAG, "onUsbDeviceDetached()...");
            vedioDataBind.videoSurfaceview.pause();
        }
    };

    private void registerUsbStateListener() {
        USBManager.getInstance().registerOnUsbStateChangeListener(onUsbStateChangeListener);
    }

    private void unregisterUsbStateListener() {
        USBManager.getInstance().unRegisterOnUsbStateChangeListener(onUsbStateChangeListener);
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(prefix, fd, writer, args);
        writer.println("=========================vido dump start================================");
        if (null != args) {
            writer.println("=========================args result:" + Arrays.toString(args));
        }
        DEBUG = true;
        if (null != args && args.length >= 3) {
            if ("warning".equals(args[0])) {
                int state = Integer.valueOf(args[1]);
                int speed = Integer.valueOf(args[2]);
                checkVideoWarningFunction(state, speed);
            }
        }else if(null != args && args.length >= 2){
            int state = AppUtils.getWatchVideoState();
            int speed = Integer.valueOf(args[1]);
            writer.println("=========================video getWatchVideoState="+state+",speed="+speed);
            checkVideoWarningFunction(state, speed);
        }
        writer.println("=========================vido dump end================================");
    }

    /**
     * 快进或者快退，如果是暂停，恢复播放 2014-4-17
     */
    private void isStop_Playing(){
        boolean playing_ =  vedioDataBind.videoSurfaceview.isPlaying();
        if (!playing_){
            VideoStrategyManager.getInstance().setUserPauseState("onPlayClick()",false);
            vedioDataBind.videoSurfaceview.start();
            videoSymbol.setVideoPlaying(true);
        }
        //如果是静音，取消静音
        vedioDataBind.videoSurfaceview.unMuteVolume();
        if(playing_){
            sendMessageUpdateProgress();
        }

    }

    /**
     * 设置为静音（快进或快退时）
     */
    private void setMuteVolume(){
        vedioDataBind.videoSurfaceview.muteVolume();
    }
    private void cleanFastOrBackForWard(){
        times=2;//快进或快退倍数
        seconds_Fast_Back_ForWard=0;//按键计时归0
    }
    /**
     * Home键 监听广播
     * 2018-06-15 李超超
     */
    BroadcastReceiver home=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)){
                MediaApplication.finishActivity(VideoPlayActivity.class);
            }
        }
    };
    /**
     * 工厂测试
     */
     private VideoFactoryTestManager.OnFactoryTestCommandListener onFactoryTestCommandListener=new VideoFactoryTestManager.OnFactoryTestCommandListener() {
        @Override
        public void onReceiverCommand(int cmd) {
            switch (cmd) {
                case FactoryTestReceiver.STATE_PLAY:
                    factoryPlay();
                    break;
                case FactoryTestReceiver.STATE_STOP:
                    factoryPlay();
                    break;
                case FactoryTestReceiver.STATE_NEXT:
                    playNextVideo();
                    sendMsgHideProgressBar(5000);
                    break;
                case FactoryTestReceiver.STATE_PRE:
                    playPreVideo();
                    sendMsgHideProgressBar(5000);
                    break;
                default:
                    // 大于表示指定列表index的歌曲
                    cmd=cmd-1;
                    if(cmd >= 0){
                        if (0 <= cmd && cmd <= videoList.size() - 1) {
                            VideoInfo videoInfo=videoList.get(cmd);
                            handler.removeMessages(MSG_NEXT);
                            handler.removeMessages(MSG_PREV);
                            VideoStrategyManager.getInstance().setUserPauseState("playPreVideo()",false);
                            VideoInfo preVideo = getPreUrl(videoInfo.getVideoUrl());
                            play(preVideo.getVideoUrl(), 0, true);

                        }else {
                            Toast.makeText(VideoPlayActivity.this,"下标越界",Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
            }

        }
    };
     private void factoryPlay(){
         boolean playing = vedioDataBind.videoSurfaceview.isPlaying();
         if (playing) {
             VideoStrategyManager.getInstance().setUserPauseState("onPlayClick()",true);
             vedioDataBind.videoSurfaceview.pause();
             videoSymbol.setVideoPlaying(false);
         } else {
             VideoStrategyManager.getInstance().setUserPauseState("onPlayClick()",false);
             vedioDataBind.videoSurfaceview.start();
             videoSymbol.setVideoPlaying(true);
         }
         sendMsgHideProgressBar(5000);
     }
}
