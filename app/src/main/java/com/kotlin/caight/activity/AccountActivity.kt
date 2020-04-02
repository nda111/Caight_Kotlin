package com.kotlin.caight.activity

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.kotlin.caight.R
import com.kotlin.caight.companion.Constants
import com.kotlin.caight.companion.Methods
import com.kotlin.caight.companion.StaticResources
import com.kotlin.caight.websocket.RequestId
import com.kotlin.caight.websocket.ResponseId
import com.kotlin.caight.websocket.WebSocketRequest
import kotlinx.android.synthetic.main.activity_account.*

class AccountActivity : AppCompatActivity(), IMutableActivity
{

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        /*
         * Action Bar
         */
        val actionBar: ActionBar = supportActionBar!!
        actionBar.setTitle(R.string.menu_account)
        actionBar.setBackgroundDrawable(ColorDrawable(ResourcesCompat.getColor(resources, R.color.colorPrimaryDark, theme)))

        /*
         * Initialize GUI Components
         */
        // nameTextView
        nameTextView.text = StaticResources.Account.getName(this)

        // nameEditImageView
        nameEditImageView.setOnClickListener {
            nameTextView.visibility = View.INVISIBLE
            nameEditImageView.visibility = View.GONE
            nameEditText.visibility = View.VISIBLE
            nameEditText.isEnabled = true
        }

        // nameEditText
        nameEditText.setText(StaticResources.Account.getName(this))
        nameEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE)
            {
                nameTextView.visibility = View.VISIBLE
                nameEditImageView.visibility = View.VISIBLE
                nameEditText.visibility = View.INVISIBLE
                nameEditText.isEnabled = false

                val newName: String = nameEditText.text.toString().trim(' ')
                if (newName.isNotEmpty() && newName != StaticResources.Account.getName(this@AccountActivity))
                {
                    disableActivity()

                    WebSocketRequest(Constants.ServerAddress, object : WebSocketRequest.RequestAdapter
                    {
                        var response: ResponseId = ResponseId.UNKNOWN
                        var name: String = ""

                        override fun onRequest(conn: WebSocketRequest)
                        {
                            name = nameEditText.text.toString().trim { it <= ' ' }
                            conn.send(RequestId.CHANGE_NAME.id)
                            conn.sendAuth(this@AccountActivity)
                            conn.send(name)
                        }

                        override fun onResponse(conn: WebSocketRequest, message: WebSocketRequest.Message)
                        {
                            response = ResponseId.fromId(Methods.NumericBinary.byteArrayToInt(message.binaryMessage))
                            conn.close()
                        }

                        override fun onClosed()
                        {
                            runOnUiThread {
                                if (response === ResponseId.CHANGE_NAME_OK)
                                {
                                    StaticResources.Account.setName(this@AccountActivity, name)
                                    nameTextView.text = name
                                }
                                else
                                {
                                    nameEditText.setText(StaticResources.Account.getName(this@AccountActivity))
                                    Toast.makeText(this@AccountActivity, R.string.err_occurred, Toast.LENGTH_SHORT).show()
                                }

                                enableActivity()
                            }
                        }
                    }).connect()
                }
                else
                {
                    nameEditText.setText(StaticResources.Account.getName(this@AccountActivity))
                }
            }
            false
        }

        // emailTextView
        emailTextView.text = StaticResources.Account.getEmail(this@AccountActivity)

        // logoutItem
        View.inflate(this, R.layout.view_icon_item, logoutItem)
        (logoutItem.findViewById(R.id.iconImageView) as ImageView).setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_logout_circle))
        (logoutItem.findViewById(R.id.nameTextView) as TextView).text = resources.getText(R.string.menu_account_logout)
        (logoutItem.findViewById(R.id.descriptionTextView) as TextView).text = resources.getText(R.string.desc_account_logout)
        logoutItem.setOnClickListener {
            disableActivity()

            WebSocketRequest(Constants.ServerAddress, object : WebSocketRequest.RequestAdapter
            {
                var response: ResponseId? = null

                override fun onRequest(conn: WebSocketRequest)
                {
                    conn.send(RequestId.LOGOUT.id)
                    conn.sendAuth(this@AccountActivity)
                }

                override fun onResponse(conn: WebSocketRequest, message: WebSocketRequest.Message)
                {
                    response = ResponseId.fromId(Methods.NumericBinary.byteArrayToInt(message.binaryMessage))
                    conn.close()
                }

                override fun onClosed()
                {
                    if (response === ResponseId.LOGOUT_OK)
                    {
                        StaticResources.AutoLogin.clear(this@AccountActivity)

                        val intent = Intent(applicationContext, LoginEntryActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                    else
                    {
                        runOnUiThread {
                            enableActivity()
                            Toast.makeText(this@AccountActivity, R.string.err_occurred, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }).connect()
        }

        // resetPwItem
        View.inflate(this, R.layout.view_icon_item, resetPwItem)
        (resetPwItem.findViewById(R.id.iconImageView) as ImageView).setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_reset_pw_circle))
        (resetPwItem.findViewById(R.id.nameTextView) as TextView).text = resources.getText(R.string.menu_account_reset_pw)
        (resetPwItem.findViewById(R.id.descriptionTextView) as TextView).text = resources.getText(R.string.desc_account_reset_pw)
        resetPwItem.setOnClickListener {
            disableActivity()

            WebSocketRequest(Constants.ServerAddress, object : WebSocketRequest.RequestAdapter
            {
                var response: ResponseId = ResponseId.UNKNOWN

                override fun onRequest(conn: WebSocketRequest)
                {
                    conn.send(RequestId.REQUEST_RESET_PASSWORD_URI.id)
                    conn.send(StaticResources.Account.getEmail(this@AccountActivity))
                }

                override fun onResponse(conn: WebSocketRequest, message: WebSocketRequest.Message)
                {
                    response = ResponseId.fromId(Methods.NumericBinary.byteArrayToInt(message.binaryMessage))
                    conn.close()
                }

                override fun onClosed()
                {
                    runOnUiThread {
                        when (response)
                        {
                            ResponseId.RESET_PASSWORD_URI_CREATED ->
                            {
                                Toast.makeText(applicationContext, R.string.msg_reset_mail_sent, Toast.LENGTH_LONG).show()
                            }

                            ResponseId.RESET_PASSWORD_URI_ERROR   ->
                            {
                                Toast.makeText(applicationContext, R.string.err_occurred, Toast.LENGTH_SHORT).show()
                            }
                        }

                        enableActivity()
                    }
                }
            }).connect()
        }

        // delAccountItem
        View.inflate(this, R.layout.view_icon_item, delAccountItem)
        (delAccountItem.findViewById(R.id.iconImageView) as ImageView).setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_delete_circle))
        (delAccountItem.findViewById(R.id.iconImageView) as ImageView).setColorFilter(ContextCompat.getColor(this, R.color.warnRed))
        (delAccountItem.findViewById(R.id.nameTextView) as TextView).text = resources.getText(R.string.menu_account_delete_account)
        (delAccountItem.findViewById(R.id.nameTextView) as TextView).setTextColor(resources.getColor(R.color.warnRed, theme))
        (delAccountItem.findViewById(R.id.descriptionTextView) as TextView).text = resources.getText(R.string.desc_account_delete_account)
        (delAccountItem.findViewById(R.id.descriptionTextView) as TextView).setTextColor(resources.getColor(R.color.warnRed, theme))
        delAccountItem.setOnClickListener() {
            val accountEmail: String = StaticResources.Account.getEmail(this@AccountActivity)
            for (group in StaticResources.Entity.getGroups(this@AccountActivity))
            {
                if (group.owner == accountEmail)
                {
                    Toast.makeText(this@AccountActivity, R.string.err_hand_over_group, Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
            }

            val intent = Intent(this@AccountActivity, DeleteAccountActivity::class.java)
            startActivity(intent)
        }
    }

    override fun enableActivity()
    {
        scrollView.isEnabled = true
        progressBar.visibility = View.GONE
    }

    override fun disableActivity()
    {
        scrollView.isEnabled = false
        progressBar.visibility = View.VISIBLE
    }
}
