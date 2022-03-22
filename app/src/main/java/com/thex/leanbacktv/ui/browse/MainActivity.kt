package com.thex.leanbacktv.ui.browse

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
import android.os.storage.StorageManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.thex.leanbacktv.MainApplication
import com.thex.leanbacktv.databinding.ActivityMainBinding
import com.thex.leanbacktv.viewmodel.UsbActionListener
import me.jahnen.libaums.core.UsbMassStorageDevice
import me.jahnen.libaums.core.fs.FileSystem
import java.io.IOException

class MainActivity : FragmentActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var usbManager: UsbManager
    lateinit var viewModelListener: UsbActionListener
    var isUsbAction: Boolean = false
    lateinit var device: UsbMassStorageDevice

//    lateinit var fs: FileSystem
//    var usbPath: String = ""


    companion object {
        lateinit var fs: FileSystem
        var usbPath: String = ""

        private val TAG = MainActivity::class.java.name
        private const val STORAGE_REQUEST_CODE = 1001
        private const val ACTION_USB_PERMISSION = "com.thex.leanbacktv" + ".USB_PERMISSION"
        private const val ACTION_USB_STATE = "android.hardware.usb.action.USB_STATE"


    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        isUsbAction = false
        viewModelListener = ViewModelProvider(this).get(UsbActionListener::class.java)
        usbManager = applicationContext.getSystemService(USB_SERVICE) as UsbManager
        //start usb broadcast and discover process
        if (storagePermission()) {
            registerBroadCast()
            discoverDevice()
        }


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
                    Log.d(TAG, "onRequestPermissionsResult: storage permission on result::OK")
                    registerBroadCast()
                    discoverDevice()
                } else {
                    Log.d(TAG, "onRequestPermissionsResult: storage permission on result::BAD")

                }
                return
            }
        }
    }


    fun discoverDevice() {


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

            if (usbDevice != null && usbManager!!.hasPermission(usbDevice)
            ) {
                Toast.makeText(applicationContext, "received device via intent", Toast.LENGTH_SHORT)
                    .show()
                MainApplication.hasRecognizedDevice = true


                setupDevice()
            } else {
                // setupDevice()
                val permissionIntent = PendingIntent.getBroadcast(
                    applicationContext, 0, Intent(
                        ACTION_USB_PERMISSION
                    ), 0

                )

                usbManager!!.requestPermission(device.usbDevice, permissionIntent)


//                if (usbManager?.hasPermission(device.usbDevice) == false) {
//                    usbManager!!.requestPermission(device.usbDevice, permissionIntent)
//                } else {
//                    setupDevice()
//                }


            }
        }
    }

    private fun registerBroadCast() {
        val filter = IntentFilter(ACTION_USB_PERMISSION)

        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        filter.addAction(ACTION_USB_STATE)
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
        unregisterReceiver(usbReceiver)
    }


    private val usbReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.M)
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && storagePermission()) {
                if (ACTION_USB_PERMISSION == intent.action) {
                    Log.d(TAG, "onReceive: action_usb_permission")
                    synchronized(this) {
                        val device: UsbDevice? =
                            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)

                        if (intent.getBooleanExtra(
                                UsbManager.EXTRA_PERMISSION_GRANTED,
                                false
                            )
                        ) {
                            device?.apply {
                                MainApplication.hasRecognizedDevice = true
                                setupDevice()
                            }
                        }
                    }
                } else if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED == intent.action) {
                    device?.apply {
                        Log.d(TAG, "onReceive: action_accessory_attached")
                        MainApplication.hasRecognizedDevice = true

                        discoverDevice()

                    }
                }

                if (intent.action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                    // on usb re attached
                    isUsbAction = true
                    discoverDevice()

                }
                if (intent.action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                    // on usb detached
                    isUsbAction = true
                    viewModelListener.isUSBAttached(false)


                }
            }

            //-----//
//            when (intent?.action) {
//                ACTION_USB_PERMISSION -> {
//                    Log.d(TAG, "onReceive: usb action permission")
//                }
//                UsbManager.ACTION_USB_ACCESSORY_ATTACHED -> {
//                    if (storagePermission()) {
//                        Log.d(TAG, "usb accessory attached: ")
//                    }
//                }
//                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
//                    if (intent.action == ACTION_USB_PERMISSION) {
//                        MainApplication.hasRecognizedDevice = true
//                        setupDevice()
//                    } else {
//                        discoverDevice()
//                    }
//                }
//                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
//                    //on usb detached
//                    isUsbAction = true
//                    viewModelListener.isUSBAttached(false)
//                }
//
//            }
        }

    }

    private fun setupDevice() {

        try {
            device.init()
            fs = device.partitions[0].fileSystem
            Log.d(
                TAG,
                "device details: product id :${device.usbDevice.productId}, vendorid: ${device.usbDevice.vendorId} class:${device.usbDevice.deviceClass}  "
            )
            if (!isUsbAction) {
                attachFragmentToFrame()
            } else {
                viewModelListener.isUSBAttached(true)
            }


            val storageManager =
                ContextCompat.getSystemService(
                    this,
                    StorageManager::class.java
                ) as StorageManager
//            val usbPathList = getUsbPaths(storageManager)
//            if (usbPathList?.isNotEmpty() == true) {
//                Log.d(TAG, "setupDevice: usb path not empty")
//                for (path in usbPathList) {
//                    usbPath = path
//                }
//
//            }
            Log.d(TAG, "usb path is $usbPath ")


        } catch (e: IOException) {

        }
    }

//    private fun getUsbPaths(storageManager: StorageManager): List<String>? {
//        val usbPaths: MutableList<String> = ArrayList()
//        val volumes: MutableList<VolumeInfo>? = storageManager.volumes
////          Collections.sort(volumes, VolumeInfo.getDescriptionComparator());
//        if (volumes != null) {
//
//            for (vol in volumes) {
//                if (vol.getType() === VolumeInfo.TYPE_PUBLIC) {
//                    val disk: DiskInfo? = vol.getDisk()
//                    if (disk != null) {
//                        if (disk.isUsb) {
//                            usbPaths.add(vol.path)
//                        }
//                    }
//                }
//            }
//        }
//        return usbPaths
//    }


}