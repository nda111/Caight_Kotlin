package com.kotlin.caight.activity

import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kotlin.caight.R
import com.kotlin.caight.companion.Constants
import com.kotlin.caight.companion.Methods
import com.kotlin.caight.companion.StaticResources
import com.kotlin.caight.delegate.ResponseListener
import com.kotlin.caight.websocket.RequestId
import com.kotlin.caight.websocket.ResponseId
import com.kotlin.caight.websocket.WebSocketRequest
import kotlinx.android.synthetic.main.activity_login_password.*
import java.util.*

class LoginPasswordActivity : AppCompatActivity(), IMutableActivity
{
    private var shakeAnimation: Animation? = null
    private var showPasswordAnimation: AnimatedVectorDrawable? = null
    private var hidePasswordAnimation: AnimatedVectorDrawable? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_password)
        supportActionBar!!.hide()

        /*
         * Resources
         */
        shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake_twice)
        showPasswordAnimation = resources.getDrawable(R.drawable.ic_anim_pw_show, theme) as AnimatedVectorDrawable
        hidePasswordAnimation = resources.getDrawable(R.drawable.ic_anim_pw_hide, theme) as AnimatedVectorDrawable

        /*
         * Initialize GUI Components
         */
        // pwEditText
        pwEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE)
            {
                val pw = pwEditText.text.toString()
                if (pwEditText.text.isEmpty())
                {
                    errTextView.setText(R.string.err_enter_pw)
                    errTextView.visibility = View.VISIBLE
                    errTextView.startAnimation(shakeAnimation)
                }
                else
                {
                    disableActivity()
                    errTextView.visibility = View.GONE

                    Methods.Request.login(this, intent.getStringExtra(Constants.IntentKey.Email)!!, pwEditText.text.toString(), object : ResponseListener
                    {
                        override fun finally(response: ResponseId, args: LinkedList<WebSocketRequest.Message>)
                        {
                            when (response)
                            {
                                ResponseId.SIGN_IN_OK       -> // queue: account_id, authentication_token, name, email
                                {
                                    StaticResources.Account.setId(this@LoginPasswordActivity, args.removeLast().binaryMessage)
                                    StaticResources.Account.setAuthenticationToken(this@LoginPasswordActivity, args.removeLast().textMessage)
                                    StaticResources.Account.setName(this@LoginPasswordActivity, args.removeLast().textMessage)
                                    val email = args.removeLast().textMessage
                                    StaticResources.Account.setEmail(this@LoginPasswordActivity, email)

                                    if (intent.getBooleanExtra(Constants.IntentKey.AutoLogin, false))
                                    {
                                        StaticResources.AutoLogin.set(this@LoginPasswordActivity, true, email, pwEditText.text.toString())
                                    }

                                    val intent = Intent(this@LoginPasswordActivity, MainActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                }

                                ResponseId.SIGN_IN_WRONG_PW ->
                                {
                                    runOnUiThread {
                                        errTextView.visibility = View.GONE
                                        forgotPwTextView.visibility = View.VISIBLE
                                    }
                                }

                                ResponseId.SIGN_IN_ERROR    ->
                                {
                                    runOnUiThread { Toast.makeText(this@LoginPasswordActivity, R.string.err_occurred, Toast.LENGTH_LONG).show() }
                                }
                            }

                            runOnUiThread { enableActivity() }
                        }
                    })
                }
            }
            false
        }

        // clearTextImageView
        clearTextImageView.setOnTouchListener { v, event ->
            pwEditText.text = null
            false
        }

        //revealImageButton
        revealImageButton.setOnClickListener(object : View.OnClickListener
        {
            private var showPassword = false

            override fun onClick(v: View?)
            {
                showPassword = !showPassword

                if (showPassword)
                {
                    pwEditText.inputType = InputType.TYPE_CLASS_TEXT
                    revealImageButton.setImageDrawable(showPasswordAnimation)
                    showPasswordAnimation!!.start()
                }
                else
                {
                    pwEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    revealImageButton.setImageDrawable(hidePasswordAnimation)
                    hidePasswordAnimation!!.start()
                }
            }
        })


        // forgotPwTextView
        forgotPwTextView.setOnClickListener {
            disableActivity()
            errTextView.visibility = View.GONE
            forgotPwTextView.text = ""

            WebSocketRequest(Constants.ServerAddress, object : WebSocketRequest.RequestAdapter
            {
                var response: ResponseId? = null
                override fun onRequest(conn: WebSocketRequest)
                {
                    conn.send(RequestId.REQUEST_RESET_PASSWORD_URI)
                    conn.send(intent.getStringExtra(Constants.IntentKey.Email)!!)
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
                        ResponseId.RESET_PASSWORD_URI_CREATED ->
                        {
                            runOnUiThread { Toast.makeText(applicationContext, R.string.msg_reset_mail_sent, Toast.LENGTH_LONG).show() }
                        }

                        ResponseId.RESET_PASSWORD_URI_ERROR   ->
                        {
                            runOnUiThread { Toast.makeText(applicationContext, R.string.err_occurred, Toast.LENGTH_SHORT).show() }
                        }
                    }

                    runOnUiThread { enableActivity() }
                }
            }).connect()
        }
    }

    override fun enableActivity()
    {
        pwEditText.isEnabled = true
        revealImageButton.isEnabled = true
        progressBar.visibility = View.GONE
    }

    override fun disableActivity()
    {
        pwEditText.isEnabled = false
        revealImageButton.isEnabled = false
        progressBar.visibility = View.VISIBLE
    }
}
