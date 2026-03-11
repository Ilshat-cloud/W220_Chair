package com.example.w220chaircontrol

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat


class permissionManager(private var context: Context) {
    private val permissions = if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.S){
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT,
        )
    }else{
    TODO("VERSION.SDK_INT<S")
    }
    private val PERMiSSION_GRANTED=PackageManager.PERMISSION_GRANTED

    fun requestPermissions(){
        for (permission in permissions){
            if (ActivityCompat.checkSelfPermission(context,permission)!=PERMiSSION_GRANTED){
                ActivityCompat.requestPermissions(context as Activity, permissions,5)
            }else{
                Log.d("PERMISSIONS","Permission $permission Granted")
            }
        }

    }

}