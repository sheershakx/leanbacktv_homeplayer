package com.thex.leanbacktv

import android.app.Application
import com.thex.leanbacktv.utils.toast
import android.os.Environment
import com.thex.leanbacktv.model.MediaDataModel
import java.io.File


class MainApplication : Application() {

    companion object {
        var mediaData: ArrayList<MediaDataModel> = ArrayList()
        var hasRecognizedDevice: Boolean = false
        var usbCachePath: File =
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "HomePlayer"
            )


    }

}