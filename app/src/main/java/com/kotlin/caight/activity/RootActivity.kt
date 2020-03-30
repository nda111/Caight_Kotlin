package com.kotlin.caight.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kotlin.caight.R
import com.kotlin.caight.websocket.WebSocketRequest

class RootActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)
    }
}
