package com.thex.leanbacktv.ui.content.mediaview

import android.app.Activity
import android.os.AsyncTask
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.thex.leanbacktv.MainApplication
import com.thex.leanbacktv.R
import com.thex.leanbacktv.databinding.ActivityImageViewBinding
import com.thex.leanbacktv.model.UsbCopyData
import com.thex.leanbacktv.ui.browse.MainActivity
import me.jahnen.libaums.core.fs.UsbFile
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.math.min

class ImageViewActivity : Activity() {
    lateinit var binding: ActivityImageViewBinding
    private lateinit var filepath: String
    private lateinit var filename: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fetchIntentData()

    }

    private fun fetchIntentData() {
        if (intent.hasExtra("filepath")) {
            filename = intent.getStringExtra("filename") as String
            filepath = intent.getStringExtra("filepath") as String
        }
        var root: UsbFile = MainActivity.fs.rootDirectory
        val file: UsbFile? = root.search(filepath)
        val usbParameter = UsbCopyData()
        if (file != null) {
            usbParameter.from = file

            MainApplication.usbCachePath.mkdirs()


            val cacheFile = File(MainApplication.usbCachePath, filename)
            if (cacheFile.exists()) {
                showImage()
            } else {
                usbParameter.to = cacheFile
                CopyUsbFile().execute(usbParameter)
            }
        }


    }

    private fun showImage() {
        binding.tvImagename.text = filename
        Glide.with(applicationContext)
            .asBitmap()
            .load("${MainApplication.usbCachePath}/$filename")
            .placeholder(
                ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.placeholder_image
                )
            )
            .into(binding.ivImage)
    }


    inner class CopyUsbFile : AsyncTask<UsbCopyData?, Int?, Void?>() {
        private var paramUsb: UsbCopyData? = null


        override fun onCancelled(result: Void?) {
            // invokes if somehow the copying process get cancelled
            if (paramUsb != null) paramUsb!!.to!!.delete()
        }


        override fun doInBackground(vararg paramUsbs: UsbCopyData?): Void? {
            val buffer: ByteBuffer = ByteBuffer.allocate(4096)
            paramUsb = paramUsbs[0]
            val length = paramUsbs[0]?.from!!.length
            try {
                val out = FileOutputStream(paramUsb?.to)
                var i: Long = 0
                while (i < length) {
                    if (!isCancelled) {
                        buffer.limit(min(buffer.capacity(), (length - i).toInt()))
                        paramUsbs[0]?.from!!.read(i, buffer)
                        out.write(buffer.array(), 0, buffer.limit())
                        publishProgress(i.toInt())
                        buffer.clear()
                    }
                    i += buffer.limit()
                }
                out.close()
            } catch (e: IOException) {
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            showImage()
        }


    }
}