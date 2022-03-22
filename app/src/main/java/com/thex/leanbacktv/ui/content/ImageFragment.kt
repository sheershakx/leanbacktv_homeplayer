package com.thex.leanbacktv.ui.content

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.app.VerticalGridSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.VerticalGridPresenter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.thex.leanbacktv.MainApplication
import com.thex.leanbacktv.model.MediaDataModel
import com.thex.leanbacktv.presenter.ImagePresenter
import com.thex.leanbacktv.ui.browse.MainActivity
import com.thex.leanbacktv.utils.toast
import com.thex.leanbacktv.viewmodel.ImageViewModel
import com.thex.leanbacktv.viewmodel.UsbActionListener
import me.jahnen.libaums.core.fs.UsbFile


class ImageFragment : VerticalGridSupportFragment(),
    BrowseSupportFragment.MainFragmentAdapterProvider {

    lateinit var viewModel: ImageViewModel
    lateinit var viewModelListener: UsbActionListener
    lateinit var gridAdapter: ArrayObjectAdapter
    lateinit var filePath: String
    lateinit var fileName: String


    companion object {
        fun newInstance(fileName: String, filePath: String): VerticalGridSupportFragment {
            val fragment = ImageFragment()
            val bundle = Bundle()
            bundle.putString("fileName", fileName)
            bundle.putString("filePath", filePath)
            fragment.arguments = bundle

            return fragment
        }

        private val TAG = ImageFragment::class.java.name

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            fileName = requireArguments().getString("fileName").toString()
            filePath = requireArguments().getString("filePath").toString()
            title = fileName
        }


        viewModel = ViewModelProvider(requireActivity()).get(ImageViewModel::class.java)
        viewModelListener = ViewModelProvider(requireActivity()).get(UsbActionListener::class.java)
        val gridPresenter = VerticalGridPresenter()
        gridPresenter.numberOfColumns = 3
        setGridPresenter(gridPresenter)
        val itemPresenter = ImagePresenter(requireActivity())
        gridAdapter = ArrayObjectAdapter(itemPresenter)
        adapter = gridAdapter

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.mediaLiveData.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "onViewCreated: obersever size:: ${it.size}")
            loadGrid(it)
        })
        viewModelListener.attachedStatus.observe(viewLifecycleOwner, Observer {
            if (it) {
                Log.d(TAG, "on view model listener observer has data ")
                populateData()

            } else {

                requireActivity().toast("USB DETACHED, NO DATA")
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

                    viewModel.readAllImages(file)
                }
            } else {
                viewModel.readAllImages(root)
            }

        } else {
            // var mediaList: ArrayList<MediaDataModel> = ArrayList<MediaDataModel>()
//            for (i in 1..3) {
//                val mediaModel = MediaDataModel(
//                    111,
//                    "image.fileName",
//                    "Image",
//                    "image.filePath",
//                    true,
//                    null
//                )
//                mediaList.add(mediaModel)
//            }
//            loadGrid(mediaList)
            requireActivity().toast("No Data")
            loadGrid(ArrayList<MediaDataModel>())


        }
    }

    private fun loadGrid(imageList: ArrayList<MediaDataModel>) {
        for (image in imageList) {
            val mediaModel = MediaDataModel(
                image.id,
                image.fileName,
                "Image",
                image.filePath,
                image.isDirectory,
                null
            )
            gridAdapter.add(mediaModel)
        }
    }

    override fun getMainFragmentAdapter(): BrowseSupportFragment.MainFragmentAdapter<*> {

        return BrowseSupportFragment.MainFragmentAdapter(this)
    }




}