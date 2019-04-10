package com.semisky.automultimedia.common.broadRecevier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.semisky.automultimedia.common.utils.LogUtil;

/**
 * Created by liuyong on 18-4-4.
 */

public class MapReceiver extends BroadcastReceiver {
    private static final String TAG = MapReceiver.class.getSimpleName();
    private static final String AUTONAVI_STANDARD_BROADCAST_SEND = "AUTONAVI_STANDARD_BROADCAST_SEND";
    private static final String EXTRA_STATE = "EXTRA_STATE";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        LogUtil.i(TAG, "action=" + action);
        if (AUTONAVI_STANDARD_BROADCAST_SEND.equals(action)) {
            int mapExtraState = intent.getIntExtra(EXTRA_STATE, -1);
            LogUtil.i(TAG, "mapExtraState=" + mapExtraState);
        }

    }
}
