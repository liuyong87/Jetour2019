package com.semisky.automultimedia.common.broadRecevier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.net.Uri;

import com.semisky.automultimedia.MediaApplication;
import com.semisky.automultimedia.common.constant.Definition;
import com.semisky.automultimedia.common.mediascan.MediaScanner;
import com.semisky.automultimedia.common.sql.SharePreferenceUtil;
import com.semisky.automultimedia.common.strategys.StrategyManager;
import com.semisky.automultimedia.common.utils.AppUtils;
import com.semisky.automultimedia.common.utils.LogUtil;
import com.semisky.automultimedia.common.utils.USBManager;
import com.semisky.automultimedia.common.utils.VideoAudioFocusManager;
import com.semisky.autoservice.manager.AutoManager;

import static com.semisky.automultimedia.common.constant.Definition.ACTION_USB_MOUNT;
import static com.semisky.automultimedia.common.constant.Definition.USB_MOUNT_SERVICE_CLZ;
import static com.semisky.automultimedia.common.constant.Definition.USB_MOUNT_SERVICE_PKG;
import static com.semisky.automultimedia.common.constant.Definition.USB_PATH;

/**
 * Created by Administrator on 2017/12/14.
 */

public class DeviceReciver extends BroadcastReceiver {

    private static final java.lang.String TAG = DeviceReciver.class.getSimpleName();
    private static final int DEV_USB_ID = 0x08;// USB设备ID
    private Context mContext;
    private static long mUsbCheckingTime;
    private static long mUsbMountedTime;
    private static boolean mBeforeMounted = false;
    public  String USBSerial=null;
    private static boolean isEqually=true;//叛徒是不是同一个U盘
    public static boolean getIsEqually(){
        return isEqually;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.mContext = context;
        String action = getAction(intent);
        String usbPath = getPath(intent);

        LogUtil.i(TAG, "===========USB STATE INFO START======================");
        LogUtil.i(TAG, "onReceive() action = " + action);
        LogUtil.i(TAG, "onReceive() usbPath = " + usbPath);
        LogUtil.i(TAG, "===========USB STATE INFO END  ======================");

        if("android.hardware.usb.action.USB_DEVICE_ATTACHED".equals(action)){
            if((isUSB(intent))){
                //获取USB序列号保存
                getFirstAndSaveUSBSerial(context,intent);
                attachedUSBHandler();
            }
            return;
        }else if("android.hardware.usb.action.USB_DEVICE_DETACHED".equals(action)){
            if(isUSB(intent)){
                //拔出U盘时如果是处于播放状态，且前台浏览图片的情况，再次插入U盘 恢复音乐播放
                if ( SharePreferenceUtil.getCurrentMusicIsPlaying(MediaApplication.context) && SharePreferenceUtil.getLastAppFlag()!=Definition.APP.FLAG_MUSIC){
                    SharePreferenceUtil.saveLastAppFlag(Definition.APP.FLAG_MUSIC);
                }

                detachedUSBHandler();
            }
            return;
        }

        // 匹配USB
        if (!USB_PATH.equals(usbPath)) {return;}

        if (Intent.ACTION_MEDIA_CHECKING.equals(action)) {
            // 检查USB事件处理
            checkingUSBHandler(usbPath);
        } else if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
            MediaApplication.appExit();
            // 挂载USB事件处理
            if(USBManager.getInstance().isUdiskExist(Definition.USB_PATH)){
                mountUSBHandler(usbPath);
            }else {
                //更新挂载对话框：加载失败
                updateDeviceMountDialog(mContext, "failure", null);
            }
        } else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
            // 卸载USB事件处理
          //  unmountUSBHandler(usbPath);
        }

    }

    private boolean isUSB(Intent intent){
        UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        int devId = device.getInterface(0).getInterfaceClass();
        LogUtil.i(TAG,"devId="+devId);
        if(DEV_USB_ID == devId){
            return true;
        }
        return false;
    }

    private void detachedUSBHandler(){
        LogUtil.i(TAG,"detachedUSBHandler()...");
        USBManager.getInstance().notifyAllObserversUsbStateDetached();
        unmountUSBHandler(Definition.USB_PATH);
    }

    private void attachedUSBHandler(){
        LogUtil.i(TAG,"attachedUSBHandler()...");
        USBManager.getInstance().notifyAllObserversUsbStateDetached();
    }

    private void unmountUSBHandler(String usbPath) {
        StrategyManager.getInstance().setmUpdateState(false);// 重置升级标识
        StrategyManager.getInstance().resetHighPriorityAppContinityEffctState();
        boolean isMounted = USBManager.getInstance().isUsbMounted(Definition.USB_PATH);
        LogUtil.i(TAG, "ACTION_MEDIA_UNMOUNTED mBeforeMounted=" + mBeforeMounted+",isMounted="+isMounted);
        if(!isMounted){
            AppUtils.setStopMediaPlayStopState(false);
        }

        AppUtils.sendLoadDataStateBroadcast(mContext,false);
//        if (mBeforeMounted) {
            mBeforeMounted = false;
            MediaScanner.getInstance().setUSBMounted(false);
            //更新挂载对话框：加载失败
            updateDeviceMountDialog(mContext, "failure", null);
            USBManager.getInstance().deleteAllMediaInfo();
            USBManager.getInstance().setCurMountedUsbPath(null);
            USBManager.getInstance().notifyAllObserversUsbStateUnMounted();
            MediaApplication.appExit();
            // 能Radio前台播放
            if(VideoAudioFocusManager.getInstance(mContext).isHasAudiofocus()){
                AppUtils.launcherRadioApp(mContext.getPackageName(), AutoManager.FOREGROUND_LAUNCH);
            }
//        }
    }

    private void mountUSBHandler(String usbPath) {
        mBeforeMounted = true;
        mUsbMountedTime = System.currentTimeMillis();
        printLoggerByUSBMountedTotalTime();// 打印USB 挂载总时间日志

        MediaScanner.getInstance().setUSBMounted(true);
        USBManager.getInstance().notifyAllObserversUsbStateMounted();

        //更新挂载对话框：加载成功
        updateDeviceMountDialog(mContext, "success", usbPath);
    }

    // 显示跳转USB挂载服务意图
    public void updateDeviceMountDialog(Context context, String flag, String path) {
        Intent intent = new Intent();
        intent.setClassName(USB_MOUNT_SERVICE_PKG, USB_MOUNT_SERVICE_CLZ);
        intent.setAction(ACTION_USB_MOUNT);
        intent.putExtra("flag", flag);
        intent.putExtra("usbpath", path);
        context.startService(intent);
    }

    // 检查挂载USB
    private void checkingUSBHandler(String usbPath) {
        mUsbCheckingTime = System.currentTimeMillis();
        boolean isMountedUsb = USBManager.getInstance().isMountedUsb();
        if (!isMountedUsb) {
            USBManager.getInstance().setCurMountedUsbPath(usbPath);
        }

        /*if (!isMountedUsb) {*/
//            updateDeviceMountDialog(mContext, "loading", usbPath);
       /* }*/
        USBManager.getInstance().deleteAllMediaInfo();// 删除多媒体数据库数据
    }

    private String getPath(Intent intent) {
        if (null != intent) {
            Uri dataUri = intent.getData();
            if (null != dataUri) {
                return dataUri.getPath();
            }
        }
        return "";
    }

    private String getAction(Intent intent) {
        if (null != intent) {
            return intent.getAction();
        }
        return "";
    }

    // 打印USB 挂载总时间日志
    private void printLoggerByUSBMountedTotalTime() {
        LogUtil.i("printLoggerByUSBMountedTotalTime()...");
        LogUtil.i("==================================================");
        LogUtil.i("======Check USB Mounted Total Time================");
        LogUtil.i("==================================================");
        LogUtil.i("USB Checking Time : " + mUsbCheckingTime);
        LogUtil.i("USB Mounted Time : " + mUsbMountedTime);
        LogUtil.i("USB Mounted Time - USB Checking Time : " + (mUsbMountedTime - mUsbCheckingTime));
        LogUtil.i("==================================================");
    }

    private void getFirstAndSaveUSBSerial(Context context,Intent intent){
        UsbManager mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        UsbDeviceConnection connection = null;
        if(null != device){
            connection = mUsbManager.openDevice(device);
        }

        LogUtil.i(TAG,"getFirstAndSaveUSBSerial() has UsbDevice="+(null != device?true:false));
        LogUtil.i(TAG,"getFirstAndSaveUSBSerial() has connection="+(null != connection?true:false));

        if(null != connection){
            USBSerial=connection.getSerial();
            connection.close();
        }

        if (SharePreferenceUtil.getUSBSerial()==null||!SharePreferenceUtil.getUSBSerial().equals(USBSerial)){
            isEqually=false;
            SharePreferenceUtil.saveUSBSerial(MediaApplication.context,USBSerial);
        }else {
            isEqually=true;
        }

    }


}
