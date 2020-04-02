package com.kotlin.caight.view

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Button
import android.widget.TextView
import androidx.annotation.StringRes
import com.kotlin.caight.R
import com.mindorks.placeholderview.annotations.Layout
import com.mindorks.placeholderview.annotations.Resolve
import com.mindorks.placeholderview.annotations.View

@Layout(R.layout.view_add_group)
class AddGroupFormView(private val context: Context, @param:StringRes private val identifierId: Int, private val identifierType: Int, @param:StringRes private val actionNameId: Int)
{
    @View(R.id.identifierTextView)
    private val identifierTextView: TextView? = null

    @View(R.id.pwEditText)
    private val passwordTextView: TextView? = null

    @View(R.id.submitButton)
    private val submitButton: Button? = null

    var onClickListener: android.view.View.OnClickListener? = null

    val identifierLength: Int
        get() = identifierTextView!!.text.length

    val passwordLength: Int
        get() = passwordTextView!!.text.length

    val isValid: Boolean
        get() = identifierLength != 0 && passwordLength != 0

    val result: Array<String>
        get() = arrayOf(
            identifierTextView!!.text.toString(),
            passwordTextView!!.text.toString()
        )

    @SuppressLint("ClickableViewAccessibility")
    @Resolve
    fun onResolve()
    {
        identifierTextView!!.inputType = identifierType
        identifierTextView.setHint(identifierId)
        submitButton!!.setText(actionNameId)

        submitButton.setOnClickListener(onClickListener)
    }

}
