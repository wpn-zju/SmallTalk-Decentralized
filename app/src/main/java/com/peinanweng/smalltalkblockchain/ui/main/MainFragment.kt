package com.peinanweng.smalltalkblockchain.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.peinanweng.smalltalkblockchain.R
import com.peinanweng.smalltalkblockchain.service.ISmallTalkServiceProvider
import com.peinanweng.smalltalkblockchain.service.KVPConstant
import com.peinanweng.smalltalkblockchain.service.blockchain.DeleteChannelEvent
import com.peinanweng.smalltalkblockchain.service.model.logic.SmallTalkApplication
import com.peinanweng.smalltalkblockchain.service.model.logic.SmallTalkViewModel
import com.peinanweng.smalltalkblockchain.service.model.logic.SmallTalkViewModelFactory
import com.peinanweng.smalltalkblockchain.ui.login.LoginActivity
import kotlinx.android.synthetic.main.fragment_main.*
import org.greenrobot.eventbus.EventBus

class MainFragment : Fragment(), ChannelListAdapter.ChannelClickListener {
    private val adapter = ChannelListAdapter()

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
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).setSupportActionBar(main_toolbar)

        adapter.setChannelClickListener(this)
        recycler_view_channel.layoutManager = LinearLayoutManager(context)
        recycler_view_channel.adapter = adapter

        val userKey: String = PreferenceManager
            .getDefaultSharedPreferences(requireActivity().applicationContext)
            .getString(KVPConstant.K_PRIVATE_KEY, null) ?: return

        viewModel.getCurrentChannels(userKey).observe(viewLifecycleOwner, { channels ->
            channels.let {
                adapter.submitList(channels)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_join_channel -> {
                val action = MainFragmentDirections.channelListNewChannel()
                requireView().findNavController().navigate(action)
            }
            R.id.navigation_switch_account -> {
                startActivity(Intent(requireActivity(), LoginActivity::class.java))
            }
        }
        return true
    }

    override fun onItemClickListener(view: View, channel: String) {
        val action = MainFragmentDirections.channelListEnterChat(channel)
        requireView().findNavController().navigate(action)
    }

    override fun onItemLongClickListener(view: View, channel: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Channel")
            .setMessage(String.format("Channel %s will be deleted!", channel))
            .setPositiveButton("Confirm") { _, _ ->
                EventBus.getDefault().post(DeleteChannelEvent(channel))}
            .setNegativeButton("Cancel", null)
            .show()
    }
}
