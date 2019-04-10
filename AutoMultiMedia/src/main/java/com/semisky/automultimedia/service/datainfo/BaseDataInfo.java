package com.semisky.automultimedia.service.datainfo;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.semisky.automultimedia.common.constant.Definition.MSG_SERVICE_TO_ACTIVITY;

/**
 * Created by chenhongrui on 2017/11/27
 * <p>
 * 内容摘要：upDataToActivity() 刷新activity
 * 版权所有：Semisky
 * 修改内容：
 * 修改日期
 */
public abstract class BaseDataInfo {

    private List<Messenger> mActivityMessengers = new ArrayList<Messenger>();

    public abstract void createDataInfo();

    public abstract void startDataInfo();

    public abstract void destroyDataInfo();

    public void setActivityMessenger(Messenger messenger) {
        Log.i("usb", "DataInfoBase setActivityMessenger");
        mActivityMessengers.add(messenger);
    }

    public void removeActivityMessenger(Messenger messenger){
        mActivityMessengers.remove(messenger);
    }

    public void onCreate() {
        Log.d("usb", "DataInfoBase onCreate");
        createDataInfo();
    }

    public void onStartCommand() {
        Log.d("usb", "DataInfoBase onStartCommand");
        startDataInfo();
    }

    public void onDestroy() {
        Log.d("usb", "DataInfoBase onDestroy");
        destroyDataInfo();
    }

    public void upDataToActivity(int apiID, Serializable data) {
        Log.d("usb", "DataInfoBase upDataToActivity");

        if (mActivityMessengers.size() > 0) {
            try {
                for (Messenger m : mActivityMessengers) {
                    Message message = Message.obtain(null, MSG_SERVICE_TO_ACTIVITY, this.hashCode(), 0);
                    message.arg2 = apiID;
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("Property", data);
                    message.setData(bundle);
                    m.send(message);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        Log.d("usb", "DataInfoBase upDataToActivity end");
    }
}
