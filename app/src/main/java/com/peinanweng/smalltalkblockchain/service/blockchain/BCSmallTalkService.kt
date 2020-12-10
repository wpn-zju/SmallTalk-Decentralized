package com.peinanweng.smalltalkblockchain.service.blockchain

import android.content.Context
import com.peinanweng.smalltalkblockchain.service.ISmallTalkService
import com.peinanweng.smalltalkblockchain.service.model.logic.SmallTalkDao


class BCSmallTalkService(context: Context) : ISmallTalkService {
    private val manager = BCContractManager(context)

    override fun connect() {
        manager.connect()
    }

    override fun disconnect() {
        manager.disconnect()
    }

    override fun setDataAccessor(smallTalkDao: SmallTalkDao) {
        manager.setDataAccessor(smallTalkDao)
    }

    override fun messageForward(channel: String, content: String, isText: Boolean) {
        manager.sendMessage(channel, content, isText)
    }
}
