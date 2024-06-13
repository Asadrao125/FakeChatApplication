package com.android.app.fakechatapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.app.fakechatapp.R
import com.android.app.fakechatapp.activities.call.audio.CallActivity
import com.android.app.fakechatapp.activities.call.details.CallDetailsActivity
import com.android.app.fakechatapp.activities.call.video.VideoCallActivity
import com.android.app.fakechatapp.activities.viewimage.ImageViewActivity
import com.android.app.fakechatapp.database.MyDatabase
import com.android.app.fakechatapp.models.Call
import com.squareup.picasso.Picasso
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class CallAdapter(private var context: Context) :
    RecyclerView.Adapter<CallAdapter.MyViewHolder>() {
    private lateinit var db: MyDatabase
    private var mList: ArrayList<Call> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.item_call, parent, false)
        db = MyDatabase(context)
        return MyViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val callModel: Call = mList[position]
        holder.tvUserName.text = callModel.userName
        holder.tvDateTime.text = "Today, " + callModel.time
        holder.imgCallDirection.setImageDrawable(
            if (callModel.callDirection == "INCOMING")
                ContextCompat.getDrawable(context, R.drawable.ic_incoming)
            else ContextCompat.getDrawable(context, R.drawable.ic_outgoing)
        )

        if (callModel.callType == "AUDIO") holder.imgCall.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.ic_phone
            )
        )
        else holder.imgCall.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.ic_video_call
            )
        )

        holder.imgCall.setOnClickListener {
            if (callModel.callType == "AUDIO") {
                val callId = insertCall(callModel, callType = "AUDIO")
                context.startActivity(
                    Intent(context, CallActivity::class.java)
                        .putExtra("user_name", callModel.userName)
                        .putExtra("call_id", callId)
                        .putExtra("user_id", callModel.callReceiverId)
                        .putExtra("profile_path", callModel.callProfileImage)
                )
            } else {
                val callId = insertCall(callModel, callType = "VIDEO")
                context.startActivity(
                    Intent(context, VideoCallActivity::class.java)
                        .putExtra("user_name", callModel.userName)
                        .putExtra("call_id", callId)
                        .putExtra("user_id", callModel.callReceiverId)
                        .putExtra("profile_path", callModel.callProfileImage)
                )
            }
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, CallDetailsActivity::class.java)
            intent.putExtra("user_id", callModel.callReceiverId)
            intent.putExtra("user_name", callModel.userName)
            intent.putExtra("profile_path", callModel.callProfileImage)
            context.startActivity(intent)
        }

        holder.profilePic.setOnClickListener {
            context.startActivity(
                Intent(context, ImageViewActivity::class.java)
                    .putExtra("image_path", callModel.callProfileImage)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
        }

        Picasso.get().load(File(callModel.callProfileImage)).placeholder(R.drawable.ic_user)
            .into(holder.profilePic)
    }

    private fun insertCall(callModel: Call, callType: String): Long {
        val call = Call(
            callId = 0,
            callReceiverId = callModel.callReceiverId,
            userName = callModel.userName,
            callDirection = "OUTGOING",
            date = getCurrentDate(),
            time = getCurrentTime(),
            callType = callType,
            callDuration = "0",
            callProfileImage = callModel.callProfileImage
        )
        return db.insertCall(call)
    }

    fun getCurrentTime(): String {
        val currentTime = Calendar.getInstance().time
        val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return format.format(currentTime)
    }

    fun getCurrentDate(): String {
        val formatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)
        } else {
            throw IllegalStateException("Unable to get date")
        }
        return LocalDate.now().format(formatter)
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
        var tvUserName: TextView
        var tvDateTime: TextView
        var imgCallDirection: ImageView
        var imgCall: ImageView
        var profilePic: ImageView

        init {
            tvUserName = itemView.findViewById(R.id.tvUserName)
            tvDateTime = itemView.findViewById(R.id.tvDateTime)
            imgCallDirection = itemView.findViewById(R.id.imgCallDirection)
            imgCall = itemView.findViewById(R.id.imgCall)
            profilePic = itemView.findViewById(R.id.profilePic)
        }
    }
}