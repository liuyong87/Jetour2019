package com.semisky.automultimedia.activity.symbol;

import android.databinding.Bindable;
import android.support.annotation.Nullable;

import com.semisky.automultimedia.BR;
import com.semisky.automultimedia.MediaApplication;
import com.semisky.automultimedia.R;
import com.semisky.automultimedia.activity.MusicPlayActivity;
import com.semisky.automultimedia.common.musicplay.MusicPlayerManager;
import com.semisky.automultimedia.common.utils.EncodingUtil;
import com.semisky.automultimedia.common.utils.LogUtil;

import java.io.Serializable;

import static com.semisky.automultimedia.common.constant.Definition.MusicMessge.FOREVER_AUDIOFOCUS_LOSS;

/**
 * Created on 2017/12/15.
 * Author: xiongjun
 * About:
 */

public class MusicPlaySymbol extends BaseSymbol {
    private String TAG = MusicPlaySymbol.class.getSimpleName();
    private MusicPlayActivity musicPlayActivity;

    public MusicPlaySymbol(MusicPlayActivity musicPlayActivity) {
        this.musicPlayActivity = musicPlayActivity;
    }

    @Override
    public void createSymbol() {

    }

    @Override
    public void destroySymbol() {

    }

    @Override
    public void onMsgRegisterSuccess() {
        // 服务绑定成功，向服务请求数据
        if(!MusicPlayerManager.getInstance().isListPlay()){
            MusicPlayerManager.getInstance().controlRequestMediaData();
        }
        MusicPlayerManager.getInstance().setListPlayState(false);
        MusicPlayerManager.getInstance().setmIsStopUSBAutoPlay(false);
    }

    @Override
    public void onMsgUnregisterSuccess() {

    }

    @Override
    protected void upDataProperty(int apiID, Serializable data) {
        switch (apiID) {
            case FOREVER_AUDIOFOCUS_LOSS:
                LogUtil.d("symbol finish MusicPlayActivity !!!");
                if (musicPlayActivity != null) {
                    MediaApplication.finishActivity(MusicPlayActivity.class);
                }
                break;
        }

    }

    // 歌曲名（不包含后缀）
    @Bindable
    private String displayName;

    // 演唱者
    @Bindable
    private String artist;

    // 专辑
    @Bindable
    private String album;

    // 时长
    @Bindable
    private int duration;

    //播放进度
    @Bindable
    private int progress;

    //当前播放位置
    @Bindable
    private String position;

    //下一曲歌名
    @Bindable
    private String nextName = "";

    @Bindable
    private byte[] alumbPicture;

    @Bindable
    private boolean playState = false;

    @Bindable
    private int playMode;


    public boolean getPlayState() {
        return playState;
    }

    public void setPlayState(boolean playState) {
        this.playState = playState;
        notifyPropertyChanged(BR.playState);
    }

    public int getPlayMode() {
        return playMode;
    }

    public void setPlayMode(int playMode) {
        this.playMode = playMode;
        notifyPropertyChanged(BR.playMode);
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        if (null == displayName || "Unknown".equals(displayName)) {
            this.displayName = musicPlayActivity.getString(R.string.music_unkown_song);
        } else if (EncodingUtil.isChinese(displayName) || EncodingUtil.isLetterAndNumber(displayName)) {
            this.displayName = displayName;
        } else {
            if (EncodingUtil.isNormalText(displayName)){
                this.displayName = displayName;
            }else {
                // this.artist = musicPlayActivity.getString(R.string.music_unkown_artist);
                this.displayName=displayName;

            }

        }
        notifyPropertyChanged(BR.displayName);
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        if (null == artist || "Unknown".equals(artist)) {
            this.artist = musicPlayActivity.getString(R.string.music_unkown_artist);
        } else if (EncodingUtil.isChinese(artist) || EncodingUtil.isLetterAndNumber(artist)) {
            this.artist = artist;
        } else {
            if (EncodingUtil.isNormalText(artist)){
                this.artist = artist;
            }else {
               // this.artist = musicPlayActivity.getString(R.string.music_unkown_artist);
                  this.artist=artist;


            }

        }
        notifyPropertyChanged(BR.artist);
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        if (null == album || "Unknown".equals(album)) {
            this.album = musicPlayActivity.getString(R.string.music_unkown_album);
        } else if (EncodingUtil.isChinese(album) || EncodingUtil.isLetterAndNumber(album)) {
            this.album = album;
        } else {
            if (EncodingUtil.isNormalText(album)){
                this.album=album;
            }else {
               // this.album = musicPlayActivity.getString(R.string.music_unkown_album);
                this.album=album;
            }

        }
        notifyPropertyChanged(BR.album);
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        LogUtil.i(TAG, "=================setDuration" + duration);
        this.duration = duration;
        notifyPropertyChanged(BR.duration);
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        notifyPropertyChanged(BR.progress);
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
        notifyPropertyChanged(BR.position);
    }

    public String getNextName() {
        return nextName;
    }

    public void setNextName(@Nullable String nextName) {
        this.nextName = nextName;
        notifyPropertyChanged(BR.nextName);
    }

    public byte[] getAlumbPicture() {
        return alumbPicture;
    }

    public void setAlumbPicture(byte[] alumbPicture) {
        this.alumbPicture = alumbPicture;
        notifyPropertyChanged(BR.alumbPicture);
    }
}
