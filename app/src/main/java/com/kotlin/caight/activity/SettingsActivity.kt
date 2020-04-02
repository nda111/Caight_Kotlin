package com.kotlin.caight.activity

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.kotlin.caight.R
import com.kotlin.caight.adapter.IconItemAdapter
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val resources = resources
        val theme = theme

        /*
         * Action Bar
         */
        val actionBar = supportActionBar
        actionBar!!.setTitle(R.string.title_settings)
        actionBar.setBackgroundDrawable(ColorDrawable(ResourcesCompat.getColor(resources, R.color.colorPrimaryDark, theme)))

        /*
         * Initialize GUI Components
         */
        // settingListView
        settingListView.divider = null
        settingListView.adapter = IconItemAdapter()
            .add(this, R.drawable.ic_account_circle, R.string.menu_account, R.string.desc_account)
            .add(this, R.drawable.ic_alert_circle, R.string.menu_notifications, R.string.desc_notifications)
        settingListView.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            if (position == 0)
            {
                val intent = Intent(this@SettingsActivity, AccountActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
