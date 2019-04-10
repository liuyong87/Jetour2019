package com.semisky.automultimedia.common.broadRecevier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.semisky.automultimedia.common.utils.LogUtil;

import static com.semisky.automultimedia.common.constant.Definition.ACTION_MODE_PLAY_MUSIC;
import static com.semisky.automultimedia.common.constant.Definition.USB_MOUNT_SERVICE_CLZ;
import static com.semisky.automultimedia.common.constant.Definition.USB_MOUNT_SERVICE_PKG;

/**
 * Created by liuyong on 18-3-2.
 */

public class ModeReciver extends BroadcastReceiver {

    private static final java.lang.String TAG = ModeReciver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        LogUtil.i(TAG, "action=" + action);
        if (ACTION_MODE_PLAY_MUSIC.equals(action)) {
            String start_app_flag = intent.getStringExtra("start_app_flag");
            LogUtil.i(TAG, "start_app_flag=" + start_app_flag);
            modePlayMusic(context, start_app_flag);
        }
    }

    // 方控模式切换至音乐意图
    public void modePlayMusic(Context context, String flag) {
        Intent intent = new Intent();
        intent.setClassName(USB_MOUNT_SERVICE_PKG, USB_MOUNT_SERVICE_CLZ);
        intent.setAction(ACTION_MODE_PLAY_MUSIC);
        intent.putExtra("flag", flag);
        context.startService(intent);
    }
}
