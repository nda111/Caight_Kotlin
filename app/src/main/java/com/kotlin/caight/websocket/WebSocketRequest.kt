package com.kotlin.caight.websocket

import android.content.Context
import android.text.Editable
import com.kotlin.caight.companion.Methods
import com.kotlin.caight.companion.StaticResources
import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketAdapter
import com.neovisionaries.ws.client.WebSocketFactory
import com.neovisionaries.ws.client.WebSocketFrame
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.net.ssl.SSLContext

class WebSocketRequest(uri: String, var customAdapter: RequestAdapter? = null) {

    class Message(message: Any) {

        var isBinary: Boolean = false
            private set
        var binaryMessage: ByteArray = ByteArray(0)
            private set
        var textMessage: String = ""
            private set

        init {
            if (message is ByteArray) {
                isBinary = true
                binaryMessage = message
            } else {
                isBinary = false
                textMessage = message.toString()
            }
        }

        override fun toString(): String {
            return if (isBinary) {
                val builder = StringBuilder()

                builder.append('[')
                for (byte in binaryMessage) {
                    builder.append(byte)
                    builder.append(' ')
                }
                builder.append(']')

                builder.toString()
            } else {
                textMessage
            }
        }
    }

    interface RequestAdapter {
        fun onRequest(conn: WebSocketRequest)
        fun onResponse(conn: WebSocketRequest, message: Message)
        fun onClosed()
    }

    private var thread: ExecutorService = Executors.newSingleThreadExecutor()
    private var webSocket: WebSocket? = null
    private var opened = false

    init {
        val factory = WebSocketFactory().setConnectionTimeout(5000)
        val context: SSLContext = SSLContext.getInstance("TLS")
        context.init(null, null, null)
        factory.sslContext = context
        webSocket = factory.createSocket(uri)

        webSocket!!.addListener(object : WebSocketAdapter() {
            @Throws(Exception::class)
            override fun onConnected(websocket: WebSocket, headers: Map<String, List<String>>) {
                opened = true
                customAdapter!!.onRequest(this@WebSocketRequest)
            }

            @Throws(Exception::class)
            override fun onTextMessage(websocket: WebSocket, text: String) {
                val msg = Message(text)
                customAdapter!!.onResponse(this@WebSocketRequest, msg)
            }

            @Throws(Exception::class)
            override fun onBinaryMessage(websocket: WebSocket, binary: ByteArray) {
                val msg = Message(binary)
                customAdapter!!.onResponse(this@WebSocketRequest, msg)
            }

            @Throws(Exception::class)
            override fun onCloseFrame(websocket: WebSocket, frame: WebSocketFrame) {
                opened = false
                thread.shutdown()

                customAdapter!!.onClosed()
            }
        })
    }

    fun connect(onFailed: (() -> Unit)?): WebSocketRequest {
        if (NetworkMonitor.isConnected)
        {
            thread = Executors.newSingleThreadExecutor()
            webSocket!!.connect(thread)
        }
        else
        {
            onFailed?.invoke()
        }
        return this
    }

    fun close() {
        webSocket!!.sendClose()
    }

    fun send(text: String) {
        webSocket!!.sendText(text, true)
    }

    fun send(bytes: ByteArray) {
        webSocket!!.sendBinary(bytes, true)
    }

    fun send(i: Int) {
        send(Methods.NumericBinary.intToByteArray(i))
    }

    fun send(l: Long) {
        send(Methods.NumericBinary.longToByteArray(l))
    }

    fun send(request: RequestId) {
        send(Methods.NumericBinary.intToByteArray(request.id))
    }

    fun send(editable: Editable) {
        send(editable.toString())
    }

    fun sendAuth(context: Context) {
        send(StaticResources.Account.getId(context))
        send(StaticResources.Account.getAuthenticationToken(context))
    }

    fun isOpened(): Boolean {
        return opened
    }
}