package com.azura.azuratime.util

object SecurityBridge {
    init {
        System.loadLibrary("azura_face_lib")
    }

    external fun verifyApkHash(apkPath: String, targetEntry: String, expectedHash: String): Boolean
}

// Example usage of SecurityBridge
fun checkApkIntegrity(context: android.content.Context): Boolean {
    val apkPath = context.packageCodePath
    val isValid = SecurityBridge.verifyApkHash(
        apkPath,
        "classes.dex", // or "lib/arm64-v8a/libazura_face_lib.so"
        "e9a8b4d2ab1234..." // replace with your actual hash
    )
    return isValid
}
