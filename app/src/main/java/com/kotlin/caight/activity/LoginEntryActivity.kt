package com.kotlin.caight.activity

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.kotlin.caight.R
import com.kotlin.caight.companion.Constants
import com.kotlin.caight.companion.Methods
import com.kotlin.caight.websocket.RequestId
import com.kotlin.caight.websocket.ResponseId
import com.kotlin.caight.websocket.WebSocketRequest
import kotlinx.android.synthetic.main.activity_login_entry.*
import java.util.regex.Matcher

class LoginEntryActivity : AppCompatActivity(), IMutableActivity
{
    var shakeAnimation: Animation? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_entry)
        supportActionBar!!.hide()

        /*
         * Resources
         */
        shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake_twice)

        /*
         * Initialize GUI Components
         */
        // emailEditText
        emailEditText.setOnEditorActionListener(object : TextView.OnEditorActionListener
        {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean
            {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    val matcher: Matcher = Constants.Regex.Email.matcher(emailEditText.text)
                    if (matcher.matches())
                    {
                        nextActivity()
                    }
                    else
                    {
                        errorTextView.setText(R.string.err_not_email)
                        errorTextView.visibility = View.VISIBLE
                        errorTextView.startAnimation(shakeAnimation)
                    }
                    return true
                }
                return false
            }
        })

        // clearTextImageView
        clearTextImageView.setOnTouchListener { _, _ ->
            emailEditText.setText("")
            false
        }
    }

    fun nextActivity()
    {
        disableActivity()

        WebSocketRequest(Constants.ServerAddress, object : WebSocketRequest.RequestAdapter
        {
            val email = emailEditText.text.toString()
            var response = ResponseId.UNKNOWN

            override fun onRequest(conn: WebSocketRequest)
            {
                conn.send(RequestId.EVALUATE_EMAIL)
                conn.send(email)
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
                    ResponseId.UNKNOWN_EMAIL    ->
                    {
                        val intent = Intent(this@LoginEntryActivity, RegisterActivity::class.java)
                        intent.putExtra(Constants.IntentKey.Email, email)
                        startActivity(intent)
                    }
                    ResponseId.REGISTERED_EMAIL ->
                    {
                        runOnUiThread {
                            errorTextView.setText(R.string.err_cert_first)
                            errorTextView.visibility = View.VISIBLE
                            errorTextView.startAnimation(shakeAnimation)
                        }
                    }
                    ResponseId.VERIFIED_EMAIL   ->
                    {
                        val intent = Intent(this@LoginEntryActivity, LoginPasswordActivity::class.java)
                        intent.putExtra(Constants.IntentKey.Email, email)
                        intent.putExtra(Constants.IntentKey.AutoLogin, autoLoginCheckBox.isChecked)
                        startActivity(intent)
                    }
                }

                runOnUiThread { enableActivity() }
            }
        }).connect()
    }

    override fun enableActivity()
    {
        emailEditText.isEnabled = true
        autoLoginCheckBox.isEnabled = true

        progressBar.visibility = View.GONE
    }

    override fun disableActivity()
    {
        emailEditText.isEnabled = false
        autoLoginCheckBox.isEnabled = false

        progressBar.visibility = View.VISIBLE
    }
}
