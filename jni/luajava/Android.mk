LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

# Get the architecture info
ARCH := $(APP_ABI)

LOCAL_C_INCLUDES += $(LOCAL_PATH)/../lua
LOCAL_MODULE     := luajava
LOCAL_SRC_FILES  := luajava.c
LOCAL_STATIC_LIBRARIES := liblua
LOCAL_LDLIBS     := -llog

include $(BUILD_SHARED_LIBRARY)
