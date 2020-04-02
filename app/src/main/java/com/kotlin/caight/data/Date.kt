package com.kotlin.caight.data

import android.icu.util.Calendar

class Date(var year: Int, var month: Short, var day: Short) : Comparable<Date> {
    fun toLong(): Long {
        val year = year.toLong()
        val month = month.toLong()
        val day = day.toLong()
        return year shl 32 or (month shl 16) or day
    }

    fun toCalendar(): Calendar {
        val calendar = Calendar.getInstance()
        calendar[year, month.toInt(), day.toInt(), 0, 0] = 0
        return calendar
    }

    override fun equals(other: Any?): Boolean {
        if (other is Date) {
            val dOther: Date = other
            return year == dOther.year && month == dOther.month && day == dOther.day
        }
        return false
    }

    override operator fun compareTo(o: Date): Int {
        return (toLong() - o.toLong()).toInt()
    }

    companion object {
        val zeroDate: Date = Date(0, 0, 0)

        fun fromBigInt(date: Long): Date {
            val year = (date shr 32).toInt()
            val month = (date shr 16 and 0xFFFF).toShort()
            val day = (date and 0xFFFF).toShort()
            return Date(year, month, day)
        }

        fun getToday(): Date {
            return fromCalendar(Calendar.getInstance())
        }

        fun getPeriod(from: Date, to: Date): Date {
            val cFrom = Calendar.getInstance()
            val cTo = Calendar.getInstance()
            cFrom[from.year, from.month.toInt()] = from.day.toInt()
            cTo[to.year, to.month.toInt()] = to.day.toInt()
            val period = Calendar.getInstance()
            period.timeInMillis = cTo.timeInMillis - cFrom.timeInMillis
            return fromCalendar(period)
        }

        private fun fromCalendar(calendar: Calendar): Date {
            return Date(
                calendar[Calendar.YEAR],
                (calendar[Calendar.MONTH] + 1).toShort(),
                calendar[Calendar.DAY_OF_MONTH].toShort()
            )
        }
    }
}