package com.android.app.fakechatapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.app.fakechatapp.R
import com.android.app.fakechatapp.models.Call

class CallDetailsAdapter(private var context: Context) :
    RecyclerView.Adapter<CallDetailsAdapter.MyViewHolder>() {
    private var mList: ArrayList<Call> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.item_call_details, parent, false)
        return MyViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val callModel: Call = mList[position]
        holder.tvCallDirection.text =
            if (callModel.callDirection == "INCOMING") "Incoming" else "Outgoing"
        holder.tvDateTime.text = callModel.time

        holder.tvDuration.text = callModel.callDuration
        holder.tvSize.text = callDurationToMb(callModel.callDuration).toString() + " MB"

        holder.imgCallDirection.setImageDrawable(
            if (callModel.callDirection == "INCOMING")
                ContextCompat.getDrawable(context, R.drawable.ic_incoming)
            else ContextCompat.getDrawable(context, R.drawable.ic_outgoing)
        )
    }

    private fun callDurationToMb(callDuration: String): Double {
        return try {
            val (hours, minutes) = callDuration.split(":").map { it.toInt() }
            val seconds = hours * 3600 + minutes * 60
            val kb = seconds * 100
            String.format("%.2f", kb.toDouble() / (1024 * 1024).toDouble()).toDouble()
        } catch (e: Exception) {
            e.printStackTrace()
            0.00
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newData: ArrayList<Call>) {
        mList = newData
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvCallDirection: TextView
        var tvDateTime: TextView
        var tvDuration: TextView
        var tvSize: TextView
        var imgCallDirection: ImageView

        init {
            tvCallDirection = itemView.findViewById(R.id.tvCallDirection)
            tvDateTime = itemView.findViewById(R.id.tvDateTime)
            tvDuration = itemView.findViewById(R.id.tvDuration)
            imgCallDirection = itemView.findViewById(R.id.imgCallDirection)
            tvSize = itemView.findViewById(R.id.tvSize)
        }
    }
}