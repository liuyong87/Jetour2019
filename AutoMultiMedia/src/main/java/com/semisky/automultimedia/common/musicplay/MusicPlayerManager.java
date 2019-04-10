package com.semisky.automultimedia.common.musicplay;

import android.content.Intent;

import com.semisky.automultimedia.MediaApplication;
import com.semisky.automultimedia.common.constant.Definition;

/**
 * 音乐播放器管理类
 * 作用：处理音乐服务与播放器界面的通信问题
 * Created by liuyong on 18-3-27.
 */

public class MusicPlayerManager extends BaseMusicPlayerManager {
    private static final String TAG = MusicPlayerManager.class.getSimpleName();
    private static MusicPlayerManager INSTANCE;

    private boolean mIsListPlay = false;// 列表播放标识
    private boolean mIsStopUSBAutoPlay = false;// 是否禁止U盘媒体自动播放标识

    private MusicPlayerManager() {
    }

    public static MusicPlayerManager getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new MusicPlayerManager();
        }
        return INSTANCE;
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public boolean notifyControlPlay(String url) {
        boolean isOk = super.notifyControlPlay(url);
        if (!isOk) {
            playListMusic(url);
            return true;
        }
        return true;
    }

    /**
     * 列表音乐播放
     */
    public void playListMusic(String url) {
        Intent i = new Intent();
        i.putExtra("url", url);
        i.setAction(Definition.ACTION_PLAY_LIST);
        i.setClassName(Definition.USB_MULTIMEDIA_PKG, Definition.USB_MUSIC_SERVICE_CLZ);
        MediaApplication.getContext().startService(i);
    }

    public void setListPlayState(boolean state) {
        this.mIsListPlay = state;
    }

    public boolean isListPlay() {
        return this.mIsListPlay;
    }

    public void setmIsStopUSBAutoPlay(boolean state){
        this.mIsStopUSBAutoPlay = state;
    }

    public boolean isStopUSBAutoPlay(){
        return this.mIsStopUSBAutoPlay;
    }

}
