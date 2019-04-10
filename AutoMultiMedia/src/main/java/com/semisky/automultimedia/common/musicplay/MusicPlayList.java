package com.semisky.automultimedia.common.musicplay;

import com.semisky.automultimedia.common.bean.MusicInfo;
import com.semisky.automultimedia.common.constant.Definition;
import com.semisky.automultimedia.common.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 音乐服务播放媒体清单
 * Created by LiuYong on 2018/9/7.
 */

public class MusicPlayList {
    private static final String TAG = "MusicPlayList";
    private int mPlayMode = PlayMode.getDefault();// 播放模式

    private static final int NO_POSITON = -1;
    private List<MusicInfo> mMusicList = new ArrayList<MusicInfo>();
    private int mCurrentIndex = NO_POSITON;// 当前播放媒体下标

    private int mNextIndex = NO_POSITON;// 下一曲播放媒体下标
    private String mPlayingMusicUrl = null;// 当前播放URL
    private MusicInfo mCurrentMusicInfo;// 当前播放歌曲ID3信息
    private MusicInfo mNextMusicInfo;// 当前播放歌曲ID3信息

    /**
     * 伴随播放模式刷新下个节目信息
     */
    public void refreshNextIndexWithPlayMode() {
        if (prepare()) {
            int size = getMusicList().size();
            switch (mPlayMode) {
                case Definition.MusicPlayModel.MODE_CIRCLE_ALL:
                    mNextIndex = mCurrentIndex + 1;
                    if(mNextIndex >= size){
                        mNextIndex = 0;
                    }
                    break;
                case Definition.MusicPlayModel.MODE_RANDOM:
                    mNextIndex = makeRandomPlayIndex();
                    break;
                case Definition.MusicPlayModel.MODE_CIRCLE_SINGL:
                    mNextIndex = mCurrentIndex;
                    break;
            }
            LogUtil.i(TAG, "=============");
            LogUtil.i(TAG, "refreshNextIndexWithPlayMode() mPlayMode=" + mPlayMode);
            LogUtil.i(TAG, "refreshNextIndexWithPlayMode() size=" + size);
            LogUtil.i(TAG, "refreshNextIndexWithPlayMode() mCurrentIndex=" + mCurrentIndex);
            LogUtil.i(TAG, "refreshNextIndexWithPlayMode() mNextIndex=" + mNextIndex);
            LogUtil.i(TAG, "=============");
            if (mNextIndex < getMusicList().size()) {
                mNextMusicInfo = getMusicList().get(mNextIndex);
            }
        }
    }

    /**
     * 获取下一曲ID3信息
     *
     * @return
     */
    public MusicInfo getmNextMusicInfo() {
        return this.mNextMusicInfo;
    }

    /**
     * 设置播放模式
     *
     * @param playMode
     */
    public void setmPlayMode(int playMode) {
        this.mPlayMode = playMode;
    }

    /**
     * 设置当前播放歌曲ID3信息
     *
     * @param musicInfo
     */
    public void setmCurrentMusicInfo(MusicInfo musicInfo) {
        this.mCurrentMusicInfo = musicInfo;
    }

    /**
     * 获取当前播放歌曲ID3信息
     *
     * @return
     */
    public MusicInfo getCurrentMusicInfo() {
        return this.mCurrentMusicInfo;
    }

    /**
     * 获取下一个节目名字
     *
     * @return
     */
    public String getNextSongName() {
        if (hasData()) {
            return getMusicList().get(mNextIndex).getDisplayName();
        }
        return null;
    }

    /**
     * 获取下一个节目URL
     */
    public String getNextProgramUrl() {
        if (hasData()) {
            onPrepareNextProgramIndex();
            mCurrentMusicInfo = getMusicList().get(this.mCurrentIndex);
            mNextMusicInfo = getMusicList().get(this.mNextIndex);
            mPlayingMusicUrl = mCurrentMusicInfo.getUrl();
            return mPlayingMusicUrl;
        }
        return null;
    }

    // 准备下一个节目下标
    private void onPrepareNextProgramIndex() {
        switch (mPlayMode) {
            case Definition.MusicPlayModel.MODE_CIRCLE_ALL:
                LogUtil.i(TAG, "==========Before Next===============");
                LogUtil.i(TAG, "onPrepareNextIndex() mCurrentIndex=" + mCurrentIndex);
                LogUtil.i(TAG, "onPrepareNextIndex() mNextIndex=" + mNextIndex);
                LogUtil.i(TAG, "==============================");
                int newNextIndex = mNextIndex + 1;

                if (mCurrentIndex == newNextIndex) {
                    int newCurrentIndex = mCurrentIndex + 1;
                    mCurrentIndex = newCurrentIndex >= getMusicList().size() ? 0 : newCurrentIndex;
                    newNextIndex = mCurrentIndex + 1;
                } else {
                    mCurrentIndex = mNextIndex;
                }

                if (newNextIndex >= getMusicList().size()) {
                    newNextIndex = 0;
                }
                mNextIndex = newNextIndex;
                LogUtil.i(TAG, "==========After Next===============");
                LogUtil.i(TAG, "onPrepareNextIndex() mCurrentIndex=" + mCurrentIndex);
                LogUtil.i(TAG, "onPrepareNextIndex() mNextIndex=" + mNextIndex);
                LogUtil.i(TAG, "==============================");
                break;
            case Definition.MusicPlayModel.MODE_RANDOM:
                mCurrentIndex = mNextIndex;
                mNextIndex = makeRandomPlayIndex();
                break;
            case Definition.MusicPlayModel.MODE_CIRCLE_SINGL:
                break;
        }
    }

    /**
     * 获取上一个节目URL
     */
    public String getPrevProgramUrl() {
        if (hasData()) {
            onPreparePrevProgramIndex();
            mCurrentMusicInfo = getMusicList().get(this.mCurrentIndex);
            mNextMusicInfo = getMusicList().get(this.mNextIndex);
            mPlayingMusicUrl = mCurrentMusicInfo.getUrl();
            return mPlayingMusicUrl;
        }
        return null;
    }

    // 准备上一个节目下标
    private void onPreparePrevProgramIndex() {
        switch (mPlayMode) {
            case Definition.MusicPlayModel.MODE_CIRCLE_ALL:
                LogUtil.i(TAG, "==========Before PREV===============");
                LogUtil.i(TAG, "onPreparePrevIndex() mCurrentIndex=" + mCurrentIndex);
                LogUtil.i(TAG, "onPreparePrevIndex() mNextIndex=" + mNextIndex);
                LogUtil.i(TAG, "==============================");
                int newPrevIndex = mNextIndex - 1;
                if (mCurrentIndex == newPrevIndex) {// 判定为下一曲下标（当前下标 > 下一个下标）
                    // 当前播放下标>=0
                    int newCurrentIndex = (mCurrentIndex - 1);
                    mCurrentIndex = newCurrentIndex < 0 ? getMusicList().size() - 1 : newCurrentIndex;
                    newPrevIndex = mCurrentIndex - 1;
                } else {
                    mCurrentIndex = mNextIndex;
                }

                if (newPrevIndex < 0) {
                    newPrevIndex = getMusicList().size() - 1;
                }
                this.mNextIndex = newPrevIndex;
                LogUtil.i(TAG, "==========After PREV===============");
                LogUtil.i(TAG, "onPreparePrevIndex() mCurrentIndex=" + mCurrentIndex);
                LogUtil.i(TAG, "onPreparePrevIndex() mNextIndex=" + mNextIndex);
                LogUtil.i(TAG, "==============================");
                break;
            case Definition.MusicPlayModel.MODE_RANDOM:
                mCurrentIndex = mNextIndex;
                mNextIndex = makeRandomPlayIndex();
                break;
            case Definition.MusicPlayModel.MODE_CIRCLE_SINGL:
                break;
        }
    }

    /**
     * 获取随机下标
     *
     * @return
     */
    private int makeRandomPlayIndex() {
        int randomIndex = new Random().nextInt(getMusicList().size());
        if (this.getMusicList().size() > 1 && randomIndex == this.mNextIndex) {
            makeRandomPlayIndex();
        }
        return randomIndex;
    }

    /**
     * 准备播放媒体下标
     *
     * @return
     */
    public boolean prepare() {
        if (hasData()) {
            if (NO_POSITON == mCurrentIndex) {
                mCurrentIndex = 0;
                mNextIndex = 1 < getMusicList().size() ? 1 : 0;
            }
            return true;
        }
        return false;
    }

    /**
     * 设置当前准备播放URL
     *
     * @param curUrl
     */
    public void setmPlayingMusicUrl(String curUrl) {
        this.mPlayingMusicUrl = curUrl;
    }

    /**
     * 刷新当前播放歌曲下标
     * 场景：
     * 1.列表播放
     * 2.断点记忆播放
     */
    public void refreshCurrentPlayingIndex() {
        LogUtil.i(TAG, "refreshCurrentPlayingIndex() ...");
        if (hasData() && mPlayingMusicUrl != null) {
            for (int i = 0; i < getMusicList().size(); i++) {
                if (getMusicList().get(i).getUrl().equals(mPlayingMusicUrl)) {

                    this.mCurrentIndex = i;

                    switch (mPlayMode) {
                        case Definition.MusicPlayModel.MODE_CIRCLE_ALL:
                            int newNextIndex = (i + 1);
                            this.mNextIndex = newNextIndex < getMusicList().size() ? newNextIndex : 0;
                            break;
                        case Definition.MusicPlayModel.MODE_RANDOM:
                            this.mNextIndex = makeRandomPlayIndex();
                            break;
                        case Definition.MusicPlayModel.MODE_CIRCLE_SINGL:
                            this.mNextIndex = i;
                            break;
                    }
                    mCurrentMusicInfo = getMusicList().get(mCurrentIndex);
                    mNextMusicInfo = getMusicList().get(mNextIndex);
                    LogUtil.i(TAG, "refreshCurrentPlayingIndex() SUC !!!");
                    break;
                }
            }
        }
    }

    /**
     * 获取当前歌曲播放序号
     * 当前歌曲下标/总歌曲数
     *
     * @return
     */
    public String getCurIndexAndTotalSizeWithProgram() {
        String pos = "0/0";
        if (hasData()) {
            int curNum = mCurrentIndex >= 0 ? mCurrentIndex + 1 : 0;
            int size = getMusicList().size();
            pos = (curNum + "/" + size);
        }
        LogUtil.i(TAG, "getCurIndexAndTotalSizeWithProgram() ..." + pos);
        return pos;
    }

    /**
     * 是否有数据
     *
     * @return
     */
    public boolean hasData() {
        return (null != getMusicList() && getMusicList().size() > 0);
    }

    /**
     * 添加播放列表
     *
     * @param musicList
     */
    public void addMusicList(List<MusicInfo> musicList) {
        if (null != musicList) {
            getMusicList().clear();
            getMusicList().addAll(musicList);
        }
    }

    /**
     * 获取播放列表
     *
     * @return
     */
    public synchronized List<MusicInfo> getMusicList() {
        if (null == mMusicList) {
            mMusicList = new ArrayList<MusicInfo>();
        }
        return this.mMusicList;
    }


}
