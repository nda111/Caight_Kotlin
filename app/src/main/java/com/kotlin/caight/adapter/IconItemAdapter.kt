package com.kotlin.caight.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.kotlin.caight.R

internal class IconItemAdapter() : BaseAdapter() {

    private class Item(context : Context, iconId : Int, nameId : Int, descriptionId : Int) {

        var icon : Drawable? = null
        var name : String? = null
        var description : String? = null

        init {
            val resources : Resources = context.resources;

            icon = ContextCompat.getDrawable(context, iconId)
            name = resources.getString(nameId)
            description = resources.getString(descriptionId)
        }

    }

    private val items : ArrayList<Item> = ArrayList<Item>()

    fun add(context : Context, iconId : Int, nameId : Int, descriptionId : Int) : IconItemAdapter {
        items.add(Item(context, iconId, nameId, descriptionId))
        return this
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater : LayoutInflater = parent!!.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.view_icon_item, parent, false)

        val iconImageView = view.findViewById<ImageView>(R.id.iconImageView)
        val nameTextView = view.findViewById<TextView>(R.id.nameTextView)
        val descriptionTextView = view.findViewById<TextView>(R.id.descriptionTextView)

        val item = items[position]
        iconImageView.setImageDrawable(item.icon)
        nameTextView.text = item.name
        descriptionTextView.text = item.description

        return view
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return items.count()
    }
}