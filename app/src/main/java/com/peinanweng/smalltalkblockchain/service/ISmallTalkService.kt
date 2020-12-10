package com.peinanweng.smalltalkblockchain.service

import com.peinanweng.smalltalkblockchain.service.model.logic.SmallTalkDao

interface ISmallTalkService {
    fun setDataAccessor(smallTalkDao: SmallTalkDao)
    fun connect()
    fun disconnect()
    fun messageForward(channel: String, content: String, isText: Boolean)
}
