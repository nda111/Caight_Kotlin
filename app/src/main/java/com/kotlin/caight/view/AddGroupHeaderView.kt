package com.kotlin.caight.view

import android.content.Context
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.kotlin.caight.R
import com.mindorks.placeholderview.annotations.Layout
import com.mindorks.placeholderview.annotations.Resolve
import com.mindorks.placeholderview.annotations.View
import com.mindorks.placeholderview.annotations.expand.Parent
import com.mindorks.placeholderview.annotations.expand.ParentPosition
import com.mindorks.placeholderview.annotations.expand.SingleTop

@Parent
@SingleTop
@Layout(R.layout.view_icon_item)
class AddGroupHeaderView(private val context: Context, @DrawableRes val iconId: Int, @StringRes val nameId: Int, @StringRes val descriptionId: Int)
{
    @View(R.id.iconImageView)
    private val iconImageView: ImageView? = null

    @View(R.id.nameTextView)
    private val titleTextView: TextView? = null

    @View(R.id.descriptionTextView)
    private val descriptionTextView: TextView? = null

    @ParentPosition
    private val mParentPosition = 0

    @Resolve
    fun onResolve()
    {
        iconImageView!!.setImageDrawable(ContextCompat.getDrawable(context!!, iconId))
        titleTextView!!.text = context.resources.getString(nameId)
        descriptionTextView!!.text = context.resources.getString(descriptionId)
    }
}
