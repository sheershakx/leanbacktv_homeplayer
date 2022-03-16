package com.thex.leanbacktv.ui.content.mediaview

import android.app.Activity
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.MediaController
import com.thex.leanbacktv.R
import com.thex.leanbacktv.databinding.ActivityPlayerBinding
import java.io.File

class PlayerActivity : Activity() {
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
        Log.d("PlayerActivity", "fetchIntentData: filepath=$filepath ")
        startPlayer()
    }

    private fun startPlayer() {
        //setting controller and anchoring view to-from controller
        var controller = MediaController(this)
        controller.setAnchorView(binding.videoviewPlayer)
        binding.videoviewPlayer.setMediaController(controller)


        //setting play content uri to player
        var uri = Uri.parse(URL)
        // var uri = Uri.parse(URL)
        //   var uri = Uri.fromFile(File("$filepath"))
        binding.videoviewPlayer.setVideoURI(uri)
        binding.videoviewPlayer.requestFocus()
        binding.videoviewPlayer.setZOrderOnTop(true)
        binding.videoviewPlayer.start()


    }
}