package com.thex.leanbacktv.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.thex.leanbacktv.model.MediaDataModel
import me.jahnen.libaums.core.fs.UsbFile

class ImageViewModel : ViewModel() {

    var mediaLiveData: MutableLiveData<ArrayList<MediaDataModel>> = MutableLiveData()
    private var mediaList: ArrayList<MediaDataModel> = ArrayList<MediaDataModel>()

    fun readAllImages(root: UsbFile) {
        mediaList.clear()

        val files: Array<UsbFile> = root.listFiles()
        //fetch image type from all files in root
        for (file in files) {
            if (file.name.endsWith(".jpg") || file.name.endsWith(".jpeg") || file.name.endsWith(
                    ".png"
                )
            ) {

                var model = MediaDataModel(
                    0,
                    file.name,
                    "Image",
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
                        childFile.name.endsWith(".jpg") || childFile.name.endsWith(".jpeg") || childFile.name.endsWith(
                            ".png"
                        )
                    if (hasImages) {
                        var model = MediaDataModel(
                            1,
                            file.name,
                            "Image",
                            "${file.absolutePath}",
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