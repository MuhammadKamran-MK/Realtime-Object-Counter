package com.example.realtimeobjectcounter.Utils

import android.content.Context
import android.content.SharedPreferences
import com.example.realtimeobjectcounter.Utils.Constants.SHARED_PREF_FILE
import com.google.gson.Gson

class SharedPreferenceManager constructor(context: Context) {

    private val sharedPreferences =
        context.getSharedPreferences(SHARED_PREF_FILE, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()
    private val gson = Gson()

    fun saveStringInPref(key: String, value: String?) {
        editor.putString(key, value)
        editor.apply()
    }

    fun getStringInPref(key: String): String? {
        return sharedPreferences.getString(key, "")
    }

}