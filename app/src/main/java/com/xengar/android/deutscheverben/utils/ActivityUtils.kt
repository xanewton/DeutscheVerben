/*
 * Copyright (C) 2018 Angel Newton
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
package com.xengar.android.deutscheverben.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceActivity
import android.preference.PreferenceManager
import android.speech.tts.TextToSpeech
import android.support.annotation.RequiresApi
import android.support.v4.app.ShareCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatDrawableManager
import android.text.Html
import android.text.Spanned
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import com.xengar.android.deutscheverben.BuildConfig
import com.xengar.android.deutscheverben.R
import com.xengar.android.deutscheverben.data.Conjugation
import com.xengar.android.deutscheverben.data.Verb
import com.xengar.android.deutscheverben.sync.AlarmReceiver
import com.xengar.android.deutscheverben.sync.JobSchedulerService
import com.xengar.android.deutscheverben.ui.DetailsActivity
import com.xengar.android.deutscheverben.ui.HelpActivity
import com.xengar.android.deutscheverben.ui.SearchActivity
import com.xengar.android.deutscheverben.ui.SettingsActivity

import android.app.AlarmManager.INTERVAL_HOUR
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.util.Log
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
import com.xengar.android.deutscheverben.utils.Constants.CONJUGATION_ID
import com.xengar.android.deutscheverben.utils.Constants.DEFAULT_FONT_SIZE
import com.xengar.android.deutscheverben.utils.Constants.DEFAULT_TTS_LOCALE
import com.xengar.android.deutscheverben.utils.Constants.DEMO_MODE
import com.xengar.android.deutscheverben.utils.Constants.ENGLISH
import com.xengar.android.deutscheverben.utils.Constants.FAVORITES
import com.xengar.android.deutscheverben.utils.Constants.LIST
import com.xengar.android.deutscheverben.utils.Constants.LOG
import com.xengar.android.deutscheverben.utils.Constants.MOST_COMMON_100
import com.xengar.android.deutscheverben.utils.Constants.MOST_COMMON_1000
import com.xengar.android.deutscheverben.utils.Constants.MOST_COMMON_25
import com.xengar.android.deutscheverben.utils.Constants.MOST_COMMON_250
import com.xengar.android.deutscheverben.utils.Constants.MOST_COMMON_50
import com.xengar.android.deutscheverben.utils.Constants.MOST_COMMON_500
import com.xengar.android.deutscheverben.utils.Constants.MOST_COMMON_ALL
import com.xengar.android.deutscheverben.utils.Constants.NONE
import com.xengar.android.deutscheverben.utils.Constants.FRENCH
import com.xengar.android.deutscheverben.utils.Constants.PREF_PREFERRED_TTS_LOCALE
import com.xengar.android.deutscheverben.utils.Constants.PREF_VERSION_CODE_KEY
import com.xengar.android.deutscheverben.utils.Constants.SHARED_PREF_NAME
import com.xengar.android.deutscheverben.utils.Constants.SPANISH
import com.xengar.android.deutscheverben.utils.Constants.TYPE_START_NOTIFICATIONS
import com.xengar.android.deutscheverben.utils.Constants.TYPE_VERB_NOTIFICATION
import com.xengar.android.deutscheverben.utils.Constants.USE_TEST_ADS
import com.xengar.android.deutscheverben.utils.Constants.USE_TEST_ALARM_INTERVALS
import com.xengar.android.deutscheverben.utils.Constants.VERB_ID
import com.xengar.android.deutscheverben.utils.Constants.VERB_NAME
import java.util.*


/**
 * ActivityUtils. To handle common tasks.
 */
object ActivityUtils {

    private val TAG = ActivityUtils::class.java.simpleName

    /**
     * Saves the variable into Preferences.
     * @param context context
     * @param name name of preference
     * @param value value
     */
    fun saveIntToPreferences(context: Context, name: String, value: Int) {
        val prefs = context.getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE)
        val e = prefs.edit()
        e.putInt(name, value)
        e.commit()
    }

    /**
     * Saves the variable into Preferences.
     * @param context context
     * @param name name of preference
     * @param value value
     */
    fun saveLongToPreferences(context: Context, name: String, value: Long) {
        val prefs = context.getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE)
        val e = prefs.edit()
        e.putLong(name, value)
        e.commit()
    }

    /**
     * Saves the variable into Preferences.
     * @param context context
     * @param name name of preference
     * @param value value
     */
    fun saveBooleanToPreferences(context: Context, name: String, value: Boolean) {
        val prefs = context.getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE)
        val e = prefs.edit()
        e.putBoolean(name, value)
        e.commit()
    }

    /**
     * Saves the variable into Preferences.
     * @param context context
     * @param name name of preference
     * @param value value
     */
    fun saveStringToPreferences(context: Context, name: String, value: String) {
        val prefs = context.getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE)
        val e = prefs.edit()
        e.putString(name, value)
        e.commit()
    }

    /**
     * Launches Details Activity.
     * @param context context
     * @param id verb id
     * @param cId conjugation id
     * @param verb verb name
     * @param demoMode demo
     */
    fun launchDetailsActivity(context: Context, id: Long, cId: Long, verb: String,
                              demoMode: Boolean) {
        val intent = Intent(context, DetailsActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val bundle = Bundle()
        bundle.putString(VERB_NAME, verb)
        bundle.putLong(VERB_ID, id)
        bundle.putLong(CONJUGATION_ID, cId)
        bundle.putBoolean(DEMO_MODE, demoMode)
        intent.putExtras(bundle)

        context.startActivity(intent)
    }

    /**
     * Launches Help Activity.
     * @param context context
     */
    fun launchHelpActivity(context: Context) {
        val intent = Intent(context, HelpActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /**
     * Launches Settings Activity.
     * @param context context
     */
    fun launchSettingsActivity(context: Context) {
        val intent = Intent(context, SettingsActivity::class.java)
        intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT,
                SettingsActivity.GeneralPreferenceFragment::class.java.name)
        intent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true)
        intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT_TITLE, R.string.settings)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /**
     * Launches Search Activity.
     * @param context context
     */
    fun launchSearchActivity(context: Context) {
        val intent = Intent(context, SearchActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /**
     * Checks if the intent is callable.
     * @param context context
     * @param intent Intent
     */
    fun isIntentCallable(context: Context, intent: Intent): Boolean {
        val list: List<ResolveInfo> = context.packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY)
        return list.isNotEmpty()
    }

    /**
     * Helper class to handle deprecated method.
     * Source: http://stackoverflow.com/questions/37904739/html-fromhtml-deprecated-in-android-n
     * @param html html string
     * @return Spanned
     */
    fun fromHtml(html: String): Spanned {
        val result: Spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(html)
        }
        return result
    }

    /**
     * Returns the value of show definitions from preferences.
     * @param context context
     * @return boolean or default(true)
     */
    fun getPreferenceShowDefinitions(context: Context): Boolean {
        val key = context.getString(R.string.pref_show_definitions_switch)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(key, true)
    }

    /**
     * Returns the value of show definitions from preferences.
     * @param context context
     * @return boolean or default(true)
     */
    fun getPreferenceFontSize(context: Context): String {
        val key = context.getString(R.string.pref_font_size)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(key, DEFAULT_FONT_SIZE)
    }

    /**
     * Returns the translation language from preferences.
     * @param context Context
     * @return code of language (default NONE)
     */
    fun getPreferenceTranslationLanguage(context: Context): String {
        val key = context.getString(R.string.pref_translation_language)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val lang = prefs.getString(key, "None")
        return when (lang) {
            "", "None" -> NONE
            "en_EN" -> ENGLISH
            "fr_FR" -> FRENCH
            "es_ES" -> SPANISH
            else -> NONE
        }
    }

    /**
     * Returns the favorites mode from preferences.
     * @param context context
     * @return CARD or LIST
     */
    fun getPreferenceFavoritesMode(context: Context): String {
        val key = context.getString(R.string.pref_favorite_mode_list)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(key, LIST)
    }


    /**
     * Returns the value of enable verb notifications from preferences.
     * @param context context
     * @return boolean or default(true)
     */
    fun getPreferenceEnableNotifications(context: Context): Boolean {
        val key = context.getString(R.string.pref_enable_notifications)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(key, true)
    }

    /**
     * Returns the notification list from preferences.
     * @param context Context
     * @return list of notifications to use
     */
    fun getPreferenceNotificationList(context: Context): String {
        val key = context.getString(R.string.pref_notification_list)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val notificationList = prefs.getString(key, "25")
        return when (notificationList) {
            "1" -> FAVORITES
            "50" -> MOST_COMMON_50
            "100" -> MOST_COMMON_100
            "250" -> MOST_COMMON_250
            "500" -> MOST_COMMON_500
            "1000" -> MOST_COMMON_1000
            "9000" -> MOST_COMMON_ALL
            "25" -> MOST_COMMON_25
            else -> MOST_COMMON_25
        }
    }

    /**
     * Returns the notification frequency from preferences.
     * @param context Context
     * @return notification frequency
     */
    fun getPreferenceNotificationFrequency(context: Context): Int {
        val key = context.getString(R.string.pref_notification_frequency)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val frequency = prefs.getString(key, "24")
        return when (frequency) {
            "1", "3", "6", "12" -> Integer.parseInt(frequency)
            "24" -> 24
            else -> 24
        }
    }

    /**
     * Returns the notification time from preferences.
     * @param context Context
     * @return notification time
     */
    fun getPreferenceNotificationTime(context: Context): Long {
        val key = context.getString(R.string.pref_notification_time)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getLong(key, 0)
    }

    /**
     * Set the correct translation or hide the view.
     * @param context Context
     * @param textView view
     * @param verb Verb
     */
    fun setTranslation(context: Context, textView: TextView?, verb: Verb) {
        val fontSize = Integer.parseInt(getPreferenceFontSize(context))
        when (getPreferenceTranslationLanguage(context)) {
            NONE -> {
                textView?.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
                textView?.visibility = View.GONE
            }
            ENGLISH -> {
                textView?.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
                textView?.text = verb.translationEN
            }
            FRENCH -> {
                textView?.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
                textView?.text = verb.translationFR
            }
            SPANISH -> {
                textView?.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
                textView?.text = verb.translationES
            }
        }
    }

    /**
     * Returns the translation of the verb according to the language.
     * @param verb Verb
     * @param language language
     * @return String
     */
    fun getTranslation(verb: Verb, language: String): String {
        var translation = ""
        when (language) {
            ENGLISH -> translation = verb.translationEN
            FRENCH -> translation = verb.translationFR
            SPANISH -> translation = verb.translationES
        }
        return translation
    }

    /**
     * Returns the value of text to speech locale from preferences.
     * @param context context
     * @return String
     */
    fun getPreferenceTextToSpeechLocale(context: Context): String {
        val key = context.getString(R.string.pref_text_to_speech_locale)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(key, DEFAULT_TTS_LOCALE)
    }

    /**
     * Returns the local from a string.
     * @param localStr preference local string in "language, country" 3 letter codes.
     * @return Locale
     */
    fun getTextToSpeechLocale(localStr: String): Locale {
        val values = localStr.split(", ")
        return if (values.size == 2) {
            when {
                values[0].contentEquals("de") -> Locale.GERMANY
                else -> Locale(values[0], values[1])
            }
        } else {
            Locale.GERMANY
        }
    }

    /**
     * Configures the language in the speech object.
     * @param tts TextToSpeech
     * @param status Int
     * @param locale Locale
     */
    fun configureTextToSpeechLanguage(tts: TextToSpeech?, status: Int, locale: Locale) {
        if (tts == null) {
            Log.e("TTS", "Cannot configure language. TTS object is null.")
            return
        }

        if (status == TextToSpeech.SUCCESS) {
            try {
                val result = tts.setLanguage(locale)
                if (result == TextToSpeech.LANG_MISSING_DATA
                        || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    if (LOG) {
                        Log.e("TTS", "This Language is not supported")
                    }
                }
            } catch (ex : Exception) {
                Log.e("TTS", "Fail to set language $locale to TTS object -" + ex.message)
            }
        } else {
            if (LOG) {
                Log.e("TTS", "Initilization Failed!")
            }
        }
    }

    /**
     * Configure the preferred locale language.
     * @param context Context
     * @param tts TextToSpeech
     */
    fun setPreferredTextToSpeechLocale(context: Context, tts: TextToSpeech?, status: Int) {
        if (tts == null) return
        val locales = getTTSSupportedLanguages(tts)

        var selectedLocal : Locale? = null
        for (locale in locales) {
            if ("DEU".contentEquals(locale.isO3Country)) {
                selectedLocal = locale
                break
            }
        }
        if (selectedLocal == null && locales.isNotEmpty()){
            selectedLocal = locales[0]
        }

        if (selectedLocal != null ) {
            val code = selectedLocal.isO3Language + ", " + selectedLocal.isO3Country
            saveStringToPreferences(context, PREF_PREFERRED_TTS_LOCALE, code)

            configureTextToSpeechLanguage(tts, status, selectedLocal)
        }
    }

    /**
     * Get the supported languages in the speech object.
     * @param tts TextToSpeech
     */
    fun getTTSSupportedLanguages(tts: TextToSpeech): List<Locale> {
        val languagesAll: ArrayList<Locale> =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getTTSSupportedLanguagesLollipop(tts)
                } else {
                    getTTSSupportedLanguagesLegacy(tts)
                }
        if (languagesAll.isNotEmpty()) {
            // filter to German
            return languagesAll.filter { s -> s.toString().contains("de") }
        }
        return languagesAll
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getTTSSupportedLanguagesLollipop(tts: TextToSpeech) : ArrayList<Locale> {
        val languages: ArrayList<Locale> = ArrayList<Locale>()
        try {
            val locales = tts.availableLanguages
            if (locales != null && locales.isNotEmpty()) {
                for (locale in locales) {
                    languages.add(locale)
                }
            }
        } catch (ex : Exception) {
            Log.e("TTS", "Fail to read available languages for LOLLIPOP -" + ex.message)
        }
        return languages
    }

    private fun getTTSSupportedLanguagesLegacy(tts: TextToSpeech) : ArrayList<Locale> {
        val languages: ArrayList<Locale> = ArrayList<Locale>()
        val allLocales = Locale.getAvailableLocales()
        if (allLocales != null && allLocales.isNotEmpty()) {
            for (locale in allLocales) {
                try {
                    val res : Int = tts.isLanguageAvailable(locale)
                    val hasVariant = !locale.variant.isNullOrEmpty()
                    val hasCountry = !locale.country.isNullOrEmpty()

                    val isLocaleSupported: Boolean =
                            !hasVariant && !hasCountry && res == TextToSpeech.LANG_AVAILABLE
                            || !hasVariant && hasCountry && res == TextToSpeech.LANG_COUNTRY_AVAILABLE
                            || res == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE

                    if (LOG) {
                        Log.d("TTS", "TextToSpeech Engine isLanguageAvailable " + locale
                                + " (supported=" + isLocaleSupported + ")")
                    }

                    if (isLocaleSupported) {
                        languages.add(locale)
                    }
                } catch (ex : Exception) {
                    if (LOG) {
                        Log.e("TTS",
                                "Error checking if language is available for TTS (locale="
                                        + locale + "): " + "-" + ex.message)
                    }
                }
            }
        }
        return languages
    }

    /**
     * Text we want to speak.
     * @param text String
     */
    fun speak(context: Context, tts: TextToSpeech?, text: String?) {
        if (text == null || tts == null) {
            return
        }

        // Use the current media player volume
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val volume = am.getStreamVolume(AudioManager.STREAM_MUSIC)
        am.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)

        // Speak
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null)
        }
    }

    /**
     * Generate all table verb columns.
     * @return String[]
     */
    fun allVerbColumns(): Array<String> {
        return arrayOf(COLUMN_ID, COLUMN_CONJUGATION_NUMBER, COLUMN_INFINITIV,
                COLUMN_PARTIZIP_PERFEKT, COLUMN_IMPERFEKT_ER, COLUMN_PRASENS_ER,
                COLUMN_DEFINITION, COLUMN_IMAGE, COLUMN_SAMPLE_1, COLUMN_SAMPLE_2, COLUMN_SAMPLE_3,
                COLUMN_COMMON, COLUMN_TYPE,
                COLUMN_COLOR, COLUMN_SCORE, COLUMN_NOTES,
                COLUMN_TRANSLATION_EN, COLUMN_TRANSLATION_FR, COLUMN_TRANSLATION_ES)
    }

    /**
     * Generate all table conjugation columns.
     * @return String[]
     */
    fun allConjugationColumns(): Array<String> {
        return arrayOf(COLUMN_ID, COLUMN_TERMINATION, COLUMN_RADICALS,
                COLUMN_INFINITIV_PRASENS,
                COLUMN_INFINITIV_PERFEKT,
                COLUMN_PARTIZIP_PRASENS,
                COLUMN_PARTIZIP_PERFEKT,

                COLUMN_IMPERATIV_DU,
                COLUMN_IMPERATIV_IHR,
                COLUMN_IMPERATIV_SIE,

                COLUMN_INDIKATIV_PRASENS_ICH,
                COLUMN_INDIKATIV_PRASENS_DU,
                COLUMN_INDIKATIV_PRASENS_ER,
                COLUMN_INDIKATIV_PRASENS_WIR,
                COLUMN_INDIKATIV_PRASENS_IHR,
                COLUMN_INDIKATIV_PRASENS_SIE,

                COLUMN_INDIKATIV_PRATERIUM_ICH,
                COLUMN_INDIKATIV_PRATERIUM_DU,
                COLUMN_INDIKATIV_PRATERIUM_ER,
                COLUMN_INDIKATIV_PRATERIUM_WIR,
                COLUMN_INDIKATIV_PRATERIUM_IHR,
                COLUMN_INDIKATIV_PRATERIUM_SIE,

                COLUMN_INDIKATIV_PERFEKT_ICH,
                COLUMN_INDIKATIV_PERFEKT_DU,
                COLUMN_INDIKATIV_PERFEKT_ER,
                COLUMN_INDIKATIV_PERFEKT_WIR,
                COLUMN_INDIKATIV_PERFEKT_IHR,
                COLUMN_INDIKATIV_PERFEKT_SIE,

                COLUMN_INDIKATIV_PLUSQUAMPERFEKT_ICH,
                COLUMN_INDIKATIV_PLUSQUAMPERFEKT_DU,
                COLUMN_INDIKATIV_PLUSQUAMPERFEKT_ER,
                COLUMN_INDIKATIV_PLUSQUAMPERFEKT_WIR,
                COLUMN_INDIKATIV_PLUSQUAMPERFEKT_IHR,
                COLUMN_INDIKATIV_PLUSQUAMPERFEKT_SIE,

                COLUMN_INDIKATIV_FUTUR1_ICH,
                COLUMN_INDIKATIV_FUTUR1_DU,
                COLUMN_INDIKATIV_FUTUR1_ER,
                COLUMN_INDIKATIV_FUTUR1_WIR,
                COLUMN_INDIKATIV_FUTUR1_IHR,
                COLUMN_INDIKATIV_FUTUR1_SIE,

                COLUMN_INDIKATIV_FUTUR2_ICH,
                COLUMN_INDIKATIV_FUTUR2_DU,
                COLUMN_INDIKATIV_FUTUR2_ER,
                COLUMN_INDIKATIV_FUTUR2_WIR,
                COLUMN_INDIKATIV_FUTUR2_IHR,
                COLUMN_INDIKATIV_FUTUR2_SIE,

                COLUMN_KONJUNKTIV1_PRASENS_ICH,
                COLUMN_KONJUNKTIV1_PRASENS_DU,
                COLUMN_KONJUNKTIV1_PRASENS_ER,
                COLUMN_KONJUNKTIV1_PRASENS_WIR,
                COLUMN_KONJUNKTIV1_PRASENS_IHR,
                COLUMN_KONJUNKTIV1_PRASENS_SIE,

                COLUMN_KONJUNKTIV1_PERFEKT_ICH,
                COLUMN_KONJUNKTIV1_PERFEKT_DU,
                COLUMN_KONJUNKTIV1_PERFEKT_ER,
                COLUMN_KONJUNKTIV1_PERFEKT_WIR,
                COLUMN_KONJUNKTIV1_PERFEKT_IHR,
                COLUMN_KONJUNKTIV1_PERFEKT_SIE,

                COLUMN_KONJUNKTIV1_FUTUR1_ICH,
                COLUMN_KONJUNKTIV1_FUTUR1_DU,
                COLUMN_KONJUNKTIV1_FUTUR1_ER,
                COLUMN_KONJUNKTIV1_FUTUR1_WIR,
                COLUMN_KONJUNKTIV1_FUTUR1_IHR,
                COLUMN_KONJUNKTIV1_FUTUR1_SIE,

                COLUMN_KONJUNKTIV1_FUTUR2_ICH,
                COLUMN_KONJUNKTIV1_FUTUR2_DU,
                COLUMN_KONJUNKTIV1_FUTUR2_ER,
                COLUMN_KONJUNKTIV1_FUTUR2_WIR,
                COLUMN_KONJUNKTIV1_FUTUR2_IHR,
                COLUMN_KONJUNKTIV1_FUTUR2_SIE,

                COLUMN_KONJUNKTIV2_PRATERIUM_ICH,
                COLUMN_KONJUNKTIV2_PRATERIUM_DU,
                COLUMN_KONJUNKTIV2_PRATERIUM_ER,
                COLUMN_KONJUNKTIV2_PRATERIUM_WIR,
                COLUMN_KONJUNKTIV2_PRATERIUM_IHR,
                COLUMN_KONJUNKTIV2_PRATERIUM_SIE,

                COLUMN_KONJUNKTIV2_PLUSQUAMPERFEKT_ICH,
                COLUMN_KONJUNKTIV2_PLUSQUAMPERFEKT_DU,
                COLUMN_KONJUNKTIV2_PLUSQUAMPERFEKT_ER,
                COLUMN_KONJUNKTIV2_PLUSQUAMPERFEKT_WIR,
                COLUMN_KONJUNKTIV2_PLUSQUAMPERFEKT_IHR,
                COLUMN_KONJUNKTIV2_PLUSQUAMPERFEKT_SIE,

                COLUMN_KONJUNKTIV2_FUTUR1_ICH,
                COLUMN_KONJUNKTIV2_FUTUR1_DU,
                COLUMN_KONJUNKTIV2_FUTUR1_ER,
                COLUMN_KONJUNKTIV2_FUTUR1_WIR,
                COLUMN_KONJUNKTIV2_FUTUR1_IHR,
                COLUMN_KONJUNKTIV2_FUTUR1_SIE,

                COLUMN_KONJUNKTIV2_FUTUR2_ICH,
                COLUMN_KONJUNKTIV2_FUTUR2_DU,
                COLUMN_KONJUNKTIV2_FUTUR2_ER,
                COLUMN_KONJUNKTIV2_FUTUR2_WIR,
                COLUMN_KONJUNKTIV2_FUTUR2_IHR,
                COLUMN_KONJUNKTIV2_FUTUR2_SIE)
    }


    /**
     * Create a Verb from the current cursor position.
     * Note: columns must exist.
     * @param cursor Cursor
     * @return Verb
     */
    fun verbFromCursor(cursor: Cursor): Verb {
        return Verb(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)),
                cursor.getInt(cursor.getColumnIndex(COLUMN_CONJUGATION_NUMBER)),
                cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_INFINITIV)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_PARTIZIP_PERFEKT)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_PRASENS_ER)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_IMPERFEKT_ER)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_DEFINITION)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_SAMPLE_1)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_SAMPLE_2)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_SAMPLE_3)) ?: "",
                cursor.getInt(cursor.getColumnIndex(COLUMN_COMMON)),
                cursor.getInt(cursor.getColumnIndex(COLUMN_TYPE)),
                cursor.getInt(cursor.getColumnIndex(COLUMN_COLOR)),
                cursor.getInt(cursor.getColumnIndex(COLUMN_SCORE)),
                cursor.getString(cursor.getColumnIndex(COLUMN_NOTES)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_TRANSLATION_EN)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_TRANSLATION_FR)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_TRANSLATION_ES)) ?: "")
    }

    /**
     * Create a Conjugation from the current cursor position.
     * Note: columns must exist.
     * @param cursor Cursor
     * @return Conjugation
     */
    fun conjugationFromCursor(cursor: Cursor): Conjugation {
        return Conjugation(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(COLUMN_TERMINATION)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_RADICALS)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_INFINITIV_PRASENS)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_INFINITIV_PERFEKT)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_PARTIZIP_PRASENS)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_PARTIZIP_PERFEKT)) ?: "",

                cursor.getString(cursor.getColumnIndex(COLUMN_IMPERATIV_DU)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_IMPERATIV_IHR)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_IMPERATIV_SIE)) ?: "",

                cursor.getString(cursor.getColumnIndex(COLUMN_INDIKATIV_PRASENS_ICH)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_INDIKATIV_PRASENS_DU)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_INDIKATIV_PRASENS_ER)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_INDIKATIV_PRASENS_WIR)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_INDIKATIV_PRASENS_IHR)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_INDIKATIV_PRASENS_SIE)) ?: "",

                cursor.getString(cursor.getColumnIndex(COLUMN_INDIKATIV_PRATERIUM_ICH)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_INDIKATIV_PRATERIUM_DU)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_INDIKATIV_PRATERIUM_ER)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_INDIKATIV_PRATERIUM_WIR)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_INDIKATIV_PRATERIUM_IHR)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_INDIKATIV_PRATERIUM_SIE)) ?: "",

                cursor.getString(cursor.getColumnIndex(COLUMN_INDIKATIV_PERFEKT_ICH)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_INDIKATIV_PERFEKT_DU)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_INDIKATIV_PERFEKT_ER)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_INDIKATIV_PERFEKT_WIR)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_INDIKATIV_PERFEKT_IHR)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_INDIKATIV_PERFEKT_SIE)) ?: "",

                cursor.getString(cursor.getColumnIndex(COLUMN_INDIKATIV_PLUSQUAMPERFEKT_ICH)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_INDIKATIV_PLUSQUAMPERFEKT_DU)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_INDIKATIV_PLUSQUAMPERFEKT_ER)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_INDIKATIV_PLUSQUAMPERFEKT_WIR)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_INDIKATIV_PLUSQUAMPERFEKT_IHR)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_INDIKATIV_PLUSQUAMPERFEKT_SIE)) ?: "",

                cursor.getString(cursor.getColumnIndex(COLUMN_INDIKATIV_FUTUR1_ICH)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_INDIKATIV_FUTUR1_DU)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_INDIKATIV_FUTUR1_ER)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_INDIKATIV_FUTUR1_WIR)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_INDIKATIV_FUTUR1_IHR)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_INDIKATIV_FUTUR1_SIE)) ?: "",

                cursor.getString(cursor.getColumnIndex(COLUMN_INDIKATIV_FUTUR2_ICH)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_INDIKATIV_FUTUR2_DU)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_INDIKATIV_FUTUR2_ER)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_INDIKATIV_FUTUR2_WIR)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_INDIKATIV_FUTUR2_IHR)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_INDIKATIV_FUTUR2_SIE)) ?: "",

                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV1_PRASENS_ICH)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV1_PRASENS_DU)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV1_PRASENS_ER)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV1_PRASENS_WIR)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV1_PRASENS_IHR)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV1_PRASENS_SIE)) ?: "",

                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV1_PERFEKT_ICH)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV1_PERFEKT_DU)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV1_PERFEKT_ER)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV1_PERFEKT_WIR)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV1_PERFEKT_IHR)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV1_PERFEKT_SIE)) ?: "",

                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV1_FUTUR1_ICH)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV1_FUTUR1_DU)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV1_FUTUR1_ER)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV1_FUTUR1_WIR)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV1_FUTUR1_IHR)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV1_FUTUR1_SIE)) ?: "",

                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV1_FUTUR2_ICH)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV1_FUTUR2_DU)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV1_FUTUR2_ER)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV1_FUTUR2_WIR)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV1_FUTUR2_IHR)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV1_FUTUR2_SIE)) ?: "",

                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV2_PRATERIUM_ICH)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV2_PRATERIUM_DU)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV2_PRATERIUM_ER)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV2_PRATERIUM_WIR)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV2_PRATERIUM_IHR)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV2_PRATERIUM_SIE)) ?: "",

                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV2_PLUSQUAMPERFEKT_ICH)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV2_PLUSQUAMPERFEKT_DU)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV2_PLUSQUAMPERFEKT_ER)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV2_PLUSQUAMPERFEKT_WIR)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV2_PLUSQUAMPERFEKT_IHR)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV2_PLUSQUAMPERFEKT_SIE)) ?: "",

                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV2_FUTUR1_ICH)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV2_FUTUR1_DU)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV2_FUTUR1_ER)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV2_FUTUR1_WIR)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV2_FUTUR1_IHR)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV2_FUTUR1_SIE)) ?: "",

                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV2_FUTUR2_ICH)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV2_FUTUR2_DU)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV2_FUTUR2_ER)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV2_FUTUR2_WIR)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV2_FUTUR2_IHR)) ?: "",
                cursor.getString(cursor.getColumnIndex(COLUMN_KONJUNKTIV2_FUTUR2_SIE)) ?: ""
        )
    }

    /**
     * Checks if we should replace the letter with apostrophe.
     * @param text String
     * @return true or false
     */
    fun useApostrophe(text: String): Boolean {
        // L'apostrophe ( ' ) est un signe qui remplace une des voyelles ( a, e, i )
        // quand le mot qui suit commence lui-même par une voyelle ou un h muet.
        return (text.startsWith("a") || text.startsWith("e") || text.startsWith("i")
                || text.startsWith("o") || text.startsWith("u") || text.startsWith("h")
                || text.startsWith("â") || text.startsWith("à") || text.startsWith("ë")
                || text.startsWith("é") || text.startsWith("ê") || text.startsWith("è")
                || text.startsWith("î") || text.startsWith("ï") || text.startsWith("ô")
                || text.startsWith("û") || text.startsWith("ù") || text.startsWith("ü"))
    }

    /**
     * Converts the verb infinitive into the file image name.
     * @param infinitive verb
     * @return String
     */
    fun generateImageName(infinitive: String): String {
        // File names should use no spaces and english alphabet lowercase letters
        var imageName = infinitive
        imageName = replaceOccurrences(imageName, "'", "")
        imageName = replaceOccurrences(imageName, " ", "")
        imageName = replaceOccurrences(imageName, "â", "a")
        imageName = replaceOccurrences(imageName, "à", "a")
        imageName = replaceOccurrences(imageName, "ë", "e")
        imageName = replaceOccurrences(imageName, "é", "e")
        imageName = replaceOccurrences(imageName, "ê", "e")
        imageName = replaceOccurrences(imageName, "è", "e")
        imageName = replaceOccurrences(imageName, "î", "i")
        imageName = replaceOccurrences(imageName, "ï", "i")
        imageName = replaceOccurrences(imageName, "ô", "o")
        imageName = replaceOccurrences(imageName, "û", "u")
        imageName = replaceOccurrences(imageName, "ù", "u")
        imageName = replaceOccurrences(imageName, "ü", "u")
        return imageName
    }

    /**
     * Replace occurrences of string
     * @param text String
     * @param oldString String
     * @param newString String
     * @return formatted String
     */
    private fun replaceOccurrences(text: String, oldString: String, newString: String): String {
        return if (text.contains(oldString)) {
            text.replace(oldString.toRegex(), newString)
        } else text
    }

    /**
     * Sets the image id into the view.
     * @param context context
     * @param view imageView
     * @param id id
     */
    @SuppressLint("RestrictedApi")
    fun setImage(context: Context, view: ImageView, id: Int) {
        view.setImageDrawable(AppCompatDrawableManager.get().getDrawable(context, id))
        view.visibility = View.VISIBLE
    }

    /**
     * Launch share text intent.
     * @param activity Activity
     * @param text String
     */
    fun launchShareText(activity: Activity, text: String) {
        // https://medium.com/google-developers/sharing-content-between-android-apps-2e6db9d1368b#.6usvw9n9p
        val shareIntent = ShareCompat.IntentBuilder.from(activity)
                .setType("text/plain")
                .setText(text)
                .intent
        if (shareIntent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(shareIntent)
        }
    }

    /**
     * Schedules a repeating event to launch the verb Notifications.
     * @param context Context
     */
    fun scheduleRepeatingNotifications(context: Context) {
        // JobScheduler works since 21+ (Lollipop).
        // But, it doesn't allow to configure start time.
        // So, use it since API 26+ (Oreo) because there it's not safe to use AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            scheduleTask(context)
        } else {
            startAlarm(context)
        }
    }

    /**
     * Cancels the repeating event that launches the verb Notifications.
     * @param context Context
     */
    fun cancelRepeatingNotifications(context: Context) {
        // JobScheduler works since 21+ (Lollipop).
        // But, it doesn't allow to configure start time.
        // So, use it since API 26+ (Oreo) because there it's not safe to use AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            cancelTask(context)
        } else {
            cancelAlarm(context)
        }
    }

    /**
     * Start an Alarm
     * @param context Context
     */
    fun startAlarm(context: Context) {
        val alarmIntent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0)
        val startTime = getPreferenceNotificationTime(context)
        var interval = INTERVAL_HOUR * getPreferenceNotificationFrequency(context)

        if (USE_TEST_ALARM_INTERVALS) {
            interval = 10000 // 10 seconds
        }
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, startTime, interval, pendingIntent)
        if (LOG) {
            Toast.makeText(context, "Alarm Set", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Cancel Alarm
     * @param context Context
     */
    fun cancelAlarm(context: Context) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0)
        manager.cancel(pendingIntent)
        if (LOG) {
            Toast.makeText(context, "Alarm Canceled", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Schedules a repeating task.
     * @param context Context
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun scheduleTask(context: Context) {
        // https://code.tutsplus.com/tutorials/using-the-jobscheduler-api-on-android-lollipop--cms-23562
        // TODO: Fix multiple starts when englishverbs resets notifications
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val builder = JobInfo.Builder(JobSchedulerService.JOB_ID,
                ComponentName(context.packageName, JobSchedulerService::class.java.name))

        var interval = INTERVAL_HOUR * getPreferenceNotificationFrequency(context)
        if (USE_TEST_ALARM_INTERVALS) {
            interval = 10000 // 10 seconds
        }

        builder.setPeriodic(interval)
        builder.setPersisted(true)

        if (jobScheduler.schedule(builder.build()) == JobScheduler.RESULT_FAILURE) {
            //If something goes wrong
            if (LOG) {
                Toast.makeText(context, "Scheduled Notification Task Failed",
                        Toast.LENGTH_SHORT).show()
            }
        } else {
            if (LOG) {
                Toast.makeText(context, "Scheduled Notification Task Set",
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Cancel all tasks
     * @param context Context
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun cancelTask(context: Context) {
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.cancel(JobSchedulerService.JOB_ID)
        if (LOG) {
            Toast.makeText(context, "Scheduled Notification Task Canceled",
                    Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Check if it's the first run, and launch the Verb Notifications with AlarmManager.
     * @param context Context
     * @param firebaseAnalytics FirebaseAnalytics
     */
    fun checkFirstRun(context: Context, firebaseAnalytics: FirebaseAnalytics) : Boolean {
        val notFound = -1
        // Get current version code
        val currentVersionCode = BuildConfig.VERSION_CODE

        // Get saved version code
        val prefs = context.getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE)
        val savedVersionCode : Int = prefs.getLong(PREF_VERSION_CODE_KEY, notFound.toLong()).toInt()

        if (currentVersionCode == savedVersionCode) {
            // This is just a normal run
            return false

        } else if (savedVersionCode == notFound || currentVersionCode > savedVersionCode) {
            // This is a new install (or the user cleared the shared preferences) or upgrade
            scheduleRepeatingNotifications(context)
            firebaseAnalyticsLogEventSelectContent(firebaseAnalytics,
                    TYPE_START_NOTIFICATIONS, "First Run", TYPE_VERB_NOTIFICATION)
        }

        // Update the shared preferences with the current version code
        saveLongToPreferences(context, PREF_VERSION_CODE_KEY, currentVersionCode.toLong())
        return true
    }

    /**
     * Initializes and show the AdMob banner.
     * Needs to be called in onCreate of the activity.
     * https://firebase.google.com/docs/admob/android/quick-start
     * @param activity activity
     * @param listener LogAdListener
     */
    fun createAdMobBanner(activity: AppCompatActivity, listener: LogAdListener): AdView {
        val adMobAppId = activity.getString(R.string.admob_app_id)
        // Initialize AdMob
        MobileAds.initialize(activity, adMobAppId)

        val adView = activity.findViewById<AdView>(R.id.adView)
        // Set listener
        // https://firebase.google.com/docs/admob/android/ad-events
        adView.adListener = listener

        // Load an ad into the AdMob banner view.
        val adRequest: AdRequest = if (USE_TEST_ADS) {
            AdRequest.Builder()
                    // Use AdRequest.Builder.addTestDevice() to get test ads on this device.
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    // SLONE SLONE Pilot_S5004 (Android 6.0, API 23)
                    .addTestDevice("4DF5D2AB04EBFA06FB2656A06D2C0EE3")
                    .build()
        } else {
            AdRequest.Builder().build()
        }
        adView.loadAd(adRequest)

        return adView
    }

    /**
     * Logs a Firebase Analytics select content event.
     * https://firebase.google.com/docs/reference/android/com/google/firebase/analytics/FirebaseAnalytics.Event#SELECT_CONTENT
     * @param analytics FirebaseAnalytics
     * @param id id
     * @param name name
     * @param type type
     */
    fun firebaseAnalyticsLogEventSelectContent(analytics: FirebaseAnalytics, id: String,
                                               name: String, type: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id)
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name)
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type)
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    /**
     * Logs a Firebase Analytics search event.
     * https://firebase.google.com/docs/reference/android/com/google/firebase/analytics/FirebaseAnalytics.Event#SEARCH
     * @param analytics FirebaseAnalytics
     * @param search string to search
     */
    fun firebaseAnalyticsLogEventSearch(analytics: FirebaseAnalytics, search: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SEARCH_TERM, search)
        analytics.logEvent(FirebaseAnalytics.Event.SEARCH, bundle)
    }

    /**
     * Logs a Firebase Analytics view search results event.
     * https://firebase.google.com/docs/reference/android/com/google/firebase/analytics/FirebaseAnalytics.Event#VIEW_SEARCH_RESULTS
     * @param analytics FirebaseAnalytics
     * @param search string to search
     */
    fun firebaseAnalyticsLogEventViewSearchResults(analytics: FirebaseAnalytics, search: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SEARCH_TERM, search)
        analytics.logEvent(FirebaseAnalytics.Event.VIEW_SEARCH_RESULTS, bundle)
    }

    /**
     * Logs a Firebase Analytics view item event.
     * https://firebase.google.com/docs/reference/android/com/google/firebase/analytics/FirebaseAnalytics.Event#VIEW_ITEM
     * @param analytics FirebaseAnalytics
     * @param id id
     * @param name name
     * @param category category
     */
    fun firebaseAnalyticsLogEventViewItem(analytics: FirebaseAnalytics, id: String, name: String,
                                          category: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id)
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name)
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, category)
        analytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle)
    }
}
