package com.semisky.automultimedia.common.data;

import android.database.Cursor;

import com.semisky.automultimedia.MediaApplication;
import com.semisky.automultimedia.common.bean.VideoInfo;
import com.semisky.automultimedia.common.sql.DBConfiguration;
import com.semisky.automultimedia.common.sql.MediaDBManager;
import com.semisky.automultimedia.common.utils.LogUtil;
import com.semisky.automultimedia.common.utils.PinyinComparatorByVideo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyong on 18-3-30.
 */

public class VideoDataModule {
    private static final String TAG = VideoDataModule.class.getSimpleName();
    private static VideoDataModule INSTANCE;

    // 视频列表集合
    // （注：1.）播放器要使用;2.）列表要使用）
    private List<VideoInfo> mVideoInfoList;

    public static final int FLAG_LIST = 1;
    public static final int FLAG_PLAYER = 2;
    private List<OnLoadDataListener> mDataListenerList = null;

    // Empty Constructor
    private VideoDataModule() {
        mVideoInfoList = new ArrayList<VideoInfo>();
        mDataListenerList = new ArrayList<OnLoadDataListener>();
    }

    // Get Singleton Entity Class
    public static VideoDataModule getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new VideoDataModule();
        }
        return INSTANCE;
    }

    // Releases resources
    public void onDestroy() {
        LogUtil.i(TAG, "onDestroy()...");
        if (null != mVideoInfoList) {
            mVideoInfoList.clear();
            mVideoInfoList = null;
        }
        if (null != mDataListenerList) {
            mDataListenerList.clear();
            mDataListenerList = null;
        }
        if (null != INSTANCE) {
            INSTANCE = null;
        }
    }

    // Interface

    public interface OnLoadDataListener {
        // 回调此方法时，返回所有音乐File集合
        void onAllVideoListResult(int to, List<VideoInfo> videoList, boolean isRecoverPlay);
    }

    public void registerOnLoadDataListener(OnLoadDataListener listener) {
        if (null != listener && !mDataListenerList.contains(listener)) {
            mDataListenerList.add(listener);
        }
    }

    public void unregisterOnLoadDataListener(OnLoadDataListener listener) {
        if (null != listener && mDataListenerList.contains(listener)) {
            mDataListenerList.remove(listener);
        }
    }

    private void notifyLoadAllVideoResult(int to, List<VideoInfo> videoList, boolean isRecoverPlay) {
        for (OnLoadDataListener listener : mDataListenerList) {
            listener.onAllVideoListResult(to, videoList, isRecoverPlay);
        }
    }

    // 加载所有视频资源
    public void loadAllVideoToPlayer(final int to, final boolean isRecoverPlay) {
        if(null != mVideoInfoList){
            mVideoInfoList.clear();
        }
        if(null == mVideoInfoList){
            mVideoInfoList = new ArrayList<VideoInfo>();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mVideoInfoList.size() <= 0) {
                    List<VideoInfo> videoInfoList = queryAllVideoList();
                    if (null != videoInfoList) {
                        mVideoInfoList.addAll(videoInfoList);
                    }
                }
                notifyLoadAllVideoResult(to, mVideoInfoList, isRecoverPlay);
            }
        }).start();
    }

    public List<VideoInfo> queryAllVideoList() {// 查询数据库指定存储路径所有视频信息

        VideoInfo videoInfo = null;
        List<VideoInfo> videoUrlList = new ArrayList<VideoInfo>();
        Cursor cursor = MediaDBManager.getInstance(MediaApplication.getContext()).queryAllVideoInfoByStorage();
        if (null != cursor) {

            while (cursor.moveToNext()) {

                String videoUrl = cursor.getString(cursor.getColumnIndex(DBConfiguration.VideoConfiguration.VIDEO_URL));
                String videoTitle = cursor.getString(cursor.getColumnIndex(DBConfiguration.VideoConfiguration.VIDEO_TITLE));
                String displayName = videoTitle.substring(0, videoTitle.lastIndexOf("."));
                String videoTitlePinYing = cursor.getString(cursor.getColumnIndex(DBConfiguration.VideoConfiguration.VIDEO_TITLE_PINYING));

                videoInfo = new VideoInfo(videoUrl, displayName, videoTitle, videoTitlePinYing, true);
                videoUrlList.add(videoInfo);
            }
            cursor.close();
        }
        Collections.sort(videoUrlList, new PinyinComparatorByVideo());
        return videoUrlList;

    }

    // Utils
    // Gettings/Settings

    // 当下正在播放或者列表选择将要播放的节目URL
    private String mPlayingProgramUrl = null;

    public void setmPlayingProgramUrl(String url) {
        this.mPlayingProgramUrl = url;
    }

    public String getmPlayingProgramUrl() {
        return this.mPlayingProgramUrl;
    }
}
