#ifndef SECURITY_STUB_H
#define SECURITY_STUB_H
#ifdef __cplusplus
extern "C" {
#endif

bool verifyApkIntegrity(const char* apkPath, const char* targetEntry, const char* expectedHashHex);

#ifdef __cplusplus
}
#endif
#endif // SECURITY_STUB_H
