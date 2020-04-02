package com.kotlin.caight.companion

import java.util.regex.Pattern

object Constants
{
    const val ServerAddress = "wss://caight.herokuapp.com/ws"

    const val NullChar = '\u0000'

    object Regex
    {
        val Email: Pattern = Pattern.compile("^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")
        val StrongPassword: Pattern = Pattern.compile("((?=.*[a-z])(?=.*[0-9])(?=.*[!@#$%^&*])(?=.*[A-Z]).{8,})")
        val PasswordConstraints = arrayOf(
            Pattern.compile("(.*[a-z].*)"),
            Pattern.compile("(.*[A-Z].*)"),
            Pattern.compile("(.*[0-9].*)"),
            Pattern.compile("(.*[!@#$%^&*].*)"),
            Pattern.compile("(.{8,})")
        )
    }

    object IntentKey
    {
        const val Email = "key_email"
        const val AutoLogin = "key_auto_login"

        const val GroupId = "group_id"
        const val CatId = "cat_id"
    }
}