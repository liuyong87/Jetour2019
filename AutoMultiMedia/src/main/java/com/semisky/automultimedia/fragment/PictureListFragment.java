package com.semisky.automultimedia.fragment;


import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.semisky.automultimedia.MediaApplication;
import com.semisky.automultimedia.PictureList;
import com.semisky.automultimedia.R;
import com.semisky.automultimedia.activity.MusicPlayActivity;
import com.semisky.automultimedia.activity.PicturePlayActivity;
import com.semisky.automultimedia.activity.SwitchActivity;
import com.semisky.automultimedia.activity.VideoPlayActivity;
import com.semisky.automultimedia.common.adapter.PictureListAdapter;
import com.semisky.automultimedia.common.bean.PictureInfo;
import com.semisky.automultimedia.common.sql.SharePreferenceUtil;
import com.semisky.automultimedia.service.datainfo.PictureDataInfo;

import java.util.ArrayList;
import java.util.List;

import static com.semisky.automultimedia.common.constant.Definition.PictureMessge.PICTURE_LIST;

/**
 * A simple {@link Fragment} subclass.
 */
public class PictureListFragment extends Fragment implements AdapterView.OnItemClickListener, View.OnTouchListener {
    private Context mContext;
    private PictureListAdapter adapter;
    private PictureListHandler handler = new PictureListHandler();
    private List<PictureInfo> pictureInfos = new ArrayList<PictureInfo>();
    private PictureList pictureList;
    public PictureListFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        pictureList = DataBindingUtil.inflate(inflater, R.layout.fragment_picture_list, container, false);
        mContext = getActivity();
        adapter = new PictureListAdapter(mContext);
        pictureList.gridView.setAdapter(adapter);

        PictureDataInfo pictureDataInfo = new PictureDataInfo(mContext);
        pictureDataInfo.getPictureList(handler);

        pictureList.gridView.setOnItemClickListener(this);
        pictureList.gridView.setOnTouchListener(this);


        return pictureList.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        handler = null;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                notifyRemoveReclocking();
                break;
            case MotionEvent.ACTION_UP:
                notifyReclocking();
                break;
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        MediaApplication.finishActivity(MusicPlayActivity.class);
        startPicturePlayer(position);
        MediaApplication.finishActivity(VideoPlayActivity.class);
        MediaApplication.finishActivity(SwitchActivity.class);
        notifyClickListItem();
    }

    private void startPicturePlayer(int pos){
        SharePreferenceUtil.setCurrentPlayingPictureUrl(MediaApplication.context,pictureInfos.get(pos).getPicUrl());
        Intent intent = new Intent(mContext, PicturePlayActivity.class);
        intent.putExtra("position", pos);
        mContext.startActivity(intent);
        getActivity().overridePendingTransition(0,0);
        // add 清空音乐播放位置，视频播放的位置 2018-4-20
        SwitchActivity activity=(SwitchActivity) getActivity();
        activity.cleanPosition_PictureVideoMusic(3);
    }

    private class PictureListHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PICTURE_LIST:
                    pictureInfos.clear();
                    pictureInfos.addAll((List<PictureInfo>) msg.obj);
                    adapter.setListUrl(pictureInfos);
                    //todo 2018-4-11
                    pictureList.gridView.setSelection(MediaApplication.getList_picturePosition());
                    break;
            }
        }
    }


    public interface OnPictureListFragmentListener {
        final int RESULT_CODE_RECLOCKING = 0;
        final int RESULT_CODE_RECLOCKING_REMOVE = 1;
        final int RESULT_CODE_CLICK_LIST_ITEM = 2;

        void onResult(int resultCode);
    }

    private OnPictureListFragmentListener mOnPictureListFragmentListener = null;

    public void setOnPictureListFragmentListener(OnPictureListFragmentListener listener) {
        this.mOnPictureListFragmentListener = listener;
    }

    // 通知重新计时
    private void notifyReclocking() {
        if (null != mOnPictureListFragmentListener) {
            mOnPictureListFragmentListener.onResult(OnPictureListFragmentListener.RESULT_CODE_RECLOCKING);
        }
    }

    // 移除消息重新计时
    private void notifyRemoveReclocking() {
        if (null != mOnPictureListFragmentListener) {
            mOnPictureListFragmentListener.onResult(OnPictureListFragmentListener.RESULT_CODE_RECLOCKING_REMOVE);
        }
    }

    // 通知点击列表条目事件
    private void notifyClickListItem(){
        if (null != mOnPictureListFragmentListener) {
            mOnPictureListFragmentListener.onResult(OnPictureListFragmentListener.RESULT_CODE_CLICK_LIST_ITEM);
        }
    }
}
