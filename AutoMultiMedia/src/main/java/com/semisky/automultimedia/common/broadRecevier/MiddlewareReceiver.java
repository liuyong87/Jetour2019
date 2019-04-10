package com.semisky.automultimedia.common.broadRecevier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.semisky.automultimedia.common.constant.Definition;
import com.semisky.automultimedia.common.strategys.StrategyManager;
import com.semisky.automultimedia.common.utils.AppUtils;
import com.semisky.automultimedia.common.utils.LogUtil;
import com.semisky.automultimedia.common.utils.USBManager;
import com.semisky.autoservice.manager.AutoManager;
import com.semisky.voicereceive.AppConstant;

/**
 * Created by liuyong on 18-3-29.
 */

public class MiddlewareReceiver extends BroadcastReceiver {
    private static final String TAG = MiddlewareReceiver.class.getSimpleName();
    private static final String ACTION_MIDDLEWARE = "com.semisky.IS_MEDIA";


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        LogUtil.i(TAG,"onReceive() action = "+action);

        if(ACTION_MIDDLEWARE.equals(action)){
            boolean isMedia = intent.getBooleanExtra("isMedia",false);
            boolean isUsbMounted = USBManager.getInstance().isUsbMounted(Definition.USB_PATH);

            LogUtil.i(TAG,"onReceive() isMedia = "+isMedia+" , isUsbMounted="+isUsbMounted);
            if(isUsbMounted){
                StrategyManager.getInstance().setMeddlewareBreakpointAppState(isMedia);
            }else {
                AppUtils.launcherRadioApp(AppConstant.PKG_MEDIA, AutoManager.FOREGROUND_LAUNCH);
            }
        }
    }
}
