package com.thex.leanbacktv.ui.content.mediaview

import android.app.Activity
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.MediaController
import com.thex.leanbacktv.MainApplication
import com.thex.leanbacktv.databinding.ActivityPlayerBinding
import com.thex.leanbacktv.model.UsbCopyData
import com.thex.leanbacktv.ui.browse.MainActivity
import me.jahnen.libaums.core.fs.UsbFile
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.math.min

class PlayerActivity : Activity() {
    var fileToUse: File? = null
    lateinit var binding: ActivityPlayerBinding
    private lateinit var filepath: String
    private lateinit var filename: String
    private val URL: String = "https://www.rmp-streaming.com/media/big-buck-bunny-360p.mp4"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchIntentData()

    }

    private fun fetchIntentData() {
        if (intent.hasExtra("filePath")) {
            filename = intent.getStringExtra("fileName") as String
            filepath = intent.getStringExtra("filePath") as String
        }
        var root: UsbFile = MainActivity.fs.rootDirectory
        val file: UsbFile? = root.search(filepath)
        val param = UsbCopyData()
        if (file != null) {
            param.from = file
            MainApplication.usbCachePath.mkdirs()
            fileToUse = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                File(this.getExternalFilesDir(Environment.DIRECTORY_MOVIES)?.absolutePath)
            } else {
                MainApplication.usbCachePath
            }
            val cacheFile = File(fileToUse, filename)
            if (cacheFile.exists()) {
                startPlayer()
            } else {
                //  MainApplication.otgViewerCachePath.createNewFile()
                param.to = cacheFile
                CopyUsbFile().execute(param)
            }
        }

    }

    private fun startPlayer() {
        //setting controller and anchoring view to-from controller
        var controller = MediaController(this)
        controller.setAnchorView(binding.videoviewPlayer)
        binding.videoviewPlayer.setMediaController(controller)


        //setting play content uri to player
        // var uri = Uri.parse(URL)

        var uri = Uri.fromFile(File("${fileToUse}/$filename"))

        binding.videoviewPlayer.setVideoURI(uri)
        binding.videoviewPlayer.requestFocus()
        binding.videoviewPlayer.setZOrderOnTop(true)
        binding.videoviewPlayer.start()


    }

    inner class CopyUsbFile : AsyncTask<UsbCopyData?, Int?, Void?>() {
        private var paramUsb: UsbCopyData? = null


        override fun onCancelled(result: Void?) {
            // Remove uncompleted data file
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

            startPlayer()
            // parent.launchIntent(param!!.to)
        }


    }
}