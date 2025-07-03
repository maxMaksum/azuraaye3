#include "face_recognizer.h"
#include <android/log.h>
#include <chrono>  // Tambahkan kalau belum ada

#define LOG_TAG "FaceRecognizer"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "AzuraNative", __VA_ARGS__)

bool init_face_model(const char* model_path) {
    auto start = std::chrono::high_resolution_clock::now(); // ⏱️ Mulai timer
    LOGI("init_face_model() called with path: %s", model_path);
    // Simulasi proses load model (ganti dengan proses asli jika ada)
    // model = tflite::FlatBufferModel::BuildFromFile(model_path);
    // interpreter = new tflite::Interpreter(...);
    // interpreter->AllocateTensors();
    // ...existing code...
    auto end = std::chrono::high_resolution_clock::now(); // ⏱️ Selesai timer
    auto duration = std::chrono::duration_cast<std::chrono::milliseconds>(end - start).count();
    LOGD("✅ Native model loaded in %lld ms", duration);
    return true;
}

bool get_face_embedding(const float* input, int input_len, float* output, int output_len) {
    LOGI("get_face_embedding() dummy run");
    for (int i = 0; i < output_len; i++) {
        output[i] = 0.01f * i; // dummy data
    }
    return true;
}

void close_face_model() {
    LOGI("close_face_model() called");
}
