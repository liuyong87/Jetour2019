package com.semisky.automultimedia.common.utils;

import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import com.semisky.automultimedia.MediaApplication;

import java.io.File;

/**
 * Created by liuyong on 18-2-28.
 */

public class FileManager {
    private static String TAG = FileManager.class.getSimpleName();
    private static volatile FileManager INSTANCE;
    private static final int FILE_LIMIT_COUNT = 3;// 本地文件限制保存数量
    public static final String DEF_SD_DIR = "/sdcard/Pictures/";

    private FileManager() {
    }

    public static FileManager getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new FileManager();
        }
        return INSTANCE;
    }


    /**
     * 将文件复制到本地
     *
     * @param oldFile
     * @param newFileDir
     */
    public void copyFileToLocal(@Nullable String oldFile, @Nullable String newFileDir) {

        String oldFileName = FileUtil.getInstance().getFileName(oldFile);
        String newFile = newFileDir + oldFileName;// 拼接新的文件路径

        boolean isOldFileExists = new File(oldFile).exists();
        boolean isNewFileExists = new File(newFile).exists();

        if (isOldFileExists) {
            if (hasFull(newFileDir, FILE_LIMIT_COUNT)) {// 文件已达到指定个数
                // 获取本地最早时间文件
                if (!isNewFileExists) {// 本地不存在此文件
                    Log.i(TAG, "-------------Replace file !!!");
                    String earliestFile = getEarliestFile(newFileDir);
                    // 删除本地时间最早文件
                    FileUtil.getInstance().deleteFile(earliestFile);
                    // 复制新的文件到本地
                    boolean result = copyFile(oldFile, newFile);
                    //保存当前设置的壁纸
                    Settings.System.putString(MediaApplication.getContext().getContentResolver(),"wallpaper",newFile);
                    notifyFileCopyResult((result ? IFileCopyListener.RESULT_CODE_SUCCESS : IFileCopyListener.RESULT_CODE_FAIL));
                    return;
                }
                notifyFileCopyResult(IFileCopyListener.RESULT_CODE_FAIL);
                Log.i(TAG, "-------------Replace file exists!!!");
            } else {
                if (!isNewFileExists) {
                    Log.i(TAG, "-------------Add File!!!");
                    boolean result = copyFile(oldFile, newFile);
                    Settings.System.putString(MediaApplication.getContext().getContentResolver(),"wallpaper",newFile);
                    notifyFileCopyResult((result ? IFileCopyListener.RESULT_CODE_SUCCESS : IFileCopyListener.RESULT_CODE_FAIL));
                    return;
                }
                notifyFileCopyResult(IFileCopyListener.RESULT_CODE_FAIL);
                Log.i(TAG, "-------------Add file exists!!!");
            }
            return;
        }
        notifyFileCopyResult(IFileCopyListener.RESULT_CODE_FAIL);
        Log.i(TAG, "-------------File No Exists!!! ,file=" + oldFile);

    }

    /**
     * 是否文件已满
     *
     * @param localPicturePath
     * @param limit
     * @return
     */
    boolean hasFull(String localPicturePath, int limit) {
        int in = -1;
        File f = new File(localPicturePath);

        if (!f.exists()) {
            f.mkdirs();
        }

        in = f.listFiles().length;
        Log.i(TAG, "hasFull()=" + in);
        if (in >= limit) {
            return true;
        }
        return false;
    }


    /**
     * 获取时间最早文件
     *
     * @param dirPath
     * @return
     */
    String getEarliestFile(String dirPath) {
        long minTime = -1;
        String earliestFilePath = null;

        File[] files = new File(dirPath).listFiles();

        for (File fs : files) {
            long fileDate = fs.lastModified();// 获取文件时间
            if (minTime == -1) {
                minTime = fileDate;
                earliestFilePath = fs.getPath();
            }
            if (fileDate < minTime) {// 最早时间文件
                minTime = fileDate;
                earliestFilePath = fs.getPath();
            }
        }
        return earliestFilePath;
    }

    boolean copyFile(String oldFilePath, String newFilePath) {
        long oldFileLen = -1;
        long newFileLen = -1;
        // 获取原文件长度
        oldFileLen = FileUtil.getInstance().getFileLength(oldFilePath);
        Log.i(TAG, "copeFile()-------oldFileLen=" + oldFileLen);

        // 复制文件到指定位置
        boolean isCopyOk = FileUtil.getInstance().copyFile(oldFilePath, newFilePath);
        Log.i(TAG, "copeFile()-------isCopyOk=" + isCopyOk);

        // 获取新文件长度
        newFileLen = FileUtil.getInstance().getFileLength(newFilePath);
        Log.i(TAG, "copeFile()-------newFileLen=" + newFileLen);

        if (!isCopyOk || oldFileLen != newFileLen || oldFileLen == -1 || newFileLen == -1) {
            Log.i(TAG, "copeFile()-------deleteFile=" + newFilePath);
            FileUtil.getInstance().deleteFile(newFilePath);
        }
        return isCopyOk;
    }

    public interface IFileCopyListener {
        final int RESULT_CODE_SUCCESS = 0;
        final int RESULT_CODE_FAIL = 1;

        void onResult(int result);
    }

    private IFileCopyListener mIFileCopyListener;

    public void registerIFileCopyListener(IFileCopyListener listener) {
        this.mIFileCopyListener = listener;
    }

    void notifyFileCopyResult(int result) {
        if (null != mIFileCopyListener) {
            notifyFileCopyResult(result);
        }
    }

}
