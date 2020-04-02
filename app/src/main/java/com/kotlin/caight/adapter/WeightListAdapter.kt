package com.kotlin.caight.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kotlin.caight.R
import com.kotlin.caight.companion.Methods
import com.kotlin.caight.data.Date
import java.util.*

class WeightListAdapter(private val context: Context, private val weights: TreeMap<Date, Float>, var listener: ItemEventListener?) : RecyclerView.Adapter<WeightListAdapter.ViewHolder>()
{
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val weightTextView: TextView = itemView.findViewById(R.id.weightTextView)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)

        var date: Date = Date.zeroDate
        var weight: Float = -1F
    }

    abstract class ItemEventListener
    {
        abstract fun onClick(viewHolder: ViewHolder)
        abstract fun onDelete(viewHolder: ViewHolder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val context = parent.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.item_weight_list, parent, false)
        val viewHolder = ViewHolder(view)

        view.setOnClickListener { listener!!.onClick(viewHolder) }
        viewHolder.deleteButton.setOnClickListener { listener!!.onDelete(viewHolder) }

        return viewHolder
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        holder.date = weights.keys.sorted()[weights.size - position - 1]
        holder.weight = weights[holder.date]!!
        holder.weightTextView.text = holder.weight.toString() + " " + context.resources.getString(R.string.unit_kg_no_parenthesis)
        holder.dateTextView.text = Methods.DateFormatter.format(holder.date)
    }

    override fun getItemCount(): Int
    {
        return weights.size
    }
}
