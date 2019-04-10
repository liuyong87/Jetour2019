package com.semisky.automultimedia.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.semisky.automultimedia.MultimediaManager;
import com.semisky.automultimedia.activity.symbol.BaseSymbol;
import com.semisky.automultimedia.common.constant.Definition;
import com.semisky.automultimedia.common.musicplay.MusicPlayerManager;
import com.semisky.automultimedia.common.strategys.VoiceManager;
import com.semisky.automultimedia.common.utils.LogUtil;

import java.util.HashMap;

/**
 * Created by chenhongrui on 2017/11/27
 * <p>
 * 内容摘要：
 * 1.初始化绑定服务
 * 2.初始化Symbol,通过生命周期管理
 * 版权所有：Semisky
 * 修改内容：
 * 修改日期
 */
public abstract class BaseActivity extends FragmentActivity {
    private static final String TAG = BaseActivity.class.getSimpleName();
    Class<?> mServiceClass = null;

    public BaseSymbol mMySymbol = null;

    public abstract void createActivity(Bundle savedInstanceState);

    public abstract void startDataBinding();

    public abstract void startActivity();

    public abstract void restartActivity();

    public abstract void resumeActivity();

    public abstract void pauseActivity();

    public abstract void stopActivity();

    public abstract void destroyActivity();

    public abstract void getDataForView();

    protected void setServiceClass(Class<?> serviceClass) {
        mServiceClass = serviceClass;
    }

    public void setmMySymbol(BaseSymbol mMySymbol) {
        this.mMySymbol = mMySymbol;
    }



    // 进入音乐列表成员标记
    public String ENTER_LIST_FLAG_KEY = "enter_list_flag_key";

    public HashMap<String,Boolean> mStateMgsMap = new HashMap<String, Boolean>();
    public void putBooleanState(String key,boolean value){
        if(null != key){
            mStateMgsMap.put(key,value);
        }
    }
    public boolean getBooleanState(String key){
        if(mStateMgsMap.containsKey(key)){
            return mStateMgsMap.get(key);
        }
        return false;
    }
    private void clearStateMgsMap(){
        mStateMgsMap.clear();
        mStateMgsMap = null;
    }
    public static final int NO_INIT = -1;
    public static final int EVENT_SHORT_CLICK = 0;
    public static final int EVENT_LONG_CLICK = 1;
    public HashMap<String,Integer> keyEventMgs = new HashMap<String, Integer>();

    public void putKeyEventName(String key,int value){
        if(null != key){
            keyEventMgs.put(key,value);
        }
    }
    public int getKeyEventValue(String key){
        if(null != key && keyEventMgs.containsKey(key)){
            return keyEventMgs.get(key);
        }
        return NO_INIT;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.d(" baseActivity oncreate");


        createActivity(savedInstanceState);

        Intent intent = super.getIntent();
        int cmd = Definition.CMD_INVALID;
        if(null != intent){
            cmd = intent.getIntExtra(Definition.PARAM_CMD, Definition.CMD_INVALID);
        }

        LogUtil.i(TAG,"================");
        LogUtil.i(TAG,"cmd="+cmd);
        LogUtil.i(TAG,"isListPlay="+MusicPlayerManager.getInstance().isListPlay());
        LogUtil.i(TAG,"isStopUSBAutoPlay="+MusicPlayerManager.getInstance().isStopUSBAutoPlay());
        LogUtil.i(TAG,"================");

        if(!MusicPlayerManager.getInstance().isListPlay()
                && !MusicPlayerManager.getInstance().isStopUSBAutoPlay()) {
            Intent startIntent = new Intent();
            startIntent.putExtra(Definition.PARAM_CMD,cmd);
            startIntent.setClass(this, mServiceClass);
            startIntent.setAction(Definition.ACTION_PLAY_TOGGLE);
            startService(startIntent);
        }

        Intent bindIntent = new Intent(this, mServiceClass);
        bindIntent.putExtra(Definition.PARAM_CMD,cmd);
        bindService(bindIntent, mMySymbol.mConn, Context.BIND_AUTO_CREATE);
        startDataBinding();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mMySymbol.mIsBound) {
            getDataForView();
        }

        startActivity();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        restartActivity();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeActivity();
//        MusicPlayerManager.getInstance().setListPlayState(false);
//        MusicPlayerManager.getInstance().setmIsStopUSBAutoPlay(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseActivity();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopActivity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyActivity();

        //需要模式管理追加接口，在onDestroy调用前进行
        if (mMySymbol.mIsBound) {
            mMySymbol.unRegister();
        }
        unbindService(mMySymbol.mConn);
        mMySymbol = null;
        keyEventMgs.clear();
        keyEventMgs = null;
        clearStateMgsMap();
        MusicPlayerManager.getInstance().setListPlayState(false);
    }


}

