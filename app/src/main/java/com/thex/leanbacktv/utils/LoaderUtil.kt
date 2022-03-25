package com.thex.leanbacktv.utils

import android.content.Context
import com.thex.leanbacktv.ui.content.LoaderDialog

class LoaderUtil {
    companion object {
        var loader: LoaderDialog? = null

        fun showLoader(context: Context, show: Boolean) {

            if (!show && loader != null) {
                loader!!.dismiss()
                return
            }

            if (loader != null && loader!!.isShowing) {
                loader!!.dismiss()
                return
            }
            if (context != null && show) {
                loader = LoaderDialog(context)
                loader!!.setCanceledOnTouchOutside(true)
                loader!!.setCancelable(true)
                loader!!.show()
            }

        }


    }


}