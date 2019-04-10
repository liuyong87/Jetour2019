#include "JNIHelp.h"
#include "jni.h"
#include "utils/Log.h"

static const char *TAG = "tellmedia_jni";

#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

unsigned char is_continue_tellmedia = 0;

unsigned char tellmedia_state = 0;

pthread_mutex_t tellmedia_mutex;

pthread_cond_t tellmedia_cond;

extern "C" void sigroutine(int signo) 
{ 
	switch (signo) 
	{ 
		case SIGIO: 
			LOGE("tellmedia Catch a signal -- SIGIO "); 

			tellmedia_state = 1;

			is_continue_tellmedia = 1;

			pthread_cond_signal(&tellmedia_cond);

			break; 

		case SIGINT: 

			LOGE("tellmedia Catch a signal -- SIGINT "); 

			tellmedia_state = 2;

			is_continue_tellmedia = 1;

			pthread_cond_signal(&tellmedia_cond);
			
			break; 
	};
	
	return; 
} 

static jint com_semisky_automultimedia_tellmedia_TellMedia_selectTellmedia(JNIEnv *env, jobject clazz)
{
	int state;

	pthread_mutex_lock(&tellmedia_mutex);
	
	while(!is_continue_tellmedia)
	{
		pthread_cond_wait(&tellmedia_cond, &tellmedia_mutex);
	};

	state = tellmedia_state;

	tellmedia_state = 0;

	is_continue_tellmedia = 0;
		
	pthread_mutex_unlock(&tellmedia_mutex);

    return (jint)state;
}


/*
 * JNI registration.
 */
static JNINativeMethod gMethods[] = {
    /* name, signature, funcPtr */
    { "select_tellmedia", "()I", (void*) com_semisky_automultimedia_tellmedia_TellMedia_selectTellmedia },
};

int register_semisky_tellmedia_TellMedia(JNIEnv* env)
{// com.semisky.automultimedia.tellmedia.TellMedia
    return jniRegisterNativeMethods(env, "com/semisky/automultimedia/tellmedia/TellMedia",
            gMethods, NELEM(gMethods));
}

extern "C" jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    JNIEnv* env = NULL;
    jint result = -1;
    
    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        ALOGE("GetEnv failed!");
        return result;
    }

    ALOG_ASSERT(env, "Could not retrieve the env!");

	ALOGE("tellmedia begin install tellmedia Signal");
		
	if(signal(SIGIO, sigroutine)==SIG_ERR)
	{
		ALOGE("tellmedia install SIGIO Signal error %s", strerror(errno));
	}

	if(signal(SIGINT, sigroutine)==SIG_ERR)
	{
		ALOGE("tellmedia install SIGINT Signal error %s", strerror(errno));
	}

	register_semisky_tellmedia_TellMedia(env);

	pthread_mutex_init(&tellmedia_mutex, NULL);

	pthread_cond_init(&tellmedia_cond, NULL);

	is_continue_tellmedia = 0;

    return JNI_VERSION_1_4;
}

extern "C" void JniUnLoad(JavaVM* vm, void* reserved)
{
     JNIEnv *env;
     if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK)
     {
         return;
     }

	 pthread_cond_destroy(&tellmedia_cond);
}

