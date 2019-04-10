package com.semisky.automultimedia.fragment;


import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.semisky.automultimedia.MediaApplication;
import com.semisky.automultimedia.MusicList;
import com.semisky.automultimedia.R;
import com.semisky.automultimedia.activity.MusicPlayActivity;
import com.semisky.automultimedia.activity.PicturePlayActivity;
import com.semisky.automultimedia.activity.SwitchActivity;
import com.semisky.automultimedia.activity.VideoPlayActivity;
import com.semisky.automultimedia.common.adapter.MusicListAllSongsAdapter;
import com.semisky.automultimedia.common.adapter.MusicListFolderAdapter;
import com.semisky.automultimedia.common.bean.MusicInfo;
import com.semisky.automultimedia.common.constant.Definition;
import com.semisky.automultimedia.common.data.MusicDataModule;
import com.semisky.automultimedia.common.musicplay.MusicPlayerManager;
import com.semisky.automultimedia.common.sql.SharePreferenceUtil;
import com.semisky.automultimedia.common.utils.AppUtils;
import com.semisky.automultimedia.common.utils.LogUtil;
import com.semisky.autoservice.manager.AutoConstants;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MusicListFragment extends Fragment implements View.OnTouchListener {
    private static final java.lang.String TAG = MusicListFragment.class.getSimpleName();
    private Activity activity;
    private MusicListAllSongsAdapter mMusicListAllSongsAdapter = null;// 所有歌曲列表ITEM适配器
    private MusicListFolderAdapter mMusicListFolderAdapter = null;// 所有文件夹ITEM适配器
    private Handler handler = new MusicHandler();
    private MusicList musicListDataBinding;

    private static final int FLAG_ITEM_ALL_SONGS = 0;
    private static final int FLAG_ITEM_ALL_FOLDER = 1;
    private static final int FLAG_ITEM_FOLDER_UNDER_FILE = 2;
    private int mNextItemFlag = FLAG_ITEM_ALL_SONGS;
    private int mCurrentItemFlag = -1;


    public MusicListFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        musicListDataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_music_list, container, false);
        activity = getActivity();
        mMusicListAllSongsAdapter = new MusicListAllSongsAdapter(activity);
        mMusicListFolderAdapter = new MusicListFolderAdapter(activity);

        musicListDataBinding.musicListView.setOnTouchListener(this);
        musicListDataBinding.musicFolderListView.setOnTouchListener(this);

        musicListDataBinding.musicListView.setOnItemClickListener(mFileListItemClickListener);// 媒体文件列表Item监听
        musicListDataBinding.musicFolderListView.setOnItemClickListener(mFolderListItemClickListener);// 媒体文件夹列表Item监听

        musicListDataBinding.musicListView.setAdapter(mMusicListAllSongsAdapter);
        musicListDataBinding.musicFolderListView.setAdapter(mMusicListFolderAdapter);
        setListener();
        return musicListDataBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MusicDataModule.getInstance().loadAllSongs();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        AppUtils.setAppStatus(MusicPlayActivity.class.getName(), "background", AutoConstants.AppStatus.RUN_BACKGROUND);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        handler = null;
        MusicDataModule.getInstance().unregisterOnLoadDataListener(mOnLoadDataListener);
    }

    public void setListener() {
        MusicDataModule.getInstance().registerOnLoadDataListener(mOnLoadDataListener);
        musicListDataBinding.llBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                notifyReclocking();
                if (mNextItemFlag == FLAG_ITEM_ALL_FOLDER) {// 显示文件夹列表
                    MusicDataModule.getInstance().loadAllFolder();
                } else if (mNextItemFlag == FLAG_ITEM_ALL_SONGS) {// 显示所有歌曲列表
                    MusicDataModule.getInstance().loadAllSongs();
                }
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (MotionEvent.ACTION_DOWN == event.getAction()) {
            notifyRemoveReclocking();
        } else if (MotionEvent.ACTION_UP == event.getAction()) {
            notifyReclocking();
        }
        return false;
    }


    // 媒体文件列表监听
    private AdapterView.OnItemClickListener mFileListItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            LogUtil.i(TAG, "mFileListItemClickListener ->onItemClick() :" + mCurrentItemFlag);
            MusicPlayerManager.getInstance().setListPlayState(true);// 列表播放标记
            if (mCurrentItemFlag == FLAG_ITEM_ALL_SONGS) {// 所有歌曲ITEM
                notifyClickListItem();
                if (null != mMusicListAllSongsAdapter) {
                    List<MusicInfo> allSongList = mMusicListAllSongsAdapter.getData();
                    if (null != allSongList && allSongList.size() > 0) {
                        String url = allSongList.get(position).getUrl();
                        MusicPlayerManager.getInstance().notifyControlPlay(url);
                        skipToPlayer();
                        MediaApplication.finishActivity(VideoPlayActivity.class);
                        MediaApplication.finishActivity(PicturePlayActivity.class);
                        MediaApplication.finishActivity(SwitchActivity.class);
                    }
                }
            } else if (mCurrentItemFlag == FLAG_ITEM_FOLDER_UNDER_FILE) {// 单个Folder下歌曲ITEM
                notifyClickListItem();
                if (null != mMusicListAllSongsAdapter) {
                    List<MusicInfo> folderUnderSongsList = mMusicListAllSongsAdapter.getData();
                    if (null != folderUnderSongsList && folderUnderSongsList.size() > 0) {
                        String url = folderUnderSongsList.get(position).getUrl();
                        MusicPlayerManager.getInstance().notifyControlPlay(url);
                        skipToPlayer();
                        MediaApplication.finishActivity(VideoPlayActivity.class);
                        MediaApplication.finishActivity(PicturePlayActivity.class);
                        MediaApplication.finishActivity(SwitchActivity.class);
                    }
                }

            }// END <else if>
        }
    };
    // 媒体文件夹列表监听
    private AdapterView.OnItemClickListener mFolderListItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            LogUtil.i(TAG, "mFolderListItemClickListener ->onItemClick() :" + mCurrentItemFlag);

            if (mCurrentItemFlag == FLAG_ITEM_ALL_FOLDER) {// 所有文件夹ITEM
                if (null != mMusicListFolderAdapter) {
                    List<String> allFolderList = mMusicListFolderAdapter.getData();
                    if (null != allFolderList && allFolderList.size() > 0) {
                        String folderMusicName = allFolderList.get(position);
                        LogUtil.i(TAG, "onItemClick= folderMusicName=" + folderMusicName);
                        MusicDataModule.getInstance().loadFolderUnderFile(folderMusicName);
                    }
                }

            }
        }
    };

    //跳转音乐播放界面
    private void skipToPlayer() {
        getActivity().startActivity(new Intent(getActivity(), MusicPlayActivity.class));
        getActivity().overridePendingTransition(0, 0);
        // add 清空视频播放，图片播放的位置 2018-4-20
        SwitchActivity activity=(SwitchActivity) getActivity();
        activity.cleanPosition_PictureVideoMusic(2);
    }

    private class MusicHandler extends Handler {
        protected static final int MSG_ALL_SONGS = 112;
        protected static final int MSG_ALL_FOLDER = 113;
        protected static final int MSG_FOLDER_UNDER_FILE = 114;

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_ALL_SONGS:
                    mCurrentItemFlag = FLAG_ITEM_ALL_SONGS;
                    mNextItemFlag = FLAG_ITEM_ALL_FOLDER;
                    setBackTextState(false);
                    mMusicListAllSongsAdapter.updateList((List<MusicInfo>) msg.obj);
                    if (msg.arg1 != -1) {
                        musicListDataBinding.musicListView.setSelection(msg.arg1);
                    }
                    musicListDataBinding.musicListView.setVisibility(View.VISIBLE);//显示File list
                    musicListDataBinding.musicFolderListView.setVisibility(View.INVISIBLE);//隐藏Folder list
                    break;
                case MSG_ALL_FOLDER:
                    mCurrentItemFlag = FLAG_ITEM_ALL_FOLDER;
                    mNextItemFlag = FLAG_ITEM_ALL_SONGS;
                    setBackTextState(true);
                    mMusicListFolderAdapter.updateList((List<String>) msg.obj);
                    musicListDataBinding.musicFolderListView.setAdapter(mMusicListFolderAdapter);

                    musicListDataBinding.musicFolderListView.setVisibility(View.VISIBLE);//显示Folder list
                    musicListDataBinding.musicListView.setVisibility(View.INVISIBLE);//隐藏File list
                    break;
                case MSG_FOLDER_UNDER_FILE:
                    mCurrentItemFlag = FLAG_ITEM_FOLDER_UNDER_FILE;
                    mNextItemFlag = FLAG_ITEM_ALL_FOLDER;
                    setBackTextState(false);
                    mMusicListAllSongsAdapter.updateList((List<MusicInfo>) msg.obj);
                    musicListDataBinding.musicListView.setVisibility(View.VISIBLE);//显示File list
                    musicListDataBinding.musicFolderListView.setVisibility(View.INVISIBLE);//隐藏Folder list
                    break;
            }
        }
    }

    // 此方法有在BaseFragment调用（暂停保留）
    public void updatePlaying(String name) {

    }

    public interface OnMusicListFragmentListener {
        final int RESULT_CODE_RECLOCKING = 0;
        final int RESULT_CODE_RECLOCKING_REMOVE = 1;
        final int RESULT_CODE_CLICK_LIST_ITEM = 2;

        void onResult(int resultCode);
    }

    private OnMusicListFragmentListener mOnMusicListFragmentListener = null;

    public void setOnMusicListFragmentListener(OnMusicListFragmentListener listener) {
        this.mOnMusicListFragmentListener = listener;
    }

    // 通知重新计时
    private void notifyReclocking() {
        if (null != mOnMusicListFragmentListener) {
            mOnMusicListFragmentListener.onResult(OnMusicListFragmentListener.RESULT_CODE_RECLOCKING);
        }
    }

    // 移除消息重新计时
    private void notifyRemoveReclocking() {
        if (null != mOnMusicListFragmentListener) {
            mOnMusicListFragmentListener.onResult(OnMusicListFragmentListener.RESULT_CODE_RECLOCKING_REMOVE);
        }
    }

    // 通知点击列表条目事件
    private void notifyClickListItem() {
        if (null != mOnMusicListFragmentListener) {
            mOnMusicListFragmentListener.onResult(OnMusicListFragmentListener.RESULT_CODE_CLICK_LIST_ITEM);
        }
    }


    private MusicDataModule.OnLoadDataListener mOnLoadDataListener = new MusicDataModule.OnLoadDataListener() {
        @Override
        public void onAllFolderListResult(List<String> mFolderList) {
            LogUtil.i(TAG, "onAllMusicListResult()" + (mFolderList != null ? mFolderList.size() : 0));
            if (null != mFolderList) {
                Message msg = handler.obtainMessage();
                msg.what = MusicHandler.MSG_ALL_FOLDER;
                msg.obj = mFolderList;
                handler.sendMessage(msg);
            }
        }

        @Override
        public void onAllMusicListResult(List<MusicInfo> musicList) {
            LogUtil.i(TAG, "onAllMusicListResult()" + (musicList != null ? musicList.size() : 0));
            if (null != musicList) {
                int pos = getPositionByCurPlayMusic(musicList);
                Message msg = handler.obtainMessage();
                msg.what = MusicHandler.MSG_ALL_SONGS;
                msg.obj = musicList;
                msg.arg1 = pos;
                handler.sendMessage(msg);
            }
        }

        @Override
        public void onFolderUnderFileListResult(List<MusicInfo> musicList) {
            LogUtil.i(TAG, "onFolderUnderFileListResult()" + (musicList != null ? musicList.size() : 0));
            if (null != musicList) {
                Message msg = handler.obtainMessage();
                msg.what = MusicHandler.MSG_FOLDER_UNDER_FILE;
                msg.obj = musicList;
                handler.sendMessage(msg);
            }
        }
    };

    // 获取当前歌曲列表中的位置
    private int getPositionByCurPlayMusic(List<MusicInfo> list) {
        if (null != list) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getUrl().equals(MusicDataModule.getInstance().getCurrentPlayMusicUrl())) {
                    return i;
                }
            }
        }
        return -1;
    }

    // 根据状态显示Back控件内容
    private void setBackTextState(boolean isAllSongsText) {
        if (isAllSongsText) {
            musicListDataBinding.tvBackText.setText(getString(R.string.all_music));
            musicListDataBinding.llBack.setVisibility(View.GONE);
            return;
        }
        musicListDataBinding.tvBackText.setText(getString(R.string.music_list_back_text));
        musicListDataBinding.llBack.setVisibility(View.VISIBLE);
        return;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != handler) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
