package com.semisky.automultimedia.activity.symbol;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.databinding.BaseObservable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.semisky.automultimedia.activity.BaseActivity;
import com.semisky.automultimedia.common.utils.LogUtil;

import java.io.Serializable;

import static com.semisky.automultimedia.common.constant.Definition.MSG_ACTIVITY_TO_SERVICE;
import static com.semisky.automultimedia.common.constant.Definition.MSG_REGISTER;
import static com.semisky.automultimedia.common.constant.Definition.MSG_REGISTER_SUCCESS;
import static com.semisky.automultimedia.common.constant.Definition.MSG_SERVICE_TO_ACTIVITY;
import static com.semisky.automultimedia.common.constant.Definition.MSG_UNREGISTER;
import static com.semisky.automultimedia.common.constant.Definition.MSG_UNREGISTER_SUCCESS;

/**
 * Created on 2017/12/15.
 * Author: xiongjun
 * About:
 */

public abstract class BaseSymbol extends BaseObservable{
    private static final String TAG = BaseSymbol.class.getSimpleName();
    public boolean mIsBound = false;

    public abstract void createSymbol();

    public abstract void destroySymbol();

    public abstract void onMsgRegisterSuccess();

    public abstract void onMsgUnregisterSuccess();

    protected abstract void upDataProperty(int apiID, Serializable data);


    @SuppressLint("HandlerLeak")
    public Handler mActivityHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_SUCCESS:
                    LogUtil.i(TAG,"mActivityHandler MSG_REGISTER_SUCCESS...");
                    mIsBound = true;
                    onMsgRegisterSuccess();
                    break;
                case MSG_UNREGISTER_SUCCESS:
                    onMsgUnregisterSuccess();
                    break;
                case MSG_SERVICE_TO_ACTIVITY:
                    int apiID = msg.arg2;
                    Bundle bundle = msg.getData();
                    Serializable data = null;
                    if (null != bundle) {
                        data = bundle.getSerializable("Property");
                    }
                    upDataProperty(apiID, data);
                    break;
            }
        }
    };

    public Messenger mActivityMessenger = new Messenger(mActivityHandler);
    public Messenger mServiceMessenger = null;

    public ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtil.i( "SymbolBase onServiceConnected");
            if (!mIsBound) {
                mIsBound = true;
                mServiceMessenger = new Messenger(service);
                try {
                    Message message = Message.obtain(null, MSG_REGISTER, this.hashCode(), 0);
                    message.replyTo = mActivityMessenger;
                    mServiceMessenger.send(message);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtil.i( "SymbolBase onServiceDisconnected");

            mIsBound = false;
            mServiceMessenger = null;
        }
    };

    protected void onCreate() {
        LogUtil.i( "SymbolBase onCreate");
        createSymbol();
    }

    protected void onDestroy() {
        LogUtil.i( "SymbolBase onDestroy");
        destroySymbol();
    }

    public void unRegister() {
        LogUtil.i( "SymbolBase unRegister");
        mIsBound = false;
        if (null != mServiceMessenger) {
            try {
                Message message = Message.obtain(null, MSG_UNREGISTER, this.hashCode(), 0);
                message.replyTo = mActivityMessenger;
                mServiceMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            mServiceMessenger = null;
        }
    }

    public void sendCommand(int apiID, Serializable data) {
        LogUtil.i( "SymbolBase sendCommand");

        if (null != mServiceMessenger) {
            try {
                Message message = Message.obtain(null, MSG_ACTIVITY_TO_SERVICE, this.hashCode(), 0);

                message.arg2 = apiID;

                Bundle bundle = new Bundle();
                bundle.putSerializable("Action", data);
                message.setData(bundle);

                mServiceMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }


}
