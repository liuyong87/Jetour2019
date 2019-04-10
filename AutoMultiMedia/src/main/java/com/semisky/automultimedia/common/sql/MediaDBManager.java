package com.semisky.automultimedia.common.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.semisky.automultimedia.common.utils.LogUtil;

import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * Created by 熊俊 on 2018/1/2.
 */

public class MediaDBManager {
    private static final String TAG = MediaDBManager.class.getSimpleName();
    private static volatile MediaDBManager instance;
    private Context mContext;

    private static MediaDBHelper mDBHelper;
    private static SQLiteDatabase db;

    private MediaDBManager(Context ctx) {
        this.mContext = ctx;
        mDBHelper = new MediaDBHelper(ctx, DBConfiguration.DATABASE_NAME, null, DBConfiguration.DATABASE_VERSION);
        db = mDBHelper.getWritableDatabase();
        db.setLocale(Locale.CHINESE);
    }

    public static MediaDBManager getInstance(Context ctx) {
        if (instance == null) {
            synchronized (MediaDBManager.class) {
                if (instance == null) {
                    instance = new MediaDBManager(ctx);
                }
            }
        }
        return instance;
    }

    /**
     * 插入图片信息到数据库
     *
     * @param values
     */
    public long insertPictureInfoToDatabase(List<ContentValues> values) {

        long result = -1;

        db.beginTransaction();
        for (ContentValues value : values) {
            result = db.insert(DBConfiguration.PictureConfiguration.TABLE_NAME, null, value);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        // LogUtil.d(TAG + ": **********insertPictureInfoToDatabase() result : "
        // + result);
        return result;
    }

    /**
     * 删除数据库图片信息
     */
    public int deleteDBPictureInfo() {
        int result = -1;
        result = db.delete(DBConfiguration.PictureConfiguration.TABLE_NAME,
                DBConfiguration.PictureConfiguration.USB_FLAG + " =? ", new String[]{DBConfiguration.USB_FLAG + ""});
        // LogUtil.d(TAG + ": **********deleteDBPictureInfo() result : " +
        // result);
        return result;
    }

    /**
     * 查询指定媒体源下的所有图片
     */
    public Cursor queryAllPicture() {
        Cursor cursor = null;
        cursor = db.query(true,DBConfiguration.PictureConfiguration.TABLE_NAME, null,
                DBConfiguration.PictureConfiguration.USB_FLAG + "=?", new String[]{DBConfiguration.USB_FLAG + ""},
                DBConfiguration.PictureConfiguration.PICTURE_URL, null,
                DBConfiguration.PictureConfiguration.DEFAULT_SORT_ORDER,null);
        return cursor;
    }

    /**
     * 查询图片目录信息
     */
    public Cursor queryPictureDirectUnder(String directory) {
        Cursor cursor = null;
        db.setLocale(Locale.CHINESE);
        cursor = db.query(DBConfiguration.PictureConfiguration.TABLE_NAME, null,
                DBConfiguration.PictureConfiguration.PICTURE_URL + " like ?", new String[]{directory + "%"}, null,
                null, DBConfiguration.PictureConfiguration.DEFAULT_SORT_ORDER);
        return cursor;
    }

    /**
     * 查询图片信息
     */
    public Cursor queryPictureInfo(int usbFlag) {
        Cursor cursor = null;
        db.setLocale(Locale.CHINESE);
        cursor = db.query(DBConfiguration.PictureConfiguration.TABLE_NAME, null,
                DBConfiguration.PictureConfiguration.USB_FLAG + " =?", new String[]{usbFlag + ""}, null, null,
                DBConfiguration.PictureConfiguration.DEFAULT_SORT_ORDER);
        return cursor;
    }

    /**
     * 插入视频信息到数据库
     */
    public long insertVideoInfoToDatabase(List<ContentValues> values) {
        long result = -1;

        db.beginTransaction();
        for (ContentValues value : values) {
            result = db.insert(DBConfiguration.VideoConfiguration.TABLE_NAME, null, value);
        }
        db.setTransactionSuccessful();
        db.endTransaction();

        LogUtil.d(TAG + ": insertVideoInfoToDatabase() ********************result: " + result);

        return result;
    }

    /**
     * 删除数据库视频信息
     */
    public int deleteDBVideoInfo() {
        int result = -1;

        result = db.delete(DBConfiguration.VideoConfiguration.TABLE_NAME, DBConfiguration.VideoConfiguration.USB_FLAG
                + " =? ", new String[]{DBConfiguration.USB_FLAG + ""});
        LogUtil.d(TAG + ": deleteDBVideoInfo() ********************result: " + result);
        return result;
    }

    /**
     * 按关键字搜索视频文件集合
     * <p>
     * 注：按关键字首字符匹配视频文件名首字符
     *
     * @param storageFlag
     * @param keywords
     * @return
     */
    public Cursor queryVideoByKeywords(int storageFlag, String keywords) {
        Cursor cursor = null;
        if (null == keywords) {
            return null;
        }
        keywords = keywords.charAt(0) + "";
        cursor = db.query(DBConfiguration.VideoConfiguration.TABLE_NAME, null,
                DBConfiguration.VideoConfiguration.USB_FLAG + "=? and "
                        + DBConfiguration.VideoConfiguration.VIDEO_TITLE_PINYING + " like ? ", new String[]{
                        storageFlag + "", keywords + "%"}, null, null, DBConfiguration.VideoConfiguration.DEFAULT_SORT_ORDER);
        LogUtil.i(TAG, "queryVideoByKeywords() keywords: " + keywords + " ,  cursor.getCount() : " + cursor.getCount());
        return cursor;
    }

    /**
     * 查询数据库指定存储路径所有视频信息
     *
     * @return
     */
    public Cursor queryAllVideoInfoByStorage() {
        db.setLocale(Locale.CHINESE);
        Cursor cursor = db.query(true,DBConfiguration.VideoConfiguration.TABLE_NAME, null,
                DBConfiguration.VideoConfiguration.USB_FLAG + "=?", new String[]{DBConfiguration.USB_FLAG + ""},
                DBConfiguration.VideoConfiguration.VIDEO_URL, null,
                DBConfiguration.VideoConfiguration.DEFAULT_SORT_ORDER,null);
        return cursor;
    }

    /**
     * 查询数据库指定存储路径所有视频信息
     *
     * @param storageFlag
     * @return
     */
    public Cursor queryAllVideoInfo(int storageFlag) {

        String url = null;

        Cursor cursor = db.query(DBConfiguration.VideoConfiguration.TABLE_NAME, null,
                DBConfiguration.VideoConfiguration.USB_FLAG + "=?", new String[]{storageFlag + ""}, null, null,
                DBConfiguration.VideoConfiguration.DEFAULT_SORT_ORDER);

        LogUtil.d(TAG + ": queryAllVideoInfo() ******************** cursor-Size: " + cursor.getCount());
        return cursor;
    }

    /**
     * 获取该目录下的直属视频文件及包含视频文件的文件夹（该文件夹只加一次） e.g. : select videoUrl from videos
     * where videoUrl like '/storage/usb0%' group by videoFolderUrl union select
     * videoUrl from videos where videoFolderUrl='/storage/usb0' order by
     * videoUrl asc
     */
    public Cursor queryVideoDirectUnder(String folderUrl) {
        Cursor cursor = null;

        // String sql = "select " + DBConfiguration.VideoConfiguration.VIDEO_URL
        // + " from "
        // + DBConfiguration.VideoConfiguration.TABLE_NAME + " where "
        // + DBConfiguration.VideoConfiguration.VIDEO_URL + " like '" +
        // folderUrl + File.separator + "%'";
        // db.setLocale(Locale.CHINESE);
        // cursor = db.rawQuery(sql, null);

        cursor = db.query(DBConfiguration.VideoConfiguration.TABLE_NAME, null,
                DBConfiguration.VideoConfiguration.VIDEO_URL + " like ? ", new String[]{folderUrl + File.separator
                        + "%"}, null, null, DBConfiguration.VideoConfiguration.DEFAULT_SORT_ORDER);

        return cursor;
    }


    /**
     * 批量插入音乐信息到数据库
     */
    public long insertMusicInfoToDatabase(List<ContentValues> values) {
        long result = -1;
        db.beginTransaction();
        for (ContentValues value : values) {
            result = db.insert(DBConfiguration.MusicConfiguration.TABLE_NAME, null, value);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        // LogUtil.d(TAG +
        // ": insertMusicInfoToDatabase() ***************result: " + result);
        return result;
    }

    /**
     * 删除音乐数据库媒体数据
     */
    public int deleteDBMusicInfo() {
        int result = -1;

        result = db.delete(DBConfiguration.MusicConfiguration.TABLE_NAME, DBConfiguration.MusicConfiguration.USB_FLAG
                + " =? ", new String[]{DBConfiguration.USB_FLAG + ""});
        LogUtil.d(TAG + ": deleteDBMusicInfo() usbFlag : " + " , ****result : " + result);
        return result;
    }

    /**
     * 查询数据库当前Storage下的所有音乐信息
     *
     * @return
     */
    public Cursor queryAllMusicInfo() {
        Cursor cursor = null;
        try {
            db.setLocale(Locale.CHINESE);
            cursor = db.query(true,DBConfiguration.MusicConfiguration.TABLE_NAME, null,
                    DBConfiguration.MusicConfiguration.USB_FLAG + "=?", new String[]{DBConfiguration.USB_FLAG + ""},
                    DBConfiguration.MusicConfiguration.MUSIC_URL, null,
                    DBConfiguration.MusicConfiguration.DEFAULT_SORT_ORDER,null);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e(TAG + ": queryAllMusicInfo() ERROR !!!");
        }
        // LogUtil.d(TAG +
        // ": queryAllMusicInfo() ******************** cursor-Size: " +
        // cursor.getCount() );
        return cursor;
    }

    /**
     * 查询指定USB下的所有歌手
     */
    public Cursor queryAllArtistFrom(int storageFlag) {
        Cursor cursor = null;
        try {
            cursor = db.query(DBConfiguration.MusicConfiguration.TABLE_NAME, null,
                    DBConfiguration.MusicConfiguration.USB_FLAG + " =? ", new String[]{storageFlag + ""},
                    DBConfiguration.MusicConfiguration.MUSIC_ARTIST, null,
                    DBConfiguration.MusicConfiguration.ARTIST_DEFAULT_SORT_ORDER);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e(TAG + ": queryAllArtistFrom() fail !!!");
        }
        return cursor;
    }

    /**
     * 查询歌手下的歌曲
     */
    public Cursor queryArtistUnderMusic(String artist) {
        Cursor cursor = null;
        try {
            cursor = db.query(DBConfiguration.MusicConfiguration.TABLE_NAME, null,
                    DBConfiguration.MusicConfiguration.MUSIC_ARTIST + " = ? ", new String[]{artist}, null, null,
                    DBConfiguration.MusicConfiguration.DEFAULT_SORT_ORDER);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e(TAG + ": queryArtistUnderMusic() fail !!!");
        }
        return cursor;
    }

    /**
     * 查询歌手下的歌曲
     */
    public Cursor queryArtistUnderMusic(int usbFlag, String artist) {
        Cursor cursor = null;
        try {
            cursor = db.query(DBConfiguration.MusicConfiguration.TABLE_NAME, null,
                    DBConfiguration.MusicConfiguration.USB_FLAG + " = ? and "
                            + DBConfiguration.MusicConfiguration.MUSIC_ARTIST + " = ? ", new String[]{usbFlag + "",
                            artist}, null, null, DBConfiguration.MusicConfiguration.DEFAULT_SORT_ORDER);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e(TAG + ": queryArtistUnderMusic() fail !!!");
        }
        return cursor;
    }

    /**
     * 模糊匹配属性指定路径的所有音乐文件
     *
     * @return
     */
    public Cursor queryMusicDirectUnder(String directory) {
        Cursor cursor = null;
        // db.setLocale(Locale.CHINESE);
        // String sql = "select * from " +
        // DBConfiguration.MusicConfiguration.TABLE_NAME + " where "
        // + DBConfiguration.MusicConfiguration.MUSIC_URL + " like '" +
        // directory + File.separator + "%'";
        // cursor = db.rawQuery(sql, null);
        cursor = db.query(true,DBConfiguration.MusicConfiguration.TABLE_NAME, null,
                DBConfiguration.MusicConfiguration.MUSIC_URL + " like ? ",
                new String[]{directory + File.separator + "%"},
                DBConfiguration.MusicConfiguration.MUSIC_URL, null,
                DBConfiguration.MusicConfiguration.DEFAULT_SORT_ORDER,null);
        return cursor;
    }

    public Cursor queryMusicByKeywords(int storageFlag, String keywords) {
        Cursor cursor = null;
        if (null == keywords) {
            return null;
        }
        keywords = keywords.charAt(0) + "";
        cursor = db.query(DBConfiguration.MusicConfiguration.TABLE_NAME, null,
                DBConfiguration.MusicConfiguration.USB_FLAG + "=? and "
                        + DBConfiguration.MusicConfiguration.MUSIC_TITLE_PINYING + " like ? ", new String[]{
                        storageFlag + "", keywords + "%"}, null, null, DBConfiguration.MusicConfiguration.DEFAULT_SORT_ORDER);
        return cursor;
    }

    /**
     * 根据歌手名称模糊匹配歌曲信息
     * 注：select * from musics where musicArtist like "%张敬轩%";
     */
    public Cursor queryLikeArtistUnderSongs(String artistKeywords) {
        Cursor cursor = null;
        cursor = db.query(DBConfiguration.MusicConfiguration.TABLE_NAME,
                null,
                DBConfiguration.MusicConfiguration.MUSIC_ARTIST + " like ?",
                new String[]{"%" + artistKeywords + "%"},
                null,
                null,
                DBConfiguration.MusicConfiguration.DEFAULT_SORT_ORDER);
        return cursor;
    }

    /**
     * 根据专辑名称模糊匹配歌曲信息
     * 注：select * from musics where musicAlbum like "%我们的歌%";
     */
    public Cursor queryLikeAlbumUnderSongs(String albumKeywords) {
        Cursor cursor = null;
        cursor = db.query(DBConfiguration.MusicConfiguration.TABLE_NAME,
                null,
                DBConfiguration.MusicConfiguration.MUSIC_ALBUM + " like ?",
                new String[]{"%" + albumKeywords + "%"},
                null,
                null,
                DBConfiguration.MusicConfiguration.DEFAULT_SORT_ORDER);
        return cursor;
    }

    /**
     * 根据指定歌曲名模糊匹配歌曲
     * 注：select * from musics where musicTitle like "%我们%";
     */
    public Cursor queryLikeSongs(String songKeywords) {
        Cursor cursor = null;
        cursor = db.query(DBConfiguration.MusicConfiguration.TABLE_NAME,
                null,
                DBConfiguration.MusicConfiguration.MUSIC_TITLE + " like ?",
                new String[]{"%" + songKeywords + "%"},
                null,
                null,
                DBConfiguration.MusicConfiguration.DEFAULT_SORT_ORDER);
        return cursor;

    }

    /**
     * 根据指定歌曲名和歌手关键字模糊匹配歌曲
     * 注：select * from musics where musicTitle like "%我们%" or musicArtist like "%王力%";
     */
    public Cursor queryLikeAtistAndSong(String artistKeywords, String songKeywords) {
        Cursor cursor =
                db.query(DBConfiguration.MusicConfiguration.TABLE_NAME,
                        null,
                        DBConfiguration.MusicConfiguration.MUSIC_TITLE + " like ? and "
                                + DBConfiguration.MusicConfiguration.MUSIC_ARTIST + " like ? ",
                        new String[]{"%" + songKeywords + "%", "%" + artistKeywords + "%"},
                        null,
                        null,
                        DBConfiguration.MusicConfiguration.DEFAULT_SORT_ORDER);
        return cursor;
    }

}
