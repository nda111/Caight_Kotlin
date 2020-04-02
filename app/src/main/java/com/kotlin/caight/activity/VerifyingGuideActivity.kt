package com.kotlin.caight.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kotlin.caight.R
import kotlinx.android.synthetic.main.activity_verifying_guide.*

class VerifyingGuideActivity : AppCompatActivity()
{

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verifying_guide)
        supportActionBar!!.hide()

        /*
         * Initialize GUI Components
         */
        // goLoginButton
        goLoginButton.setOnClickListener { onBackPressed() }
    }

    override fun onBackPressed()
    {
        super.onBackPressed()

        val intent = Intent(applicationContext, LoginEntryActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
