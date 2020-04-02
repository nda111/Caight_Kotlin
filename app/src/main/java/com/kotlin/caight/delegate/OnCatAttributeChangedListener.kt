package com.kotlin.caight.delegate

interface OnCatAttributeChangedListener {
    fun changed(id: Int, newValue: Any?)

    companion object {
        const val IdColor = 0
        const val IdName = 1
        const val IdBirthday = 2
        const val IdGender = 3
        const val IdSpecies = 4
        const val IdWeights = 5
    }
}