package com.azura.protect

import android.content.Context

object NativeIntegrity {
    init {
        System.loadLibrary("azura_face_lib")
    }

    external fun checkAppIntegrity(context: Context): Boolean
}
