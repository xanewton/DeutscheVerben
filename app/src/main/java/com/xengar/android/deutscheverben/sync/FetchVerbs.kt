/*
 * Copyright (C) 2018 Angel Garcia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xengar.android.deutscheverben.sync

import android.content.ContentResolver
import android.database.Cursor
import android.os.AsyncTask
import android.util.Log

import com.xengar.android.deutscheverben.adapter.VerbAdapter
import com.xengar.android.deutscheverben.data.Verb
import com.xengar.android.deutscheverben.utils.ActivityUtils
import com.xengar.android.deutscheverben.utils.FragmentUtils

import java.util.ArrayList

import fr.castorflex.android.circularprogressbar.CircularProgressBar

import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_COLOR
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_COMMON
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_TYPE
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INFINITIV
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.CONTENT_FAVORITE_VERBS_URI
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.CONTENT_VERBS_URI
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.S_TOP_100
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.S_TOP_1000
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.S_TOP_25
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.S_TOP_250
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.S_TOP_50
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.S_TOP_500
import com.xengar.android.deutscheverben.utils.Constants.ALPHABET
import com.xengar.android.deutscheverben.utils.Constants.COLOR
import com.xengar.android.deutscheverben.utils.Constants.FAVORITES
import com.xengar.android.deutscheverben.utils.Constants.TYPE
import com.xengar.android.deutscheverben.utils.Constants.TYPE_WEAK
import com.xengar.android.deutscheverben.utils.Constants.TYPE_STRONG
import com.xengar.android.deutscheverben.utils.Constants.TYPE_MIXED
import com.xengar.android.deutscheverben.utils.Constants.TYPE_ALL
import com.xengar.android.deutscheverben.utils.Constants.LOG
import com.xengar.android.deutscheverben.utils.Constants.MOST_COMMON_100
import com.xengar.android.deutscheverben.utils.Constants.MOST_COMMON_1000
import com.xengar.android.deutscheverben.utils.Constants.MOST_COMMON_25
import com.xengar.android.deutscheverben.utils.Constants.MOST_COMMON_250
import com.xengar.android.deutscheverben.utils.Constants.MOST_COMMON_50
import com.xengar.android.deutscheverben.utils.Constants.MOST_COMMON_500
import com.xengar.android.deutscheverben.utils.Constants.MOST_COMMON_ALL

/**
 * FetchVerbs from the database.
 */
class FetchVerbs// Constructor
(private val type: String, // Verb type (1=weak, 2=strong, 3=mixed, 0=all)
 private val sort: String, // Sort order (alphabet, color, groups)
 private val common: String, // Common (Top50, Top100, Top25, all)
 private val adapter: VerbAdapter,
 private val contentResolver: ContentResolver,
 private val verbs: MutableList<Verb>,
 private val progressBar: CircularProgressBar)
    : AsyncTask<Void, Void, ArrayList<Verb>>() {

    private val TAG = FetchVerbs::class.java.simpleName

    override fun doInBackground(vararg voids: Void): ArrayList<Verb> {
        // Define a projection that specifies the columns from the table we care about.
        val columns = ActivityUtils.allVerbColumns()
        val sortOrder: String = when (sort) {
            ALPHABET -> "$COLUMN_INFINITIV ASC"
            COLOR -> "$COLUMN_COLOR DESC, $COLUMN_INFINITIV ASC"
            TYPE -> "$COLUMN_TYPE ASC, $COLUMN_INFINITIV ASC"
            else -> "$COLUMN_INFINITIV ASC"
        }

        var where: String? = null
        val listArgs = ArrayList<String>()
        when (common) {
            MOST_COMMON_25 -> {
                where = "$COLUMN_COMMON = ?"
                listArgs.add(S_TOP_25)
            }
            MOST_COMMON_50 -> {
                where = "$COLUMN_COMMON = ? OR $COLUMN_COMMON = ?"
                listArgs.add(S_TOP_25)
                listArgs.add(S_TOP_50)
            }
            MOST_COMMON_100 -> {
                where = "$COLUMN_COMMON = ? OR $COLUMN_COMMON = ? OR $COLUMN_COMMON = ?"
                listArgs.add(S_TOP_25)
                listArgs.add(S_TOP_50)
                listArgs.add(S_TOP_100)
            }
            MOST_COMMON_250 -> {
                where = (COLUMN_COMMON + " = ? OR " + COLUMN_COMMON
                        + " = ? OR " + COLUMN_COMMON + " = ? OR " + COLUMN_COMMON + " = ?")
                listArgs.add(S_TOP_25)
                listArgs.add(S_TOP_50)
                listArgs.add(S_TOP_100)
                listArgs.add(S_TOP_250)
            }
            MOST_COMMON_500 -> {
                where = (COLUMN_COMMON + " = ? OR " + COLUMN_COMMON
                        + " = ? OR " + COLUMN_COMMON + " = ? OR " + COLUMN_COMMON + " = ? OR "
                        + COLUMN_COMMON + " = ?")
                listArgs.add(S_TOP_25)
                listArgs.add(S_TOP_50)
                listArgs.add(S_TOP_100)
                listArgs.add(S_TOP_250)
                listArgs.add(S_TOP_500)
            }
            MOST_COMMON_1000 -> {
                where = (COLUMN_COMMON + " = ? OR " + COLUMN_COMMON
                        + " = ? OR " + COLUMN_COMMON + " = ? OR " + COLUMN_COMMON + " = ? OR "
                        + COLUMN_COMMON + " = ? OR " + COLUMN_COMMON + " = ?")
                listArgs.add(S_TOP_25)
                listArgs.add(S_TOP_50)
                listArgs.add(S_TOP_100)
                listArgs.add(S_TOP_250)
                listArgs.add(S_TOP_500)
                listArgs.add(S_TOP_1000)
            }
            MOST_COMMON_ALL -> {
            }
            else -> {
            }
        }

        val cursor: Cursor?
        when (type) {
            TYPE_WEAK, TYPE_STRONG, TYPE_MIXED -> {
                where = if (where == null) {
                    "$COLUMN_TYPE = ?"
                } else {
                    "($where) AND $COLUMN_TYPE = ?"
                }
                // type substring should match type numbers (1,2,3,0)
                listArgs.add(type.substring(0, 1))
                val whereArgs = if (listArgs.size > 0) listArgs.toTypedArray() else null
                cursor = contentResolver.query(CONTENT_VERBS_URI, columns, where, whereArgs, sortOrder)
            }
            TYPE_ALL -> {
                val whereArgs = if (listArgs.size > 0) listArgs.toTypedArray() else null
                cursor = contentResolver.query(CONTENT_VERBS_URI, columns, where, whereArgs, sortOrder)
            }

            FAVORITES -> cursor = contentResolver.query(CONTENT_FAVORITE_VERBS_URI, columns,
                    null, null, sortOrder)
            else -> {
                val whereArgs = if (listArgs.size > 0) listArgs.toTypedArray() else null
                cursor = contentResolver.query(CONTENT_VERBS_URI, columns, where, whereArgs, sortOrder)
            }
        }

        val verbs = ArrayList<Verb>()
        if (cursor != null && cursor.count != 0) {
            while (cursor.moveToNext()) {
                verbs.add(ActivityUtils.verbFromCursor(cursor))
            }
        } else {
            if (LOG) {
                Log.d(TAG, "Cursor is empty")
            }
        }
        cursor?.close()
        return verbs
    }

    override fun onPostExecute(list: ArrayList<Verb>?) {
        super.onPostExecute(list)
        if (list != null) {
            verbs.addAll(list)
            adapter.notifyDataSetChanged()
        }
        FragmentUtils.updateProgressBar(progressBar, false)
    }
}
