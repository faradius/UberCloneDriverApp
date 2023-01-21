package com.alex.uberclonedriverapp.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alex.uberclonedriverapp.R
import com.alex.uberclonedriverapp.models.History
import com.alex.uberclonedriverapp.utils.RelativeTime

class HistoriesAdapter (val context: Activity, val histories:ArrayList<History>): RecyclerView.Adapter<HistoriesAdapter.HistoriesAdapterViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoriesAdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardview_history, parent, false)
        return HistoriesAdapterViewHolder(view)
    }

    override fun getItemCount(): Int {
        return histories.size
    }

    override fun onBindViewHolder(holder: HistoriesAdapterViewHolder, position: Int) {
        val history = histories[position]
        holder.tvOrigin.text = history.origin
        holder.tvDestination.text = history.destination
        if (history.timestamp != null){
            holder.tvDate.text = RelativeTime.getTimeAgo(history.timestamp!!, context)
        }

    }

    class HistoriesAdapterViewHolder(view: View): RecyclerView.ViewHolder(view){
        val tvOrigin: TextView
        val tvDestination: TextView
        val tvDate: TextView

        init {
            tvOrigin = view.findViewById(R.id.tvOrigin)
            tvDestination = view.findViewById(R.id.tvDestination)
            tvDate = view.findViewById(R.id.tvDate)
        }
    }
}