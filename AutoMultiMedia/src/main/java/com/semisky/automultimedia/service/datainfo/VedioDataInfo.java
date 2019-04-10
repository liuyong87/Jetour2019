package com.semisky.automultimedia.service.datainfo;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;

import com.semisky.automultimedia.common.bean.VideoInfo;
import com.semisky.automultimedia.common.constant.Definition;
import com.semisky.automultimedia.common.sql.DBConfiguration;
import com.semisky.automultimedia.common.sql.MediaDBManager;
import com.semisky.automultimedia.common.utils.PinyinComparatorByVideo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by 熊俊 on 2017/12/28.
 */

public class VedioDataInfo  {
    private Context mContext;

    public VedioDataInfo(Context mContext) {
        this.mContext = mContext;
    }

    public void getVideoList(Handler handler){
        Thread thread = new Thread(new QueryVideoRunnable(handler));
        thread.start();
    }

    private class QueryVideoRunnable implements Runnable{
        private Handler handler;

        public QueryVideoRunnable(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void run() {
            queryAllVideoList(handler);
        }
    }

    public void queryAllVideoList(Handler handler) {// 查询数据库指定存储路径所有视频信息

        VideoInfo videoInfo = null;
        List<VideoInfo> videoUrlList = new ArrayList<VideoInfo>();
        Cursor cursor = MediaDBManager.getInstance(mContext).queryAllVideoInfoByStorage();
        if (null != cursor) {

            while (cursor.moveToNext()) {

                String videoUrl = cursor.getString(cursor.getColumnIndex(DBConfiguration.VideoConfiguration.VIDEO_URL));
                String videoTitle = cursor.getString(cursor.getColumnIndex(DBConfiguration.VideoConfiguration.VIDEO_TITLE));
                String displayName = videoTitle.substring(0, videoTitle.indexOf("."));
                String videoTitlePinYing = cursor.getString(cursor.getColumnIndex(DBConfiguration.VideoConfiguration.VIDEO_TITLE_PINYING));

                videoInfo = new VideoInfo(videoUrl,displayName,videoTitle,videoTitlePinYing,true);
                videoUrlList.add(videoInfo);
            }
            cursor.close();
        }
        Collections.sort(videoUrlList, new PinyinComparatorByVideo());

        Message message = Message.obtain();
        message.what = Definition.VideoMessge.VEDIO_LIST;
        message.obj = videoUrlList;
        handler.sendMessage(message);
    }

}
