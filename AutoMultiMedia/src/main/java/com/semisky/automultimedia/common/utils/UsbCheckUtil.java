package com.semisky.automultimedia.common.utils;

import java.io.File;

/**
 * Created by 熊俊 on 2018/1/2.
 */

public class UsbCheckUtil {
    public static boolean checkUsb(String path) {
        boolean isExsit = false;
        File usbFile = new File(path);
        if (usbFile != null && usbFile.exists()) {
            File[] files = usbFile.listFiles();
            if (files != null && files.length > 0) {
                isExsit = true;
            } else {
                isExsit = false;
            }
        }
        return isExsit;
    }
}
