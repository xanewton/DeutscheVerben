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
package com.xengar.android.deutscheverben.ui

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.preference.CheckBoxPreference
import android.preference.EditTextPreference
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.preference.SwitchPreference
import android.speech.tts.TextToSpeech
import android.support.v4.app.NavUtils
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDelegate
import android.util.Log
import android.view.MenuItem
import android.widget.Toast

import com.google.firebase.analytics.FirebaseAnalytics
import com.xengar.android.deutscheverben.R
import com.xengar.android.deutscheverben.utils.ActivityUtils
import com.xengar.android.deutscheverben.utils.Constants.ACT_CHECK_TTS_DATA
import com.xengar.android.deutscheverben.utils.Constants.DEFAULT_TTS_LOCALE
import com.xengar.android.deutscheverben.utils.Constants.LOG
import com.xengar.android.deutscheverben.utils.Constants.PREF_PREFERRED_TTS_LOCALE
import com.xengar.android.deutscheverben.utils.Constants.SHARED_PREF_NAME
import com.xengar.android.deutscheverben.utils.Constants.TYPE_START_NOTIFICATIONS
import com.xengar.android.deutscheverben.utils.Constants.TYPE_STOP_NOTIFICATIONS
import com.xengar.android.deutscheverben.utils.Constants.TYPE_VERB_NOTIFICATION
import com.xengar.android.deutscheverben.utils.FontDialog
import java.util.*

/**
 * A [PreferenceActivity] that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 *
 * See [Android Design: Settings](http://developer.android.com/design/patterns/settings.html)
 * for design guidelines and the [Settings API Guide](http://developer.android.com/guide/topics/ui/settings.html)
 * for more information on developing a Settings UI.
 */
class SettingsActivity : AppCompatPreferenceActivity(),
        SharedPreferences.OnSharedPreferenceChangeListener {

    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        setupActionBar()

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
    }

    /**
     * Set up the [android.app.ActionBar], if the API is available.
     */
    private fun setupActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onMenuItemSelected(featureId: Int, item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this)
            }
            return true
        }
        return super.onMenuItemSelected(featureId, item)
    }

    /**
     * {@inheritDoc}
     */
    override fun onIsMultiPane(): Boolean {
        return isXLargeTablet(this)
    }

    /**
     * {@inheritDoc}
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onBuildHeaders(target: List<PreferenceActivity.Header>) {
        val resourceId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            R.xml.pref_headers_v21
        else
            R.xml.pref_headers
        loadHeadersFromResource(resourceId, target)
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    override fun isValidFragment(fragmentName: String): Boolean {
        return PreferenceFragment::class.java.name == fragmentName
                || GeneralPreferenceFragment::class.java.name == fragmentName
    }

    // Registers a shared preference change listener that gets notified when preferences change.
    override fun onResume() {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        sp.registerOnSharedPreferenceChangeListener(this)
        super.onResume()
    }

    // Unregisters a shared preference change listener.
    override fun onPause() {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        sp.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    /**
     * Called after a preference changes.
     * @param sharedPreferences SharedPreferences
     * @param key key
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == getString(R.string.pref_enable_notifications)
                || key == getString(R.string.pref_notification_list)
                || key == getString(R.string.pref_notification_time)
                || key == getString(R.string.pref_notification_frequency)) {
            // Reconfigure Verb Notifications
            val enabled = ActivityUtils.getPreferenceEnableNotifications(applicationContext)
            if (!enabled) {
                ActivityUtils.cancelRepeatingNotifications(applicationContext)
                ActivityUtils.firebaseAnalyticsLogEventSelectContent(mFirebaseAnalytics!!,
                        TYPE_STOP_NOTIFICATIONS, "Preferences", TYPE_VERB_NOTIFICATION)
            } else {
                ActivityUtils.scheduleRepeatingNotifications(applicationContext)
                ActivityUtils.firebaseAnalyticsLogEventSelectContent(mFirebaseAnalytics!!,
                        TYPE_START_NOTIFICATIONS, "Preferences", TYPE_VERB_NOTIFICATION)
            }
        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class GeneralPreferenceFragment : PreferenceFragment() {

        private var tts: TextToSpeech? = null
        private val sharedPrefsChangeListener =
                SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
                    if (key == getString(R.string.pref_font_size)) {
                        updateSummary()
                    } else if (key == getString(R.string.pref_text_to_speech_locale)) {
                        processTextToSpeechLocalPreferenceChange()
                    }
                }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            // TODO: Find a way to calculate the start time and implement it
            val resourceId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                R.xml.pref_general_v26
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                R.xml.pref_general_v21
            else
                R.xml.pref_general
            addPreferencesFromResource(resourceId)
            setHasOptionsMenu(true)

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_translation_language)))
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_favorite_mode_list)))
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_font_size)))
            var dataPref = findPreference(getString(R.string.pref_notification_list)) as ListPreference
            bindPreferenceSummaryToValue(dataPref)
            if (dataPref.value == null) {
                dataPref.setValueIndex(1)
                dataPref.summary = getString(R.string.most_common_25)
            }
            dataPref = findPreference(getString(R.string.pref_notification_frequency)) as ListPreference
            bindPreferenceSummaryToValue(dataPref)
            if (dataPref.value == null) {
                dataPref.setValueIndex(4)
                dataPref.summary = getString(R.string.hour_24)
            }

            fillTTSVoiceDataOptions()
        }

        private fun fillTTSVoiceDataOptions() {
            // Check to see if we have TTS voice data
            val intent = Intent()
            intent.action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA
            if (ActivityUtils.isIntentCallable(activity, intent)) {
                startActivityForResult(intent, ACT_CHECK_TTS_DATA)
            } else {
                val msg =  getString(R.string.tts_not_supported)
                Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
                fillTTSLanguageOptionsAsNone()
            }
        }

        /** Called when returning from startActivityForResult  */
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            if (requestCode == ACT_CHECK_TTS_DATA) {
                if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                    // data exists, so we instantiate the TTS engine
                    tts = TextToSpeech(activity, TextToSpeech.OnInitListener { status ->
                        ActivityUtils.configureTextToSpeechLanguage(tts, status, Locale.US)
                        setLanguageOptions()
                    })

                } else {
                    // data is missing, ask to install TTS voice data
                    fillTTSLanguageOptionsAsNone()
                    installTTSVoiceData()
                }
            }
        }

        private fun fillTTSLanguageOptionsAsNone() {
            val langPref = findPreference(getString(R.string.pref_text_to_speech_locale)) as ListPreference
            langPref.entries = arrayOf("None")
            langPref.entryValues = arrayOf("None")
            bindPreferenceSummaryToValue(langPref)
        }

        private fun installTTSVoiceData() {
            // Ask to install TTS engine voice data
            val alertDialog = AlertDialog.Builder(activity).create()
            alertDialog.setTitle(getString(R.string.pref_title_text_to_speech_locale))
            alertDialog.setMessage(getString(R.string.tts_install_voice_data))

            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.yes), { _, _ ->
                // Start TTS installation
                val intent = Intent()
                intent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                startActivity(intent)
            })
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.no), { _, _ -> })
            alertDialog.show()
        }

        private fun processTextToSpeechLocalPreferenceChange() {
            val prefs = activity.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
            val preferredTTSLocale = prefs.getString(PREF_PREFERRED_TTS_LOCALE, DEFAULT_TTS_LOCALE)

            // current value
            val currentTTSLocale = ActivityUtils.getPreferenceTextToSpeechLocale(activity)

            if (!currentTTSLocale.contentEquals(preferredTTSLocale)) {
                ActivityUtils.saveStringToPreferences(activity,
                        PREF_PREFERRED_TTS_LOCALE, currentTTSLocale)

                // Notify MainActivity to change language.
                // Note that MainActivity will be shown over current SettingsActivity.
                // TODO: Figure out another way to notify MainActivity without changing views
                val intent = Intent(activity, MainActivity::class.java)
                intent.action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA
                startActivityForResult(intent, ACT_CHECK_TTS_DATA)
            }
        }

        private fun setLanguageOptions() {

            val languages = ActivityUtils.getTTSSupportedLanguages(tts!!)
            if (languages.isEmpty()) {
                fillTTSLanguageOptionsAsNone()
                return
            }

            val entries = arrayOfNulls<String>(languages.size)
            val entryValues = arrayOfNulls<String>(languages.size)

            val prefs = activity.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
            val preferredTTSLocale = prefs.getString(PREF_PREFERRED_TTS_LOCALE, DEFAULT_TTS_LOCALE)

            var currentLocaleIndex = 0
            for (i in languages.indices) {
                entries[i] = languages[i].getDisplayName(languages[i])
                val code = languages[i].isO3Language + ", " + languages[i].isO3Country
                entryValues[i] = code
                if (preferredTTSLocale.equals(code)) {
                    currentLocaleIndex = i
                }
            }

            val langPref = findPreference(getString(R.string.pref_text_to_speech_locale)) as ListPreference
            langPref.entries = entries
            langPref.entryValues = entryValues

            if (langPref.value == null) {
                langPref.setValueIndex(currentLocaleIndex)
                langPref.summary = entries[currentLocaleIndex]
            }
            bindPreferenceSummaryToValue(langPref)
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                startActivity(Intent(activity, MainActivity::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }

        private fun updateSummary() {
            val fontPref = findPreference(getString(R.string.pref_font_size))
            fontPref.summary = ActivityUtils.getPreferenceFontSize(activity)
        }

        override fun onPause() {
            super.onPause()
            preferenceScreen
                    .sharedPreferences
                    .unregisterOnSharedPreferenceChangeListener(sharedPrefsChangeListener)
        }

        override fun onResume() {
            super.onResume()
            updateSummary()
            preferenceScreen
                    .sharedPreferences
                    .registerOnSharedPreferenceChangeListener(sharedPrefsChangeListener)
        }
    }

    companion object {

        /**
         * A preference value change listener that updates the preference's summary
         * to reflect its new value.
         */
        private val sBindPreferenceSummaryToValueListener =
                Preference.OnPreferenceChangeListener { preference, value ->
            val stringValue = value.toString()

            when (preference) {
                is ListPreference -> {
                    // For list preferences, look up the correct display value in
                    // the preference's 'entries' list.
                    val index = preference.findIndexOfValue(stringValue)

                    // Set the summary to reflect the new value.
                    preference.setSummary(
                            if (index >= 0)
                                preference.entries[index]
                            else
                                null)
                }
                is SwitchPreference -> // For a boolean value, set the default value "true"
                    preference.setDefaultValue(stringValue.contains("t"))
                else -> // For all other preferences, set the summary to the value's
                    // simple string representation.
                    preference.summary = stringValue
            }
            true
        }

        /**
         * Helper method to determine if the device has an extra-large screen. For
         * example, 10" tablets are extra-large.
         */
        private fun isXLargeTablet(context: Context): Boolean {
            return context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_XLARGE
        }

        /**
         * Binds a preference's summary to its value. More specifically, when the
         * preference's value is changed, its summary (line of text below the
         * preference title) is updated to reflect the value. The summary is also
         * immediately updated upon calling this method. The exact display format is
         * dependent on the type of preference.

         * @see .sBindPreferenceSummaryToValueListener
         */
        private fun bindPreferenceSummaryToValue(preference: Preference) {
            // Set the listener to watch for value changes.
            preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener

            // Trigger the listener immediately with the preference' current value.
            if (preference is ListPreference
                    || preference is EditTextPreference
                    || preference is FontDialog) {
                sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                        PreferenceManager
                                .getDefaultSharedPreferences(preference.context)
                                .getString(preference.key, ""))
            } else if (preference is SwitchPreference || preference is CheckBoxPreference) {
                sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                        PreferenceManager
                                .getDefaultSharedPreferences(preference.context)
                                .getBoolean(preference.key, true))
            }
        }
    }
}
