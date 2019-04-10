package com.semisky.automultimedia.common.musicplay;

import com.semisky.automultimedia.common.bean.MusicInfo;
import com.semisky.automultimedia.common.utils.LogUtil;

/**
 * Created by liuyong on 18-3-27.
 */

public abstract class BaseMusicPlayerManager {

    private static final java.lang.String TAG = BaseMusicPlayerManager.class.getSimpleName();
    private OnClientCallback mOnClientCallback = null;
    private OnServiceCallback mOnServiceCallback = null;

    public abstract void onDestroy();


    public interface OnServiceCallback {
        void onNotifyReset();// 媒体重置

        void onNotifyPrepare();// 媒体准备

        void onNotifyPause();// 媒体暂停

        void onNotifyStart();// 媒体开始播放

        void onNotifyStop(); // 媒体播放停止

        void onNotifyMediaError();// 媒体播放错误

        void onNotifyUpdateProgress(int progress);// 更新播放进度

        void onNotifyUpdateMediaInfos(MusicInfo info);// 更新媒体详情

        void onNotifyReviseTotalTime(int time);// 修正媒体总进度

        void onNotifyUpdateProgramPostion(String posAndTotalQty);// 更新当前播放节目位置

        void onNotifyChangePlayMode(int playMode);// 更新界面播放模式

        void onNotifyNextMediaInfo(MusicInfo info);// 下一个节目信息

        void onNotifyAudioFocusChange(int audioFocus);// 音乐焦点改变

        void onNotifyPlayStateChange(boolean state);// 音乐播放状态
    }

    public interface OnClientCallback {
        void onControlNext();// 下一个节目

        void onControlPrev();// 上一个节目

        void onControlFastForward();// 节目快进

        void onControlFastBackward();// 节目快退

        void onControlPause();// 暂停

        void onControlStart();// 开始播放

        void onControlPlay(String url); // 播放指定音乐

        void onControlPlayOrPause(boolean isManual);// 暂停或播放

        void onControlUpdateProgressEnable(boolean enable);// 是否更新播放进度条 true:更新，false:停止更新

        void onControlChangePlayMode();// 切换播放模式

        void onControlRequestMediaData();// 向服务请求数据

        void onControlCancelFastBackward();// 取消快退操作

        void onControlCancelFastForward();// 取消快进操作

        void onControlSeekTo(int progress);// 播放指定进度媒体
    }

    /*----------------------OnServiceCallback------------------------------------------*/
    // 注册音乐服务状态更新监听
    public void registerOnServiceCallback(OnServiceCallback callback) {
        this.mOnServiceCallback = callback;
    }

    public void unregisterOnServiceCallback() {
        this.mOnServiceCallback = null;
    }

    public void onNotifyReset() {
        if (null != mOnServiceCallback) {
            mOnServiceCallback.onNotifyReset();
        }
    }

    public void onNotifyPrepare() {
        if (null != mOnServiceCallback) {
            mOnServiceCallback.onNotifyPrepare();
        }
    }

    public void onNotifyPause() {
        if (null != mOnServiceCallback) {
            mOnServiceCallback.onNotifyPause();
        }
    }

    public void onNotifyStart() {
        if (null != mOnServiceCallback) {
            mOnServiceCallback.onNotifyStart();
        }
    }

    public void onNotifyStop() {
        if (null != mOnServiceCallback) {
            mOnServiceCallback.onNotifyStop();
        }
    }

    public void onNotifyMediaError() {
        if (null != mOnServiceCallback) {
            mOnServiceCallback.onNotifyMediaError();
        }
    }

    public void onNotifyUpdateProgress(int progress) {
        if (null != mOnServiceCallback) {
            mOnServiceCallback.onNotifyUpdateProgress(progress);
        }
    }

    public void onNotifyUpdateMediaInfos(MusicInfo info) {
        if (mOnServiceCallback == null) {
            LogUtil.e(TAG, "mOnServiceCallback == null");
        }
        if (null != mOnServiceCallback) {
            mOnServiceCallback.onNotifyUpdateMediaInfos(info);
        }
    }

    public void onNotifyReviseTotalTime(int time) {
        if (null != mOnServiceCallback) {
            mOnServiceCallback.onNotifyReviseTotalTime(time);
        }
    }

    public void onNotifyUpdateProgramPostion(String posAndTotalQty) {
        if (null != mOnServiceCallback) {
            mOnServiceCallback.onNotifyUpdateProgramPostion(posAndTotalQty);
        }
    }

    // 通知界面更新播放模式
    public void onNotifyChangePlayMode(int playMode) {
        if (null != mOnServiceCallback) {
            mOnServiceCallback.onNotifyChangePlayMode(playMode);
        }
    }

    // 通知界面更新下一个节目信息
    public void onNotifyNextMediaInfo(MusicInfo info) {
        if (null != mOnServiceCallback) {
            mOnServiceCallback.onNotifyNextMediaInfo(info);
        }
    }

    // 音乐焦点丢失
    public void onNotifyAudioFocusChange(int audioFocus){
        if(null != mOnServiceCallback){
            mOnServiceCallback.onNotifyAudioFocusChange(audioFocus);
        }
    }

    // 音乐播放状态变化
    public void onNotifyPlayStateChange(boolean state){
        if(null != mOnServiceCallback){
            mOnServiceCallback.onNotifyPlayStateChange(state);
        }
    }


    /*----------------------OnClientCallback------------------------------------------*/
    // 注册控制音乐服务监听
    public void registerOnClientCallback(OnClientCallback callback) {
        this.mOnClientCallback = callback;
    }

    public void unregisterOnClientCallback() {
        this.mOnClientCallback = null;
    }

    public void notifyControlNext() {
        if (null != mOnClientCallback) {
            mOnClientCallback.onControlNext();
        }
    }

    public void notifyControlPrev() {
        if (null != mOnClientCallback) {
            mOnClientCallback.onControlPrev();
        }
    }

    // 快退
    public void notifyControlFastBackward() {
        if (null != mOnClientCallback) {
            mOnClientCallback.onControlFastBackward();
        }
    }

    // 快进
    public void notifyControlFastForward() {
        if (null != mOnClientCallback) {
            mOnClientCallback.onControlFastForward();
        }
    }

    public void notifyControlPause() {
        if (null != mOnClientCallback) {
            mOnClientCallback.onControlPause();
            ;
        }
    }

    public void notifyControlStart() {
        if (null != mOnClientCallback) {
            mOnClientCallback.onControlStart();
        }
    }

    public void notifyControlPlayOrPause(boolean isManual) {
        if (null != mOnClientCallback) {
            mOnClientCallback.onControlPlayOrPause(isManual);
        }
    }

    public boolean notifyControlPlay(String url) {
        if (null != mOnClientCallback) {
            mOnClientCallback.onControlPlay(url);
            return true;
        }
        return false;
    }

    public void notifyControlUpdateProgressEnable(boolean enable) {
        if (null != mOnClientCallback) {
            mOnClientCallback.onControlUpdateProgressEnable(enable);
        }
    }

    // 切换播放模式
    public void controlChangePlayMode() {
        if (null != mOnClientCallback) {
            mOnClientCallback.onControlChangePlayMode();
        }
    }

    // 向服务请数据
    public void controlRequestMediaData() {
        if (null != mOnClientCallback) {
            mOnClientCallback.onControlRequestMediaData();
        }
    }

    // 取消快进操作
    public void controlCancelFastForward() {
        if (null != mOnClientCallback) {
            mOnClientCallback.onControlCancelFastForward();
        }
    }

    // 取消快退操作
    public void controlCancelFastBackward() {
        if (null != mOnClientCallback) {
            mOnClientCallback.onControlCancelFastBackward();
        }
    }

    // 播放指定进度媒体
    public void controlSeekTo(int progress){
        if(null != mOnClientCallback){
            mOnClientCallback.onControlSeekTo(progress);
        }
    }

}
