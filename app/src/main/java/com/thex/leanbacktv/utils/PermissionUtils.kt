package com.thex.leanbacktv.utils

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import android.provider.Settings


object PermissionUtils {
    fun useRunTimePermissions(): Boolean {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1
    }

    fun hasPermission(activity: Activity, permission: String?): Boolean {
        return if (useRunTimePermissions()) {
            activity.checkSelfPermission(permission.toString()) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    fun requestPermissions(activity: Activity, permission: Array<String?>?, requestCode: Int) {
        if (useRunTimePermissions()) {
            activity.requestPermissions(permission as Array<out String>, requestCode)
        }
    }

//    fun requestPermissions(fragment: Fragment, permission: Array<String?>?, requestCode: Int) {
//        if (useRunTimePermissions()) {
//            fragment.requestPermissions(permission, requestCode)
//        }
//    }

    fun shouldShowRational(activity: Activity, permission: String?): Boolean {
        return if (useRunTimePermissions()) {
            activity.shouldShowRequestPermissionRationale(permission.toString())
        } else false
    }

    fun shouldAskForPermission(activity: Activity, permission: String?): Boolean {
        return if (useRunTimePermissions()) {
            !hasPermission(activity, permission) &&
                    (!hasAskedForPermission(activity, permission) ||
                            shouldShowRational(activity, permission))
        } else false
    }

    fun goToAppSettings(activity: Activity) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", activity.packageName, null)
        )
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.startActivity(intent)
    }

    fun hasAskedForPermission(activity: Activity?, permission: String?): Boolean {
        return PreferenceManager
            .getDefaultSharedPreferences(activity)
            .getBoolean(permission, false)
    }

    fun markedPermissionAsAsked(activity: Activity?, permission: String?) {
        PreferenceManager
            .getDefaultSharedPreferences(activity)
            .edit()
            .putBoolean(permission, true)
            .apply()
    }
}