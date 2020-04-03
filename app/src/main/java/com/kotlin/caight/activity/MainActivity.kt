package com.kotlin.caight.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.kotlin.caight.R
import com.kotlin.caight.companion.Constants
import com.kotlin.caight.companion.Methods
import com.kotlin.caight.companion.StaticResources
import com.kotlin.caight.data.Cat
import com.kotlin.caight.data.CatGroup
import com.kotlin.caight.dialog.DeletionConfirmDialog
import com.kotlin.caight.view.CatGroupView
import com.kotlin.caight.view.CatView
import com.kotlin.caight.view.EntityViewBase
import com.kotlin.caight.websocket.RequestId
import com.kotlin.caight.websocket.ResponseId
import com.kotlin.caight.websocket.WebSocketRequest
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.scwang.smartrefresh.header.BezierCircleHeader
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class MainActivity : AppCompatActivity()
{

    private val onGroupClickListener: EntityViewBase.OnEntityListItemTouchListener = object : EntityViewBase.OnEntityListItemTouchListener
    {
        override fun onClick(sender: EntityViewBase)
        {
            val item = (sender as CatGroupView)

            if (item.isExpanded)
            {
                entityListView.collapse(item.mParentPosition)
            }
            else
            {
                entityListView.expand(item.mParentPosition)
            }
        }
    }
    private val onGroupDeleteListener: EntityViewBase.OnEntityListItemTouchListener = object : EntityViewBase.OnEntityListItemTouchListener
    {
        override fun onClick(sender: EntityViewBase)
        {
            val group: CatGroup = (sender as CatGroupView).group
            if (group.owner != StaticResources.Account.getEmail(this@MainActivity))
            {
                val groupId = group.id
                progressBar.visibility = View.VISIBLE

                WebSocketRequest(Constants.ServerAddress, object : WebSocketRequest.RequestAdapter
                {
                    var response: ResponseId = ResponseId.UNKNOWN

                    override fun onRequest(conn: WebSocketRequest)
                    {
                        conn.send(RequestId.WITHDRAW_GROUP.id)
                        conn.sendAuth(this@MainActivity)
                        conn.send(groupId)
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
                                ResponseId.WITHDRAW_GROUP_OK    -> downloadEntities(false)
                                ResponseId.WITHDRAW_GROUP_ERROR -> Toast.makeText(this@MainActivity, R.string.err_occurred, Toast.LENGTH_SHORT).show()
                            }

                            progressBar.visibility = View.GONE
                        }
                    }
                }).connect()
            }
            else
            {
                Toast.makeText(this@MainActivity, R.string.msg_manager_cannot, Toast.LENGTH_SHORT).show()
            }
        }
    }
    private val onGroupEditListener: EntityViewBase.OnEntityListItemTouchListener = object : EntityViewBase.OnEntityListItemTouchListener
    {
        override fun onClick(sender: EntityViewBase)
        {
            val group: CatGroup = (sender as CatGroupView).group
            if (group.owner == StaticResources.Account.getEmail(this@MainActivity))
            {
                val intent = Intent(this@MainActivity, EditGroupActivity::class.java)
                intent.putExtra(Constants.IntentKey.GroupId, group.id)
                startActivity(intent)
            }
            else
            {
                Toast.makeText(this@MainActivity, R.string.msg_only_manager, Toast.LENGTH_SHORT).show()
            }
        }
    }
    private val onGroupShowQRListener: EntityViewBase.OnEntityListItemTouchListener = object : EntityViewBase.OnEntityListItemTouchListener
    {
        override fun onClick(sender: EntityViewBase)
        {
            TODO("Not yet implemented")
        }
    }
    private val onCatClickListener: EntityViewBase.OnEntityListItemTouchListener = object : EntityViewBase.OnEntityListItemTouchListener
    {
        override fun onClick(sender: EntityViewBase)
        {
            val cat: Cat = (sender as CatView).cat
            val group: CatGroup = sender.group

            val intent = Intent(this@MainActivity, CatDetailActivity::class.java)
            intent.putExtra(Constants.IntentKey.GroupId, group.id)
            intent.putExtra(Constants.IntentKey.CatId, cat.id)
            startActivity(intent)
        }
    }
    private val onCatDeleteListener: EntityViewBase.OnEntityListItemTouchListener = object : EntityViewBase.OnEntityListItemTouchListener
    {
        override fun onClick(sender: EntityViewBase)
        {
            val cat: Cat = (sender as CatView).cat
            val group: CatGroup = sender.group

            if (group.owner == StaticResources.Account.getEmail(this@MainActivity))
            {
                DeletionConfirmDialog(R.string.word_delete, cat.name, object : DeletionConfirmDialog.OnDeletionConfirmListener
                {
                    override fun onConfirm()
                    {
                        progressBar.visibility = View.VISIBLE

                        WebSocketRequest(Constants.ServerAddress, object : WebSocketRequest.RequestAdapter
                        {
                            var response: ResponseId = ResponseId.UNKNOWN

                            override fun onRequest(conn: WebSocketRequest)
                            {
                                conn.send(RequestId.DROP_CAT.id)
                                conn.sendAuth(this@MainActivity)
                                conn.send(cat.id)
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
                                        ResponseId.DROP_CAT_OK    ->
                                        {
                                            downloadEntities(false)
                                        }
                                        ResponseId.DROP_CAT_ERROR ->
                                        {
                                            Toast.makeText(this@MainActivity, R.string.err_occurred, Toast.LENGTH_SHORT).show()
                                        }
                                    }

                                    progressBar.visibility = View.GONE
                                }
                            }
                        }).connect()
                    }

                    override fun onCancel()
                    {
                    }
                }).show(supportFragmentManager, null)
            }
            else
            {
                Toast.makeText(this@MainActivity, R.string.msg_only_manager, Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private val onCatEditListener: EntityViewBase.OnEntityListItemTouchListener = object : EntityViewBase.OnEntityListItemTouchListener
    {
        override fun onClick(sender: EntityViewBase)
        {
            val view = sender as CatView
            val cat = view.cat
            val group = view.group

            val intent = Intent(this@MainActivity, EditCatActivity::class.java)
            intent.putExtra(Constants.IntentKey.GroupId, group.id)
            intent.putExtra(Constants.IntentKey.CatId, cat.id)

            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar!!.hide()

        /*
         * String resources
         */
        StaticResources.StringArrays.initializeIfNotExists(this)

        /*
         * Initialize GUI Components
         */
        // refreshLayout
        refreshLayout.setOnRefreshListener { downloadEntities(true) }
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, layout ->
            layout.setPrimaryColorsId(R.color.colorPrimary, R.color.knownWhite)
            BezierCircleHeader(context)
        }

        // menuDialNewGroupItem
        val menuDialNewGroupItem: SpeedDialActionItem = SpeedDialActionItem.Builder(R.id.sdItemAddGroup, R.drawable.ic_new_group)
            .setFabBackgroundColor(android.graphics.Color.WHITE)
            .setFabImageTintColor(resources.getColor(R.color.colorPrimary, theme))
            .setLabel(R.string.act_add_group)
            .setLabelClickable(false)
            .create()

        // menuDialNewCatItem
        val menuDialNewCatItem: SpeedDialActionItem = SpeedDialActionItem.Builder(R.id.sdItemAddCat, R.drawable.ic_new_cat)
            .setFabBackgroundColor(resources.getColor(R.color.colorPrimary, theme))
            .setLabel(R.string.act_add_cat)
            .setLabelClickable(false)
            .create()

        // menuDialAccountItem
        val menuDialAccountItem: SpeedDialActionItem = SpeedDialActionItem.Builder(R.id.sdItemAccount, R.drawable.ic_setting)
            .setFabBackgroundColor(android.graphics.Color.WHITE)
            .setFabImageTintColor(resources.getColor(R.color.colorPrimary, theme))
            .setLabel(R.string.act_settings)
            .setLabelClickable(false)
            .create()

        // menuDial
        menuDial.addActionItem(menuDialNewGroupItem)
        menuDial.addActionItem(menuDialNewCatItem)
        menuDial.addActionItem(menuDialAccountItem)
        menuDial.setOnActionSelectedListener { actionItem ->
            when (actionItem.id)
            {
                R.id.sdItemAddGroup ->
                {
                    val intent = Intent(this@MainActivity, AddGroupActivity::class.java)
                    startActivity(intent)
                }
                R.id.sdItemAddCat   ->
                {
                    if (StaticResources.Entity.getGroups(this@MainActivity).size > 0)
                    {
                        val intent = Intent(this@MainActivity, AddCatActivity::class.java)
                        startActivity(intent)
                    }
                    else
                    {
                        Toast.makeText(this@MainActivity, R.string.err_no_group, Toast.LENGTH_SHORT).show()
                    }
                }
                R.id.sdItemAccount  ->
                {
                    val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                    startActivity(intent)
                }
                else                ->
                {
                }
            }
            false
        }

        downloadEntities(false)
    }

    override fun onResume()
    {
        super.onResume()
        if (StaticResources.Entity.getUpdateList(this))
        {
            StaticResources.Entity.setUpdateList(this, false)
            downloadEntities(false)
        }
    }

    override fun onDestroy()
    {
        super.onDestroy()
        println("MAIN_ACTIVITY_ON_DESTROY")
        // StaticResources.Account.clear(this)
        // StaticResources.Entity.clear(this)
    }

    private fun downloadEntities(isRefresh: Boolean)
    {
        rootLayout.isEnabled = false

        if (!isRefresh)
        {
            progressBar.visibility = View.VISIBLE
        }

        WebSocketRequest(Constants.ServerAddress, object : WebSocketRequest.RequestAdapter
        {
            private var response: ResponseId = ResponseId.UNKNOWN
            private var group: CatGroup? = null
            private var cats: ArrayList<Cat>? = null
            private val groups: ArrayList<CatGroup> = ArrayList<CatGroup>()
            private val entries: HashMap<CatGroup, List<Cat>> = HashMap<CatGroup, List<Cat>>()

            override fun onRequest(conn: WebSocketRequest)
            {
                conn.send(RequestId.DOWNLOAD_ENTITY)
                conn.sendAuth(this@MainActivity)
            }

            override fun onResponse(conn: WebSocketRequest, message: WebSocketRequest.Message)
            {
                if (message.isBinary)
                {
                    response = ResponseId.fromId(Methods.NumericBinary.byteArrayToInt(message.binaryMessage))
                    if (response === ResponseId.END_OF_ENTITY)
                    {
                        if (group != null)
                        {
                            groups.add(group!!)
                            entries[group!!] = cats!!
                        }
                        conn.close()
                    }
                }
                else
                {
                    val data: String = message.textMessage
                    when (response)
                    {
                        ResponseId.ENTITY_GROUP ->
                        {
                            if (group != null)
                            {
                                groups.add(group!!)
                                entries[group!!] = cats!!
                            }
                            try
                            {
                                val json = JSONObject(data)
                                group = CatGroup.parseJson(json)
                                cats = ArrayList<Cat>()
                            }
                            catch (e: JSONException)
                            {
                                e.printStackTrace()
                            }
                        }
                        ResponseId.ENTITY_CAT   ->
                        {
                            try
                            {
                                val json = JSONObject(data)
                                val cat: Cat = Cat.parseJson(json)
                                cats!!.add(cat)
                            }
                            catch (e: JSONException)
                            {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }

            override fun onClosed()
            {
                StaticResources.Entity.setEntries(this@MainActivity, groups, entries)
                runOnUiThread {
                    val context: Context = this@MainActivity
                    entityListView.removeAllViews()
                    for (group in groups)
                    {
                        val groupView = CatGroupView(context, group)
                        groupView.onClickListener = onGroupClickListener
                        groupView.onDeleteListener = onGroupDeleteListener
                        groupView.onEditListener = onGroupEditListener
                        groupView.onShowQrListener = onGroupShowQRListener
                        entityListView.addView(groupView)
                        val cats: ArrayList<Cat>? = entries[group] as ArrayList<Cat>?
                        for (cat in cats!!)
                        {
                            val catView = CatView(context, cat, group)
                            catView.onClickListener = onCatClickListener
                            catView.onDeleteListener = onCatDeleteListener
                            catView.onEditListener = onCatEditListener
                            entityListView.addView(catView)
                        }
                    }
                }
                runOnUiThread {
                    rootLayout.isEnabled = true
                    if (isRefresh)
                    {
                        refreshLayout.finishRefresh()
                    }
                    else
                    {
                        progressBar.visibility = View.GONE
                    }
                    entityListView.invalidate()
                }
            }
        }).connect()
    }
}
