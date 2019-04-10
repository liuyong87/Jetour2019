package com.semisky.automultimedia.common.broadRecevier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.semisky.automultimedia.common.strategys.StrategyManager;
import com.semisky.automultimedia.common.utils.LogUtil;

/**
 * Created by liuyong on 18-3-24.
 */

public class ScreensaverReciver extends BroadcastReceiver {
    private static final String TAG = ScreensaverReciver.class.getSimpleName();
    /**
     * 屏保广播ACTION NAME
     */
    private static final String ACTION_START_SCREEN = "com.semisky.broadcast.SCREEN_START_ACTIVITY";
    /**
     * 屏保广播携带参数KEY NAME
     */
    private static final String START_SCREEN_FLAG = "start_screen_flag";

    /**关屏广播ACTION NAME*/
    public static final String ACTION_START_BASEBOARD = "com.semisky.broadcast.BASEBOARD_START_ACTIVITY";
    /**
     * 关屏广播携带参数KEY NAME
     */
    public static final String START_BASEBOARD_FLAG = "start_baseboard_flag";//1 = 进入；0 = 退出

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if (ACTION_START_SCREEN.equals(action)) {
            int screenFlag = intent.getIntExtra(START_SCREEN_FLAG, 0);
            boolean isOpenScreen = screenFlag > 0 ? true : false;
            StrategyManager.getInstance().setScreensaverState(isOpenScreen);
            StrategyManager.getInstance().notifyStategyEvent(StrategyManager.EVENT_SCREEN_SAVER);
            LogUtil.i(TAG, "onReceive() screenFlag : " + screenFlag + " , isOpenScreen : " + isOpenScreen);
        }else if(ACTION_START_BASEBOARD.equals(action)){
            int closeScreenState = intent.getIntExtra(START_BASEBOARD_FLAG,0);
            boolean isCloseSceen = closeScreenState > 0 ? true:false;
            StrategyManager.getInstance().setmColseScreenState(isCloseSceen);
            StrategyManager.getInstance().notifyStategyEvent(StrategyManager.EVENT_COLSE_SCRREN);
        }

    }
}
