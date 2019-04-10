package com.semisky.automultimedia.common.musicplay;

import com.semisky.automultimedia.common.constant.Definition;

public class PlayMode {

    public static int getDefault() {
        return Definition.MusicPlayModel.MODE_CIRCLE_ALL;
    }

    public static int switchNextMode(int current) {
        switch (current) {
            case Definition.MusicPlayModel.MODE_CIRCLE_ALL:
                return Definition.MusicPlayModel.MODE_RANDOM;
            case Definition.MusicPlayModel.MODE_RANDOM:
                return Definition.MusicPlayModel.MODE_CIRCLE_SINGL;
            case Definition.MusicPlayModel.MODE_CIRCLE_SINGL:
                return Definition.MusicPlayModel.MODE_CIRCLE_ALL;
        }
        return getDefault();
    }
}
