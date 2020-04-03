package com.kotlin.caight.websocket

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkRequest


object NetworkMonitor
{
    var callback: NetworkCallback? = null
    var isConnected: Boolean = false
        private set

    fun initialize(context: Context)
    {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val builder = NetworkRequest.Builder()
        manager.registerNetworkCallback(builder.build(), object : NetworkCallback()
        {
            override fun onAvailable(network: Network)
            {
                isConnected = true
                callback?.onAvailable(network)
            }

            override fun onLost(network: Network)
            {
                isConnected = false
                callback?.onLost(network)
            }
        })
    }
}