package com.thex.leanbacktv.ui.content.mediaview

import android.app.Activity
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.thex.leanbacktv.R
import com.thex.leanbacktv.databinding.ActivityImageViewBinding
import java.io.File

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
        val imageFile = File("$filepath")
        if (imageFile == null) {
            Log.d("LOG_IMAGE", "LOG_IMAGE: file is null ")
        }
        Log.d("LOG_IMAGE", "image path is::: /storage/sda1$filepath")
//        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
//        binding.tvImagename.text = filename
//        binding.ivImage.setImageBitmap(bitmap)
        ///storage/sda1/testimage4.jpg

        Glide.with(this)
            .asBitmap()
            .load(Uri.fromFile(File("/storage/sda1$filepath")))
            .placeholder(ContextCompat.getDrawable(this, R.drawable.placeholder_image))
            .into(binding.ivImage)
    }
}