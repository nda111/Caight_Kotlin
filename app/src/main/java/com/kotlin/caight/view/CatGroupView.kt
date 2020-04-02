package com.kotlin.caight.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.kotlin.caight.R
import com.kotlin.caight.companion.Methods
import com.kotlin.caight.data.CatGroup
import com.mindorks.placeholderview.annotations.Layout
import com.mindorks.placeholderview.annotations.Resolve
import com.mindorks.placeholderview.annotations.View
import com.mindorks.placeholderview.annotations.expand.*

@Parent
@SingleTop
@Layout(R.layout.view_group)
class CatGroupView(context: Context, val group: CatGroup) : EntityViewBase(context)
{
    private val expandAnimation = ContextCompat.getDrawable(context, R.drawable.ic_anim_expand) as AnimatedVectorDrawable?
    private val collapseAnimation = ContextCompat.getDrawable(context, R.drawable.ic_anim_collapse) as AnimatedVectorDrawable?

    @View(R.id.rootLayout)
    private val rootLayout: ConstraintLayout? = null

    @View(R.id.arrImageView)
    private val arrImageView: ImageView? = null

    @View(R.id.groupTextView)
    private val groupTextView: TextView? = null

    @View(R.id.idTextView)
    private val idTextView: TextView? = null

    @View(R.id.toggleLayout)
    private val toggleLayout: LinearLayout? = null

    @View(R.id.deleteImageButton)
    private val deleteImageButton: ImageButton? = null

    @View(R.id.editButton)
    private val editButton: ImageButton? = null

    @View(R.id.qrButton)
    private val qrButton: ImageButton? = null

    @ParentPosition
    val mParentPosition = 0

    var isExpanded = false
        private set

    var onClickListener: OnEntityListItemTouchListener? = null
    var onDeleteListener: OnEntityListItemTouchListener? = null
    var onEditListener: OnEntityListItemTouchListener? = null
    var onShowQrListener: OnEntityListItemTouchListener? = null

    override fun setGuiComponents()
    {
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setEventListeners()
    {
        rootLayout!!.setOnClickListener {
            onClickListener?.onClick(thisEntity)
        }
        deleteImageButton!!.setOnClickListener {
            onDeleteListener?.onClick(thisEntity)
        }
        editButton!!.setOnClickListener {
            onEditListener?.onClick(thisEntity)
        }
        qrButton!!.setOnClickListener {
            onShowQrListener?.onClick(thisEntity)
        }
    }

    @Resolve
    override fun onResolve()
    {
        super.onResolve()
        groupTextView!!.text = group.name
        idTextView!!.text = Methods.Hex.toHexId(group.id)
    }

    @Expand
    override fun onExpand()
    {
        arrImageView!!.setImageDrawable(expandAnimation)
        expandAnimation!!.start()

        isExpanded = true
    }

    @Collapse
    override fun onCollapse()
    {
        arrImageView!!.setImageDrawable(collapseAnimation)
        collapseAnimation!!.start()

        isExpanded = false
    }
}
