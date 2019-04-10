package com.semisky.automultimedia.common.strategys;

import android.os.IBinder;
import android.os.RemoteException;

import com.semisky.automultimedia.MediaApplication;
import com.semisky.automultimedia.common.utils.LogUtil;
import com.semisky.voicereceive.AppConstant;
import com.semisky.voicereceive.BinderPool;
import com.semisky.voicereceiveclient.IUSBMusicListener;
import com.semisky.voicereceiveclient.IUSBMusicPlay;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyong on 18-3-27.
 */

public class VoiceManager {
    private static final String TAG = VoiceManager.class.getSimpleName();
    private static VoiceManager INSTANCE;
    private List<OnVoiceControlListener> voiceControlListenerList;

    private VoiceManager() {
        voiceControlListenerList = new ArrayList<OnVoiceControlListener>();
    }

    public static VoiceManager getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new VoiceManager();
        }
        return INSTANCE;
    }

    public void onDestory() {

    }

    public static final int NO_INVLID = -1000;
    public static final int CMD_PREV = 0;// 上一个节目
    public static final int CMD_NEXT = 1;// 下一个节目
    public static final int CMD_START = 2;// 节目播放
    public static final int CMD_PAUSE = 3;// 节目暂停
    public static final int CMD_OPEN_LIST = 4;// 打开列表
    public static final int CMD_CHANGE_PLAY_MODE = 5;// 切换播放模式
    public static final int CMD_PLAY_APPOINT_ATIST = 6;// 播放歌手歌曲（索引列表下的第一首歌曲）
    public static final int CMD_PLAY_APPOINT_SONG = 7;// 播放指定歌曲名字
    public static final int CMD_PLAY_APPOINT_ATIST_AND_SONG = 8;// 播放指定歌手下歌曲名
    public static final int CMD_RESUME_PLAY = 9;// 未指定播放歌曲,相当于恢复播放，且到前台播放
    public static final int CMD_PLAY_APPOINT_ALBUM = 10;// 播放专辑歌曲(索引列表下的第一首歌曲)
    public static final int CMD_SONG_SEARCHED = 11;// 歌曲已搜索到

    public interface OnVoiceControlListener {
        void onCommandResult(int cmdCode);

        void onCommandResult(int cmdCode, Object[] agrs);
    }

    public void registerOnVoiceControlListener(OnVoiceControlListener linster) {
        if (null != linster && null != voiceControlListenerList && !voiceControlListenerList.contains(linster)) {
            voiceControlListenerList.add(linster);
        }
    }

    public void unregisterOnVoiceControlListener(OnVoiceControlListener linster) {
        if (null != linster && null != voiceControlListenerList && voiceControlListenerList.contains(linster)) {
            voiceControlListenerList.remove(linster);
        }
    }

    public void nofityCommandResult(int cmdCode) {
        if (null != voiceControlListenerList) {
            for (OnVoiceControlListener linster : voiceControlListenerList) {
                linster.onCommandResult(cmdCode);
            }
        }
    }

    public void nofityCommandResult(int cmdCode, Object[] agrs) {
        if (null != voiceControlListenerList) {
            for (OnVoiceControlListener linster : voiceControlListenerList) {
                linster.onCommandResult(cmdCode, agrs);
            }
        }
    }


    // Remote Voice interface


    private IUSBMusicPlay mUsbMusicPlaySevice = null;

    public void returnResult(int resultCode) {
        if (null != mUsbMusicPlaySevice) {
            try {
                mUsbMusicPlaySevice.resultCode(resultCode);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isBindRemoteVoiceService(){
        if(null != mUsbMusicPlaySevice){
            return true;
        }
        return false;
    }

    public void bindRemoteVoiceService() {
        LogUtil.i(TAG, "bindVoiceService() 01...");
        if (null == mUsbMusicPlaySevice) {
            BinderPool binderPool = BinderPool.getInstance(MediaApplication.getContext());
            IBinder binder = binderPool.queryBinder(AppConstant.USB_MUSIC_BINDER);
            mUsbMusicPlaySevice = IUSBMusicPlay.Stub.asInterface(binder);

        }

        if (null == mUsbMusicPlaySevice) {
            return;
        }
        try {
            mUsbMusicPlaySevice.setOnVoiceListener(new IUSBMusicListener.Stub() {

                @Override
                public int playResume() throws RemoteException {// 未指定播放歌曲
                    LogUtil.i(TAG, "===>playResume()");
                    nofityCommandResult(VoiceManager.CMD_RESUME_PLAY);
                    return 0;
                }

                @Override
                public int playByArtist(String s) throws RemoteException {//
                    LogUtil.i(TAG, "===>playByArtist()=" + s);
                    String[] agrs = {s};
                    nofityCommandResult(VoiceManager.CMD_PLAY_APPOINT_ATIST, agrs);
                    return 0;
                }

                @Override
                public int playBySong(String s) throws RemoteException {
                    LogUtil.i(TAG, "===>playBySong()=" + s);
                    String[] agrs = {s};
                    nofityCommandResult(VoiceManager.CMD_PLAY_APPOINT_SONG, agrs);
                    return 0;
                }

                @Override
                public int playByArtistAndSong(String s, String s1) throws RemoteException {
                    LogUtil.i(TAG, "===>playByArtistAndSong() s=" + s + ",s1=" + s1);
                    String[] agrs = {s, s1};
                    nofityCommandResult(VoiceManager.CMD_PLAY_APPOINT_ATIST_AND_SONG, agrs);
                    return 0;
                }

                @Override
                public int playByAlbum(String s) throws RemoteException {
                    LogUtil.i(TAG, "===>playByAlbum() s=" + s );
                    //CMD_PLAY_APPOINT_ALBUM
                    String[] agrs = {s};
                    nofityCommandResult(VoiceManager.CMD_PLAY_APPOINT_ALBUM, agrs);
                    return 0;
                }

                @Override
                public int changePlayOrder(int i) throws RemoteException {
                    LogUtil.i(TAG, "===>changePlayOrder()=" + i);
                    Integer[] args = {i};
                    nofityCommandResult(VoiceManager.CMD_CHANGE_PLAY_MODE, args);
                    return 0;
                }

                @Override
                public int pause() throws RemoteException {
                    LogUtil.i(TAG, "===>pause()");
                    nofityCommandResult(VoiceManager.CMD_PAUSE);
                    return 0;
                }

                @Override
                public int play() throws RemoteException {
                    LogUtil.i(TAG, "===>play()");
                    nofityCommandResult(VoiceManager.CMD_START);
                    return 0;
                }

                @Override
                public int lastProgram() throws RemoteException {
                    LogUtil.i(TAG, "===>lastProgram()");
                    nofityCommandResult(VoiceManager.CMD_PREV);
                    return 0;
                }

                @Override
                public int NextProgram() throws RemoteException {
                    LogUtil.i(TAG, "===>NextProgram()");
                    nofityCommandResult(VoiceManager.CMD_NEXT);
                    return 0;
                }

                @Override
                public int openMusicList() throws RemoteException {
                    LogUtil.i(TAG, "===>openMusicList()...");
                    nofityCommandResult(VoiceManager.CMD_OPEN_LIST);
                    return 0;
                }
            });
        } catch (Exception e) {
            e.getStackTrace();
            LogUtil.e(TAG, "IUSBMusicListener callback excption !!!!");
        }
    }

}
