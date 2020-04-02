package com.kotlin.caight.data

import org.json.JSONException
import org.json.JSONObject

class CatGroup {
    var id: Int = -1
    var name: String = ""
    var owner: String = ""
    var isLocked: Boolean = false
        private set

    fun toJsonObject(): JSONObject? {
        return try {
            val json = JSONObject()
            json.put(__JSON_KEY_GROUP_ID__, id)
            json.put(__JSON_KEY_GROUP_NAME__, name)
            json.put(__JSON_KEY_GROUP_OWNER__, owner)
            json.put(__JSON_KEY_GROUP_LOCKED__, isLocked)
            json
        } catch (e: JSONException) {
            e.printStackTrace()
            null
        }
    }

    fun lock() {
        isLocked = true
    }

    fun unlock() {
        isLocked = false
    }

    companion object {
        const val __JSON_KEY_GROUP_ID__ = "group_id"
        const val __JSON_KEY_GROUP_NAME__ = "group_name"
        const val __JSON_KEY_GROUP_OWNER__ = "group_owner"
        const val __JSON_KEY_GROUP_LOCKED__ = "group_locked"

        fun parseJson(json: JSONObject): CatGroup? {
            return try {
                val group = CatGroup()
                group.id = json.getInt(__JSON_KEY_GROUP_ID__)
                group.name = json.getString(__JSON_KEY_GROUP_NAME__)
                group.owner = json.getString(__JSON_KEY_GROUP_OWNER__)
                group.isLocked = json.getBoolean(__JSON_KEY_GROUP_LOCKED__)
                group
            } catch (e: JSONException) {
                e.printStackTrace()
                null
            }
        }
    }
}