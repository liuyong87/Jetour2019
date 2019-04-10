package com.semisky.automultimedia.activity.symbol;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.semisky.automultimedia.BR;

/**
 * Created by 熊俊 on 2018/1/12.
 */

public class VideoSymbol extends BaseObservable {
    @Bindable
    private int videoDuration;
    @Bindable
    private int videoProgress;
    @Bindable
    private boolean videoPlaying;
    @Bindable
    private boolean visiable = true;
    @Bindable
    private boolean blackCanvasVisiable = false;
    @Bindable
    private boolean showVideoDamageView = false;
    @Bindable
    private boolean showVideoWarrnigView = false;

    public int getVideoDuration() {
        return videoDuration;
    }

    public void setVideoDuration(int videoDuration) {
        this.videoDuration = videoDuration;
        notifyPropertyChanged(BR.videoDuration);
    }

    public int getVideoProgress() {
        return videoProgress;
    }

    public void setVideoProgress(int videoProgress) {
        this.videoProgress = videoProgress;
        notifyPropertyChanged(BR.videoProgress);
    }

    public boolean getVideoPlaying() {
        return videoPlaying;
    }

    public void setVideoPlaying(boolean videoPlaying) {
        this.videoPlaying = videoPlaying;
        notifyPropertyChanged(BR.videoPlaying);
    }

    public boolean getVisiable() {
        return visiable;
    }

    public void setVisiable(boolean visiable) {
        this.visiable = visiable;
        notifyPropertyChanged(BR.visiable);
    }

    public boolean isBlackCanvasVisiable() {
        return blackCanvasVisiable;
    }

    public void setBlackCanvasVisiable(boolean blackCanvasVisiable) {
        this.blackCanvasVisiable = blackCanvasVisiable;
        notifyPropertyChanged(BR.blackCanvasVisiable);
    }

    public boolean isShowVideoDamageView() {
        return showVideoDamageView;
    }

    public void setShowVideoDamageView(boolean showVideoDamageView) {
        this.showVideoDamageView = showVideoDamageView;
        notifyPropertyChanged(BR.showVideoDamageView);
    }

    public boolean isShowVideoWarrnigView() {
        return showVideoWarrnigView;
    }

    public void setShowVideoWarrnigView(boolean showVideoWarrnigView) {
        this.showVideoWarrnigView = showVideoWarrnigView;
        notifyPropertyChanged(BR.showVideoWarrnigView);
    }
}
