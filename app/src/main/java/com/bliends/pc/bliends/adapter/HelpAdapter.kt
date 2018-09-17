package com.bliends.pc.bliends.adapter

import android.graphics.Color
import android.os.Build
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bliends.pc.bliends.R
import com.bliends.pc.bliends.R.layout.*
import com.bliends.pc.bliends.data.Help
import com.bliends.pc.bliends.data.Help.Companion.HELP_LOST_MONEY
import com.bliends.pc.bliends.data.Help.Companion.HELP_LOST_WAY
import com.bliends.pc.bliends.data.Help.Companion.HELP_REQUEST
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.find

class HelpAdapter(private var mHelpLog: ArrayList<Help>) : RecyclerView.Adapter<HelpAdapter.ViewHolder>() {

    fun add(h: Help){
        mHelpLog.add(h)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.dateText.text = mHelpLog[position].time
        if(position == itemCount - 1) holder.background.backgroundColorResource = R.color.helpNewItemBackground
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var layout = -1
        when(viewType){
            HELP_REQUEST-> layout = help_log_request
            HELP_LOST_WAY-> layout = help_log_lost_way
            HELP_LOST_MONEY-> layout = help_log_lost_money
        }
        val v = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ViewHolder(v)
    }

    override fun getItemViewType(position: Int): Int = mHelpLog[position].type

    override fun getItemCount(): Int = mHelpLog.size

    class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        val dateText: TextView = itemView!!.find(R.id.help_time)
        val background: ConstraintLayout = itemView!!.find(R.id.background)
    }
}