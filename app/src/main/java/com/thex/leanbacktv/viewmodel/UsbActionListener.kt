package com.thex.leanbacktv.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.thex.leanbacktv.model.MediaDataModel

class UsbActionListener : ViewModel() {
    var attachedStatus: MutableLiveData<Boolean> = MutableLiveData()

    fun isUSBAttached(status: Boolean) {
        attachedStatus.value = status
    }


}