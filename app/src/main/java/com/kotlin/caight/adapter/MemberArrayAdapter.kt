package com.kotlin.caight.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.kotlin.caight.R

class MemberArrayAdapter(context: Context, resource: Int, val list: List<Item>) : ArrayAdapter<List<MemberArrayAdapter.Item?>?>(context, resource)
{
    data class Item(val name: String, val email: String)

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int
    {
        return list.size
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View
    {
        val item = list[position]

        val view: View = inflater.inflate(R.layout.item_member_spinner, parent, false)
        (view.findViewById<View>(R.id.nameTextView) as TextView).text = item.name
        (view.findViewById<View>(R.id.emailTextView) as TextView).text = item.email

        return view
    }

    override fun getDropDownView(position: Int, convertView: View, parent: ViewGroup): View
    {
        return getView(position, convertView, parent)
    }
}