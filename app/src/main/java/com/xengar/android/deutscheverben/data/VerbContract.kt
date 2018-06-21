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

import android.content.ContentResolver
import android.net.Uri
import android.provider.BaseColumns

/**
 * API Contract for the Verbs
 */

object VerbContract {

    @JvmField val CONTENT_AUTHORITY = "com.xengar.android.deutscheverben"

    private val BASE_CONTENT_URI = Uri.parse("content://$CONTENT_AUTHORITY")

    @JvmField val PATH_VERBS = "verbs"
    @JvmField val PATH_CONJUGATIONS = "conjugations"
    @JvmField val PATH_FAVORITES = "favorites"
    @JvmField val PATH_FAVORITE_VERBS = "favorite_verbs"

    /**
     * Inner class that defines constant values for the verbs database table.
     * Each entry in the table represents a single verb.
     */
    class VerbEntry : BaseColumns {
        companion object {
            /** The content URI to access the verb data in the provider  */
            @JvmField val CONTENT_VERBS_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_VERBS)!!
            @JvmField val CONTENT_CONJUGATIONS_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_CONJUGATIONS)!!

            @JvmField val CONTENT_FAVORITES_URI = Uri.withAppendedPath(BASE_CONTENT_URI,
                    PATH_FAVORITES)!!
            @JvmField val CONTENT_FAVORITE_VERBS_URI = Uri.withAppendedPath(BASE_CONTENT_URI,
                    PATH_FAVORITE_VERBS)!!

            /** The MIME type of the [.CONTENT_VERBS_URI] for a list of verbs.  */
            @JvmField val CONTENT_LIST_TYPE_VERB =
                    ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VERBS
            @JvmField val CONTENT_LIST_TYPE_CONJUGATION =
                    ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONJUGATIONS
            @JvmField val CONTENT_LIST_TYPE_FAVORITE =
                    ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITES

            /** The MIME type of the [.CONTENT_VERBS_URI] for a single verb.  */
            @JvmField val CONTENT_ITEM_TYPE_VERB =
                    ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VERBS
            @JvmField val CONTENT_ITEM_TYPE_CONJUGATION =
                    ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONJUGATIONS
            @JvmField val CONTENT_ITEM_TYPE_FAVORITE =
                    ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITES

            /** Name of database table for verbs  */
            @JvmField val VERBS_TBL = "VERBS_TBL"
            @JvmField val FAVORITES_TBL = "FAVORITES_TBL"
            @JvmField val CONJUGATION_TBL = "CONJUGATION_TBL"

            /** Unique ID number for the verb (only for use in the database table). - Type: INTEGER  */
            @JvmField val _ID = BaseColumns._ID
            @JvmField val COLUMN_ID = "ID"
            @JvmField val COLUMN_IMAGE = "IMAGE"

            /** Table of Conjugation number - Type: TEXT  */
            @JvmField val COLUMN_CONJUGATION_NUMBER = "CONJUGATION_ID"

            /** Table Verbs - Type: TEXT   */
            @JvmField val COLUMN_INFINITIV = "INFINITIV"
            @JvmField val COLUMN_IMPERFEKT_ER = "IMPERFEKT_ER"
            @JvmField val COLUMN_PRASENS_ER = "PRASENS_ER"

            /** Common usage of the verb. - Type: INTEGER  */
            @JvmField val COLUMN_COMMON = "COMMON"

            /** Possible values for the common usage of the verb.  */
            @JvmField val TOP_25 = 1
            @JvmField val TOP_50 = 2
            @JvmField val TOP_100 = 3
            @JvmField val TOP_250 = 4
            @JvmField val TOP_500 = 5
            @JvmField val TOP_1000 = 6
            @JvmField val OTHER = 99
            @JvmField val S_TOP_25 = "" + TOP_25
            @JvmField val S_TOP_50 = "" + TOP_50
            @JvmField val S_TOP_100 = "" + TOP_100
            @JvmField val S_TOP_250 = "" + TOP_250
            @JvmField val S_TOP_500 = "" + TOP_500
            @JvmField val S_TOP_1000 = "" + TOP_1000

            fun isValidCommonUsage(usage: Int): Boolean {
                return (usage == TOP_25 || usage == TOP_50 || usage == TOP_100 || usage == TOP_250
                        || usage == TOP_500 || usage == TOP_1000 || usage == OTHER)
            }

            /** Group verbs (1st, 2nd, 3rd, all). - Type: INTEGER  */
            @JvmField val COLUMN_GROUP = "GROUPE"

            /** Possible values for verb groups.  */
            @JvmField val GROUP_1 = 1
            @JvmField val GROUP_2 = 2
            @JvmField val GROUP_3 = 3
            @JvmField val GROUP_ALL = 0

            fun isValidGroup(value: Int): Boolean {
                return value == GROUP_1 || value == GROUP_2
                        || value == GROUP_3 || value == GROUP_ALL
            }

            /** Definition of the verb. - Type: TEXT  */
            @JvmField val COLUMN_DEFINITION = "DEFINITION"

            /** Examples of the verb. - Type: TEXT   */
            @JvmField val COLUMN_SAMPLE_1 = "SAMPLE1"
            @JvmField val COLUMN_SAMPLE_2 = "SAMPLE2"
            @JvmField val COLUMN_SAMPLE_3 = "SAMPLE3"

            /** Other information for the verb. - Type: TEXT  */
            @JvmField val COLUMN_NOTES = "NOTES"

            /** Color assigned by the user. - Type: INTEGER  */
            @JvmField val COLUMN_COLOR = "COLOR"

            /** Score assigned by using the exercises or tests. - Type: INTEGER  */
            @JvmField val COLUMN_SCORE = "SCORE"

            /**
             * Translations of the verb into the language.
             * Type: TEXT
             */
            @JvmField val COLUMN_TRANSLATION_EN = "TRANSLATION_EN"
            @JvmField val COLUMN_TRANSLATION_FR = "TRANSLATION_FR"
            @JvmField val COLUMN_TRANSLATION_ES = "TRANSLATION_ES"


            /**
             * Verb CONJUGATION columns.
             * Type: TEXT
             */
            @JvmField val COLUMN_TERMINATION = "TERMINATION"
            @JvmField val COLUMN_RADICALS = "RADICALS"

            @JvmField val COLUMN_INFINITIV_PRASENS = "INFINITIV_PRASENS"
            @JvmField val COLUMN_INFINITIV_PERFEKT = "INFINITIV_PERFEKT"
            @JvmField val COLUMN_PARTIZIP_PRASENS = "PARTIZIP_PRASENS"
            @JvmField val COLUMN_PARTIZIP_PERFEKT = "PARTIZIP_PERFEKT"

            @JvmField val COLUMN_IMPERATIV_DU = "IMPERATIV_DU"
            @JvmField val COLUMN_IMPERATIV_IHR = "IMPERATIV_IHR"
            @JvmField val COLUMN_IMPERATIV_SIE = "IMPERATIV_SIE"

            @JvmField val COLUMN_INDIKATIV_PRASENS_ICH = "INDIKATIV_PRASENS_ICH"
            @JvmField val COLUMN_INDIKATIV_PRASENS_DU = "INDIKATIV_PRASENS_DU"
            @JvmField val COLUMN_INDIKATIV_PRASENS_ER = "INDIKATIV_PRASENS_ER"
            @JvmField val COLUMN_INDIKATIV_PRASENS_WIR = "INDIKATIV_PRASENS_WIR"
            @JvmField val COLUMN_INDIKATIV_PRASENS_IHR = "INDIKATIV_PRASENS_IHR"
            @JvmField val COLUMN_INDIKATIV_PRASENS_SIE = "INDIKATIV_PRASENS_SIE"

            @JvmField val COLUMN_INDIKATIV_PRATERIUM_ICH = "INDIKATIV_PRATERIUM_ICH"
            @JvmField val COLUMN_INDIKATIV_PRATERIUM_DU = "INDIKATIV_PRATERIUM_DU"
            @JvmField val COLUMN_INDIKATIV_PRATERIUM_ER = "INDIKATIV_PRATERIUM_ER"
            @JvmField val COLUMN_INDIKATIV_PRATERIUM_WIR = "INDIKATIV_PRATERIUM_WIR"
            @JvmField val COLUMN_INDIKATIV_PRATERIUM_IHR = "INDIKATIV_PRATERIUM_IHR"
            @JvmField val COLUMN_INDIKATIV_PRATERIUM_SIE = "INDIKATIV_PRATERIUM_SIE"

            @JvmField val COLUMN_INDIKATIV_PERFEKT_ICH = "INDIKATIV_PERFEKT_ICH"
            @JvmField val COLUMN_INDIKATIV_PERFEKT_DU = "INDIKATIV_PERFEKT_DU"
            @JvmField val COLUMN_INDIKATIV_PERFEKT_ER = "INDIKATIV_PERFEKT_ER"
            @JvmField val COLUMN_INDIKATIV_PERFEKT_WIR = "INDIKATIV_PERFEKT_WIR"
            @JvmField val COLUMN_INDIKATIV_PERFEKT_IHR = "INDIKATIV_PERFEKT_IHR"
            @JvmField val COLUMN_INDIKATIV_PERFEKT_SIE = "INDIKATIV_PERFEKT_SIE"

            @JvmField val COLUMN_INDIKATIV_PLUSQUAMPERFEKT_ICH = "INDIKATIV_PLUSQUAMPERFEKT_ICH"
            @JvmField val COLUMN_INDIKATIV_PLUSQUAMPERFEKT_DU = "INDIKATIV_PLUSQUAMPERFEKT_DU"
            @JvmField val COLUMN_INDIKATIV_PLUSQUAMPERFEKT_ER = "INDIKATIV_PLUSQUAMPERFEKT_ER"
            @JvmField val COLUMN_INDIKATIV_PLUSQUAMPERFEKT_WIR = "INDIKATIV_PLUSQUAMPERFEKT_WIR"
            @JvmField val COLUMN_INDIKATIV_PLUSQUAMPERFEKT_IHR = "INDIKATIV_PLUSQUAMPERFEKT_IHR"
            @JvmField val COLUMN_INDIKATIV_PLUSQUAMPERFEKT_SIE = "INDIKATIV_PLUSQUAMPERFEKT_SIE"

            @JvmField val COLUMN_INDIKATIV_FUTUR1_ICH = "INDIKATIV_FUTUR1_ICH"
            @JvmField val COLUMN_INDIKATIV_FUTUR1_DU = "INDIKATIV_FUTUR1_DU"
            @JvmField val COLUMN_INDIKATIV_FUTUR1_ER = "INDIKATIV_FUTUR1_ER"
            @JvmField val COLUMN_INDIKATIV_FUTUR1_WIR = "INDIKATIV_FUTUR1_WIR"
            @JvmField val COLUMN_INDIKATIV_FUTUR1_IHR = "INDIKATIV_FUTUR1_IHR"
            @JvmField val COLUMN_INDIKATIV_FUTUR1_SIE = "INDIKATIV_FUTUR1_SIE"

            @JvmField val COLUMN_INDIKATIV_FUTUR2_ICH = "INDIKATIV_FUTUR2_ICH"
            @JvmField val COLUMN_INDIKATIV_FUTUR2_DU = "INDIKATIV_FUTUR2_DU"
            @JvmField val COLUMN_INDIKATIV_FUTUR2_ER = "INDIKATIV_FUTUR2_ER"
            @JvmField val COLUMN_INDIKATIV_FUTUR2_WIR = "INDIKATIV_FUTUR2_WIR"
            @JvmField val COLUMN_INDIKATIV_FUTUR2_IHR = "INDIKATIV_FUTUR2_IHR"
            @JvmField val COLUMN_INDIKATIV_FUTUR2_SIE = "INDIKATIV_FUTUR2_SIE"

            @JvmField val COLUMN_KONJUNKTIV1_PRASENS_ICH = "KONJUNKTIV1_PRASENS_ICH"
            @JvmField val COLUMN_KONJUNKTIV1_PRASENS_DU = "KONJUNKTIV1_PRASENS_DU"
            @JvmField val COLUMN_KONJUNKTIV1_PRASENS_ER = "KONJUNKTIV1_PRASENS_ER"
            @JvmField val COLUMN_KONJUNKTIV1_PRASENS_WIR = "KONJUNKTIV1_PRASENS_WIR"
            @JvmField val COLUMN_KONJUNKTIV1_PRASENS_IHR = "KONJUNKTIV1_PRASENS_IHR"
            @JvmField val COLUMN_KONJUNKTIV1_PRASENS_SIE = "KONJUNKTIV1_PRASENS_SIE"

            @JvmField val COLUMN_KONJUNKTIV1_PERFEKT_ICH = "KONJUNKTIV1_PERFEKT_ICH"
            @JvmField val COLUMN_KONJUNKTIV1_PERFEKT_DU = "KONJUNKTIV1_PERFEKT_DU"
            @JvmField val COLUMN_KONJUNKTIV1_PERFEKT_ER = "KONJUNKTIV1_PERFEKT_ER"
            @JvmField val COLUMN_KONJUNKTIV1_PERFEKT_WIR = "KONJUNKTIV1_PERFEKT_WIR"
            @JvmField val COLUMN_KONJUNKTIV1_PERFEKT_IHR = "KONJUNKTIV1_PERFEKT_IHR"
            @JvmField val COLUMN_KONJUNKTIV1_PERFEKT_SIE = "KONJUNKTIV1_PERFEKT_SIE"

            @JvmField val COLUMN_KONJUNKTIV1_FUTUR1_ICH = "KONJUNKTIV1_FUTUR1_ICH"
            @JvmField val COLUMN_KONJUNKTIV1_FUTUR1_DU = "KONJUNKTIV1_FUTUR1_DU"
            @JvmField val COLUMN_KONJUNKTIV1_FUTUR1_ER = "KONJUNKTIV1_FUTUR1_ER"
            @JvmField val COLUMN_KONJUNKTIV1_FUTUR1_WIR = "KONJUNKTIV1_FUTUR1_WIR"
            @JvmField val COLUMN_KONJUNKTIV1_FUTUR1_IHR = "KONJUNKTIV1_FUTUR1_IHR"
            @JvmField val COLUMN_KONJUNKTIV1_FUTUR1_SIE = "KONJUNKTIV1_FUTUR1_SIE"

            @JvmField val COLUMN_KONJUNKTIV1_FUTUR2_ICH = "KONJUNKTIV1_FUTUR2_ICH"
            @JvmField val COLUMN_KONJUNKTIV1_FUTUR2_DU = "KONJUNKTIV1_FUTUR2_DU"
            @JvmField val COLUMN_KONJUNKTIV1_FUTUR2_ER = "KONJUNKTIV1_FUTUR2_ER"
            @JvmField val COLUMN_KONJUNKTIV1_FUTUR2_WIR = "KONJUNKTIV1_FUTUR2_WIR"
            @JvmField val COLUMN_KONJUNKTIV1_FUTUR2_IHR = "KONJUNKTIV1_FUTUR2_IHR"
            @JvmField val COLUMN_KONJUNKTIV1_FUTUR2_SIE = "KONJUNKTIV1_FUTUR2_SIE"

            @JvmField val COLUMN_KONJUNKTIV2_PRATERIUM_ICH = "KONJUNKTIV2_PRATERIUM_ICH"
            @JvmField val COLUMN_KONJUNKTIV2_PRATERIUM_DU = "KONJUNKTIV2_PRATERIUM_DU"
            @JvmField val COLUMN_KONJUNKTIV2_PRATERIUM_ER = "KONJUNKTIV2_PRATERIUM_ER"
            @JvmField val COLUMN_KONJUNKTIV2_PRATERIUM_WIR = "KONJUNKTIV2_PRATERIUM_WIR"
            @JvmField val COLUMN_KONJUNKTIV2_PRATERIUM_IHR = "KONJUNKTIV2_PRATERIUM_IHR"
            @JvmField val COLUMN_KONJUNKTIV2_PRATERIUM_SIE = "KONJUNKTIV2_PRATERIUM_SIE"

            @JvmField val COLUMN_KONJUNKTIV2_PLUSQUAMPERFEKT_ICH = "KONJUNKTIV2_PLUSQUAMPERFEKT_ICH"
            @JvmField val COLUMN_KONJUNKTIV2_PLUSQUAMPERFEKT_DU = "KONJUNKTIV2_PLUSQUAMPERFEKT_DU"
            @JvmField val COLUMN_KONJUNKTIV2_PLUSQUAMPERFEKT_ER = "KONJUNKTIV2_PLUSQUAMPERFEKT_ER"
            @JvmField val COLUMN_KONJUNKTIV2_PLUSQUAMPERFEKT_WIR = "KONJUNKTIV2_PLUSQUAMPERFEKT_WIR"
            @JvmField val COLUMN_KONJUNKTIV2_PLUSQUAMPERFEKT_IHR = "KONJUNKTIV2_PLUSQUAMPERFEKT_IHR"
            @JvmField val COLUMN_KONJUNKTIV2_PLUSQUAMPERFEKT_SIE = "KONJUNKTIV2_PLUSQUAMPERFEKT_SIE"

            @JvmField val COLUMN_KONJUNKTIV2_FUTUR1_ICH = "KONJUNKTIV2_FUTUR1_ICH"
            @JvmField val COLUMN_KONJUNKTIV2_FUTUR1_DU = "KONJUNKTIV2_FUTUR1_DU"
            @JvmField val COLUMN_KONJUNKTIV2_FUTUR1_ER = "KONJUNKTIV2_FUTUR1_ER"
            @JvmField val COLUMN_KONJUNKTIV2_FUTUR1_WIR = "KONJUNKTIV2_FUTUR1_WIR"
            @JvmField val COLUMN_KONJUNKTIV2_FUTUR1_IHR = "KONJUNKTIV2_FUTUR1_IHR"
            @JvmField val COLUMN_KONJUNKTIV2_FUTUR1_SIE = "KONJUNKTIV2_FUTUR1_SIE"

            @JvmField val COLUMN_KONJUNKTIV2_FUTUR2_ICH = "KONJUNKTIV2_FUTUR2_ICH"
            @JvmField val COLUMN_KONJUNKTIV2_FUTUR2_DU = "KONJUNKTIV2_FUTUR2_DU"
            @JvmField val COLUMN_KONJUNKTIV2_FUTUR2_ER = "KONJUNKTIV2_FUTUR2_ER"
            @JvmField val COLUMN_KONJUNKTIV2_FUTUR2_WIR = "KONJUNKTIV2_FUTUR2_WIR"
            @JvmField val COLUMN_KONJUNKTIV2_FUTUR2_IHR = "KONJUNKTIV2_FUTUR2_IHR"
            @JvmField val COLUMN_KONJUNKTIV2_FUTUR2_SIE = "KONJUNKTIV2_FUTUR2_SIE"
        }
    }

}// To prevent someone from accidentally instantiating the contract class,
// give it an empty constructor.
