package com.semisky.automultimedia.common.utils;

import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by liuyong on 18-2-28.
 */

public class FileUtil {
    private static final String TAG = FileUtil.class.getSimpleName();
    private static volatile FileUtil INSTANCE;

    private FileUtil() {

    }

    public static FileUtil getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new FileUtil();
        }
        return INSTANCE;
    }

    /**
     * 获取文件所在的文件路径
     *
     * @param path
     * @return
     */
    public String getPathOfFile(@Nullable String path) {
        return path.substring(0, path.lastIndexOf(File.separator) + 1);
    }

    /**
     * 获取最后一反斜杠后的字符串
     *
     * @param file
     * @return
     */
    public String getFileName(@Nullable String file) {
        return file.substring(file.lastIndexOf(File.separator) + 1);
    }

    /**
     * 获取指定路径下文件总数量
     *
     * @param path
     * @return
     */
    public int getFileTotalCount(String path) {
        File filePath = new File(path);
        if (filePath.exists()) {
            return filePath.listFiles().length;
        }
        return -1;
    }

    /**
     * 删除文件
     */
    public void deleteFile(String path) {
        Log.i(TAG, "---------deleteFile ="+path);
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }


    /**
     * 确定文件存在后获取文件大小
     *
     * @param path
     * @return
     */
    public long getFileLength(String path) {
        File file = new File(path);
        if (!file.exists()) {// 文件存在
            return -1;
        }
        return file.length();
    }


    /**
     * 判断文件是否存在,不存在则创建
     *
     * @param path
     */
    void createFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 复制文件
     *
     * @param oldPath
     * @param newPath
     * @return
     */
    public boolean copyFile(String oldPath, String newPath) {
        try {
            int byteread = 0;
            File oldfile = new File(oldPath);
            createFile(newPath);
            if (oldfile.exists()) { // 文件存在时
                Log.i(TAG, "---------exists:");
                FileInputStream fis = new FileInputStream(oldPath); // 读入原文件
                FileOutputStream fos = new FileOutputStream(newPath);
                byte[] buffer = new byte[1024];
                while ((byteread = fis.read(buffer)) != -1) {
                    fos.write(buffer, 0, byteread);
                }
                Log.i(TAG, "---------while:");
                fos.flush();
                fis.close();
                fos.close();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;

        }
        return false;
    }
}
