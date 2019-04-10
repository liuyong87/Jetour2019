package com.semisky.automultimedia.common.bean;

public class VideoInfo {

    private String videoUrl;//视频Url
    private String videoDisplayName;//	视频名称(带后缀视频名称)
    private String videoTitle;//	视频名称(不带后缀视频名称)
    private String videoTitlePinYing;// 视频名字拼音
    private int videoWidth;//	视频原始宽
    private int videoHeight;//	视频原始高
    private int videoDuration;//	视频总时长

    //是否正常
    private boolean ok;
    //是否在播放
    private boolean playing = false;

    public VideoInfo() {
    }

    public VideoInfo(String videoUrl, String videoDisplayName, String videoTitle, String videoTitlePinYing, boolean isok) {
        super();
        this.videoUrl = videoUrl;
        this.videoDisplayName = videoDisplayName;
        this.videoTitle = videoTitle;
        this.videoTitlePinYing = videoTitlePinYing;
        this.ok = isok;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getVideoDisplayName() {
        return videoDisplayName;
    }

    public void setVideoDisplayName(String videoDisplayName) {
        this.videoDisplayName = videoDisplayName;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public int getVideoWidth() {
        return videoWidth;
    }

    public void setVideoWidth(int videoWidth) {
        this.videoWidth = videoWidth;
    }

    public int getVideoHeight() {
        return videoHeight;
    }

    public void setVideoHeight(int videoHeight) {
        this.videoHeight = videoHeight;
    }

    public int getVideoDuration() {
        return videoDuration;
    }

    public void setVideoDuration(int videoDuration) {
        this.videoDuration = videoDuration;
    }

    public String getVideoTitlePinYing() {
        return videoTitlePinYing;
    }

    public void setVideoTitlePinYing(String videoTitlePinYing) {
        this.videoTitlePinYing = videoTitlePinYing;
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }
    private boolean isValid=true;

    public void setValid(boolean valid) {
        isValid = valid;
    }
    public boolean isValid(){
        return isValid;
    }
}
