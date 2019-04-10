package com.semisky.automultimedia.common.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.semisky.automultimedia.common.utils.LogUtil;

/**
 * Created by 熊俊 on 2018/1/2.
 */

public class MediaDBHelper extends SQLiteOpenHelper{

    private static final String TAG = "MediaDBHelper";

    private static final String CREATE_MUSIC_TABLE =  "create table "
            +DBConfiguration.MusicConfiguration.TABLE_NAME+"("
            +DBConfiguration.MusicConfiguration._ID
            +" integer primary key autoincrement ,"
            +DBConfiguration.MusicConfiguration.USB_FLAG + " integer ,"
            +DBConfiguration.FILE_TYPE + " integer ,"
            +DBConfiguration.MusicConfiguration.MUSIC_URL + " text ,"
            +DBConfiguration.MusicConfiguration.MUSIC_FOLDER_URL + " text ,"
            +DBConfiguration.MusicConfiguration.MUSIC_TITLE + " text ,"
            +DBConfiguration.MusicConfiguration.MUSIC_TITLE_PINYING + " text, "
            +DBConfiguration.MusicConfiguration.MUSIC_ARTIST + " text ,"
            +DBConfiguration.MusicConfiguration.EMBEDDED_PICTURE + " BLOB ,"
            +DBConfiguration.MusicConfiguration.MUSIC_ARTIST_PINYING + " text,"
            +DBConfiguration.MusicConfiguration.MUSIC_ALBUM + " text ,"
            +DBConfiguration.MusicConfiguration.MUSIC_DURATION + " text "
            +")";


    private static final String CREATE_PICTURE_TABLE = "create table "
            +DBConfiguration.PictureConfiguration.TABLE_NAME+"("
            +DBConfiguration.PictureConfiguration._ID
            +" integer primary key autoincrement ,  "
            +DBConfiguration.PictureConfiguration.USB_FLAG+" integer ,"
            +DBConfiguration.FILE_TYPE + " integer ,"
            +DBConfiguration.PictureConfiguration.PICTURE_URL+" text ,"
            +DBConfiguration.PictureConfiguration.PICTURE_TITLE + " text,"
            +DBConfiguration.PictureConfiguration.PICTURE_TITLE_PINYING + " text,"
            +DBConfiguration.PictureConfiguration.PICTURE_FOLDER_URL+" text "
            +")";

    private static final String CREATE_VIDEO_TABLE = "create table "
            +DBConfiguration.VideoConfiguration.TABLE_NAME+"("
            +DBConfiguration.VideoConfiguration._ID
            +" integer primary key autoincrement  , "
            +DBConfiguration.VideoConfiguration.USB_FLAG + " integer ,"
            +DBConfiguration.FILE_TYPE + " integer ,"
            +DBConfiguration.VideoConfiguration.VIDEO_URL + " text ,"
            +DBConfiguration.VideoConfiguration.VIDEO_TITLE + " text ,"
            +DBConfiguration.VideoConfiguration.VIDEO_TITLE_PINYING + " text,"
            +DBConfiguration.VideoConfiguration.VIDEO_FOLDER_URL+" text "
            +")";

    public MediaDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        LogUtil.i(TAG, "-------------->onCreate()");
        db.execSQL(CREATE_MUSIC_TABLE);
        db.execSQL(CREATE_PICTURE_TABLE);
        db.execSQL(CREATE_VIDEO_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LogUtil.i(TAG, "-------------->onUpgrade()");
        db.execSQL("DROP TABLE IF EXISTS "+DBConfiguration.MusicConfiguration.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ DBConfiguration.VideoConfiguration.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBConfiguration.PictureConfiguration.TABLE_NAME);
        onCreate(db);
    }

}
