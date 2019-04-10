package com.semisky.automultimedia.common.utils;

import android.content.Context;
import android.content.pm.PackageInfo;

/**
 * Created by liuyong on 18-4-10.
 */

public class APKVersionCodeUtils {
    /**
     * 获取本地软件版本号
     */
    public static int getVersionCode(Context ctx) {
        int localVersion = 0;
        try {

            PackageInfo packageInfo = ctx.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
            localVersion = packageInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return localVersion;
    }

    /**
     * 获取本地软件版本号名称
     */
    public static String getVersionName(Context ctx) {
        String localVersion = "";
        try {
            PackageInfo packageInfo = ctx.getApplicationContext().getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            localVersion = packageInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return localVersion;
    }
}
