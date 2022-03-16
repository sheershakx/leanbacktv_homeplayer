package com.thex.leanbacktv.ui.content

import android.os.Bundle
import android.view.View
import androidx.leanback.app.HeadersSupportFragment
import androidx.leanback.widget.ListRow

import androidx.leanback.widget.ArrayObjectAdapter

import androidx.leanback.widget.HeaderItem


class HeaderFragment : HeadersSupportFragment() {
    companion object {
        private val TAG = HeaderFragment::class.java.name

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupHeaders()
    }

    private fun setupHeaders() {
        var objectAdapter = ArrayObjectAdapter()
        for (i in 1..5) {
            val header = HeaderItem(i.toLong(), "Menu $i ")
            val myAdapter = ArrayObjectAdapter()
            myAdapter.add("test")
            objectAdapter.add(0, ListRow(header, myAdapter))
        }

        adapter = objectAdapter

    }
}