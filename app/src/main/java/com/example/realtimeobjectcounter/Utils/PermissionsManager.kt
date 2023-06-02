package com.example.realtimeobjectcounter.Utils

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.realtimeobjectcounter.R
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.PermissionListener

class PermissionsManager constructor(private val activity: AppCompatActivity) {

    fun dexterPermissions(permission:String): Boolean {
        var granted = false
        Dexter.withContext(activity)
            .withPermission(permission)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    // Permission granted, do something
                    granted = true
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    // Permission denied, show explanation or do something else
                    granted = false
                    showSettingsDialog()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: com.karumi.dexter.listener.PermissionRequest?,
                    token: PermissionToken?
                ) {
                    // Show explanation and ask for permission again
                    token?.continuePermissionRequest()
                }

            }).check()
        return granted
    }

    fun showSettingsDialog() {
        // Show settings dialog
        val settingDialog = AlertDialog.Builder(
            activity,
            R.style.AlertDialogTheme
        )
        settingDialog.setTitle("Need Permissions")
            .setMessage("This app needs camera permission to use this feature. You can grant them in app settings")
            .setCancelable(false)
            .setPositiveButton("Go to Settings") { dialog, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", activity.packageName, null)
                activity.startActivity(intent)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.create()

        settingDialog.show()
    }

}