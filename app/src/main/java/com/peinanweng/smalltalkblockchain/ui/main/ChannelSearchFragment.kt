package com.peinanweng.smalltalkblockchain.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.peinanweng.smalltalkblockchain.R
import com.peinanweng.smalltalkblockchain.service.blockchain.InsertChannelEvent
import kotlinx.android.synthetic.main.fragment_channel_search.*
import org.greenrobot.eventbus.EventBus

class ChannelSearchFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_channel_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_new_channel.setOnClickListener {
            val channel = input_new_channel.text.toString()
            input_new_channel.text.clear()
            if (channel.isNotEmpty()) {
                EventBus.getDefault().post(InsertChannelEvent(channel))
                val action = ChannelSearchFragmentDirections.newChannelEnterChat(channel)
                requireView().findNavController().navigate(action)
            }
        }
    }
}
