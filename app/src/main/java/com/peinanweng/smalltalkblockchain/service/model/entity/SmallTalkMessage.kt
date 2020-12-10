package com.peinanweng.smalltalkblockchain.service.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "small_talk_message", indices = [Index(value = ["channel", "sender", "content"], unique = true)])
data class SmallTalkMessage (
    @ColumnInfo(name = "channel")
    @SerializedName("channel")
    val channel: String,
    @ColumnInfo(name = "sender")
    @SerializedName("sender")
    val sender: String,
    @ColumnInfo(name = "content")
    @SerializedName("content")
    val content: String,
    @ColumnInfo(name = "is_text")
    @SerializedName("is_text")
    val isText: Boolean,
) : Serializable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "message_id")
    @SerializedName("message_id")
    var messageId: Int = 0
}
