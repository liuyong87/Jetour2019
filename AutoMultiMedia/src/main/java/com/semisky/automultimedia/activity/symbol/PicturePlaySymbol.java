package com.semisky.automultimedia.activity.symbol;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.semisky.automultimedia.BR;

/**
 * Created by 熊俊 on 2018/1/18.
 */

public class PicturePlaySymbol extends BaseObservable {
    @Bindable
    private boolean picturePlaying;
    @Bindable
    private boolean showNoticeView = false;
    @Bindable
    private boolean showBottomBar = true;// 播放界面底部控制导航栏默认显示

    public boolean isShowBottomBar() {
        return showBottomBar;
    }

    public void setShowBottomBar(boolean showBottomBar) {
        this.showBottomBar = showBottomBar;
        notifyPropertyChanged(BR.showBottomBar);
    }

    public boolean getPicturePlaying() {
        return picturePlaying;
    }

    public void setPicturePlaying(boolean picturePlaying) {
        this.picturePlaying = picturePlaying;
        notifyPropertyChanged(BR.picturePlaying);
    }

    public boolean isShowNoticeView() {
        return showNoticeView;
    }

    public void setShowNoticeView(boolean showNoticeView) {
        this.showNoticeView = showNoticeView;
        notifyPropertyChanged(BR.showNoticeView);
    }
}
