package com.kotlin.caight.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.kotlin.caight.R
import com.kotlin.caight.adapter.WeightListAdapter
import com.kotlin.caight.companion.Constants
import com.kotlin.caight.companion.Methods
import com.kotlin.caight.companion.StaticResources
import com.kotlin.caight.data.Cat
import com.kotlin.caight.data.CatGroup
import com.kotlin.caight.data.Date
import com.kotlin.caight.websocket.RequestId
import com.kotlin.caight.websocket.ResponseId
import com.kotlin.caight.websocket.WebSocketRequest
import kotlinx.android.synthetic.main.activity_cat_detail.*
import lecho.lib.hellocharts.model.Line
import lecho.lib.hellocharts.model.LineChartData
import lecho.lib.hellocharts.model.PointValue
import lecho.lib.hellocharts.model.Viewport
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.math.max

class CatDetailActivity : AppCompatActivity(), IMutableActivity
{
    private class SyncData(var type: Int, var date: Long, var newValue: Float = -1F, var oldValue: Float = -1F)
    {
        companion object
        {
            const val SyncTypeInsert = 0
            const val SyncTypeUpdate = 1
            const val SyncTypeDelete = 2
        }
    }

    companion object
    {
        private const val __JSON_KEY_UPSERT__ = "upsert"
        private const val __JSON_KEY_DELETE__ = "delete"
        private const val JsonKeyDate = "date"
        private const val JsonKeyWeight = "weight"
    }

    private var cat: Cat? = null

    private var nowFrom: Date = Date.zeroDate
    private var nowTo: Date = Date.zeroDate

    private var signatureColor = 0
    private var darkerColor = 0
    private var textColor = 0

    private var newDate: Date = Date.zeroDate

    private val syncs = HashMap<Long, SyncData>()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cat_detail)
        nowFrom = Date.getToday()
        if (nowFrom.month <= 3)
        {
            nowFrom.year = nowFrom.year - 1
            nowFrom.month = (12 + nowFrom.month - 3).toShort()
        }
        else
        {
            nowFrom.month = (nowFrom.month - 3).toShort()
        }
        nowTo = Date.getToday()

        /*
         * ActionBar
         */
        val actionBar = supportActionBar!!
        actionBar.hide()

        /*
         * Initialize GUi Components
         */
        // backImageView
        backImageView.setOnClickListener(View.OnClickListener { onBackPressed() })

        // dateFromTextView
        dateFromTextView.setOnClickListener(View.OnClickListener {
            val dialog = DatePickerDialog(this@CatDetailActivity)
            dialog.datePicker.init(
                nowFrom.year,
                nowFrom.month - 1,
                nowFrom.day.toInt(),
                null
            )
            dialog.setOnDateSetListener { _, year, month, dayOfMonth ->
                nowFrom = Date(year, (month + 1).toShort(), dayOfMonth.toShort())
                updateChart(nowFrom, nowTo)
            }
            dialog.show()
        })

        // dateToTextView
        dateToTextView.setOnClickListener {
            val dialog = DatePickerDialog(this@CatDetailActivity)
            dialog.datePicker.init(
                nowTo.year,
                nowTo.month - 1,
                nowTo.day.toInt(),
                null
            )
            dialog.setOnDateSetListener { _, year, month, dayOfMonth ->
                nowTo = Date(year, (month + 1).toShort(), dayOfMonth.toShort())
                updateChart(nowFrom, nowTo)
            }
            dialog.show()
        }

        // uploadImageView
        uploadImageView.setOnClickListener {
            if (syncs.size != 0)
            {
                disableActivity()

                WebSocketRequest(Constants.ServerAddress, object : WebSocketRequest.RequestAdapter
                {
                    var response: ResponseId = ResponseId.UNKNOWN

                    override fun onRequest(conn: WebSocketRequest)
                    {
                        val weightJson = JSONObject()
                        val upsert = JSONArray()
                        val delete = JSONArray()

                        for (data in syncs.values)
                        {
                            val dWeight = data.newValue.toString().toDouble()
                            when (data.type)
                            {
                                SyncData.SyncTypeInsert, SyncData.SyncTypeUpdate ->
                                {
                                    val obj = JSONObject()
                                    obj.put(JsonKeyDate, data.date)
                                    obj.put(JsonKeyWeight, dWeight)
                                    upsert.put(obj)
                                }

                                SyncData.SyncTypeDelete                          ->
                                {
                                    val obj = JSONObject()
                                    obj.put(JsonKeyDate, data.date)
                                    obj.put(JsonKeyWeight, dWeight)
                                    delete.put(obj)
                                }
                            }
                        }

                        weightJson.put(__JSON_KEY_UPSERT__, upsert)
                        weightJson.put(__JSON_KEY_DELETE__, delete)

                        conn.send(RequestId.UPLOAD_WEIGHT.id)
                        conn.sendAuth(this@CatDetailActivity)
                        conn.send(cat!!.id)
                        conn.send(weightJson.toString(0))
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
                                ResponseId.UPLOAD_WEIGHT_OK ->
                                {
                                    syncs.clear()
                                    Toast.makeText(this@CatDetailActivity, R.string.msg_upload_complete, Toast.LENGTH_SHORT).show()
                                }

                                ResponseId.UPLOAD_WEIGHT_ERROR ->
                                {
                                    Toast.makeText(this@CatDetailActivity, R.string.err_occurred, Toast.LENGTH_SHORT).show()
                                }
                            }

                            enableActivity()
                        }
                    }
                }).connect()
            }
        }

        // revertImageView
        revertImageView.setOnClickListener(View.OnClickListener
        {
            for (sync in syncs.values)
            {
                val date: Date = Date.fromBigInt(sync.date)

                when (sync.type)
                {
                    SyncData.SyncTypeInsert ->
                    {
                        cat!!.weights.remove(date)
                    }

                    SyncData.SyncTypeUpdate, SyncData.SyncTypeDelete ->
                    {
                        cat!!.weights[date] = sync.oldValue
                    }
                }
            }

            syncs.clear()
            updateChart(nowFrom, nowTo)
            writeTable()
        })

        // newDateTextView
        newDate = Date.getToday()
        newDateTextView.text = Methods.DateFormatter.format(Date.getToday())
        newDateTextView.setOnClickListener(View.OnClickListener {
            val dialog = DatePickerDialog(this@CatDetailActivity)
            dialog.datePicker.init(
                newDate.year,
                newDate.month - 1,
                newDate.day.toInt(),
                null
            )
            dialog.setOnDateSetListener { _, year, month, dayOfMonth ->
                newDate = Date(year, (month + 1).toShort(), dayOfMonth.toShort())
                newDateTextView.text = Methods.DateFormatter.format(newDate)
            }
            dialog.show()
        })

        // newWeightEditText
        newWeightEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == IME_ACTION_DONE)
            {
                val longDate: Long = newDate.toLong()
                val weight = (newWeightEditText.text.toString().toFloat() * 100).toInt() / 100.0f

                if (!cat!!.hasWeightOn(newDate))
                {
                    if (syncs.containsKey(longDate))
                    {
                        val sync = syncs[longDate]
                        when (sync!!.type)
                        {
                            SyncData.SyncTypeDelete                          ->
                            {
                                if (sync.oldValue == weight)
                                {
                                    syncs.remove(longDate)
                                }
                                else
                                {
                                    sync.type = SyncData.SyncTypeUpdate
                                    sync.newValue = weight
                                }
                            }
                        }
                    }
                    else
                    {
                        syncs[longDate] = SyncData(SyncData.SyncTypeInsert, longDate, weight, -1F)
                    }
                    cat!!.weights[newDate] = weight

                    newWeightEditText.setText(null)
                    if (nowFrom.toLong() <= longDate && longDate <= nowTo.toLong())
                    {
                        updateChart(nowFrom, nowTo)
                    }
                    writeTable()
                }
                else
                {
                    val dupConfirm = AlertDialog.Builder(this@CatDetailActivity)
                        .setTitle(R.string.title_overwriting)
                        .setMessage(R.string.warn_overwriting)
                        .setCancelable(true)
                        .setPositiveButton(R.string.act_overwrite) { _, _ ->
                            if (syncs.containsKey(longDate))
                            {
                                val sync = syncs[longDate]
                                when (sync!!.type)
                                {
                                    SyncData.SyncTypeInsert -> sync.newValue = weight
                                    SyncData.SyncTypeUpdate -> if (sync.oldValue == weight)
                                    {
                                        syncs.remove(longDate)
                                    }
                                    else
                                    {
                                        sync.newValue = weight
                                    }
                                    SyncData.SyncTypeDelete ->
                                    {
                                    }                                }
                            }
                            else
                            {
                                syncs[longDate] = SyncData(SyncData.SyncTypeUpdate, longDate, weight, if (cat!!.hasWeightOn(newDate))
                                {
                                    cat!!.weights[newDate]!!
                                }
                                else
                                {
                                    -1F
                                })
                            }

                            cat!!.weights[newDate] = weight
                            newWeightEditText.text = null
                            if (nowFrom.toLong() <= longDate && longDate <= nowTo.toLong())
                            {
                                updateChart(nowFrom, nowTo)
                            }
                            writeTable()
                        }.setNegativeButton(R.string.act_cancel, null)
                        .create()
                    dupConfirm.show()
                }
            }
            false
        }

        // weightTable
        weightList.layoutManager = LinearLayoutManager(this)

        /*
         * Intent
         */
        val intent = intent
        val groupId = intent.getIntExtra(Constants.IntentKey.GroupId, -1)
        val catId = intent.getIntExtra(Constants.IntentKey.CatId, -1)
        val groups: ArrayList<CatGroup> = StaticResources.Entity.getGroups(this@CatDetailActivity)
        var group: CatGroup? = null
        for (g in groups)
        {
            if (g.id == groupId)
            {
                group = g
                break
            }
        }

        val entries: HashMap<CatGroup, List<Cat>> = StaticResources.Entity.getEntries(this@CatDetailActivity)
        val cats: List<Cat> = entries[group]!!
        for (c in cats)
        {
            if (c.id == catId)
            {
                cat = c
                break
            }
        }
        applyCat(cat)
        updateChart(nowFrom, nowTo)
    }

    override fun onBackPressed()
    {
        StaticResources.Entity.setUpdateList(this, true)
        super.onBackPressed()
    }

    private fun applyCat(cat: Cat?)
    {
        //
        // Name
        //
        nameTextView.text = cat!!.name

        //
        // Color
        //
        val color: Color = Color.valueOf(cat.colorInteger)
        signatureColor = color.toArgb()
        val brightness = max(max(color.red(), color.green()), color.blue())
        var newBrightness = 255 * (brightness - 0.3f)
        if (newBrightness < 0)
        {
            newBrightness = 0f
        }
        val r = (color.red() * newBrightness).toInt()
        val g = (color.green() * newBrightness).toInt()
        val b = (color.blue() * newBrightness).toInt()
        textColor = if (brightness >= 90) Color.BLACK else Color.WHITE
        darkerColor = (-0x1000000
                or (r shl 16
                ) or (g shl 8
                ) or b)

        // status bar
        val window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = darkerColor
        // background
        rootLayout!!.setBackgroundColor(darkerColor)
        // name, name wrapper
        nameWrapperImageView!!.setColorFilter(signatureColor, PorterDuff.Mode.SRC_IN)
        nameTextView!!.setTextColor(textColor)
        // progress bar
        progressBar!!.indeterminateTintList = ColorStateList.valueOf(signatureColor)

        //
        // Attribute Chips
        //
        // gender chip
        val genderChip = createChip()
        genderChip.setText(if (cat.gender.isMale()) R.string.attr_gender_male else R.string.attr_gender_female)
        attrChipGroup!!.addView(genderChip)

        // neutered/spayed chip
        val neuteredChip = createChip()
        neuteredChip.setText(if (cat.gender.isMale()) R.string.word_neutered else R.string.word_spayed)
        attrChipGroup!!.addView(neuteredChip)

        // species chip
        val speciesChip = createChip()
        speciesChip.text = StaticResources.StringArrays.getSpecies(this)[cat.species]
        attrChipGroup!!.addView(speciesChip)

        // age chip
        val ageChip = createChip()
        val ageBuilder = StringBuilder()
        val age: IntArray = cat.age
        if (age[0] != 0)
        {
            ageBuilder.append(age[0])
            ageBuilder.append(' ')
            ageBuilder.append(resources.getString(R.string.unit_old_year))
        }
        else
        {
            ageBuilder.append(age[1])
            ageBuilder.append(' ')
            ageBuilder.append(resources.getString(R.string.unit_old_month))
        }
        ageChip.text = ageBuilder.toString()

        // attribute chips
        if (cat.attributes.isNotEmpty())
        {
            for (attr in cat.attributes)
            {
                val chip = createChip()
                chip.text = attr
                attrChipGroup!!.addView(chip)
            }
        }

        //
        // Weights
        //
        updateChart(nowFrom, nowTo)
        writeTable()
    }

    private fun createChip(): Chip
    {
        val chip = Chip(this)
        chip.chipBackgroundColor = ColorStateList.valueOf(signatureColor)
        chip.setTextColor(textColor)
        chip.textSize = 16f
        chip.isClickable = true
        return chip
    }

    private fun writeTable()
    {
        weightList!!.adapter = WeightListAdapter(this, cat!!.weights, object : WeightListAdapter.ItemEventListener()
        {
            @SuppressLint("SetTextI18n")
            override fun onClick(viewHolder: WeightListAdapter.ViewHolder)
            {
                newDate = viewHolder.date
                newWeightEditText.setText(viewHolder.weight.toString())
                newDateTextView.text = Methods.DateFormatter.format(newDate)
            }

            override fun onDelete(viewHolder: WeightListAdapter.ViewHolder)
            {
                val date: Long = viewHolder.date.toLong()
                val weight: Float = viewHolder.weight
                if (syncs.containsKey(date))
                {
                    val sync = syncs[date]
                    when (sync!!.type)
                    {
                        SyncData.SyncTypeInsert ->
                        {
                            syncs.remove(date)
                        }
                        SyncData.SyncTypeUpdate ->
                        {
                            sync.type = SyncData.SyncTypeDelete
                            sync.newValue = -1f
                        }
                        SyncData.SyncTypeDelete ->
                        {
                        }
                    }
                }
                else
                {
                    syncs[date] = SyncData(SyncData.SyncTypeDelete, date, weight)
                }
                // cat!!.weights.remove(date)
                cat!!.weights.remove(viewHolder.date)
                if (nowFrom.toLong() <= date && date <= nowTo.toLong())
                {
                    updateChart(nowFrom, nowTo)
                }
                writeTable()
            }
        })
    }

    private fun updateChart(from: Date, to: Date)
    {
        val weightsMap: TreeMap<Long, Float> = cat!!.getChronologicalWeightsInRange(from, to)
        val data = createLineChartData(
            weightsMap,
            signatureColor, darkerColor,
            from.toCalendar().timeInMillis, to.toCalendar().timeInMillis
        )
        weightChart!!.lineChartData = data
        weightChart!!.isViewportCalculationEnabled = false
        dateFromTextView.text = Methods.DateFormatter.format(from)
        dateToTextView.text = Methods.DateFormatter.format(to)
    }

    private fun createLineChartData(map: TreeMap<Long, Float>, color: Int, pointColor: Int, from: Long, to: Long): LineChartData
    {
        val min: Long = from
        val range: Float = (to - from + 1).toFloat()
        val data = LineChartData()
        val lines: MutableList<Line> = ArrayList(1)
        val points: MutableList<PointValue> = ArrayList(map.size)

        var max = Float.MIN_VALUE
        for ((key, value) in map)
        {
            points.add(PointValue((key.toLong() - min) / range, value))
            if (max < value)
            {
                max = value
            }
        }

        val line = Line(points)
            .setHasLines(true)
            .setHasPoints(true)
            .setPointRadius(3)
            .setFilled(true)
            .setColor(color)
            .setPointColor(pointColor)
        lines.add(line)
        data.lines = lines
        data.baseValue = Float.NEGATIVE_INFINITY
        val v = Viewport(weightChart!!.maximumViewport)
        v.bottom = 0f
        v.top = 7f
        v.left = 0f
        v.right = 1f
        weightChart!!.maximumViewport = v
        weightChart!!.setCurrentViewportWithAnimation(v)
        return data
    }

    override fun enableActivity()
    {
        dateFromTextView.isEnabled = true
        dateToTextView.isEnabled = true

        newDateTextView.isEnabled = true
        newWeightEditText.isEnabled = true

        weightList.isEnabled = true

        progressBar.visibility = View.GONE
    }

    override fun disableActivity()
    {
        dateFromTextView.isEnabled = false
        dateToTextView.isEnabled = false

        newDateTextView.isEnabled = false
        newWeightEditText.isEnabled = false

        weightList.isEnabled = false

        progressBar.visibility = View.VISIBLE
    }
}
