package com.kotlin.caight.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.hrules.charter.CharterLine
import com.kotlin.caight.R
import com.kotlin.caight.data.Cat
import com.kotlin.caight.data.CatGroup
import com.kotlin.caight.data.Date
import com.kotlin.caight.delegate.OnCatAttributeChangedListener
import com.mindorks.placeholderview.annotations.Layout
import com.mindorks.placeholderview.annotations.Resolve
import com.mindorks.placeholderview.annotations.View
import com.mindorks.placeholderview.annotations.expand.ChildPosition
import com.mindorks.placeholderview.annotations.expand.ParentPosition

@Layout(R.layout.view_cat)
class CatView(context: Context, val cat: Cat, val group: CatGroup) : EntityViewBase(context)
{
    @View(R.id.rootLayout)
    private val rootLayout: ConstraintLayout? = null

    @View(R.id.colorView)
    private val colorView: ImageView? = null

    @View(R.id.genderImageView)
    private val genderImageView: ImageView? = null

    @View(R.id.nameTextView)
    private val nameTextView: TextView? = null

    @View(R.id.ageTextView)
    private val ageTextView: TextView? = null

    @View(R.id.weightTextView)
    private val weightTextView: TextView? = null

    @View(R.id.dateTextView)
    private val dateTextView: TextView? = null

    @View(R.id.weightChart)
    private val weightChart: CharterLine? = null

    @View(R.id.deleteImageButton)
    private val deleteButton: ImageButton? = null

    @View(R.id.editButton)
    private val editButton: ImageButton? = null

    @ParentPosition
    private val mParentPosition = 0

    @ChildPosition
    private val mChildPosition = 0

    private val attributeChangedListener: OnCatAttributeChangedListener = object : OnCatAttributeChangedListener
    {
        override fun changed(id: Int, newValue: Any?)
        {
            updateCatAttr(id, newValue)
        }
    }

    var onClickListener: OnEntityListItemTouchListener? = null
    var onDeleteListener: OnEntityListItemTouchListener? = null
    var onEditListener: OnEntityListItemTouchListener? = null

    private fun updateCatAttr(id: Int, newValue: Any?)
    {
        when (id)
        {
            OnCatAttributeChangedListener.IdColor    -> colorView!!.imageTintList = ColorStateList.valueOf(cat.colorInteger)

            OnCatAttributeChangedListener.IdName     -> nameTextView!!.text = cat.name

            OnCatAttributeChangedListener.IdGender   -> genderImageView!!.setImageDrawable(
                context.resources.getDrawable(
                    if (cat.gender.isMale()) R.drawable.ic_gender_male
                    else R.drawable.ic_gender_female,
                    context.theme
                )
            )

            OnCatAttributeChangedListener.IdBirthday ->
            {
                val age: IntArray = cat.age
                val ageBuilder = StringBuilder()
                ageBuilder.append('(')
                if (age[0] == 0)
                {
                    ageBuilder.append(age[1])
                    ageBuilder.append(context.resources.getString(R.string.unit_old_month))
                }
                else
                {
                    ageBuilder.append(age[0])
                    ageBuilder.append(context.resources.getString(R.string.unit_old_year))
                }
                ageBuilder.append(')')
                ageTextView!!.text = ageBuilder.toString()
            }

            OnCatAttributeChangedListener.IdWeights  ->
                if (cat.weights.size > 0)
                {
                    val weightBuilder = StringBuilder()
                    val lastEntry: Map.Entry<Date, Float> = cat.lastWeight!!
                    weightBuilder.append(lastEntry.value)
                    weightBuilder.append(context.resources.getString(R.string.unit_kg))
                    weightTextView!!.text = weightBuilder.toString()
                    val dateBuilder = StringBuilder()
                    dateBuilder.append('(')
                    dateBuilder.append(lastEntry.key.year)
                    dateBuilder.append('.')
                    dateBuilder.append(lastEntry.key.month)
                    dateBuilder.append('.')
                    dateBuilder.append(lastEntry.key.day)
                    dateBuilder.append(')')
                    dateTextView!!.text = dateBuilder.toString()
                    val wCollections: Collection<Float> = cat.weights.values
                    val weights = FloatArray(wCollections.size)
                    val iterator = wCollections.iterator()
                    var idx = 0
                    while (iterator.hasNext())
                    {
                        weights[idx++] = iterator.next()
                    }
                    weightChart!!.values = if (weights.size == 1) floatArrayOf(weights[0], weights[0]) else weights
                }
                else
                {
                    weightTextView!!.text = null
                    dateTextView!!.text = null
                    weightChart!!.values = FloatArray(0)
                }

            OnCatAttributeChangedListener.IdSpecies  ->
            {
            }
        }
    }

    override fun setGuiComponents()
    {
        // weightChart
        weightChart!!.isIndicatorVisible = false

        // editButton
        editButton!!.isFocusable = false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setEventListeners()
    {
        rootLayout!!.setOnClickListener {
            onClickListener?.onClick(thisEntity)
        }
        deleteButton!!.setOnClickListener {
            onDeleteListener?.onClick(thisEntity)
        }
        editButton!!.setOnClickListener {
            onEditListener?.onClick(thisEntity)
        }
    }

    @Resolve
    override fun onResolve()
    {
        super.onResolve()
        updateCatAttr(OnCatAttributeChangedListener.IdColor, null)
        updateCatAttr(OnCatAttributeChangedListener.IdName, null)
        updateCatAttr(OnCatAttributeChangedListener.IdGender, null)
        updateCatAttr(OnCatAttributeChangedListener.IdBirthday, null)
        updateCatAttr(OnCatAttributeChangedListener.IdWeights, null)
    }

    override fun onExpand()
    {
    }

    override fun onCollapse()
    {
    }
}