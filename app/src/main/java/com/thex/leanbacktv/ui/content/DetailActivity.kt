package com.thex.leanbacktv.ui.content

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.thex.leanbacktv.databinding.ActivityDetailBinding

class DetailActivity : FragmentActivity() {
    lateinit var binding: ActivityDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        receiveIntentData()


    }

    private fun receiveIntentData() {
        val fileName: String = intent.getStringExtra("fileName")
        val filePath: String = intent.getStringExtra("filePath")
        val fileType: String = intent.getStringExtra("fileType")

        when (fileType) {
            "Image" -> {
                supportFragmentManager.beginTransaction()
                    .replace(
                        binding.fragmentDetailContainer.id,
                        ImageFragment.newInstance(fileName, filePath)
                    ).commit()
            }
            "Audio" -> {
                supportFragmentManager.beginTransaction()
                    .replace(
                        binding.fragmentDetailContainer.id,
                        AudioFragment.newInstance(fileName, filePath)
                    ).commit()
            }
            "Video" -> {
                supportFragmentManager.beginTransaction()
                    .replace(
                        binding.fragmentDetailContainer.id,
                        VideoFragment.newInstance(fileName, filePath)
                    ).commit()
            }
        }


    }
}