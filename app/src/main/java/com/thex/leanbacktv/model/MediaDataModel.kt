package com.thex.leanbacktv.model

import android.graphics.Bitmap

data class MediaDataModel(
    var id: Int?,
    var fileName: String,
    var fileType: String?,
    var filePath: String,
    var isDirectory: Boolean?,
    var fileDrawable: Bitmap?

)