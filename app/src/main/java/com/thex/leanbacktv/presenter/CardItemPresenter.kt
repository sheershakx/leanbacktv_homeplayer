package com.thex.leanbacktv.presenter

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.thex.leanbacktv.R
import com.thex.leanbacktv.R.color.light_blue

class CardItemPresenter : Presenter() {

    class ViewHolder(view: View?) : Presenter.ViewHolder(view) {
        var cardView = view as ImageCardView
    }

    override fun onCreateViewHolder(parent: ViewGroup?): Presenter.ViewHolder {
        val context = parent?.context
        val cardView = ImageCardView(context)
        cardView.isFocusable = true
        cardView.isFocusableInTouchMode = true
        cardView.setBackgroundColor(ContextCompat.getColor(context!!, light_blue));
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: Presenter.ViewHolder?, item: Any?) {
    }

    override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder?) {

    }
}