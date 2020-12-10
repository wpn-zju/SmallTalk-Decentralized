package com.peinanweng.smalltalkblockchain.service.model.logic

import com.peinanweng.smalltalkblockchain.service.model.entity.SmallTalkChannel
import com.peinanweng.smalltalkblockchain.service.model.entity.SmallTalkMessage
import kotlinx.coroutines.flow.Flow

class SmallTalkRepository(private val smallTalkDao: SmallTalkDao) {
    fun getDataAccessor(): SmallTalkDao {
        return smallTalkDao
    }

    fun getChannels(userKey: String): Flow<List<SmallTalkChannel>> {
        return smallTalkDao.getChannels(userKey)
    }

    fun getChatMessageList(channel: String): Flow<List<SmallTalkMessage>> {
        return smallTalkDao.getChatMessageList(channel)
    }
}
