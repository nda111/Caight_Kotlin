package com.kotlin.caight.listener

interface OnCatAttributeChangedListener {
    fun changed(id: Int, newValue: Any?)

    companion object {
        const val __ID_COLOR__ = 0
        const val __ID_NAME__ = 1
        const val __ID_BIRTHDAY__ = 2
        const val __ID_GENDER__ = 3
        const val __ID_SPECIES__ = 4
        const val __ID_WEIGHTS__ = 5
    }
}