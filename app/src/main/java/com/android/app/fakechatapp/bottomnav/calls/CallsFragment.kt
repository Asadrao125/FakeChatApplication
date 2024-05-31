package com.android.app.fakechatapp.bottomnav.calls

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.app.fakechatapp.adapters.CallAdapter
import com.android.app.fakechatapp.adapters.UserAdapter
import com.android.app.fakechatapp.database.Database
import com.android.app.fakechatapp.databinding.FragmentCallsBinding
import com.android.app.fakechatapp.utils.DialogCustomProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CallsFragment : Fragment() {
    private lateinit var binding: FragmentCallsBinding
    private lateinit var database: Database
    private lateinit var callAdapter: CallAdapter
    private lateinit var dialog: DialogCustomProgress

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCallsBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = this
        database = Database(requireContext())
        dialog = DialogCustomProgress(activity)
        binding.callsRv.layoutManager = LinearLayoutManager(context)
        binding.callsRv.setHasFixedSize(true)
        callAdapter = CallAdapter(requireContext())
        binding.callsRv.adapter = callAdapter

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        getDataAndSetAdapter()
    }

    private fun getDataAndSetAdapter() {
        CoroutineScope(Dispatchers.IO).launch {
            val calls = withContext(Dispatchers.IO) {
                database.allCalls
            }

            withContext(Dispatchers.Main) {
                if (calls != null)
                    callAdapter.submitList(calls)
            }
        }
    }
}