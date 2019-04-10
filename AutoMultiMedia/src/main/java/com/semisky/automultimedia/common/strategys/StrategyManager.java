package com.semisky.automultimedia.common.strategys;

import android.util.Log;

import com.semisky.automultimedia.common.constant.Definition;
import com.semisky.automultimedia.common.utils.AppUtils;
import com.semisky.automultimedia.common.utils.LogUtil;
import com.semisky.automultimedia.common.utils.USBManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyong on 18-3-24.
 */

public class StrategyManager {
    private static final String TAG = StrategyManager.class.getSimpleName();
    private static StrategyManager INSTANCE;

    // 屏保事件标识
    public static final int EVENT_SCREEN_SAVER = 0;
    public static final int EVENT_BT_CALL = 1;
    public static final int EVENT_IFLYTEK_VOICE = 2;// 科大讯飞语音标识
    public static final int EVENT_UPDATE = 3;// 地图与导航升级标识
    public static final int EVENT_AVM = 4;// AVM事件标识
    public static final int EVENT_COLSE_SCRREN = 5;// 关屏事件标识

    private StrategyManager() {

    }

    public static StrategyManager getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new StrategyManager();
        }
        return INSTANCE;
    }


    private boolean mBTCallState;// 蓝牙电话状态
    private boolean mIsScreensaverMode;// 屏保状态
    private boolean mIsMeddlewareBreakpointApp = false;
    private boolean mIFlytekVoiceState;
    private boolean mUpdateState;
    private boolean mAVMState;
    private boolean highPriorityAppContinityEffctState;// 高优先任务延续影响标识
    private boolean mColseScreenState;// 关屏状态


    public void setmColseScreenState(boolean state){
        this.mColseScreenState =  state;
    }

    public boolean getmColseScreenState(){
        return this.mColseScreenState;
    }

    public void setHighPriorityAppContinityEffctState(boolean state) {

        if (USBManager.getInstance().isUsbMounted(Definition.USB_PATH)) {
            LogUtil.i(TAG, "setHighPriorityAppContinityEffctState=" + state);
            this.highPriorityAppContinityEffctState = state;
        }
    }

    public void resetHighPriorityAppContinityEffctState() {
        this.highPriorityAppContinityEffctState = false;
        LogUtil.i(TAG, "resetHighPriorityAppContinityEffctState=" + highPriorityAppContinityEffctState);
    }

    public boolean isHighPriorityAppContinityEffct() {
        LogUtil.i(TAG, "isHighPriorityAppContinityEffct=" + highPriorityAppContinityEffctState);
        return this.highPriorityAppContinityEffctState;
    }

    public void setmAVMState(boolean state) {
        this.mAVMState = state;
    }

    public boolean getAVMState() {
        return this.mAVMState;
    }

    public void setmUpdateState(boolean state) {
        this.mUpdateState = state;
    }

    public boolean getUpdateState() {
        return this.mUpdateState;
    }

    public void setIFlytekVoiceState(boolean state) {
        this.mIFlytekVoiceState = state;
    }

    public boolean getIFlytekVoiceState() {
        Log.i(TAG, "getIFlytekVoiceState()=" + mIFlytekVoiceState);
        return this.mIFlytekVoiceState;
    }

    public void setMeddlewareBreakpointAppState(boolean state) {
        this.mIsMeddlewareBreakpointApp = state;
    }

    // 是否为中间件最后一个记忆的应用
    public boolean isMeddlewareBreakpointApp() {
        return mIsMeddlewareBreakpointApp;
    }

    // 匹配策略1
    public boolean isMatchStrategyByFirst() {
        return (AppUtils.isHighPriorityAppRunning() || getIFlytekVoiceState() || AppUtils.isStopMediaPlay() || getUpdateState());
    }

    // 设置蓝牙打电话状态
    public void setBTCallState(boolean state) {
        this.mBTCallState = state;
    }

    // 获取蓝牙打电话状态
    public boolean getBTCallState() {
        return this.mBTCallState;
    }

    // 设置屏保状态
    public void setScreensaverState(boolean isScreensaverMode) {
        this.mIsScreensaverMode = isScreensaverMode;
    }

    // 是否为屏保模式
    public boolean isScreensaverMode() {
        return this.mIsScreensaverMode;
    }


    public interface OnStategyStateListener {
        void onNotifyStategyEvent(int eventCode);
    }

    private List<OnStategyStateListener> mOnStategyStateListenerList = new ArrayList<OnStategyStateListener>();

    public void registerOnStategyStateListener(OnStategyStateListener listener) {
        if (null != listener && !mOnStategyStateListenerList.contains(listener)) {
            mOnStategyStateListenerList.add(listener);
        }
    }

    public void unregisterOnStategyStateListener(OnStategyStateListener listener) {
        if (null != listener && mOnStategyStateListenerList.contains(listener)) {
            mOnStategyStateListenerList.remove(listener);
            listener = null;
        }
    }

    public void notifyStategyEvent(int eventCode) {
        for (OnStategyStateListener listener : mOnStategyStateListenerList) {
            listener.onNotifyStategyEvent(eventCode);
        }
    }


}
