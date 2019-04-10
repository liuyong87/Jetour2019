package com.semisky.automultimedia.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import com.semisky.automultimedia.MediaApplication;
import com.semisky.automultimedia.R;
import com.semisky.automultimedia.activity.symbol.MusicPlaySymbol;
import com.semisky.automultimedia.common.bean.MusicInfo;
import com.semisky.automultimedia.common.constant.Definition;
import com.semisky.automultimedia.common.mediascan.MediaParaser;
import com.semisky.automultimedia.common.musicplay.MusicPlayerManager;
import com.semisky.automultimedia.common.sql.SharePreferenceUtil;
import com.semisky.automultimedia.common.utils.AdbUtils;
import com.semisky.automultimedia.common.utils.AppUtils;
import com.semisky.automultimedia.common.utils.DataBindingUtils;
import com.semisky.automultimedia.common.utils.LogUtil;
import com.semisky.automultimedia.databinding.MusicData;
import com.semisky.automultimedia.service.MusicPlayService;
import com.semisky.autoservice.manager.AutoConstants;

import java.io.FileDescriptor;
import java.io.PrintWriter;

/**
 * Created on 2017/12/15.
 * Author: xiongjun
 * About:
 */

public class MusicPlayActivity extends BaseActivity {
    private String TAG = MusicPlayActivity.class.getSimpleName();
    private static boolean DEBUG = false;
    private MusicPlaySymbol musicPlaySymbol;
    MusicData musicData;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 监听语言变化时，如果是文本控件，需要重新设控件显示资源，以保证当前显示字符与当前语言环境相符
        LogUtil.i(TAG,"onConfigurationChanged() ...");
    }

    @Override
    public void createActivity(Bundle savedInstanceState) {
        musicPlaySymbol = new MusicPlaySymbol(this);
        setServiceClass(MusicPlayService.class);
        setmMySymbol(musicPlaySymbol);
        //注册home广播 2018/06/15
        registerReceiver(home, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

    }

    @Override
    public void startDataBinding() {
        musicData = DataBindingUtil.setContentView(this, R.layout.activity_music_play);
        musicData.setMusicPlaySymbol(musicPlaySymbol);
        initListener(musicData);
        debugFunction();


        int playMode = SharePreferenceUtil.getPlayMode(this);
        musicPlaySymbol.setPlayMode(playMode);
        SharePreferenceUtil.saveLastAppFlag(Definition.APP.FLAG_MUSIC);
        MusicPlayerManager.getInstance().registerOnServiceCallback(mOnServiceCallback);
    }

    @Override
    public void startActivity() {

    }

    @Override
    public void restartActivity() {
        //后台播放音乐，再次回到前台，（播放图片会更改记录app flag）
        SharePreferenceUtil.saveLastAppFlag(Definition.APP.FLAG_MUSIC);
    }

    @Override
    public void resumeActivity() {
        AppUtils.finishOtherApp();
        super.putBooleanState(ENTER_LIST_FLAG_KEY, false);
        AppUtils.setAppStatus(getClass().getName(), getResources().getString(R.string.music), AutoConstants.AppStatus.RUN_FOREGROUND);
//        AppUtils.closeUsbConnectDialog();
    }

    @Override
    public void pauseActivity() {
        if (!super.getBooleanState(ENTER_LIST_FLAG_KEY)) {
//            AppUtils.setAppStatus(getClass().getName(), "background", AutoConstants.AppStatus.RUN_BACKGROUND);
        }
    }

    @Override
    public void stopActivity() {

    }

    @Override
    public void destroyActivity() {
        LogUtil.i(TAG, "-------------------------destroyActivity()");
//        AppUtils.setAppStatus(getClass().getName(), getResources().getString(R.string.music), AutoConstants.AppStatus.DESTROY);
        //2018/06/15 李超超
        unregisterReceiver(home);
    }

    @Override
    public void getDataForView() {

    }

    public void fastBackForWardLossEffect() {
    }

    private void initListener(MusicData musicData) {
        musicData.setListener(new MusicControlListener());
        musicData.sbMusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private int mCurrentProgress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mCurrentProgress = progress;
                    // 设置当前拖动进度条的时间
                    musicPlaySymbol.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 暂停时间进度更新
                MusicPlayerManager.getInstance().notifyControlUpdateProgressEnable(false);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 恢复播放并恢复时间进度更新
                MusicPlayerManager.getInstance().controlSeekTo(mCurrentProgress);// 播放指定进度音乐
                MusicPlayerManager.getInstance().notifyControlUpdateProgressEnable(true);
                MusicPlayerManager.getInstance().notifyControlStart();
            }
        });

        musicData.imgPre.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {// 长按快进操作
                putKeyEventName("imgPre", EVENT_LONG_CLICK);
                MusicPlayerManager.getInstance().notifyControlFastBackward();// 快进操作
                return true;
            }
        });
        musicData.imgNext.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {// 长按快退操作
                putKeyEventName("imgNext", EVENT_LONG_CLICK);
                MusicPlayerManager.getInstance().notifyControlFastForward();// 快退操作

                return true;
            }
        });

        musicData.imgPre.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    LogUtil.i(TAG, "========================>imgPre onTouch() ACTION_UP");
                    if (EVENT_LONG_CLICK == getKeyEventValue("imgPre")) {
                        MusicPlayerManager.getInstance().controlCancelFastBackward();// 取消快退操作
                    }
                }
                return false;
            }
        });

        musicData.imgNext.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    LogUtil.i(TAG, "========================>imgNext onTouch() ACTION_UP");
                    if (EVENT_LONG_CLICK == getKeyEventValue("imgNext")) {
                        MusicPlayerManager.getInstance().controlCancelFastForward();// 取消快进操作
                    }
                }
                return false;
            }
        });
    }

    public class MusicControlListener {

        public void onMenuClick() {
            Intent intent = new Intent(MusicPlayActivity.this, SwitchActivity.class);
            intent.putExtra("type", 0);
            MusicPlayActivity.this.startActivity(intent);
            overridePendingTransition(0, 0);
            MusicPlayActivity.this.putBooleanState(ENTER_LIST_FLAG_KEY, true);
        }

        public void onPreClick() {
            LogUtil.i(TAG, "===============onPreClick");
            putKeyEventName("imgPre", EVENT_SHORT_CLICK);
            MusicPlayerManager.getInstance().notifyControlPrev();
        }

        public void onPlayClick() {
            MusicPlayerManager.getInstance().notifyControlPlayOrPause(true);
        }

        public void onNextClick() {
            LogUtil.i(TAG, "===============onNextClick");
            putKeyEventName("imgNext", EVENT_SHORT_CLICK);
            MusicPlayerManager.getInstance().notifyControlNext();
        }

        public void onModeClick() {
            MusicPlayerManager.getInstance().controlChangePlayMode();
        }

    }

    MusicPlayerManager.OnServiceCallback mOnServiceCallback = new MusicPlayerManager.OnServiceCallback() {
        private String LINE = "====================>";

        @Override
        public void onNotifyReset() {
            LogUtil.i(TAG, LINE + "onNotifyReset()...");
            musicPlaySymbol.setProgress(0);
            musicPlaySymbol.setDuration(0);
            musicPlaySymbol.setPlayState(false);
        }

        @Override
        public void onNotifyPrepare() {
            musicPlaySymbol.setPlayState(true);
        }

        @Override
        public void onNotifyPause() {
            //暂停后的状态，如果是前台播放图片，再次启动依然为图片
//            SharePreferenceUtil.saveLastAppFlag(Definition.APP.FLAG_MUSICISStop);
            musicPlaySymbol.setPlayState(false);
        }

        @Override
        public void onNotifyStart() {
            LogUtil.i(TAG, LINE + "onNotifyStart()...");
            musicPlaySymbol.setPlayState(true);
        }

        @Override
        public void onNotifyStop() {
            musicPlaySymbol.setPlayState(false);
        }

        @Override
        public void onNotifyMediaError() {
            musicPlaySymbol.setPlayState(false);
        }

        @Override
        public void onNotifyUpdateProgress(int progress) {
            if(DEBUG){
                LogUtil.i(TAG,"onNotifyUpdateProgress()..."+progress);
            }
           if(progress > 0){
               musicPlaySymbol.setProgress(progress);
           }
        }

        @Override
        public void onNotifyUpdateMediaInfos(MusicInfo musicInfo) {
            if (null == musicInfo) {
                return;
            }
            if (null == musicData) {
                LogUtil.e(TAG, "onNotifyUpdateMediaInfos() ERROR : null == musicData !!!");
                return;
            }
            musicPlaySymbol.setDisplayName(musicInfo.getDisplayName());
            musicPlaySymbol.setArtist(musicInfo.getArtist());
            musicPlaySymbol.setAlbum(musicInfo.getAlbum());
            MediaParaser.getInstance().setAlbumImage(musicData.ivAlbumThumb, musicInfo.getUrl());
            AppUtils.closeUsbConnectDialog();
        }

        @Override
        public void onNotifyPlayStateChange(boolean state) {
            musicPlaySymbol.setPlayState(state);
        }

        @Override
        public void onNotifyNextMediaInfo(MusicInfo musicInfo) {
            if (null == musicInfo) {
                return;
            }
            musicPlaySymbol.setNextName(musicInfo.getDisplayName());
        }

        @Override
        public void onNotifyReviseTotalTime(int time) {
            LogUtil.i(TAG, "onNotifyReviseTotalTime() " + time);
            musicPlaySymbol.setDuration(time);
            musicData.sbMusic.setMax(time);
        }

        @Override
        public void onNotifyUpdateProgramPostion(String posAndTotalQty) {// 更新显示曲目位置及曲目总数
            LogUtil.i(TAG, "onNotifyUpdateProgramPostion() " + posAndTotalQty);
            musicPlaySymbol.setPosition(posAndTotalQty);
        }

        @Override
        public void onNotifyChangePlayMode(int playMode) {
            LogUtil.i(TAG, "onNotifyChangePlayMode() playMode=" + playMode);
            musicPlaySymbol.setPlayMode(playMode);
        }

        @Override
        public void onNotifyAudioFocusChange(int audioFocus) {
            LogUtil.i(TAG, "onNotifyAudioFocusChange() audioFocus=" + audioFocus);
            if (AudioManager.AUDIOFOCUS_LOSS == audioFocus) {
                MediaApplication.finishActivity(MusicPlayActivity.class);
                MusicPlayerManager.getInstance().setListPlayState(false);
            }
        }
    };
    BroadcastReceiver home = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                AppUtils.setAppStatus(getClass().getName(), "background", AutoConstants.AppStatus.RUN_BACKGROUND);
                MediaApplication.finishActivity(MusicPlayActivity.class);
            }
        }
    };

    int mLongClickCount = 0;

    private void debugFunction() {
        musicData.ivIconMusicDebug.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mLongClickCount++;
                if (mLongClickCount > 2) {
                    mLongClickCount = 0;
                    String ip = AdbUtils.getIpAddress(MusicPlayActivity.this);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            LogUtil.i(TAG, "Runnable openADB() ....");
                            AdbUtils.openADB();
                        }
                    }).start();
                    Toast.makeText(MusicPlayActivity.this, "ADB DEBUG !!! ,IP=" + ip, Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter pw, String[] args) {
        super.dump(prefix, fd, pw, args);
        pw.println("============MUSIC DUMP START============================");
        if (args != null && args.length > 0) {
            switch (Integer.valueOf(args[0])) {
                case 0:
                    int seekbarMax = musicData.sbMusic.getMax();
                    LogUtil.i(TAG,"seekbarMax="+seekbarMax);
                    pw.println("seekbarMax[ "+ DataBindingUtils.getVideoFormatTime(seekbarMax)+" ]");
                    break;
                case 1:
                    DEBUG = true;
                    break;
            }
        }
        pw.println("============MUSIC DUMP END============================");
    }
}
