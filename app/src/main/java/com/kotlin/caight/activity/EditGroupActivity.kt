package com.kotlin.caight.activity

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.kotlin.caight.R
import com.kotlin.caight.adapter.MemberArrayAdapter
import com.kotlin.caight.companion.Constants
import com.kotlin.caight.companion.Methods
import com.kotlin.caight.companion.StaticResources
import com.kotlin.caight.data.CatGroup
import com.kotlin.caight.dialog.DeletionConfirmDialog
import com.kotlin.caight.websocket.RequestId
import com.kotlin.caight.websocket.ResponseId
import com.kotlin.caight.websocket.WebSocketRequest
import kotlinx.android.synthetic.main.activity_edit_group.*
import org.json.JSONObject
import java.util.*

class EditGroupActivity : AppCompatActivity(), IMutableActivity
{
    companion object
    {
        private const val JsonKeyId = "id"
        private const val JsonKeyName = "name"
        private const val JsonKeyPassword = "password"
        private const val JsonKeyLocked = "locked"
        private const val JsonKeyManager = "manager"
    }

    private var group: CatGroup? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_group)

        /*
         * Intent
         */
        val groupId = intent.getIntExtra(Constants.IntentKey.GroupId, -1)
        for (group in StaticResources.Entity.getGroups(this@EditGroupActivity))
        {
            if (groupId == group.id)
            {
                this.group = group
                break
            }
        }
        if (group == null)
        {
            StaticResources.Entity.setUpdateList(this@EditGroupActivity, true)
            Toast.makeText(applicationContext, R.string.err_occurred, Toast.LENGTH_SHORT).show()

            finish()
        }

        /*
         * Action Bar
         */
        val actionBar = supportActionBar!!
        actionBar.setTitle(group!!.name)
        actionBar.setBackgroundDrawable(ColorDrawable(ResourcesCompat.getColor(resources, R.color.colorPrimaryDark, theme)))

        /*
         * Initialize GUI Components
         */
        // nameEditText
        nameEditText.setText(group!!.name)

        // lockSwitch
        lockSwitch.isChecked = group!!.isLocked

        // managerSpinner
        disableActivity()
        val memberList = ArrayList<MemberArrayAdapter.Item>()
        WebSocketRequest(Constants.ServerAddress, object : WebSocketRequest.RequestAdapter
        {
            var response: ResponseId = ResponseId.UNKNOWN
            var managerPosition = -1

            override fun onRequest(conn: WebSocketRequest)
            {
                conn.send(RequestId.DOWNLOAD_MEMBER.id)
                conn.send(group!!.id)
            }

            override fun onResponse(conn: WebSocketRequest, message: WebSocketRequest.Message)
            {
                if (!message.isBinary)
                {
                    val args: List<String> = message.textMessage.split(Constants.NullChar)
                    if (args[1] == StaticResources.Account.getEmail(this@EditGroupActivity))
                    {
                        managerPosition = memberList.size
                    }

                    memberList.add(MemberArrayAdapter.Item(args[0], args[1]))
                }
                else
                {
                    response = ResponseId.fromId(Methods.NumericBinary.byteArrayToInt(message.binaryMessage))
                    when (response)
                    {
                        ResponseId.DOWNLOAD_MEMBER_ERROR ->
                        {
                            Toast.makeText(applicationContext, R.string.err_occurred, Toast.LENGTH_SHORT).show()
                        }
                    }
                    conn.close()
                }
            }

            override fun onClosed()
            {
                runOnUiThread {
                    if (response === ResponseId.END_OF_MEMBER)
                    {
                        managerSpinner.adapter = MemberArrayAdapter(this@EditGroupActivity, R.layout.item_group_spinner, memberList)
                        managerSpinner.setSelection(managerPosition)
                        enableActivity()
                    }
                    else if (response === ResponseId.DOWNLOAD_MEMBER_ERROR)
                    {
                        Toast.makeText(applicationContext, R.string.err_occurred, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }).connect()

        // deleteButton
        deleteButton.setOnClickListener {
            val dialog = DeletionConfirmDialog(R.string.word_delete, group!!.name, object : DeletionConfirmDialog.OnDeletionConfirmListener
            {
                override fun onConfirm()
                {
                    disableActivity()

                    WebSocketRequest(Constants.ServerAddress, object : WebSocketRequest.RequestAdapter
                    {
                        var response: ResponseId = ResponseId.UNKNOWN

                        override fun onRequest(conn: WebSocketRequest)
                        {
                            conn.send(RequestId.DROP_GROUP.id)
                            conn.sendAuth(this@EditGroupActivity)
                            conn.send(group!!.id)
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
                                    ResponseId.DROP_GROUP_OK            ->
                                    {
                                        Toast.makeText(applicationContext, R.string.msg_group_deleted, Toast.LENGTH_LONG).show()
                                        StaticResources.Entity.setUpdateList(this@EditGroupActivity, true)
                                        finish()
                                    }

                                    ResponseId.DROP_GROUP_MEMBER_EXISTS ->
                                    {
                                        Toast.makeText(applicationContext, R.string.err_del_group, Toast.LENGTH_LONG).show()
                                    }

                                    ResponseId.DROP_GROUP_ERROR         ->
                                    {
                                        Toast.makeText(applicationContext, R.string.err_occurred, Toast.LENGTH_SHORT).show()
                                    }
                                }
                                enableActivity()
                            }
                        }
                    }).connect()
                }

                override fun onCancel()
                {
                }
            })
            dialog.show(supportFragmentManager, null)
        }

        // saveButton
        saveButton.setOnClickListener() {
            val json = JSONObject()
            var update = false

            val name = nameEditText.text.toString()
            val password = pwEditText.text.toString().trim()
            val locked = lockSwitch.isChecked
            val manager = memberList[managerSpinner.selectedItemPosition]

            if (name == group!!.name)
            {
                json.put(JsonKeyName, name)
                update = true
            }
            if (password.isEmpty())
            {
                json.put(JsonKeyPassword, password)
                update = true
            }
            if (locked == group!!.isLocked)
            {
                json.put(JsonKeyLocked, locked)
                update = true
            }
            if (manager.email == StaticResources.Account.getEmail(this@EditGroupActivity))
            {
                json.put(JsonKeyManager, manager)
                update = true
            }

            if (update)
            {
                disableActivity()

                WebSocketRequest(Constants.ServerAddress, object : WebSocketRequest.RequestAdapter
                {
                    var response: ResponseId = ResponseId.UNKNOWN

                    override fun onRequest(conn: WebSocketRequest)
                    {
                        conn.send(RequestId.UPDATE_GROUP.id)
                        conn.sendAuth(this@EditGroupActivity)
                        conn.send(json.toString())
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
                                ResponseId.UPDATE_GROUP_OK    ->
                                {
                                    StaticResources.Entity.setUpdateList(this@EditGroupActivity, true)
                                    finish()
                                }

                                ResponseId.UPDATE_GROUP_ERROR ->
                                {
                                    Toast.makeText(this@EditGroupActivity, R.string.err_occurred, Toast.LENGTH_SHORT).show()
                                }
                            }
                            enableActivity()
                        }
                    }
                }).connect()
            }
            else
            {
                finish()
            }
        }
    }

    override fun enableActivity()
    {
        nameEditText.isEnabled = true
        pwEditText.isEnabled = true
        lockSwitch.isEnabled = true
        managerSpinner.isEnabled = true
        deleteButton.isEnabled = true
        saveButton.isEnabled = true
        progressBar.visibility = View.GONE
    }

    override fun disableActivity()
    {
        nameEditText.isEnabled = false
        pwEditText.isEnabled = false
        lockSwitch.isEnabled = false
        managerSpinner.isEnabled = false
        deleteButton.isEnabled = false
        saveButton.isEnabled = false
        progressBar.visibility = View.VISIBLE
    }
}
