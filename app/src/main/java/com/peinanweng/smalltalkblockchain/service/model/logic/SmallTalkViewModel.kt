package com.peinanweng.smalltalkblockchain.service.model.logic

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.peinanweng.smalltalkblockchain.service.model.entity.SmallTalkChannel
import com.peinanweng.smalltalkblockchain.service.model.entity.SmallTalkMessage

class SmallTalkViewModel(private val application: SmallTalkApplication, private val repository: SmallTalkRepository) : AndroidViewModel(application) {
    fun getCurrentChannels(userKey: String): LiveData<List<SmallTalkChannel>> {
        return repository.getChannels(userKey).asLiveData()
    }

    fun getCurrentMessageList(channel: String): LiveData<List<SmallTalkMessage>> {
        return repository.getChatMessageList(channel).asLiveData()
    }
}
