package com.thex.leanbacktv.ui.content

import android.os.Bundle
import android.view.View
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.app.VerticalGridSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.VerticalGridPresenter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.thex.leanbacktv.MainApplication
import com.thex.leanbacktv.model.MediaDataModel
import com.thex.leanbacktv.presenter.VideoPresenter
import com.thex.leanbacktv.ui.browse.MainActivity
import com.thex.leanbacktv.utils.toast
import com.thex.leanbacktv.viewmodel.UsbActionListener
import com.thex.leanbacktv.viewmodel.VideoViewModel
import me.jahnen.libaums.core.fs.UsbFile


class VideoFragment : VerticalGridSupportFragment(),
    BrowseSupportFragment.MainFragmentAdapterProvider {

    lateinit var viewModel: VideoViewModel
    lateinit var viewModelListener: UsbActionListener

    lateinit var gridAdapter: ArrayObjectAdapter
    lateinit var fileName: String
    lateinit var filePath: String

    companion object {
        fun newInstance(fileName: String, filePath: String): VerticalGridSupportFragment {
            val fragment = VideoFragment()
            val bundle = Bundle()
            bundle.putString("fileName", fileName)
            bundle.putString("filePath", filePath)
            fragment.arguments = bundle
            return fragment
        }

        private val TAG = VideoFragment::class.java.name

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {

            fileName = requireArguments().getString("fileName").toString()
            filePath = requireArguments().getString("filePath").toString()
            title = fileName
        }


        viewModel = ViewModelProvider(requireActivity()).get(VideoViewModel::class.java)
        viewModelListener = ViewModelProvider(requireActivity()).get(UsbActionListener::class.java)

        val gridPresenter = VerticalGridPresenter()
        gridPresenter.numberOfColumns = 3
        setGridPresenter(gridPresenter)
        val itemPresenter = VideoPresenter(requireActivity())
        gridAdapter = ArrayObjectAdapter(itemPresenter)

        adapter = gridAdapter


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.mediaLiveData.observe(viewLifecycleOwner, Observer {
            loadGrid(it)
        })
        viewModelListener.attachedStatus.observe(viewLifecycleOwner, Observer {
            if (it) {
                populateData()

            } else {
                requireActivity().toast("USB DETACHED")
                MainApplication.hasRecognizedDevice = false
                gridAdapter.clear()


            }
        })

        populateData()

    }

    private fun populateData() {
        if (MainApplication.hasRecognizedDevice) {
            var root: UsbFile = MainActivity.fs.rootDirectory
            if (arguments != null) {
                val file: UsbFile? = root.search(filePath)
                if (file != null) {
                    viewModel.readAllVideos(file)
                }
            } else {


                viewModel.readAllVideos(root)
            }

        } else {
            loadGrid(ArrayList<MediaDataModel>())

        }
    }

    private fun loadGrid(videoList: ArrayList<MediaDataModel>) {
        for (video in videoList) {
            var mediaModel = MediaDataModel(
                video.id,
                video.fileName,
                "Video",
                video.filePath,
                video.isDirectory,
                null
            )
            gridAdapter.add(mediaModel)
        }


        adapter = gridAdapter
    }

    override fun getMainFragmentAdapter(): BrowseSupportFragment.MainFragmentAdapter<*> {

        return BrowseSupportFragment.MainFragmentAdapter(this)
    }

}