package com.semisky.automultimedia.common.mediascan;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.semisky.automultimedia.MediaApplication;
import com.semisky.automultimedia.R;
import com.semisky.automultimedia.common.bean.MusicInfo;
import com.semisky.automultimedia.common.bean.VideoInfo;
import com.semisky.automultimedia.common.sql.DBConfiguration;
import com.semisky.automultimedia.common.utils.EncodingUtil;
import com.semisky.automultimedia.common.utils.HanziToPinyinUtil;
import com.semisky.automultimedia.common.utils.LogUtil;

import java.io.File;
import java.lang.reflect.Method;


/**
 * Created by 熊俊 on 2018/1/3.
 * 文件信息解析类
 */

public class MediaParaser {

    private static final String TAG = "MediaParaser";
    private static volatile MediaParaser instance;

    public static MediaParaser getInstance() {
        if (instance == null) {
            synchronized (MediaParaser.class) {
                if (instance == null) {
                    instance = new MediaParaser();
                }
            }
        }
        return instance;
    }

    /**
     * 解析图片
     *
     * @param url
     * @return
     */
    public ContentValues parserPictureInfo(String url) {
        ContentValues contentValues = null;
        String pictureUrl = null;
        String pictureFolderUrl = null;

        if (url == null) {
            return null;
        }

        try {
            pictureUrl = url;
            pictureFolderUrl = "NA"/*url.substring(0 , url.lastIndexOf(File.separator))*/;
            contentValues = new ContentValues();
            String videoTitle = splitLastSeparatorString(url).trim();
            String videoTitlePinYing = HanziToPinyinUtil.getIntance().getSortKey(videoTitle, HanziToPinyinUtil.FullNameStyle.CHINESE);

            contentValues.put(DBConfiguration.PictureConfiguration.USB_FLAG, DBConfiguration.USB_FLAG);
            contentValues.put(DBConfiguration.PictureConfiguration.PICTURE_URL, pictureUrl);
            contentValues.put(DBConfiguration.PictureConfiguration.PICTURE_TITLE, videoTitle);
            contentValues.put(DBConfiguration.PictureConfiguration.PICTURE_TITLE_PINYING, videoTitlePinYing);
            contentValues.put(DBConfiguration.PictureConfiguration.PICTURE_FOLDER_URL, pictureFolderUrl);
        } catch (Exception e) {
            LogUtil.e(TAG + ": parserPictureInfo() fali !!! filePath: " + url);
        }
        return contentValues;
    }

    // 分离出字符串最后斜杠后的字符串
    private String splitLastSeparatorString(String str) {
        String newStr = str.substring(str.lastIndexOf(File.separator) + 1);
        return newStr;
    }


    //****************************************************** video ************************************************************************//
    private OnParseVideoInfoListener mOnParseVideoInfoListener;

    /**
     * 解析视频
     *
     * @param filePath
     * @return
     */
    public ContentValues parserVideoInfo(String filePath) {
        ContentValues contentValues = null;
        String videoFolderUrl = null;

        if (filePath == null) {
            return null;
        }

        try {
            contentValues = new ContentValues();
            videoFolderUrl = "NA"/*
                                 * filePath.substring(0,
								 * filePath.lastIndexOf(File.separator))
								 */;
            String videoTitle = splitLastSeparatorString(filePath);
            String videoTitlePinYing = HanziToPinyinUtil.getIntance().getSortKey(videoTitle, HanziToPinyinUtil.FullNameStyle.CHINESE);
            videoTitlePinYing = videoTitlePinYing.toLowerCase();// 将所有的视频名称拼音转换为小写,便于排序（因字母大写与小写ASCII码是不一样的,会导致排序错误）
            contentValues.put(DBConfiguration.VideoConfiguration.USB_FLAG, DBConfiguration.USB_FLAG);
            contentValues.put(DBConfiguration.VideoConfiguration.VIDEO_URL, filePath);
            contentValues.put(DBConfiguration.VideoConfiguration.VIDEO_TITLE, videoTitle);
            contentValues.put(DBConfiguration.VideoConfiguration.VIDEO_TITLE_PINYING, videoTitlePinYing);
            contentValues.put(DBConfiguration.VideoConfiguration.VIDEO_FOLDER_URL, videoFolderUrl);
        } catch (Exception e) {
            LogUtil.d(TAG + ": parserVideoInfo() fail, filePath : " + filePath);
        }

        return contentValues;
    }


    /**
     * 解析视频Url Metadata信息，视频 高、宽、时长等
     */
    public VideoInfo parserMetadataByVideoUrl(String videoUrl) {
        VideoInfo mVideoInfo = null;

        if (!new File(videoUrl).exists()) {
            LogUtil.e(TAG, "-----------------------视频文件不存在");
            // 文件不存在回调
            if (mOnParseVideoInfoListener != null) {
                mOnParseVideoInfoListener.onParseVideoFileNotExist(videoUrl);
            }
            return mVideoInfo;
        }

        LogUtil.i(TAG, "-----------------------视频解析开始");
        // 解析开始回调
        if (mOnParseVideoInfoListener != null) {
            mOnParseVideoInfoListener.onParseVideoInfoStart();
        }
        // 音视频解析工具对象
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try {
            // 设置解析源
            mmr.setDataSource(videoUrl);
        } catch (Exception e) {
            LogUtil.e(TAG, "-----------------------视频文件源有问题");
            LogUtil.e(TAG, "MediaMetadataRetriever.setDataSource() error !!!");
            // 解析出错回调
            if (mOnParseVideoInfoListener != null) {
                mOnParseVideoInfoListener.onParseVideoInfoError(videoUrl);
            }
            safeCloseMediaMetadataRetriever(mmr);
            return mVideoInfo;
        }

        mVideoInfo = new VideoInfo();
        // url
        mVideoInfo.setVideoUrl(videoUrl);
        // displayName
        mVideoInfo.setVideoDisplayName(videoUrl.substring(videoUrl.lastIndexOf(File.separator) + 1));
        // title
        mVideoInfo.setVideoTitle(mVideoInfo.getVideoDisplayName().substring(0,
                mVideoInfo.getVideoDisplayName().lastIndexOf(".")));
        // duration
        try {
            mVideoInfo.setVideoDuration(Integer.valueOf(mmr
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));
        } catch (Exception e) {
            LogUtil.e(TAG, "-----------------------解析视频时长出错");
            mVideoInfo = null;
            // 解析出错回调
            if (mOnParseVideoInfoListener != null) {
                mOnParseVideoInfoListener.onParseVideoInfoError(videoUrl);
            }
            safeCloseMediaMetadataRetriever(mmr);
            return mVideoInfo;
        }

        try {

            // width
            mVideoInfo.setVideoWidth(Integer.valueOf(getDefaultEncodeString(mmr
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))));
            // height
            mVideoInfo.setVideoHeight(Integer.valueOf(getDefaultEncodeString(mmr
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))));
        } catch (Exception e) {
            LogUtil.e(TAG, "-----------------------解析视频宽高出错");
            mVideoInfo.setVideoWidth(800);
            mVideoInfo.setVideoHeight(480);
            //            mVideoInfo = null;
            //            // 解析出错回调
            //            if (mOnParseVideoInfoListener != null) {
            //                mOnParseVideoInfoListener.onParseVideoInfoError(videoUrl);
            //            }
            //            safeCloseMediaMetadataRetriever(mmr);
            //            return mVideoInfo;
        }

        LogUtil.d(TAG, "-----------------------视频解析完成");
        // 解析成功回调
        if (mOnParseVideoInfoListener != null) {
            mOnParseVideoInfoListener.onParseVideoInfoComplete(mVideoInfo);
        }

        safeCloseMediaMetadataRetriever(mmr);
        return mVideoInfo;
    }

    /**
     * 将乱码转换为正常显示信息
     *
     * @param string
     * @return
     */
    private String getDefaultEncodeString(String string) {
        // android.os.ProtocolManager.getInstance().getDefaultEncodeString(string);
        try {
            Class<?> clzProtocolManager = Class.forName("android.os.ProtocolManager");
            Method instance = clzProtocolManager.getDeclaredMethod("getInstance");
            Object objclzProtocolManager = instance.invoke(clzProtocolManager);

            Method getDefaultEncodeStringMethod = clzProtocolManager.getDeclaredMethod("getDefaultEncodeString",
                    String.class);
            String encodeString = (String) getDefaultEncodeStringMethod.invoke(objclzProtocolManager,
                    new Object[]{string});

            return encodeString;
        } catch (Exception e) {
            return string;
        }
    }

    public interface OnParseVideoInfoListener {
        // 开始解析
        void onParseVideoInfoStart();

        // 解析完成
        void onParseVideoInfoComplete(VideoInfo info);

        // 解析错误
        void onParseVideoInfoError(String url);

        // 文件不存在
        void onParseVideoFileNotExist(String url);
    }

    /**
     * 注册视频解析监听
     */
    public void registerOnParseVideoInfoListener(OnParseVideoInfoListener listener) {
        this.mOnParseVideoInfoListener = listener;
    }


    //*********************************************************** Music ***********************************************************//


    public synchronized MusicInfo parseMusic(String filePath) {
        try {
            if (null == filePath) {
                LogUtil.e(TAG, "parseMusic() null == filePath !!!");
                return null;
            }
            ContentValues values = parserMusic(filePath);
            MusicInfo musicInfo = null;
            if (null != values) {
                musicInfo = new MusicInfo();
                String title = values.getAsString(DBConfiguration.MusicConfiguration.MUSIC_TITLE);
                musicInfo.setTitle(title);
                musicInfo.setTitlePinYing(values.getAsString(DBConfiguration.MusicConfiguration.MUSIC_TITLE_PINYING));
                musicInfo.setDisplayName(title.substring(0, title.lastIndexOf(".")));
                musicInfo.setArtist(values.getAsString(DBConfiguration.MusicConfiguration.MUSIC_ARTIST));
                musicInfo.setArtistPinYing(values.getAsString(DBConfiguration.MusicConfiguration.MUSIC_ARTIST_PINYING));
                //            musicInfo.setAlbumPicture(values.getAsByteArray(DBConfiguration.MusicConfiguration.EMBEDDED_PICTURE));
                musicInfo.setDuration(values.getAsInteger(DBConfiguration.MusicConfiguration.MUSIC_DURATION));
                musicInfo.setUrl(values.getAsString(DBConfiguration.MusicConfiguration.MUSIC_URL));
                musicInfo.setAlbum(values.getAsString(DBConfiguration.MusicConfiguration.MUSIC_ALBUM));
                return musicInfo;
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "parseMusic FAIL !!!!");
            e.printStackTrace();

        }
        LogUtil.e(TAG, "parseMusic() MusicInfo == null !!!");
        return null;
    }

    /**
     * 解析音乐详细信息：歌手/时常等
     *
     * @param filePath
     * @return
     */
    public synchronized ContentValues parserMusic(String filePath) {
        ContentValues contentValues = null;
        MediaMetadataRetriever mmr = null;

        String musicUrl = "Unknown";
        String musicFolderUrl = "Unknown";
        String musicTitle = "Unknown";
        String musicTitlePinYing = "Unknown";
        String musicArtist = "Unknown";
        String musicArtistPinYing = "";
        String musicAlbum = "Unknown";
        int musicDuration = 0;

        // 实例化媒体解析类
        mmr = new MediaMetadataRetriever();

        try {
            if (new File(filePath).exists()) {
                mmr.setDataSource(filePath);
            } else {
                LogUtil.e(TAG, "parserMusic() URL NO EXISTS !!! " + filePath);
                safeCloseMediaMetadataRetriever(mmr);
                return null;
            }
        } catch (Exception e) {
            LogUtil.e(TAG + ": MediaScanner:setDataSource() error !!! url: " + filePath);
            safeCloseMediaMetadataRetriever(mmr);
            return null;
        }

        // duration
        String sDuration = "0";

        try {
            sDuration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        } catch (Exception e) {
            LogUtil.e(TAG, "Get Total time fail , url: " + filePath);
        }
        try {
            musicDuration = Integer.valueOf((sDuration != null ? sDuration : "0"));// 时长有可能是乱码，防止类型转换异常
        } catch (Exception e) {
            musicDuration = 0;
            LogUtil.e(TAG, "Total time format fail , sDuration: " + sDuration + ", url: " + filePath);
        }
        if (musicDuration == 0) {
            musicDuration = 0;
        }

        // url
        musicUrl = filePath;
        // folder url
        musicFolderUrl = musicUrl.substring(0, musicUrl.lastIndexOf(File.separator));

        // title
        // 获得带后缀的名字
        musicTitle = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
        // title pinying
        // 获得带后缀名字拼音
        try {
            musicTitlePinYing = HanziToPinyinUtil.getIntance().getSortKey(musicTitle, HanziToPinyinUtil.FullNameStyle.CHINESE);
        } catch (Exception e2) {
            e2.printStackTrace();
        }

        // 获得歌手名字
        try {
            musicArtist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            if (null != musicArtist) {
                EncodingUtil encodingUtil = new EncodingUtil();
                musicArtist = encodingUtil.getEncodeString(musicArtist, null);
            }
        } catch (Exception e1) {
            LogUtil.e(TAG, "Get artist fail !!! , url=" + filePath);
        }
        if (musicArtist == null || musicArtist != null && musicArtist.length() <= 0 || "".equals(musicArtist)) {
            musicArtist = "Unknown";
        }

        // 获取歌手拼音
        try {
            musicArtistPinYing = HanziToPinyinUtil.getIntance().getSortKey(musicArtist, HanziToPinyinUtil.FullNameStyle.CHINESE);
        } catch (Exception e) {
            e.printStackTrace();
        }


        // 获得Album
        try {
            musicAlbum = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            //add 2018-4-20
            if (null != musicAlbum) {
                EncodingUtil encodingUtil = new EncodingUtil();
                musicAlbum = encodingUtil.getEncodeString(musicAlbum, null);
            }
        } catch (Exception e1) {
            musicAlbum = null;
        }
        if (musicAlbum == null || musicAlbum != null && musicAlbum.length() <= 0) {
            musicAlbum = "Unknown";
        }

        //获取专辑封面
       /* byte[] art = new byte[0];
        try {
            art = mmr.getEmbeddedPicture();
        } catch (Exception e) {
            LogUtil.e("get getEmbeddedPicture failed!");
            safeCloseMediaMetadataRetriever(mmr);
        }*/

        safeCloseMediaMetadataRetriever(mmr);

        contentValues = new ContentValues();
        contentValues.put(DBConfiguration.MusicConfiguration.USB_FLAG, DBConfiguration.USB_FLAG);
        contentValues.put(DBConfiguration.MusicConfiguration.MUSIC_URL, musicUrl);
        contentValues.put(DBConfiguration.MusicConfiguration.MUSIC_FOLDER_URL, musicFolderUrl);
        contentValues.put(DBConfiguration.MusicConfiguration.MUSIC_TITLE, musicTitle);
        contentValues.put(DBConfiguration.MusicConfiguration.MUSIC_TITLE_PINYING, musicTitlePinYing);
        contentValues.put(DBConfiguration.MusicConfiguration.MUSIC_ARTIST, musicArtist);
        //        contentValues.put(DBConfiguration.MusicConfiguration.EMBEDDED_PICTURE, art);
        contentValues.put(DBConfiguration.MusicConfiguration.MUSIC_ARTIST_PINYING, musicArtistPinYing);
        contentValues.put(DBConfiguration.MusicConfiguration.MUSIC_ALBUM, musicAlbum);
        contentValues.put(DBConfiguration.MusicConfiguration.MUSIC_DURATION, musicDuration);
        return contentValues;
    }

    private void safeCloseMediaMetadataRetriever(MediaMetadataRetriever mmr) {
        if (null != mmr) {
            try {
                mmr.release();
                mmr = null;
            } catch (Exception e) {
                LogUtil.w(TAG, "----------------------safeCloseMediaMetadataRetriever()");
            }
        }
    }

    private String mAlbumFlag;// 标识符，防错乱

    private void setmAlbumFlag(String albumUrlFlag) {
        this.mAlbumFlag = albumUrlFlag;
    }

    public String getmAlbumFlag() {
        return this.mAlbumFlag;
    }

    public void setAlbumImage(final ImageView imageView, final String musicUrl) {

        setmAlbumFlag(musicUrl);
        new AsyncTask<String, Void, byte[]>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                LogUtil.i(TAG, "----------开始获取专辑图片");
            }

            @Override
            protected byte[] doInBackground(String... strings) {
                return getAlbumBitmap(musicUrl);
            }

            @Override
            protected void onPostExecute(byte[] byteImg) {
                super.onPostExecute(byteImg);
                if (!TextUtils.isEmpty(getmAlbumFlag()) && getmAlbumFlag().equals(musicUrl)) {
                    if (byteImg == null) {
                        LogUtil.i(TAG, "----------无法获取专辑图片");
                        imageView.setImageResource(R.drawable.background_song_album_thumb);
                    } else {
                        LogUtil.i(TAG, "----------获取专辑图片成功");
                        try {
                            Glide.with(MediaApplication.getContext())
                                    .load(byteImg)
                                    .error(R.drawable.background_song_album_thumb)
                                    .override(337, 350)
                                    .centerCrop()
                                    .into(imageView);
                        } catch (Exception e) {
                            LogUtil.e(TAG, "setAlbumImage() fail !!!");
                        }
                    }
                }

            }
        }.execute(musicUrl);
    }

    /**
     * 获取专辑图片Bitmap
     *
     * @param musicUrl
     * @return
     */
    private byte[] getAlbumBitmap(String musicUrl) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try {

            mmr.setDataSource(musicUrl);
            byte[] art = mmr.getEmbeddedPicture();
            if (null != art) {
                return art/*BitmapFactory.decodeByteArray(art,0,art.length)*/;
            }
        } catch (Exception e) {
            return null;
        } finally {
            try {
                mmr.release();
                mmr = null;
            } catch (Exception e) {
                LogUtil.e(TAG, "---------------------getAlbumBitmap() FAIL !!!!");
                return null;
            }
        }
        return null;
    }


}
