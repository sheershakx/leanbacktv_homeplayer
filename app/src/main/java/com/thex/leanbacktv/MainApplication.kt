package com.thex.leanbacktv

import android.app.Application
import com.thex.leanbacktv.utils.toast
import android.os.Environment
import java.io.File


class MainApplication : Application() {

    companion object {
        var hasRecognizedDevice: Boolean = false


        val usbCachePath: File =
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "MyTvApp"
            )
//        val otgViewerCachePath: File = File(
//            Environment.getExternalStorageDirectory()
//                .absolutePath, "/Download/MyTvApp"
        //  )


    }

}