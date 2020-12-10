package com.peinanweng.smalltalkblockchain.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.peinanweng.smalltalkblockchain.R
import com.peinanweng.smalltalkblockchain.service.ISmallTalkServiceProvider
import com.peinanweng.smalltalkblockchain.service.KVPConstant
import com.peinanweng.smalltalkblockchain.service.model.logic.SmallTalkApplication
import com.peinanweng.smalltalkblockchain.service.model.logic.SmallTalkViewModel
import com.peinanweng.smalltalkblockchain.service.model.logic.SmallTalkViewModelFactory
import com.peinanweng.smalltalkblockchain.ui.file.FileSelectActivity
import kotlinx.android.synthetic.main.fragment_chat.*

class ChatFragment : Fragment(), ChatMessageListAdapter.ChatMessageClickListener {
    private val args: ChatFragmentArgs by navArgs()

    private val adapter = ChatMessageListAdapter()

    private val viewModel: SmallTalkViewModel by viewModels {
        SmallTalkViewModelFactory(requireContext().applicationContext as SmallTalkApplication)
    }

    private lateinit var serviceProvider: ISmallTalkServiceProvider

    override fun onAttach(context: Context) {
        super.onAttach(context)

        serviceProvider = requireActivity() as ISmallTalkServiceProvider
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter.setChatMessageClickListener(this)

        val layoutManager = LinearLayoutManager(context)
        chat_message_list.layoutManager = layoutManager
        chat_message_list.adapter = adapter

        var inited = false
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                chat_message_list.postDelayed({
                    if (layoutManager.findLastVisibleItemPosition() + 2 >= positionStart) {
                        if (inited) {
                            inited = true
                            chat_message_list.scrollToPosition(positionStart + itemCount)
                        } else {
                            chat_message_list.smoothScrollToPosition(positionStart + itemCount)
                        }
                    }
                }, 100)
            }
        })

        more_options.setOnClickListener {
            if (more_options_bar.visibility == View.GONE) {
                more_options_bar.visibility = View.VISIBLE
            } else {
                more_options_bar.visibility = View.GONE
            }
        }

        send_message.setOnClickListener {
            if (input_text.text.isNotEmpty()) {
                if (serviceProvider.hasService()) {
                    serviceProvider.getService()!!
                        .messageForward(args.channel, input_text.text.toString(), true)
                }
                input_text.text.clear()
            }
        }

        more_options_image.setOnClickListener {
            more_options_bar.visibility = View.GONE

            val intent = Intent(requireActivity(), FileSelectActivity::class.java)
            intent.putExtra("channel", args.channel)
            startActivity(intent)
        }

        viewModel.getCurrentMessageList(args.channel).observe(viewLifecycleOwner) { chatMessageList ->
            chatMessageList.let {
                adapter.submitList(it) { }
            }
        }

        chat_toolbar?.title = args.channel
    }

    override fun getCurrentAddress(): String {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KVPConstant.K_PUBLIC_ADDRESS, null)!!
    }
}
