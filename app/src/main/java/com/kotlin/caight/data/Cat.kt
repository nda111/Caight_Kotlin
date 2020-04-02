package com.kotlin.caight.data

import com.kotlin.caight.delegate.OnCatAttributeChangedListener
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class Cat
{
    var id = -1
    var colorInteger = -1
    var name: String = ""
    var birthday: Date = Date.getToday()
    val age: IntArray
        get()
        {
            val age = Date.getPeriod(birthday, Date.getToday())
            return intArrayOf(age.year - 1970, age.month - 1)
        }
    var gender: Gender = Gender.MALE
    var species = -1
    var weights: TreeMap<Date, Float> = TreeMap<Date, Float>()
        private set
    val hasWeight: Boolean
        get() = weights.size > 0
    val lastWeight: MutableMap.MutableEntry<Date, Float>?
        get() = weights.lastEntry()
    var attributes: ArrayList<String> = ArrayList<String>(0)
        private set

    //
    // event Listeners
    //
    val attrChangedListener: LinkedList<OnCatAttributeChangedListener> =
        LinkedList<OnCatAttributeChangedListener>()

    //
    // private Methods
    //
    private fun raiseAttrChangedEvent(id: Int, newValue: Any)
    {
        val iterator: Iterator<OnCatAttributeChangedListener> = attrChangedListener.listIterator()
        while (iterator.hasNext())
        {
            iterator.next().changed(id, newValue)
        }
    }

    //
    // public Methods
    //
    fun toJsonObject(): JSONObject?
    {
        return try
        {
            val weightArray = JSONArray()
            for ((key, value) in weights)
            {
                val weight = JSONObject()
                weight.put(JsonKeyWeightsWhen, key.toLong())
                weight.put(JsonKeyWeightsValue, value)
                weightArray.put(weight)
            }
            val attributes = JSONArray()
            for (attr in this.attributes)
            {
                attributes.put(attr)
            }
            val json = JSONObject()
            json.put(JsonKeyId, id)
            json.put(JsonKeyColor, colorInteger)
            json.put(JsonKeyName, name)
            json.put(JsonKeyBirthday, birthday.toLong())
            json.put(JsonKeyGender, gender.value)
            json.put(JsonKeySpecies, species)
            json.put(JsonKeyWeights, weightArray)
            json.put(JsonKeyAttributes, attributes)
            json
        }
        catch (e: JSONException)
        {
            e.printStackTrace()
            return null
        }
    }

    //
    // Getter / Setter
    //
    fun hasWeightOn(date: Date): Boolean
    {
        return weights.containsKey(date)
    }

    fun getWeightsInRange(from: Date, to: Date): TreeMap<Date, Float>
    {
        val f: Long = from.toLong()
        val t: Long = to.toLong()
        val result = TreeMap<Date, Float>()
        val iterator: Iterator<Map.Entry<Date, Float>> =
            weights.entries.iterator()
        while (iterator.hasNext())
        {
            val entry = iterator.next()
            if (entry.key.toLong() >= f)
            {
                result[entry.key] = entry.value
                break
            }
        }
        while (iterator.hasNext())
        {
            val entry = iterator.next()
            if (entry.key.toLong() <= t)
            {
                result[entry.key] = entry.value
            }
            else
            {
                break
            }
        }
        return result
    }

    fun getChronologicalWeightsInRange(from: Date, to: Date): TreeMap<Long, Float>
    {
        val weightsInRange = getWeightsInRange(from, to)
        val weights = TreeMap<Long, Float>()

        for (entry in weightsInRange)
        {
            weights[entry.key.toCalendar().timeInMillis] = entry.value
        }

        return weights
    }

    companion object
    {
        //
        // public static final Fields
        //
        // Json keys
        const val JsonKeyId = "id"
        const val JsonKeyColor = "color"
        const val JsonKeyName = "name"
        const val JsonKeyBirthday = "birthday"
        const val JsonKeyGender = "gender"
        const val JsonKeySpecies = "species"
        const val JsonKeyWeights = "weights"
        const val JsonKeyWeightsWhen = "when"
        const val JsonKeyWeightsValue = "weight"
        const val JsonKeyAttributes = "attributes"

        //
        // public static Methods
        //
        fun parseJson(json: JSONObject): Cat
        {
            val weightArray = json.getJSONArray(JsonKeyWeights)
            val weights = TreeMap<Date, Float>()
            val length = weightArray.length()
            for (i in 0 until length)
            {
                val weight = weightArray.getJSONObject(i)
                weights[Date.fromBigInt(weight.getLong(JsonKeyWeightsWhen))] =
                    weight.getDouble(JsonKeyWeightsValue).toFloat()
            }

            val attributes = json.getJSONArray(JsonKeyAttributes)
            val attrArray = ArrayList<String>(attributes.length())
            for (i in attrArray.indices)
            {
                attrArray[i] = attributes.getString(i)
            }

            val cat = Cat()
            cat.id = json.getInt(JsonKeyId)
            cat.colorInteger = json.getInt(JsonKeyColor)
            cat.name = json.getString(JsonKeyName)
            cat.birthday = Date.fromBigInt(json.getLong(JsonKeyBirthday))
            cat.gender = Gender.fromValue(json.getInt(JsonKeyGender))!!
            cat.species = json.getInt(JsonKeySpecies)
            cat.weights = weights
            cat.attributes = attrArray

            return cat
        }
    }
}