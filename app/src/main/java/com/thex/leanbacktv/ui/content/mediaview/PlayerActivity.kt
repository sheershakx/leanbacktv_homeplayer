package com.thex.leanbacktv.ui.content.mediaview

import android.annotation.SuppressLint
import android.app.Activity
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.exoplayer2.ui.TimeBar
import com.thex.leanbacktv.MainApplication
import com.thex.leanbacktv.R
import com.thex.leanbacktv.databinding.ActivityPlayerBinding
import com.thex.leanbacktv.model.MediaDataModel
import com.thex.leanbacktv.model.UsbCopyData
import com.thex.leanbacktv.ui.browse.MainActivity
import com.thex.leanbacktv.ui.dialiogs.ErrorDialog
import com.thex.leanbacktv.utils.LoaderUtil
import com.thex.leanbacktv.utils.toast
import me.jahnen.libaums.core.fs.UsbFile
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Error
import java.nio.ByteBuffer
import kotlin.math.min

class PlayerActivity : FragmentActivity() {
    lateinit var root: UsbFile
    lateinit var binding: ActivityPlayerBinding
    var videoDataList: ArrayList<MediaDataModel> = ArrayList<MediaDataModel>()
    private lateinit var filepath: String
    private lateinit var filename: String
    private lateinit var fileType: String
    lateinit var exoTitle: TextView
    lateinit var exoPlayer: ExoPlayer
    lateinit var exoThumbnail: ImageView
    lateinit var exoRewind: ImageButton
    lateinit var exoForward: ImageButton
    lateinit var exoPlay: ImageButton
    lateinit var exoPause: ImageButton
    lateinit var exoNext: ImageButton
    lateinit var exoPrev: ImageButton
    lateinit var exoTimeBar: DefaultTimeBar
    var durationtoSkip: Long = 0
    private val URL_V: String = "https://www.rmp-streaming.com/media/big-buck-bunny-360p.mp4"
    private val URL_A: String = "http://mediaserv30.live-streams.nl:8086/live"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        root = MainActivity.fs.rootDirectory
        fetchIntentData()

        if (MainApplication.mediaData != null) {
            LoaderUtil.showLoader(this, true)
            createPlaylist(MainApplication.mediaData)
            startExoPlayer()
        }
    }


    private fun fetchIntentData() {

        if (intent.hasExtra("filePath")) {
            filename = intent.getStringExtra("fileName") as String
            filepath = intent.getStringExtra("filePath") as String
            fileType = intent.getStringExtra("fileType") as String
        }
    }


    private fun copyProcess(thisFilePath: String) {
        val usbParameter = UsbCopyData()
        val file: UsbFile? = root.search(thisFilePath)
        usbParameter.from = file
        MainApplication.usbCachePath.mkdirs()
        val cacheFile = File(MainApplication.usbCachePath, file?.name)
        if (!cacheFile.exists()) {
            usbParameter.to = cacheFile
            CopyUsbFile().execute(usbParameter)
        }
    }

    private fun createPlaylist(mediaList: ArrayList<MediaDataModel>) {
        videoDataList.clear()
        for (media in mediaList) {
            if (media.isDirectory == false) {
                videoDataList.add(
                    MediaDataModel(
                        null,
                        media.fileName,
                        null,
                        media.filePath,
                        media.isDirectory,
                        null
                    )
                )
            }

        }
    }

    private fun startExoPlayer() {

        initializeControllerViews()
        /* exo player creation and setup controllers*/
        exoPlayer = ExoPlayer.Builder(this).build()

        //aspect ratio
        binding.videoviewPlayer.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        exoPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
        binding.videoviewPlayer.player = exoPlayer

        //artwork
        binding.videoviewPlayer.defaultArtwork = if (fileType == "Audio") {
            ContextCompat.getDrawable(this, R.drawable.placeholder_audio)
        } else {
            ContextCompat.getDrawable(this, R.drawable.placeholder_video)
        }
        binding.videoviewPlayer.useArtwork = true

        //media items setup
        for (item in videoDataList) {

            val mediaItem = MediaItem.Builder()
            val metaData = MediaMetadata.Builder()
            mediaItem.setUri(Uri.fromFile(File("${MainApplication.usbCachePath}/${item.fileName}")))
            metaData.setTitle(item.fileName)
            metaData.setDisplayTitle(item.fileName)
            mediaItem.setMediaMetadata(metaData.build())
            exoPlayer.addMediaItem(mediaItem.build())

        }
        //listener
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                showErrorDialog(error.errorCodeName, error.message.toString())
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == ExoPlayer.STATE_BUFFERING) {
                    LoaderUtil.showLoader(this@PlayerActivity, true)
                }
                if (playbackState == ExoPlayer.STATE_IDLE) {
                    LoaderUtil.showLoader(this@PlayerActivity, true)

                    var currMediaPos = exoPlayer.currentMediaItemIndex
                    val cacheFile =
                        File(MainApplication.usbCachePath, videoDataList[currMediaPos].fileName)
                    if (!cacheFile.exists()) {
                        copyProcess(videoDataList[currMediaPos].filePath)

                    }
                }
                if (playbackState == ExoPlayer.STATE_READY) {
                    exoPlayer.play()
                    Log.d("PlayerActivity", "media count:${exoPlayer.mediaItemCount} ")
                    LoaderUtil.showLoader(this@PlayerActivity, false)
                    exoTitle.text = exoPlayer.currentMediaItem?.mediaMetadata?.title
                    Glide.with(applicationContext).load(R.drawable.placeholder_video)
                        .placeholder(R.drawable.placeholder_image).into(exoThumbnail)
                    exoPrev.isEnabled = exoPlayer.hasPreviousMediaItem()
                    exoNext.isEnabled = exoPlayer.hasNextMediaItem()
                    durationtoSkip = if (exoPlayer.duration < 600000)  //10 min=600000 ms
                    {
                        60000               //60 sec
                    } else {
                        30000               //30 sec
                    }

                }
            }
        })

        //player start
        exoPlayer.prepare()


    }

    private fun initializeControllerViews() {
        exoTitle = binding.videoviewPlayer.findViewById<TextView>(R.id.custom_exo_title)
        exoThumbnail = binding.videoviewPlayer.findViewById<ImageView>(R.id.custom_exo_thumbail)
        exoRewind = binding.videoviewPlayer.findViewById<ImageButton>(R.id.exo_rew)
        exoForward = binding.videoviewPlayer.findViewById<ImageButton>(R.id.exo_ffwd)
        exoPrev = binding.videoviewPlayer.findViewById<ImageButton>(R.id.exo_prev)
        exoNext = binding.videoviewPlayer.findViewById<ImageButton>(R.id.exo_next)
        //  exoPause = binding.videoviewPlayer.findViewById<ImageButton>(R.id.exo_pause)
        //  exoPlay = binding.videoviewPlayer.findViewById<ImageButton>(R.id.exo_play)
        exoTimeBar =
            binding.videoviewPlayer.findViewById(R.id.exo_progress) as DefaultTimeBar

        //set how many count video reaches to end when seek changed through seekbar (not forward button)
        exoTimeBar.setKeyCountIncrement(10)

        exoForward.setOnClickListener {
            exoPlayer.seekTo(exoPlayer.currentPosition + durationtoSkip)
        }
        exoRewind.setOnClickListener {
            exoPlayer.seekTo(exoPlayer.currentPosition - durationtoSkip)
        }
        exoNext.setOnClickListener {
            val currMediaPos = exoPlayer.currentMediaItemIndex
            val cacheFile =
                File(MainApplication.usbCachePath, videoDataList[currMediaPos + 1].fileName)
            if (exoPlayer.hasNextMediaItem()) {
                if (cacheFile.exists()) {
                    exoPlayer.seekToNextMediaItem()
                } else {
                    copyProcess(videoDataList[currMediaPos + 1].filePath)
                    //todo after cache process is done execure player.seektonext()
                }
            }

        }


        exoPrev.setOnClickListener {
            var currMediaPos = exoPlayer.currentMediaItemIndex
            val cacheFile =
                File(MainApplication.usbCachePath, videoDataList[currMediaPos - 1].fileName)
            if (exoPlayer.hasPreviousMediaItem()) {
                if (cacheFile.exists()) {
                    exoPlayer.seekToPreviousMediaItem()
                } else {
                    copyProcess(videoDataList[currMediaPos + 1].filePath)
                    //todo after cache process is done execure player.seektonext()

                }
            }

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
    }


    inner class CopyUsbFile : AsyncTask<UsbCopyData?, Int?, Void?>() {
        private var paramUsb: UsbCopyData? = null
        override fun onPreExecute() {
        }

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
            exoPlayer.prepare()
        }


    }

    private fun showErrorDialog(title: String, message: String) {
        var errorDialog = ErrorDialog(title, message)
        errorDialog.show(supportFragmentManager, "ErrorDialog")

    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        var keyCode = event?.keyCode
        if (!binding.videoviewPlayer.isControllerVisible) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    exoForward.performClick()
                    toast("forward")
                }
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    exoRewind.performClick()
                    toast("rewind")

                }
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    exoNext.performClick()
                    toast("next")


                }
                KeyEvent.KEYCODE_DPAD_UP -> {
                    exoPrev.performClick()
                    toast("previous")


                }
                KeyEvent.KEYCODE_DPAD_CENTER -> {
                    binding.videoviewPlayer.showController()
                    binding.videoviewPlayer.controllerShowTimeoutMs = 30000

                }
            }
        }

        return super.dispatchKeyEvent(event)

    }
}