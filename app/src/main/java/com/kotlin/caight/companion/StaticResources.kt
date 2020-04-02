package com.kotlin.caight.companion

import android.content.Context
import android.content.SharedPreferences
import com.kotlin.caight.R
import com.kotlin.caight.data.Cat
import com.kotlin.caight.data.CatGroup
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

internal object StaticResources
{

    internal object AutoLogin
    {
        private var preferences: SharedPreferences? = null

        private const val KeyPreferences = "__KEY_PREFERENCE_AUTO_LOGIN__"
        private const val KeyDoAutoLogin = "__KEY_DO_AUTO_LOGIN__"
        private const val KeyEmail = "__KEY_EMAIL__"
        private const val KeyPassword = "__KEY_PASSWORD__"

        private fun getPreferencesIfNull(context: Context)
        {
            if (preferences == null)
            {
                preferences = context.getSharedPreferences(
                    KeyPreferences,
                    Context.MODE_PRIVATE
                )
            }
        }

        fun clear(context: Context)
        {
            getPreferencesIfNull(context)
            preferences!!.edit().clear().apply()
        }

        fun set(context: Context, doAutoLogin: Boolean, email: String?, password: String?)
        {
            getPreferencesIfNull(context)
            if (doAutoLogin)
            {
                preferences!!.edit()
                    .putBoolean(KeyDoAutoLogin, doAutoLogin)
                    .putString(KeyEmail, email)
                    .putString(KeyPassword, password)
                    .apply()
            }
            else
            {
                preferences!!.edit()
                    .putBoolean(KeyDoAutoLogin, doAutoLogin)
                    .remove(KeyEmail)
                    .remove(KeyPassword)
                    .apply()
            }
        }

        fun getDoAutoLogin(context: Context): Boolean
        {
            getPreferencesIfNull(context)
            return preferences!!.getBoolean(KeyDoAutoLogin, false)
        }

        fun getEmail(context: Context): String?
        {
            getPreferencesIfNull(context)
            return preferences!!.getString(KeyEmail, null)
        }

        fun getPassword(context: Context): String?
        {
            getPreferencesIfNull(context)
            return preferences!!.getString(KeyPassword, null)
        }
    }

    internal object StringArrays
    {
        private var preferences: SharedPreferences? = null
        private const val KeyPreferences = "__KEY_PREFERENCES_STRING_ARRAYS__"
        private const val KeyNameExamples = "__KEY_NAME_EXAMPLES__"
        private const val KeySpecies = "__KEY_SPECIES__"
        private const val KeySortedSpecies = "__KEY_SPECIES_SORTED__"

        private fun getPreferencesIfNull(context: Context)
        {
            if (preferences == null)
            {
                preferences = context.getSharedPreferences(KeyPreferences, Context.MODE_PRIVATE)
            }
        }

        fun initializeIfNotExists(context: Context)
        {
            getPreferencesIfNull(context)

            val resources = context.resources

            if (!preferences!!.contains(KeyNameExamples))
            {
                val names = resources.getStringArray(R.array.name_examples)
                setNameExamples(context, names)
            }

            if (!preferences!!.contains(KeySpecies) || !preferences!!.contains(KeySortedSpecies))
            {
                val species = resources.getStringArray(R.array.species)
                setSpecies(context, species)
            }
        }

        fun getNameExamples(context: Context): Array<String>
        {
            getPreferencesIfNull(context)
            return preferences!!.getString(KeyNameExamples, "")!!.split("\u0000").toTypedArray()
        }

        fun setNameExamples(context: Context, examples: Array<String>)
        {
            getPreferencesIfNull(context)

            preferences!!.edit()
                .putString(KeyNameExamples, examples.joinToString("\u0000"))
                .apply()
        }

        fun getSpecies(context: Context): Array<String>
        {
            getPreferencesIfNull(context)
            return preferences!!.getString(KeySpecies, "")!!.split("\u0000").toTypedArray()
        }

        fun getSortedSpecies(context: Context): Array<String>
        {
            getPreferencesIfNull(context)
            return preferences!!.getString(KeySortedSpecies, "")!!.split("\u0000").toTypedArray()
        }

        fun setSpecies(context: Context, species: Array<String>)
        {
            getPreferencesIfNull(context)
            val sortedSpecies = species.copyOf()
            sortedSpecies.sort()

            preferences!!.edit()
                .putString(KeySpecies, species.joinToString("\u0000"))
                .putString(KeySortedSpecies, sortedSpecies.joinToString("\u0000"))
                .apply()
        }
    }

    internal object Account
    {
        private var preferences: SharedPreferences? = null

        private const val KeyPreferences = "__KEY_PREFERENCES_ACCOUNT__"
        private const val KeyEmail = "__KEY_EMAIL__"
        private const val KeyName = "__KEY_NAME__"
        private const val KeyId = "__KEY_ID__"
        private const val KeyAuthenticationToken = "__KEY_AUTHENTICATION_TOKEN__"

        private fun getPreferencesIfNull(context: Context)
        {
            if (preferences == null)
            {
                preferences = context.getSharedPreferences(
                    KeyPreferences,
                    Context.MODE_PRIVATE
                )
            }
        }

        fun clear(context: Context)
        {
            getPreferencesIfNull(context)
            preferences!!.edit().clear().apply()
        }

        fun getEmail(context: Context): String
        {
            getPreferencesIfNull(context)
            return preferences!!.getString(
                KeyEmail,
                ""
            )!!
        }

        fun setEmail(context: Context, email: String?)
        {
            getPreferencesIfNull(context)
            preferences!!.edit()
                .putString(KeyEmail, email).apply()
        }

        fun getName(context: Context): String
        {
            getPreferencesIfNull(context)
            return preferences!!.getString(
                KeyName,
                ""
            )!!
        }

        fun setName(context: Context, name: String?)
        {
            getPreferencesIfNull(context)
            preferences!!.edit()
                .putString(KeyName, name).apply()
        }

        fun getId(context: Context): ByteArray
        {
            getPreferencesIfNull(context)
            return Methods.NumericBinary.longToByteArray(
                preferences!!.getLong(
                    KeyId,
                    -1
                )
            )
        }

        fun setId(context: Context, id: ByteArray?)
        {
            getPreferencesIfNull(context)
            preferences!!.edit()
                .putLong(KeyId, Methods.NumericBinary.byteArrayToLong(id!!).toLong()).apply()
        }

        fun getAuthenticationToken(context: Context): String
        {
            getPreferencesIfNull(context)
            return preferences!!.getString(
                KeyAuthenticationToken,
                ""
            )!!
        }

        fun setAuthenticationToken(
            context: Context,
            token: String?
        )
        {
            getPreferencesIfNull(context)
            preferences!!.edit()
                .putString(KeyAuthenticationToken, token).apply()
        }
    }

    internal object Entity
    {
        private var preferences: SharedPreferences? = null

        private const val KeyPreferences = "__KEY_PREFERENCES_ENTITY__"
        private const val KeyUpdateList = "__KEY_UPDATE_LIST__"
        private const val KeyEntries = "__KEY_ENTRIES__"
        private const val JsonKeyGroup = "__JSON_KEY_GROUP__"
        private const val JsonKeyCats = "__JSON_KEY_CATS__"

        private var groups: ArrayList<CatGroup>? = null
        private var entries: HashMap<CatGroup, List<Cat>>? = null
        private var updateList: Boolean? = null

        private fun getPreferencesIfNull(context: Context)
        {
            if (preferences == null)
            {
                preferences = context.getSharedPreferences(
                    KeyPreferences,
                    Context.MODE_PRIVATE
                )
            }
        }

        fun loadEntries(context: Context)
        {
            getPreferencesIfNull(context)
            groups = ArrayList<CatGroup>()
            entries =
                HashMap<CatGroup, List<Cat>>()
            val jsonString = preferences!!.getString(
                KeyEntries,
                "[]"
            )
            try
            {
                val array = JSONArray(jsonString)
                for (i in 0 until array.length())
                {
                    val entry = array[i] as JSONObject
                    val group: CatGroup = CatGroup.parseJson(entry[JsonKeyGroup] as JSONObject)!!
                    val catsJson = entry[JsonKeyCats] as JSONArray
                    val cats: ArrayList<Cat> = ArrayList<Cat>(catsJson.length())
                    for (j in 0 until catsJson.length())
                    {
                        val cat: Cat = Cat.parseJson(catsJson[i] as JSONObject)!!
                        cats.add(cat)
                    }
                    groups!!.add(group)
                    entries!![group] = cats
                }
            }
            catch (e: JSONException)
            {
                e.printStackTrace()
            }
        }

        fun clear(context: Context)
        {
            getPreferencesIfNull(context)
            preferences!!.edit().clear().apply()
        }

        fun getGroups(context: Context): ArrayList<CatGroup>
        {
            if (groups == null)
            {
                loadEntries(context)
            }
            else
            {
                getPreferencesIfNull(context)
            }
            return groups!!
        }

        fun getEntries(context: Context): HashMap<CatGroup, List<Cat>>
        {
            if (entries == null)
            {
                loadEntries(context)
            }
            else
            {
                getPreferencesIfNull(context)
            }
            return entries!!
        }

        fun setEntries(
            context: Context,
            groups: ArrayList<CatGroup>,
            entries: HashMap<CatGroup, List<Cat>>
        )
        {
            getPreferencesIfNull(context)
            try
            {
                val array = JSONArray()
                for (group in groups)
                {
                    val cats: List<Cat>? = entries[group]
                    val entry = JSONObject()
                    entry.put(JsonKeyGroup, group.toJsonObject())
                    val catArray = JSONArray()
                    if (cats != null)
                    {
                        for (cat in cats)
                        {
                            catArray.put(cat.toJsonObject())
                        }
                    }
                    entry.put(JsonKeyCats, catArray)
                }
                preferences!!.edit()
                    .putString(KeyEntries, array.toString()).apply()
                Entity.groups = groups
                Entity.entries = entries
            }
            catch (e: JSONException)
            {
                e.printStackTrace()
            }
        }

        fun getUpdateList(context: Context): Boolean
        {
            if (updateList == null)
            {
                getPreferencesIfNull(context)
                updateList = preferences!!.getBoolean(
                    KeyUpdateList,
                    false
                )
            }
            return updateList!!
        }

        fun setUpdateList(
            context: Context,
            updateList: Boolean
        )
        {
            getPreferencesIfNull(context)
            Entity.updateList = updateList
            preferences!!.edit()
                .putBoolean(KeyUpdateList, updateList).apply()
        }
    }
}