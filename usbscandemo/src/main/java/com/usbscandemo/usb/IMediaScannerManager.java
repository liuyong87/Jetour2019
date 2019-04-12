package com.usbscandemo.usb;

public interface IMediaScannerManager {

    interface OnUSBScanStateListener {

        void onScanStart(int usbFlag);

        void onScanning(int usbFlag);

        void onScanStoped(int usbFlag);

        void onScanDone(int usbFlag);
    }

    void registerOnUSBScanStateListener(OnUSBScanStateListener l);

    /**
     * USB挂载
     *
     * @param usbPath
     */
    void onUSBMounted(String usbPath);

    /**
     * USB卸载
     *
     * @param usbPath
     */
    void onUSBUnMounted(String usbPath);

    /**
     * 获取首个扫描到媒体音乐URL
     *
     * @param usbFlag
     * @return
     */
    String getScanFirstMusicUrl(int usbFlag);

    /**
     * 获取首个扫描到媒体视频URL
     *
     * @param usbFlag
     * @return
     */
    String getScanFirstVideoUrl(int usbFlag);

    /**
     * 获取首个扫描到媒体图片URL
     *
     * @param usbFlag
     * @return
     */
    String getScanFirstPhotoUrl(int usbFlag);
}
