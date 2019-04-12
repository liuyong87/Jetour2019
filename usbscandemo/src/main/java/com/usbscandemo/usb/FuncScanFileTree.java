package com.usbscandemo.usb;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FuncScanFileTree {
    public interface OnFileTreeScanListener {
        void onScanFileTreeStart();

        void onScanFileTreeResult(ConstantsMediaSuffix.MediaSuffixType type, String fileUri);

        void onScanFileStoped();

        void onScanFileDone();
    }

    private interface OnFileFilterListener {
        void onChangedFolder(File folder);

        void onChangedFile(String file);
    }

    private OnFileTreeScanListener mOnFileTreeScanListener;
    private List<File> mFirstFolderList = new ArrayList<File>();
    private List<File> mSecondFolderList = new ArrayList<File>();
    private boolean mIsForceStopScan = false;// 是否强制停止扫描媒体文件
    private boolean mIsRunning = false;// 是否在扫描


    void notifyScanFileTreeStart() {
        if (null != mOnFileTreeScanListener) {
            mOnFileTreeScanListener.onScanFileTreeStart();
        }
    }

    void notifyScanFileTreeResult(ConstantsMediaSuffix.MediaSuffixType type, String fileUri) {
        if (null != mOnFileTreeScanListener) {
            mOnFileTreeScanListener.onScanFileTreeResult(type, fileUri);
        }
    }

    void notifyScanFileStoped() {
        if (null != mOnFileTreeScanListener) {
            mOnFileTreeScanListener.onScanFileStoped();
        }
    }

    void notifyScanFileDone() {
        if (null != mOnFileTreeScanListener) {
            mOnFileTreeScanListener.onScanFileDone();
        }
    }

    // 媒体文件是否在扫描工作
    public boolean isRunning() {
        return this.mIsRunning;
    }

    // 停止扫描媒体文件
    public FuncScanFileTree stop() {
        this.mIsForceStopScan = true;
        return this;
    }

    // 准备扫描媒体文件
    public FuncScanFileTree prepare() {
        this.mIsForceStopScan = false;
        return this;
    }

    /**
     * 请求扫描媒体文件
     */
    public void reqestScanUsb(OnFileTreeScanListener l, String url) {
        this.mOnFileTreeScanListener = l;
        // 通知媒体文件扫描开始
        notifyScanFileTreeStart();
        scanFile(new File(url));
        this.mIsRunning = false;

        if (mIsForceStopScan) {
            notifyScanFileStoped();// 强制停止通知
        } else {
            notifyScanFileDone();// 媒体文件扫描完成通知
        }
        resetCacheData();

    }


    private void scanFile(File targetFile) {

        if (targetFile.isDirectory()) {

            reqScanFile(new OnFileFilterListener() {
                @Override
                public void onChangedFolder(File folder) {
                    if (!mIsForceStopScan) {
                        mFirstFolderList.add(folder);
                    }
                }

                @Override
                public void onChangedFile(String filePath) {
                    // TO DO
                    if (!mIsForceStopScan) {
                        handlerSortMediaFile(filePath);
                    }
                }
            }, targetFile);

            while (!mFirstFolderList.isEmpty() || !mSecondFolderList.isEmpty()) {

                Log.i("test", "mIsForceStopScan = " + mIsForceStopScan);
                Log.i("test", "mFirstFolderList.size = " + mFirstFolderList.size());
                Log.i("test", "mSecondFolderList.size = " + mSecondFolderList.size());
                if (mIsForceStopScan) {
                    mFirstFolderList.clear();
                    mSecondFolderList.clear();
                    // notify msg
                    notifyScanFileStoped();
                    return;
                }
                // 遍历第一个媒体文件夹集合
                for (int i = 0; i < mFirstFolderList.size(); i++) {
                    if (mIsForceStopScan) {
                        return;
                    }
                    reqScanFile(new OnFileFilterListener() {
                        @Override
                        public void onChangedFolder(File folder) {
                            if (!mIsForceStopScan) {
                                mSecondFolderList.add(folder);
                            }
                        }

                        @Override
                        public void onChangedFile(String filePath) {
                            // TO DO
                            if (!mIsForceStopScan) {
                                handlerSortMediaFile(filePath);
                            }
                        }
                    }, mFirstFolderList.get(i));
                }
                mFirstFolderList.clear();// 清除第一个文件夹集合

                // 遍历第二个媒体文件夹集合
                for (int j = 0; j < mSecondFolderList.size(); j++) {
                    if (mIsForceStopScan) {
                        return;
                    }
                    reqScanFile(new OnFileFilterListener() {
                        @Override
                        public void onChangedFolder(File folder) {
                            if (!mIsForceStopScan) {
                                mFirstFolderList.add(folder);
                            }
                        }

                        @Override
                        public void onChangedFile(String filePath) {
                            // TO DO
                            if (!mIsForceStopScan) {
                                handlerSortMediaFile(filePath);
                            }
                        }
                    }, mSecondFolderList.get(j));

                }
                mSecondFolderList.clear();
            }
        }// end if(targetFile.isDirectory())
    }

    // 处理分类媒体文件
    void handlerSortMediaFile(String filePath) {
        if (isMatchFileType(filePath, ConstantsMediaSuffix.SUFFIX_ARRAY_AUDIO)) {
            if (null != mOnFileTreeScanListener) {
                mOnFileTreeScanListener.onScanFileTreeResult(ConstantsMediaSuffix.MediaSuffixType.SUFFIX_TYPE_AUDIO, filePath);
            }
            return;
        }

        if (isMatchFileType(filePath, ConstantsMediaSuffix.SUFFIX_ARRAY_VIDEO)) {
            if (null != mOnFileTreeScanListener) {
                mOnFileTreeScanListener.onScanFileTreeResult(ConstantsMediaSuffix.MediaSuffixType.SUFFIX_TYPE_VIDEO, filePath);
            }
            return;
        }

        if (isMatchFileType(filePath, ConstantsMediaSuffix.SUFFIX_ARRAY_PHOTO)) {
            if (null != mOnFileTreeScanListener) {
                mOnFileTreeScanListener.onScanFileTreeResult(ConstantsMediaSuffix.MediaSuffixType.SUFFIX_TYPE_PHOTO, filePath);
            }
            return;
        }
    }

    // 请求扫描媒体文件
    void reqScanFile(OnFileFilterListener l, File targetFile) {
        if (targetFile.isDirectory()) {// 文件夹
            File[] fs = targetFile.listFiles();
            if (null != fs && fs.length > 0) {
                for (File f : fs) {
                    if (mIsForceStopScan) {
                        return;
                    }
                    if (f.isDirectory()) {// 文件夹
                        l.onChangedFolder(f);
                    } else {// 文件
                        l.onChangedFile(f.getAbsolutePath());
                    }
                }
            }
        } else {// 文件
            l.onChangedFile(targetFile.getAbsolutePath());
        }
    }

    // 是否匹配文件类型
    boolean isMatchFileType(String fileUri, String[] suffix) {
        for (String sx : suffix) {
            if (fileUri.toLowerCase().endsWith(sx)) {
                return true;
            }
        }
        return false;
    }

    // 重置缓存数据
    void resetCacheData(){
        if(null != mFirstFolderList){
            mFirstFolderList.clear();
        }
        if(null != mSecondFolderList){
            mSecondFolderList.clear();
        }
    }


}
