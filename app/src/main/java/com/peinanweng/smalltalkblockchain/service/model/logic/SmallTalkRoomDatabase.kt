package com.peinanweng.smalltalkblockchain.service.model.logic

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.peinanweng.smalltalkblockchain.service.model.entity.SmallTalkChannel
import com.peinanweng.smalltalkblockchain.service.model.entity.SmallTalkMessage

@Database(entities = [
    SmallTalkChannel::class,
    SmallTalkMessage::class],
    version = 1, exportSchema = false)

abstract class SmallTalkRoomDatabase : RoomDatabase() {
    abstract fun smallTalkDao(): SmallTalkDao

    companion object {
        @Volatile
        private var INSTANCE: SmallTalkRoomDatabase? = null

        fun getDatabase(context: Context): SmallTalkRoomDatabase {
            return INSTANCE?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmallTalkRoomDatabase::class.java,
                    "small_talk_database").build()
                INSTANCE = instance
                instance
            }
        }
    }
}
