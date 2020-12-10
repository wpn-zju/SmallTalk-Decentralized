package com.peinanweng.smalltalkblockchain.service.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "small_talk_user", indices = [Index(value = ["user_key", "channel"], unique = true)])
data class SmallTalkChannel (
    @ColumnInfo(name = "user_key")
    @SerializedName("user_key")
    val userKey: String,
    @ColumnInfo(name = "channel")
    @SerializedName("channel")
    val channel: String,
) : Serializable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "channel_id")
    @SerializedName("channel_id")
    var channelId: Int = 0
}
