package com.kotlin.caight.companion

import android.content.Context
import android.graphics.Color
import com.kotlin.caight.data.Date
import com.kotlin.caight.delegate.ResponseListener
import com.kotlin.caight.websocket.RequestId
import com.kotlin.caight.websocket.ResponseId
import com.kotlin.caight.websocket.WebSocketRequest
import java.util.*

object Methods
{
    object NumericBinary
    {
        fun intToByteArray(i: Int): ByteArray
        {
            return byteArrayOf(
                i.toByte(),
                (i shr 8).toByte(),
                (i shr 16).toByte(),
                (i shr 24).toByte()
            )
        }

        fun longToByteArray(l: Long): ByteArray
        {
            return byteArrayOf(
                l.toByte(),
                (l shr 8).toByte(),
                (l shr 16).toByte(),
                (l shr 24).toByte(),
                (l shr 32).toByte(),
                (l shr 40).toByte(),
                (l shr 48).toByte(),
                (l shr 56).toByte()
            )
        }

        fun byteArrayToInt(b: ByteArray): Int
        {
            return b[0].toInt() or (b[1].toInt() shl 8) or (b[1].toInt() shl 16) or (b[1].toInt() shl 24)
        }

        fun byteArrayToLong(b: ByteArray): Int
        {
            return b[0].toInt() or (b[1].toInt() shl 8) or (b[2].toInt() shl 16) or (b[3].toInt() shl 24) or (b[4].toInt() shl 32) or (b[5].toInt() shl 40) or (b[6].toInt() shl 48) or (b[7].toInt() shl 56)
        }
    }

    object Hex
    {
        fun toHexId(id: Int): String
        {
            val idString = id.toString(16).toUpperCase(Locale.ROOT)
            val idBuilder = StringBuilder()
            var cnt0 = 8 - idString.length

            idBuilder.append('#')
            while (cnt0-- > 0)
            {
                idBuilder.append('0')
            }
            idBuilder.append(idString)

            return idBuilder.toString()
        }

        fun Color.toHexCode(): String
        {
            val color = this.toArgb()

            val r = color shr 16 and 0xFF
            val g = color shr 8 and 0xFF
            val b = color and 0xFF

            val locale = Locale.getDefault()
            val hex = java.lang.StringBuilder("#")
            hex.append(r.toString(16).toUpperCase(locale).padStart(2, '0'))
            hex.append(g.toString(16).toUpperCase(locale).padStart(2, '0'))
            hex.append(b.toString(16).toUpperCase(locale).padStart(2, '0'))

            return hex.toString()
        }
    }

    object DateFormatter
    {
        private var str: String? = null

        fun parse(str: String): Date?
        {
            DateFormatter.str = str
            val part = str.split("-").toTypedArray()
            if (part.size != 3)
            {
                return null
            }

            val parsed = intArrayOf(part[0].toInt(), part[1].toInt(), part[2].toInt())
            return Date(parsed[0], parsed[1].toShort(), parsed[2].toShort())
        }

        fun format(date: Date): String
        {
            val builder = StringBuilder()

            builder.append(date.year)
            builder.append(". ")

            val month: Short = date.month
            if (month < 10)
            {
                builder.append('0')
            }
            builder.append(month)
            builder.append(". ")

            val day: Short = date.day
            if (day < 10)
            {
                builder.append('0')
            }
            builder.append(day)

            return builder.toString()
        }
    }

    object Request
    {
        fun login(context: Context, email: String, password: String, action: ResponseListener)
        {
            WebSocketRequest(Constants.ServerAddress, object : WebSocketRequest.RequestAdapter
            {
                private var response: ResponseId = ResponseId.UNKNOWN
                private var args: LinkedList<WebSocketRequest.Message> = LinkedList<WebSocketRequest.Message>()
                private var count = 0

                override fun onRequest(conn: WebSocketRequest)
                {
                    val builder = StringBuilder()
                    builder.append(email)
                    builder.append(Constants.NullChar)
                    builder.append(password)

                    conn.send(RequestId.SIGN_IN)
                    conn.send(builder.toString())
                }

                override fun onResponse(conn: WebSocketRequest, message: WebSocketRequest.Message)
                {
                    when (++count)
                    {
                        1    ->
                        {
                            response = ResponseId.fromId(NumericBinary.byteArrayToInt(message.binaryMessage))
                            if (response !== ResponseId.SIGN_IN_OK)
                            {
                                conn.close()
                            }
                        }
                        2, 3 -> args.addFirst(message)
                        4    ->
                        {
                            args.addFirst(message)
                            args.addFirst(WebSocketRequest.Message(email))
                            conn.close()
                        }
                    }
                }

                override fun onClosed()
                {
                    action.finally(response, args)
                }
            }).connect()
        }
    }
}