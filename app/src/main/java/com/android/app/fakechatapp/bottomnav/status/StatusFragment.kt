package com.android.app.fakechatapp.bottomnav.status

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.app.fakechatapp.R
import com.android.app.fakechatapp.adapters.StatusMutedAdapter
import com.android.app.fakechatapp.adapters.StatusRecentAdapter
import com.android.app.fakechatapp.adapters.StatusViewedAdapter
import com.android.app.fakechatapp.database.Database
import com.android.app.fakechatapp.databinding.FragmentStatusBinding
import com.android.app.fakechatapp.activities.add_status.AddStatusActivity
import com.android.app.fakechatapp.utils.Constants
import com.android.app.fakechatapp.utils.DialogCustomProgress
import com.android.app.fakechatapp.utils.SharedPref
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class StatusFragment : Fragment() {
    private lateinit var binding: FragmentStatusBinding
    private lateinit var database: Database
    private lateinit var statusRecentAdapter: StatusRecentAdapter
    private lateinit var statusViewedAdapter: StatusViewedAdapter

    //private lateinit var statusMutedAdapter: StatusMutedAdapter
    private lateinit var dialog: DialogCustomProgress
    private lateinit var sharedPref: SharedPref

    private var isHideRecentRv = false
    private var isHideViewedRv = false
    //private var isHideMutedRv = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatusBinding.inflate(inflater, container, false)
        dialog = DialogCustomProgress(activity)
        database = Database(requireContext())
        sharedPref = SharedPref(requireContext())

        // Recycler Recent
        binding.statusRecentRv.layoutManager = LinearLayoutManager(context)
        binding.statusRecentRv.setHasFixedSize(true)
        statusRecentAdapter = StatusRecentAdapter(requireContext())
        binding.statusRecentRv.adapter = statusRecentAdapter

        // Recycler Viewed
        binding.statusViewedRv.layoutManager = LinearLayoutManager(context)
        binding.statusViewedRv.setHasFixedSize(true)
        statusViewedAdapter = StatusViewedAdapter(requireContext())
        binding.statusViewedRv.adapter = statusViewedAdapter

        // Recycler Muted
        /*binding.statusMutedRv.layoutManager = LinearLayoutManager(context)
        binding.statusMutedRv.setHasFixedSize(true)
        statusMutedAdapter = StatusMutedAdapter(requireContext())
        binding.statusMutedRv.adapter = statusMutedAdapter*/

        binding.tvRecentUpdate.setOnClickListener {
            if (!isHideRecentRv) {
                val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_down)
                binding.tvRecentUpdate.setDrawableEnd(drawable!!)
                binding.statusRecentRv.visibility = GONE
                isHideRecentRv = true
            } else {
                val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_up)
                binding.tvRecentUpdate.setDrawableEnd(drawable!!)
                binding.statusRecentRv.visibility = VISIBLE
                isHideRecentRv = false
            }
        }

        binding.tvViewedUpdates.setOnClickListener {
            if (!isHideViewedRv) {
                val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_down)
                binding.tvViewedUpdates.setDrawableEnd(drawable!!)
                binding.statusViewedRv.visibility = GONE
                isHideViewedRv = true
            } else {
                val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_up)
                binding.tvViewedUpdates.setDrawableEnd(drawable!!)
                binding.statusViewedRv.visibility = VISIBLE
                isHideViewedRv = false
            }
        }

        /*binding.tvMutedUpdates.setOnClickListener {
            if (!isHideMutedRv) {
                val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_down)
                binding.tvMutedUpdates.setDrawableEnd(drawable!!)
                binding.statusMutedRv.visibility = GONE
                isHideMutedRv = true
            } else {
                val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_up)
                binding.tvMutedUpdates.setDrawableEnd(drawable!!)
                binding.statusMutedRv.visibility = VISIBLE
                isHideMutedRv = false
            }
        }*/

        binding.addStatusLayout.setOnClickListener {
            startActivity(Intent(requireContext(), AddStatusActivity::class.java))
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        getDataAndSetAdapter()
        loadProfilePic()
    }

    private fun loadProfilePic() {
        if (sharedPref.read(Constants.USER_PROFILE_PIC_PATH, "")?.isNotEmpty() == true) {
            Picasso.get()
                .load(File(sharedPref.read(Constants.USER_PROFILE_PIC_PATH, "")!!))
                .placeholder(R.drawable.ic_user)
                .into(binding.profilePic)
        }
    }

    fun getDataAndSetAdapter() {
        CoroutineScope(Dispatchers.IO).launch {
            val statusRecent = withContext(Dispatchers.IO) {
                database.getAllStatus(0)
            }

            withContext(Dispatchers.Main) {
                if (statusRecent != null) {
                    statusRecentAdapter.submitList(statusRecent)
                    binding.tvRecentUpdate.visibility = VISIBLE
                    binding.statusRecentRv.visibility = VISIBLE
                } else {
                    binding.tvRecentUpdate.visibility = GONE
                    binding.statusRecentRv.visibility = GONE
                }
            }

            val statusViewed = withContext(Dispatchers.IO) {
                database.getAllStatus(1)
            }

            withContext(Dispatchers.Main) {
                if (statusViewed != null) {
                    statusViewedAdapter.submitList(statusViewed)
                    binding.tvViewedUpdates.visibility = VISIBLE
                    binding.statusViewedRv.visibility = VISIBLE
                } else {
                    binding.tvViewedUpdates.visibility = GONE
                    binding.statusViewedRv.visibility = GONE
                }
            }

            /*val statusMuted = withContext(Dispatchers.IO) {
                database.getAllMutedStatus(1)
            }

            withContext(Dispatchers.Main) {
                if (statusMuted != null) {
                    statusMutedAdapter.submitList(statusMuted)
                    binding.tvMutedUpdates.visibility = VISIBLE
                    binding.statusMutedRv.visibility = VISIBLE
                } else {
                    binding.tvMutedUpdates.visibility = GONE
                    binding.statusMutedRv.visibility = GONE
                }
            }*/
        }
    }

    private fun TextView.setDrawableEnd(drawable: Drawable) {
        this.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
    }
}