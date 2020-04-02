package com.kotlin.caight.view

import android.content.Context
import android.view.MotionEvent
import com.mindorks.placeholderview.annotations.Resolve
import com.mindorks.placeholderview.annotations.expand.Collapse
import com.mindorks.placeholderview.annotations.expand.Expand

abstract class EntityViewBase(val context: Context)
{
    interface OnEntityListItemTouchListener
    {
        fun onClick(sender: EntityViewBase)
    }

    protected val thisEntity: EntityViewBase
        get() = this

    @Resolve
    protected open fun onResolve()
    {
        setGuiComponents()
        setEventListeners()
    }

    protected abstract fun setGuiComponents()
    protected abstract fun setEventListeners()

    @Expand
    protected abstract fun onExpand()

    @Collapse
    protected abstract fun onCollapse()
}