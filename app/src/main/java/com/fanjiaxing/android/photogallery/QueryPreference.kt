package com.fanjiaxing.android.photogallery

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit

private const val PREF_NAME = "PREF_SEARCH_QUERY"
private const val PREF_SEARCH_QUERY = "searchQuery"
private const val PREF_LAST_RESULT_ID = "lastResultId"
private const val PREF_IS_POLLING = "isPolling"

object QueryPreference {

    fun getStoredQuery(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        return prefs.getString(PREF_SEARCH_QUERY, "")!!
    }

    fun setStoredQuery(context: Context, query: String) {
        context.getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit {
            putString(PREF_SEARCH_QUERY, query)
        }
    }

    fun setLastResultId(context: Context, lastResultId: String) {
        context.getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit {
            putString(PREF_LAST_RESULT_ID, lastResultId)
        }
    }

    fun getLastResultId(context: Context): String {
        return context.getSharedPreferences(PREF_NAME, MODE_PRIVATE)
            .getString(PREF_LAST_RESULT_ID, "")!!
    }

    fun isPolling(context: Context): Boolean {
        return context.getSharedPreferences(PREF_NAME, MODE_PRIVATE)
            .getBoolean(PREF_IS_POLLING, false)
    }

    fun setPolling(context: Context, isOn: Boolean) {
        context.getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit {
            putBoolean(PREF_IS_POLLING, isOn)
        }
    }
}