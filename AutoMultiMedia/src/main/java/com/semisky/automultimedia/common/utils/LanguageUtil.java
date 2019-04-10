package com.semisky.automultimedia.common.utils;

import android.app.backup.BackupManager;
import android.content.res.Configuration;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

/**
 * Created by Anter on 2018/7/27.
 */

public class LanguageUtil {
    private static final String TAG = LanguageUtil.class.getSimpleName();

    /**
     * 切换语言
     *
     * @param locale
     */
    public static void updateLanguage(Locale locale) {
        LogUtil.i(TAG,"updateLanguage() ..."+locale.getLanguage());
        //参考frameworks/base/core/java/com/android/internal/app/LocalePicker.java  中的updateLocale()方法
        try {
            Object am;
            //获取类
            Class IActivityManager = Class.forName("android.app.IActivityManager");
            Class ActivityManagerNative = Class.forName("android.app.ActivityManagerNative");
            Class clzConfig = Class.forName("android.content.res.Configuration");
            //获取方法
            Method ActivityManagerNative$getDefault = ActivityManagerNative.getDeclaredMethod("getDefault");
            Method IActivityManager$getConfiguration = IActivityManager.getDeclaredMethod("getConfiguration");
            Method IActivityManager$updateConfiguration = IActivityManager.getDeclaredMethod("updateConfiguration", Configuration.class);
            //获取字段
            Field userSetLocale = clzConfig.getDeclaredField("userSetLocale");

            //am = ActivityManagerNative.getDefault();
            am = ActivityManagerNative$getDefault.invoke(ActivityManagerNative);
            //config = am.getConfiguration();
            Configuration config = (Configuration) IActivityManager$getConfiguration.invoke(am);

            config.setLocale(locale);
            userSetLocale.set(config, true);//持久化  config.userSetLocale = true; 这样做的目的是下次开机之后仍然使用改变后的语言
            //am.updateConfiguration(config);
            IActivityManager$updateConfiguration.invoke(am, config);

            BackupManager.dataChanged("com.android.providers.settings");
        } catch (Exception e) {
            LogUtil.e("LanguageUtil", e.toString());
        }
    }
}
