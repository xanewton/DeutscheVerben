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
package com.xengar.android.deutscheverben.data

import android.content.ContentValues
import android.content.Context
import android.content.res.XmlResourceParser
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.support.v4.content.ContextCompat
import android.util.Log

import com.xengar.android.deutscheverben.R

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException

import java.io.IOException

import android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_COLOR
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_COMMON
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_CONJUGATION_NUMBER
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_DEFINITION
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_TYPE
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_ID
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_IMAGE
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_IMPERATIV_DU
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_IMPERATIV_IHR
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_IMPERATIV_SIE
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_IMPERFEKT_ER
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INDIKATIV_FUTUR1_DU
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INDIKATIV_FUTUR1_ER
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INDIKATIV_FUTUR1_ICH
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INDIKATIV_FUTUR1_IHR
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INDIKATIV_FUTUR1_SIE
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INDIKATIV_FUTUR1_WIR
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INDIKATIV_FUTUR2_DU
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INDIKATIV_FUTUR2_ER
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INDIKATIV_FUTUR2_ICH
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INDIKATIV_FUTUR2_IHR
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INDIKATIV_FUTUR2_SIE
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INDIKATIV_FUTUR2_WIR
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INDIKATIV_PERFEKT_DU
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INDIKATIV_PERFEKT_ER
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INDIKATIV_PERFEKT_ICH
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INDIKATIV_PERFEKT_IHR
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INDIKATIV_PERFEKT_SIE
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INDIKATIV_PERFEKT_WIR
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INDIKATIV_PLUSQUAMPERFEKT_DU
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INDIKATIV_PLUSQUAMPERFEKT_ER
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INDIKATIV_PLUSQUAMPERFEKT_ICH
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INDIKATIV_PLUSQUAMPERFEKT_IHR
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INDIKATIV_PLUSQUAMPERFEKT_SIE
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INDIKATIV_PLUSQUAMPERFEKT_WIR
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INDIKATIV_PRASENS_DU
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INDIKATIV_PRASENS_ER
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INDIKATIV_PRASENS_ICH
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INDIKATIV_PRASENS_IHR
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INDIKATIV_PRASENS_SIE
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INDIKATIV_PRASENS_WIR
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INDIKATIV_PRATERIUM_DU
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INDIKATIV_PRATERIUM_ER
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INDIKATIV_PRATERIUM_ICH
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INDIKATIV_PRATERIUM_IHR
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INDIKATIV_PRATERIUM_SIE
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INDIKATIV_PRATERIUM_WIR
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INFINITIV
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INFINITIV_PERFEKT
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_INFINITIV_PRASENS
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV1_FUTUR1_DU
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV1_FUTUR1_ER
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV1_FUTUR1_ICH
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV1_FUTUR1_IHR
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV1_FUTUR1_SIE
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV1_FUTUR1_WIR
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV1_FUTUR2_DU
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV1_FUTUR2_ER
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV1_FUTUR2_ICH
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV1_FUTUR2_IHR
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV1_FUTUR2_SIE
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV1_FUTUR2_WIR
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV1_PERFEKT_DU
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV1_PERFEKT_ER
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV1_PERFEKT_ICH
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV1_PERFEKT_IHR
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV1_PERFEKT_SIE
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV1_PERFEKT_WIR
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV1_PRASENS_DU
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV1_PRASENS_ER
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV1_PRASENS_ICH
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV1_PRASENS_IHR
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV1_PRASENS_SIE
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV1_PRASENS_WIR
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV2_FUTUR1_DU
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV2_FUTUR1_ER
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV2_FUTUR1_ICH
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV2_FUTUR1_IHR
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV2_FUTUR1_SIE
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV2_FUTUR1_WIR
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV2_FUTUR2_DU
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV2_FUTUR2_ER
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV2_FUTUR2_ICH
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV2_FUTUR2_IHR
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV2_FUTUR2_SIE
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV2_FUTUR2_WIR
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV2_PLUSQUAMPERFEKT_DU
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV2_PLUSQUAMPERFEKT_ER
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV2_PLUSQUAMPERFEKT_ICH
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV2_PLUSQUAMPERFEKT_IHR
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV2_PLUSQUAMPERFEKT_SIE
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV2_PLUSQUAMPERFEKT_WIR
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV2_PRATERIUM_DU
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV2_PRATERIUM_ER
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV2_PRATERIUM_ICH
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV2_PRATERIUM_IHR
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV2_PRATERIUM_SIE
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_KONJUNKTIV2_PRATERIUM_WIR
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_NOTES
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_PARTIZIP_PERFEKT
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_PARTIZIP_PRASENS
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_PRASENS_ER
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_RADICALS
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_SAMPLE_1
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_SAMPLE_2
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_SAMPLE_3
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_SCORE
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_TERMINATION
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_TRANSLATION_EN
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_TRANSLATION_FR
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_TRANSLATION_ES
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.CONJUGATION_TBL
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.FAVORITES_TBL
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.VERBS_TBL
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion._ID
import com.xengar.android.deutscheverben.utils.Constants.LOG

/**
 * Database helper for Verbs app. Manages database creation and version management.
 */
class VerbDBHelper
/**
 * Constructs a new instance of [VerbDBHelper].
 * @param context of the app
 */
(private val context: Context)
    : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {

        private val TAG = VerbDBHelper::class.java.simpleName

        /** Name of the database file  */
        private val DATABASE_NAME = "verbs.db"

        /**
         * Database version. If you change the database schema, you must increment the database version.
         */
        private val DATABASE_VERSION = 1

        // List of pre-loaded favorite verbs.
        private val favorites = arrayOf(
                arrayOf("1"), // sein
                arrayOf("2"), // haben
                arrayOf("3"), // werden
                arrayOf("4"), // können
                arrayOf("5")  // müssen
        )

        /**
         * Count the predefined verbs in the xml file.
         * @param context Context
         * @return count
         */
        fun countPredefinedVerbs(context: Context): Int {
            val parser = context.resources.getXml(R.xml.verbs)
            var eventType = -1
            var count = 0
            try {
                // Loop through the XML data
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlResourceParser.START_TAG) {
                        val verbValue = parser.name
                        if (verbValue == "verb") {
                            count++
                        }
                    }
                    eventType = parser.next()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                if (LOG) {
                    Log.e(TAG, "Error loading verbs xml file. ")
                }
            } catch (e: XmlPullParserException) {
                e.printStackTrace()
                if (LOG) {
                    Log.e(TAG, "Error loading verbs xml file. ")
                }
            }

            return count
        }
    }


    /**
     * This is called when the database is created for the first time.
     */
    override fun onCreate(db: SQLiteDatabase) {
        createTables(db, DATABASE_VERSION)
        insertVerbs(db, DATABASE_VERSION)
        insertFavorites(db, DATABASE_VERSION)
        insertConjugation(db, DATABASE_VERSION)
    }

    /**
     * Creates the tables for the schema version.
     * @param db SQLiteDatabase
     * @param version database schema version
     */
    private fun createTables(db: SQLiteDatabase, version: Int) {
        when (version) {
            in 1 .. DATABASE_VERSION -> {
                createTableVerbs(db)
                createTableFavorites(db)
                createTableConjugation(db)
            }
            else -> {}
        }
    }

    private fun createTableVerbs(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE " + VERBS_TBL + " ("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_ID + " INTEGER NOT NULL, "
                + COLUMN_CONJUGATION_NUMBER + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_IMAGE + " TEXT, "
                + COLUMN_INFINITIV + " TEXT NOT NULL, "
                + COLUMN_PARTIZIP_PERFEKT + " TEXT NOT NULL, "
                + COLUMN_IMPERFEKT_ER + " TEXT NOT NULL, "
                + COLUMN_PRASENS_ER + " TEXT NOT NULL, "
                + COLUMN_SAMPLE_1 + " TEXT, "
                + COLUMN_SAMPLE_2 + " TEXT, "
                + COLUMN_SAMPLE_3 + " TEXT, "
                + COLUMN_COMMON + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_TYPE + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_COLOR + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_SCORE + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_DEFINITION + " TEXT NOT NULL, "
                + COLUMN_NOTES + " TEXT, "
                + COLUMN_TRANSLATION_EN + " TEXT, "
                + COLUMN_TRANSLATION_FR + " TEXT, "
                + COLUMN_TRANSLATION_ES + " TEXT);")
    }

    private fun createTableFavorites(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE " + FAVORITES_TBL + " ("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_ID + " INTEGER NOT NULL); ")
    }

    private fun createTableConjugation(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE " + CONJUGATION_TBL + " ("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_ID + " INTEGER NOT NULL, "
                + COLUMN_TERMINATION + " TEXT, "
                + COLUMN_RADICALS + " TEXT, "

                + COLUMN_INFINITIV_PRASENS + " TEXT NOT NULL, "
                + COLUMN_INFINITIV_PERFEKT + " TEXT NOT NULL, "
                + COLUMN_PARTIZIP_PRASENS + " TEXT NOT NULL, "
                + COLUMN_PARTIZIP_PERFEKT + " TEXT NOT NULL, "

                + COLUMN_IMPERATIV_DU + " TEXT, "
                + COLUMN_IMPERATIV_IHR + " TEXT, "
                + COLUMN_IMPERATIV_SIE + " TEXT, "

                + COLUMN_INDIKATIV_PRASENS_ICH + " TEXT, "
                + COLUMN_INDIKATIV_PRASENS_DU + " TEXT, "
                + COLUMN_INDIKATIV_PRASENS_ER + " TEXT, "
                + COLUMN_INDIKATIV_PRASENS_WIR + " TEXT, "
                + COLUMN_INDIKATIV_PRASENS_IHR + " TEXT, "
                + COLUMN_INDIKATIV_PRASENS_SIE + " TEXT, "

                + COLUMN_INDIKATIV_PRATERIUM_ICH + " TEXT, "
                + COLUMN_INDIKATIV_PRATERIUM_DU + " TEXT, "
                + COLUMN_INDIKATIV_PRATERIUM_ER + " TEXT, "
                + COLUMN_INDIKATIV_PRATERIUM_WIR + " TEXT, "
                + COLUMN_INDIKATIV_PRATERIUM_IHR + " TEXT, "
                + COLUMN_INDIKATIV_PRATERIUM_SIE + " TEXT, "

                + COLUMN_INDIKATIV_PERFEKT_ICH + " TEXT, "
                + COLUMN_INDIKATIV_PERFEKT_DU + " TEXT, "
                + COLUMN_INDIKATIV_PERFEKT_ER + " TEXT, "
                + COLUMN_INDIKATIV_PERFEKT_WIR + " TEXT, "
                + COLUMN_INDIKATIV_PERFEKT_IHR + " TEXT, "
                + COLUMN_INDIKATIV_PERFEKT_SIE + " TEXT, "

                + COLUMN_INDIKATIV_PLUSQUAMPERFEKT_ICH + " TEXT, "
                + COLUMN_INDIKATIV_PLUSQUAMPERFEKT_DU + " TEXT, "
                + COLUMN_INDIKATIV_PLUSQUAMPERFEKT_ER + " TEXT, "
                + COLUMN_INDIKATIV_PLUSQUAMPERFEKT_WIR + " TEXT, "
                + COLUMN_INDIKATIV_PLUSQUAMPERFEKT_IHR + " TEXT, "
                + COLUMN_INDIKATIV_PLUSQUAMPERFEKT_SIE + " TEXT, "

                + COLUMN_INDIKATIV_FUTUR1_ICH + " TEXT, "
                + COLUMN_INDIKATIV_FUTUR1_DU + " TEXT, "
                + COLUMN_INDIKATIV_FUTUR1_ER + " TEXT, "
                + COLUMN_INDIKATIV_FUTUR1_WIR + " TEXT, "
                + COLUMN_INDIKATIV_FUTUR1_IHR + " TEXT, "
                + COLUMN_INDIKATIV_FUTUR1_SIE + " TEXT, "

                + COLUMN_INDIKATIV_FUTUR2_ICH + " TEXT, "
                + COLUMN_INDIKATIV_FUTUR2_DU + " TEXT, "
                + COLUMN_INDIKATIV_FUTUR2_ER + " TEXT, "
                + COLUMN_INDIKATIV_FUTUR2_WIR + " TEXT, "
                + COLUMN_INDIKATIV_FUTUR2_IHR + " TEXT, "
                + COLUMN_INDIKATIV_FUTUR2_SIE + " TEXT, "

                + COLUMN_KONJUNKTIV1_PRASENS_ICH + " TEXT, "
                + COLUMN_KONJUNKTIV1_PRASENS_DU + " TEXT, "
                + COLUMN_KONJUNKTIV1_PRASENS_ER + " TEXT, "
                + COLUMN_KONJUNKTIV1_PRASENS_WIR + " TEXT, "
                + COLUMN_KONJUNKTIV1_PRASENS_IHR + " TEXT, "
                + COLUMN_KONJUNKTIV1_PRASENS_SIE + " TEXT, "

                + COLUMN_KONJUNKTIV1_PERFEKT_ICH + " TEXT, "
                + COLUMN_KONJUNKTIV1_PERFEKT_DU + " TEXT, "
                + COLUMN_KONJUNKTIV1_PERFEKT_ER + " TEXT, "
                + COLUMN_KONJUNKTIV1_PERFEKT_WIR + " TEXT, "
                + COLUMN_KONJUNKTIV1_PERFEKT_IHR + " TEXT, "
                + COLUMN_KONJUNKTIV1_PERFEKT_SIE + " TEXT, "

                + COLUMN_KONJUNKTIV1_FUTUR1_ICH + " TEXT, "
                + COLUMN_KONJUNKTIV1_FUTUR1_DU + " TEXT, "
                + COLUMN_KONJUNKTIV1_FUTUR1_ER + " TEXT, "
                + COLUMN_KONJUNKTIV1_FUTUR1_WIR + " TEXT, "
                + COLUMN_KONJUNKTIV1_FUTUR1_IHR + " TEXT, "
                + COLUMN_KONJUNKTIV1_FUTUR1_SIE + " TEXT, "

                + COLUMN_KONJUNKTIV1_FUTUR2_ICH + " TEXT, "
                + COLUMN_KONJUNKTIV1_FUTUR2_DU + " TEXT, "
                + COLUMN_KONJUNKTIV1_FUTUR2_ER + " TEXT, "
                + COLUMN_KONJUNKTIV1_FUTUR2_WIR + " TEXT, "
                + COLUMN_KONJUNKTIV1_FUTUR2_IHR + " TEXT, "
                + COLUMN_KONJUNKTIV1_FUTUR2_SIE + " TEXT, "

                + COLUMN_KONJUNKTIV2_PRATERIUM_ICH + " TEXT, "
                + COLUMN_KONJUNKTIV2_PRATERIUM_DU + " TEXT, "
                + COLUMN_KONJUNKTIV2_PRATERIUM_ER + " TEXT, "
                + COLUMN_KONJUNKTIV2_PRATERIUM_WIR + " TEXT, "
                + COLUMN_KONJUNKTIV2_PRATERIUM_IHR + " TEXT, "
                + COLUMN_KONJUNKTIV2_PRATERIUM_SIE + " TEXT, "

                + COLUMN_KONJUNKTIV2_PLUSQUAMPERFEKT_ICH + " TEXT, "
                + COLUMN_KONJUNKTIV2_PLUSQUAMPERFEKT_DU + " TEXT, "
                + COLUMN_KONJUNKTIV2_PLUSQUAMPERFEKT_ER + " TEXT, "
                + COLUMN_KONJUNKTIV2_PLUSQUAMPERFEKT_WIR + " TEXT, "
                + COLUMN_KONJUNKTIV2_PLUSQUAMPERFEKT_IHR + " TEXT, "
                + COLUMN_KONJUNKTIV2_PLUSQUAMPERFEKT_SIE + " TEXT, "

                + COLUMN_KONJUNKTIV2_FUTUR1_ICH + " TEXT, "
                + COLUMN_KONJUNKTIV2_FUTUR1_DU + " TEXT, "
                + COLUMN_KONJUNKTIV2_FUTUR1_ER + " TEXT, "
                + COLUMN_KONJUNKTIV2_FUTUR1_WIR + " TEXT, "
                + COLUMN_KONJUNKTIV2_FUTUR1_IHR + " TEXT, "
                + COLUMN_KONJUNKTIV2_FUTUR1_SIE + " TEXT, "

                + COLUMN_KONJUNKTIV2_FUTUR2_ICH + " TEXT, "
                + COLUMN_KONJUNKTIV2_FUTUR2_DU + " TEXT, "
                + COLUMN_KONJUNKTIV2_FUTUR2_ER + " TEXT, "
                + COLUMN_KONJUNKTIV2_FUTUR2_WIR + " TEXT, "
                + COLUMN_KONJUNKTIV2_FUTUR2_IHR + " TEXT, "
                + COLUMN_KONJUNKTIV2_FUTUR2_SIE + " TEXT);")
    }


    /**
     * This is called when the database needs to be upgraded.
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion > newVersion) {
            // This should not happen, version numbers should increment. Start clean.
            db.execSQL("DROP TABLE IF EXISTS $VERBS_TBL")
            db.execSQL("DROP TABLE IF EXISTS $FAVORITES_TBL")
            db.execSQL("DROP TABLE IF EXISTS $CONJUGATION_TBL")
        }

        // Update version by version using a method for the update.
        val previousVersion = DATABASE_VERSION - 1
        when (oldVersion) {
            in 1 .. previousVersion -> {
                updateSchemaToCurrentVersion(db, DATABASE_VERSION)
            }
            else -> {}
        }
    }

    /**
     * Update from schema (1 .. 9) to schema 10.
     * Changes:
     *    schema02 = +50 verbs
     */
    private fun updateSchemaToCurrentVersion(db: SQLiteDatabase, version: Int) {
        // Save user verb colors, then recreate tables and add verbs and conjugations.
        val OLD_VERBS = "OLD_VERBS"
        val COLUMN_OLD_ID = "OLD_ID"
        val COLUMN_OLD_COLOR = "OLD_COLOR"

        // Save user colors
        db.execSQL("CREATE TABLE " + OLD_VERBS + " ("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_OLD_ID + " INTEGER NOT NULL, "
                + COLUMN_OLD_COLOR + " INTEGER NOT NULL DEFAULT 0)")
        db.execSQL("INSERT INTO " + OLD_VERBS + " ("
                + COLUMN_OLD_ID + "," + COLUMN_OLD_COLOR + ")"
                + " SELECT " + COLUMN_ID + ", " + COLUMN_COLOR
                + " FROM " + VERBS_TBL)

        // Remove tables and create them again
        db.execSQL("DROP TABLE IF EXISTS $VERBS_TBL")
        db.execSQL("DROP TABLE IF EXISTS $CONJUGATION_TBL")
        // Keep Favorites table as before, verb id's should never change.
        createTableVerbs(db)
        createTableConjugation(db)
        insertConjugation(db, version)
        insertVerbs(db, version)

        // Restore user colors
        db.execSQL("UPDATE " + VERBS_TBL
                + " SET " + COLUMN_COLOR + "="
                + "(SELECT " + COLUMN_OLD_COLOR
                + " FROM " + OLD_VERBS + " WHERE " + COLUMN_OLD_ID + " = " + COLUMN_ID + ")"
                + " WHERE " + COLUMN_ID + " IN "
                + "(SELECT " + COLUMN_OLD_ID
                + " FROM " + OLD_VERBS + " WHERE " + COLUMN_OLD_ID + " = " + COLUMN_ID + " )")
        db.execSQL("DROP TABLE IF EXISTS $OLD_VERBS")
    }


    /**
     * Insert the 5 most common verbs.
     * @param db SQLiteDatabase
     * @param version database schema version
     */
    private fun insertFavorites(db: SQLiteDatabase, version: Int) {
        when (version) {
            in 1 .. DATABASE_VERSION -> {
                insertFavoritesToSchema01(db)
            }
            else -> {}
        }
    }

    private fun insertFavoritesToSchema01(db: SQLiteDatabase) {
        val values = ContentValues()
        val updateValues = ContentValues()
        val FAVORITES_COLOR = "" + ContextCompat.getColor(context, R.color.colorDeepOrange)
        for (i in favorites.indices) {
            values.put("_id", i)
            values.put(COLUMN_ID, favorites[i][0])
            db.insertWithOnConflict(FAVORITES_TBL, null, values, CONFLICT_REPLACE)

            // Change color
            updateValues.put(COLUMN_COLOR, FAVORITES_COLOR)
            db.updateWithOnConflict(VERBS_TBL, updateValues,
                    "$COLUMN_ID = ?", arrayOf(Integer.toString(i)), CONFLICT_REPLACE)
        }
    }


    /**
     * Insert default verbs.
     * NOTE: If the resources change, add code for the upgrade also.
     * @param db SQLiteDatabase
     * @param version database schema version
     */
    private fun insertVerbs(db: SQLiteDatabase, version: Int) {
        when (version) {
            in 1 .. DATABASE_VERSION -> {
                insertVerbsToSchema01(db)
            }
            else -> {}
        }
    }

    private fun insertVerbsToSchema01(db: SQLiteDatabase) {
        val values = ContentValues()
        val DEFAULT_COLOR = "" + ContextCompat.getColor(context, R.color.colorBlack)
        val DEFAULT_SCORE = "0"

        // Initialize a XmlResourceParser instance
        val parser = context.resources.getXml(R.xml.verbs)
        var eventType = -1
        var i = 0
        var verbName: String
        var verbId: String
        try {
            // Loop through the XML data
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlResourceParser.START_TAG) {
                    val item = parser.name
                    if (item == "verb") {
                        values.put("_id", i)
                        verbId = parser.getAttributeValue(null, "id")
                        verbName = parser.getAttributeValue(null, "in")
                        values.put(COLUMN_ID, verbId)
                        values.put(COLUMN_CONJUGATION_NUMBER, parser.getAttributeValue(null, "ta"))
                        values.put(COLUMN_IMAGE, parser.getAttributeValue(null, "img"))
                        values.put(COLUMN_INFINITIV, verbName)
                        values.put(COLUMN_PARTIZIP_PERFEKT, parser.getAttributeValue(null, "pp"))
                        values.put(COLUMN_IMPERFEKT_ER, parser.getAttributeValue(null, "pahe"))
                        values.put(COLUMN_PRASENS_ER, parser.getAttributeValue(null, "prhe"))
                        values.put(COLUMN_SAMPLE_1, parser.getAttributeValue(null, "s1"))
                        values.put(COLUMN_SAMPLE_2, parser.getAttributeValue(null, "s2"))
                        values.put(COLUMN_SAMPLE_3, parser.getAttributeValue(null, "s3"))
                        values.put(COLUMN_COMMON, parser.getAttributeValue(null, "co"))
                        values.put(COLUMN_TYPE, parser.getAttributeValue(null, "type"))
                        values.put(COLUMN_COLOR, DEFAULT_COLOR)
                        values.put(COLUMN_SCORE, DEFAULT_SCORE)
                        values.put(COLUMN_DEFINITION, parser.getAttributeValue(null, "de"))
                        values.put(COLUMN_NOTES, parser.getAttributeValue(null, "no"))
                        values.put(COLUMN_TRANSLATION_EN, parser.getAttributeValue(null, "tren"))
                        values.put(COLUMN_TRANSLATION_FR, parser.getAttributeValue(null, "trfr"))
                        values.put(COLUMN_TRANSLATION_ES, parser.getAttributeValue(null, "tres"))
                        try {
                            db.insertWithOnConflict(VERBS_TBL, null, values, CONFLICT_REPLACE)
                        } catch (e: Exception) {
                            if (LOG) {
                                Log.e(TAG, "Error inserting verb: $verbId $verbName")
                            }
                            throw e
                        }
                        i++
                    }
                }
                eventType = parser.next()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            if (LOG) {
                Log.e(TAG, "Error loading verbs xml file. ")
            }
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
            if (LOG) {
                Log.e(TAG, "Error loading verbs xml file. ")
            }
        }
    }


    /**
     * Insert conjugation verb models.
     * @param db SQLiteDatabase
     * @param version database schema version
     */
    private fun insertConjugation(db: SQLiteDatabase, version: Int) {
        when (version) {
            in 1 .. DATABASE_VERSION -> {
                insertConjugationToSchema01(db)
            }
            else -> {}
        }
    }

    private fun insertConjugationToSchema01(db: SQLiteDatabase) {
        val values = ContentValues()

        // Initialize a XmlResourceParser instance
        val parser = context.resources.getXml(R.xml.conjugations)
        var eventType = -1
        var i = 1
        var verbName: String
        var conjugationId: String
        try {
            // Loop through the XML data
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlResourceParser.START_TAG) {
                    val item = parser.name
                    if (item == "conjugation") {
                        values.put("_id", i)
                        conjugationId = parser.getAttributeValue(null, "id")
                        verbName = parser.getAttributeValue(null, "inf_prasens")
                        values.put(COLUMN_ID, conjugationId)
                        values.put(COLUMN_TERMINATION, parser.getAttributeValue(null, "term"))
                        values.put(COLUMN_RADICALS, parser.getAttributeValue(null, "radicals"))

                        values.put(COLUMN_INFINITIV_PRASENS, verbName)
                        values.put(COLUMN_INFINITIV_PERFEKT, parser.getAttributeValue(null, "inf_perfekt"))
                        values.put(COLUMN_PARTIZIP_PRASENS, parser.getAttributeValue(null, "par_prasens"))
                        values.put(COLUMN_PARTIZIP_PERFEKT, parser.getAttributeValue(null, "par_perfekt"))

                        values.put(COLUMN_IMPERATIV_DU, parser.getAttributeValue(null, "imperativ_du"))
                        values.put(COLUMN_IMPERATIV_IHR, parser.getAttributeValue(null, "imperativ_ihr"))
                        values.put(COLUMN_IMPERATIV_SIE, parser.getAttributeValue(null, "imperativ_sie"))

                        values.put(COLUMN_INDIKATIV_PRASENS_ICH, parser.getAttributeValue(null, "ind_prasens_ich"))
                        values.put(COLUMN_INDIKATIV_PRASENS_DU, parser.getAttributeValue(null, "ind_prasens_du"))
                        values.put(COLUMN_INDIKATIV_PRASENS_ER, parser.getAttributeValue(null, "ind_prasens_er"))
                        values.put(COLUMN_INDIKATIV_PRASENS_WIR, parser.getAttributeValue(null, "ind_prasens_wir"))
                        values.put(COLUMN_INDIKATIV_PRASENS_IHR, parser.getAttributeValue(null, "ind_prasens_ihr"))
                        values.put(COLUMN_INDIKATIV_PRASENS_SIE, parser.getAttributeValue(null, "ind_prasens_sie"))

                        values.put(COLUMN_INDIKATIV_PRATERIUM_ICH, parser.getAttributeValue(null, "ind_prateritum_ich"))
                        values.put(COLUMN_INDIKATIV_PRATERIUM_DU, parser.getAttributeValue(null, "ind_prateritum_du"))
                        values.put(COLUMN_INDIKATIV_PRATERIUM_ER, parser.getAttributeValue(null, "ind_prateritum_er"))
                        values.put(COLUMN_INDIKATIV_PRATERIUM_WIR, parser.getAttributeValue(null, "ind_prateritum_wir"))
                        values.put(COLUMN_INDIKATIV_PRATERIUM_IHR, parser.getAttributeValue(null, "ind_prateritum_ihr"))
                        values.put(COLUMN_INDIKATIV_PRATERIUM_SIE, parser.getAttributeValue(null, "ind_prateritum_sie"))

                        values.put(COLUMN_INDIKATIV_PERFEKT_ICH, parser.getAttributeValue(null, "ind_perfekt_ich"))
                        values.put(COLUMN_INDIKATIV_PERFEKT_DU, parser.getAttributeValue(null, "ind_perfekt_du"))
                        values.put(COLUMN_INDIKATIV_PERFEKT_ER, parser.getAttributeValue(null, "ind_perfekt_er"))
                        values.put(COLUMN_INDIKATIV_PERFEKT_WIR, parser.getAttributeValue(null, "ind_perfekt_wir"))
                        values.put(COLUMN_INDIKATIV_PERFEKT_IHR, parser.getAttributeValue(null, "ind_perfekt_ihr"))
                        values.put(COLUMN_INDIKATIV_PERFEKT_SIE, parser.getAttributeValue(null, "ind_perfekt_sie"))

                        values.put(COLUMN_INDIKATIV_PLUSQUAMPERFEKT_ICH, parser.getAttributeValue(null, "ind_plusquamperfekt_ich"))
                        values.put(COLUMN_INDIKATIV_PLUSQUAMPERFEKT_DU, parser.getAttributeValue(null, "ind_plusquamperfekt_du"))
                        values.put(COLUMN_INDIKATIV_PLUSQUAMPERFEKT_ER, parser.getAttributeValue(null, "ind_plusquamperfekt_er"))
                        values.put(COLUMN_INDIKATIV_PLUSQUAMPERFEKT_WIR, parser.getAttributeValue(null, "ind_plusquamperfekt_wir"))
                        values.put(COLUMN_INDIKATIV_PLUSQUAMPERFEKT_IHR, parser.getAttributeValue(null, "ind_plusquamperfekt_ihr"))
                        values.put(COLUMN_INDIKATIV_PLUSQUAMPERFEKT_SIE, parser.getAttributeValue(null, "ind_plusquamperfekt_sie"))

                        values.put(COLUMN_INDIKATIV_FUTUR1_ICH, parser.getAttributeValue(null, "ind_futur1_ich"))
                        values.put(COLUMN_INDIKATIV_FUTUR1_DU, parser.getAttributeValue(null, "ind_futur1_du"))
                        values.put(COLUMN_INDIKATIV_FUTUR1_ER, parser.getAttributeValue(null, "ind_futur1_er"))
                        values.put(COLUMN_INDIKATIV_FUTUR1_WIR, parser.getAttributeValue(null, "ind_futur1_wir"))
                        values.put(COLUMN_INDIKATIV_FUTUR1_IHR, parser.getAttributeValue(null, "ind_futur1_ihr"))
                        values.put(COLUMN_INDIKATIV_FUTUR1_SIE, parser.getAttributeValue(null, "ind_futur1_sie"))

                        values.put(COLUMN_INDIKATIV_FUTUR2_ICH, parser.getAttributeValue(null, "ind_futur2_ich"))
                        values.put(COLUMN_INDIKATIV_FUTUR2_DU, parser.getAttributeValue(null, "ind_futur2_du"))
                        values.put(COLUMN_INDIKATIV_FUTUR2_ER, parser.getAttributeValue(null, "ind_futur2_er"))
                        values.put(COLUMN_INDIKATIV_FUTUR2_WIR, parser.getAttributeValue(null, "ind_futur2_wir"))
                        values.put(COLUMN_INDIKATIV_FUTUR2_IHR, parser.getAttributeValue(null, "ind_futur2_ihr"))
                        values.put(COLUMN_INDIKATIV_FUTUR2_SIE, parser.getAttributeValue(null, "ind_futur2_sie"))

                        values.put(COLUMN_KONJUNKTIV1_PRASENS_ICH, parser.getAttributeValue(null, "kon1_prasens_ich"))
                        values.put(COLUMN_KONJUNKTIV1_PRASENS_DU, parser.getAttributeValue(null, "kon1_prasens_du"))
                        values.put(COLUMN_KONJUNKTIV1_PRASENS_ER, parser.getAttributeValue(null, "kon1_prasens_er"))
                        values.put(COLUMN_KONJUNKTIV1_PRASENS_WIR, parser.getAttributeValue(null, "kon1_prasens_wir"))
                        values.put(COLUMN_KONJUNKTIV1_PRASENS_IHR, parser.getAttributeValue(null, "kon1_prasens_ihr"))
                        values.put(COLUMN_KONJUNKTIV1_PRASENS_SIE, parser.getAttributeValue(null, "kon1_prasens_sie"))

                        values.put(COLUMN_KONJUNKTIV1_PERFEKT_ICH, parser.getAttributeValue(null, "kon1_perfekt_ich"))
                        values.put(COLUMN_KONJUNKTIV1_PERFEKT_DU, parser.getAttributeValue(null, "kon1_perfekt_du"))
                        values.put(COLUMN_KONJUNKTIV1_PERFEKT_ER, parser.getAttributeValue(null, "kon1_perfekt_er"))
                        values.put(COLUMN_KONJUNKTIV1_PERFEKT_WIR, parser.getAttributeValue(null, "kon1_perfekt_wir"))
                        values.put(COLUMN_KONJUNKTIV1_PERFEKT_IHR, parser.getAttributeValue(null, "kon1_perfekt_ihr"))
                        values.put(COLUMN_KONJUNKTIV1_PERFEKT_SIE, parser.getAttributeValue(null, "kon1_perfekt_sie"))

                        values.put(COLUMN_KONJUNKTIV1_FUTUR1_ICH, parser.getAttributeValue(null, "kon1_futur1_ich"))
                        values.put(COLUMN_KONJUNKTIV1_FUTUR1_DU, parser.getAttributeValue(null, "kon1_futur1_du"))
                        values.put(COLUMN_KONJUNKTIV1_FUTUR1_ER, parser.getAttributeValue(null, "kon1_futur1_er"))
                        values.put(COLUMN_KONJUNKTIV1_FUTUR1_WIR, parser.getAttributeValue(null, "kon1_futur1_wir"))
                        values.put(COLUMN_KONJUNKTIV1_FUTUR1_IHR, parser.getAttributeValue(null, "kon1_futur1_ihr"))
                        values.put(COLUMN_KONJUNKTIV1_FUTUR1_SIE, parser.getAttributeValue(null, "kon1_futur1_sie"))

                        values.put(COLUMN_KONJUNKTIV1_FUTUR2_ICH, parser.getAttributeValue(null, "kon1_futur2_ich"))
                        values.put(COLUMN_KONJUNKTIV1_FUTUR2_DU, parser.getAttributeValue(null, "kon1_futur2_du"))
                        values.put(COLUMN_KONJUNKTIV1_FUTUR2_ER, parser.getAttributeValue(null, "kon1_futur2_er"))
                        values.put(COLUMN_KONJUNKTIV1_FUTUR2_WIR, parser.getAttributeValue(null, "kon1_futur2_wir"))
                        values.put(COLUMN_KONJUNKTIV1_FUTUR2_IHR, parser.getAttributeValue(null, "kon1_futur2_ihr"))
                        values.put(COLUMN_KONJUNKTIV1_FUTUR2_SIE, parser.getAttributeValue(null, "kon1_futur2_sie"))

                        values.put(COLUMN_KONJUNKTIV2_PRATERIUM_ICH, parser.getAttributeValue(null, "kon2_prateritum_ich"))
                        values.put(COLUMN_KONJUNKTIV2_PRATERIUM_DU, parser.getAttributeValue(null, "kon2_prateritum_du"))
                        values.put(COLUMN_KONJUNKTIV2_PRATERIUM_ER, parser.getAttributeValue(null, "kon2_prateritum_er"))
                        values.put(COLUMN_KONJUNKTIV2_PRATERIUM_WIR, parser.getAttributeValue(null, "kon2_prateritum_wir"))
                        values.put(COLUMN_KONJUNKTIV2_PRATERIUM_IHR, parser.getAttributeValue(null, "kon2_prateritum_ihr"))
                        values.put(COLUMN_KONJUNKTIV2_PRATERIUM_SIE, parser.getAttributeValue(null, "kon2_prateritum_sie"))

                        values.put(COLUMN_KONJUNKTIV2_PLUSQUAMPERFEKT_ICH, parser.getAttributeValue(null, "kon2_plusquamperfekt_ich"))
                        values.put(COLUMN_KONJUNKTIV2_PLUSQUAMPERFEKT_DU, parser.getAttributeValue(null, "kon2_plusquamperfekt_du"))
                        values.put(COLUMN_KONJUNKTIV2_PLUSQUAMPERFEKT_ER, parser.getAttributeValue(null, "kon2_plusquamperfekt_er"))
                        values.put(COLUMN_KONJUNKTIV2_PLUSQUAMPERFEKT_WIR, parser.getAttributeValue(null, "kon2_plusquamperfekt_wir"))
                        values.put(COLUMN_KONJUNKTIV2_PLUSQUAMPERFEKT_IHR, parser.getAttributeValue(null, "kon2_plusquamperfekt_ihr"))
                        values.put(COLUMN_KONJUNKTIV2_PLUSQUAMPERFEKT_SIE, parser.getAttributeValue(null, "kon2_plusquamperfekt_sie"))

                        values.put(COLUMN_KONJUNKTIV2_FUTUR1_ICH, parser.getAttributeValue(null, "kon2_futur1_ich"))
                        values.put(COLUMN_KONJUNKTIV2_FUTUR1_DU, parser.getAttributeValue(null, "kon2_futur1_du"))
                        values.put(COLUMN_KONJUNKTIV2_FUTUR1_ER, parser.getAttributeValue(null, "kon2_futur1_er"))
                        values.put(COLUMN_KONJUNKTIV2_FUTUR1_WIR, parser.getAttributeValue(null, "kon2_futur1_wir"))
                        values.put(COLUMN_KONJUNKTIV2_FUTUR1_IHR, parser.getAttributeValue(null, "kon2_futur1_ihr"))
                        values.put(COLUMN_KONJUNKTIV2_FUTUR1_SIE, parser.getAttributeValue(null, "kon2_futur1_sie"))

                        values.put(COLUMN_KONJUNKTIV2_FUTUR2_ICH, parser.getAttributeValue(null, "kon2_futur2_ich"))
                        values.put(COLUMN_KONJUNKTIV2_FUTUR2_DU, parser.getAttributeValue(null, "kon2_futur2_du"))
                        values.put(COLUMN_KONJUNKTIV2_FUTUR2_ER, parser.getAttributeValue(null, "kon2_futur2_er"))
                        values.put(COLUMN_KONJUNKTIV2_FUTUR2_WIR, parser.getAttributeValue(null, "kon2_futur2_wir"))
                        values.put(COLUMN_KONJUNKTIV2_FUTUR2_IHR, parser.getAttributeValue(null, "kon2_futur2_ihr"))
                        values.put(COLUMN_KONJUNKTIV2_FUTUR2_SIE, parser.getAttributeValue(null, "kon2_futur2_sie"))

                        try {
                            db.insertWithOnConflict(CONJUGATION_TBL, null, values, CONFLICT_REPLACE)
                        } catch (e: Exception) {
                            if (LOG) {
                                Log.e(TAG, "Error inserting conjugation: $conjugationId $verbName")
                            }
                            throw e
                        }
                        i++
                    }
                }
                eventType = parser.next()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            if (LOG) {
                Log.e(TAG, "Error loading conjugations xml file. ")
            }
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
            if (LOG) {
                Log.e(TAG, "Error loading conjugations xml file. ")
            }
        }
    }

}
