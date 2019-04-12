package com.usbscandemo.usb;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

public class MediaScannerManager implements IMediaScannerManager {
    private static final String TAG = "MediaScannerManager";
    private OnUSBScanStateListener mOnUSBScanStateListener;
    private static MediaScannerManager _INSTANCE;
    private String mScanFirstMusicUrlOfUsb1 = null;
    private String mScanFirstMusicUrlOfUsb2 = null;
    private String mScanFirstVideoUrlOfUsb1 = null;
    private String mScanFirstVideoUrlOfUsb2 = null;
    private String mScanFirstPhotoUrlOfUsb1 = null;
    private String mScanFirstPhotoUrlOfUsb2 = null;

    private EngineScannerRunnable
            mEngineScannerRunnableByUsb1,
            mEngineScannerRunnableByUsb2;

    private String
            mUsbPath1 = "/storage/udisk/",
            mUsbPath2 = "/storage/udisk2/";
    private final int
            FLAG_USB1 = 1,
            FLAG_USB2 = 2;

    private MediaScannerManager() {
        initThread();
    }

    public static MediaScannerManager getInstance() {
        if (null == _INSTANCE) {
            _INSTANCE = new MediaScannerManager();
        }
        return _INSTANCE;
    }

    @Override
    public void registerOnUSBScanStateListener(OnUSBScanStateListener l) {
        this.mOnUSBScanStateListener = l;
    }

    void notifyUSBScanStart(int usbFlag) {
        if (null != mOnUSBScanStateListener) {
            mOnUSBScanStateListener.onScanStart(usbFlag);
        }
    }

    void notifyUSBScanning(int usbFlag) {
        if (null != mOnUSBScanStateListener) {
            mOnUSBScanStateListener.onScanning(usbFlag);
        }
    }

    void notifyUSBScanDone(int usbFlag) {
        if (null != mOnUSBScanStateListener) {
            mOnUSBScanStateListener.onScanDone(usbFlag);
        }
    }

    void notifyUSBScanStoped(int usbFlag) {
        if (null != mOnUSBScanStateListener) {
            mOnUSBScanStateListener.onScanStoped(usbFlag);
        }
    }

    @Override
    public void onUSBMounted(String usbPath) {
        // TO DO
        Log.i(TAG, "onUSBMounted() ..." + usbPath);
        removeScanFirstMediaUrl(usbPath);
        startEngineScannerRunnable(usbPath);
    }

    @Override
    public void onUSBUnMounted(String usbPath) {
        // TO DO
        Log.i(TAG, "onUSBUnMounted() ..." + usbPath);
        removeScanFirstMediaUrl(usbPath);
        stopEngineScannerRunnable(usbPath);
    }


    @Override
    public String getScanFirstMusicUrl(int usbFlag) {
        switch (usbFlag) {
            case FLAG_USB1:
                return this.mScanFirstMusicUrlOfUsb1;
            case FLAG_USB2:
                return this.mScanFirstMusicUrlOfUsb2;
        }
        return null;
    }

    @Override
    public String getScanFirstVideoUrl(int usbFlag) {
        switch (usbFlag) {
            case FLAG_USB1:
                return this.mScanFirstVideoUrlOfUsb1;
            case FLAG_USB2:
                return this.mScanFirstVideoUrlOfUsb2;
        }
        return null;
    }

    @Override
    public String getScanFirstPhotoUrl(int usbFlag) {
        switch (usbFlag) {
            case FLAG_USB1:
                return this.mScanFirstPhotoUrlOfUsb1;
            case FLAG_USB2:
                return this.mScanFirstPhotoUrlOfUsb2;
        }
        return null;
    }

    // U盘路径转换U盘标识
    private int conversionUsbPathToUsbFlag(String usbPath) {
        if (usbPath.endsWith(mUsbPath1)) {
            return FLAG_USB1;
        } else if (usbPath.endsWith(mUsbPath2)) {
            return FLAG_USB2;
        }
        return -1;
    }


    // 多媒体扫描线程
    private static final class EngineScannerRunnable implements Runnable {
        private FuncScanFileTree mFuncScanFileTree;
        private MediaScannerManager mMediaScannerManager;
        private String mScanPath;
        private int
                mMusicFileCount = 0,
                mVideoFileCount = 0,
                mPhotoFileCount = 0;
        private int mUsbFlag = -1;// usb标识

        EngineScannerRunnable(MediaScannerManager mgr) {
            this.mMediaScannerManager = mgr;
        }


        public EngineScannerRunnable setScanPath(String scanPath) {
            this.mScanPath = scanPath;
            if (null != mMediaScannerManager) {
                this.mUsbFlag = mMediaScannerManager.conversionUsbPathToUsbFlag(scanPath);
            }
            return this;
        }

        public EngineScannerRunnable stop() {
            if (null != mFuncScanFileTree) {
                this.mFuncScanFileTree.stop();
            }
            return this;
        }

        public EngineScannerRunnable prepare() {
            if (null != mFuncScanFileTree) {
                mFuncScanFileTree.prepare();
            }
            return this;
        }

        public boolean isRunning() {
            if (null != mFuncScanFileTree) {
                return mFuncScanFileTree.isRunning();
            }
            return false;
        }


        @Override
        public void run() {

            this.mFuncScanFileTree = new FuncScanFileTree();
            this.mFuncScanFileTree.reqestScanUsb(mOnFileTreeScanListener, mScanPath);

        }// end ---run();

        private FuncScanFileTree.OnFileTreeScanListener mOnFileTreeScanListener = new FuncScanFileTree.OnFileTreeScanListener() {
            @Override
            public void onScanFileTreeStart() {
                Log.i(TAG, "onScanFileTreeStart() ...mMusicFileCount = " + mMusicFileCount);
                Log.i(TAG, "onScanFileTreeStart() ...mVideoFileCount = " + mVideoFileCount);
                Log.i(TAG, "onScanFileTreeStart() ...mPhotoFileCount = " + mPhotoFileCount);
                mMediaScannerManager.notifyUSBScanStart(mUsbFlag);
            }

            @Override
            public void onScanFileTreeResult(ConstantsMediaSuffix.MediaSuffixType type, String fileUri) {
                mMediaScannerManager.notifyUSBScanning(mUsbFlag);
                switch (type) {
                    case SUFFIX_TYPE_AUDIO:
                        mMusicFileCount += 1;
                        break;
                    case SUFFIX_TYPE_VIDEO:
                        mVideoFileCount += 1;
                        break;
                    case SUFFIX_TYPE_PHOTO:
                        mPhotoFileCount += 1;
                        break;

                }
            }

            @Override
            public void onScanFileStoped() {
                Log.i(TAG, "onScanFileStoped() ...mMusicFileCount = " + mMusicFileCount);
                Log.i(TAG, "onScanFileStoped() ...mVideoFileCount = " + mVideoFileCount);
                Log.i(TAG, "onScanFileStoped() ...mPhotoFileCount = " + mPhotoFileCount);
                mMusicFileCount = mVideoFileCount = mPhotoFileCount = 0;
                mMediaScannerManager.notifyUSBScanStoped(mUsbFlag);
            }

            @Override
            public void onScanFileDone() {
                Log.i(TAG, "onScanFileDone() ...mMusicFileCount = " + mMusicFileCount);
                Log.i(TAG, "onScanFileDone() ...mVideoFileCount = " + mVideoFileCount);
                Log.i(TAG, "onScanFileDone() ...mPhotoFileCount = " + mPhotoFileCount);
                mMusicFileCount = mVideoFileCount = mPhotoFileCount = 0;
                mMediaScannerManager.notifyUSBScanDone(mUsbFlag);
            }
        };
    }// end ----inner class : EngineScannerRunnable

    // 启动扫描线程
    private void startEngineScannerRunnable(String scanPath) {
        if (null == scanPath) {
            return;
        }
        switch (conversionUsbPathToUsbFlag(scanPath)) {
            case FLAG_USB1:
                if (null == mEngineScannerRunnableByUsb1) {
                    // 实例化USB1扫描线程
                    this.mEngineScannerRunnableByUsb1 = new EngineScannerRunnable(this);
                }
                if (mEngineScannerRunnableByUsb1.isRunning()) {
                    /**
                     * 1.停止USB1扫描线程
                     * 2.移除USB1扫描线程
                     * 3.重新创建一个USB1扫描线程
                     */
                    this.mEngineScannerRunnableByUsb1.stop();
                    this.mBackgroundHandler.removeCallbacks(mEngineScannerRunnableByUsb1);
                    this.mEngineScannerRunnableByUsb1 = new EngineScannerRunnable(this);
                }
                /**
                 * 1.USB1扫描线程准备
                 * 2.设置扫描线程要扫描的USB1路径
                 * 3.启动USB1线程
                 */
                this.mEngineScannerRunnableByUsb1.prepare();
                this.mEngineScannerRunnableByUsb1.setScanPath(scanPath);
                this.mBackgroundHandler.post(mEngineScannerRunnableByUsb1);
                break;
            case FLAG_USB2:
                if (null == mEngineScannerRunnableByUsb2) {
                    // 实例化USB2扫描线程
                    this.mEngineScannerRunnableByUsb2 = new EngineScannerRunnable(this);
                }
                if (mEngineScannerRunnableByUsb2.isRunning()) {
                    /**
                     * 1.停止USB2扫描线程
                     * 2.移除USB2扫描线程
                     * 3.重新创建一个USB2扫描线程
                     */
                    this.mEngineScannerRunnableByUsb2.stop();
                    this.mBackgroundHandler.removeCallbacks(mEngineScannerRunnableByUsb2);
                    this.mEngineScannerRunnableByUsb2 = new EngineScannerRunnable(this);
                }
                /**
                 * 1.USB2扫描线程准备
                 * 2.设置扫描线程要扫描的USB2路径
                 * 3.启动USB2线程
                 */
                this.mEngineScannerRunnableByUsb2.prepare();
                this.mEngineScannerRunnableByUsb2.setScanPath(scanPath);
                this.mBackgroundHandler.post(mEngineScannerRunnableByUsb2);
                break;
        }


    }

    // 停止扫描线程
    private void stopEngineScannerRunnable(String scanPath) {
        Log.i(TAG, "stopEngineScannerRunnable() ..." + scanPath);
        switch (conversionUsbPathToUsbFlag(scanPath)) {
            case FLAG_USB1:
                if (null != mEngineScannerRunnableByUsb1) {
                    this.mEngineScannerRunnableByUsb1.stop();
                    this.mBackgroundHandler.removeCallbacks(mEngineScannerRunnableByUsb1);
                }
                break;
            case FLAG_USB2:
                if (null != mEngineScannerRunnableByUsb2) {
                    this.mEngineScannerRunnableByUsb2.stop();
                    this.mBackgroundHandler.removeCallbacks(mEngineScannerRunnableByUsb2);
                }
                break;
        }
    }

    // 初始化后台线程
    private Handler mBackgroundHandler;

    private void initThread() {
        Log.i(TAG, "initThread() ...");
        HandlerThread mHandlerThread = new HandlerThread("MediaScanTHread");
        mHandlerThread.start();
        this.mBackgroundHandler = new Handler(mHandlerThread.getLooper());
    }

    private void removeScanFirstMediaUrl(String usbPath) {
        Log.i(TAG, "removeScanFirstMediaUrl() ..." + usbPath);
        /**
         * 1.删除偏好记录首个媒体URL
         * 2.删除缓存里首个媒体URL
         */
        switch (conversionUsbPathToUsbFlag(usbPath)) {
            case FLAG_USB1:
                this.mScanFirstMusicUrlOfUsb1 = null;
                this.mScanFirstVideoUrlOfUsb1 = null;
                this.mScanFirstPhotoUrlOfUsb1 = null;
                break;
            case FLAG_USB2:
                this.mScanFirstMusicUrlOfUsb2 = null;
                this.mScanFirstVideoUrlOfUsb2 = null;
                this.mScanFirstPhotoUrlOfUsb2 = null;
                break;
        }
    }
}
