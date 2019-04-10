package com.semisky.automultimedia.common.broadRecevier;

import android.content.Intent;

import com.semisky.automultimedia.MediaApplication;
import com.semisky.automultimedia.activity.VideoPlayActivity;

/**
 * Created by liuyong on 18-6-21.
 */

public class VideoFactoryTestManager {
    private static VideoFactoryTestManager _instance;
    private OnFactoryTestCommandListener mOnFactoryTestCommandListener;// 监听接收工厂测试命令接口

    private VideoFactoryTestManager(){

    }

    public static VideoFactoryTestManager getInstance(){
        if(null == _instance){
            _instance = new VideoFactoryTestManager();
        }
        return _instance;
    }

    // 监听接收工厂测试命令接口
    public interface OnFactoryTestCommandListener{
        void onReceiverCommand(int cmd);
    }



    /**
     * 注册监听接收工厂测试命令接口
     * @param listener
     */
    public void registerOnFactoryTestCommandListener(OnFactoryTestCommandListener listener){
        this.mOnFactoryTestCommandListener = listener;
    }
    public void unRegisterOnFactoryTestListener(){
        if (mOnFactoryTestCommandListener!=null){
            mOnFactoryTestCommandListener=null;
        }
    }

    /**
     * 接受工厂测试命令通知
     * @param cmd
     */
    public void notifyReceiverCommand(int cmd){

        if(null != this.mOnFactoryTestCommandListener){
            this.mOnFactoryTestCommandListener.onReceiverCommand(cmd);
        }
    }

}
