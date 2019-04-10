package com.semisky.automultimedia.common.sql;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.semisky.automultimedia.MediaApplication;
import com.semisky.automultimedia.common.utils.LogUtil;

import static android.content.Context.MODE_PRIVATE;
import static com.semisky.automultimedia.common.constant.Definition.MusicPlayModel.MODE_CIRCLE_ALL;

/**
 * Created by xiong on 2017/7/31.
 */

public class SharePreferenceUtil {
    public static final String SHARE_NAME = "semiskyAutoMultiMedia";
    public static final String PICTURE_SIZE = "pictureSize";
    public static final String MUSIC_SIZE = "musicSize";
    public static final String VEDIO_SIZE = "vedioSize";

    private static SharedPreferences getSharedPreferences(Context ctx) {
        return ctx.getSharedPreferences(SHARE_NAME, MODE_PRIVATE);
    }

    public static int getLastAppFlag() {
        SharedPreferences sharedPref = getSharedPreferences(MediaApplication.getContext());
        return sharedPref.getInt("last_app_flag", -1);
    }

    public static void saveLastAppFlag(int appFlag) {
        SharedPreferences sharedPref = getSharedPreferences(MediaApplication.getContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("last_app_flag", appFlag);
        editor.commit();
        LogUtil.i("saveLastAppFlag() appFLag=" + appFlag);
    }

    public static void insertPictureSize(Context context, int pictureSize) {
        SharedPreferences sharedPref = getSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(PICTURE_SIZE, pictureSize);
        editor.commit();
        LogUtil.i(" vedioSize " + pictureSize);
    }

    public static int getPictureSize(Context context) {
        SharedPreferences sharedPref = getSharedPreferences(context);
        int pictureSize = sharedPref.getInt(PICTURE_SIZE, -1);
        return pictureSize;
    }

    public static void insertmusicSize(Context context, int musicSize) {
        SharedPreferences sharedPref = getSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(MUSIC_SIZE, musicSize);
        editor.commit();
        LogUtil.i(" vedioSize " + musicSize);
    }

    public static int getMusicSize(Context context) {
        SharedPreferences sharedPref = getSharedPreferences(context);
        int musicSize = sharedPref.getInt(MUSIC_SIZE, -1);
        return musicSize;
    }

    public static void insertVedioSize(Context context, int vedioSize) {
        SharedPreferences sharedPref = getSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(VEDIO_SIZE, vedioSize);
        editor.commit();
        LogUtil.i(" vedioSize " + vedioSize);
    }

    public static int getVedioSize(Context context) {
        SharedPreferences sharedPref = getSharedPreferences(context);
        int vedioSize = sharedPref.getInt(VEDIO_SIZE, -1);
        return vedioSize;
    }

    /**
     * 设置媒体播放模式
     */
    public static void setPlayMode(Context ctx, int mode) {
        SharedPreferences sharedPref = getSharedPreferences(ctx);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("playmode", mode);
        editor.commit();
        LogUtil.i(" playmode " + mode);
    }

    /**
     * 获得媒体播放模式
     */
    public static int getPlayMode(Context ctx) {
        SharedPreferences sharedPref = getSharedPreferences(ctx);
        int playmode = sharedPref.getInt("playmode", MODE_CIRCLE_ALL);
        return playmode;
    }


    /**
     * 读取当前播放的音频文件的播放进度
     */
    public static int getCurrentPlayingMusicProgress(Context context) {
        return getSharedPreferences(context).getInt("PlayingMusicProgress", 0);
    }

    /**
     * 保存当前播放的音频文件的播放进度
     */
    public static boolean setCurrentPlayingMusicProgress(Context context, int progress) {
        return getSharedPreferences(context).edit().putInt("PlayingMusicProgress", progress).commit();
    }

    /**
     * 读取当前播放的音频文件路径
     */
    public static String getCurrentPlayingMusicUrl(Context context) {
         if(getSharedPreferences(context) != null){
            return getSharedPreferences(context).getString("PlayingMusicUrl", null);
        }
        return null;
    }

    /**
     * 保存当前播放的音频文件路径
     */
    public static boolean setCurrentPlayingMusicUrl(Context context, String filePath) {
        LogUtil.i("setCurrentPlayingMusicUrl " + filePath);
        return getSharedPreferences(context).edit().putString("PlayingMusicUrl", filePath).commit();
    }


    /**
     * 保存当前播放的视频文件路径
     */
    public static boolean setCurrentPlayingVideoUrl(Context context, String videoUrl) {
        return getSharedPreferences(context).edit().putString("currentPlayingVideoUrl", videoUrl).commit();
    }

    /**
     * 读取当前播放的视频文件路径
     */
    public static String getCurrentPlayingVideoUrl(Context context) {
        return getSharedPreferences(context).getString("currentPlayingVideoUrl", null);
    }

    /**
     * 保存当前播放的视频当前播放进度
     */
    public static boolean setCurrentPlayVideoProgress(Context ctx, int progress) {
        return getSharedPreferences(ctx).edit().putInt("videoPlayProgress", progress).commit();
    }

    /**
     * 获取当前播放的视频当前播放进度
     */
    public static int getCurrentPlayVideoProgress(Context ctx) {
        return getSharedPreferences(ctx).getInt("videoPlayProgress", 0);
    }

    /**
     * 保存最后视频是否在播放（只保存用户手动操作状态）
     */
    public static boolean setVideoFinallyPlaying(Context context, boolean isPlaying) {
        return getSharedPreferences(context).edit().putBoolean("videoFinallyPlayStatus", isPlaying).commit();
    }

    /**
     * 最后视频是否在播放
     */
    public static boolean isVideoFinallyPlaying(Context context) {
        return getSharedPreferences(context).getBoolean("videoFinallyPlayStatus", false);
    }

    /**
     * 保存当前播放音乐的文件夹
     */
    public static boolean setCurrentPlayMusicFolder(Context ctx, String folder) {
        return getSharedPreferences(ctx).edit().putString("musicPlayFolder", folder).commit();
    }

    /**
     * 获取当前播放音乐的文件夹
     */
    public static String getCurrentPlayMusicFolder(Context ctx) {
        return getSharedPreferences(ctx).getString("musicPlayFolder", null);
    }

    /**
     * 保存音乐文件最后创建时间
     */
    public static boolean saveMusicFileLastModified(long lastModifiedTime) {
        return getSharedPreferences(MediaApplication.getContext()).edit().putLong("musicLastModifiedTime", lastModifiedTime).commit();
    }

    /**
     * 获取音乐文件最后创建时间
     */
    public static long getMusicFileLastModified(){
        return getSharedPreferences(MediaApplication.getContext()).getLong("musicLastModifiedTime",-1);
    }

    /**
     * 保存视频文件最后创建时间
     */
    public static boolean saveVideoFileLastModified(long lastModifiedTime) {
        return getSharedPreferences(MediaApplication.getContext()).edit().putLong("videoLastModifiedTime", lastModifiedTime).commit();
    }

    /**
     * 获取视频文件最后创建时间
     */
    public static long getVideoFileLastModified(){
        return getSharedPreferences(MediaApplication.getContext()).getLong("videoLastModifiedTime",-1);
    }

    /**
     * 保存当前播放的图片文件路径
     */
    public static boolean setCurrentPlayingPictureUrl(Context context, String videoUrl) {
        return getSharedPreferences(context).edit().putString("currentPlayingPictureUrl", videoUrl).commit();
    }

    /**
     * 读取当前播放的图片文件路径
     */
    public static String getCurrentPlayingPictureUrl(Context context) {
        return getSharedPreferences(context).getString("currentPlayingPictureUrl", null);
    }

    /**
     * 保存图片文件最后创建时间
     */
    public static boolean savePictureFileLastModified(long lastModifiedTime) {
        return getSharedPreferences(MediaApplication.getContext()).edit().putLong("pictureLastModifiedTime", lastModifiedTime).commit();
    }

    /**
     * 获取图片文件最后创建时间
     */
    public static long getPictureFileLastModified(){
        return getSharedPreferences(MediaApplication.getContext()).getLong("pictureLastModifiedTime",-1);
    }

    /**
     * 保存当前U盘序列号
     */
    public static boolean saveUSBSerial(Context context, String serial) {
        return getSharedPreferences(context).edit().putString("USBSerial", serial).commit();
    }

    /**
     * 获取U盘序列号
     */
    public static String getUSBSerial() {
        return getSharedPreferences(MediaApplication.getContext()).getString("USBSerial",null);
    }

    /**
     * 保存当前音乐是否播放
     */
    public static boolean saveCurrentMusicIsPlaying(Context context, boolean isPlaying) {
        return getSharedPreferences(context).edit().putBoolean("isPlaying", isPlaying).commit();
    }

    /**
     * 获取拔出U盘音乐是否在播放
     */
    public static boolean getCurrentMusicIsPlaying(Context context) {
        return getSharedPreferences(context).getBoolean("isPlaying", false);
    }



}
