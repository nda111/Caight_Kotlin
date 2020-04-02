package com.kotlin.caight.activity

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.kotlin.caight.R
import com.kotlin.caight.companion.Constants
import com.kotlin.caight.companion.Methods
import com.kotlin.caight.companion.StaticResources
import com.kotlin.caight.websocket.RequestId
import com.kotlin.caight.websocket.ResponseId
import com.kotlin.caight.websocket.WebSocketRequest
import kotlinx.android.synthetic.main.activity_delete_account.*

class DeleteAccountActivity : AppCompatActivity(), IMutableActivity
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete_account)

        /*
         * Action Bar
         */
        val actionBar = supportActionBar!!
        actionBar.setTitle(R.string.title_del_account)
        actionBar.setBackgroundDrawable(ColorDrawable(ResourcesCompat.getColor(resources, R.color.colorPrimaryDark, theme)))

        /*
         * Initialize GUi Components
         */
        // deleteButton
        deleteButton.setOnClickListener {
            if (emailEditText.text.toString() == StaticResources.Account.getEmail(this@DeleteAccountActivity))
            {
                disableActivity()

                WebSocketRequest(Constants.ServerAddress, object : WebSocketRequest.RequestAdapter
                {
                    var response: ResponseId = ResponseId.UNKNOWN

                    override fun onRequest(conn: WebSocketRequest)
                    {
                        conn.send(RequestId.DELETE_ACCOUNT.id)
                        conn.sendAuth(this@DeleteAccountActivity)
                    }

                    override fun onResponse(conn: WebSocketRequest, message: WebSocketRequest.Message)
                    {
                        response = ResponseId.fromId(Methods.NumericBinary.byteArrayToInt(message.binaryMessage))
                        conn.close()
                    }

                    override fun onClosed()
                    {
                        when (response)
                        {
                            ResponseId.DELETE_ACCOUNT_OK ->
                            {
                                StaticResources.AutoLogin.clear(this@DeleteAccountActivity)
                                Toast.makeText(applicationContext, R.string.msg_deleted, Toast.LENGTH_SHORT).show()

                                val intent = Intent(applicationContext, LoginEntryActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            }

                            ResponseId.DELETE_ACCOUNT_NO ->
                            {
                                runOnUiThread { Toast.makeText(this@DeleteAccountActivity, R.string.err_occurred, Toast.LENGTH_SHORT).show() }
                            }
                        }
                        runOnUiThread { enableActivity() }
                    }
                }).connect()
            }
            else
            {
                Toast.makeText(this@DeleteAccountActivity, R.string.err_enter_email, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun enableActivity()
    {
        emailEditText.isEnabled = true
        deleteButton.isEnabled = true
        progressBar.visibility = View.GONE
    }

    override fun disableActivity()
    {
        emailEditText.isEnabled = false
        deleteButton.isEnabled = false
        progressBar.visibility = View.VISIBLE
    }
}
