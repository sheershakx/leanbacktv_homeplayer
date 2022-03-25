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
import android.graphics.Bitmap

import android.graphics.BitmapFactory
import android.widget.ImageView.ScaleType
import java.io.File

import me.jahnen.libaums.core.fs.UsbFileStreamFactory.createBufferedInputStream
import java.io.InputStream
import kotlin.math.*


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
            //    var mediaList: ArrayList<MediaDataModel> = ArrayList<MediaDataModel>()

//            for (i in 1..3) {
//                val mediaModel = MediaDataModel(
//                    111,
//                    "fileName",
//                    "Image",
//                    "filePath",
//                    false,
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

    private fun getImageThumbnail(path: String?, mMaxWidth: Int, mMaxHeight: Int): Bitmap? {
        val mDecodeConfig = Bitmap.Config.RGB_565
        val mScaleType: ScaleType = ScaleType.CENTER_CROP
        val bitmapFile = File(path)
        var bitmap: Bitmap? = null
        if (!bitmapFile.exists() || !bitmapFile.isFile) {
            return bitmap
        }
        val decodeOptions = BitmapFactory.Options()
        decodeOptions.inInputShareable = true
        decodeOptions.inPurgeable = true
        decodeOptions.inPreferredConfig = mDecodeConfig
        if (mMaxWidth == 0 && mMaxHeight == 0) {
            bitmap = BitmapFactory.decodeFile(bitmapFile.absolutePath, decodeOptions)
        } else {
            // If we have to resize this image, first get the natural bounds.
            decodeOptions.inJustDecodeBounds = true
            BitmapFactory.decodeFile(bitmapFile.absolutePath, decodeOptions)
            val actualWidth = decodeOptions.outWidth
            val actualHeight = decodeOptions.outHeight

            // Then compute the dimensions we would ideally like to decode to.
            val desiredWidth: Int = getResizedDimension(
                mMaxWidth, mMaxHeight,
                actualWidth, actualHeight, mScaleType
            )
            val desiredHeight: Int = getResizedDimension(
                mMaxHeight, mMaxWidth,
                actualHeight, actualWidth, mScaleType
            )

            // Decode to the nearest power of two scaling factor.
            decodeOptions.inJustDecodeBounds = false
            decodeOptions.inSampleSize = findBestSampleSize(
                actualWidth,
                actualHeight,
                desiredWidth,
                desiredHeight
            )
            val tempBitmap = BitmapFactory.decodeFile(bitmapFile.absolutePath, decodeOptions)
            // If necessary, scale down to the maximal acceptable size.
            if (tempBitmap != null
                && (tempBitmap.width > desiredWidth || tempBitmap.height > desiredHeight)
            ) {
                bitmap = Bitmap.createScaledBitmap(
                    tempBitmap, desiredWidth,
                    desiredHeight, true
                )
                tempBitmap.recycle()
            } else {
                bitmap = tempBitmap
            }
        }
        return bitmap
    }

    private fun getResizedDimension(
        maxPrimary: Int, maxSecondary: Int, actualPrimary: Int,
        actualSecondary: Int, scaleType: ScaleType
    ): Int {
        // If no dominant value at all, just return the actual.
        if (maxPrimary == 0 && maxSecondary == 0) {
            return actualPrimary
        }
        // If ScaleType.FIT_XY fill the whole rectangle, ignore ratio.
        if (scaleType == ScaleType.FIT_XY) {
            return if (maxPrimary == 0) {
                actualPrimary
            } else maxPrimary
        }
        // If primary is unspecified, scale primary to match secondary's scaling ratio.
        if (maxPrimary == 0) {
            val ratio = maxSecondary.toDouble() / actualSecondary.toDouble()
            return (actualPrimary * ratio).toInt()
        }
        if (maxSecondary == 0) {
            return maxPrimary
        }
        val ratio = actualSecondary.toDouble() / actualPrimary.toDouble()
        var resized = maxPrimary
        // If ScaleType.CENTER_CROP fill the whole rectangle, preserve aspect ratio.
        if (scaleType == ScaleType.CENTER_CROP) {
            if (resized * ratio < maxSecondary) {
                resized = (maxSecondary / ratio).toInt()
            }
            return resized
        }
        if (resized * ratio > maxSecondary) {
            resized = (maxSecondary / ratio).toInt()
        }
        return resized
    }

    // Visible for testing.
    private fun findBestSampleSize(
        actualWidth: Int, actualHeight: Int, desiredWidth: Int, desiredHeight: Int
    ): Int {
        val wr = actualWidth.toDouble() / desiredWidth
        val hr = actualHeight.toDouble() / desiredHeight
        val ratio = min(wr, hr)
        var n = 1.0f
        while (n * 2 <= ratio) {
            n *= 2f
        }
        return n.toInt()
    }


}