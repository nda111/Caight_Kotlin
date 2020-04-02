package com.kotlin.caight.activity

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import com.kotlin.caight.R
import com.kotlin.caight.adapter.GroupSpinnerAdapter
import com.kotlin.caight.companion.Constants
import com.kotlin.caight.companion.Methods
import com.kotlin.caight.companion.Methods.Hex.toHexCode
import com.kotlin.caight.companion.StaticResources
import com.kotlin.caight.data.CatGroup
import com.kotlin.caight.data.Date
import com.kotlin.caight.data.Gender
import com.kotlin.caight.websocket.RequestId
import com.kotlin.caight.websocket.ResponseId
import com.kotlin.caight.websocket.WebSocketRequest
import kotlinx.android.synthetic.main.activity_add_cat.*
import java.util.*

class AddCatActivity : AppCompatActivity(), IMutableActivity, ColorPickerDialogListener
{
    private var selectedGroup: CatGroup? = null
    private var selectedColor: Int = 0
    private var selectedBirthday: Long = 0
    private var selectedSpecies: Int = -1

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_cat)
        supportActionBar!!.hide()

        /*
         * Initialize GUI Components
         */
        // pwEditText
        pwEditText.addTextChangedListener(object : TextWatcher
        {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int)
            {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int)
            {
            }

            override fun afterTextChanged(s: Editable)
            {
                pwValidCheckBox.isChecked = s.isNotEmpty()
            }
        })

        // groupSpinner
        val adapter = GroupSpinnerAdapter(this, R.id.groupTextView, StaticResources.Entity.getGroups(this@AddCatActivity))
        groupSpinner.adapter = adapter
        groupSpinner.onItemSelectedListener = object : OnItemSelectedListener
        {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long)
            {
                selectedGroup = StaticResources.Entity.getGroups(this@AddCatActivity)[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?)
            {
            }
        }

        // colorViewer, rgbTextView
        val colorPickerTrigger = object : View.OnClickListener
        {
            private val picker = ColorPickerDialog
                .newBuilder()
                .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                .setColor(Color.WHITE)
                .create()

            override fun onClick(v: View) = picker.show(supportFragmentManager, null)
        }
        colorViewer.setOnClickListener(colorPickerTrigger)
        rgbTextView.setOnClickListener(colorPickerTrigger)

        // nameEditText
        val names = StaticResources.StringArrays.getNameExamples(this@AddCatActivity)
        nameEditText.hint = names[(Math.random() * names.size).toInt()]
        nameEditText.addTextChangedListener(object : TextWatcher
        {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int)
            {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int)
            {
            }

            override fun afterTextChanged(s: Editable)
            {
                nameValidCheckBox.isChecked = s.isNotEmpty()
            }
        })

        // birthdayEditText
        val today: Date = Date.getToday()
        birthdayEditText.hint = Methods.DateFormatter.format(today)
        selectedBirthday = today.toLong()
        birthdayEditText.setOnClickListener {
            val datePicker = OnDateSetListener { _, year, month, dayOfMonth ->
                val cal = Date(year, month.toShort(), dayOfMonth.toShort())
                selectedBirthday = cal.toLong()
                val nowString = Methods.DateFormatter.format(cal)
                birthdayEditText.setText(nowString)
            }

            val now = Date.getToday()
            DatePickerDialog(
                this, datePicker,
                now.year,
                now.month.toInt(),
                now.day.toInt()
            ).show()
        }

        // speciesSpinner
        val speciesAdapter: ArrayAdapter<*> = ArrayAdapter.createFromResource(this, R.array.species, android.R.layout.simple_spinner_item)
        speciesAdapter.sort { o1, o2 -> (o1 as String).compareTo((o2 as String)) }
        speciesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        speciesSpinner.adapter = speciesAdapter
        speciesSpinner.onItemSelectedListener = object : OnItemSelectedListener
        {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long)
            {
                val species = StaticResources.StringArrays.getSpecies(this@AddCatActivity)
                val item = speciesAdapter.getItem(position) as String
                for (idx in species.indices)
                {
                    if (item == species[idx])
                    {
                        println(idx)
                        selectedSpecies = idx
                        return
                    }
                }
                selectedSpecies = -1
            }

            override fun onNothingSelected(parent: AdapterView<*>?)
            {
            }
        }

        // weightEditText
        weightEditText.addTextChangedListener(object : TextWatcher
        {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int)
            {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int)
            {
            }

            override fun afterTextChanged(s: Editable)
            {
                weightValidCheckBox.isChecked = s.isNotEmpty()
            }
        })


        // registerButton
        registerButton.setOnClickListener {
            if (pwValidCheckBox.isChecked && nameValidCheckBox.isChecked && weightValidCheckBox.isChecked)
            {
                disableActivity()

                WebSocketRequest(Constants.ServerAddress, object : WebSocketRequest.RequestAdapter
                {
                    var response: ResponseId? = null

                    override fun onRequest(conn: WebSocketRequest)
                    {
                        val builder = StringBuilder()
                        builder.append(selectedGroup!!.id)
                        builder.append(Constants.NullChar)
                        builder.append(pwEditText.text.toString())
                        builder.append(Constants.NullChar)
                        builder.append(selectedColor)
                        builder.append(Constants.NullChar)
                        builder.append(nameEditText.text.toString())
                        builder.append(Constants.NullChar)
                        builder.append(selectedBirthday)
                        builder.append(Constants.NullChar)
                        builder.append(Gender.evaluate(!genderToggleButton.isChecked, isNeuteredCheckBox.isChecked)!!.value)
                        builder.append(Constants.NullChar)
                        builder.append(selectedSpecies)
                        builder.append(Constants.NullChar)
                        builder.append(today.toLong())
                        builder.append(Constants.NullChar)
                        builder.append(weightEditText.text.toString().toFloat())
                        builder.append(Constants.NullChar)
                        builder.append((findViewById<View>(R.id.keywordEditText) as EditText).text.toString())

                        conn.send(RequestId.NEW_CAT.id)
                        conn.sendAuth(this@AddCatActivity)
                        conn.send(builder.toString())
                    }

                    override fun onResponse(conn: WebSocketRequest, message: WebSocketRequest.Message)
                    {
                        response = ResponseId.fromId(Methods.NumericBinary.byteArrayToInt(message.binaryMessage))
                        conn.close()
                    }

                    override fun onClosed()
                    {
                        runOnUiThread() {
                            when (response)
                            {
                                ResponseId.ADD_ENTITY_OK     ->
                                {
                                    StaticResources.Entity.setUpdateList(this@AddCatActivity, true)
                                    finish()
                                }
                                ResponseId.ADD_ENTITY_NO     ->
                                {
                                    Toast.makeText(application, R.string.err_other_device_logged_in, Toast.LENGTH_SHORT).show()

                                    val intent = Intent(applicationContext, RootActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                }
                                ResponseId.ADD_ENTITY_NOT_PW ->
                                {
                                    Toast.makeText(application, R.string.err_wrong_pw, Toast.LENGTH_SHORT).show()
                                }
                                ResponseId.ADD_ENTITY_ERROR  ->
                                {
                                    Toast.makeText(application, R.string.err_occurred, Toast.LENGTH_SHORT).show()
                                }
                            }

                            enableActivity()
                        }
                    }
                }).connect()
            }
        }
    }

    override fun onColorSelected(dialogId: Int, color: Int)
    {
        selectedColor = color

        rgbTextView.text = Color.valueOf(color).toHexCode()
        colorViewer.setBackgroundColor(color)
    }

    override fun onDialogDismissed(dialogId: Int)
    {
    }

    override fun disableActivity()
    {
        wrapperLayout.isEnabled = false
        progressBar.visibility = View.VISIBLE
    }

    override fun enableActivity()
    {
        wrapperLayout.isEnabled = true
        progressBar.visibility = View.GONE
    }
}
