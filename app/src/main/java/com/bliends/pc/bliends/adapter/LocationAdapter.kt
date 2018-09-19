package com.bliends.pc.bliends.adapter

import android.content.Context
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.bliends.pc.bliends.R
import com.bliends.pc.bliends.data.Location
import com.bliends.pc.bliends.util.AddMarkerUtil
import com.google.android.gms.maps.GoogleMap
import org.jetbrains.anko.find

class LocationAdapter(private val context: Context,
                      private val googleMap: GoogleMap,
                      private val behavior: BottomSheetBehavior<LinearLayout>,
                      private var mLocationLog: ArrayList<Location>) :
        RecyclerView.Adapter<LocationAdapter.ViewHolder>(), View.OnClickListener {

    private lateinit var view : View

    fun add(l: Location){
        mLocationLog.add(l)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.location_log_item, parent, false)
        view = v.rootView.find(R.id.location_log_item_layout)
        view.setOnClickListener(this)
        return ViewHolder(v)
    }

    override fun getItemViewType(position: Int): Int{
        return 0
    }

    override fun getItemCount(): Int = mLocationLog.size

    class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView)

    override fun onClick(v: View?) {
        AddMarkerUtil.addWad(context, googleMap, 36.3907123, 127.3654646, R.drawable.location_wad)
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }
}