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
import com.peinanweng.smalltalkblockchain.service.model.entity.SmallTalkMessage
import com.squareup.picasso.Picasso

class ChatMessageListAdapter
    :ListAdapter<SmallTalkMessage, ChatMessageListAdapter.MessageViewHolder>(ChatMessageListDiffCallback()) {

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = getItem(position)
        holder.avatar.setImageResource(R.mipmap.ic_launcher)

        when (message.isText) {
            true -> {
                val rHolder = holder as TextViewHolder
                rHolder.content.text = message.content
            }
            false -> {
                val rHolder = holder as ImageViewHolder
                Picasso.Builder(holder.itemView.context).listener { _, _, e -> e.printStackTrace() }.build()
                    .load(message.content).into(rHolder.content)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            MSG_TXT_R -> {
                TextViewHolder(layoutInflater.inflate(R.layout.layout_prefab_message_text_right, parent, false))
            }
            MSG_TXT_L -> {
                TextViewHolder(layoutInflater.inflate(R.layout.layout_prefab_message_text_left, parent, false))
            }
            MSG_IMG_R -> {
                ImageViewHolder(layoutInflater.inflate(R.layout.layout_prefab_message_image_right, parent, false))
            }
            MSG_IMG_L -> {
                ImageViewHolder(layoutInflater.inflate(R.layout.layout_prefab_message_image_left, parent, false))
            }
            else -> {
                TextViewHolder(layoutInflater.inflate(R.layout.layout_prefab_message_text_left, parent, false))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message: SmallTalkMessage = getItem(position)
        val userAddress: String = chatMessageClickListener!!.getCurrentAddress()
        val isSubject: Boolean = message.sender == userAddress
        return when (message.isText) {
            true -> if (isSubject) MSG_TXT_R else MSG_TXT_L
            false -> if (isSubject) MSG_IMG_R else MSG_IMG_L
        }
    }

    open inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: ImageView = view.findViewById(R.id.message_avatar)
    }

    inner class TextViewHolder(view: View) : MessageViewHolder(view) {
        val content: TextView = view.findViewById(R.id.message_text_content)
    }

    inner class ImageViewHolder(view: View) : MessageViewHolder(view) {
        val content: ImageView = view.findViewById(R.id.message_image_content)
    }

    private var chatMessageClickListener: ChatMessageClickListener? = null

    fun setChatMessageClickListener(listener: ChatMessageClickListener) {
        chatMessageClickListener = listener
    }

    interface ChatMessageClickListener {
        fun getCurrentAddress(): String
    }

    companion object {
        const val MSG_TXT_L: Int = 0
        const val MSG_TXT_R: Int = 1
        const val MSG_IMG_L: Int = 2
        const val MSG_IMG_R: Int = 3
    }
}

class ChatMessageListDiffCallback : DiffUtil.ItemCallback<SmallTalkMessage>() {
    override fun areItemsTheSame(oldItem: SmallTalkMessage, newItem: SmallTalkMessage): Boolean {
        return oldItem.messageId == newItem.messageId
    }

    override fun areContentsTheSame(oldItem: SmallTalkMessage, newItem: SmallTalkMessage): Boolean {
        return oldItem.messageId == newItem.messageId
    }
}
