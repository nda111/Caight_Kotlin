package com.kotlin.caight.activity

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import com.kotlin.caight.R
import com.kotlin.caight.companion.Constants
import com.kotlin.caight.companion.Methods
import com.kotlin.caight.companion.Methods.Hex.toHexCode
import com.kotlin.caight.companion.StaticResources
import com.kotlin.caight.data.Cat
import com.kotlin.caight.data.CatGroup
import com.kotlin.caight.data.Date
import com.kotlin.caight.data.Gender
import com.kotlin.caight.websocket.RequestId
import com.kotlin.caight.websocket.ResponseId
import com.kotlin.caight.websocket.WebSocketRequest
import kotlinx.android.synthetic.main.activity_edit_cat.*
import org.json.JSONObject
import java.util.*

class EditCatActivity : AppCompatActivity(), IMutableActivity, ColorPickerDialogListener
{
    companion object
    {
        private const val JsonKeyId = "id"
        private const val JsonKeyColor = "color"
        private const val JsonKeyName = "name"
        private const val JsonKeyBirthday = "birthday"
        private const val JsonKeyGender = "gender"
        private const val JsonKeySpecies = "species"
        private const val JsonKeyAttributes = "attributes"
    }

    private var cat: Cat? = null

    private var selectedSpecies: Int = -1
    private var selectedColor: Int = -1
    private var selectedBirthday: Long = 0
    private var prevAttrs: Array<String> = arrayOf()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_cat)

        /*
         * Intent
         */
        val groupId = intent.getIntExtra(Constants.IntentKey.GroupId, -1)
        val catId = intent.getIntExtra(Constants.IntentKey.CatId, -1)
        if (groupId == -1 || catId == -1)
        {
            Toast.makeText(applicationContext, R.string.err_occurred, Toast.LENGTH_SHORT).show()
            finish()
        }
        for (group in StaticResources.Entity.getGroups(this@EditCatActivity))
        {
            if (group.id == groupId)
            {
                val entries: HashMap<CatGroup, List<Cat>> = StaticResources.Entity.getEntries(this@EditCatActivity)
                for (cat in entries[group]!!)
                {
                    if (cat.id == catId)
                    {
                        this.cat = cat
                        break
                    }
                }
                break
            }
        }
        if (cat == null)
        {
            Toast.makeText(applicationContext, R.string.err_occurred, Toast.LENGTH_SHORT).show()
            finish()
        }

        /*
         * Action Bar
         */
        val actionBar = supportActionBar!!
        actionBar.title = cat!!.name
        actionBar.setBackgroundDrawable(ColorDrawable(ResourcesCompat.getColor(resources, R.color.colorPrimaryDark, theme)))

        /*
         * Initialize GUI Components
         */
        // colorViewer, rgbTextView
        val colorPickerTrigger = object : View.OnClickListener
        {
            private val picker = ColorPickerDialog
                .newBuilder()
                .setDialogType(ColorPickerDialog.TYPE_PRESETS)
                .setColor(Color.WHITE)
                .create()

            override fun onClick(v: View?)
            {
                picker.show(supportFragmentManager, null)
            }
        }
        selectedColor = cat!!.colorInteger
        colorViewer.setBackgroundColor(selectedColor)
        rgbTextView.text = Color.valueOf(cat!!.colorInteger).toHexCode()
        colorViewer.setOnClickListener(colorPickerTrigger)
        rgbTextView.setOnClickListener(colorPickerTrigger)

        // nameEditText
        nameEditText.setText(cat!!.name)

        // birthdayEditText
        birthdayEditText.setText(Methods.DateFormatter.format(cat!!.birthday))
        selectedBirthday = cat!!.birthday.toLong()
        birthdayEditText.setOnClickListener {
            val datePicker = OnDateSetListener { _, year, month, dayOfMonth ->
                val date = com.kotlin.caight.data.Date(year, month.toShort(), dayOfMonth.toShort())
                selectedBirthday = date.toLong()

                val nowString = Methods.DateFormatter.format(date)
                birthdayEditText.setText(nowString)
            }

            val now: Date = Date.getToday()
            DatePickerDialog(
                this, datePicker,
                now.year,
                now.month.toInt(),
                now.day.toInt()
            ).show()
        }

        // maleRadioButton, femaleRadioButton, neuteredCheckBox
        if (cat!!.gender.isMale())
        {
            maleRadioButton.isChecked = true
        }
        else
        {
            femmaleRadioButton.isChecked = true
        }
        isNeuteredCheckBox.isChecked = cat!!.gender.isNeuteredOrSpayed()

        // speciesSpinner
        val species = StaticResources.StringArrays.getSpecies(this);
        val sortedSpecies = StaticResources.StringArrays.getSortedSpecies(this);
        val speciesAdapter = ArrayAdapter.createFromResource(this, R.array.species, android.R.layout.simple_spinner_item);
        speciesAdapter.sort { o1, o2 -> (o1 as String).compareTo(o2 as String) }
        speciesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        speciesSpinner.adapter = speciesAdapter;
        speciesSpinner.onItemSelectedListener = object : OnItemSelectedListener
        {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long)
            {
                val item = speciesAdapter.getItem(position) as String?
                for (idx in species.indices)
                {
                    if (item == species[idx])
                    {
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
        selectedSpecies = Arrays.binarySearch(sortedSpecies, species[cat!!.species])
        speciesSpinner.setSelection(selectedSpecies)

        // keywordEditText
        prevAttrs = cat!!.attributes.copyOf()
        keywordEditText.setText(prevAttrs.joinToString(" "))

        // saveButton
        saveButton.setOnClickListener {
            var anythingHasChanged = false
            val json = JSONObject()
            val color = selectedColor or -0x1000000
            val name = nameEditText.text.toString().trim()
            val birthday = selectedBirthday
            val gender = Gender.evaluate(maleRadioButton.isChecked, isNeuteredCheckBox.isChecked)
            val speciesId = selectedSpecies

            json.put(JsonKeyId, cat!!.id)
            if (cat!!.colorInteger != color)
            {
                anythingHasChanged = true
                json.put(JsonKeyColor, color)
            }
            if (cat!!.name != name)
            {
                anythingHasChanged = true
                json.put(JsonKeyName, name)
            }
            if (cat!!.birthday.toLong() != birthday)
            {
                anythingHasChanged = true
                json.put(JsonKeyBirthday, birthday)
            }
            if (cat!!.gender !== gender)
            {
                anythingHasChanged = true
                json.put(JsonKeyGender, gender!!.value)
            }
            if (cat!!.species != speciesId)
            {
                anythingHasChanged = true
                json.put(JsonKeySpecies, speciesId)
            }
            val attrString = keywordEditText.text.toString()
            val attrs = attrString.split(" ")
            if (attrs.size != prevAttrs.size)
            {
                anythingHasChanged = true
                json.put(JsonKeyAttributes, attrString)
            }
            else
            {
                for (i in attrs.indices)
                {
                    if (prevAttrs[i] != attrs[i])
                    {
                        anythingHasChanged = true
                        json.put(JsonKeyAttributes, attrString)
                        break
                    }
                }
            }

            if (anythingHasChanged)
            {
                disableActivity()

                WebSocketRequest(Constants.ServerAddress, object : WebSocketRequest.RequestAdapter
                {
                    var response: ResponseId = ResponseId.UNKNOWN
                    override fun onRequest(conn: WebSocketRequest)
                    {
                        conn.send(RequestId.EDIT_CAT.id)
                        conn.sendAuth(this@EditCatActivity)
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
                                ResponseId.EDIT_CAT_OK    ->
                                {
                                    StaticResources.Entity.setUpdateList(this@EditCatActivity, true)
                                    finish()
                                }

                                ResponseId.EDIT_CAT_ERROR ->
                                {
                                    Toast.makeText(this@EditCatActivity, R.string.err_occurred, Toast.LENGTH_LONG).show()
                                }
                            }

                            disableActivity()
                        }
                    }
                }).connect { Toast.makeText(this, R.string.msg_no_connection, Toast.LENGTH_LONG).show() }
            }
            else
            {
                finish()
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

    override fun enableActivity()
    {
        colorViewer.isEnabled = true
        rgbTextView.isEnabled = true
        nameEditText.isEnabled = true
        birthdayEditText.isEnabled = true
        maleRadioButton.isEnabled = true
        femmaleRadioButton.isEnabled = true
        isNeuteredCheckBox.isEnabled = true
        speciesSpinner.isEnabled = true
        keywordEditText.isEnabled = true
        saveButton.isEnabled = true
        progressBar.visibility = View.GONE
    }

    override fun disableActivity()
    {
        colorViewer.isEnabled = false
        rgbTextView.isEnabled = false
        nameEditText.isEnabled = false
        birthdayEditText.isEnabled = false
        maleRadioButton.isEnabled = false
        femmaleRadioButton.isEnabled = false
        isNeuteredCheckBox.isEnabled = false
        speciesSpinner.isEnabled = false
        keywordEditText.isEnabled = false
        saveButton.isEnabled = false
        progressBar.visibility = View.VISIBLE
    }
}
