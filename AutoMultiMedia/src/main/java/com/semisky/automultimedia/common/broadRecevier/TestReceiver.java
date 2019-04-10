package com.semisky.automultimedia.common.broadRecevier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.semisky.automultimedia.common.utils.APKVersionCodeUtils;
import com.semisky.automultimedia.common.utils.LanguageUtil;
import com.semisky.automultimedia.common.utils.LogUtil;

import java.util.Locale;

/**
 * Created by liuyong on 18-4-10.
 */

public class TestReceiver extends BroadcastReceiver {
    private static final String TAG = TestReceiver.class.getSimpleName();
    private static final String ACTION_GET_APP_INFO = "com.semisky.broadcast.action.GET_APP_INFO";
    private static final String ACTION_LANGUAGE = "com.semisky.broadcast.ACTION_LANGUAGE_CHANGE";
    private static final int EN = 0;
    private static final int ZH = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        LogUtil.d(TAG, "onReceive() action=" + action);
        if (ACTION_GET_APP_INFO.equals(action)) {
            int appVersion = APKVersionCodeUtils.getVersionCode(context);
            String appVersionName = APKVersionCodeUtils.getVersionName(context);
            LogUtil.d(TAG, "===================================");
            LogUtil.d(TAG, "appVersionName =\t" + appVersionName + "\nappVersion =\t" + appVersion);
            LogUtil.d(TAG, "===================================");
            Toast.makeText(context, "版本名称《"+appVersionName+"》", Toast.LENGTH_LONG).show();
        }if(ACTION_LANGUAGE.equals(action)){
            int state = intent.getIntExtra("state",-1);
            LogUtil.d(TAG, "=============ACTION_LANGUAGE======================"+state);
            switch (state){
                case EN:
                    LogUtil.d(TAG, "=============ENGLISH======================");
                    updateLanguage(Locale.ENGLISH);
                    break;
                case ZH:
                    LogUtil.d(TAG, "=============SIMPLIFIED_CHINESE======================");
                    updateLanguage(Locale.SIMPLIFIED_CHINESE);
                    break;
            }
        }
    }

    private  void updateLanguage(final Locale locale){
        new Thread(new Runnable() {
            @Override
            public void run() {
                LogUtil.d(TAG, "SET LANGUAGE [ "+locale.getLanguage()+" ]");
                LanguageUtil.updateLanguage(locale);
            }
        }).start();
    }

}
