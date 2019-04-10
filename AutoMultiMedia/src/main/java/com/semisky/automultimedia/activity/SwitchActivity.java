package com.semisky.automultimedia.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

import com.semisky.automultimedia.BR;
import com.semisky.automultimedia.MediaApplication;
import com.semisky.automultimedia.R;
import com.semisky.automultimedia.common.constant.Definition;
import com.semisky.automultimedia.common.sql.SharePreferenceUtil;
import com.semisky.automultimedia.common.utils.AppUtils;
import com.semisky.automultimedia.common.utils.LogUtil;
import com.semisky.automultimedia.databinding.SwitchData;
import com.semisky.automultimedia.fragment.MusicListFragment;
import com.semisky.automultimedia.fragment.PictureListFragment;
import com.semisky.automultimedia.fragment.VideoListFragment;
import com.semisky.autoservice.manager.AutoConstants;
import com.semisky.autoservice.manager.AutoManager;

import java.lang.ref.WeakReference;

/**
 * Created by 熊俊 on 2017/12/21.
 */

public class SwitchActivity extends FragmentActivity {
    private static final String TAG = SwitchActivity.class.getSimpleName();
    private int mCurrentIndex;
    private Fragment[] mFragments = new Fragment[3];
    private FragmentManager fragmentManager;
    private SwitchData switchData;
    private String videoPlayingUrl;
    private Handler _handler = new MyHandler(this);
    private boolean isEnterPlayer = false;
    private boolean isClickListItem = false;
    private boolean isAutoExitApp = false;
    private int from;// 从哪里进入到列表界面

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LogUtil.i(TAG,"onConfigurationChanged() ...");
        switchData.tabMusic.setText(getString(R.string.music));
        switchData.tabVedio.setText(getString(R.string.vedio));
        switchData.tabPicture.setText(getString(R.string.picture));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        switchData = DataBindingUtil.setContentView(this, R.layout.activity_switch);
        switchData.setVariable(BR.tablistener, new SelectTabListener());



        Intent intent = getIntent();
        mCurrentIndex = intent.getIntExtra("type", 0);
        videoPlayingUrl = intent.getStringExtra("playingUrl");
        from = intent.getIntExtra("from", -1);
        LogUtil.i(TAG, "onCreate() from=" + from);

        initShowFragment(mCurrentIndex);// 初始化显示界面
        initTabUiState();
        //注册广播 2018-06/15
        registerReceiver(home,new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }

    // 初始化显示界面
    private void initShowFragment(int fragmentFlag){
        LogUtil.i(TAG,"initShowFragment() ..."+fragmentFlag);
        try{
            fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            mFragments[0] = new MusicListFragment();
            mFragments[1] = new VideoListFragment();
            mFragments[2] = new PictureListFragment();
            registerOnMusicListFragmentListener();
            registerOnVideoListFragmentListener();
            registerOnPictureListFragmentListener();

            switch (mCurrentIndex) {
                case 0:
                    if(AppUtils.hasMediaData(Definition.APP.FLAG_MUSIC) && !mFragments[0].isAdded()){
                        fragmentTransaction.add(R.id.contianer, mFragments[0]);
                        fragmentTransaction.commit();
                        switchData.tabMusic.setBackground(getResources().getDrawable(R.drawable.background_tabselected));
                    }
                    break;
                case 1:
                    if(AppUtils.hasMediaData(Definition.APP.FLAG_VIDEO) && !mFragments[1].isAdded()) {
                        fragmentTransaction.add(R.id.contianer, mFragments[1]);
                        if (videoPlayingUrl != null) {
                            Bundle bundle = new Bundle();
                            bundle.putString("playingUrl", videoPlayingUrl);
                            mFragments[1].setArguments(bundle);
                        }
                        fragmentTransaction.commit();
                        switchData.tabVedio.setBackground(getResources().getDrawable(R.drawable.background_tabselected));
                    }
                    break;
                case 2:
                    if(AppUtils.hasMediaData(Definition.APP.FLAG_PICTURE) && !mFragments[2].isAdded()) {
                        fragmentTransaction.add(R.id.contianer, mFragments[2]);
                        fragmentTransaction.commit();
                        switchData.tabPicture.setBackground(getResources().getDrawable(R.drawable.background_tabselected));
                    }
                    break;
            }
        }catch (Exception e){
            LogUtil.e(TAG,"initShowFragment() fail !!!");
            e.printStackTrace();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        from = intent.getIntExtra("from", -1);
        LogUtil.i(TAG, "onNewIntent() from=" + from);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.i(TAG, "onResume()");
        isClickListItem = false;
        isEnterPlayer = false;
        isAutoExitApp = false;
        sendMsgDelayCloseApp();
        AutoManager.getInstance().setAppStatus(getClass().getName(), getResources().getString(R.string.list), AutoConstants.AppStatus.RUN_FOREGROUND);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isEnterPlayer && !isClickListItem && !isAutoExitApp) {
            AutoManager.getInstance().setAppStatus(getClass().getName(), "background", AutoConstants.AppStatus.RUN_BACKGROUND);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.i(TAG, "onDestroy()");
        removeMsgDelayCloseApp();
        AutoManager.getInstance().setAppStatus(getClass().getName(), getResources().getString(R.string.list), AutoConstants.AppStatus.DESTROY);
        //2018/06/15 李超超
        unregisterReceiver(home);
    }

    public class SelectTabListener {

        // 设置正常背景
        public void setNormalBackground(TextView tv) {
            tv.setBackground(getResources().getDrawable(R.drawable.module_list_tab_bg_selector));
        }

        // 设置高亮背景
        public void setHighLightBackground(TextView tv) {
            tv.setBackground(getResources().getDrawable(R.drawable.background_tabselected));
        }

        public void onMusicClick() {
            if (AppUtils.hasMediaData(Definition.APP.FLAG_MUSIC)) {

                switchFragment(0);
                if (AppUtils.hasMediaData(Definition.APP.FLAG_VIDEO)) {
                    setNormalBackground(switchData.tabVedio);
                }
                if (AppUtils.hasMediaData(Definition.APP.FLAG_PICTURE)) {
                    setNormalBackground(switchData.tabPicture);
                }
                setHighLightBackground(switchData.tabMusic);
            }
        }

        public void onVedioClick() {
            if (AppUtils.hasMediaData(Definition.APP.FLAG_VIDEO)) {
                switchFragment(1);
                if (AppUtils.hasMediaData(Definition.APP.FLAG_MUSIC)) {
                    setNormalBackground(switchData.tabMusic);
                }
                if (AppUtils.hasMediaData(Definition.APP.FLAG_PICTURE)) {
                    setNormalBackground(switchData.tabPicture);
                }
                setHighLightBackground(switchData.tabVedio);
            }
        }

        public void onPictureClick() {
            if (AppUtils.hasMediaData(Definition.APP.FLAG_PICTURE)) {
                switchFragment(2);
                if (AppUtils.hasMediaData(Definition.APP.FLAG_MUSIC)) {
                    setNormalBackground(switchData.tabMusic);
                }
                if (AppUtils.hasMediaData(Definition.APP.FLAG_VIDEO)) {
                    setNormalBackground(switchData.tabVedio);
                }
                setHighLightBackground(switchData.tabPicture);
            }
        }

        public void onBackClick() {
            isEnterPlayer = true;
            MediaApplication.finishActivity(SwitchActivity.class);
            SwitchActivity.this.overridePendingTransition(0,0);
        }
    }

    // 根据状态初始化列表Tab ui
    private void initTabUiState(){
        if (!AppUtils.hasMediaData(Definition.APP.FLAG_MUSIC)) {
            switchData.tabMusic.setTextColor(getResources().getColor(R.color.color_gray));
        }
        if (!AppUtils.hasMediaData(Definition.APP.FLAG_VIDEO)) {
            switchData.tabVedio.setTextColor(getResources().getColor(R.color.color_gray));
        }
        if (!AppUtils.hasMediaData(Definition.APP.FLAG_PICTURE)) {
            switchData.tabPicture.setTextColor(getResources().getColor(R.color.color_gray));
        }
    }

    private void switchFragment(int position) {
        try {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            if (!mFragments[position].isAdded()) {
                fragmentTransaction.add(R.id.contianer, mFragments[position]);
            }

            fragmentTransaction.hide(mFragments[mCurrentIndex]).show(mFragments[position]);
            fragmentTransaction.commit();
            mCurrentIndex = position;
            sendMsgDelayCloseApp();
        }catch (Exception e){
            LogUtil.e(TAG,"switchFragment() fail !!! ..."+position);
            e.printStackTrace();
        }
    }


    private void removeMsgDelayCloseApp() {
        _handler.removeMessages(MyHandler.MSG_DELAY_CLOSE_APP);
    }

    private void sendMsgDelayCloseApp() {
        _handler.removeMessages(MyHandler.MSG_DELAY_CLOSE_APP);
        _handler.sendEmptyMessageDelayed(MyHandler.MSG_DELAY_CLOSE_APP, MyHandler.DELAY);
    }


    protected static class MyHandler extends Handler {
        private static final long DELAY = 10000;
        private static final int MSG_DELAY_CLOSE_APP = 0;
        WeakReference<SwitchActivity> mReference;

        MyHandler(SwitchActivity activity) {
            mReference = new WeakReference<SwitchActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            LogUtil.i(TAG, "from=" + mReference.get().from);
            mReference.get().isAutoExitApp = true;
            mReference.get().skipTo(mReference.get().from);
            MediaApplication.finishActivity(SwitchActivity.class);
            mReference.get().overridePendingTransition(0,0);
        }

    }

    private void skipTo(int appFlag) {
        switch (appFlag) {
            case Definition.APP.FLAG_MUSIC:
                startActivitys(MusicPlayActivity.class);
                break;
            case Definition.APP.FLAG_VIDEO:
                startActivitys(VideoPlayActivity.class);
                break;
            case Definition.APP.FLAG_PICTURE:
                startActivitys(PicturePlayActivity.class);
                break;
        }
    }

    private void startActivitys(Class cls) {
        Intent intent = new Intent(SwitchActivity.this, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void registerOnPictureListFragmentListener() {
        ((PictureListFragment) mFragments[2]).setOnPictureListFragmentListener(new PictureListFragment.OnPictureListFragmentListener() {
            @Override
            public void onResult(int resultCode) {
                if (PictureListFragment.OnPictureListFragmentListener.RESULT_CODE_RECLOCKING == resultCode) {
                    //todo 李超超 2018-4-4
                    sendMsgDelayCloseApp();
                } else if (PictureListFragment.OnPictureListFragmentListener.RESULT_CODE_RECLOCKING_REMOVE == resultCode) {
                    //todo 李超超 2018-4-4
                    removeMsgDelayCloseApp();
                } else if (PictureListFragment.OnPictureListFragmentListener.RESULT_CODE_CLICK_LIST_ITEM == resultCode) {
                    isClickListItem = true;
                }
            }
        });
    }

    private void registerOnVideoListFragmentListener() {
        ((VideoListFragment) mFragments[1]).setOnVideoListFragmentListener(new VideoListFragment.OnVideoListFragmentListener() {
            @Override
            public void onResult(int resultCode) {
                if (VideoListFragment.OnVideoListFragmentListener.RESULT_CODE_RECLOCKING == resultCode) {
                    sendMsgDelayCloseApp();
                } else if (VideoListFragment.OnVideoListFragmentListener.RESULT_CODE_RECLOCKING_REMOVE == resultCode) {
                    removeMsgDelayCloseApp();
                } else if (VideoListFragment.OnVideoListFragmentListener.RESULT_CODE_CLICK_LIST_ITEM == resultCode) {
                    isClickListItem = true;
                }
            }
        });
    }

    private void registerOnMusicListFragmentListener() {
        ((MusicListFragment) mFragments[0]).setOnMusicListFragmentListener(new MusicListFragment.OnMusicListFragmentListener() {
            @Override
            public void onResult(int resultCode) {
                if (MusicListFragment.OnMusicListFragmentListener.RESULT_CODE_RECLOCKING == resultCode) {
                    sendMsgDelayCloseApp();
                } else if (MusicListFragment.OnMusicListFragmentListener.RESULT_CODE_RECLOCKING_REMOVE == resultCode) {
                    removeMsgDelayCloseApp();
                } else if (MusicListFragment.OnMusicListFragmentListener.RESULT_CODE_CLICK_LIST_ITEM == resultCode) {
                    isClickListItem = true;
                }
            }
        });
    }


    public void cleanPosition_PictureVideoMusic(int fragmentFlag){

        switch (fragmentFlag){
            case 1://清空图片播放记录位置，音乐播放位置
                MediaApplication.setList_picturePosition(0);
                SharePreferenceUtil.setCurrentPlayingMusicUrl(MediaApplication.context,"NoPath..");
                SharePreferenceUtil.setCurrentPlayMusicFolder(MediaApplication.context,"NoPath");
                SharePreferenceUtil.setCurrentPlayingMusicProgress(MediaApplication.context,0);
                break;
            case 2://清空video上一次播放的位置，图片位置
                SharePreferenceUtil.setCurrentPlayingVideoUrl(MediaApplication.context,"notPath");
                SharePreferenceUtil.setCurrentPlayVideoProgress(MediaApplication.context,0);
                MediaApplication.setList_picturePosition(0);
                break;
            case 3://清空video上一次播放位置，音乐播放位置
                SharePreferenceUtil.setCurrentPlayingVideoUrl(MediaApplication.context,"notPath");
                SharePreferenceUtil.setCurrentPlayVideoProgress(MediaApplication.context,0);
//                SharePreferenceUtil.setCurrentPlayingMusicUrl(MediaApplication.context,"NoPath..");
//                SharePreferenceUtil.setCurrentPlayMusicFolder(MediaApplication.context,"NoPath");
//                SharePreferenceUtil.setCurrentPlayingMusicProgress(MediaApplication.context,0);
                break;
        }

    }

    /**
     * Home键 监听广播
     * 2018-06-15 李超超
     */
    BroadcastReceiver home=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)){
                MediaApplication.finishActivity(SwitchActivity.class);
            }
        }
    };

}
