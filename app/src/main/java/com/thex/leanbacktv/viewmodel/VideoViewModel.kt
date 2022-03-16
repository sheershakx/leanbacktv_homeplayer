package com.thex.leanbacktv.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.thex.leanbacktv.model.MediaDataModel
import com.thex.leanbacktv.ui.browse.MainActivity
import me.jahnen.libaums.core.fs.UsbFile

class VideoViewModel : ViewModel() {

    var mediaLiveData: MutableLiveData<ArrayList<MediaDataModel>> = MutableLiveData()
    private var mediaList: ArrayList<MediaDataModel> = ArrayList<MediaDataModel>()


    fun readAllVideos(root: UsbFile) {
        mediaList.clear()
        val files: Array<UsbFile> = root.listFiles()
        //fetch image type from all files in root
        for (file in files) {
            if (file.name.endsWith(".mp4") || file.name.endsWith(".mkv")
            ) {

                var model = MediaDataModel(
                    0,
                    file.name,
                    "Video",
                    "${file.absolutePath}",
                    false,
                    null
                )
                mediaList.add(model)
                // read from a file


            }


            if (file.isDirectory) {
                var subRoot: UsbFile = file
                val subFiles: Array<UsbFile> = subRoot.listFiles()
                for (childFile in subFiles) {
                    var hasImages: Boolean =
                        childFile.name.endsWith(".mp4") || childFile.name.endsWith(".mkv")
                    if (hasImages) {
                        var model = MediaDataModel(
                            1,
                            file.name,
                            "Video",
                            "${MainActivity.usbPath}${file.absolutePath}",
                            true,
                            null
                        )
                        mediaList.add(model)
                        break
                    }


                }
            }
        }

        mediaLiveData.value = mediaList

    }
}