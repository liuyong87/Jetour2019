package com.semisky.automultimedia.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.semisky.automultimedia.common.utils.LogUtil;
import com.semisky.automultimedia.service.adapter.SemiskyBaseAdapter;
import com.semisky.automultimedia.service.datainfo.BaseDataInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.semisky.automultimedia.common.constant.Definition.MSG_ACTIVITY_TO_SERVICE;
import static com.semisky.automultimedia.common.constant.Definition.MSG_ADAPTER_TO_SERVICE;
import static com.semisky.automultimedia.common.constant.Definition.MSG_REGISTER;
import static com.semisky.automultimedia.common.constant.Definition.MSG_REGISTER_SUCCESS;
import static com.semisky.automultimedia.common.constant.Definition.MSG_UNREGISTER;
import static com.semisky.automultimedia.common.constant.Definition.MSG_UNREGISTER_SUCCESS;

/**
 * Created on 2017/12/15.
 * Author: xiongjun
 * About:
 */

public abstract class BaseService extends Service {
    private List<Messenger> mActivityMessengers = new ArrayList<Messenger>();
    public BaseDataInfo mMyDataInfo = null;
    public SemiskyBaseAdapter mMyAdapter = null;

    @SuppressLint("HandlerLeak")
    public Handler mServiceHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int apiID;
            Bundle bundle;
            Serializable data;

            switch (msg.what) {
                case MSG_REGISTER:
                    mMyDataInfo.setActivityMessenger(msg.replyTo);
                    mActivityMessengers.add(msg.replyTo);
                    try {
                        Message message = Message.obtain(null, MSG_REGISTER_SUCCESS, this.hashCode(), 0);
                        msg.replyTo.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case MSG_UNREGISTER:
                    mMyDataInfo.removeActivityMessenger(msg.replyTo);
                    mActivityMessengers.remove(msg.replyTo);
                    try {
                        Message message = Message.obtain(null, MSG_UNREGISTER_SUCCESS, this.hashCode(), 0);
                        msg.replyTo.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case MSG_ACTIVITY_TO_SERVICE:
                    apiID = msg.arg2;
                    bundle = msg.getData();
                    data = null;
                    if (null != bundle) {
                        data = bundle.getSerializable("Action");
                    }
                    receiveCommand(apiID, data);
                    break;
                case MSG_ADAPTER_TO_SERVICE:
                    apiID = msg.arg2;
                    bundle = msg.getData();
                    data = null;
                    if (null != bundle) {
                        data = bundle.getSerializable("Data");
                    }
                    receiveData(apiID, data);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    public Messenger mServiceMessenger = new Messenger(mServiceHandler);

    public abstract void createService() ;

    public abstract void startService(Intent intent, int flags, int startId) ;

    public abstract void destroyService();

    public abstract void receiveCommand(int apiID, Serializable data) ;

    public abstract void receiveData(int apiID, Serializable data) ;

    @Override
    public void onCreate() {
        LogUtil.i("BaseService onCreate");
        super.onCreate();
        createService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.i("BaseService onStartCommand");
        startService(intent, flags, startId);
        mMyDataInfo.onStartCommand();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogUtil.i( "BaseService onBind");
        return mServiceMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogUtil.i("BaseService onUnbind");
        super.onUnbind(intent);
        return false;
    }


    @Override
    public void onDestroy() {
        LogUtil.i( "BaseService onDestroy");
        mMyDataInfo.onDestroy();
        mMyDataInfo = null;
        destroyService();
        super.onDestroy();
    }
}
