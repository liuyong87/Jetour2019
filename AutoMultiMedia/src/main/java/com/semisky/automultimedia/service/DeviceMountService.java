package com.semisky.automultimedia.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.semisky.automultimedia.MediaApplication;
import com.semisky.automultimedia.MultimediaAidlImpl;
import com.semisky.automultimedia.activity.MusicPlayActivity;
import com.semisky.automultimedia.activity.PicturePlayActivity;
import com.semisky.automultimedia.activity.VideoPlayActivity;
import com.semisky.automultimedia.aidl.IMultimediaAidl;
import com.semisky.automultimedia.common.broadRecevier.DeviceReciver;
import com.semisky.automultimedia.common.broadRecevier.IFlytekVoiceReceiver;
import com.semisky.automultimedia.common.constant.Definition;
import com.semisky.automultimedia.common.mediascan.MediaScanner;
import com.semisky.automultimedia.common.musicplay.MusicPlayerManager;
import com.semisky.automultimedia.common.sql.SharePreferenceUtil;
import com.semisky.automultimedia.common.strategys.StrategyManager;
import com.semisky.automultimedia.common.strategys.VoiceManager;
import com.semisky.automultimedia.common.utils.AppUtils;
import com.semisky.automultimedia.common.utils.LogUtil;
import com.semisky.automultimedia.common.utils.USBManager;
import com.semisky.automultimedia.common.view.USBMountDialog;

import java.lang.ref.WeakReference;

import static com.semisky.automultimedia.common.constant.Definition.ACTION_LAUNCHER_START;
import static com.semisky.automultimedia.common.constant.Definition.ACTION_MODE_PLAY_MUSIC;
import static com.semisky.automultimedia.common.constant.Definition.ACTION_USB_MOUNT;
import static com.semisky.automultimedia.common.constant.Definition.APP.FLAG_MUSIC;
import static com.semisky.automultimedia.common.constant.Definition.APP.FLAG_PICTURE;
import static com.semisky.automultimedia.common.constant.Definition.APP.FLAG_VIDEO;
import static com.semisky.automultimedia.common.view.USBMountDialog.StateMode.STATE_LOADDING;
import static com.semisky.automultimedia.common.view.USBMountDialog.StateMode.STATE_LOAD_FAIL;
import static com.semisky.automultimedia.common.view.USBMountDialog.StateMode.STATE_LOAD_NO_MEDIA;
import static com.semisky.automultimedia.common.view.USBMountDialog.StateMode.STATE_LOAD_NO_USB;
import static com.semisky.automultimedia.common.view.USBMountDialog.StateMode.STATE_LOAD_SUCCESS;

/**
 * Created on 2017/12/14.
 * Author: xiongjun
 * About: 管理加载对话框的service
 */

public class DeviceMountService extends Service {

    private static final java.lang.String TAG = DeviceMountService.class.getSimpleName();
    private USBMountDialog loadDialog;
    private DeviceMountHandler mHandler;
    private StateManager mStateManager = null;

    private MultimediaAidlImpl mProxyMultimediaService = null;


    private static final int MSG_SKIP_APP = 0x10;
    private static final int MSG_REFRESH_USB_DIALOG_STATE = 0x11;
    private static final int MSG_DISMISS_USB_DIALOG = 0x12;

    private static class DeviceMountHandler extends Handler {
        WeakReference<DeviceMountService> mReference = null;

        DeviceMountHandler(DeviceMountService service) {
            this.mReference = new WeakReference<DeviceMountService>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SKIP_APP:
                    mReference.get().skipTo(msg.arg1);

                    break;
                case MSG_REFRESH_USB_DIALOG_STATE:
                    LogUtil.i(TAG, "handleMessage() MSG_REFRESH_USB_DIALOG_STATE =" + msg.arg1);
                    // @liuyong 2019.3.27 修改问题：插入U盘，正在加载媒体资源，进入屏保时钟，拔出U盘，u盘加载弹窗不消失问题。
                    // 1==>导航会拉起来 0==>导航不需要拉起来
                    int status_navai = Settings.System.getInt(MediaApplication.context.getContentResolver(), "STATUS_NAVI", 0);
                    boolean ppmNavai = (status_navai == 1);
                    boolean isMatchStrategyByFirst = StrategyManager.getInstance().isMatchStrategyByFirst();

                    LogUtil.i(TAG, "$$$$$$$$$$$$$$$");
                    LogUtil.i(TAG, "MSG_REFRESH_USB_DIALOG_STATE : " + msg.arg1);
                    LogUtil.i(TAG, "isMatchStrategyByFirst : " + isMatchStrategyByFirst);
                    LogUtil.i(TAG, "status_navai : " + status_navai);
                    LogUtil.i(TAG, "$$$$$$$$$$$$$$$");

                    if (!isMatchStrategyByFirst && !ppmNavai) {
                        mReference.get().refreshDeviceDialogState(msg.arg1, (Boolean) msg.obj);
                    }

                    if (/*STATE_LOAD_SUCCESS == msg.arg1
                            ||*/ STATE_LOAD_FAIL == msg.arg1
                            || STATE_LOAD_NO_MEDIA == msg.arg1
                            || STATE_LOAD_NO_USB == msg.arg1) {

                        mReference.get().sendMsgDismissDeviceDialog(5000);

                    } else if (STATE_LOAD_SUCCESS == msg.arg1) {
                        mReference.get().sendMsgDismissDeviceDialog(3000);
                    }
                    break;
                case MSG_DISMISS_USB_DIALOG:
                    LogUtil.i(TAG, "handleMessage() MSG_DISMISS_USB_DIALOG");
                    mReference.get().dismissDeviceDialog();
                    break;
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (null == mHandler) {
            mHandler = new DeviceMountHandler(this);
        }

//        bindRemoteVoiceService();// 绑定远程语音服务

        mStateManager = new StateManager();
        StrategyManager.getInstance().registerOnStategyStateListener(mOnStategyStateListener);
    }

    private void bindRemoteVoiceService() {
        if (!VoiceManager.getInstance().isBindRemoteVoiceService()) {
            LogUtil.i(TAG, "bindVoiceService() ...");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        VoiceManager.getInstance().bindRemoteVoiceService();
                    } catch (Exception e) {
                        LogUtil.e(TAG, "bindRemoteVoiceService() FAIL !!!!");
                    }
                }
            }).start();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null == intent) {
            LogUtil.e(TAG, "服务启动意图为空！");
            return super.onStartCommand(intent, flags, startId);
        }
        String action = intent.getAction();
        LogUtil.i(TAG, "onStartCommand action = " + action);
        if (ACTION_USB_MOUNT.equals(action)) {
            bindRemoteVoiceService();// 绑定远程语音服务

            String flag = intent.getStringExtra("flag");
            String usbPath = intent.getStringExtra("usbpath");
            if ("loading".equals(flag)) {
                AppUtils.sendLoadDataStateBroadcast(this, false);
                mStateManager.setmLoadState(StateManager.STATE_LOADING);
                sendMsgRefreshDeviceDialogState(STATE_LOADDING, true);
            } else if ("success".equals(flag)) {
                mHandler.removeCallbacksAndMessages(null);
                mStateManager.setmLoadState(StateManager.STATE_LOADING);
                // 1==>导航会拉起来 0==>导航不需要拉起来
                int status_navai = Settings.System.getInt(MediaApplication.context.getContentResolver(), "STATUS_NAVI", 0);
                if (StrategyManager.getInstance().isMatchStrategyByFirst() || status_navai == 1) {
                    StrategyManager.getInstance().setHighPriorityAppContinityEffctState(true);
                }
                sendMsgRefreshDeviceDialogState(STATE_LOADDING, true);
                scanUSBdata(usbPath);
            } else if ("failure".equals(flag)) {
                int loadState = mStateManager.getmLoadState();

                LogUtil.i(TAG, "onStartCommand() loadState=" + loadState);
                if (StateManager.STATE_LOADING == loadState) {// USB未加载完数据时，拔出才显示加载失败弹窗
                    sendMsgRefreshDeviceDialogState(STATE_LOAD_FAIL, true);
                } else {
                    mStateManager.setmLoadState(StateManager.STATE_RESET);
                    removeAllMessage();
                    sendMsgDismissDeviceDialog(0);
                }
                //删除数据库中的信息
                USBManager.getInstance().deleteAllMediaInfo();
            }
        } else if (ACTION_LAUNCHER_START.equals(action)) {
            checkUsbData();
        } else if (ACTION_MODE_PLAY_MUSIC.equals(action)) {// 方控MODE播放音乐
           /* String flag = intent.getStringExtra("flag");
            LogUtil.i(TAG, "flag=" + flag);
            if ("background".equals(flag)) {
                checkUsbDataByMusic();
            }*/
        } else if (Definition.ACTION_COLSE_USB_CONNECT_DIALOG.equals(action)) {// 关闭多媒体加载弹窗
            sendMsgDismissDeviceDialog(0);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    // 启动一个音乐服务
    private void startMusicService() {
        LogUtil.i(TAG, "startMusicService()");
        Intent i = new Intent();
        i.setAction(Definition.ACTION_PLAY_TOGGLE);
        i.setClassName("com.semisky.automultimedia",
                "com.semisky.automultimedia.service.MusicPlayService");
        startService(i);
    }

    // 检查音乐媒体数据
    private void checkUsbDataByMusic() {
        boolean mountedUsb = USBManager.getInstance().isMountedUsb();
        LogUtil.i("checkUsbDataByMusic() mountedUsb = " + mountedUsb);
        if (mountedUsb) {
            int musicSize = SharePreferenceUtil.getMusicSize(this);
            if (musicSize > 0) {
                //ToDo 后台播放音乐意图
                startMusicService();
            }
        }
    }

    public void checkUsbData() {
        boolean mountedUsb = USBManager.getInstance().isUdiskExist(Definition.USB_PATH);
        LogUtil.i("mountedUsb = " + mountedUsb + " , getmLoadState=" + mStateManager.getmLoadState());
        if (mountedUsb) {
            if (mStateManager.getmLoadState() == StateManager.STATE_UNINITIALIZED) {
                if (AppUtils.hasMediaData()) {
                    LogUtil.i("checkUsbData() =============== hasData == true");
                    skipToSpecifyApp();
                } else {
                    LogUtil.i("checkUsbData() =============== STATE_UNINITIALIZED");
                    sendMsgRefreshDeviceDialogState(STATE_LOADDING, true);
                    scanUSBdata(Definition.USB_PATH);
                }

            } else if (mStateManager.getmLoadState() == StateManager.STATE_LOADING) {
                LogUtil.i("checkUsbData() =============== STATE_LOADING");
                sendMsgRefreshDeviceDialogState(STATE_LOADDING, true);
            } else if (mStateManager.getmLoadState() == StateManager.STATE_LOAD_FINISHED) {
                LogUtil.i("checkUsbData() =============== STATE_LOAD_FINISHED");
                skipToSpecifyApp();
            }
        } else {
            LogUtil.i("no Usb Dialog");
            sendMsgRefreshDeviceDialogState(STATE_LOAD_NO_USB, true);
        }


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (null == mProxyMultimediaService) {
            mProxyMultimediaService = new MultimediaAidlImpl();
            mProxyMultimediaService.attachLocalService(this);
        }
        return mProxyMultimediaService;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mProxyMultimediaService) {
            mProxyMultimediaService.detachLocalService();
        }
        MediaScanner.getInstance().unRegisterOnUsbScannerListener(listener);
        StrategyManager.getInstance().unregisterOnStategyStateListener(mOnStategyStateListener);
    }

    private void scanUSBdata(String usbPath) {
        //删除数据库中的信息
        USBManager.getInstance().deleteAllMediaInfo();
        //开始扫描文件
        USBManager.getInstance().startScanMediaFileForUsb(usbPath);
        //注册监听扫描状态监听
        MediaScanner.getInstance().registerOnUsbScannerListener(listener);
    }


    private void startActivitys(@Nullable Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        DeviceMountService.this.startActivity(intent);
    }

    private void startActivitys(Class cls) {
        Intent intent = new Intent(DeviceMountService.this, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        DeviceMountService.this.startActivity(intent);
    }

    private void sendMsgDismissDeviceDialog(int delayMillis) {
        mHandler.removeMessages(MSG_DISMISS_USB_DIALOG);
        mHandler.sendEmptyMessageDelayed(MSG_DISMISS_USB_DIALOG, delayMillis);
    }

    private void dismissDeviceDialog() {
        if (null != loadDialog) {
            loadDialog.dismiss();
        }
    }

    // 发送消息刷新USB弹窗状态信息显示
    private void sendMsgRefreshDeviceDialogState(int state, boolean isShowing) {
        LogUtil.i(TAG, "sendMsgRefreshDeviceDialogState() state=" + state + ", isShowing=" + isShowing);
        mHandler.removeMessages(MSG_REFRESH_USB_DIALOG_STATE);
        Message msg = mHandler.obtainMessage();
        msg.what = MSG_REFRESH_USB_DIALOG_STATE;
        msg.arg1 = state;
        msg.obj = isShowing;
        mHandler.sendMessageDelayed(msg, 0);
    }

    public void refreshDeviceDialogState(int state, boolean isShowing) {
        LogUtil.i(TAG, "==============refreshDeviceDialogState()" + state + ",isShowing=" + isShowing);
        // 初始化USB弹窗
        if (null == loadDialog) {
            loadDialog = new USBMountDialog(this);
        }
        // 更新USB弹窗状态显示信息
        loadDialog.updateState(state);
        // 显示USB弹窗
        if (isShowing) {
            loadDialog.show();
        }
        // 取消USB弹窗
        else {
            loadDialog.dismiss();
        }
    }

    private void sendMsgSkipApp(int appFlag, boolean isDelayTime) {
        removeMsgSkipApp();
        Message msg = mHandler.obtainMessage(MSG_SKIP_APP, appFlag, -1);
        mHandler.sendMessageDelayed(msg, isDelayTime ? 2000 : 0);
    }

    private void removeMsgSkipApp() {
        mHandler.removeMessages(MSG_SKIP_APP);
    }

    // 跳转应用
    private void skipTo(int appFlag) {
        switch (appFlag) {
            case FLAG_MUSIC:
                MusicPlayerManager.getInstance().setmIsStopUSBAutoPlay(true);
                startMusicService();
                startActivitys(MusicPlayActivity.class);
               /* if (null != loadDialog) {
                    loadDialog.dismiss();
                }*/
                break;
            case FLAG_VIDEO:
                startActivitys(VideoPlayActivity.class);
                /*if (null != loadDialog) {
                    loadDialog.dismiss();
                }*/
                break;
            case FLAG_PICTURE:
//                Intent intent = new Intent(DeviceMountService.this, PicturePlayActivity.class);
//                intent.putExtra("type", 2);
//                intent.putExtra("from", Definition.APP.FLAG_PICTURE);
                startActivitys(PicturePlayActivity.class);
               /* if (null != loadDialog) {
                    loadDialog.dismiss();
                }*/
                break;
        }
    }

    private void removeAllMessage() {
        LogUtil.i("removeAllMessage() ...");
        mHandler.removeCallbacksAndMessages(null);
    }

    private MediaScanner.OnUsbScannerListener listener = new MediaScanner.OnUsbScannerListener() {
        @Override
        public void onUsbScanned(int musicSize, int pictureSize, int vedioSize) {
//            boolean mountedUsb = USBManager.getInstance().isMountedUsb();
            boolean mountedUsb = USBManager.getInstance().isUsbMounted(Definition.USB_PATH);
            LogUtil.i("mountedUsb = " + mountedUsb);
            LogUtil.i("musicSize = " + musicSize + ", vedioSize=" + vedioSize + ",pictureSize=" + pictureSize);

            MediaApplication.appExit();
            if (!mountedUsb) {
                return;
            }

            mStateManager.setmLoadState(StateManager.STATE_LOAD_FINISHED);
            AppUtils.sendLoadDataStateBroadcast(DeviceMountService.this, true);
            if (StrategyManager.getInstance().isHighPriorityAppContinityEffct()) {
                LogUtil.i(TAG, "###StrategyMode####");
                if (musicSize > 0 || vedioSize > 0 || pictureSize > 0) {
                    if (AppUtils.isNaviAtForeground() && !AppUtils.isStopMediaPlay()) {// 导航在前台
                        brackgroundPlayMusic();
                    }
                }
                sendMsgRefreshDeviceDialogState(STATE_LOAD_SUCCESS, true);
                return;
            }


            if (musicSize > 0 || vedioSize > 0 || pictureSize > 0) {
                //  MusicPlayerManager.getInstance().setmIsStopUSBAutoPlay(true);
                // skipToSpecifyApp();
                onAutoSkipLastApp();
                sendMsgRefreshDeviceDialogState(STATE_LOAD_SUCCESS, true);
            } else {
                sendMsgRefreshDeviceDialogState(STATE_LOAD_NO_MEDIA, true);
            }
        }
    };

    /**
     * 自动跳转最后记忆的APP
     * 注意：当最后记忆应用标识为图片应用标识时，播放默认次序多媒体应用
     */
    private void onAutoSkipLastApp() {
        int lastAppFlag = SharePreferenceUtil.getLastAppFlag();
        LogUtil.i(TAG, "skipToSpecifyApp() lastAppFlag=" + lastAppFlag);
        // 1.不是同一个U盘 2.记忆的应用标识无数据 3.应用的标识是图片应用标识  ,以上三个条件任意一个条件成立，播放默认次序多媒体应用媒体文件
        if (!DeviceReciver.getIsEqually() || !AppUtils.hasMediaData(lastAppFlag) || lastAppFlag == Definition.APP.FLAG_PICTURE) {
            skipToDefaultApp();

        } else {
            if (AppUtils.hasMediaData(lastAppFlag)) {
                sendMsgSkipApp(lastAppFlag, false);
            }
        }
    }

    private void skipToSpecifyApp() {
        int lastAppFlag = SharePreferenceUtil.getLastAppFlag();
        LogUtil.i(TAG, "skipToSpecifyApp() lastAppFlag=" + lastAppFlag);
        if (!DeviceReciver.getIsEqually() || !AppUtils.hasMediaData(lastAppFlag)) {
            skipToDefaultApp();

        } else {
            if (AppUtils.hasMediaData(lastAppFlag)) {
                sendMsgSkipApp(lastAppFlag, false);
            }
        }

//        if (AppUtils.hasMediaData(lastAppFlag)) {
//
//            switch (lastAppFlag){
//                case Definition.APP.FLAG_MUSIC:
//                    String playingMusicUrl=SharePreferenceUtil.getCurrentPlayingMusicUrl(MediaApplication.getContext());
//                    long time=SharePreferenceUtil.getMusicFileLastModified();
//                    if (!AppUtils.isFileExist(playingMusicUrl,time)){
//                        skipToDefaultApp();
//                    }else {
//                        sendMsgSkipApp(lastAppFlag, false);
//                    }
//                    break;
//                case Definition.APP.FLAG_VIDEO:
//                    String videoPath=SharePreferenceUtil.getCurrentPlayingVideoUrl(MediaApplication.getContext());
//                    long timeVideo=SharePreferenceUtil.getVideoFileLastModified();
//                    if (!AppUtils.isFileExist(videoPath,timeVideo)){
//                        skipToDefaultApp();
//                    }else {
//                        sendMsgSkipApp(lastAppFlag, false);
//                    }
//                    break;
//                case Definition.APP.FLAG_PICTURE:
//                    String picturePath=SharePreferenceUtil.getCurrentPlayingPictureUrl(MediaApplication.getContext());
//                    long timePicture=SharePreferenceUtil.getPictureFileLastModified();
//                    if (!AppUtils.isFileExist(picturePath,timePicture)){
//                        skipToDefaultApp();
//                    }else {
//                        sendMsgSkipApp(lastAppFlag, false);
//                    }
//                    break;
//            }
//            sendMsgSkipApp(lastAppFlag, false);
//        } else {
//            skipToDefaultApp();
//        }

    }

    private void brackgroundPlayMusic() {
        int lastAppFlag = SharePreferenceUtil.getLastAppFlag();
        if (Definition.APP.FLAG_MUSIC == lastAppFlag) {// 最后一次播放的音乐，才恢复播放
            if (AppUtils.hasMediaData(lastAppFlag)) {
                LogUtil.i(TAG, "brackgroundPlayMusic() SUCCESS !!!");
                startMusicService();
            }
        }
    }

    // 跳转默认顺序多媒体应用（muisc->video->picture）
    private void skipToDefaultApp() {
        LogUtil.i(TAG, "skipToDefaultApp()...");
        if (AppUtils.hasMediaData(Definition.APP.FLAG_MUSIC)) {
            sendMsgSkipApp(FLAG_MUSIC, false);
            return;
        } else if (AppUtils.hasMediaData(Definition.APP.FLAG_VIDEO)) {
            sendMsgSkipApp(FLAG_VIDEO, false);
            return;
        } else if (AppUtils.hasMediaData(Definition.APP.FLAG_PICTURE)) {
            sendMsgSkipApp(FLAG_PICTURE, false);
            return;
        }
        sendMsgRefreshDeviceDialogState(STATE_LOAD_NO_MEDIA, true);
        LogUtil.i(TAG, "skipToDefaultApp() fail...");
    }

    /**
     * USB扫描状态管理内部类
     */
    final class StateManager {
        public static final int STATE_UNINITIALIZED = -1000;
        public static final int STATE_LOADING = 1;
        public static final int STATE_LOAD_FINISHED = 2;
        public static final int STATE_RESET = STATE_UNINITIALIZED;

        private int mLoadState = STATE_UNINITIALIZED;

        public int getmLoadState() {
            return mLoadState;
        }

        public void setmLoadState(int mLoadState) {
            this.mLoadState = mLoadState;
        }
    }

    private StrategyManager.OnStategyStateListener mOnStategyStateListener = new StrategyManager.OnStategyStateListener() {
        public void onNotifyStategyEvent(int eventCode) {
            LogUtil.i(TAG, "onNotifyStategyEvent...." + eventCode);

            switch (eventCode) {
                case StrategyManager.EVENT_IFLYTEK_VOICE:
                    if (StrategyManager.getInstance().getIFlytekVoiceState()) {
                        sendMsgDismissDeviceDialog(0);
                        StrategyManager.getInstance().setHighPriorityAppContinityEffctState(true);
                    }
                    break;
                case StrategyManager.EVENT_SCREEN_SAVER:
                    if (StrategyManager.getInstance().isScreensaverMode()) {
                        sendMsgDismissDeviceDialog(0);
                        StrategyManager.getInstance().setHighPriorityAppContinityEffctState(true);
                    }
                    break;
                case StrategyManager.EVENT_BT_CALL:
                    if (StrategyManager.getInstance().getBTCallState()) {
                        sendMsgDismissDeviceDialog(0);
                        StrategyManager.getInstance().setHighPriorityAppContinityEffctState(true);
                    }
                    break;
                case StrategyManager.EVENT_UPDATE:
                    if (StrategyManager.getInstance().getUpdateState()) {
                        sendMsgDismissDeviceDialog(0);
                        StrategyManager.getInstance().setHighPriorityAppContinityEffctState(true);
                    }
                    break;
                case StrategyManager.EVENT_AVM:
                    if (StrategyManager.getInstance().getAVMState()) {
                        sendMsgDismissDeviceDialog(0);
                        StrategyManager.getInstance().setHighPriorityAppContinityEffctState(true);
                    }
                    break;
                case StrategyManager.EVENT_COLSE_SCRREN:
                    if (StrategyManager.getInstance().getmColseScreenState()) {
                        sendMsgDismissDeviceDialog(0);
                        StrategyManager.getInstance().setHighPriorityAppContinityEffctState(true);
                    }
                    break;
            }
        }
    };


}
