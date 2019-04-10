LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_PRELINK_MODULE := false

LOCAL_SRC_FILES := tellmedia_jni.cpp
	
LOCAL_SHARED_LIBRARIES := libnativehelper liblog

LOCAL_MODULE    := libtellmedia
LOCAL_MODULE_TAGS := eng optional

include $(BUILD_SHARED_LIBRARY)
