package com.semisky.automultimedia.service.datainfo;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;

import com.semisky.automultimedia.common.bean.MusicInfo;
import com.semisky.automultimedia.common.bean.PictureInfo;
import com.semisky.automultimedia.common.constant.Definition;
import com.semisky.automultimedia.common.sql.DBConfiguration;
import com.semisky.automultimedia.common.sql.MediaDBManager;
import com.semisky.automultimedia.common.utils.LogUtil;
import com.semisky.automultimedia.common.utils.PinyinComparatorBySong;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.semisky.automultimedia.common.constant.Definition.PictureMessge.PICTURE_LIST;

/**
 * Created by 熊俊 on 2017/12/29.
 */

public class PictureDataInfo extends BaseDataInfo {
    private Context mContext;

    public PictureDataInfo(Context mContext) {
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

    public void getPictureList(Handler handler){
        Thread thread = new Thread(new QueryPictureRunnable(handler));
        thread.start();
    }

    private class QueryPictureRunnable implements Runnable{
        private Handler handler;

        public QueryPictureRunnable(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void run() {
            queryAllPictureList(handler);
        }
    }

    /**
     * 查询图片信息
     */
    private void queryAllPictureList(Handler handler) {
        Cursor cursor = MediaDBManager.getInstance(mContext).queryAllPicture();
        List<PictureInfo> tempPicinfoList = new ArrayList<PictureInfo>();
        PictureInfo pictureInfo = null;

        if (null != cursor) {
            while (cursor.moveToNext()) {
                String picUrl = cursor.getString(cursor.getColumnIndex(DBConfiguration.PictureConfiguration.PICTURE_URL));
                String picTitle = picUrl.substring(picUrl.lastIndexOf(File.separator));
                pictureInfo = new PictureInfo(picUrl, picTitle);
                tempPicinfoList.add(pictureInfo);

            }
            Message message = Message.obtain();
            message.what = PICTURE_LIST;
            message.obj = tempPicinfoList;
            handler.sendMessage(message);

            pictureInfo = null;
            tempPicinfoList = null;
            cursor.close();
        }

    }
}
