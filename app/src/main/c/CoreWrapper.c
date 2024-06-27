#include <stdbool.h>
#include <stdio.h>
#include "TcpClient/TcpClient.h"
#include <unistd.h>
#include <limits.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include "logging.h"
#include <string.h>
#include <jni.h>

//JavaVM* jvm;
jobject main_activity;
JNIEnv *callback_jnienv;



void didDownloadFile(const char *filename){

//    jint result = (*jvm)->GetEnv(jvm, &callback_jnienv, JNI_VERSION_1_6);
    jclass myClass = (*callback_jnienv)->GetObjectClass(callback_jnienv, main_activity);
    jmethodID myMethod = (*callback_jnienv)->GetMethodID(callback_jnienv, myClass, "onFileUploaded", "(Ljava/lang/String;)V");
    jstring jFilename = (*callback_jnienv)->NewStringUTF(callback_jnienv, filename);
    (*callback_jnienv)->CallVoidMethod(callback_jnienv, main_activity, myMethod, jFilename);
}

jint
Java_cc_cloudon_MainActivity_connectToServer( JNIEnv* env, jobject thiz, jstring ip, jint port){
    LOGE("connect_to_server...");

    uint32_t code;
    const char * ip_str = (*env)->GetStringUTFChars(env, ip, NULL);
    uint32_t connection = connect_to_server(ip_str, port, &code);

    if(connection != 0){
        printf("Can't connect to the server. Exiting...\n");
        return 0;
    }
    printf("Instance code: %d\n", code);
    return (int)code;
}


jint
Java_cc_cloudon_MainActivity_runCloud( JNIEnv *env, jobject thiz) {
//        (*env)->GetJavaVM(env, &jvm);
//    main_activity = (*env)->NewGlobalRef(env, thiz);
//    callback_jnienv = env;
    return runEndlessServer();
}

//JNIEXPORT jint
//Java_cc_cloudon_MainActivity_closeConnection( JNIEnv *env, jobject thiz) {
//    close_connection();
//    return 0;
//}


jint
Java_cc_cloudon_MainActivity_setupEnvironment( JNIEnv* env, jobject thiz, jstring path){
    LOGE("setup_environment...");
//    main_activity = (*env)->NewGlobalRef(env, arg_main_activity);
//    (*env)->GetJavaVM(env, &jvm);
//    didDownloadFile("sd");

    const char * path_str = (*env)->GetStringUTFChars(env, path, NULL);
    char buff[PATH_MAX];
    snprintf(buff, PATH_MAX, "%s/hosted_files", path_str);
    struct stat st = {0};

    if (stat(buff, &st) == -1) {
        if(mkdir(buff, 0777) < 0){
            LOGE("mkdir error = %s", strerror(errno));
            return -1;
        }
    }
    main_activity = (*env)->NewGlobalRef(env, thiz);
    callback_jnienv = env;
    setup_environment(buff);
    callback_did_download_file_funptr = didDownloadFile;
//    register_natives(thiz, env);
    return 0;
}


jstring
Java_cc_cloudon_FileListFragment_listDirLocally( JNIEnv* env, jobject thiz) {
    char *json = NULL;
    list_dir_locally(NULL, &json);
    jstring str = (*env)->NewStringUTF(env, json);
    return str;
}

static JNINativeMethod methodsMainActivity[] = {
        {"setupEnvironment", "(Ljava/lang/String;)I", (void *)&Java_cc_cloudon_MainActivity_setupEnvironment},
        {"connectToServer",    "(Ljava/lang/String;I)I", (void *)&Java_cc_cloudon_MainActivity_connectToServer},
        {"runCloud", "()I",                       (void *)&Java_cc_cloudon_MainActivity_runCloud},
};

static JNINativeMethod methodsFileListFragment[] = {
        {"listDirLocally",      "()Ljava/lang/String;",  (void *)&Java_cc_cloudon_FileListFragment_listDirLocally},
};



void register_natives(JNIEnv* env){
//    jclass myClass = (*callback_jnienv)->GetObjectClass(callback_jnienv, main_activity);
    jclass myClass = (*env)->FindClass(env, "cc/cloudon/CoreWrapper");
    (*env)->RegisterNatives(env, myClass,
                            methodsMainActivity, sizeof(methodsMainActivity)/sizeof(methodsMainActivity[0]));

    jclass myClassFileListFragment = (*env)->FindClass(env, "cc/cloudon/FileListFragment");
    (*env)->RegisterNatives(env, myClassFileListFragment,
                            methodsFileListFragment, sizeof(methodsFileListFragment)/sizeof(methodsFileListFragment[0]));
}

typedef union {
    JNIEnv* env;
    void* venv;
} UnionJNIEnvToVoid;

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved){
    LOGE("Hello from JNI_OnLoad");
    UnionJNIEnvToVoid uenv;
    uenv.venv = NULL;
    jint result = -1;
    JNIEnv* env = NULL;

    if ((*vm)->GetEnv(vm, &uenv.venv, JNI_VERSION_1_6) != JNI_OK) {
        LOGE("ERROR: GetEnv failed");
        return JNI_VERSION_1_6;
    }
    env = uenv.env;
    register_natives(env);
    return JNI_VERSION_1_6;
}




//JNIEXPORT jint JNICALL
//Java_cc_cloudon_CoreWrapper_runCloud(JNIEnv *env, jobject thiz, jobject activity) {
//    // TODO: implement runCloud()
//}