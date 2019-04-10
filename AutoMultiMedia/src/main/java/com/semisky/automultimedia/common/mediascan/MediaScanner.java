package com.semisky.automultimedia.common.mediascan;

import android.content.ContentValues;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.semisky.automultimedia.MediaApplication;
import com.semisky.automultimedia.R;
import com.semisky.automultimedia.common.constant.Definition;
import com.semisky.automultimedia.common.sql.MediaDBManager;
import com.semisky.automultimedia.common.sql.SharePreferenceUtil;
import com.semisky.automultimedia.common.utils.LogUtil;
import com.semisky.automultimedia.common.utils.USBManager;
import com.semisky.automultimedia.common.utils.UsbCheckUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 熊俊 on 2018/1/2.
 */

public class MediaScanner {

    public static final String TAG = "MediaScanner";
    private static MediaScanner instance;
    private Context mContext;
    private String[] image_suffix;// 图片文件后缀数组
    private String[] music_suffix;// 音频文件后缀数组
    private String[] video_suffix;// 视频文件后缀数组
    private String[] audio_and_video_suffix;// 即是音频也是视频文件后缀的
    private volatile boolean mUSBMountState = true;

    private List<String> mMusicUrlsByUsb;
    private List<String> mVideoUrlsByUsb;
    private List<String> mPictureUrlsByUsb;

    private ScannerRunnable mScannerRunnable = null;
    private DbRunnable mDbRunnable = null;


    private MediaScanner() {
        mScannerRunnable = new ScannerRunnable();
        mDbRunnable = new DbRunnable();

        initMediaScannerThread();
        this.mContext = MediaApplication.context;
        image_suffix = mContext.getResources().getStringArray(R.array.image_suffix);
        music_suffix = mContext.getResources().getStringArray(R.array.music_suffix);
        video_suffix = mContext.getResources().getStringArray(R.array.video_suffix);
        audio_and_video_suffix = mContext.getResources().getStringArray(R.array.audio_and_video_suffix);
        mMusicUrlsByUsb = new ArrayList<String>();
        mVideoUrlsByUsb = new ArrayList<String>();
        mPictureUrlsByUsb = new ArrayList<String>();
    }

    public static MediaScanner getInstance() {
        if (instance == null) {
            synchronized (MediaScanner.class) {
                if (instance == null) {
                    instance = new MediaScanner();
                }
            }
        }
        return instance;
    }


    private class MediaScannerThread extends HandlerThread implements Handler.Callback {
        MediaScannerThread(String name) {
            super(name);
        }

        @Override
        public boolean handleMessage(Message msg) {

            return false;
        }
    }

    private Handler mBackgroundHandler = null;
    private MediaScannerThread mMediaScannerThread = null;

    private void initMediaScannerThread() {
        LogUtil.i(TAG, "===============initMediaScannerThread()===================");
        if (null == mMediaScannerThread) {
            mMediaScannerThread = new MediaScannerThread("MediaScannerThread");
            mMediaScannerThread.start();
        }
        if (null == mBackgroundHandler) {
            mBackgroundHandler = new Handler(mMediaScannerThread.getLooper(), mMediaScannerThread);
        }
    }

    // 启动扫描媒体线程
    private void startScanMediaFileRunnable(String usbPath) {
        LogUtil.i(TAG, "===============startScanMediaFileRunnable()===================");
        mScannerRunnable.setUsbPath(usbPath);
        mBackgroundHandler.removeCallbacks(mScannerRunnable);
        mBackgroundHandler.post(mScannerRunnable);
    }

    // 启动解析媒体线程
    private void startParserMediaFileRunnable() {
        LogUtil.i(TAG, "===============startParserMediaFileRunnable()===================");
        mBackgroundHandler.removeCallbacks(mDbRunnable);
        mBackgroundHandler.post(mDbRunnable);
    }

    /**
     * 扫描指定Usb媒体文件
     */
    public void startScanMediaFileForUsb(String usbPath) {
        LogUtil.d(": -------startScanMediaFileForUsb() usbFlag: " + usbPath);

        if (!UsbCheckUtil.checkUsb(usbPath)) {
            return;
        }
        notifyClearAllMediaDataByUsb();
        startScanMediaFileRunnable(usbPath);
    }

    public void notifyClearAllMediaDataByUsb() {
        mMusicUrlsByUsb.clear();
        mVideoUrlsByUsb.clear();
        mPictureUrlsByUsb.clear();
    }


    /**
     * 扫描媒体文件线程
     */
    private class ScannerRunnable implements Runnable {
        private String usbPath;
        private boolean mIsRunning;

        public ScannerRunnable() {

        }

        public ScannerRunnable(String usbPath) {
            this.usbPath = usbPath;
        }

        public void setUsbPath(String path) {
            this.usbPath = path;
        }

        public boolean isRunning() {// 当前线程运行状态
            return mIsRunning;
        }

        @Override
        public void run() {
            LogUtil.d(TAG, "=======USB SCANN RUNNING !!!");

            mIsRunning = true;// 线程正在运行
            scanMediaFile(new File(usbPath));// 调用递归函数扫描媒体文件
            mIsRunning = false;// 线程结束

            LogUtil.d(TAG, "**********ScannerRunnable start*****************\n"
                    + "getMusicUrlsByUsbSize: " + mMusicUrlsByUsb.size() + "\n"
                    + "getPictureUrlsByUsbSize: " + mPictureUrlsByUsb.size() + "\n"
                    + "getVideoUrlsByUsbSize: " + mVideoUrlsByUsb.size() + "\n"
                    + "**********ScannerRunnable end*****************\n");
            LogUtil.d(TAG, "=======USB SCANNER DONE!!! ");

            // 启动解析媒体线程
            startParserMediaFileRunnable();
        }

    }

    /**
     * 写入数据库线程
     */
    private class DbRunnable implements Runnable {

        @Override
        public void run() {
            LogUtil.d(TAG, "=======USB FILE PARSER RUNNING !!!");
            parserMusicFileAndInsertToDatabase();
            parserPictureFileAndInsertToDatabase();
            parserVideoFileAndInsertToDatabase();
            LogUtil.d(TAG, "=======USB FILE PARSER DONE !!!");

            //写入sp中是否有音乐/图片/视频
            SharePreferenceUtil.insertmusicSize(mContext, mMusicUrlsByUsb.size());
            SharePreferenceUtil.insertPictureSize(mContext, mPictureUrlsByUsb.size());
            SharePreferenceUtil.insertVedioSize(mContext, mVideoUrlsByUsb.size());

            if (mUSBMountState) {
                for (OnUsbScannerListener listener : onUsbScannerListeners) {
                    listener.onUsbScanned(mMusicUrlsByUsb.size(), mPictureUrlsByUsb.size(), mVideoUrlsByUsb.size());
                }
            }
        }
    }


    /**
     * 扫描多媒体文件（递归）
     */
    void scanMediaFile(File targetFile) {
        excuteImmediatelyStop();//如果usb unmount 时，立即停止程序

        if (targetFile == null || !targetFile.exists()) {
            return;
        }

        if (targetFile.isDirectory()) {// 如果是文件夹
            File[] files = targetFile.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    excuteImmediatelyStop();
                    if (!isStopScannFolderName(file.getName())) {
                        scanMediaFile(file);// 递归扫描
                    }
                }
            }
        } else {// 如果是文件
            String fileUrl = targetFile.getAbsolutePath();// 文件路径

            if (check(fileUrl, audio_and_video_suffix)) {// 即是音频也是视频文件后缀的
                mMusicUrlsByUsb.add(fileUrl);
                mVideoUrlsByUsb.add(fileUrl);
            }

            if (check(fileUrl, image_suffix)) {// 如果是图片文件
                mPictureUrlsByUsb.add(fileUrl);
                return;
            }

            if (check(fileUrl, music_suffix)) {// 如果是音频文件
                mMusicUrlsByUsb.add(fileUrl);
                return;
            }

            if (check(fileUrl, video_suffix)) {// 如果是视频文件
                mVideoUrlsByUsb.add(fileUrl);
                return;
            }
        }
    }

    private void excuteImmediatelyStop() {
        if (!mUSBMountState)
            return;
    }

    /**
     * 通过文件名判断是什么类型的文件.
     */
    boolean check(final String name, final String[] extensions) {
        for (String end : extensions) {
            // name永远不会为null,无需异常处理
            if (name.toLowerCase().endsWith(end)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 解析图片并存入数据
     */
    private void parserPictureFileAndInsertToDatabase() {
        LogUtil.d(TAG + ": PictureHandlerThread  ");
        List<String> copyPictureUrlList = new ArrayList<String>();
        copyPictureUrlList.addAll(mPictureUrlsByUsb);// 将Model类中的数据copy到新的集合

        List<ContentValues> tempPictureInfos = new ArrayList<ContentValues>();// 从图片URL解析出的信息存储临时集合中[目地:方便批量插入操作]
        int mPictureUrlsSize = copyPictureUrlList.size();// 图片集合长度
        int tempLength = 0;// 记录遍历临时存储图片URL集合次数

        LogUtil.d("@@@@@@-PICTURE\n" + TAG + ": parserPictureFileAndInsertToDatabase() " + "\n"
                + TAG + ": parserPictureFileAndInsertToDatabase() copyPictureUrlList len: " + copyPictureUrlList.size()
                + "\n" + TAG + ": parserPictureFileAndInsertToDatabase() mSaveLastPictureUrlListSizeByUsb: "
                + "\n" + TAG + ": getMountStateByUsb(): " + mUSBMountState + "\n" + "-------");
        if (mPictureUrlsSize > 0) {

            for (String filePath : copyPictureUrlList) {
                if (!isUSBMounted()) {// 文件未扫描完成，突然拔出U盘，停止函数执行，并reset相关变量
                    tempLength = 0;
                    copyPictureUrlList.clear();
                    mPictureUrlsByUsb.clear();
                    tempPictureInfos.clear();
                    LogUtil.d(TAG
                            + ":------------parserPictureFileAndInsertToDatabase()----usbFlag: "
                            + ", stop usb !!!");
                    return;
                }
                tempLength += 1;
                ContentValues values = MediaParaser.getInstance().parserPictureInfo(filePath);
                if (values != null) {
                    tempPictureInfos.add(values);
                    // LogUtil.d(TAG+"PictureHandlerThread usbFlag: "+usbFlag+" , tempPictureInfos len: "+tempPictureInfos.size());
                }

                if (tempLength % 10 == 0 || tempLength == mPictureUrlsSize) {// 每十条数据插入一次数据库
                    // 图片信息插入数据库
                    MediaDBManager.getInstance(mContext).insertPictureInfoToDatabase(tempPictureInfos);
                    tempPictureInfos.clear();// 图片集合信息已存储到数据库,清除临时集合中的图片信息
                }
            }
        }

    }


    /**
     * 解析音乐文件并存入数据库
     */
    private void parserMusicFileAndInsertToDatabase() {

        List<String> copyMusicUrlsToNewList = new ArrayList<String>();
        copyMusicUrlsToNewList.addAll(mMusicUrlsByUsb);

        List<ContentValues> musicInfos = new ArrayList<ContentValues>();// 临时存储已解析的音乐信息
        int tempLength = 0;// 记录遍历音乐URL集合,URL个数。

        LogUtil.d("@@@@@@-MUISC\n" + TAG + ": parserMusicFileAndInsertToDatabase() " + "\n" + TAG
                + ": parserMusicFileAndInsertToDatabase() copyMusicUrlsToNewList len: " + copyMusicUrlsToNewList.size()
                + "\n" + TAG + ": mSaveLastMusicUrlListSizeByUsb: " + copyMusicUrlsToNewList.size() + "\n"
                + TAG + ": getMountStateByUsb(): " + mUSBMountState + "\n" + "-------");

        if (copyMusicUrlsToNewList.size() > 0) {

            for (String filePath : copyMusicUrlsToNewList) {
                if (!isUSBMounted()) {// usb拔出未扫描完成时，强制停止
                    tempLength = 0;
                    copyMusicUrlsToNewList.clear();
                    mMusicUrlsByUsb.clear();
                    musicInfos.clear();
                    LogUtil.d(TAG + ": --------------STOP parserMusicFileAndInsertToDatabase(), stop usb !!!");
                    return;
                }

                tempLength += 1;
                // 解析音乐Url
                ContentValues values = MediaParaser.getInstance().parserMusic(filePath);
                if (values != null) {
                    musicInfos.add(values);// 将解析完成的音乐信息暂存到临时集合里
                } else {
                    LogUtil.e(TAG + "parserMusicFileAndInsertToDatabase()" + ", parser fail !!! ,filePath: " + filePath);
                }

                if ((tempLength % 10) == 0 || tempLength == copyMusicUrlsToNewList.size()) {// 解析10个URL文件批量插入一下数据库，最后不满10
                    LogUtil.d(TAG + ": parserMusicFileAndInsertToDatabase()" + ", ----------insertMusicInfoToDatabase()");
                    long result = MediaDBManager.getInstance(mContext).insertMusicInfoToDatabase(musicInfos);
                    LogUtil.d(TAG + ": parserMusicFileAndInsertToDatabase(): " + ", ----------insertMusicInfoToDatabase()-------SUCCESS  !!!!! ");
                    musicInfos.clear();
                }
            }
        }

    }


    /**
     * 解析视频文件并存入数据
     */
    private void parserVideoFileAndInsertToDatabase() {
        LogUtil.d(TAG + ": parserVideoFileAndInsertToDatabase() ");

        List<String> copyVideoUrlList = new ArrayList<String>();
        copyVideoUrlList.addAll(mVideoUrlsByUsb);

        List<ContentValues> tempVideoInfos = new ArrayList<ContentValues>();// 从视频URL解析出的信息存储临时集合中[目地:方便批量插入操作]
        int mVideoUrlsSize = copyVideoUrlList.size();// 视频集合长度
        int tempLength = 0;// 记录遍历临时存储视频URL集合次数

        LogUtil.d("@@@@@@-VIDEO\n" + TAG + ": parserVideoFileAndInsertToDatabase() " + "\n" + TAG
                + ": parserVideoFileAndInsertToDatabase() copyVideoUrlList len: " + copyVideoUrlList.size() + "\n"
                + TAG + ": parserVideoFileAndInsertToDatabase() mSaveLastVideoUrlListSizeByUsb: "
                + copyVideoUrlList.size() + "\n" + TAG + ": getMountStateByUsb(): " + mUSBMountState + "\n" + "-------");

        if (mVideoUrlsSize > 0) {

            for (String filePath : copyVideoUrlList) {
                if (!isUSBMounted()) {// 文件未扫描完成，突然拔出U盘，停止函数执行，并reset相关变量
                    tempLength = 0;
                    copyVideoUrlList.clear();
                    mVideoUrlsByUsb.clear();
                    tempVideoInfos.clear();
                    LogUtil.d(TAG + ": ---------------parserVideoFileAndInsertToDatabase()----usbFlag: " + ", stop usb !!!");
                    return;
                }

                // LogUtil.d(TAG+": parserVideoFileAndInsertToDatabase() filePath: "+filePath);
                tempLength += 1;
                ContentValues values = MediaParaser.getInstance().parserVideoInfo(filePath);
                if (values != null) {
                    tempVideoInfos.add(values);
                    // LogUtil.d(TAG+": parserVideoFileAndInsertToDatabase() tempVideoInfos len: "+tempVideoInfos.size());
                }
                if (tempLength % 10 == 0 || tempLength == copyVideoUrlList.size()) {// 每十条数据插入一次数据库
                    // 视频信息插入数据库
                    MediaDBManager.getInstance(mContext).insertVideoInfoToDatabase(tempVideoInfos);
                    tempVideoInfos.clear();// 图片集合信息已存储到数据库,清除临时集合中的图片信息
                }

            }
        }

    }


    // 设置usb挂载状态
    public void setUSBMounted(boolean isMounted) {
        this.mUSBMountState = isMounted;
    }

    // 主动获取USBusb挂载状态
    public boolean isUSBMounted() {
        return USBManager.getInstance().isUsbMounted(Definition.USB_PATH);
    }


    private List<OnUsbScannerListener> onUsbScannerListeners = new ArrayList<OnUsbScannerListener>();

    /**
     * U盘状态变化监听器
     */
    public interface OnUsbScannerListener {
        void onUsbScanned(int musicSize, int pictureSize, int vedioSize);

    }

    /**
     * 注册扫描状态监听
     */
    public void registerOnUsbScannerListener(OnUsbScannerListener listener) {
        if (listener != null && onUsbScannerListeners != null && !onUsbScannerListeners.contains(listener)) {
            onUsbScannerListeners.add(listener);
        }
    }

    /**
     * 反注册扫描状态监听
     */
    public void unRegisterOnUsbScannerListener(OnUsbScannerListener listener) {
        if (onUsbScannerListeners != null && listener != null && onUsbScannerListeners.contains(listener)) {
            onUsbScannerListeners.remove(listener);
        }
    }

    private static String[] STOP_SCANN_FORLDER_NAME = {
            "lost.dir",
            "abi",
            "art",
            "bionic",
            "bootable",
            "docs",
            "external",
            "libcore",
            "ndk",
            "packages",
            "prebuilts",
            "sdk",
            "system",
            "vendor",
            "src",
            "System Volume Information"};

    /**
     * 是否为禁止策略
     *
     * @return
     */
    private boolean isStopScannFolderName(String dir) {

        for (String name : STOP_SCANN_FORLDER_NAME) {
            if (name.toLowerCase().equals(dir.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

}
