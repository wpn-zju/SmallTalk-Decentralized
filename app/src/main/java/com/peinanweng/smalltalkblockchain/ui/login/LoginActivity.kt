package com.peinanweng.smalltalkblockchain.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.peinanweng.smalltalkblockchain.R
import com.peinanweng.smalltalkblockchain.service.KVPConstant
import com.peinanweng.smalltalkblockchain.ui.main.MainActivity
import com.peinanweng.smalltalkblockchain.ui.test.TestActivity
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        btn_signIn.setOnClickListener {
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString(KVPConstant.K_ENDPOINT, input_endpoint.text.toString())
                .putString(KVPConstant.K_CONTRACT_ADDRESS, input_contract_address.text.toString())
                .putString(KVPConstant.K_PRIVATE_KEY, input_key.text.toString()).apply()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
