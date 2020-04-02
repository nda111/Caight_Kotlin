package com.kotlin.caight.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.kotlin.caight.R
import com.kotlin.caight.companion.Methods
import com.kotlin.caight.data.CatGroup
import java.util.*

class GroupSpinnerAdapter(context: Context, id: Int, private val list: ArrayList<CatGroup>) : ArrayAdapter<List<CatGroup>>(context, id)
{
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int
    {
        return list.size
    }

    @SuppressLint("ViewHolder", "SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View
    {
        val item: CatGroup = list[position]
        val view: View = inflater.inflate(R.layout.item_group_spinner, parent, false)

        (view.findViewById<View>(R.id.groupTextView) as TextView).text = item.name
        (view.findViewById<View>(R.id.idTextView) as TextView).text = '(' + Methods.Hex.toHexId(item.id) + ')'

        return view
    }

    override fun getDropDownView(position: Int, convertView: View, parent: ViewGroup): View
    {
        return getView(position, convertView, parent)
    }
}