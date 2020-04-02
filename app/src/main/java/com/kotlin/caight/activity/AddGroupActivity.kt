package com.kotlin.caight.activity

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kotlin.caight.R
import com.kotlin.caight.companion.Constants
import com.kotlin.caight.companion.Methods
import com.kotlin.caight.companion.StaticResources
import com.kotlin.caight.view.AddGroupFormView
import com.kotlin.caight.view.AddGroupHeaderView
import com.kotlin.caight.websocket.RequestId
import com.kotlin.caight.websocket.ResponseId
import com.kotlin.caight.websocket.WebSocketRequest
import kotlinx.android.synthetic.main.activity_add_group.*
import java.lang.String

class AddGroupActivity : AppCompatActivity(), IMutableActivity
{
    private val createHeader = AddGroupHeaderView(this, R.drawable.ic_account_circle, R.string.menu_create_group, R.string.desc_create_group)
    private val createGroupView = AddGroupFormView(this, R.string.attr_name, InputType.TYPE_TEXT_VARIATION_PERSON_NAME, R.string.act_create_group)
    private val joinHeader = AddGroupHeaderView(this, R.drawable.ic_account_circle, R.string.menu_join_group, R.string.desc_join_group)
    private val joinGroupView = AddGroupFormView(this, R.string.attr_id, InputType.TYPE_NUMBER_VARIATION_NORMAL, R.string.act_join_group)

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_group)
        supportActionBar!!.hide()

        /*
         * Initialize GUI Components
         */
        // listView
        listView.addView(createHeader)
        listView.addView(createGroupView)
        listView.addView(joinHeader)
        listView.addView(joinGroupView)

        // createGroupView
        createGroupView.onClickListener = View.OnClickListener { _ ->
            if (createGroupView.isValid)
            {
                createGroup(createGroupView.result)
            }
            else
            {
                Toast.makeText(this@AddGroupActivity, R.string.msg_fill_form, Toast.LENGTH_SHORT).show()
            }
        }

        // joinGroupView
        joinGroupView.onClickListener = View.OnClickListener { _ ->
            if (joinGroupView.isValid)
            {
                val addArgument = joinGroupView.result
                for (group in StaticResources.Entity.getGroups(this@AddGroupActivity))
                {
                    if (addArgument[0] == String.valueOf(group.id))
                    {
                        Toast.makeText(this@AddGroupActivity, R.string.err_join_joined, Toast.LENGTH_SHORT).show()
                        return@OnClickListener
                    }
                }

                joinGroup(addArgument)
            }
            else
            {
                Toast.makeText(this@AddGroupActivity, R.string.msg_fill_form, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createGroup(args: Array<kotlin.String>)
    {
        disableActivity()

        WebSocketRequest(Constants.ServerAddress, object : WebSocketRequest.RequestAdapter
        {
            var response: ResponseId = ResponseId.UNKNOWN

            override fun onRequest(conn: WebSocketRequest)
            {
                conn.send(RequestId.NEW_GROUP)
                conn.sendAuth(this@AddGroupActivity)
                conn.send(args[0] + Constants.NullChar + args[1])
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
                        ResponseId.ADD_ENTITY_OK    ->
                        {
                            StaticResources.Entity.setUpdateList(this@AddGroupActivity, true)
                        }

                        ResponseId.ADD_ENTITY_NO    ->
                        {
                            Toast.makeText(application, R.string.err_other_device_logged_in, Toast.LENGTH_SHORT).show()
                            val intent = Intent(applicationContext, RootActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }

                        ResponseId.ADD_ENTITY_ERROR ->
                        {
                            Toast.makeText(application, R.string.err_occurred, Toast.LENGTH_SHORT).show()
                        }
                    }

                    finish()
                }
            }
        }).connect()
    }

    private fun joinGroup(args: Array<kotlin.String>)
    {
        disableActivity()

        WebSocketRequest(Constants.ServerAddress, object : WebSocketRequest.RequestAdapter
        {
            var response: ResponseId = ResponseId.UNKNOWN

            override fun onRequest(conn: WebSocketRequest)
            {
                conn.send(Methods.NumericBinary.intToByteArray(RequestId.JOIN_GROUP.id))
                conn.sendAuth(this@AddGroupActivity)
                conn.send(args[0] + Constants.NullChar + args[1])
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
                    ResponseId.JOIN_GROUP_OK             ->
                    {
                        StaticResources.Entity.setUpdateList(this@AddGroupActivity, true)
                        runOnUiThread { finish() }
                    }

                    ResponseId.JOIN_GROUP_NOT_EXISTS     ->
                    {
                        runOnUiThread { Toast.makeText(this@AddGroupActivity, R.string.err_join_not_exists, Toast.LENGTH_LONG).show() }
                    }

                    ResponseId.JOIN_GROUP_REJECTED       ->
                    {
                        runOnUiThread { Toast.makeText(this@AddGroupActivity, R.string.err_join_rejected, Toast.LENGTH_LONG).show() }
                    }

                    ResponseId.JOIN_GROUP_WRONG_PASSWORD ->
                    {
                        runOnUiThread { Toast.makeText(this@AddGroupActivity, R.string.err_join_wrong_password, Toast.LENGTH_LONG).show() }
                    }

                    ResponseId.JOIN_GROUP_ERROR          ->
                    {
                        runOnUiThread { Toast.makeText(this@AddGroupActivity, R.string.err_occurred, Toast.LENGTH_SHORT).show() }
                    }
                }
                runOnUiThread { enableActivity() }
            }
        }).connect()
    }

    override fun enableActivity()
    {
        listView.isEnabled = true
        progressBar.visibility = View.GONE
    }

    override fun disableActivity()
    {
        listView.isEnabled = false
        progressBar.visibility = View.VISIBLE
    }
}
