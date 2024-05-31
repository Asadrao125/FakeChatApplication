package com.android.app.fakechatapp.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import java.util.*

class SharedPref(context: Context) {
    private val mSharedPref: SharedPreferences =
        context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)

    fun read(key: String, defValue: String): String? {
        return mSharedPref.getString(key, defValue)
    }

    fun read(key: String?, defValue: Int): Int {
        return mSharedPref.getInt(key, defValue)
    }

    fun read(key: String?, defValue: Boolean): Boolean {
        return mSharedPref.getBoolean(key, defValue)
    }

    fun write(key: String?, value: String?) {
        val prefsEditor = mSharedPref.edit()
        prefsEditor.putString(key, value)
        prefsEditor.apply()
    }

    fun write(key: String?, value: Boolean) {
        val prefsEditor = mSharedPref.edit()
        prefsEditor.putBoolean(key, value)
        prefsEditor.apply()
    }

    fun write(key: String?, value: Int?) {
        val prefsEditor = mSharedPref.edit()
        prefsEditor.putInt(key, value!!).apply()
    }

    @SuppressLint("CommitPrefEdits")
    fun remove(key: String?) {
        val prefsEditor = mSharedPref.edit()
        prefsEditor.remove(key)
    }
}