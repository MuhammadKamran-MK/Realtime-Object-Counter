package com.example.realtimeobjectcounter.Utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

class NetworkChecker(context: Context) {

    private var connected: Boolean = false
    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @SuppressLint("ObsoleteSdkInt")
    @Suppress("DEPRECATION")
    fun isOnline(): Boolean {

        connected = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            val networkCapabilities = cm.activeNetwork ?: return false
            val activeNetwork = cm.getNetworkCapabilities(networkCapabilities) ?: return false
            activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

        } else {

            val netInfo = cm.activeNetworkInfo
            netInfo?.isConnectedOrConnecting == true

        }

        return connected

    }

}