package com.semisky.automultimedia.fragment;


import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.semisky.automultimedia.MediaApplication;
import com.semisky.automultimedia.R;
import com.semisky.automultimedia.VedioList;
import com.semisky.automultimedia.activity.MusicPlayActivity;
import com.semisky.automultimedia.activity.PicturePlayActivity;
import com.semisky.automultimedia.activity.SwitchActivity;
import com.semisky.automultimedia.activity.VideoPlayActivity;
import com.semisky.automultimedia.common.adapter.VideoListAdapter;
import com.semisky.automultimedia.common.bean.VideoInfo;
import com.semisky.automultimedia.common.mediascan.MediaParaser;
import com.semisky.automultimedia.common.strategys.VideoStrategyManager;
import com.semisky.automultimedia.common.utils.AppUtils;
import com.semisky.automultimedia.service.datainfo.VedioDataInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.semisky.automultimedia.common.constant.Definition.VideoMessge.VEDIO_LIST;

/**
 * A simple {@link Fragment} subclass.
 */
public class VideoListFragment extends Fragment implements AdapterView.OnItemClickListener, View.OnTouchListener {
    private Activity activity;
    private VideoListAdapter mAdapter;
    private VedioDataInfo vedioDataInfo;
    private VideoHandler handler = new VideoHandler();
    private List<VideoInfo> videoInfoList = new ArrayList<VideoInfo>();
    private String playingUrl;
    private VedioList vedioList;

    public VideoListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        vedioList = DataBindingUtil.inflate(inflater, R.layout.fragment_vedio_list, container, false);
        Bundle bundle = getArguments();
        if (bundle != null) {
            playingUrl = bundle.getString("playingUrl");
        }

        activity = getActivity();
        mAdapter = new VideoListAdapter(activity);
        vedioList.videoListView.setAdapter(mAdapter);
        vedioList.videoListView.setOnItemClickListener(this);
        vedioList.videoListView.setOnTouchListener(this);

        return vedioList.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vedioDataInfo = new VedioDataInfo(activity);
        getVideoList();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        handler = null;
    }

    private void getVideoList() {
        vedioDataInfo.getVideoList(handler);
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
        notifyClickListItem();
        VideoStrategyManager.getInstance().setUserSelectListPlayState(true);
        startVideoPlayer(videoInfoList.get(position).getVideoUrl());
//        MediaApplication.finishActivity(MusicPlayActivity.class);
        MediaApplication.finishActivity(PicturePlayActivity.class);
        MediaApplication.finishActivity(SwitchActivity.class);
    }

    // 跳转视频播放器意图
    private void startVideoPlayer(String url){
        if(url != null){
            Intent intent = new Intent(activity, VideoPlayActivity.class);
            intent.putExtra("url", url);
            activity.startActivity(intent);
            activity.overridePendingTransition(0,0);
            // add 清空音乐播放位置，图片播放的位置 2018-4-20
            SwitchActivity activity=(SwitchActivity) getActivity();
            activity.cleanPosition_PictureVideoMusic(1);
        }
    }
    MyParsingVideo videoThread;
    private class VideoHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case VEDIO_LIST:
                    videoInfoList.addAll((Collection<? extends VideoInfo>) msg.obj);
                    if (playingUrl != null) {
                        int pos = -1;
                        for (VideoInfo video : videoInfoList) {
                            pos+=1;
                            if (video.getVideoUrl().equals(playingUrl)) {
                                video.setPlaying(true);
                                vedioList.videoListView.setSelection(pos);
                            }
                        }
                    }
                    //todo,视频是否有效 4-13
//                    if (vedioList!=null&&videoInfoList.size()>0){
//                        for (VideoInfo videoInfo:videoInfoList){
//                            VideoInfo v=MediaParaser.getInstance().parserMetadataByVideoUrl(videoInfo.getVideoUrl());
//                            if (v==null){
//                                videoInfo.setValid(false);
//                            }else {
//                                videoInfo.setValid(true);
//                            }
//                        }
//                    }

                    mAdapter.setmVedioList(videoInfoList);
                    List<VideoInfo> list_m=new ArrayList<VideoInfo>();
                    list_m=videoInfoList;
                    if (list_m!=null&&list_m.size()>0){
                        videoThread=new MyParsingVideo(list_m);
                        videoThread.start();
                    }

                    break;
                case 0:
                    List<VideoInfo> list=(List<VideoInfo>) msg.obj;
                    mAdapter.setmVedioList(list);
                    break;
            }
        }
    }

    public interface OnVideoListFragmentListener {
        final int RESULT_CODE_RECLOCKING = 0;
        final int RESULT_CODE_RECLOCKING_REMOVE = 1;
        final int RESULT_CODE_CLICK_LIST_ITEM = 2;

        void onResult(int resultCode);
    }

    private OnVideoListFragmentListener mOnVideoListFragmentListener = null;

    public void setOnVideoListFragmentListener(OnVideoListFragmentListener listener) {
        this.mOnVideoListFragmentListener = listener;
    }

    // 通知重新计时
    private void notifyReclocking() {
        if (null != mOnVideoListFragmentListener) {
            mOnVideoListFragmentListener.onResult(OnVideoListFragmentListener.RESULT_CODE_RECLOCKING);
        }
    }

    // 移除消息重新计时
    private void notifyRemoveReclocking() {
        if (null != mOnVideoListFragmentListener) {
            mOnVideoListFragmentListener.onResult(OnVideoListFragmentListener.RESULT_CODE_RECLOCKING_REMOVE);
        }
    }

    // 通知点击列表条目事件
    private boolean isRun=false;
    private void notifyClickListItem(){
        if (null != mOnVideoListFragmentListener) {
            mOnVideoListFragmentListener.onResult(OnVideoListFragmentListener.RESULT_CODE_CLICK_LIST_ITEM);
        }
    }
    public class MyParsingVideo extends Thread{
        List<VideoInfo> videoInfos;
        public MyParsingVideo(List<VideoInfo> videoInfos){
            this.videoInfos=videoInfos;
        }
        @Override
        public void run() {
            for (VideoInfo v:videoInfos){
                if (isRun){
                    return;
                }
                 if (AppUtils.parsingVedioData(v.getVideoUrl())==0){
                     v.setValid(true);
                 }else {
                     v.setValid(false);
                 }
            }
            //释放资源
            AppUtils.closeMediaMetadataRetriever();
            Message message=new Message();
            message.obj=videoInfos;
            message.what=0;
            if (handler!=null){
                handler.sendMessage(message);
            }

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
         isRun=true;
    }
}
