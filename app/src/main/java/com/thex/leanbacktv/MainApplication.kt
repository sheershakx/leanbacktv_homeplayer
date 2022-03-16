package com.thex.leanbacktv

import android.app.Application

class MainApplication : Application() {
    companion object {
        var hasRecognizedDevice: Boolean = false
    }
}