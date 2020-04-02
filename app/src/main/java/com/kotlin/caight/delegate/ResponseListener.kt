package com.kotlin.caight.delegate

import com.kotlin.caight.websocket.ResponseId
import com.kotlin.caight.websocket.WebSocketRequest
import java.util.*

interface ResponseListener {
    fun finally(response: ResponseId, args: LinkedList<WebSocketRequest.Message>);
}