package com.semisky.automultimedia.common.broadRecevier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.semisky.automultimedia.common.constant.Definition;
import com.semisky.automultimedia.common.utils.LogUtil;


/**
 * 工厂测试广播
 * Created by liuyong on 18-4-27.
 */

public class FactoryTestReceiver extends BroadcastReceiver {
    private static final String TAG = FactoryTestReceiver.class.getSimpleName();
    private Context mContext;
    public static final String ACTION_USB_MUSIC = "com.semisky.autotest.ACTION_USB_MUSIC_PLAY";//音乐广播action
    public static final String ACTION_USB_VIDEO = "com.semisky.autotest.ACTION_USB_VIDEO_PLAY";//视频广播action
    //音乐与视频共用键与值
    // 1.)command < 0 :表示控命令全部用负数表示;2.)command > 0 :表示节目列表条目position
    public static final String KEY_USB = "state";//命令值 cmd
    public static final int STATE_PLAY = -1000;//视频或音乐==》播放
    public static final int STATE_STOP = -1001;//视频或音乐==》暂停
    public static final int STATE_PRE = -1002;//视频或音乐==》上一个节目
    public static final int STATE_NEXT = -1003;//视频或音乐==》下一个节目
    public static final int STATE_INVALID = -1;//默认无效

    @Override
    public void onReceive(Context context, Intent intent) {
        this.mContext = context;

        String action = intent.getAction();
        LogUtil.i(TAG, "onReceive() action=" + action);

        if (ACTION_USB_MUSIC.equals(action)) {
            int cmd = intent.getIntExtra(KEY_USB,STATE_INVALID);
            LogUtil.i(TAG, "onReceive() cmd=" + cmd);

            switch (cmd){
                case STATE_PLAY:
                    startMusicService(Definition.ACTION_PLAY_START);
                    break;
                case STATE_STOP:
                    startMusicService(Definition.ACTION_PLAY_PAUSE);
                    break;
                case STATE_PRE:
                    startMusicService(Definition.ACTION_PLAY_PREV);
                    break;
                case STATE_NEXT:
                    startMusicService(Definition.ACTION_PLAY_NEXT);
                break;
                default:
                    // 大于表示指定列表index的歌曲
                    if(cmd > 0){
                        startMusicService(Definition.ACTION_PLAY_APPOINT,cmd-1);
                    }
                    break;
            }

        }else if(ACTION_USB_VIDEO.equals(action)){
            int cmd = intent.getIntExtra(KEY_USB,STATE_INVALID);
            VideoFactoryTestManager.getInstance().notifyReceiverCommand(cmd);
        }
    }

    private void startMusicService(String action) {
        LogUtil.i(TAG,"====startMusicService() action="+action);
        Intent intent = new Intent();
        intent.setAction(action);
        intent.setClassName(Definition.USB_MULTIMEDIA_PKG, Definition.USB_MUSIC_SERVICE_CLZ);
        mContext.startService(intent);
    }

    private void startMusicService(String action,int index) {
        LogUtil.i(TAG,"====startMusicService() action="+action+",index="+index);
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("index",index);
        intent.setClassName(Definition.USB_MULTIMEDIA_PKG, Definition.USB_MUSIC_SERVICE_CLZ);
        mContext.startService(intent);
    }
}
