package com.semisky.automultimedia.common.utils;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

public class ActivityManagerUtils {
    private static final String TAG = "ActivityManagerUtils";
    private static ActivityManagerUtils mActivityManagerUtils = null;

    public static ActivityManagerUtils getInstance() {
        if (null == mActivityManagerUtils) {
            mActivityManagerUtils = new ActivityManagerUtils();
        }
        return mActivityManagerUtils;
    }

    public String getTastActivity(Context mContext, int taskActivityByIndex,
                                  int maxRunningTask) {
        String curActivityName = null;
        ActivityManager mActivityManager = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> mRunningTaskInfo = mActivityManager
                .getRunningTasks(maxRunningTask);
        if (mRunningTaskInfo.size() <= 0) {
            return "";
        }
        RunningTaskInfo cinfo = mRunningTaskInfo
                .get(taskActivityByIndex);
        ComponentName mComponentName = cinfo.topActivity;
        curActivityName = mComponentName.getClassName();
        return curActivityName;
    }

    public boolean getTopActivity(Context mContext, String clz,
                                  int taskActivityByIndex, int maxRunningTask) {
        String curActivityName = null;
        ActivityManager mActivityManager = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> mRunningTaskInfo = mActivityManager
                .getRunningTasks(maxRunningTask);
        RunningTaskInfo cinfo = mRunningTaskInfo
                .get(taskActivityByIndex);
        ComponentName mComponentName = cinfo.topActivity;
        curActivityName = mComponentName.getClassName();

        if (null != clz || !("".equals(clz))) {
            if (clz.equals(curActivityName)) {
                return true;
            }
        }

        return false;
    }

}
