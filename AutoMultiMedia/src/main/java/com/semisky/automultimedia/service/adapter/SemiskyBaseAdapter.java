package com.semisky.automultimedia.service.adapter;

import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.semisky.automultimedia.service.BaseService;

import java.io.Serializable;
import static com.semisky.automultimedia.common.constant.Definition.MSG_ADAPTER_TO_SERVICE;


/**
 * Created by chenhongrui on 2017/12/8
 * <p>
 * 内容摘要：adapter下发中间层方法，sendToService() 回调service
 * 版权所有：Semisky
 * 修改内容：
 * 修改日期
 */
public class SemiskyBaseAdapter {

    //调用Service函数用
    private BaseService mMyCallBack = null;

    public void onCreate(BaseService adapterAble) {
        //Service对象赋值
        mMyCallBack = adapterAble;
    }

    public void onStartCommand() {

    }

    public void sendToService(int apiID, Serializable data) {
        Log.d("radio", "sendToService");

        if (null != mMyCallBack.mServiceMessenger) {
            try {
                Message message = Message.obtain(null, MSG_ADAPTER_TO_SERVICE, this.hashCode(), 0);
                message.arg2 = apiID;
                Bundle bundle = new Bundle();
                bundle.putSerializable("Data", data);
                message.setData(bundle);

                mMyCallBack.mServiceMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }


}
