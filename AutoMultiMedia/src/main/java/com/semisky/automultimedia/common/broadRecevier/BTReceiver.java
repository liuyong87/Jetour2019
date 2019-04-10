package com.semisky.automultimedia.common.broadRecevier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.semisky.automultimedia.common.strategys.StrategyManager;
import com.semisky.automultimedia.common.utils.LogUtil;

/**
 * Created by liuyong on 18-3-26.
 */

public class BTReceiver extends BroadcastReceiver {
    private static final String TAG = BTReceiver.class.getSimpleName();
    public static final String ACTION_CALL_STATE_CHANGED = "com.semisky.cx62.bluetooth.adapter.action.ACTION_CALL_STATE_CHANGED";
    public static final String EXTRA_CALL_ACTIVE = "com.semisky.cx62.bluetooth.adapter.extra.EXTRA_CALL_ACTIVE";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        LogUtil.i(TAG,"onReceive() action="+action);
        if(ACTION_CALL_STATE_CHANGED.equals(action)){
            boolean btCallState = intent.getBooleanExtra(EXTRA_CALL_ACTIVE,false);
            LogUtil.i(TAG,"onReceive() btCallState="+btCallState);
            StrategyManager.getInstance().setBTCallState(btCallState);
            StrategyManager.getInstance().notifyStategyEvent(StrategyManager.EVENT_BT_CALL);
        }

    }
}
