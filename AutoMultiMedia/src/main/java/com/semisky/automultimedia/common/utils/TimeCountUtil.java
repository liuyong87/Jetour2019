package com.semisky.automultimedia.common.utils;

/**
 * Created by liuyong on 18-3-7.
 */

public class TimeCountUtil {
    private static TimeCountUtil INSTANCE;
    private long firstTime = 0;
    private long lastTime = 0;


    public static TimeCountUtil getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new TimeCountUtil();
        }
        return INSTANCE;
    }

    /**
     * 记录首个时间
     */
    public void recordFirstTime() {
        this.firstTime = System.currentTimeMillis();
    }

    /**
     * 记录最新时间
     */
    public void recordLastTime() {
        this.lastTime = System.currentTimeMillis();
    }

    /**
     * 获取当前时间
     *
     * @return
     */
    public long getCurTimeDifference() {
        return (lastTime - firstTime);
    }

}
