package com.semisky.automultimedia.common.interfaces;

/**
 * Created by liuyong on 18-2-27.
 */

public interface ISetWallpaperListener {
    final int RESULT_CODE_FAIL = 0;
    final int RESULT_CODE_SUCCESS = 1;

    void onResult(int resultCode);
}
