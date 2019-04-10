package com.semisky.automultimedia.common.utils;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;

import com.semisky.automultimedia.MediaApplication;
import com.semisky.automultimedia.common.constant.Definition;
import com.semisky.automultimedia.common.interfaces.ISetWallpaperListener;
import com.semisky.automultimedia.common.sql.SharePreferenceUtil;
import com.semisky.autoservice.manager.AutoManager;
import com.semisky.autoservice.manager.CarCtrlManager;
import com.semisky.autoservice.manager.ICMManager;

import java.io.File;
import java.io.IOException;

import static com.semisky.autoservice.manager.AudioManager.STREAM_ANDROID;
import static com.semisky.autoservice.manager.AudioManager.STREAM_ANDROID_VIDEO;

/**
 * Created by liuyong on 18-2-26.
 */

public class AppUtils {
    private static final String TAG = AppUtils.class.getSimpleName();

    /**
     * 关闭酷我和喜马拉雅APP
     */
    public static void finishOtherApp() {
        String voiceServiceAction = "com.semisky.voice";
        String otherExtraName = "FINISH_OTHER_APP";
        int otherValue = 1;
        Intent mIntent = new Intent();
        mIntent.setAction(voiceServiceAction);
        mIntent.putExtra(otherExtraName, otherValue);
        MediaApplication.getContext().startService(mIntent);
    }

    /**
     * 音乐服务状态广播
     * @param state
     */
    public static void sendBroadcastByMusicServiceStateChange(boolean state){
        LogUtil.i(TAG,"sendBroadcastByMusicServiceStateChange()..." + state);
        Intent i = new Intent();
        i.setAction(Definition.ACTION_MUSIC_SERVICE_STATE_CHANGE);
        i.putExtra(Definition.KEY_MUSIC_SERVICE_STATE,state);
        MediaApplication.getContext().sendBroadcast(i);
    }

    /**
     * 强制关闭U盘连接弹窗
     */
    public static void closeUsbConnectDialog(){
        LogUtil.i(TAG,"closeUsbConnectDialog()...");
        Intent intent = new Intent();
        intent.setClassName(Definition.USB_MOUNT_SERVICE_PKG,Definition.USB_MOUNT_SERVICE_CLZ);
        intent.setAction(Definition.ACTION_COLSE_USB_CONNECT_DIALOG);
        MediaApplication.getContext().startService(intent);
    }
    /**
     * 获取文件最后创建的时间
     * @param path
     * @return
     */
    public static long getFileLastModifiedTime(String path) {
        if (null != path) {
            File f = new File(path);
            if (f.exists()) {
                return f.lastModified();
            }
        }
        return -1;
    }

    /**
     * 是否导航在前台
     *
     * @return
     */
    public static boolean isNaviAtForeground() {
        boolean isNaviAtForeground = AutoManager.getInstance().getNaviStatus();
        LogUtil.i(TAG, "isNaviAtForeground=" + isNaviAtForeground);
        return isNaviAtForeground;
    }

    public static void setStopMediaPlayStopState(boolean state) {
        LogUtil.i(TAG, "setStopMediaPlayStopState() state=" + state);
        AutoManager.getInstance().setStopMediaPlay(state);
    }

    /**
     * 是否禁止媒体播放操作
     *
     * @return
     */
    public static boolean isStopMediaPlay() {
        boolean isStopMediaPlay = AutoManager.getInstance().ShouldStopMediaPlay();
        LogUtil.i(TAG, "isStopMediaPlay=" + isStopMediaPlay);
        return isStopMediaPlay;
    }

    /**
     * 启动指定界面
     *
     * @param clz
     */
    public static void startActivityByFlag(Class clz) {
        LogUtil.i(TAG, "startActivityByFlag() = " + clz.getSimpleName());
        Intent i = new Intent();
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setClass(MediaApplication.getContext(), clz);
        MediaApplication.getContext().startActivity(i);
    }
    /**
     * 启动指定界面
     *
     * @param clz
     */
    public static void startActivityByFlag(Class clz,int cmd) {
        LogUtil.i(TAG, "startActivityByFlag() = " + clz.getSimpleName());
        Intent i = new Intent();
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setClass(MediaApplication.getContext(), clz);
        i.putExtra(Definition.PARAM_CMD,cmd);
        MediaApplication.getContext().startActivity(i);
    }

    // 获取当前是否有高优级应用在运行
    public static boolean isHighPriorityAppRunning() {
        boolean isHighPriorityAppRunning = false;
        try {
            isHighPriorityAppRunning = AutoManager.getInstance().isHighPriorityAppRunning();
        } catch (Exception e) {
            LogUtil.e(TAG, "ERROR METHOD : isHighPriorityAppRunning()");
        }
        LogUtil.i(TAG, "isHighPriorityAppRunning() =\t" + isHighPriorityAppRunning);
        return isHighPriorityAppRunning;
    }

    // 设置APP状态到中间件
    public static void setAppStatus(String className, String title, int status) {
        try {
            AutoManager.getInstance().setAppStatus(className, title, status);
        } catch (Exception e) {
            LogUtil.e(TAG, "setAppStatus() fail!!!,error info-:" + e.getMessage());
        }
    }

    // 跳转默认音源Radio (pkg : 应用包名；launchType : BACKGROUND_LAUNCH = 3 ,FOREGROUND_LAUNCH = 2)
    public static void launcherRadioApp(String pkg, int launchType) {
        LogUtil.i(TAG, "launcherRadioApp() pkg=" + pkg + " , launchType=" + launchType);
        AutoManager.getInstance().launchRadio(MediaApplication.getContext(), pkg, launchType);
    }

    // 发送数据加载状态广播
    public static void sendLoadDataStateBroadcast(Context ctx, boolean state) {
        Intent i = new Intent("com.semisky.action.MEDIA_LOAD_STATE_CHANGE");
        i.putExtra("state", state);
        ctx.sendBroadcast(i);
    }

    // 仪表交互：设置当前多媒体播放状态到仪表
    public static void setCurrentPlayStatus(boolean playStatus) {
        ICMManager.getInstance().setCurrentPlayStatus(playStatus);
    }

    // 打开多媒体音频通道
    public static void openAndroidStreamVolume() {
        if (getCurrentAudioType() != STREAM_ANDROID) {
            LogUtil.i(TAG, "openStreamVolume(STREAM_ANDROID)....");
            com.semisky.autoservice.manager.AudioManager.getInstance().openStreamVolume(STREAM_ANDROID);
        }
    }

    // 打开多媒体视频音频通道
    public static void openAndroidStreamVolumeByVideo() {
        if (getCurrentAudioType() != STREAM_ANDROID_VIDEO) {
            LogUtil.i(TAG, "openStreamVolume(STREAM_ANDROID_VIDEO)....");
            com.semisky.autoservice.manager.AudioManager.getInstance().openStreamVolume(STREAM_ANDROID_VIDEO);
        }
    }

    // 获取当前音源
    public static int getCurrentAudioType() {
        int currentAudioType = com.semisky.autoservice.manager.AudioManager.getInstance().getCurrentAudioType();
        LogUtil.i(TAG, "getCurrentAudioType() currentAudioType=" + currentAudioType);
        return currentAudioType;
    }

    // 是否有媒体数据
    public static boolean hasMediaData(int appFlag) {
        switch (appFlag) {
            case Definition.APP.FLAG_MUSIC:
                if (SharePreferenceUtil.getMusicSize(MediaApplication.getContext()) > 0) {
                    return true;
                }
                break;
            case Definition.APP.FLAG_VIDEO:
                if (SharePreferenceUtil.getVedioSize(MediaApplication.getContext()) > 0) {
                    return true;
                }
                break;
            case Definition.APP.FLAG_PICTURE:
                if (SharePreferenceUtil.getPictureSize(MediaApplication.getContext()) > 0) {
                    return true;
                }
                break;
        }
        return false;
    }

    public static boolean hasMediaData() {
        int musicSize = SharePreferenceUtil.getMusicSize(MediaApplication.getContext());
        int videoSize = SharePreferenceUtil.getVedioSize(MediaApplication.getContext());
        int pictureSize = SharePreferenceUtil.getPictureSize(MediaApplication.getContext());
        if (musicSize > 0 || videoSize > 0 || pictureSize > 0) {
            return true;
        }
        return false;
    }

    /**
     * 获取默认App标识
     *
     * @return
     */
    public static int getDefaultAppFlag() {
        if (AppUtils.hasMediaData(Definition.APP.FLAG_MUSIC)) {
            return Definition.APP.FLAG_MUSIC;
        } else if (AppUtils.hasMediaData(Definition.APP.FLAG_VIDEO)) {
            return Definition.APP.FLAG_VIDEO;
        } else if (AppUtils.hasMediaData(Definition.APP.FLAG_PICTURE)) {
            return Definition.APP.FLAG_PICTURE;
        }
        return -1;
    }

    /**
     * 设置当前节目到仪表盘
     *
     * @param name
     */
    public static void setCurrentSourceNameToICM(String name) {
        ICMManager.getInstance().setCurrentSourceName(name);
    }

    /**
     * 正常屏显示
     *
     * @param ctx
     */
    public static void setNormalScreenMode(Activity ctx) {
        ctx.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public static void setFullScreenMode(Activity ctx) {
        ctx.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * 全屏显示
     *
     * @param clzName
     * @return
     */
    public static boolean isTopActivity(@Nullable String clzName) {
        boolean isTopActivity =
                ActivityManagerUtils.getInstance().getTopActivity(MediaApplication.getContext(), clzName, 0, 1);
        LogUtil.i(TAG, "isTopActivity() = " + isTopActivity);
        return isTopActivity;
    }


    /**
     * 设置壁纸到系统
     *
     * @param wm         壁纸管理类
     * @param pictureUrl 图片路径
     * @param listener   设置壁纸监听
     */
    public static void setWallpaper(WallpaperManager wm, @Nullable String pictureUrl, @Nullable ISetWallpaperListener listener) {
        // Bitmap bitmap = BitmapFactory.decodeFile(pictureUrl);
        Bitmap bitmap = getImage(pictureUrl);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width == 1280 && height == 720 && (pictureUrl.endsWith("jpg") || pictureUrl.endsWith("png") || pictureUrl.endsWith("bmp"))) {
            try {
                wm.setBitmap(bitmap);
                FileManager.getInstance().copyFileToLocal(pictureUrl, FileManager.DEF_SD_DIR);
            } catch (IOException e) {
                e.printStackTrace();
            }
            listener.onResult(ISetWallpaperListener.RESULT_CODE_SUCCESS);
        } else {
            listener.onResult(ISetWallpaperListener.RESULT_CODE_FAIL);
        }
    }

    /**
     * 设置背景
     *
     * @param v
     */
    public static void setBackground(View v) {
        Drawable background = MediaApplication.getWallpaperManager().getDrawable();
        v.setBackground(background);
    }

    /**
     * 获取车速
     *
     * @return
     */
    public static int getSpeed() {
        try {
            return CarCtrlManager.getInstance().getOverspeedWarning();
        } catch (Exception e) {
            LogUtil.e(TAG, "getOverspeedWarning() exception !!!");
        }
        return 0;
    }

    /**
     * 获取行车观看视频开关状态
     *
     * @return 0:代表行车禁止观察视频;1:代表行车允许观看视频
     */
    public static int getWatchVideoState() {

        return Settings.System.getInt(MediaApplication.getContext().getContentResolver(), "run_vedio", 0);
    }

    /**
     * 内存溢出，压缩
     *
     * @param url
     * @return
     */
    private static Bitmap getImage(String url) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(url);
            return bitmap;
        } catch (OutOfMemoryError error) {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = 4;
            Bitmap bmp = BitmapFactory.decodeFile(url, opts);
            return bmp;
        }

    }

    public static MediaMetadataRetriever mmr;

    public static int parsingVedioData(String url) {
        if (!new File(url).exists()) {
            return 1;
        }
        // 音视频解析工具对象
        if (mmr == null) {
            mmr = new MediaMetadataRetriever();
        }
        try {
            // 设置解析源
            mmr.setDataSource(url);
            //解析时长
            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            //解析视频宽度
            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            //解析视频高度
            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        } catch (Exception e) {
            return 1;
        }
        return 0;
    }

    public static void closeMediaMetadataRetriever() {
        mmr.release();
        mmr = null;
    }
    public static boolean isFileExist(String path,long time){
        if (path==null){
            return false;
        }
        File file=new File(path);
        if (file.exists()){
            long lastChange=file.lastModified();
            if (lastChange==time){
                return true;
            }
        }
        return false;
    }

}
