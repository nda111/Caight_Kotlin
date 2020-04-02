package com.kotlin.caight.activity

import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kotlin.caight.R
import com.kotlin.caight.companion.Constants
import com.kotlin.caight.companion.Constants.Regex.PasswordConstraints
import com.kotlin.caight.companion.Methods
import com.kotlin.caight.websocket.RequestId
import com.kotlin.caight.websocket.ResponseId
import com.kotlin.caight.websocket.WebSocketRequest
import kotlinx.android.synthetic.main.activity_register.*
import java.util.regex.Matcher

class RegisterActivity : AppCompatActivity(), IMutableActivity
{
    private val shakeAnimation: Animation = AnimationUtils.loadAnimation(this, R.anim.shake_twice)
    private val registerOkAnimation = resources.getDrawable(R.drawable.ic_anim_register_ok, theme) as AnimatedVectorDrawable
    private val registerNoAnimation = resources.getDrawable(R.drawable.ic_anim_register_no, theme) as AnimatedVectorDrawable

    private val pwConstraintCheckBoxes = arrayOf(pwConstraintCheckBox1, pwConstraintCheckBox2, pwConstraintCheckBox3, pwConstraintCheckBox4, pwConstraintCheckBox5)

    private var passwordValid: Boolean = false
    private var nameValid: Boolean = false
    private var buttonValid: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        supportActionBar!!.hide()

        /*
         * Initialize GUI Components
         */
        // emailTextView
        emailTextView.text = intent.getStringExtra(Constants.IntentKey.Email)

        // pwEditText
        pwEditText.addTextChangedListener(object : TextWatcher
        {
            override fun afterTextChanged(s: Editable?) { }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
                val password = pwEditText.text.toString()
                val matcher: Matcher = Constants.Regex.StrongPassword.matcher(password)
                if (matcher.matches())
                {
                    for (i in pwConstraintCheckBoxes.indices)
                    {
                        pwConstraintCheckBoxes[i].isChecked = true
                    }
                    passwordValid = true
                }
                else
                {
                    for (i in PasswordConstraints.indices)
                    {
                        val checked: Boolean = PasswordConstraints[i].matcher(password).matches()
                        if (pwConstraintCheckBoxes[i].isChecked != checked)
                        {
                            pwConstraintCheckBoxes[i].isChecked = checked
                            if (!checked)
                            {
                                pwConstraintCheckBoxes[i].startAnimation(shakeAnimation)
                            }
                        }
                    }
                    passwordValid = false
                }

                setRegisterButtonEnabled()
            }
        })

        // nameEditText
        nameEditText.addTextChangedListener(object : TextWatcher
        {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) { }

            override fun afterTextChanged(s: Editable)
            {
                val name = nameEditText.text.toString()
                val length = name.length
                if (length < 2 || 15 < length)
                {
                    nameErrTextView.setText(R.string.name_constraint_number)
                    nameErrTextView.visibility = View.VISIBLE
                    nameErrTextView.startAnimation(shakeAnimation)
                    nameValid = false
                }
                else
                {
                    nameErrTextView.visibility = View.GONE
                    nameValid = true
                }

                setRegisterButtonEnabled()
            }
        })

        // registerButton
        registerButton.setOnTouchListener { _, _ ->
            if (passwordValid && nameValid)
            {
                disableActivity()

                WebSocketRequest(Constants.ServerAddress, object : WebSocketRequest.RequestAdapter
                {
                    private var response: ResponseId = ResponseId.UNKNOWN
                    override fun onRequest(conn: WebSocketRequest)
                    {
                        val builder = StringBuilder()
                        builder.append(emailTextView.text.toString())
                        builder.append(Constants.NullChar)
                        builder.append(pwEditText.text.toString())
                        builder.append(Constants.NullChar)
                        builder.append(nameEditText.text.toString())

                        conn.send(RequestId.REGISTER_EMAIL.id)
                        conn.send(builder.toString())
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
                            ResponseId.REGISTER_OK ->
                            {
                                val intent = Intent(this@RegisterActivity, VerifyingGuideActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            }
                            ResponseId.REGISTER_NO ->
                            {
                                runOnUiThread {
                                    Toast.makeText(applicationContext, R.string.err_occurred, Toast.LENGTH_LONG).show()
                                    this@RegisterActivity.finish()
                                }
                            }
                        }

                        runOnUiThread { enableActivity() }
                    }
                }).connect()
            }
            false
        }
    }

    private fun setRegisterButtonEnabled()
    {
        val valid = passwordValid && nameValid
        if (buttonValid != valid)
        {
            buttonValid = valid

            val anim = if (valid) registerOkAnimation else registerNoAnimation

            registerButton.setImageDrawable(anim)
            anim.start()
        }
    }

    override fun enableActivity()
    {
        pwEditText.isEnabled = true
        nameEditText.isEnabled = true
        registerButton.isEnabled = true
        progressBar.visibility = View.GONE
    }

    override fun disableActivity()
    {
        pwEditText.isEnabled = false
        nameEditText.isEnabled = false
        registerButton.isEnabled = false
        progressBar.visibility = View.VISIBLE
    }
}
