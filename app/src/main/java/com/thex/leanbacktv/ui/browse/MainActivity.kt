package com.thex.leanbacktv.ui.browse

import android.app.Application
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.storage.StorageManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.thex.leanbacktv.MainApplication
import com.thex.leanbacktv.databinding.ActivityMainBinding
import com.thex.leanbacktv.utils.PermissionUtils
import com.thex.leanbacktv.utils.toast
import com.thex.leanbacktv.viewmodel.UsbActionListener
import me.jahnen.libaums.core.UsbMassStorageDevice
import me.jahnen.libaums.core.fs.FileSystem
import java.io.File
import java.io.IOException

class MainActivity : FragmentActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var usbManager: UsbManager
    lateinit var viewModelListener: UsbActionListener
    var isUsbAction: Boolean = false
    lateinit var device: UsbMassStorageDevice

    companion object {
        lateinit var fs: FileSystem
        var usbPath: String = ""

        private val TAG = MainActivity::class.java.name
        private const val STORAGE_REQUEST_CODE = 1001
        private const val ACTION_USB_PERMISSION = "com.thex.leanbacktv" + ".USB_PERMISSION"


    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        isUsbAction = false
        viewModelListener = ViewModelProvider(this).get(UsbActionListener::class.java)
        usbManager = applicationContext.getSystemService(USB_SERVICE) as UsbManager

        //set cache directory based on Android version codes
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            MainApplication.usbCachePath =
                File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath)
        }
        //start usb broadcast and discover process
        registerBroadCast()
        discoverDevice()


    }

    private fun attachFragmentToFrame() {
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, MainFragment()).commit()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun storagePermission(): Boolean {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {
                return true

            }
            else -> {
//                if (shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                    requestPermissions(
//                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
//                        STORAGE_REQUEST_CODE
//                    )
//                } else
//                {
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    STORAGE_REQUEST_CODE
                )

            }
        }




        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    registerBroadCast()
                    discoverDevice()
                }
                return
            }
        }
    }


    private fun discoverDevice() {


        val devices = UsbMassStorageDevice.getMassStorageDevices(applicationContext)

        if (devices.isEmpty()) {
            Log.d(TAG, "USB device not found::: ${devices.size}")
            Toast.makeText(applicationContext, "device not found", Toast.LENGTH_SHORT).show()
            attachFragmentToFrame()
        }

        // we only use the first device
        if (devices.isNotEmpty()) {
            // Toast.makeText(applicationContext, "device found", Toast.LENGTH_SHORT).show()

            device = devices[0]
            Log.d(TAG, "USB device recognized")

            val usbDevice = this.intent?.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)


            val sendIntent = Intent(ACTION_USB_PERMISSION)

            sendBroadcast(sendIntent)
//            if (usbDevice != null && usbManager!!.hasPermission(usbDevice)
//            ) {
//                Toast.makeText(applicationContext, "received device via intent", Toast.LENGTH_SHORT)
//                    .show()
//                MainApplication.hasRecognizedDevice = true
//
//
//                setupDevice()
//            } else {
//                // setupDevice()
//                val permissionIntent = PendingIntent.getBroadcast(
//                    applicationContext, 0, Intent(
//                        ACTION_USB_PERMISSION
//                    ), 0
//
//                )
//
//                usbManager!!.requestPermission(device.usbDevice, permissionIntent)
//
//
//        }
        }
    }

    private fun registerBroadCast() {
        val filter = IntentFilter(ACTION_USB_PERMISSION)

        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        registerReceiver(usbReceiver, filter)
    }


    override fun onPause() {
        super.onPause()

        try {
            applicationContext.unregisterReceiver(usbReceiver)


        } catch (e: Exception) {
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        var filearray = MainApplication.usbCachePath.listFiles()
        for (file in filearray) {
            file.delete()
        }
        var filearraynew = MainApplication.usbCachePath.listFiles()

        Log.d(TAG, "onDestroy: ${filearraynew.size}")
        unregisterReceiver(usbReceiver)
    }


    private val usbReceiver = object : BroadcastReceiver() {


        @RequiresApi(Build.VERSION_CODES.M)
        override fun onReceive(context: Context?, intent: Intent?) {

            if (intent != null && storagePermission()) {
                if (intent.action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                    // on usb re attached
                    MainApplication.hasRecognizedDevice = true
                    isUsbAction = true
                    discoverDevice()


                }
                if (intent.action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                    // on usb detached
                    toast("usb detached")
                    isUsbAction = true
                    viewModelListener.isUSBAttached(false)


                }
                if (intent.action.equals(ACTION_USB_PERMISSION)) {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        MainApplication.hasRecognizedDevice = true
                        setupDevice()

                    }
//                    if (usbManager.hasPermission(device.usbDevice)) {
//                        MainApplication.hasRecognizedDevice = true
//                        setupDevice()
                    else {
                        val permissionIntent = PendingIntent.getBroadcast(
                            applicationContext, 0, Intent(
                                ACTION_USB_PERMISSION
                            ), 0
                        )

                        usbManager!!.requestPermission(device.usbDevice, permissionIntent)
                    }


                }
            }

        }

    }

    private fun setupDevice() {
        try {
            device.init()
            fs = device.partitions[0].fileSystem
            if (!isUsbAction) {
                attachFragmentToFrame()
            } else {
                viewModelListener.isUSBAttached(true)
            }

        } catch (e: IOException) {

        }
    }


}