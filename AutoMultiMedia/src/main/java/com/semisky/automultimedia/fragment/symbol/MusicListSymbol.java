package com.semisky.automultimedia.fragment.symbol;

import android.util.Log;

import com.semisky.automultimedia.activity.symbol.BaseSymbol;
import com.semisky.automultimedia.common.bean.MusicInfo;
import com.semisky.automultimedia.common.utils.LogUtil;
import com.semisky.automultimedia.fragment.MusicListFragment;

import java.io.Serializable;

import static com.semisky.automultimedia.common.constant.Definition.MusicMessge.CURRENT_PLAY_MUSIC;
import static com.semisky.automultimedia.common.constant.Definition.MusicMessge.GET_CURRENT_PLAY_MUSIC;

/**
 * Created by 熊俊 on 2018/1/5.
 */

public class MusicListSymbol extends BaseSymbol {
    private MusicListFragment fragment;

    public MusicListSymbol(MusicListFragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public void createSymbol() {

    }

    @Override
    public void destroySymbol() {

    }

    @Override
    public void onMsgRegisterSuccess() {
        sendCommand(GET_CURRENT_PLAY_MUSIC,null);
    }

    @Override
    public void onMsgUnregisterSuccess() {

    }

    @Override
    protected void upDataProperty(int apiID, Serializable data) {
        if (apiID == CURRENT_PLAY_MUSIC){
            if (data!=null){
                LogUtil.i("data = "+data.toString());
                fragment.updatePlaying(((MusicInfo) data).getDisplayName());
            }

        }
    }


}
