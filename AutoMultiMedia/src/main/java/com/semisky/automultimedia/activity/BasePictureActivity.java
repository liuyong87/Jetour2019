package com.semisky.automultimedia.activity;

import android.support.v4.app.FragmentActivity;

import java.util.HashMap;

/**
 * Created by liuyong on 18-3-8.
 */

public abstract class BasePictureActivity extends FragmentActivity {
    // 设置正常屏显示
    protected abstract void setNormalScreen();
    // 设置全屏显示
    protected abstract void setFullScreen();
    // 发送消息延时全屏显示
    protected abstract void sendMsgDelayShowFullScreenMode(int delayMillis);

    // 显示模式成员
    public String DISPLAY_MODE_KEY = "display_mode";
    public boolean DISPLAY_MODE_FULL_SCREEN = true;
    public boolean DISPLAY_MODE_NORMAL_SCREEN = false;

    private HashMap<String, Boolean> mBooleanStateMaps = new HashMap<String, Boolean>();

    public void putBooleanState(String key, boolean value) {
        mBooleanStateMaps.put(key, value);
    }

    public boolean getBooleanState(String key) {
        if (mBooleanStateMaps.containsKey(key)) {
            return mBooleanStateMaps.get(key);
        }
        return false;
    }

    // 清除状态管理缓存
    public void removeAllStateManagerCache(){
        mBooleanStateMaps.clear();
    }
}
