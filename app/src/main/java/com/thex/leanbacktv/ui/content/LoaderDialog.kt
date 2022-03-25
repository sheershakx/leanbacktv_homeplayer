package com.thex.leanbacktv.ui.content

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import com.thex.leanbacktv.R

class LoaderDialog(context: Context) : Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_loader)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}