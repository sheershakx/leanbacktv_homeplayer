package com.thex.leanbacktv.presenter

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.leanback.widget.BaseCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import com.thex.leanbacktv.R
import com.thex.leanbacktv.model.MediaDataModel
import com.thex.leanbacktv.ui.content.DetailActivity
import com.thex.leanbacktv.ui.content.mediaview.ImageViewActivity
import com.thex.leanbacktv.ui.content.mediaview.PlayerActivity
import com.thex.leanbacktv.utils.toast
import java.io.File


class AudioPresenter(val context: Activity) : Presenter() {
    class MYViewHolder(ItemView: View) : Presenter.ViewHolder(ItemView) {
        var cardImage: ImageView = ItemView.findViewById(R.id.img_cardimage)
        var cardTitle: TextView = ItemView.findViewById(R.id.tv_cardtitle)
        var cardLayout: ConstraintLayout = ItemView.findViewById(R.id.layout_card)

    }

    override fun onCreateViewHolder(parent: ViewGroup?): ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.layout_card, parent, false)
        return MYViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder?, item: Any?) {
        val holder = viewHolder as MYViewHolder
        if (item is MediaDataModel) {
            holder.cardTitle.text = item.fileName
            if (item.isDirectory) {
                holder.cardImage.setImageResource(R.drawable.img_directory)
            } else {
                Glide.with(context)
                    .load(Uri.fromFile(File(item.filePath)))
                    .into(viewHolder.cardImage)
            }
            holder.cardLayout.setOnClickListener {
                if (item.isDirectory) {
                    val intent = Intent(context, DetailActivity::class.java)
                    intent.putExtra("filePath", item.filePath)
                    intent.putExtra("fileName", item.fileName)
                    intent.putExtra("fileType", item.fileType)
                    context.startActivity(intent)


                } else {
                    val intent = Intent(context, PlayerActivity::class.java)
                    intent.putExtra("filePath", item.filePath)
                    intent.putExtra("fileName", item.fileName)
                    context.startActivity(intent)
                }
            }

        }


    }

    override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder?) {
    }


}