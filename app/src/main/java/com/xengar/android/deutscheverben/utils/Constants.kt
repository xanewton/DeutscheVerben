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

/**
 * Constants
 */
object Constants {

    val LAST_ACTIVITY = "last_activity"
    val MAIN_ACTIVITY = "main_activity"

    val SHARED_PREF_NAME = "com.xengar.android.deutscheverben"
    val VERB_TYPE = "verb_type"
    val TYPE_WEAK = "1_weak"
    val TYPE_STRONG = "2_strong"
    val TYPE_MIXED = "3_mixed"
    val TYPE_ALL = "all_types"
    val FAVORITES = "favorites"
    val SORT_TYPE = "sort_type"
    val ALPHABET = "alphabet"
    val COLOR = "color"
    val TYPE = "type"
    val COMMON_TYPE = "common_type"
    val MOST_COMMON_25 = "25"
    val MOST_COMMON_50 = "50"
    val MOST_COMMON_100 = "100"
    val MOST_COMMON_250 = "250"
    val MOST_COMMON_500 = "500"
    val MOST_COMMON_1000 = "1000"
    val MOST_COMMON_ALL = "all"

    val ITEM_TYPE = "item_type"
    val CARD = "card"
    val LIST = "list"

    val CURRENT_PAGE = "current_page"
    val PAGE_VERBS = "Verbs"
    val PAGE_CARDS = "Cards"
    val PAGE_FAVORITES = "Favorites"

    val DEFAULT_FONT_SIZE = "14"

    val VERB_ID = "verb_id"
    val CONJUGATION_ID = "conjugation_id"
    val VERB_NAME = "verb_name"
    val DEMO_MODE = "demo_mode"
    val DISPLAY_VERB_TYPE = "display_verb_type"
    val DISPLAY_SORT_TYPE = "display_sort_type"
    val DISPLAY_COMMON_TYPE = "display_common_type"
    val NOTIFICATION_VERB_ID = "notification_verb_id"
    val PREF_VERSION_CODE_KEY = "version_code"
    val PREF_NO_ALARM_MANAGER_SINCE_API_26 = "no_alarm_manager_since_api_26"
    val PREF_PREFERRED_TTS_LOCALE = "preferred_text_to_speech_locale"

    // Pronoms personnels
    val ICH = "ich "        // I
    val DU = "du "          // You (informal singular)
    val ER = "er/sie/es "   // He / She / It
    val WIR = "wir "        // We (us)
    val IHR = "ihr "        // You (informal plural)
    val SIE = "sie/Sie "    // They / You (formal - singular or plural)

    // Translation languages
    val NONE = "None"
    val ENGLISH = "en"
    val FRENCH = "fr"
    val SPANISH = "es"
    // Check Text To Speech
    val ACT_CHECK_TTS_DATA = 1000
    val DEFAULT_TTS_LOCALE = "de"

    // Firebase strings
    val TYPE_PAGE = "page"
    val TYPE_AD = "Ad"
    val TYPE_CONTEXT_HELP = "Context Help"
    val DETAILS_ACTIVITY = "details_activity"
    val PAGE_VERB_DETAILS = "Verb Details"
    val PAGE_SEARCH = "Search"
    val PAGE_HELP = "Help"
    val TYPE_ADD_FAV = "add to Favorites"
    val TYPE_DEL_FAV = "remove from Favorites"
    val TYPE_VERB_NOTIFICATION = "Verb Notification"
    val TYPE_START_NOTIFICATIONS = "Start Scheduled Verb Notifications"
    val TYPE_STOP_NOTIFICATIONS = "Stop Scheduled Verb Notifications"
    val TYPE_SHARE = "Share"
    val VERBS = "verbs"
    @JvmField val VERB = "verb_"
    @JvmField val DRAWABLE = "drawable"


    /**
     * Boolean used to log or not lines
     * Usage:
     * if (LOG) {
     * if (condition) Log.i(...);
     * }
     * When you set LOG to false, the compiler will strip out all code inside such checks
     * (since it is a static final, it knows at compile time that code is not used.)
     * http://stackoverflow.com/questions/2446248/remove-all-debug-logging-calls-before-publishing-are-there-tools-to-do-this
     */
    val LOG = true

    /**
     * Enable test ads for AdMob
     * See ActivityUtils.createAdMobBanner()
     */
    val USE_TEST_ADS = true
    /**
     * Enable overwriting or Alarm Interval time
     */
    val USE_TEST_ALARM_INTERVALS = false

}
