package com.kotlin.caight.activity

import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kotlin.caight.R
import com.kotlin.caight.companion.Constants
import com.kotlin.caight.companion.Methods
import com.kotlin.caight.companion.StaticResources
import com.kotlin.caight.delegate.ResponseListener
import com.kotlin.caight.websocket.NetworkMonitor
import com.kotlin.caight.websocket.ResponseId
import com.kotlin.caight.websocket.WebSocketRequest
import java.util.*
import java.util.concurrent.Executors

class RootActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)

        /*
         * Action Bar
         */
        supportActionBar!!.hide()

        /*
         * Network Monitor
         */
        NetworkMonitor.initialize(applicationContext)
        NetworkMonitor.callback = object : ConnectivityManager.NetworkCallback()
        {
            override fun onAvailable(network: Network)
            {
                Toast.makeText(this@RootActivity, R.string.msg_back_online, Toast.LENGTH_LONG).show()
            }

            override fun onLost(network: Network)
            {
                Toast.makeText(this@RootActivity, R.string.msg_no_connection, Toast.LENGTH_LONG).show()
            }
        }

        /*
         * Initialize String Resources
         */
        StaticResources.StringArrays.initializeIfNotExists(this)

        /*
         * Auto login
         */
        if (StaticResources.AutoLogin.getDoAutoLogin(this@RootActivity))
        {
            val email = StaticResources.AutoLogin.getEmail(this@RootActivity)
            val password = StaticResources.AutoLogin.getPassword(this@RootActivity)

            if (email == null || password == null)
            {
                val intent = Intent(applicationContext, LoginEntryActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            else
            {
                Methods.Request.login(this, email, password, object : ResponseListener
                {
                    override fun finally(response: ResponseId, args: LinkedList<WebSocketRequest.Message>)
                    {
                        var failed = false

                        when (response)
                        {
                            ResponseId.SIGN_IN_OK       -> // queue: account_id, authentication_token, name, email
                            {
                                StaticResources.Account.setId(this@RootActivity, args.removeLast().binaryMessage)
                                StaticResources.Account.setAuthenticationToken(this@RootActivity, args.removeLast().textMessage)
                                StaticResources.Account.setName(this@RootActivity, args.removeLast().textMessage)
                                StaticResources.Account.setEmail(this@RootActivity, args.removeLast().textMessage)

                                val intent = Intent(applicationContext, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            }

                            ResponseId.SIGN_IN_WRONG_PW ->
                            {
                                runOnUiThread { Toast.makeText(applicationContext, R.string.err_wrong_pw, Toast.LENGTH_LONG).show() }
                                failed = true
                            }

                            ResponseId.SIGN_IN_ERROR    ->
                            {
                                runOnUiThread { Toast.makeText(applicationContext, R.string.err_occurred, Toast.LENGTH_LONG).show() }
                                failed = true
                            }
                        }

                        if (failed)
                        {
                            StaticResources.AutoLogin.set(this@RootActivity, false, null, null)

                            val intent = Intent(applicationContext, LoginEntryActivity::class.java)
                            intent.putExtra(Constants.IntentKey.Email, email)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                    }
                })
            }
        }
        else
        {
            Executors.newSingleThreadExecutor().execute {
                    Thread.sleep(1500)
                    runOnUiThread {
                        val intent = Intent(applicationContext, LoginEntryActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                }
        }
    }
}
