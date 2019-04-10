package com.semisky.automultimedia.common.bean;

public class PictureInfo {

	private String picUrl;
	private String picTitle;// 图片名称(不带后缀视频名称)

	public PictureInfo() {
		super();
	}

	public PictureInfo(String picUrl, String picTitle) {
		super();
		this.picUrl = picUrl;
		this.picTitle = picTitle;
	}

	public String getPicUrl() {
		return picUrl;
	}

	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}

	public String getPicTitle() {
		return picTitle;
	}

	public void setPicTitle(String picTitle) {
		this.picTitle = picTitle;
	}

}
