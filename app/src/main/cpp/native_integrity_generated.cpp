#include <jni.h>
#include <string>
#include <android/log.h>
#include <fstream>
#include <sstream>
#include <vector>
#include "external/sha256/sha256.h"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "AZURA_INTEGRITY", __VA_ARGS__)

const uint8_t ENCRYPTED_EXPECTED_HASH[32] = { 0xfd, 0x13, 0xdb, 0xb5, 0x73, 0x43, 0x8e, 0x33, 0x66, 0xa2, 0x19, 0xbb, 0x72, 0x11, 0xad, 0x31, 0xeb, 0x05, 0xe3, 0x88, 0xc3, 0xa3, 0x39, 0x54, 0xd2, 0x7b, 0x53, 0xfd, 0xd0, 0xac, 0x0c, 0x76 };

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
