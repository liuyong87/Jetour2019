package com.semisky.automultimedia.common.broadRecevier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.semisky.automultimedia.common.strategys.StrategyManager;
import com.semisky.automultimedia.common.utils.LogUtil;

/**
 * Created by liuyong on 18-5-3.
 */

public class AVMReceiver extends BroadcastReceiver {
    private static final String TAG = AVMReceiver.class.getSimpleName();
    private static final String ACTION_AVM = "com.semisky.IS_AVM";
    private static final String KEY_AVM_STATE = "isAVM";
    private static final String ACTION_AVM_CLOSE = "com.semisky.IS_AD_CLOSE";
    private static final String KEY_AD_CLOSE_STATE = "isClose";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        LogUtil.i(TAG,"onReceive() action="+action);

        if(ACTION_AVM.equals(action)){
            boolean isAvm = intent.getBooleanExtra(KEY_AVM_STATE,false);
            if(isAvm){
                StrategyManager.getInstance().setmAVMState(true);
                StrategyManager.getInstance().notifyStategyEvent(StrategyManager.EVENT_AVM);
            }
            LogUtil.i(TAG,"onReceive() isAvm="+isAvm);

        }else if (ACTION_AVM_CLOSE.equals(action)){
            boolean isAvmColse = intent.getBooleanExtra(KEY_AD_CLOSE_STATE,false);
            if(isAvmColse){
                StrategyManager.getInstance().setmAVMState(false);
                StrategyManager.getInstance().notifyStategyEvent(StrategyManager.EVENT_AVM);
            }
            LogUtil.i(TAG,"onReceive() isAvmColse="+isAvmColse);
        }
    }
}
