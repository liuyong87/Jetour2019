package com.semisky.automultimedia.common.broadRecevier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.semisky.automultimedia.common.strategys.StrategyManager;
import com.semisky.automultimedia.common.utils.LogUtil;

/**
 * Created by liuyong on 18-5-2.
 */

public class UpdateReceiver extends BroadcastReceiver {
    private static final String TAG = UpdateReceiver.class.getSimpleName();
    private static final String UPDATE_ACTION = "com.semisky.broadcast.ACTION_UPDATE";
    private static final String KEY_STATE = "state";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        LogUtil.i(TAG,"onReceive() action="+action);
        if (UPDATE_ACTION.equals(action)) {
            boolean state = intent.getBooleanExtra(KEY_STATE,false);
            LogUtil.i(TAG,"onReceive() state="+state);
            StrategyManager.getInstance().setmUpdateState(state);
            StrategyManager.getInstance().notifyStategyEvent(StrategyManager.EVENT_UPDATE);
        }
    }
}
