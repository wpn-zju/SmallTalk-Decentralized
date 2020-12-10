package com.peinanweng.smalltalkblockchain.service.model.logic

import androidx.room.*
import com.peinanweng.smalltalkblockchain.service.model.entity.SmallTalkChannel
import com.peinanweng.smalltalkblockchain.service.model.entity.SmallTalkMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface SmallTalkDao {
    @Query("SELECT * FROM small_talk_user WHERE user_key = :userKey ORDER BY channel_id")
    fun getChannels(userKey: String): Flow<List<SmallTalkChannel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChannel(smallTalkChannel: SmallTalkChannel)

    @Query("DELETE FROM small_talk_user WHERE user_key = :userKey AND channel = :channel")
    fun deleteChannel(userKey: String, channel: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessage(smallTalkMessage: SmallTalkMessage)

    @Delete
    fun deleteMessage(smallTalkMessage: SmallTalkMessage)

    @Query("SELECT * FROM small_talk_message WHERE message_id = :messageId ORDER BY message_id")
    fun getMessage(messageId: Int): Flow<List<SmallTalkMessage>>

    @Query("SELECT * FROM small_talk_message WHERE channel = :channel ORDER BY message_id")
    fun getChatMessageList(channel: String): Flow<List<SmallTalkMessage>>
}
