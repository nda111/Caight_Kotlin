package com.kotlin.caight.companion

import com.kotlin.caight.data.Date
import java.util.*

object Methods {
    fun intToByteArray(i: Int): ByteArray {
        return byteArrayOf(
            i.toByte(),
            (i shr 8).toByte(),
            (i shr 16).toByte(),
            (i shr 24).toByte()
        )
    }

    fun longToByteArray(l: Long): ByteArray {
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

    fun byteArrayToInt(b: ByteArray): Int {
        return b[0].toInt() or (b[1].toInt() shl 8) or (b[1].toInt() shl 16) or (b[1].toInt() shl 24)
    }

    fun byteArrayToLong(b: ByteArray): Int {
        return b[0].toInt() or (b[1].toInt() shl 8) or (b[2].toInt() shl 16) or (b[3].toInt() shl 24) or (b[4].toInt() shl 32) or (b[5].toInt() shl 40) or (b[6].toInt() shl 48) or (b[7].toInt() shl 56)
    }

    fun toHexId(id: Int): String {
        val idString = id.toString(16).toUpperCase(Locale.ROOT)
        val idBuilder = StringBuilder()
        var cnt0 = 8 - idString.length

        idBuilder.append('#')
        while (cnt0-- > 0) {
            idBuilder.append('0')
        }
        idBuilder.append(idString)

        return idBuilder.toString()
    }

    object DateFormatter {
        private var str: String? = null

        fun parse(str: String): Date? {
            DateFormatter.str = str
            val part = str.split("-").toTypedArray()
            if (part.size != 3) {
                return null
            }

            val parsed = intArrayOf(part[0].toInt(), part[1].toInt(), part[2].toInt())
            return Date(parsed[0], parsed[1].toShort(), parsed[2].toShort())
        }

        fun format(date: Date): String {
            val builder = StringBuilder()

            builder.append(date.year)
            builder.append(". ")

            val month: Short = date.month
            if (month < 10) {
                builder.append('0')
            }
            builder.append(month)
            builder.append(". ")

            val day: Short = date.day
            if (day < 10) {
                builder.append('0')
            }
            builder.append(day)

            return builder.toString()
        }
    }
}