package com.semisky.automultimedia;

import android.os.RemoteException;

import com.semisky.automultimedia.aidl.IMultimediaAidl;
import com.semisky.automultimedia.common.constant.Definition;
import com.semisky.automultimedia.common.sql.SharePreferenceUtil;
import com.semisky.automultimedia.common.strategys.StrategyManager;
import com.semisky.automultimedia.common.utils.AppUtils;
import com.semisky.automultimedia.common.utils.LogUtil;
import com.semisky.automultimedia.common.utils.USBManager;
import com.semisky.automultimedia.service.DeviceMountService;

import java.lang.ref.WeakReference;

/**
 * Created by liuyong on 18-4-16.
 */

public class MultimediaAidlImpl extends IMultimediaAidl.Stub {
    private static final String TAG = MultimediaAidlImpl.class.getSimpleName();
    private WeakReference<DeviceMountService> mServiceRef;


    /**
     * 绑定本地服务
     *
     * @param service
     */
    public void attachLocalService(DeviceMountService service) {
        LogUtil.i(TAG, "attechPoxyService() ...");
        mServiceRef = new WeakReference<DeviceMountService>(service);
    }

    /**
     * 解绑本地服务
     */
    public void detachLocalService() {
        LogUtil.i(TAG, "detechPoxyService() ...");
        mServiceRef.clear();
    }

    /**
     * 是否有代理服务对象
     *
     * @return
     */
    private boolean hasProxyServiceRef() {
        if (null != mServiceRef && mServiceRef.get() != null) {
            return true;
        }
        return false;
    }

    /**
     * 检查媒体数据
     **/
    @Override
    public boolean hasMediaData() throws RemoteException {
        if (hasProxyServiceRef()) {
            boolean isUsbMounted = USBManager.getInstance().isUsbMounted(Definition.USB_PATH);
            if (isUsbMounted) {
                boolean hasData = AppUtils.hasMediaData();
                if (hasData) {
                    LogUtil.i(TAG, "EXISTED USB MEDIA DATA !!!" + hasData);
                    return true;
                }
                LogUtil.w(TAG, "USB EMPTY MEDIA DATA ..." + hasData);
                return false;
            }
            LogUtil.w(TAG, "USB UNMOUNTED STATE ..." + isUsbMounted);
            return false;
        }
        LogUtil.w(TAG, "hasProxyServiceRef == NULL ...");
        return false;
    }

    /**
     * 检查媒体数据
     **/
    @Override
    public boolean hasMediaDataByAppointFlag(int appFlag) throws RemoteException {
        if (hasProxyServiceRef()) {
            boolean isUsbMounted = USBManager.getInstance().isUsbMounted(Definition.USB_PATH);
            if (isUsbMounted) {
                boolean hasData = AppUtils.hasMediaData(appFlag);
                if(hasData){
                    LogUtil.i(TAG, "EXISTED USB MEDIA DATA !!!" + hasData);
                    return true;
                }
                LogUtil.w(TAG, "USB EMPTY MEDIA DATA ...appFlag=" + appFlag+", hasData="+hasData);
                return false;
            }
            LogUtil.w(TAG, "USB UNMOUNTED STATE ..." + isUsbMounted);
            return false;
        }
        LogUtil.w(TAG, "hasProxyServiceRef == NULL ...");
        return false;
    }

    /**
     * 获取多媒体应用APP标识
     *
     * @return
     * @throws RemoteException
     */
    @Override
    public int getAppFlag() throws RemoteException {
        int appFlag = SharePreferenceUtil.getLastAppFlag();
        LogUtil.i(TAG, "getAppFlag() appFlag=" + appFlag);
        if (AppUtils.hasMediaData(appFlag)) {
            LogUtil.i(TAG, "BROKEN APP FLAG ...");
            return appFlag;
        } else {
            LogUtil.i(TAG, "DEFAULT APP FLAG ...");
            return AppUtils.getDefaultAppFlag();
        }
    }

    /**
     * 处理媒体数据异常
     **/
    @Override
    public void handlerMediaDataException() throws RemoteException {
        AppUtils.setStopMediaPlayStopState(false);
        StrategyManager.getInstance().setHighPriorityAppContinityEffctState(false);
        if (hasProxyServiceRef()) {
            LogUtil.i(TAG, "handlerMediaDataException()...");
            mServiceRef.get().checkUsbData();
        }
    }


}
