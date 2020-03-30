package com.kotlin.caight.data

import com.kotlin.caight.listener.OnCatAttributeChangedListener
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class Cat {
    var id = -1
    var colorInteger = -1
    var name: String = ""
    var birthday: Date = Date.getToday()
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
    private fun raiseAttrChangedEvent(id: Int, newValue: Any) {
        val iterator: Iterator<OnCatAttributeChangedListener> = attrChangedListener.listIterator()
        while (iterator.hasNext()) {
            iterator.next().changed(id, newValue)
        }
    }

    //
    // public Methods
    //
    fun toJsonObject(): JSONObject? {
        return try {
            val weightArray = JSONArray()
            for ((key, value) in weights) {
                val weight = JSONObject()
                weight.put(__JSON_KEY_WEIGHTS_WHEN__, key.toLong())
                weight.put(__JSON_KEY_WEIGHTS_VALUE__, value)
                weightArray.put(weight)
            }
            val attributes = JSONArray()
            for (attr in this.attributes) {
                attributes.put(attr)
            }
            val json = JSONObject()
            json.put(__JSON_KEY_ID__, id)
            json.put(__JSON_KEY_COLOR__, colorInteger)
            json.put(__JSON_KEY_NAME__, name)
            json.put(__JSON_KEY_BIRTHDAY__, birthday.toLong())
            json.put(__JSON_KEY_GENDER__, gender.value)
            json.put(__JSON_KEY_SPECIES__, species)
            json.put(__JSON_KEY_WEIGHTS__, weightArray)
            json.put(__JSON_KEY_ATTRIBUTES__, attributes)
            json
        } catch (e: JSONException) {
            e.printStackTrace()
            return null
        }
    }

    //
    // Getter / Setter
    //
    fun hasWeightOn(date: Date): Boolean {
        return weights.containsKey(date)
    }

    fun getWeightsInRange(from: Date, to: Date): TreeMap<Date, Float> {
        val f: Long = from.toLong()
        val t: Long = to.toLong()
        val result = TreeMap<Date, Float>()
        val iterator: Iterator<Map.Entry<Date, Float>> =
            weights.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.key.toLong() >= f) {
                result[entry.key] = entry.value
                break
            }
        }
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.key.toLong() <= t) {
                result[entry.key] = entry.value
            } else {
                break
            }
        }
        return result
    }

    companion object {
        //
        // public static final Fields
        //
        // Json keys
        const val __JSON_KEY_ID__ = "id"
        const val __JSON_KEY_COLOR__ = "color"
        const val __JSON_KEY_NAME__ = "name"
        const val __JSON_KEY_BIRTHDAY__ = "birthday"
        const val __JSON_KEY_GENDER__ = "gender"
        const val __JSON_KEY_SPECIES__ = "species"
        const val __JSON_KEY_WEIGHTS__ = "weights"
        const val __JSON_KEY_WEIGHTS_WHEN__ = "when"
        const val __JSON_KEY_WEIGHTS_VALUE__ = "weight"
        const val __JSON_KEY_ATTRIBUTES__ = "attributes"

        //
        // public static Methods
        //
        fun parseJson(json: JSONObject): Cat? {
            return try {
                val weightArray = json.getJSONArray(__JSON_KEY_WEIGHTS__)
                val weights = TreeMap<Date, Float>()
                val length = weightArray.length()
                for (i in 0 until length) {
                    val weight = weightArray.getJSONObject(i)
                    weights[Date.fromBigInt(weight.getLong(__JSON_KEY_WEIGHTS_WHEN__))] =
                        weight.getDouble(__JSON_KEY_WEIGHTS_VALUE__).toFloat()
                }

                val attributes = json.getJSONArray(__JSON_KEY_ATTRIBUTES__)
                val attrArray = ArrayList<String>(attributes.length())
                for (i in attrArray.indices) {
                    attrArray[i] = attributes.getString(i)
                }

                val cat = Cat()
                cat.id = json.getInt(__JSON_KEY_ID__)
                cat.colorInteger = json.getInt(__JSON_KEY_COLOR__)
                cat.name = json.getString(__JSON_KEY_NAME__)
                cat.birthday = Date.fromBigInt(json.getLong(__JSON_KEY_BIRTHDAY__))
                cat.gender = Gender.fromValue(json.getInt(__JSON_KEY_GENDER__))!!
                cat.species = json.getInt(__JSON_KEY_SPECIES__)
                cat.weights = weights
                cat.attributes = attrArray
                cat
            } catch (e: JSONException) {
                e.printStackTrace()
                null
            }
        }
    }
}