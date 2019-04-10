package com.semisky.automultimedia.activity;

import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.semisky.automultimedia.MediaApplication;
import com.semisky.automultimedia.R;
import com.semisky.automultimedia.activity.symbol.PicturePlaySymbol;
import com.semisky.automultimedia.common.adapter.PicturePageAdapter;
import com.semisky.automultimedia.common.bean.PictureInfo;
import com.semisky.automultimedia.common.bean.VideoInfo;
import com.semisky.automultimedia.common.broadRecevier.BTReceiver;
import com.semisky.automultimedia.common.constant.Definition;
import com.semisky.automultimedia.common.interfaces.ISetWallpaperListener;
import com.semisky.automultimedia.common.sql.SharePreferenceUtil;
import com.semisky.automultimedia.common.strategys.StrategyManager;
import com.semisky.automultimedia.common.utils.AppUtils;
import com.semisky.automultimedia.common.utils.LogUtil;
import com.semisky.automultimedia.common.view.SelfDialogHint;
import com.semisky.automultimedia.databinding.PictureData;
import com.semisky.automultimedia.service.datainfo.PictureDataInfo;
import com.semisky.autoservice.manager.AutoConstants;
import com.semisky.autoservice.manager.AutoManager;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static com.semisky.automultimedia.common.constant.Definition.PictureMessge.PICTURE_LIST;
import static com.semisky.automultimedia.common.constant.Definition.PictureMessge.PICTURE_PLAY;
import static com.semisky.automultimedia.common.constant.Definition.PictureMessge.PICTURE_HIDE_NOTICE_VIEW;
import static com.semisky.automultimedia.common.constant.Definition.PictureMessge.PICTURE_SHOW_NOTICE_VIEW;
import static com.semisky.automultimedia.common.constant.Definition.PictureMessge.PICTURE_FULL_SCREEN_MODE;

/**
 * Created by 熊俊 on 2017/12/29.
 */

public class PicturePlayActivity extends BasePictureActivity implements View.OnClickListener, View.OnTouchListener ,ViewPager.OnPageChangeListener{

    private String TAG = PicturePlayActivity.class.getSimpleName();
    private PictureData pictureData;
    private List<PictureInfo> pictureInfoList = new ArrayList<PictureInfo>();
    private PicturePageAdapter mAdapter;
    private PictureListsHandler handler = new PictureListsHandler();
    private PictureDataInfo pictureDataInfo;
    private PicturePlaySymbol picturePlaySymbol;
    private int position;
    private boolean isPlaying = false;
    private int scale_Factor = 0;
    private boolean isEnterList = false;// 是否进入列表
    private int currentPosition = 0;//回到主页时记录当前图片位置
    private boolean isStop_AVM=false;//AVM广播结束时，当前图片是否暂停
    /**
     * 接收倒车广播 处理图片的状态
     *  "com.semisky.IS_AVM" 接收倒车影像广播
     *  "com.semisky.IS_AD_CLOSE" 接收到关闭倒车影像广播
     */
//    BroadcastReceiver avm=new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (action.equals("com.semisky.IS_AVM")) {
//                boolean isAvm = intent.getBooleanExtra("isAVM", false);
//                if (isAvm) {
//                    if (isPlaying) {
//                        pausePicture();
//                    } else {
//                        isStop_AVM = true;
//                    }
//                }
//            } else if (action.equals("com.semisky.IS_AD_CLOSE")) {
//                boolean isAvmColse = intent.getBooleanExtra("isClose", false);
//                if (isAvmColse) {
//                    if (isStop_AVM) {
//                        isStop_AVM = false;
//                        return;
//                    }
//                    if (!isPlaying) {
//                        playPicture();
//                    }
//                }
//
//          }
//        }
//    };


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LogUtil.i(TAG,"onConfigurationChanged() ...");

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        pictureData = DataBindingUtil.setContentView(this, R.layout.activity_picture_play);
        picturePlaySymbol = new PicturePlaySymbol();
        pictureData.setPictureSymbol(picturePlaySymbol);

        mAdapter = new PicturePageAdapter(this);
        mAdapter.registerOnClickListener(this);
        mAdapter.registerOnTochListener(this);
        pictureData.viewPager.setAdapter(mAdapter);

        pictureData.setListener(new PictureControlListener());
        pictureDataInfo = new PictureDataInfo(this);
        pictureDataInfo.getPictureList(handler);
        Log.i(TAG, "onCreate()...");
        handIntent(getIntent());
        super.putBooleanState(super.DISPLAY_MODE_KEY, super.DISPLAY_MODE_NORMAL_SCREEN);
        SharePreferenceUtil.saveLastAppFlag(Definition.APP.FLAG_PICTURE);
        //屏幕滑动图片动作
        pictureData.viewPager.setOnPageChangeListener(this);
        //注册AVM广播
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.semisky.IS_AVM");
        filter.addAction("com.semisky.IS_AD_CLOSE");
//        registerReceiver(avm, filter);
        registerReceiver(home,new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        //注册断电广播
        registerReceiver(powerIsStop,new IntentFilter("com.semisky.broadcast.POWERMODE"));
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LogUtil.i(TAG, "onNewIntent()...");
        handIntent(intent);
        //pictureData.setUrl(intent.getStringExtra("imageurl"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        isEnterList = false;
        if (pictureStopOrPlaying){
            playPicture();
        }
        sendMsgDelayShowFullScreenMode(5000);
        AutoManager.getInstance().setAppStatus(getClass().getName(), getResources().getString(R.string.picture), AutoConstants.AppStatus.RUN_FOREGROUND);
        AppUtils.closeUsbConnectDialog();
    }
    private boolean pictureStopOrPlaying=false;
    @Override
    protected void onPause() {
        super.onPause();
        // todo 记录当前图片的位置
        currentPosition = pictureData.viewPager.getCurrentItem();
        MediaApplication.setList_picturePosition(currentPosition);
        pictureStopOrPlaying=isPlaying;
        pausePicture();
        //-------------
        if (!isEnterList) {
            AutoManager.getInstance().setAppStatus(getClass().getName(), "background", AutoConstants.AppStatus.RUN_BACKGROUND);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        super.removeAllStateManagerCache();

        AutoManager.getInstance().setAppStatus(getClass().getName(), getResources().getString(R.string.picture), AutoConstants.AppStatus.DESTROY);

        handler.removeCallbacksAndMessages(null);
        handler = null;
//        unregisterReceiver(avm);
        unregisterReceiver(home);
        unregisterReceiver(powerIsStop);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.photo_view) {
            if (MotionEvent.ACTION_DOWN == event.getAction()) {
                pausePicture();
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.photo_view) {

            if (getBooleanState(DISPLAY_MODE_KEY)) {
                //全屏显示
                setFullScreen();

            } else {
                // 正常屏显示
                setNormalScreen();
            }
        }
    }

    @Override
    protected void setNormalScreen() {
        sendMsgDelayShowFullScreenMode(5000);
        AppUtils.setNormalScreenMode(this);
        picturePlaySymbol.setShowBottomBar(true);
        super.putBooleanState(super.DISPLAY_MODE_KEY, super.DISPLAY_MODE_FULL_SCREEN);
    }

    @Override
    protected void setFullScreen() {
        AppUtils.setFullScreenMode(this);
        picturePlaySymbol.setShowBottomBar(false);
        super.putBooleanState(super.DISPLAY_MODE_KEY, super.DISPLAY_MODE_NORMAL_SCREEN);
    }

    @Override
    protected void sendMsgDelayShowFullScreenMode(int delayMillis) {
        handler.removeMessages(PICTURE_FULL_SCREEN_MODE);
        handler.sendEmptyMessageDelayed(PICTURE_FULL_SCREEN_MODE, delayMillis);
    }

    private void handIntent(Intent intent) {
        // todo 2018-4-3  李超超 更改
        position = intent.getIntExtra("position", -1);
        if (position == -1) {
            position = currentPosition;
        }
        //---------如果是从home退出flag
        if (isHomeReceiver) {
            isHomeReceiver = false;
            return;
        }
        if (pictureInfoList.size() > 0) {

            setViewpagerPosition(position);
            isPlaying = false;
            playPicture();
        }

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
          restore_Picture(true);
          String picUrl=pictureInfoList.get(position).getPicUrl();
          SharePreferenceUtil.setCurrentPlayingPictureUrl(MediaApplication.context,picUrl);
          SharePreferenceUtil.savePictureFileLastModified(AppUtils.getFileLastModifiedTime(picUrl));
    }

    @Override
    public void onPageScrollStateChanged(int state) {


    }

    public class PictureControlListener {
        public void onMenuClick() {
            isEnterList = true;
            Intent intent = new Intent(PicturePlayActivity.this, SwitchActivity.class);
            intent.putExtra("type", 2);
            PicturePlayActivity.this.startActivity(intent);
            overridePendingTransition(0, 0);
            handler.removeMessages(PICTURE_FULL_SCREEN_MODE);
            pausePicture();
        }

        public void onWallpaperClick() {
            pausePicture();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SetWallPaper();
                    sendMsgDelayShowFullScreenMode(5000);
                }
            }).start();

//            final SelfDialogHint dialogHint = new SelfDialogHint(PicturePlayActivity.this);
//            dialogHint.setMessage("图片大小为1280*720？");
//            dialogHint.setTitle("提  示");
//            dialogHint.setNoOnclickListener("取  消", new SelfDialogHint.onNoOnclickListener() {
//                @Override
//                public void onNoClick() {
//                    dialogHint.dismiss();
//                }
//            });
//            dialogHint.setYesOnclickListener("确 定", new SelfDialogHint.onYesOnclickListener() {
//                @Override
//                public void onYesClick() {
//                    dialogHint.dismiss();
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            SetWallPaper();
//                            sendMsgDelayShowFullScreenMode(5000);
//                        }
//                    }).start();
//                }
//            });
//            dialogHint.show();

        }

        public void onPreClick() {
            previous();
            pausePicture();
            sendMsgDelayShowFullScreenMode(5000);

        }

        public void onPlayClick() {
            if (isPlaying)
                pausePicture();
            else
                playPicture();

            sendMsgDelayShowFullScreenMode(5000);
        }

        public void onNextClick() {
            next();
            pausePicture();
            sendMsgDelayShowFullScreenMode(5000);
        }

        public void onScaleClick() {
            int position = pictureData.viewPager.getCurrentItem();
             restore_IsZoonUp_Picture(position);
//            scale_Factor++;
//            if (scale_Factor % 5 == 0) {
//                scale_Factor=0;// todo  还原操作时 scale 归0
//                mAdapter.scale(position, 1 / 16f);
//            } else {
//                mAdapter.scale(position, 2f);
//
//            }
            pausePicture();
            sendMsgDelayShowFullScreenMode(5000);
        }

        public void onRotateClick() {
            restore_Picture(false);
            int position = pictureData.viewPager.getCurrentItem();
            mAdapter.rotate(position);
            pausePicture();
            sendMsgDelayShowFullScreenMode(5000);
        }

    }

    protected void playPicture() {
        if (!isPlaying) {
            LogUtil.i(TAG, "playPicture()...");
            isPlaying = true;
            picturePlaySymbol.setPicturePlaying(true);
            handler.removeMessages(PICTURE_PLAY);
            handler.sendEmptyMessageDelayed(PICTURE_PLAY, 5000);
        }
    }

    //设置壁纸
    public void SetWallPaper() {
        PictureInfo pictureInfo = pictureInfoList.get(pictureData.viewPager.getCurrentItem());
        String pictureUrl = pictureInfo.getPicUrl();
        AppUtils.setWallpaper(MediaApplication.getWallpaperManager(), pictureUrl, new ISetWallpaperListener() {
            @Override
            public void onResult(int resultCode) {
                switch (resultCode) {
                    case ISetWallpaperListener.RESULT_CODE_SUCCESS:
                        sendMsgShowNoticeMessage(1);
                        break;
                    case ISetWallpaperListener.RESULT_CODE_FAIL:
                        sendMsgShowNoticeMessage(0);
                        break;
                }
            }
        });
    }

    private void sendMsgShowNoticeMessage(int msgFlag) {
        handler.removeMessages(PICTURE_SHOW_NOTICE_VIEW);
        handler.obtainMessage(PICTURE_SHOW_NOTICE_VIEW, msgFlag).sendToTarget();
    }

    // 设置壁纸状态提示信息
    private void showNoticeMessage(int msgId) {
        handler.removeMessages(PICTURE_HIDE_NOTICE_VIEW);
        String resTextId = null;
        switch (msgId) {
            case 0:
                resTextId = getString(R.string.wallpaper_warn/*picture_set_wallpaper_fail_info*/);
                break;
            case 1:
                resTextId = getResources().getString(R.string.picture_set_wallpaper_success_info);
                break;
        }
        picturePlaySymbol.setShowNoticeView(true);
        pictureData.showMessageView.setShowMessageText(resTextId);
        sendBroadcast(new Intent(Intent.ACTION_WALLPAPER_CHANGED));
        handler.sendEmptyMessageDelayed(PICTURE_HIDE_NOTICE_VIEW, 2000);
    }

    // 暂停图片播放
    protected void pausePicture() {
        if (isPlaying) {

            isPlaying = false;
            picturePlaySymbol.setPicturePlaying(false);
            handler.removeMessages(PICTURE_PLAY);
        }
    }

    // 上一个图片
    private void previous() {
        int cur_item_position_prev = pictureData.viewPager.getCurrentItem();
        restore_Picture(true);//todo 图片还原 2018-4-9
//        scale_Factor = 0;
        pausePicture();
        if (cur_item_position_prev == 0) {
            //todo 李超超
            cur_item_position_prev = pictureInfoList.size();
            Toast.makeText(PicturePlayActivity.this, "列表结束，回到最后一张", Toast.LENGTH_LONG).show();
//            cur_item_position_prev = 1;
        }
        pictureData.viewPager.setCurrentItem(cur_item_position_prev - 1, false);
    }


    // 下一个图片
    private void next() {
        int cur_item_position_next = pictureData.viewPager.getCurrentItem();
        restore_Picture(true);//todo 图片还原 2018-4-9
//        scale_Factor = 0;
        if (null != mAdapter) {
            int pic_dir_list_size = pictureInfoList.size() - 1;
            // LogUtil.i(TAG, "===> next() cur_item_position_next : " +
            // cur_item_position_next + " , pic_dir_list_size : "
            // + pic_dir_list_size);
            if (cur_item_position_next == pic_dir_list_size) {

                //todo 李超超
                cur_item_position_next = 0;
                pictureData.viewPager.setCurrentItem(cur_item_position_next, false);
                Toast.makeText(PicturePlayActivity.this, "列表结束，回到第一张", Toast.LENGTH_LONG).show();
                // pausePicture();
            } else {
                cur_item_position_next += 1;
                pictureData.viewPager.setCurrentItem(cur_item_position_next, false);
            }
        }
    }


    private class PictureListsHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PICTURE_LIST:
                    pictureInfoList.clear();
                    pictureInfoList.addAll((List<PictureInfo>) msg.obj);
                    mAdapter.setmPictureList(pictureInfoList);
                    //U盘再次插入时 获取上一次的位置
                    setViewpagerPosition(getLastPosition());
                    if (pictureInfoList.size() > 0) {
                        playPicture();
                    }
                    break;
                case PICTURE_PLAY:
                    next();
                    handler.sendEmptyMessageDelayed(PICTURE_PLAY, 5000);
                    break;
                case PICTURE_SHOW_NOTICE_VIEW:
                    showNoticeMessage((Integer) msg.obj);
                    break;
                case PICTURE_HIDE_NOTICE_VIEW:
                    // 隐藏提示窗口视图
                    picturePlaySymbol.setShowNoticeView(false);
                    break;
                case PICTURE_FULL_SCREEN_MODE:
                    // 全屏显示
                    setFullScreen();
                    break;
            }
        }
    }

    private void setViewpagerPosition(int position) {
        mAdapter.notifyDataSetChanged();

        pictureData.viewPager.setCurrentItem(position);
    }


    /**
     * todo lichaochao 2018-4-8
     * 图片放大 还原操作
     */
    private void restore_Picture(boolean isReduction) {
        if (mAdapter==null){
            return;
        }
        int position = pictureData.viewPager.getCurrentItem();
        if (isReduction){
            mAdapter.reduction(position);
            return;
        }
        float scale_value=mAdapter.getScaleMaxValue(position);
        mAdapter.scale(position, 1 / scale_value);


    }

    /**
     *  图片处于放大状态，还原图片
     * @param position
     */
    private void restore_IsZoonUp_Picture(int position){
        float scale_value=mAdapter.getScaleMaxValue(position);
        if (scale_value>=16){
            mAdapter.scale(position, 1 / 16f);
        }else {
            mAdapter.scale(position, 2f);
        }
    }
    private int getLastPosition(){
        if (pictureInfoList!=null&&pictureInfoList.size()>0){
            String playingPictureUrl=SharePreferenceUtil.getCurrentPlayingPictureUrl(MediaApplication.context);
            if (playingPictureUrl==null){
                return 0;
            }
            for (int i=0;i<pictureInfoList.size();i++){
                if (pictureInfoList.get(i).getPicUrl().equals(playingPictureUrl)){
                    return i;
                }
            }
        }
        return 0;
    }

    /**
     * 监听home 广播
     */
    private boolean isHomeReceiver=false;
    BroadcastReceiver home=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)){
                 isHomeReceiver=true;
                 MediaApplication.finishActivity(PicturePlayActivity.class);
            }
        }
    };
    /**
     *
     * 监听断电模式，不需要保存图片activity的flag
     */
     BroadcastReceiver powerIsStop=new BroadcastReceiver() {
         // 0x01：工作模式 0x02：15分钟模式 0x03待机模式 0x04断电模式
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getByteExtra("PowerMode",(byte) 0)==(byte) 4){

                SharePreferenceUtil.saveLastAppFlag(Definition.APP.FLAG_MUSIC);


            }
        }
    };
}
