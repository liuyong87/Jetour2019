package com.semisky.automultimedia.common.data;

import android.content.Context;
import android.database.Cursor;

import com.semisky.automultimedia.MediaApplication;
import com.semisky.automultimedia.common.bean.MusicInfo;
import com.semisky.automultimedia.common.interfaces.OnVoiceSearchInfoListener;
import com.semisky.automultimedia.common.sql.DBConfiguration;
import com.semisky.automultimedia.common.sql.MediaDBManager;
import com.semisky.automultimedia.common.sql.SharePreferenceUtil;
import com.semisky.automultimedia.common.utils.LogUtil;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyong on 18-3-20.
 */

public class MusicDataModule {
    private static final String TAG = MusicDataModule.class.getSimpleName();
    private OnLoadDataListener mCurrentListener = null;


    private static MusicDataModule INSTACE;
    /**
     * 音乐文件夹名字集合<br/>
     * 1.只音乐列表用到
     */
    private List<String> mAllFolderList;
    /**
     * 所有音乐<p>
     * 1.音乐服务会用到<br/>
     * 2.音乐列表会用到<br/>
     */
    private List<MusicInfo> mAllMusicList;


    /**
     * 音乐列表文件夹集合使用数据缓存<br/>
     * 1.只音乐列表用到
     */
    private Map<String, List<MusicInfo>> mFolderUnderFileMap;

    private MusicDataModule() {
//        initHandlerThread();
    }

    public static MusicDataModule getInstance() {
        if (null == INSTACE) {
            INSTACE = new MusicDataModule();
        }
        return INSTACE;
    }

    public void destroy() {
        LogUtil.i(TAG, "destroy()...");
//        closeHandlerThread();
        if (null != mFolderUnderFileMap) {
            mFolderUnderFileMap.clear();
            mFolderUnderFileMap = null;
        }
        if (null != mAllFolderList) {
            mAllFolderList.clear();
            mAllFolderList = null;
        }
        if (null != mAllMusicList) {
            mAllMusicList.clear();
            mAllMusicList = null;
        }
        if (null != mFolderUnderFileMap) {
            mFolderUnderFileMap.clear();
            mFolderUnderFileMap = null;
        }
        if (listeners != null) {
            listeners.clear();
            listeners = null;
        }
        INSTACE = null;
    }

    // All songs
    public List<MusicInfo> getSongs() {
        if (null == mAllMusicList) {
            this.mAllMusicList = new ArrayList<MusicInfo>();
        }
        return this.mAllMusicList;
    }

    // 获取当前播放音乐URL
    public String getCurrentPlayMusicUrl() {
        return SharePreferenceUtil.getCurrentPlayingMusicUrl(getContext());
    }

    // 上下文
    private Context getContext() {
        return MediaApplication.getContext();
    }

    // 加载所有音乐文件
    public void loadAllSongs() {
        new Thread(new LoadDataRunable(this, LoadDataRunable.MSG_GET_ALL_SONGS)).start();
    }

    public void loadAllSongs(OnLoadDataListener listener) {
        new Thread(new LoadDataRunable(this, listener, LoadDataRunable.MSG_GET_ALL_SONGS)).start();
    }

    // 加载所有文件夹
    public void loadAllFolder() {
        new Thread(new LoadDataRunable(this, LoadDataRunable.MSG_GET_ALL_FOLDER)).start();
    }

    // 获取文件夹对应音乐文件集合
    public void loadFolderUnderFile( String name) {
        new Thread(new LoadDataRunable(this, LoadDataRunable.MSG_GET_FOLDER_UNDER_FILE, name)).start();
    }

    // 根据语音歌手名关键字模糊匹配歌曲
    public void voiceFuzzyMatchArtistUnderSongs(String args) {
        new Thread(new LoadDataRunable(this, LoadDataRunable.MSG_FUZZY_MATCH_ARTIS_UNDER_SONG, args)).start();
    }

    // 根据语音album名关键字模糊匹配歌曲
    public void voiceFuzzyMatchAlbumUnderSongs(String args) {
        LogUtil.i(TAG, "voiceFuzzyMatchAlbumUnderSongs() args=" + args);
        new Thread(new LoadDataRunable(this, LoadDataRunable.MSG_FUZZY_MATCH_ALBUM_UNDER_SONG, args)).start();
    }

    // 根据语音歌曲名关键字模糊匹配歌曲
    public void voiceFuzzyMatchSongs(String args) {
        new Thread(new LoadDataRunable(this, LoadDataRunable.MSG_FUZZY_MATCH_SONG, args)).start();
    }

    // 根据语音歌曲名和歌手名关键字模糊匹配歌曲
    public void voiceFuzzyMatchArtistAndSong(String artistKeywords, String songKeywords) {
        new Thread(new LoadDataRunable(this, LoadDataRunable.MSG_FUZZY_MATCH_ARTIS_AND_SONG, artistKeywords, songKeywords)).start();
    }

    protected static class LoadDataRunable implements Runnable {
        WeakReference<MusicDataModule> weakReference;
        private OnLoadDataListener mOnLoadDataListener;
        public static final int MSG_GET_ALL_SONGS = 10;
        public static final int MSG_GET_ALL_FOLDER = 11;
        public static final int MSG_GET_FOLDER_UNDER_FILE = 12;
        public static final int MSG_FUZZY_MATCH_ARTIS_UNDER_SONG = 13;
        public static final int MSG_FUZZY_MATCH_ALBUM_UNDER_SONG = 16;
        public static final int MSG_FUZZY_MATCH_SONG = 14;
        public static final int MSG_FUZZY_MATCH_ARTIS_AND_SONG = 15;

        public int mMessagFlag;
        public String mArgs;
        public String mArgs2;


        LoadDataRunable(MusicDataModule module, OnLoadDataListener listener, int msgFlag) {
            this(module, msgFlag);
            this.mOnLoadDataListener = listener;
        }

        LoadDataRunable(MusicDataModule module, int msgFlag) {
            this(module, msgFlag, null);
        }

        LoadDataRunable(MusicDataModule module, int msgFlag, String arg) {
            this(module, msgFlag, arg, null);
        }

        LoadDataRunable(MusicDataModule module, int msgFlag, String agr, String arg2) {
            weakReference = new WeakReference<MusicDataModule>(module);
            this.mMessagFlag = msgFlag;
            this.mArgs = agr;
            this.mArgs2 = arg2;
        }

        @Override
        public void run() {

            switch (mMessagFlag) {
                case MSG_GET_ALL_SONGS:
                    if (null == weakReference.get().mAllMusicList || weakReference.get().mAllMusicList.size() <= 0) {
                        weakReference.get().mAllMusicList = weakReference.get().queryAllMusicFile();
                    }

                    if (null != mOnLoadDataListener) {
                        mOnLoadDataListener.onAllMusicListResult(weakReference.get().mAllMusicList);
                    } else if (null != weakReference.get().mCurrentListener) {
                        weakReference.get().mCurrentListener.onAllMusicListResult(weakReference.get().mAllMusicList);
                    }
                       /* for (OnLoadDataListener listener : weakReference.get().listeners) {
                            if (listener.equals(weakReference.get().mCurrentListener)) {
                                listener.onAllMusicListResult(weakReference.get().mAllMusicList);
                            }
                        }*/
                    break;
                case MSG_GET_ALL_FOLDER:
                    if (null == weakReference.get().mFolderUnderFileMap) {
                        weakReference.get().mFolderUnderFileMap = weakReference.get().queryAllMusicFolderUnderFileList();
                    }

                    if (null != weakReference.get().mFolderUnderFileMap) {
                        weakReference.get().mAllFolderList = new ArrayList<String>();
                        weakReference.get().mAllFolderList.add("All Songs");
                        for (Map.Entry<String, List<MusicInfo>> entry : weakReference.get().mFolderUnderFileMap.entrySet()) {
                            weakReference.get().mAllFolderList.add(entry.getKey());
                        }
                    }

                    if (null != weakReference.get().mAllFolderList) {
                        for (OnLoadDataListener listener : weakReference.get().listeners) {
                            if (listener.equals(weakReference.get().mCurrentListener)) {
                                listener.onAllFolderListResult(weakReference.get().mAllFolderList);
                            }
                        }
                    }
                    break;
                case MSG_GET_FOLDER_UNDER_FILE:
                    LogUtil.i(TAG, "LoadDataRunable->MSG_GET_FOLDER_UNDER_FILE");
                    if (null != weakReference.get().mFolderUnderFileMap) {
                        List<MusicInfo> musicList = null;
                        boolean isContainsKey = weakReference.get().mFolderUnderFileMap.containsKey(mArgs);
                        LogUtil.i(TAG, "isContainsKey=" + isContainsKey);
                        if (isContainsKey) {
                            musicList = weakReference.get().mFolderUnderFileMap.get(mArgs);
                        } else if ("All Songs".equals(mArgs)) {// 所有歌曲文件夹歌曲
                            if (null == weakReference.get().mAllMusicList) {
                                musicList = weakReference.get().queryAllMusicFile();
                                weakReference.get().mAllMusicList.addAll(musicList);
                            } else {
                                musicList = weakReference.get().mAllMusicList;
                            }
                        }

                        for (OnLoadDataListener listener : weakReference.get().listeners) {
                            if (listener.equals(weakReference.get().mCurrentListener)) {
                                listener.onFolderUnderFileListResult(musicList);
                            }
                        }
                    }
                    break;
                case MSG_FUZZY_MATCH_ARTIS_UNDER_SONG:
                    LogUtil.i(TAG, "MSG_SEARCH_ARTIS_UNDER_SONG agrs=" + mArgs);
                    MusicInfo info = weakReference.get().getFuzzyMatchArtistUnderSong(mArgs);
                    weakReference.get().notifySearchResult(info);
                    break;
                case MSG_FUZZY_MATCH_SONG:
                    LogUtil.i(TAG, "MSG_FUZZY_MATCH_SONG agrs=" + mArgs);
                    MusicInfo info2 = weakReference.get().getFuzzyMatchSong(mArgs);
                    weakReference.get().notifySearchResult(info2);
                    break;
                case MSG_FUZZY_MATCH_ARTIS_AND_SONG:
                    LogUtil.i(TAG, "MSG_FUZZY_MATCH_ARTIS_AND_SONG mArgs=" + mArgs + " , mArgs2=" + mArgs2);
                    MusicInfo info3 = weakReference.get().getFuzzyMatchArtisAndSong(mArgs, mArgs2);
                    weakReference.get().notifySearchResult(info3);
                    break;
                case MSG_FUZZY_MATCH_ALBUM_UNDER_SONG:
                    LogUtil.i(TAG, "MSG_FUZZY_MATCH_ALBUM_UNDER_SONG agrs=" + mArgs);
                    MusicInfo info4 = weakReference.get().getFuzzyMatchAlbumUnderSong(mArgs);
                    weakReference.get().notifySearchResult(info4);
                    break;
            }

        }
    }

    /**
     * 模糊匹配指定歌手和歌曲
     */
    public MusicInfo getFuzzyMatchArtisAndSong(String artistKeywords, String songKeywords) {
        LogUtil.i(TAG, "getFuzzyMatchArtisAndSong() artistKeywords=" + artistKeywords + " , songKeywords=" + songKeywords);
        Cursor cursor = MediaDBManager.getInstance(getContext()).queryLikeAtistAndSong(artistKeywords, songKeywords);
        LogUtil.i(TAG, "getFuzzyMatchArtisAndSong() cursor.getCount=" + (cursor != null ? cursor.getCount() : 0));
        if (null != cursor && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                return getMusicInfoFromCursor(cursor);
            }
        }
        return null;
    }

    /**
     * 模糊匹配歌曲
     *
     * @return
     */
    public MusicInfo getFuzzyMatchSong(String songKeywords) {
        LogUtil.i(TAG, "getFuzzyMatchSong() songKeywords=" + songKeywords);
        Cursor cursor = MediaDBManager.getInstance(getContext()).queryLikeSongs(songKeywords);
        LogUtil.i(TAG, "getFuzzyMatchSong() cursor.getCount=" + (cursor != null ? cursor.getCount() : 0));

        if (null != cursor && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                return getMusicInfoFromCursor(cursor);
            }
        }
        return null;
    }

    /**
     * 根据歌手查找歌曲URL
     *
     * @param atistKeywords
     * @return
     */
    public MusicInfo getFuzzyMatchArtistUnderSong(String atistKeywords) {
        LogUtil.i(TAG, "getFuzzyMatchArtistUnderSong() atistKeywords=" + atistKeywords);
        Cursor cursor = MediaDBManager.getInstance(getContext()).queryLikeArtistUnderSongs(atistKeywords);
        LogUtil.i(TAG, "getFuzzyMatchArtistUnderSong() cursor.getCount=" + (cursor != null ? cursor.getCount() : 0));

        if (null != cursor && cursor.getCount() > 0) {

            while (cursor.moveToNext()) {
                return getMusicInfoFromCursor(cursor);
            }
        }

        return null;
    }

    /**
     * 根据专辑查找歌曲URL
     *
     * @param albumKeywords
     * @return
     */
    public MusicInfo getFuzzyMatchAlbumUnderSong(String albumKeywords) {
        LogUtil.i(TAG, "getFuzzyMatchAlbumUnderSong() albumKeywords=" + albumKeywords);
        Cursor cursor = MediaDBManager.getInstance(getContext()).queryLikeAlbumUnderSongs(albumKeywords);
        LogUtil.i(TAG, "getFuzzyMatchAlbumUnderSong() cursor.getCount=" + (cursor != null ? cursor.getCount() : 0));

        if (null != cursor && cursor.getCount() > 0) {

            while (cursor.moveToNext()) {
                return getMusicInfoFromCursor(cursor);
            }
        }

        return null;
    }

    /**
     * 查询所有歌曲
     *
     * @return
     */
    private List<MusicInfo> queryAllMusicFile() {
        List<MusicInfo> musicInfoList = new ArrayList<MusicInfo>();
        // 查询所有音乐信息
        Cursor cursor = MediaDBManager.getInstance(getContext()).queryAllMusicInfo();

        if (null != cursor && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                MusicInfo musicInfo = getMusicInfoFromCursor(cursor);
                if (null != musicInfo) {
                    musicInfoList.add(musicInfo);
                }
            }
        }
        return musicInfoList;
    }


    /**
     * 查询所有文件夹下的音乐文件集合
     */
    private Map<String, List<MusicInfo>> queryAllMusicFolderUnderFileList() {
        MusicInfo musicInfo;
        List<MusicInfo> musicInfoList;
        Map<String, List<MusicInfo>> folderMap = new HashMap<String, List<MusicInfo>>();// 创始存储对应文件夹

        // 查询所有音乐信息
        Cursor cursor = MediaDBManager.getInstance(getContext()).queryAllMusicInfo();

        if (null != cursor && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {

                musicInfo = getMusicInfoFromCursor(cursor);// 获取游标里的音乐信息
                if (null != musicInfo) {
                    //  获取音乐URL文件夹名字
                    String folderName = new File(musicInfo.getUrl()).getParentFile().getName();
                    if (folderMap.containsKey(folderName)) {
                        // 将歌曲添加存在的文件夹对应的集合
                        folderMap.get(folderName).add(musicInfo);
                    } else {
                        // 创建新的集合存储文件夹下的所有歌曲
                        musicInfoList = new ArrayList<MusicInfo>();
                        musicInfoList.add(musicInfo);
                        folderMap.put(folderName, musicInfoList);
                    }
                }
            }
        }
        return folderMap;
    }

    /**
     * 通过指定游标获得音乐实体信息
     */
    private MusicInfo getMusicInfoFromCursor(Cursor cursor) {
        String url = cursor.getString(cursor.getColumnIndex(DBConfiguration.MusicConfiguration.MUSIC_URL));
        String title = cursor.getString(cursor.getColumnIndex(DBConfiguration.MusicConfiguration.MUSIC_TITLE));
        String titlePinYing = cursor.getString(cursor
                .getColumnIndex(DBConfiguration.MusicConfiguration.MUSIC_TITLE_PINYING));
        String displayName = title.substring(0, title.lastIndexOf("."));
        String art = cursor.getString(cursor.getColumnIndex(DBConfiguration.MusicConfiguration.MUSIC_ARTIST));
        String album =
                cursor.getString(cursor.getColumnIndex(DBConfiguration.MusicConfiguration.MUSIC_ALBUM));
       /* String album = "";
        byte[] albumPicture = cursor.getBlob(cursor.getColumnIndex(DBConfiguration.MusicConfiguration.EMBEDDED_PICTURE));*/
        int duration = cursor.getInt(cursor.getColumnIndex(DBConfiguration.MusicConfiguration.MUSIC_DURATION));
        MusicInfo musicInfo = new MusicInfo(url, displayName, title, titlePinYing, art, album, null, duration, true);
        return musicInfo;
    }


    public interface OnLoadDataListener {
        // 回调此方法时，返回所有音乐Folder集合
        void onAllFolderListResult(List<String> mFolderList);

        // 回调此方法时，返回所有音乐File集合
        void onAllMusicListResult(List<MusicInfo> musicList);

        // 回调此方法时，返回返回单个文件夹下音乐文件集合
        void onFolderUnderFileListResult(List<MusicInfo> musicList);
    }


    private List<OnLoadDataListener> listeners = new ArrayList<OnLoadDataListener>();

    public void registerOnLoadDataListener(OnLoadDataListener listener) {
        mCurrentListener = listener;
        if (null != listener) {
            LogUtil.i(TAG, "registerOnLoadDataListener()" + listeners.size());
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
    }

    public void unregisterOnLoadDataListener(OnLoadDataListener listener) {
        if (listener != null && listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    private OnVoiceSearchInfoListener mOnVoiceSearchInfoListener = null;

    public void registerOnVoiceSearchInfoListener(OnVoiceSearchInfoListener listener) {
        this.mOnVoiceSearchInfoListener = listener;
    }

    public void unregisterOnVoiceSearchInfoListener() {
        this.mOnVoiceSearchInfoListener = null;
    }

    private void notifySearchResult(Object result) {
        if (null != mOnVoiceSearchInfoListener) {
            mOnVoiceSearchInfoListener.onSearchResult(result);
        }
    }

}
