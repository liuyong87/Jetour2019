package com.semisky.automultimedia.common.broadRecevier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.semisky.automultimedia.common.strategys.StrategyManager;
import com.semisky.automultimedia.common.utils.LogUtil;

/**
 * Created by liuyong on 18-4-19.
 */

public class IFlytekVoiceReceiver extends BroadcastReceiver {
    private static final String TAG = IFlytekVoiceReceiver.class.getSimpleName();
    public static final String ACTION_START_VOICE = "com.semisky.broadcast.VOICE_START_ACTIVITY";
    public static final String START_VOICE_FLAG = "start_voice_flag";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        LogUtil.i(TAG,"onReceive() action="+action);
        if(ACTION_START_VOICE.equals(action)){

            boolean iflytekVoiceState = intent.getBooleanExtra(START_VOICE_FLAG,false);
            LogUtil.i(TAG,"onReceive() iflytekVoiceState="+iflytekVoiceState);

            StrategyManager.getInstance().setIFlytekVoiceState(iflytekVoiceState);
            StrategyManager.getInstance().notifyStategyEvent(StrategyManager.EVENT_IFLYTEK_VOICE);
        }
    }
}
