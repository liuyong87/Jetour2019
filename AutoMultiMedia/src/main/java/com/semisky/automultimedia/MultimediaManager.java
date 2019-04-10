package com.semisky.automultimedia;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.semisky.automultimedia.aidl.IMultimediaAidl;
import com.semisky.automultimedia.common.utils.LogUtil;

/**
 * Created by liuyong on 18-4-16.
 */

public class MultimediaManager {

    private static final String TAG = MultimediaManager.class.getSimpleName();
    private static final String MEDIA_PKG_NAME = "com.semisky.automultimedia";
    private static final String USB_SERVICE_CLZ_NAME = "com.semisky.automultimedia.service.DeviceMountService";
    private static final String MUSIC_SERVICE_CLZ_NAME = "com.semisky.automultimedia.service.MusicPlayService";
    // Multimedia app flag
    public static final int APP_MUSIC_FLAG = 1;
    public static final int APP_VIDEO_FLAG = 2;
    public static final int APP_PICTURE_FLAG = 3;

    private Context mContext;
    private static MultimediaManager INSTANCE;
    private IMultimediaAidl mProxyService;
    private OnServiceConnetedCompletedListener mOnServiceConnetedCompletedListener;


    private MultimediaManager(Context ctx) {
        this.mContext = ctx;
        // Empty
    }

    public static MultimediaManager getInstance(Context ctx) {
        if (null == INSTANCE) {
            synchronized (MultimediaManager.class) {
                if (null == INSTANCE) {
                    INSTANCE = new MultimediaManager(ctx);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 服务连接监听接口
     *
     * @author LiuXia
     */
    public interface OnServiceConnetedCompletedListener {
        void onConnectedCompleted(IBinder service);
    }

    /**
     * 注册服务连接监听接口
     *
     * @param listener
     */
    public void registerOnServiceConnetedCompletedListener(
            OnServiceConnetedCompletedListener listener) {
        mOnServiceConnetedCompletedListener = listener;
    }

    /**
     * 反注册服务连接监听接口
     */
    public void unregisterOnServiceConnetedCompletedListener() {
        mOnServiceConnetedCompletedListener = null;
    }

    /**
     * 服务连接接口回调
     */
    private ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            INSTANCE = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtil.i(TAG, "Multimedia onServiceConnected() SUCCESS !!!");
            mProxyService = IMultimediaAidl.Stub.asInterface(service);
            if (mOnServiceConnetedCompletedListener != null) {
                mOnServiceConnetedCompletedListener.onConnectedCompleted(service);
            }
        }
    };

    /**
     * 启动服务
     */
    public void startServiceInvoke() {
        LogUtil.i(TAG, "startServiceInvoke()...");
        Intent i = new Intent();
        i.setClassName(MEDIA_PKG_NAME, USB_SERVICE_CLZ_NAME);
        mContext.startService(i);
    }

    /**
     * 停止服务
     */
    public void stopServiceInvoke() {
        LogUtil.i(TAG, "stopServiceInvoke()...");
        Intent i = new Intent();
        i.setClassName(MEDIA_PKG_NAME, USB_SERVICE_CLZ_NAME);
        mContext.stopService(i);
    }

    /**
     * 绑定服务
     */
    public void bindServiceInvoke() {
        LogUtil.i(TAG, "bindServiceInvoke() ...");
        Intent i = new Intent();
        i.setClassName(MEDIA_PKG_NAME, USB_SERVICE_CLZ_NAME);
        mContext.bindService(i, mConn, Context.BIND_AUTO_CREATE);
    }

    /**
     * 解绑服务
     */
    public void unbindServiceInvoke() {
        LogUtil.i(TAG, "unbindServiceInvoke() ...");
        if (mConn != null) {
            mContext.unbindService(mConn);
            mConn = null;
        }
    }

    /**
     * 获取多媒体代理服务
     *
     * @return
     */
    public IMultimediaAidl getProxyService() {
        return this.mProxyService;
    }

    /**
     * 是否连接代理多媒体服务
     *
     * @return
     */
    public boolean isConnected() {
        if (null != mProxyService) {
            return true;
        }
        return false;
    }

    /**
     * 检查媒体数据
     */
    public boolean hasData() {
        try {
            if (isConnected()) {
                return mProxyService.hasMediaData();
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
        return false;
    }

    /**
     * 检查媒体数据
     */
    public boolean hasData(int appFlag) {
        try {
            if (isConnected()) {
                return mProxyService.hasMediaDataByAppointFlag(appFlag);
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
        return false;
    }

    /**
     * 获取媒体应用记忆标识
     *
     * @return
     */
    public int getAppFlag() {
        try {
            if (isConnected()) {
                return mProxyService.getAppFlag();
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
        return -1;
    }

    /**
     * 处理异常获取媒体异常
     */
    public void handlerMediaDataException() {
        try {
            mProxyService.handlerMediaDataException();
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    /**
     * 后台恢复音乐播放
     */
    public void backgroundPlayMusic() {
        Intent i = new Intent();
        i.setAction("com.semisky.music.ACTION_PLAY_TOGGLE");
        i.setClassName(MEDIA_PKG_NAME, MUSIC_SERVICE_CLZ_NAME);
        mContext.startService(i);
    }


}
