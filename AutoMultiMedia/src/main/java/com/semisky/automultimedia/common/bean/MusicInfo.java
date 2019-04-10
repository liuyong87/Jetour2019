package com.semisky.automultimedia.common.bean;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class MusicInfo implements Serializable {
    private static final long serialVersionUID = 123L;
    // 路径
    private String url;
    //是否是文件夹
    private boolean isDir;
    // 歌曲名（不包含后缀）
    private String displayName;
    // 歌曲名（包含后缀）
    private String title;
    //歌曲名全拼
    private String titlePinYing;
    // 演唱者
    private String artist;
    //专辑封面
    private byte[] albumPicture;
    // 演唱者全拼
    private String artistPinYing;
    // 专辑
    private String album;
    // 时长
    private int duration;
    //是否正常
    private boolean ok;
    //是否在播放
    private boolean playing = false;


    public MusicInfo() {
        super();
        // TODO Auto-generated constructor stub
    }


    public MusicInfo(String url, String displayName, String title,
                     String titlePinYing, String artist, String album, byte[] albumPicture,int duration,boolean ok) {
        super();
        this.url = url;
        this.displayName = displayName;
        this.title = title;
        this.titlePinYing = titlePinYing;
        this.artist = artist;
        this.album = album;
        this.albumPicture = albumPicture;
        this.duration = duration;
        this.ok = ok;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitlePinYing() {
        return titlePinYing;
    }

    public void setTitlePinYing(String titlePinYing) {
        this.titlePinYing = titlePinYing;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getArtistPinYing() {
        return artistPinYing;
    }

    public void setArtistPinYing(String artistPinYing) {
        this.artistPinYing = artistPinYing;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public byte[] getAlbumPicture() {
        return albumPicture;
    }

    public void setAlbumPicture(byte[] albumPicture) {
        this.albumPicture = albumPicture;
    }

    public boolean getIsDir() {
        return isDir;
    }

    public void setIsDir(boolean dirInfo) {
        this.isDir = dirInfo;
    }
}
