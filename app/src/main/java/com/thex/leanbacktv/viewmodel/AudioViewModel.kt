package com.thex.leanbacktv.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.thex.leanbacktv.model.MediaDataModel
import me.jahnen.libaums.core.fs.UsbFile

class AudioViewModel : ViewModel() {

    var mediaLiveData: MutableLiveData<ArrayList<MediaDataModel>> = MutableLiveData()
    private var mediaList: ArrayList<MediaDataModel> = ArrayList<MediaDataModel>()


    fun readAllAudios(root: UsbFile) {
        mediaList.clear()
        val files: Array<UsbFile> = root.listFiles()
        //fetch image type from all files in root
        for (file in files) {
            if (file.name.endsWith(".mp3") || file.name.endsWith(".wav")
            ) {

                var model = MediaDataModel(
                    0,
                    file.name,
                    "Audio",
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
                        childFile.name.endsWith(".mp3") || childFile.name.endsWith(".wav")
                    if (hasImages) {
                        var model = MediaDataModel(
                            1,
                            file.name,
                            "Audio",
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