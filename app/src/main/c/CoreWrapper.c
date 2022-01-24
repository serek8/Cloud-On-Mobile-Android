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


jint
Java_cc_cloudon_MainActivity_setupEnvironment( JNIEnv* env, jobject thiz, jstring path){
    LOGE("setup_environment...");
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

    setup_environment(buff);
    return 0;
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
Java_cc_cloudon_MainActivity_runCloud( JNIEnv* env, jobject thiz) {
    return runEndlessServer();
}

jstring
Java_cc_cloudon_FileListFragment_listDirLocally( JNIEnv* env, jobject thiz) {
    char *json = NULL;
    list_dir_locally(NULL, &json);
    jstring str = (*env)->NewStringUTF(env, json);
    return str;
}










