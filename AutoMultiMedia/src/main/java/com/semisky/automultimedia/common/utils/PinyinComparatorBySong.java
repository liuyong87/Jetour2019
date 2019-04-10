package com.semisky.automultimedia.common.utils;


import com.semisky.automultimedia.common.bean.MusicInfo;

import java.util.Comparator;

public class PinyinComparatorBySong implements Comparator<MusicInfo> {

    public int compare(MusicInfo o1, MusicInfo o2) {


            String left = o1.getTitlePinYing().substring(0, 1);
            String right = o2.getTitlePinYing().substring(0, 1);

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
            } else {
                return left.compareTo(right);
            }

    }

}
