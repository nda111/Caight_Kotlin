package com.kotlin.caight.data

enum class Gender(val value: Int) {
    MALE(0b00),
    FEMALE(0b01),
    NEUTERED(0b10),
    SPAYED(0b11);

    open fun isMale(): Boolean {
        return value % 2 == 0
    }

    open fun isFemale(): Boolean {
        return value % 2 == 1
    }

    open fun isNeuteredOrSpayed(): Boolean {
        return value shr 1 == 1
    }

    companion object {
        fun fromValue(value: Int): Gender? {
            return when (value) {
                0 -> MALE
                1 -> FEMALE
                2 -> NEUTERED
                3 -> SPAYED
                else -> null
            }
        }

        fun evaluate(isMale: Boolean, neuteredOrSpayed: Boolean): Gender? {
            return if (isMale) {
                if (neuteredOrSpayed) {
                    NEUTERED
                } else {
                    MALE
                }
            } else {
                if (neuteredOrSpayed) {
                    SPAYED
                } else {
                    FEMALE
                }
            }
        }
    }
}