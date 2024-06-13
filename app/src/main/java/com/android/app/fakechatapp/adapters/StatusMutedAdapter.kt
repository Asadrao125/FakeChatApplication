package com.android.app.fakechatapp.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.app.fakechatapp.R
import com.android.app.fakechatapp.models.Status
import com.android.app.fakechatapp.activities.viewstatus.ViewStatusActivity
import com.bumptech.glide.Glide

class StatusMutedAdapter(private var context: Context) :
    RecyclerView.Adapter<StatusMutedAdapter.MyViewHolder>() {
    private var mList: ArrayList<Status> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.item_status_muted, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val status: Status = mList[position]
        holder.tvUserName.text = status.userName

        if (status.statusType == 1) {
            holder.status.text = status.statusMessage
            holder.statusImage.visibility = View.GONE
            holder.status.visibility = View.VISIBLE
        } else if (status.statusType == 2) {
            holder.status.visibility = View.GONE
            holder.statusImage.visibility = View.VISIBLE

            Glide.with(context)
                .load(status.imagePath)
                .centerCrop()
                .placeholder(R.drawable.ic_user)
                .into(holder.statusImage)
        }

        holder.itemView.setOnClickListener {
            context.startActivity(
                Intent(context, ViewStatusActivity::class.java)
                    .putExtra("status_msg", status.statusMessage)
                    .putExtra("status_color", status.statusColor)
                    .putExtra("status_id", status.statusId)
                    .putExtra("statusType", status.statusType)
                    .putExtra("statusImagePath", status.imagePath)
            )
        }
    }

    fun submitList(newData: ArrayList<Status>) {
        mList = newData
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvUserName: TextView
        var status: TextView
        var statusLayout: RelativeLayout
        var statusImage: ImageView

        init {
            status = itemView.findViewById(R.id.status)
            tvUserName = itemView.findViewById(R.id.tvUserName)
            statusLayout = itemView.findViewById(R.id.statusLayout)
            statusImage = itemView.findViewById(R.id.statusImage)
        }
    }
}