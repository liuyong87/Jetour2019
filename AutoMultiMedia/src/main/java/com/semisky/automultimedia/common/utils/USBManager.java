package com.semisky.automultimedia.common.utils;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.provider.Settings;

import com.semisky.automultimedia.MediaApplication;
import com.semisky.automultimedia.common.mediascan.MediaScanner;
import com.semisky.automultimedia.common.sql.MediaDBManager;
import com.semisky.automultimedia.common.sql.SharePreferenceUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/12/14.
 */

public class USBManager {
    private static final String TAG = USBManager.class.getSimpleName();
    private String mCurMountedUsbPath = null;// 当前挂载的首个USB 标识
    private boolean mIsMountUsb1 = false;
    private Context mContext;

    public static volatile USBManager instance;
    private List<OnUsbStateChangeListener> usbListeners = new ArrayList<OnUsbStateChangeListener>();


    private USBManager() {
        this.mContext = MediaApplication.context;
    }

    public static USBManager getInstance() {
        if (instance == null){
            synchronized (USBManager.class){
                if (instance == null){
                    instance = new USBManager();
                }
            }
        }
        return instance;
    }

    /** U盘状态变化监听器 */
    public interface OnUsbStateChangeListener {
        void onUsbMounted();

        void onUsbUnMounted();
        // USB_DEVICE_ATTACHED USB_DEVICE_DETACHED
        void onUsbDeviceAttached();
        void onUsbDeviceDetached();
    }

    /** 注册USB状态监听 */
    public void registerOnUsbStateChangeListener(OnUsbStateChangeListener listener) {
        if (listener != null && usbListeners != null && !usbListeners.contains(listener)) {
            usbListeners.add(listener);
        }
    }

    /** 反注册USB状态监听 */
    public void unRegisterOnUsbStateChangeListener(OnUsbStateChangeListener listener) {
        if (usbListeners != null && listener != null && usbListeners.contains(listener)) {
            usbListeners.remove(listener);
            listener = null;
        }
    }

    /** 通知所有的观察者U盘挂载 */
    public void notifyAllObserversUsbStateMounted() {
        if (usbListeners != null) {
            for (OnUsbStateChangeListener uListener : usbListeners) {
                uListener.onUsbMounted();
            }
            this.mIsMountUsb1 = true;
        }
    }

    /** 通知所有的观察者U盘卸载 */
    public void notifyAllObserversUsbStateUnMounted() {
        if (usbListeners != null) {
            for (OnUsbStateChangeListener uListener : usbListeners) {
                uListener.onUsbUnMounted();
            }
            this.mIsMountUsb1 = false;
        }
    }

    public void notifyAllObserversUsbStateAttached() {
        if (usbListeners != null) {
            for (OnUsbStateChangeListener uListener : usbListeners) {
                uListener.onUsbDeviceAttached();
            }
        }
    }

    public void notifyAllObserversUsbStateDetached() {
        if (usbListeners != null) {
            for (OnUsbStateChangeListener uListener : usbListeners) {
                uListener.onUsbDeviceDetached();
            }
        }
    }


    /**
     * @return 是否挂载usb
     */
    public boolean isMountedUsb() {
        if (null != getCurMountedUsbPath()) {
            return true;
        }
        return false;
    }

    /**
     *
     * @return
     */
    public String getCurMountedUsbPath() {
        // 1.当前多媒体应用已挂载USB1/USB2,由于异常多媒体退出，首个挂载U盘标识被重置
        // 2.如何USB1/USB2挂载的情况下，默认启用作为首个挂载USB顺序：USB1->USB2
        if (null == mCurMountedUsbPath) {
            String saveFinallCurrentMountedFirstUsbPath = getCurrentMountedUsbPath();

            if (null != saveFinallCurrentMountedFirstUsbPath) {
                boolean isMounted = isUsbMounted(mContext, saveFinallCurrentMountedFirstUsbPath);
                if (isMounted) {
                    mCurMountedUsbPath = saveFinallCurrentMountedFirstUsbPath;
                }
            } else {

            }
        }
        return mCurMountedUsbPath;
    }

    public void setCurMountedUsbPath(String mCurMountedFirstUsbPath) {
        this.mCurMountedUsbPath = mCurMountedFirstUsbPath;
        SaveCurrentMountedPath(mCurMountedFirstUsbPath);
    }


    private void SaveCurrentMountedPath(String path) {
        try {
            Settings.System.putString(mContext.getContentResolver(), "FinallCurrentMountedUsbPath", path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getCurrentMountedUsbPath() {
        return Settings.System.getString(mContext.getContentResolver(), "FinallCurrentMountedUsbPath");
    }


    /** 删除数据库指定路径所有媒体信息 */
    public void deleteAllMediaInfo() {
        LogUtil.d(": ----deleteAllMediaInfo usbFlag: " );
        // 删除指定usb源下数据库媒体数据
        MediaDBManager.getInstance(mContext).deleteDBMusicInfo();
        // 删除数据库图片信息
        MediaDBManager.getInstance(mContext).deleteDBPictureInfo();
        // 删除数据库视频信息
        MediaDBManager.getInstance(mContext).deleteDBVideoInfo();
        // 清除缓存中的指定usbFlag下媒体数据
        MediaScanner.getInstance().notifyClearAllMediaDataByUsb();

        SharePreferenceUtil.insertmusicSize(mContext,0);
        SharePreferenceUtil.insertVedioSize(mContext,0);
        SharePreferenceUtil.insertPictureSize(mContext,0);
//        // 通知音乐信息清除消息
//        notifyMusicInfoClearMessage(usbFlag);
//        // 通知视频信息清除消息
//        notifyVideoInfoClearMessage(usbFlag);
//        // 通知图片信息清除消息
//        notifyPictureInfoClearMessage(usbFlag);
    }

    public void startScanMediaFileForUsb(String usbPath) {// 扫描指定路径下的媒体文件
        MediaScanner.getInstance().startScanMediaFileForUsb(usbPath);
    }


    public boolean isUsbMounted(String storagePath) {
        return isUsbMounted(mContext,storagePath);
    }
    /**
     * 利用反射判断是否挂载了外部存储
     *
     * @param context
     * @param storagePath
     *            挂载路径
     * @return
     */
    public boolean isUsbMounted(Context context, String storagePath) {
        boolean isMounted = false;
        if (storagePath != null && checkUsb(storagePath)) {
            StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            try {
                Method getState = sm.getClass().getMethod("getVolumeState", String.class);
                String state = (String) getState.invoke(sm, storagePath);
                LogUtil.d( "isUsbMounted() storagePath: " + storagePath + ",state: " + state);
                if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                    isMounted = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return isMounted;
    }

    public boolean checkUsb(String path) {
        boolean isExsit = false;
        File usbFile = new File(path);
        if (usbFile != null && usbFile.exists()) {
            File[] files = usbFile.listFiles();
            if (files != null && files.length > 0) {
                isExsit = true;
            } else {
                isExsit = false;
            }
        }
        return isExsit;
    }

    public static String PATH_A = "udisk";
    public static String PATH_B = "udisk2";
    /**
     * 检查USB是否挂载
     * @return
     */
    public boolean isUdiskExist(String usbPath) {
        LogUtil.d(TAG, "isUdiskExist()...");
        if(null == usbPath){
            return false;
        }

        if(usbPath.endsWith(PATH_A)){
            usbPath = PATH_A;
        }else if(usbPath.endsWith(PATH_B)){
            usbPath = PATH_B;
        }else {
            return false;
        }

        String path = "/proc/mounts";

        boolean ret = false;
        try {
            String encoding = "GBK";
            File file = new File(path);
            if ((file.isFile()) && (file.exists())) {
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file), encoding);
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while (((lineTxt = bufferedReader.readLine()) != null) && (!ret)) {
                    String[] a = lineTxt.split(" ");//将读出来的一行字符串用 空格 来分割成字符串数组并存储进数组a里面
                    String str = a[0];//取出位置0处的字符串

                    if ((str.contains("/dev/block/vold")) &&
                            (a[1].contains("udisk"))) {
                        ret = true;
                    }
                }

                read.close();
            } else {
                LogUtil.d(TAG, "can't find file: " + path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LogUtil.d(TAG, "isUdiskExist()="+ret);
        return ret;
    }
}
