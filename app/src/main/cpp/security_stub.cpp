#include <zip.h>
#include "external/sha256/sha256.h"
#include <android/log.h>
#include <fstream>
#include <vector>
#include <cstring>
#include "security_stub.h"

#define LOG_TAG "NativeSecurity"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

extern "C" bool verifyApkIntegrity(const char* apkPath, const char* targetEntry, const char* expectedHashHex) {
    int err = 0;
    zip_t* apk = zip_open(apkPath, 0, &err);
    if (!apk) {
        LOGD("❌ Failed to open APK with libzip");
        return false;
    }

    zip_stat_t sb;
    if (zip_stat(apk, targetEntry, 0, &sb) != 0) {
        LOGD("❌ Target entry not found: %s", targetEntry);
        zip_close(apk);
        return false;
    }

    zip_file_t* file = zip_fopen(apk, targetEntry, 0);
    if (!file) {
        LOGD("❌ Failed to open entry: %s", targetEntry);
        zip_close(apk);
        return false;
    }

    std::vector<unsigned char> buffer(sb.size);
    zip_fread(file, buffer.data(), sb.size);
    zip_fclose(file);
    zip_close(apk);

    // Hash using portable SHA256
    unsigned char hash[32];
    SHA256_CTX ctx;
    sha256_init(&ctx);
    sha256_update(&ctx, buffer.data(), buffer.size());
    sha256_final(&ctx, hash);

    char hashHex[65] = {0};
    for (int i = 0; i < 32; ++i)
        sprintf(hashHex + i * 2, "%02x", hash[i]);

    LOGD("✅ Calculated hash: %s", hashHex);
    LOGD("🔒 Expected hash:   %s", expectedHashHex);

    return strcmp(hashHex, expectedHashHex) == 0;
}
