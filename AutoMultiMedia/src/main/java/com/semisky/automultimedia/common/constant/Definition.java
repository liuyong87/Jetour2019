package com.semisky.automultimedia.common.constant;

import com.semisky.automultimedia.activity.PicturePlayActivity;

/**
 * Created by chenhongrui on 2017/12/8
 * <p>
 * 内容摘要：
 * 版权所有：Semisky
 * 修改内容：
 * 修改日期
 */
public class Definition {
    public static final int UNKNOW = 0;

    /*强制关闭U盘加载弹窗service action*/
    public static final String ACTION_COLSE_USB_CONNECT_DIALOG = "com.semisky.service.ACTION_COLSE_USB_CONNECT_DIALOG";
    /*导航*/
    public static final String PKG_NAVI = "com.winmu.autoNavi";
    public static final String CLS_NAVI = "ritu.navi.main.Navigation";

    public static final String USB_PATH = "/storage/udisk";
    public static final String ACTION_USB_MOUNT = "com.semisky.action.USB_MOUNT";
    public static final String ACTION_LAUNCHER_START = "com.semisky.action.LAUNCHER_START_MEDIA";
    // 后台播放音乐广播ACTION名字
    public static final String ACTION_MODE_PLAY_MUSIC = "com.semisky.broadcast.MUSIC_START_ACTIVITY";
    public static final String USB_MOUNT_SERVICE_PKG = "com.semisky.automultimedia";
    public static final String USB_MOUNT_SERVICE_CLZ = "com.semisky.automultimedia.service.DeviceMountService";
    public static final String USB_MULTIMEDIA_PKG = "com.semisky.automultimedia";
    public static final String USB_MUSIC_SERVICE_CLZ = "com.semisky.automultimedia.service.MusicPlayService";
    // 音乐意图参数命令字段
    public static final String PARAM_CMD = "cmd";
    public static final int CMD_INVALID = -65536;



    // 后台播放音乐服务Action名字
    public static final String ACTION_PLAY_TOGGLE = "com.semisky.music.ACTION_PLAY_TOGGLE";
    public static final String ACTION_PLAY_START = "com.semisky.music.ACTION_PLAY_START";
    public static final String ACTION_PLAY_PAUSE = "com.semisky.music.ACTION_PLAY_PAUSE";
    public static final String ACTION_PLAY_PREV = "com.semisky.music.ACTION_PLAY_PREV";
    public static final String ACTION_PLAY_NEXT = "com.semisky.music.ACTION_PLAY_NEXT";
    public static final String ACTION_PLAY_LIST = "com.semisky.music.ACTION_PLAY_LIST";
    public static final String ACTION_PLAY_APPOINT="com.semisky.music.ACTION_PLAY_INDEX";
    // 音乐服务状态广播
    public static final String ACTION_MUSIC_SERVICE_STATE_CHANGE = "com.semisky.broadcast.ACTION_MUSIC_SERVICE_STATE_CHANGE";
    public static final String KEY_MUSIC_SERVICE_STATE = "state";// true ：表示音乐服务启动，false 表示音乐服务未启动。

    public static final int MSG_REGISTER = 1;
    public static final int MSG_UNREGISTER = 2;
    public static final int MSG_REGISTER_SUCCESS = 3;
    public static final int MSG_UNREGISTER_SUCCESS = 4;

    public static final int MSG_SERVICE_TO_ACTIVITY = 11;
    public static final int MSG_ACTIVITY_TO_SERVICE = 12;
    public static final int MSG_ADAPTER_TO_SERVICE = 13;
    public static final int MSG_SERVICE_TO_ADAPTER = 14;
    public static final int EVENT_SERVICE_TO_ACTIVITY = 15;

    public class APP{
        public static final int FLAG_MUSIC = 1;
        public static final int FLAG_VIDEO = 2;
        public static final int FLAG_PICTURE = 3;
    }

    public class SymbolToActivity {
        public static final int UNKNOW = 0x0000;
        public static final int GET_DATA_FOR_VIEW = 0x0001;
    }

    public class ActionId {
        public static final int UNKNOW = 0x0000;
        public static final int MUTE = 0x0001;
        public static final int UNMUTE = 0x0002;
        public static final int PREVIOUS = 0x0003;
        public static final int SEEK = 0x0004;
        public static final int RADIO_PLAY = 0x0005;
        public static final int RADIO_NEXT_FREQ = 0x0006;
        public static final int RADIO_LAST_FREQ = 0x0007;
        public static final int RADIO_COLLECT = 0x0008;
        public static final int RADIO_CANCEL_COLLECT = 0x0009;
        public static final int RADIO_DEGREE_OL = 0x000A;
        public static final int RADIO_DEGREE_DX = 0x000B;
        public static final int SEEK_DATA_FM = 0x000C;
        public static final int SEEK_DATA_AM = 0x000D;
    }

    public class NativeNotice {
        public static final int SET_MUTE_NOTICE = 0;
        public static final int PLAY_FREQ_NOTICE = 1;
        public static final int START_SCAN_NOTICE = 2;
        public static final int OPEN_DEV_NOTICE = 3;
        public static final int CLOSE_DEV_NOTICE = 4;
    }

    public class StationType {
        public static final int STATION_TYPE_UNKNOW = 0;
        public static final int STATION_TYPE_FM = 1;
        public static final int STATION_TYPE_AM = 2;
    }

    public class Numerical {
        public static final int FM_MAX_FREQ = 10800;
        public static final int FM_MIN_FREQ = 8750;
        public static final int AM_MAX_FREQ = 1629;
        public static final int AM_MIN_FREQ = 531;
    }

    public class DeviceMessge{
        public static final int FILE_SCANRESULT = 112;
        public static final int GET_FILE_SCANRESULT = 116;
    }

    public class MusicMessge{
        public static final int MUSIC_LIST = 11;
        public static final int FOLDER_MUSIC_LIST = 12;
        //service给activity信息
        public static final int CURRENT_PLAY_MUSIC = 600;
        public static final int PLAY_STATE = 610;
        public static final int PROGRESS = 620;
        public static final int NEXT_MUSICINFO = 630;
        public static final int CURRENT_POSITION = 640;
        public static final int FAST_BACK_FORWARD_LOSS_EFFECT = 650;
        public static final int FOREVER_AUDIOFOCUS_LOSS = 651;
        public static final int MEDIA_PALYER_RESET = 660;
        //Activity给service的命令
        public static final int PLAYORPAUSE_MUSIC = 100;
        public static final int MUSIC_START = 101;
        public static final int MUSIC_PAUSE = 102;
        public static final int GET_CURRENT_PLAY_MUSIC = 110;
        public static final int NEXT_MUSIC = 120;
        public static final int PRE_MUSIC = 130;
        public static final int PLAY_URL_MUSIC = 140;
        public static final int PLAY_MODE = 150;
        public static final int FAST_FORWARDS = 160;
        public static final int BACK_FORWARDS = 170;
        public static final int SEEK_TO_PROGRESS = 180;
        public static final int START_UPDATE_PROGRESS = 181;
        public static final int PAUSE_UPDATE_PROGRESS = 182;
        public static final int PLAY_MUSIC_LIST = 190;
        public static final int USER_SELCT_MUSIC_URL = 196;
        public static final int REMOVE_ALL_NEXT_PREV_EVENT_MSG = 200;

        //activity内部使用
        public static final int FAST_FORWARD = 210;
        public static final int BACK_FORWARD = 220;
    }

    public class AudioFocusConstants{
        public static final int NO_INVLID = -1;
        public static final int RESET = 0;
        public static final int FOREVER_LOSS = 1;
        public static final int TRANSIENT_LOSS = 2;
    }

    public class MusicPlayModel{
        public static final int MODE_RANDOM = 0;
        public static final int MODE_CIRCLE_ALL = 1;
        public static final int MODE_CIRCLE_SINGL = 2;
    }

    public class VideoMessge{
        public static final int VEDIO_LIST = 11;
        public static final int UPDATE_PROGRESS = 12;
        public static final int FAST_FORWARD = 13;
        public static final int FAST_BACKWARD = 14;
        public static final int HIDE_PROGRESSBAR = 15;
        public static final int SHOW_PROGRESSBAR = 16;
        public static final int MSG_NEXT = 17;
        public static final int MSG_PREV = 18;
    }

    public class PictureMessge{
        public static final int PICTURE_LIST = 11;
        public static final int PICTURE_PLAY = 12;
        public static final int PICTURE_SHOW_NOTICE_VIEW = 13;
        public static final int PICTURE_HIDE_NOTICE_VIEW = 14;
        public static final int PICTURE_FULL_SCREEN_MODE = 15;
    }
}
