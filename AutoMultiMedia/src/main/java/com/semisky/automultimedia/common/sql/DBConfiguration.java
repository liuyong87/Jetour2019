package com.semisky.automultimedia.common.sql;

import android.provider.BaseColumns;

/**
 * Created by 熊俊 on 2018/1/2.
 */

public class DBConfiguration {
    public static final String DATABASE_NAME = "AJ_Multimedia.db";
    public static final int DATABASE_VERSION = 3;
    public static final String USB_FLAG = "mediaFlag";

    // 文件类型
    public static final String FILE_TYPE = "typeFile";
    public static final int FLAG_MUSIC = 0;
    public static final int FLAG_VIDEO = 1;
    public static final int FLAG_PHOTO = 2;

    /** 图片数据库配置 */
    public static class PictureConfiguration implements BaseColumns {
        public static final String TABLE_NAME = "pictures";// 表名
        public static final String USB_FLAG = "usbFlag";// U盘标识
        public static final String PICTURE_URL = "pictureUrl";// 图片路径
        public static final String PICTURE_TITLE = "pictureTitle";// 图片名字
        public static final String PICTURE_TITLE_PINYING = "pictureTitlePinYing";// 图片名字全拼
        public static final String PICTURE_FOLDER_URL = "pictureFolderUrl";// 所属文件夹路径
        public static final String DEFAULT_SORT_ORDER = PICTURE_TITLE_PINYING
                + " COLLATE LOCALIZED ASC";// 排序方式
    }

    /** 音乐文件夹数据库配置 */
    public static class MusicFolderConfiguration implements BaseColumns {
        public static final String TABLE_NAME = "musicsFolder";// 表名
        public static final String USB_FLAG = "usbFlag";// U盘标识
        public static final String FOLDER_URL = "folderUrl";// 文件夹路径
        public static final String FOLDER_NAME = "folderName";//文件夹名字
    }

    /** 音乐数据库配置 */
    public static class MusicConfiguration implements BaseColumns {
        public static final String TABLE_NAME = "musics";// 表名
        public static final String USB_FLAG = "usbFlag";// U盘标识
        public static final String MUSIC_URL = "musicUrl";// 音乐路径
        public static final String MUSIC_TITLE = "musicTitle";//音乐名字
        public static final String MUSIC_TITLE_PINYING = "musicTitlePinYing";//音乐名字全拼
        public static final String MUSIC_ARTIST = "musicArtist";//歌手
        public static final String MUSIC_ARTIST_PINYING= "musicArtistPinYing";
        public static final String MUSIC_ALBUM = "musicAlbum";//专辑
        public static final String MUSIC_DURATION = "musicDuration";//总时长
        public static final String  EMBEDDED_PICTURE = "EmbeddedPicture";//专辑封面图片
        public static final String MUSIC_FOLDER_URL = "musicFolderUrl";// 所属文件夹路径
        public static final String DEFAULT_SORT_ORDER = MUSIC_TITLE_PINYING
                + " COLLATE LOCALIZED ASC";// 排序方式
        public static final String ARTIST_DEFAULT_SORT_ORDER = MUSIC_ARTIST_PINYING
                + " COLLATE LOCALIZED ASC";// 歌手默认排序方式
    }

    /** 视频数据库配置 */
    public static class VideoConfiguration implements BaseColumns {
        public static final String TABLE_NAME = "videos";// 表名
        public static final String USB_FLAG = "usbFlag";// U盘标识
        public static final String VIDEO_URL = "videoUrl";// 音乐路径
        public static final String VIDEO_TITLE = "videoTitle";// 视频名字
        public static final String VIDEO_TITLE_PINYING = "videoTitlePinYing";// 视频名字全拼
        public static final String VIDEO_FOLDER_URL = "videoFolderUrl";// 所属文件夹路径
        public static final String DEFAULT_SORT_ORDER = VIDEO_TITLE_PINYING
                + " COLLATE LOCALIZED ASC";// 排序方式
    }
}
