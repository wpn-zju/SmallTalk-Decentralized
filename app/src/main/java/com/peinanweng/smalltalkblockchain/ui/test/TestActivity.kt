package com.peinanweng.smalltalkblockchain.ui.test

import android.os.Bundle
import android.view.Menu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.peinanweng.smalltalkblockchain.R
import com.peinanweng.smalltalkblockchain.service.model.entity.SmallTalkMessage
import com.peinanweng.smalltalkblockchain.service.model.logic.SmallTalkApplication
import com.peinanweng.smalltalkblockchain.service.model.logic.SmallTalkDao
import com.peinanweng.smalltalkblockchain.service.model.logic.SmallTalkViewModel
import com.peinanweng.smalltalkblockchain.service.model.logic.SmallTalkViewModelFactory
import com.peinanweng.smalltalkblockchain.ui.main.ChatMessageListAdapter
import kotlinx.android.synthetic.main.fragment_chat.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MessageEvent(val channel: String, val sender: String, val message: String)

class TestActivity: AppCompatActivity(), ChatMessageListAdapter.ChatMessageClickListener {
    private lateinit var smallTalkDao: SmallTalkDao

    private val adapter = ChatMessageListAdapter()

    private val viewModel: SmallTalkViewModel by viewModels {
        SmallTalkViewModelFactory(applicationContext as SmallTalkApplication)
    }

    override fun onStart() {
        super.onStart()

        smallTalkDao = (applicationContext as SmallTalkApplication).repository.getDataAccessor()

        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()

        EventBus.getDefault().unregister(this)
    }

    private var counter: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        adapter.setChatMessageClickListener(this)

        val layoutManager = LinearLayoutManager(this)
        chat_message_list.layoutManager = layoutManager
        chat_message_list.adapter = adapter

        var inited = false
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                chat_message_list.postDelayed({
                    //Log.v("T1", layoutManager.findLastVisibleItemPosition().toString())
                    //Log.v("T2", layoutManager.findLastCompletelyVisibleItemPosition().toString())
                    //Log.v("T3", layoutManager.findFirstVisibleItemPosition().toString())
                    //Log.v("T4", layoutManager.findFirstCompletelyVisibleItemPosition().toString())
                    //Log.v("T5", positionStart.toString())
                    //Log.v("T6", itemCount.toString())
                    if (layoutManager.findLastVisibleItemPosition() + 2 >= positionStart) {
                        if (inited) {
                            inited = true
                            chat_message_list.scrollToPosition(positionStart + itemCount - 1)
                        } else {
                            chat_message_list.smoothScrollToPosition(positionStart + itemCount - 1)
                        }
                    }
                }, 100)
            }
        })

        viewModel.getCurrentMessageList("hello").observe(this) { chatMessageList ->
            chatMessageList.let {
                adapter.submitList(it) {
                    // Write here is also an option
                    //chat_message_list.postDelayed({
                    //    chat_message_list.smoothScrollToPosition(it.size) }, 100)
                }
            }
        }

        send_message.setOnClickListener {
            EventBus.getDefault().post(MessageEvent("hello", "TESTACCOUNT", "test" + counter++))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun getCurrentAddress(): String {
        return "TESTACCOUNT"
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun testMessage(messageEvent: MessageEvent) {
        smallTalkDao.insertMessage(SmallTalkMessage(messageEvent.channel, messageEvent.sender, messageEvent.message, true))
    }
}
