package com.thex.leanbacktv.ui.browse

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.leanback.app.BackgroundManager
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import com.thex.leanbacktv.R
import com.thex.leanbacktv.model.HeaderTitleModel
import com.thex.leanbacktv.ui.content.AudioFragment
import com.thex.leanbacktv.ui.content.ImageFragment
import com.thex.leanbacktv.ui.content.VideoFragment
import kotlin.text.Typography.section


class MainFragment : BrowseSupportFragment() {
    var headerList = ArrayList<HeaderTitleModel>()


    companion object {
        private val TAG = MainFragment::class.java.name
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        headerList.add(HeaderTitleModel(0, "Image"))
        headerList.add(HeaderTitleModel(1, "Audio"))
        headerList.add(HeaderTitleModel(2, "Video"))
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        loadData()
        val mBackgroundManager = BackgroundManager.getInstance(activity)
        mBackgroundManager.attach(requireActivity().window)
        mainFragmentRegistry.registerFragment(
            PageRow::class.java,
            PageRowFragmentFactory()
        )
    }


    private fun loadData() {
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        adapter = rowsAdapter
        for (i in 0 until headerList.size) {
            val header = HeaderItem(headerList[i].headerID.toLong(), headerList[i].headerName)
            val pageRow = PageRow(header)
            rowsAdapter.add(pageRow)
        }
//        val headerImage = HeaderItem(IMAGE_HEADER, "Image")
//        val pageRowImage = PageRow(headerImage)
//        rowsAdapter.add(pageRowImage)
//        val headerAudio = HeaderItem(AUDIO_HEADER, "Audio")
//        val pageRowAudio = PageRow(headerAudio)
//        rowsAdapter.add(pageRowAudio)
//        val headerVideo = HeaderItem(VIDEO_HEADER, "Video")
//        val pageRowVideo = PageRow(headerVideo)
//        rowsAdapter.add(pageRowVideo)

    }

//
//    private fun setRows() {
//        val gridPresenter = ImagePresenter(requireActivity())
//
//        //creating instance of adapter which holds data from gridpresenter
//        val gridRowAdapter = ArrayObjectAdapter(gridPresenter)
//
//
//        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
//        val headerItem1 = HeaderItem(0, "header 0")
//
//
//        rowsAdapter.add(ListRow(headerItem1, gridRowAdapter))
//        adapter = rowsAdapter
//
//    }


    private fun setupUI() {

        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true
        brandColor = ContextCompat.getColor(requireContext(), R.color.gray)

    }

//
//    private fun loadRowsData() {
//        //creating instance of rows adapter which will hold rows
//        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
//
//        //creating title that displays in header in left side
//        val gridHeader = HeaderItem(0, "Menu One")
//        val gridHeader1 = HeaderItem(1, "Menu Two")
//
//        //creating new instance of GridItemPresenter Class
//        val gridPresenter = ImagePresenter(requireActivity())
//
//        //creating instance of adapter which holds data from gridpresenter
//        val gridRowAdapter = ArrayObjectAdapter(gridPresenter)
//
//        //adding set of dummy card items
//        for (i in 1..6) {
//            gridRowAdapter.add("Movie $i")
//        }
//
//        //adding list of all rows along with header to rowsAdapter
//        rowsAdapter.add(ListRow(gridRowAdapter))
//        rowsAdapter.add(ListRow(gridRowAdapter))
//
//        //attaching our adapter to the adapter function
//        adapter = rowsAdapter
//    }

    inner class PageRowFragmentFactory :
        BrowseSupportFragment.FragmentFactory<Fragment>() {
        override fun createFragment(rowObj: Any?): Fragment {
            val row = rowObj as Row
            title = row.headerItem.name
            return when (row.headerItem.id) {
                0L -> ImageFragment()
                1L -> AudioFragment()
                2L -> VideoFragment()
                else -> throw IllegalArgumentException(String.format("Invalid row %s", rowObj))
            }
        }
    }
}