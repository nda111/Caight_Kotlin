package com.kotlin.caight.websocket

import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketAdapter
import com.neovisionaries.ws.client.WebSocketFactory
import com.neovisionaries.ws.client.WebSocketFrame
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.net.ssl.SSLContext

class WebSocketRequest {

    class Message {

        var isBinary: Boolean = false
        var binaryMessage: ByteArray = ByteArray(0)
        var textMessage: String = ""


        constructor (message: Any) {
            if (message is ByteArray) {
                isBinary = true
                binaryMessage = message
            } else {
                isBinary = false
                textMessage = message.toString()
            }
        }

        override fun toString(): String {
            if (isBinary) {
                val builder = StringBuilder()

                builder.append('[');
                for (byte in binaryMessage) {
                    builder.append(byte)
                    builder.append(' ')
                }
                builder.append(']');

                return builder.toString()
            } else {
                return textMessage
            }
        }
    }

    internal interface RequestAdapter {
        fun onRequest(conn: WebSocketRequest)
        fun onResponse(conn: WebSocketRequest, message: Message)
        fun onClosed()
    }

    private var thread: ExecutorService = Executors.newSingleThreadExecutor()
    private var webSocket: WebSocket? = null
    private var opened = false

    internal var customAdapter: WebSocketAdapter? = null
    internal var requestAdapter: RequestAdapter? = null

    constructor (uri: String, adapter: WebSocketAdapter? = null) {
        val factory = WebSocketFactory().setConnectionTimeout(5000)
        val context: SSLContext = SSLContext.getInstance("TLS")
        context.init(null, null, null)
        factory.sslContext = context

        webSocket = factory.createSocket(uri)

        webSocket!!.addListener(object : WebSocketAdapter() {
            @Throws(Exception::class)
            override fun onConnected(websocket: WebSocket, headers: Map<String, List<String>>) {
                opened = true

                requestAdapter!!.onRequest(this@WebSocketRequest)
                customAdapter!!.onConnected(websocket, headers)
            }

            @Throws(Exception::class)
            override fun onTextMessage(websocket: WebSocket, text: String) {
                val msg: WebSocketRequest.Message = WebSocketRequest.Message(text)

                requestAdapter!!.onResponse(this@WebSocketRequest, msg)
                customAdapter!!.onTextMessage(websocket, text)
            }

            @Throws(Exception::class)
            override fun onBinaryMessage(websocket: WebSocket, binary: ByteArray) {
                val msg: WebSocketRequest.Message = WebSocketRequest.Message(binary)

                requestAdapter!!.onResponse(this@WebSocketRequest, msg)
                customAdapter!!.onBinaryMessage(websocket, binary)
            }

            @Throws(Exception::class)
            override fun onCloseFrame(websocket: WebSocket, frame: WebSocketFrame) {
                opened = false
                thread.shutdown()

                requestAdapter!!.onClosed();
                customAdapter!!.onCloseFrame(websocket, frame)
            }
        })

        customAdapter = adapter
    }

    fun connect(): WebSocketRequest {
        thread = Executors.newSingleThreadExecutor()
        webSocket!!.connect(thread)
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

    fun isOpened(): Boolean {
        return opened
    }
}