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
import com.thex.leanbacktv.presenter.AudioPresenter
import com.thex.leanbacktv.ui.browse.MainActivity
import com.thex.leanbacktv.utils.toast
import com.thex.leanbacktv.viewmodel.AudioViewModel
import com.thex.leanbacktv.viewmodel.UsbActionListener
import me.jahnen.libaums.core.fs.UsbFile


class AudioFragment : VerticalGridSupportFragment(),
    BrowseSupportFragment.MainFragmentAdapterProvider {
    lateinit var viewModel: AudioViewModel
    lateinit var viewModelListener: UsbActionListener

    lateinit var gridAdapter: ArrayObjectAdapter
    lateinit var filePath: String
    lateinit var fileName: String

    companion object {
        fun newInstance(fileName: String, filePath: String): VerticalGridSupportFragment {
            val fragment = AudioFragment()
            val bundle = Bundle()
            bundle.putString("fileName", fileName)
            bundle.putString("filePath", filePath)
            fragment.arguments = bundle

            return fragment
        }

        private val TAG = AudioFragment::class.java.name

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {

            fileName = requireArguments().getString("fileName", "")
            filePath = requireArguments().getString("filePath", "")
            title = fileName
        }
        viewModel = ViewModelProvider(requireActivity()).get(AudioViewModel::class.java)
        viewModelListener = ViewModelProvider(requireActivity()).get(UsbActionListener::class.java)

        val gridPresenter = VerticalGridPresenter()
        gridPresenter.numberOfColumns = 3
        setGridPresenter(gridPresenter)
        val itemPresenter = AudioPresenter(requireActivity())
        gridAdapter = ArrayObjectAdapter(itemPresenter)
        adapter = gridAdapter
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.mediaLiveData.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                loadGrid(it)
            } else {
                requireActivity().toast("No Data")
            }
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
                Log.d(TAG, "bundle file path: ${file?.absolutePath}")
                if (file != null) {
                    viewModel.readAllAudios(file)
                }
            } else {
                viewModel.readAllAudios(root)
            }

        } else {

            loadGrid(ArrayList<MediaDataModel>())


        }
    }

    private fun loadGrid(audioList: ArrayList<MediaDataModel>) {
        for (audio in audioList) {
            val mediaModel = MediaDataModel(
                audio.id,
                audio.fileName,
                "Audio",
                audio.filePath,
                audio.isDirectory,
                null
            )
            gridAdapter.add(mediaModel)
        }
    }

    override fun getMainFragmentAdapter(): BrowseSupportFragment.MainFragmentAdapter<*> {

        return BrowseSupportFragment.MainFragmentAdapter(this)
    }

}