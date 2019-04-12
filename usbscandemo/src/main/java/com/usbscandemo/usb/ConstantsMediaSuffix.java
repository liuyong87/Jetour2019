package com.usbscandemo.usb;

/**
 * 媒体后缀
 * Created by liuyong on 18-4-25.
 */

public class ConstantsMediaSuffix {

    /**
     * 媒体后缀类型枚举
     */
    public enum MediaSuffixType {
        SUFFIX_TYPE_AUDIO,
        SUFFIX_TYPE_VIDEO,
        SUFFIX_TYPE_PHOTO
    }

    /**
     * 音频后缀
     */
    public static final String[] SUFFIX_ARRAY_AUDIO = {
            ".mp3",
            ".wma",
            ".aac",
            ".wav",
            ".ape",
            ".ogg",
            ".flac",
            ".midi",
            ".aif",
            ".aifc",
            ".aiff",
            ".acm",
            ".m4a",
            ".wmv",
            ".wam",
            ".ra",
            ".amr",
            ".ac3"

    };

    /**
     * 视频后缀
     */
    public static final String[] SUFFIX_ARRAY_VIDEO = {
            ".mp4",
            ".avi",
            ".mkv",
            ".rmvb",
            ".mpeg",
            ".flv",
            ".3gp",
            ".3g2",
            ".mov",
            ".mpe",
            ".asx",
            ".m4v",
            ".ts",
            ".mpg",
            ".vob",
            ".bup",
            ".ifo",
            ".vop",
            ".rm",
            ".wmv",
            ".divx",
            ".asf",
            ".f4v",
            ".3gpp",
            ".trp",
            ".webm",
            ".dat"
    };

    /**
     * 图片后缀
     */
    public static String[] SUFFIX_ARRAY_PHOTO = {".png", ".jpg", ".bmp", ".gif", ".jpeg"};

    /**
     * 即是音频也是视频文件后缀的
     */
    public static final String[] SUFFIX_ARRAY_AUDIO_VIDEO = {
            ".wmv"
    };

    /**
     * 忽略扫描文件夹名字
     * 注：<br>
     * "IXNavi","CityMap" 地图文件夹名<br>
     * "System Volume Information",".Trashes" u盘默认生成文件夹名<br>
     */
    public static final String[] IGNORE_SCANN_FOLDER_NAME = {"System Volume Information", ".Trashes", "IXNavi", "CityMap"};
    /**
     * 忽略扫描视频文件后缀名
     */
    public static final String[] IGNORE_SUFFIX_ARRAY_VIDEO = {".ini"};
}
