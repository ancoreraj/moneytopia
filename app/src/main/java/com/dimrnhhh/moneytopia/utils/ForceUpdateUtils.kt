package com.dimrnhhh.moneytopia.utils

import android.content.Context
import android.content.pm.PackageManager
import com.dimrnhhh.moneytopia.models.AppUpdateInfoResponseModel

object ForceUpdateUtils {
    fun isForceUpdateDialogVisible(
        appUpdateInfoResponseModel: AppUpdateInfoResponseModel,
        context: Context
    ): Boolean {
        val myAppVersion = getAppVersion(context)?.first
        val playStoreVersion = appUpdateInfoResponseModel.appVersion

        return if (myAppVersion != null && playStoreVersion != null) {
            val myAppVersionCode = calculateVersionCode(myAppVersion)
            val playStoreVersionCode = calculateVersionCode(playStoreVersion)
            println("620555 - myAppVersionCode: $myAppVersionCode")
            println("620555 - playStoreVersionCode: $playStoreVersionCode")

            if (myAppVersionCode != null && playStoreVersionCode != null) {
                if (myAppVersionCode < playStoreVersionCode)
                    appUpdateInfoResponseModel.isUpdateRequired == true
                else false
            } else {
                false
            }
        } else {
            false
        }
    }

    fun calculateVersionCode(versionName: String): Int? {
        // Split the version name by '.' and parse each part
        val versionParts = versionName.split(".")

        // Ensure there are exactly 3 parts (major, minor, patch)
        if (versionParts.size != 3) return null

        return try {
            // Parse major, minor, and patch as integers
            val major = versionParts[0].toIntOrNull() ?: return null
            val minor = versionParts[1].toIntOrNull() ?: return null
            val patch = versionParts[2].toIntOrNull() ?: return null

            // Calculate version code
            (major * 10000) + (minor * 100) + patch
        } catch (e: NumberFormatException) {
            null // Return null if parsing fails
        }
    }

    private fun getAppVersion(context: Context): Pair<String?, Int>? {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionName = packageInfo.versionName
            val versionCode =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) packageInfo.longVersionCode.toInt() else
                    packageInfo.versionCode // Use `.versionCode` for API levels below 28
            Pair(versionName, versionCode)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }
    }

}