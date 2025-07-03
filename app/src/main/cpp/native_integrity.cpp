#include <jni.h>
#include <string>
#include <fstream>
#include <sstream>
#include <android/log.h>
#include <sys/stat.h>
#include "external/sha256/sha256.h"
#include <vector>
#include <dlfcn.h>
#ifdef USE_LIBZIP
#include <zip.h>
#endif

#define LOG_TAG "NativeIntegrity"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define XOR_KEY 0x5A

// 🔐 XOR-obfuscated expected hash (replace with your real hash, obfuscated)
const char ENCRYPTED_EXPECTED_HASH[] = {
    'd'^0x5A, 'a'^0x5A, '4'^0x5A, '0'^0x5A, '4'^0x5A, '2'^0x5A, '5'^0x5A, '2'^0x5A,
    '1'^0x5A, 'c'^0x5A, 'a'^0x5A, '4'^0x5A, 'a'^0x5A, '3'^0x5A, 'e'^0x5A, '9'^0x5A,
    'b'^0x5A, '1'^0x5A, '7'^0x5A, '7'^0x5A, 'e'^0x5A, '7'^0x5A, 'b'^0x5A, '7'^0x5A,
    '0'^0x5A, '8'^0x5A, '5'^0x5A, '8'^0x5A, 'c'^0x5A, '6'^0x5A, '3'^0x5A, '7'^0x5A,
    'f'^0x5A, '2'^0x5A, 'b'^0x5A, '1'^0x5A, 'f'^0x5A, '7'^0x5A, '2'^0x5A, '5'^0x5A,
    '2'^0x5A, '5'^0x5A, '9'^0x5A, 'c'^0x5A, '6'^0x5A, '9'^0x5A, 'd'^0x5A, '4'^0x5A,
    'd'^0x5A, '3'^0x5A, '7'^0x5A, '8'^0x5A, 'e'^0x5A, '3'^0x5A, 'f'^0x5A, '3'^0x5A,
    '5'^0x5A, 'd'^0x5A, 'd'^0x5A, 'a'^0x5A, '1'^0x5A, 'c'^0x5A, '5'^0x5A, 'b'^0x5A,
    '\0'
};

std::string decryptXor(const char* encrypted) {
    std::string result;
    for (int i = 0; encrypted[i] != '\0'; ++i) {
        result += (encrypted[i] ^ XOR_KEY);
    }
    return result;
}

std::string computeFileSha256(const std::string& filePath) {
    std::ifstream file(filePath, std::ios::binary);
    if (!file) {
        LOGI("[NativeIntegrity] File not found or not readable: %s", filePath.c_str());
        return "";
    }
    std::vector<unsigned char> buffer(8192);
    SHA256_CTX sha256;
    sha256_init(&sha256);
    while (file.good()) {
        file.read(reinterpret_cast<char*>(&buffer[0]), buffer.size());
        sha256_update(&sha256, buffer.data(), file.gcount());
    }
    unsigned char hash[32];
    sha256_final(&sha256, hash);
    std::ostringstream result;
    for (int i = 0; i < 32; ++i) {
        result << std::hex << std::nouppercase << ((hash[i] >> 4) & 0xF);
        result << std::hex << std::nouppercase << (hash[i] & 0xF);
    }
    return result.str();
}

#ifdef USE_LIBZIP
std::string computeFileSha256FromZip(const std::string& apkPath, const std::string& entryPath) {
    int err = 0;
    zip_t* za = zip_open(apkPath.c_str(), 0, &err);
    if (!za) return "";
    zip_file_t* zf = zip_fopen(za, entryPath.c_str(), 0);
    if (!zf) { zip_close(za); return ""; }
    SHA256_CTX sha256;
    sha256_init(&sha256);
    char buf[8192];
    zip_int64_t n;
    while ((n = zip_fread(zf, buf, sizeof(buf))) > 0) {
        sha256_update(&sha256, (const uint8_t*)buf, n);
    }
    zip_fclose(zf);
    zip_close(za);
    unsigned char hash[32];
    sha256_final(&sha256, hash);
    std::ostringstream result;
    for (int i = 0; i < 32; ++i) {
        result << std::hex << std::nouppercase << ((hash[i] >> 4) & 0xF);
        result << std::hex << std::nouppercase << (hash[i] & 0xF);
    }
    return result.str();
}
#endif

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_azura_protect_NativeIntegrity_checkAppIntegrity(JNIEnv *env, jobject thiz, jobject context) {
    LOGI("[NativeIntegrity] checkAppIntegrity called");
    Dl_info info;
    if (dladdr((void*)&Java_com_azura_protect_NativeIntegrity_checkAppIntegrity, &info) && info.dli_fname) {
        std::string libPath = info.dli_fname;
        LOGI("[NativeIntegrity] Loaded library path (dladdr): %s", libPath.c_str());
        std::string actualHash;
#if !defined(USE_LIBZIP)
        if (libPath.find(".apk!") != std::string::npos) {
            LOGI("[NativeIntegrity] APK zip path detected, but zip logic is disabled. Failing integrity check.");
            return JNI_FALSE;
        }
#endif
#ifdef USE_LIBZIP
        if (libPath.find(".apk!") != std::string::npos) {
            size_t excl = libPath.find(".apk!");
            std::string apkPath = libPath.substr(0, excl + 4); // include .apk
            std::string entryPath = libPath.substr(excl + 5); // skip !/
            // Remove leading slash if present
            if (!entryPath.empty() && entryPath[0] == '/') entryPath = entryPath.substr(1);
            LOGI("[NativeIntegrity] APK path: %s, entry: %s", apkPath.c_str(), entryPath.c_str());
            actualHash = computeFileSha256FromZip(apkPath, entryPath);
        } else
#endif
        {
            struct stat st;
            if (stat(libPath.c_str(), &st) != 0) {
                LOGI("[NativeIntegrity] Library file does not exist: %s", libPath.c_str());
                return JNI_FALSE;
            }
            actualHash = computeFileSha256(libPath);
        }
        std::string expectedHash = decryptXor(ENCRYPTED_EXPECTED_HASH);
        LOGI("[NativeIntegrity] Expected Hash: %s", expectedHash.c_str());
        LOGI("[NativeIntegrity] Actual   Hash: %s", actualHash.c_str());
        if (actualHash.empty()) {
            LOGI("[NativeIntegrity] Actual hash is empty, file may not be readable or hash failed");
            return JNI_FALSE;
        }
        if (actualHash == expectedHash) {
            LOGI("[NativeIntegrity] Hash match: integrity OK");
            return JNI_TRUE;
        } else {
            LOGI("[NativeIntegrity] Hash mismatch: integrity FAIL");
            return JNI_FALSE;
        }
    } else {
        LOGI("[NativeIntegrity] dladdr failed to get library path");
        return JNI_FALSE;
    }
}
