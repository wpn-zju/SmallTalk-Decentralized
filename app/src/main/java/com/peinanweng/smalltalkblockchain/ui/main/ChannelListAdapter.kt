package com.peinanweng.smalltalkblockchain.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.peinanweng.smalltalkblockchain.R
import com.peinanweng.smalltalkblockchain.service.model.entity.SmallTalkChannel

class ChannelListAdapter
    : ListAdapter<SmallTalkChannel, ChannelListAdapter.ChannelListViewHolder>(ChannelListDiffCallback()) {

    override fun onBindViewHolder(holder: ChannelListViewHolder, position: Int) {
        val channel = getItem(position)
        holder.channelAvatar.setImageResource(R.mipmap.ic_launcher)
        holder.channelName.text = channel.channel

        holder.itemView.setOnClickListener {
            if (channelClickListener != null) {
                if (position != RecyclerView.NO_POSITION) {
                    channelClickListener!!.onItemClickListener(holder.itemView, channel.channel)
                }
            }
        }

        holder.itemView.setOnLongClickListener {
            if (channelClickListener != null) {
                if (position != RecyclerView.NO_POSITION) {
                    channelClickListener!!.onItemLongClickListener(holder.itemView, channel.channel)
                }
            }
            true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.layout_prefab_channel, parent, false)
        return ChannelListViewHolder(view)
    }

    inner class ChannelListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val channelAvatar: ImageView = view.findViewById(R.id.channel_icon)
        val channelName: TextView = view.findViewById(R.id.channel_name)
    }

    private var channelClickListener: ChannelClickListener? = null

    fun setChannelClickListener(listener: ChannelClickListener) {
        channelClickListener = listener
    }

    interface ChannelClickListener {
        fun onItemClickListener(view: View, channel: String)
        fun onItemLongClickListener(view: View, channel: String)
    }
}

class ChannelListDiffCallback : DiffUtil.ItemCallback<SmallTalkChannel>() {
    override fun areItemsTheSame(oldItem: SmallTalkChannel, newItem: SmallTalkChannel): Boolean {
        return oldItem.userKey == newItem.userKey && oldItem.channel == newItem.channel
    }

    override fun areContentsTheSame(oldItem: SmallTalkChannel, newItem: SmallTalkChannel): Boolean {
        return oldItem.channelId == newItem.channelId
    }
}
