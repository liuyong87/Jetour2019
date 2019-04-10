package com.semisky.automultimedia.fragment.symbol;

import com.semisky.automultimedia.activity.symbol.BaseSymbol;
import com.semisky.automultimedia.fragment.VideoListFragment;

import java.io.Serializable;

/**
 * Created by 熊俊 on 2018/1/8.
 */

public class VedioListSymbol extends BaseSymbol {
    private VideoListFragment videoListFragment;

    public VedioListSymbol(VideoListFragment videoListFragment) {
        this.videoListFragment = videoListFragment;
    }

    @Override
    public void createSymbol() {

    }

    @Override
    public void destroySymbol() {

    }

    @Override
    public void onMsgRegisterSuccess() {

    }

    @Override
    public void onMsgUnregisterSuccess() {

    }

    @Override
    protected void upDataProperty(int apiID, Serializable data) {

    }
}
