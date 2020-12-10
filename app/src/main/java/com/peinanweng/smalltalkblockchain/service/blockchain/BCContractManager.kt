package com.peinanweng.smalltalkblockchain.service.blockchain

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import com.peinanweng.smalltalkblockchain.R
import com.peinanweng.smalltalkblockchain.service.KVPConstant
import com.peinanweng.smalltalkblockchain.service.model.entity.SmallTalkChannel
import com.peinanweng.smalltalkblockchain.service.model.entity.SmallTalkMessage
import com.peinanweng.smalltalkblockchain.service.model.logic.SmallTalkDao
import com.peinanweng.smalltalkblockchain.ui.main.MainActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.apache.commons.codec.binary.Hex
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.web3j.abi.EventEncoder
import org.web3j.abi.TypeEncoder
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.http.HttpService
import org.web3j.tx.gas.DefaultGasProvider
import java.math.BigInteger
import java.security.MessageDigest

class BCContractManager(private val context: Context) {
    private val subscribeMap = mutableMapOf<String, Disposable>()
    private val gasProvider = object : DefaultGasProvider() {
        override fun getGasPrice(contractFunc: String): BigInteger {
            return BigInteger("300")
        }

        override fun getGasLimit(contractFunc: String): BigInteger {
            return BigInteger("3000000")
        }
    }

    private lateinit var httpService: HttpService
    private lateinit var web3: Web3j
    private lateinit var smalltalkDao: SmallTalkDao
    private lateinit var accounts: List<String>
    private lateinit var currentCredential: Credentials
    private lateinit var contract: Relay

    fun setDataAccessor(smallTalkDao: SmallTalkDao) {
        if (!this::smalltalkDao.isInitialized) {
            smalltalkDao = smallTalkDao
        }
    }

    fun connect() {
        EventBus.getDefault().register(this)

        val endpoint = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KVPConstant.K_ENDPOINT, null)
        val contractAddress = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KVPConstant.K_CONTRACT_ADDRESS, null)
        val userKey = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KVPConstant.K_PRIVATE_KEY, null)

        try {
            httpService = HttpService(endpoint)
            web3 = Web3j.build(httpService)
            accounts = web3.ethAccounts().sendAsync().get().accounts
            currentCredential = Credentials.create(userKey)
            contract = Relay.load(contractAddress, web3, currentCredential, gasProvider)
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(KVPConstant.K_PUBLIC_ADDRESS, currentCredential.address).apply()
        } catch (e: Exception) {
            e.printStackTrace()
            EventBus.getDefault().post(InvalidEvent())
        }
    }

    fun disconnect() {
        EventBus.getDefault().unregister(this)

        subscribeMap.forEach { (_, u) -> u.dispose() }
    }

    private fun addSubscribe(channel: String) {
        val userKey = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KVPConstant.K_PRIVATE_KEY, null) ?: return

        smalltalkDao.insertChannel(SmallTalkChannel(userKey, channel))

        val filter = EthFilter(
            DefaultBlockParameterName.EARLIEST,
            DefaultBlockParameterName.LATEST,
            contract.contractAddress)
            .addSingleTopic(EventEncoder.encode(Relay.MESSAGE_EVENT))
            .addOptionalTopics("0x" + TypeEncoder.encode(Uint256(getChannelId(channel))))

        val subscriber = contract.messageEventFlowable(filter)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { event -> EventBus.getDefault()
                    .post(InsertMessageEvent(channel, event.sender, event.message, event.isText)) },
                { err -> Log.d("RECEIVE ERROR", err.toString()) })

        subscribeMap[channel] = subscriber
    }

    private fun removeSubscribe(channel: String) {
        val userKey = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KVPConstant.K_PRIVATE_KEY, null) ?: return
        smalltalkDao.deleteChannel(userKey, channel)
        subscribeMap[channel]?.dispose()
        subscribeMap.remove(channel)
    }

    fun sendMessage(channel: String, content: String, isText: Boolean) {
        contract.send(getChannelId(channel), content, isText).sendAsync()
    }

    private fun getChannelId(channel: String): BigInteger {
        val digest = MessageDigest.getInstance("SHA-256").digest(channel.toByteArray())
        val hexStr = Hex.encodeHexString(digest)
        return BigInteger(hexStr, 16)
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onInsertChannelMessage(insertChannelEvent: InsertChannelEvent) {
        addSubscribe(insertChannelEvent.channel)
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onDeleteChannelMessage(deleteChannelEvent: DeleteChannelEvent) {
        removeSubscribe(deleteChannelEvent.channel)
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onInsertMessageMessage(insertMessageEvent: InsertMessageEvent) {
        smalltalkDao.insertMessage(SmallTalkMessage(insertMessageEvent.channel, insertMessageEvent.sender, insertMessageEvent.message, insertMessageEvent.isText))
        if (!PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KVPConstant.K_IS_FOREGROUND, true)) {
            val notificationText: String = if (insertMessageEvent.isText) insertMessageEvent.message else "[Image]"
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("channel", insertMessageEvent.channel)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            val builder = NotificationCompat.Builder(context, "Message")
                .setSmallIcon(R.drawable.ic_baseline_message_24)
                .setContentTitle("Small Talk")
                .setContentText(notificationText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
            with(NotificationManagerCompat.from(context)) {
                notify(10000, builder.build())
            }
        }
    }
}
