package com.semisky.automultimedia.common.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.semisky.carlife.CarLife;

import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Created by Anter on 2018/7/5.
 */

public class AdbUtils {

    private static final String TAG = AdbUtils.class.getSimpleName();
    //是否已连接
    public static boolean isConn = false;

    public static void openADB() {
        LogUtil.i(TAG, "openADB() ...");
        if (!isConn) {
            isConn = true;
            try {
                CarLife.getInstance().execLinuxCmd("setprop service.adb.tcp.port 5555 \n");
                CarLife.getInstance().execLinuxCmd("stop adbd \n");
                Thread.sleep(500);
                CarLife.getInstance().execLinuxCmd("start adbd \n");
                LogUtil.i(TAG, "openADB() SUCCESS !!!");
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.w(TAG, "openADB() FAIL !!!");
            }
        }
    }

  
    /**
     * @return 获取ip地址
     */
    public static String getIpAddress(Context ctx) {
        String name = "";

        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            Method method = connectivityManager.getClass().getMethod("getTetheredIfaces");
            String[] names = (String[]) method.invoke(connectivityManager);
            name = names[0];
        } catch (Exception e) {
        }

        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && inetAddress instanceof Inet4Address) {

                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
