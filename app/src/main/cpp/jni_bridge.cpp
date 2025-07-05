#include <jni.h>
#include <android/log.h>
#include <string>
#include "security_stub.h"

#define LOG_TAG "FaceBridge"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Native model state (dummy for now)
namespace {
    bool g_modelInitialized = false;
    std::string g_modelPath;
}

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_azura_azuratime_nativebridge_FaceBridge_initializeNative(
    JNIEnv *env, jclass clazz, jstring modelPath) {
    LOGD("initializeNative() called");
    const char *pathStr = env->GetStringUTFChars(modelPath, nullptr);
    if (pathStr == nullptr) {
        LOGE("Failed to get model path string");
        return JNI_FALSE;
    }
    g_modelPath = pathStr;
    env->ReleaseStringUTFChars(modelPath, pathStr);
    // TODO: Load your model here using g_modelPath
    g_modelInitialized = true;
    LOGD("Model initialized at path: %s", g_modelPath.c_str());
    return JNI_TRUE;
}

JNIEXPORT jfloatArray JNICALL
Java_com_azura_azuratime_nativebridge_FaceBridge_recognizeFace___3F(
    JNIEnv *env, jclass clazz, jfloatArray input) {
    if (!g_modelInitialized) {
        LOGE("recognizeFace() called before model initialized!");
        return nullptr;
    }
    jsize inputLength = env->GetArrayLength(input);
    jfloat* inputData = env->GetFloatArrayElements(input, nullptr);
    LOGD("recognizeFace() called with input length: %d", inputLength);
    // Dummy embedding
    const int embeddingSize = 128;
    jfloat dummyEmbedding[embeddingSize];
    for (int i = 0; i < embeddingSize; ++i) {
        dummyEmbedding[i] = (i % 10) * 0.1f;
    }
    jfloatArray output = env->NewFloatArray(embeddingSize);
    env->SetFloatArrayRegion(output, 0, embeddingSize, dummyEmbedding);
    env->ReleaseFloatArrayElements(input, inputData, JNI_ABORT);
    return output;
}

JNIEXPORT void JNICALL
Java_com_azura_azuratime_nativebridge_FaceBridge_closeNative(
    JNIEnv *env, jclass clazz) {
    LOGD("closeNative() called");
    // TODO: Free model resources here
    g_modelInitialized = false;
    g_modelPath.clear();
}

JNIEXPORT jboolean JNICALL
Java_com_azura_azuratime_util_SecurityBridge_verifyApkHash(
    JNIEnv *env, jclass clazz, jstring apk_path, jstring target_entry, jstring expected_hash) {
    const char* apkPath = env->GetStringUTFChars(apk_path, nullptr);
    const char* entryName = env->GetStringUTFChars(target_entry, nullptr);
    const char* expected = env->GetStringUTFChars(expected_hash, nullptr);

    bool result = verifyApkIntegrity(apkPath, entryName, expected);

    env->ReleaseStringUTFChars(apk_path, apkPath);
    env->ReleaseStringUTFChars(target_entry, entryName);
    env->ReleaseStringUTFChars(expected_hash, expected);

    return result ? JNI_TRUE : JNI_FALSE;
}

} // extern "C"
