package com.semisky.automultimedia.common.utils;

import com.semisky.automultimedia.common.bean.VideoInfo;

import java.util.Comparator;

/**
 * Created by 熊俊 on 2018/1/11.
 */

public class PinyinComparatorByVideo implements Comparator<VideoInfo>{

    public int compare(VideoInfo o1, VideoInfo o2) {
        String left = o1.getVideoTitlePinYing().substring(0, 1);
        String right = o2.getVideoTitlePinYing().substring(0, 1);

        if (left.equals(right)) {
            return 0;
        } else if ("@".equals(left)) {
            return 1;
        } else if ("@".equals(right)) {
            return -1;
        } else if ("#".equals(left)) {
            return 1;
        } else if ("#".equals(right)) {
            return -1;
        } else if("[".equals(left)){
            return 1;
        }else if("[".equals(right)){
            return -1;
        }
        else {
            return left.compareTo(right);
        }
    }


}
