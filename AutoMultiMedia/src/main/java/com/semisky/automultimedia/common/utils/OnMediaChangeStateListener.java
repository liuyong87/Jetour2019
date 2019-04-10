package com.semisky.automultimedia.common.utils;

/**
 * 媒体播放器控制监听接口
 * @author liuyong
 */
public interface OnMediaChangeStateListener {
	/**
	 * 媒体重置
	 */
	void onMediaReset();
	/**
	 * 准备媒体资源
	 */
	void onMediaPrepare();
	/**
	 * 准备媒体资源完成
	 */
	void  onMediaPrepareFinish();
	/**
	 * 媒体开始播放
	 */
	void onMediaStart();
	/**
	 * 媒体准备失败
	 */
	void onMediaPrepareFail(int what, int extra);
	/**
	 * 媒体更新当前进度
	 */
	void onUpdateProgress(int progress);
	/**
	 * 媒体暂停
	 */
	void onMediaPause();
	/**
	 * 媒体停止
	 */
	void onMediaStop();
	/**
	 * 媒体播放完成
	 */
	void onMediaPlayCompletion();
	/**
	 * 注销音频焦点
	 */
	void onAbandonAudiofocus();
	/**
	 * 媒体指示与警告信息
	 * @param what
	 * what the type of info or warning. 
	, MEDIA_INFO_UNKNOWN 
	, MEDIA_INFO_VIDEO_TRACK_LAGGING 
	, MEDIA_INFO_VIDEO_RENDERING_START 
	, MEDIA_INFO_BUFFERING_START 
	, MEDIA_INFO_BUFFERING_END 
	, MEDIA_INFO_BAD_INTERLEAVING 
	, MEDIA_INFO_NOT_SEEKABLE 
	, MEDIA_INFO_METADATA_UPDATE 
	@param extra
	extra an extra code, specific to the info. Typically implementation dependent.
	 * 
	 */
	void onMediaInfo(int what, int extra);
}
