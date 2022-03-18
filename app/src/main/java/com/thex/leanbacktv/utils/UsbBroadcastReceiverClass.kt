package com.thex.leanbacktv.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import com.thex.leanbacktv.MainApplication
import com.thex.leanbacktv.ui.browse.MainActivity

class UsbBroadcastReceiverClass : BroadcastReceiver() {
    companion object {
        private const val ACTION_USB_PERMISSION = "com.thex.leanbacktv" + ".USB_PERMISSION"

    }

    override fun onReceive(context: Context?, intent: Intent?) {
        MainActivity().discoverDevice()
        if (intent != null) {
            if (ACTION_USB_PERMISSION == intent.action) {
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
                            Log.d("Broadcast", "onReceive:1 ")
                            // setupDevice()
                        }
                    }
                }
            } else if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED == intent.action) {
//                device?.apply {
//                    MainApplication.hasRecognizedDevice = true
//
//                    discoverDevice()
//
//                }
            }

            if (intent.action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                Log.d("Broadcast", "onReceive:2 ")
                // on usb re attached
//                isUsbAction = true
//                finish()
//                discoverDevice()

            }
            if (intent.action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                // on usb detached
//                isUsbAction = true
//                viewModelListener.isUSBAttached(false)


            }
        }
    }
}