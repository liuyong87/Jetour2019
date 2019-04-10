package com.semisky.automultimedia.aidl;


interface IMultimediaAidl {

    /**
    *   检查媒体数据
    **/
    boolean hasMediaData();
    /**
    *   检查媒体数据
    **/
    boolean hasMediaDataByAppointFlag(int appFlag);
    /**
    * 获取断点记忆多媒体应用标记
    **/
    int getAppFlag();
    /**
    * 处理媒体数据异常
    **/
    void handlerMediaDataException();


}
