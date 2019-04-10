package com.semisky.automultimedia.service.datainfo;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.semisky.automultimedia.R;
import com.semisky.automultimedia.common.bean.MusicInfo;
import com.semisky.automultimedia.common.constant.Definition;
import com.semisky.automultimedia.common.sql.DBConfiguration;
import com.semisky.automultimedia.common.sql.MediaDBManager;
import com.semisky.automultimedia.common.utils.LogUtil;
import com.semisky.automultimedia.common.utils.PinyinComparatorBySong;
import com.semisky.automultimedia.common.utils.USBManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created on 2017/12/15.
 * Author: xiongjun
 * About:
 */

public class MusicDataInfo extends BaseDataInfo {
    private Context mContext;

    public MusicDataInfo(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void createDataInfo() {

    }

    @Override
    public void startDataInfo() {

    }

    @Override
    public void destroyDataInfo() {

    }

    public void getMusicList(Handler handler) {
        Thread thread = new Thread(new QueryMusicRunnable(handler, null));
        thread.start();
    }

    public void getFolderList(Handler handler, String folderUrl) {
        Thread thread = new Thread(new QueryMusicRunnable(handler, folderUrl));
        thread.start();
    }

    private class QueryMusicRunnable implements Runnable {
        private Handler handler;
        private String folderUrl;

        public QueryMusicRunnable(Handler handler, String folderUrl) {
            this.handler = handler;
            this.folderUrl = folderUrl;
        }

        @Override
        public void run() {
            if (folderUrl == null)
                queryAllMusicList(handler);
            else
                queryFolderMusicList(handler, folderUrl);
        }
    }


    /**
     * 查询音乐列表信息
     */
    private void queryAllMusicList(Handler handler) {
        Cursor cursor = null;

        // 临时存储音乐信息集合
        ArrayList<MusicInfo> tempMusicList = new ArrayList<MusicInfo>();
        ArrayList<MusicInfo> folderList = new ArrayList<MusicInfo>();

        //新建“所有音乐”的对象
        String curMountedFirstUsbPath = USBManager.getInstance().getCurrentMountedUsbPath();
        MusicInfo allmusicInfo = new MusicInfo();
        allmusicInfo.setTitle(mContext.getString(R.string.all_music));
        allmusicInfo.setUrl(curMountedFirstUsbPath);
        allmusicInfo.setIsDir(true);
        folderList.add(allmusicInfo);

        cursor = MediaDBManager.getInstance(mContext).queryAllMusicInfo();
        LogUtil.d(": queryAllMusicList() cursor.getCount() : " + (cursor != null ? cursor.getCount() : "null"));

        if (cursor != null && cursor.getCount()> 0) {
            while (cursor.moveToNext()) {
                MusicInfo musicInfo = getMusicInfoFromCursor(cursor);
                tempMusicList.add(musicInfo);

                boolean i = false;
                String folder = musicInfo.getUrl().substring(0, musicInfo.getUrl().lastIndexOf(File.separator));

                for (MusicInfo str : folderList) {
                    if (str.getIsDir()) {
                        if (null != str.getUrl() && str.getUrl().equals(folder)) {
                            i = true;
                            break;
                        }
                    }
                }
                if (!i) {
                    MusicInfo tempfolder = new MusicInfo();
                    int j = folder.lastIndexOf(File.separator);
                    tempfolder.setTitle(folder.substring(j+1 , folder.length()));
                    tempfolder.setIsDir(true);
                    tempfolder.setUrl(folder);
                    folderList.add(tempfolder);
                }
            }
            cursor.close();
            if (tempMusicList.size() > 0) {
                Collections.sort(tempMusicList, new PinyinComparatorBySong());
            }

        }
        Message message = Message.obtain();
        message.what = Definition.MusicMessge.MUSIC_LIST;
        Bundle bundle = new Bundle();
        bundle.putSerializable("musicList", tempMusicList);
        bundle.putSerializable("folderList", folderList);
        message.obj = bundle;
        handler.sendMessage(message);
    }

    /**
     * 查询目录下的音乐集合
     */
    private void queryFolderMusicList(Handler handler, String folderURL) {
        Cursor cursor = null;

        // 临时存储音乐信息集合
        ArrayList<MusicInfo> tempMusicList = new ArrayList<MusicInfo>();
        ArrayList<MusicInfo> folderList = new ArrayList<MusicInfo>();

        //新建“所有音乐”的对象
        String curMountedFirstUsbPath = USBManager.getInstance().getCurrentMountedUsbPath();
        MusicInfo allmusicInfo = new MusicInfo();
        allmusicInfo.setTitle(mContext.getString(R.string.all_music));
        allmusicInfo.setUrl(curMountedFirstUsbPath);
        allmusicInfo.setIsDir(true);
        folderList.add(allmusicInfo);

        cursor = MediaDBManager.getInstance(mContext).queryMusicDirectUnder(folderURL);
        LogUtil.d(": queryAllMusicList() cursor.getCount() : " + (cursor != null ? cursor.getCount() : "null"));

        if (cursor != null) {
            while (cursor.moveToNext()) {
                MusicInfo musicInfo = getMusicInfoFromCursor(cursor);
                tempMusicList.add(musicInfo);
            }
            cursor.close();
            if (tempMusicList.size() > 0) {
                Collections.sort(tempMusicList, new PinyinComparatorBySong());
            }

        }
        Message message = Message.obtain();
        message.what = Definition.MusicMessge.FOLDER_MUSIC_LIST;
        Bundle bundle = new Bundle();
        bundle.putSerializable("musicList", tempMusicList);
        bundle.putSerializable("folderList", folderList);
        message.obj = bundle;
        handler.sendMessage(message);
    }

    /**
     * 通过指定游标获得音乐实体信息
     */
    private MusicInfo getMusicInfoFromCursor(Cursor cursor) {
        String url = cursor.getString(cursor.getColumnIndex(DBConfiguration.MusicConfiguration.MUSIC_URL));
        String title = cursor.getString(cursor.getColumnIndex(DBConfiguration.MusicConfiguration.MUSIC_TITLE));
        String titlePinYing = cursor.getString(cursor
                .getColumnIndex(DBConfiguration.MusicConfiguration.MUSIC_TITLE_PINYING));
        String displayName = title.substring(0, title.indexOf("."));
        String art = cursor.getString(cursor.getColumnIndex(DBConfiguration.MusicConfiguration.MUSIC_ARTIST));
        // String album =
        // cursor.getString(cursor.getColumnIndex(DBConfiguration.MusicConfiguration.MUSIC_ALBUM));
        String album = "";
        byte[] albumPicture = cursor.getBlob(cursor.getColumnIndex(DBConfiguration.MusicConfiguration.EMBEDDED_PICTURE));
        int duration = cursor.getInt(cursor.getColumnIndex(DBConfiguration.MusicConfiguration.MUSIC_DURATION));

        MusicInfo musicInfo = new MusicInfo(url, displayName, title, titlePinYing, art, album, albumPicture, duration, true);
        return musicInfo;
    }

}
