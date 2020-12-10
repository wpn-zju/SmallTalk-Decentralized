package com.peinanweng.smalltalkblockchain.ui.main

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.Menu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.peinanweng.smalltalkblockchain.R
import com.peinanweng.smalltalkblockchain.service.ISmallTalkService
import com.peinanweng.smalltalkblockchain.service.ISmallTalkServiceProvider
import com.peinanweng.smalltalkblockchain.service.KVPConstant
import com.peinanweng.smalltalkblockchain.service.RootService
import com.peinanweng.smalltalkblockchain.service.blockchain.InsertChannelEvent
import com.peinanweng.smalltalkblockchain.service.blockchain.InvalidEvent
import com.peinanweng.smalltalkblockchain.service.model.logic.SmallTalkApplication
import com.peinanweng.smalltalkblockchain.service.model.logic.SmallTalkViewModel
import com.peinanweng.smalltalkblockchain.service.model.logic.SmallTalkViewModelFactory
import com.peinanweng.smalltalkblockchain.ui.login.LoginActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class MainActivity : AppCompatActivity(), ISmallTalkServiceProvider {
    private val viewModel: SmallTalkViewModel by viewModels {
        SmallTalkViewModelFactory(applicationContext as SmallTalkApplication)
    }

    private lateinit var service: ISmallTalkService
    private var bound: Boolean = false
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            service = (binder as RootService.RootServiceBinder).getService()
            service.setDataAccessor((application as SmallTalkApplication).repository.getDataAccessor())
            bound = true

            val userKey: String = PreferenceManager
                .getDefaultSharedPreferences(applicationContext)
                .getString(KVPConstant.K_PRIVATE_KEY, null) ?: return

            val liveData = viewModel.getCurrentChannels(userKey)
            liveData.observe(this@MainActivity, { channels ->
                if (channels.isNotEmpty()) {
                    liveData.removeObservers(this@MainActivity)
                    channels.forEach { EventBus.getDefault().post(InsertChannelEvent(it.channel)) }
                }
            })
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            bound = false
        }
    }

    override fun hasService(): Boolean {
        return bound
    }

    override fun getService(): ISmallTalkService? {
        return if (bound) {
            service
        } else {
            null
        }
    }

    override fun onStart() {
        super.onStart()

        Intent(this, RootService::class.java).also { intent -> startService(
            intent
        ) }

        Intent(this, RootService::class.java).also { intent -> bindService(
            intent,
            connection,
            Context.BIND_AUTO_CREATE
        ) }

        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()

        unbindService(connection)
        bound = false

        EventBus.getDefault().unregister(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleInvalid(invalidEvent: InvalidEvent) {
        startActivity(Intent(this, LoginActivity::class.java))
    }
}
