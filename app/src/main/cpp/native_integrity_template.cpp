#include <jni.h>
#include <string>
#include <android/log.h>
#include <fstream>
#include <sstream>
#include <vector>
#include "external/sha256/sha256.h"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "AZURA_INTEGRITY", __VA_ARGS__)

//__HASH_PLACEHOLDER__

bool verifyHash(const std::string& soPath);

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_azura_protect_NativeIntegrity_checkAppIntegrity(JNIEnv *env, jobject thiz, jobject context) {
    jclass contextCls = env->GetObjectClass(context);
    jmethodID getAppInfo = env->GetMethodID(contextCls, "getApplicationInfo", "()Landroid/content/pm/ApplicationInfo;");
    jobject appInfo = env->CallObjectMethod(context, getAppInfo);
    jclass appInfoCls = env->GetObjectClass(appInfo);
    jfieldID libDirField = env->GetFieldID(appInfoCls, "nativeLibraryDir", "Ljava/lang/String;");
    jstring libDirJStr = (jstring) env->GetObjectField(appInfo, libDirField);

    const char *libDir = env->GetStringUTFChars(libDirJStr, nullptr);
    std::string fullPath = std::string(libDir) + "/libazura_face_lib.so";

    env->ReleaseStringUTFChars(libDirJStr, libDir);
    return verifyHash(fullPath) ? JNI_TRUE : JNI_FALSE;
}

bool verifyHash(const std::string& soPath) {
    std::ifstream file(soPath, std::ios::binary);
    if (!file) return false;

    std::stringstream buffer;
    buffer << file.rdbuf();
    std::string content = buffer.str();

    unsigned char hash[32];
    sha256((const uint8_t*)content.data(), content.size(), hash);

    for (int i = 0; i < 32; i++) {
        if ((hash[i] ^ 0xAA) != ENCRYPTED_EXPECTED_HASH[i]) {
            LOGI("Integrity check failed at byte %d", i);
            return false;
        }
    }

    LOGI("Integrity check passed");
    return true;
}
