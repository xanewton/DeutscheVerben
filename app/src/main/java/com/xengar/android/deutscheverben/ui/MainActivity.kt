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
package com.xengar.android.deutscheverben.ui

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem

import com.google.android.gms.ads.AdView
import com.google.firebase.analytics.FirebaseAnalytics
import com.xengar.android.deutscheverben.R
import com.xengar.android.deutscheverben.utils.ActivityUtils
import com.xengar.android.deutscheverben.utils.LogAdListener
import com.xengar.android.deutscheverben.utils.ActivityUtils.checkFirstRun
import com.xengar.android.deutscheverben.utils.Constants.ACT_CHECK_TTS_DATA
import com.xengar.android.deutscheverben.utils.Constants.ALPHABET
import com.xengar.android.deutscheverben.utils.Constants.CARD
import com.xengar.android.deutscheverben.utils.Constants.COLOR
import com.xengar.android.deutscheverben.utils.Constants.COMMON_TYPE
import com.xengar.android.deutscheverben.utils.Constants.CURRENT_PAGE
import com.xengar.android.deutscheverben.utils.Constants.DISPLAY_COMMON_TYPE
import com.xengar.android.deutscheverben.utils.Constants.DISPLAY_SORT_TYPE
import com.xengar.android.deutscheverben.utils.Constants.DISPLAY_VERB_TYPE
import com.xengar.android.deutscheverben.utils.Constants.FAVORITES
import com.xengar.android.deutscheverben.utils.Constants.TYPE
import com.xengar.android.deutscheverben.utils.Constants.TYPE_WEAK
import com.xengar.android.deutscheverben.utils.Constants.TYPE_STRONG
import com.xengar.android.deutscheverben.utils.Constants.TYPE_MIXED
import com.xengar.android.deutscheverben.utils.Constants.TYPE_ALL
import com.xengar.android.deutscheverben.utils.Constants.ITEM_TYPE
import com.xengar.android.deutscheverben.utils.Constants.LAST_ACTIVITY
import com.xengar.android.deutscheverben.utils.Constants.LIST
import com.xengar.android.deutscheverben.utils.Constants.LOG
import com.xengar.android.deutscheverben.utils.Constants.MAIN_ACTIVITY
import com.xengar.android.deutscheverben.utils.Constants.MOST_COMMON_100
import com.xengar.android.deutscheverben.utils.Constants.MOST_COMMON_25
import com.xengar.android.deutscheverben.utils.Constants.MOST_COMMON_250
import com.xengar.android.deutscheverben.utils.Constants.MOST_COMMON_50
import com.xengar.android.deutscheverben.utils.Constants.MOST_COMMON_500
import com.xengar.android.deutscheverben.utils.Constants.MOST_COMMON_ALL
import com.xengar.android.deutscheverben.utils.Constants.PAGE_CARDS
import com.xengar.android.deutscheverben.utils.Constants.PAGE_FAVORITES
import com.xengar.android.deutscheverben.utils.Constants.PAGE_VERBS
import com.xengar.android.deutscheverben.utils.Constants.SHARED_PREF_NAME
import com.xengar.android.deutscheverben.utils.Constants.SORT_TYPE
import com.xengar.android.deutscheverben.utils.Constants.TYPE_PAGE
import com.xengar.android.deutscheverben.utils.Constants.VERB_TYPE
import kotlinx.android.synthetic.main.activity_main.*

/**
 * MainActivity
 */
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {


    private var verbsFragment: UniversalFragment? = null
    private var cardsFragment: UniversalFragment? = null
    private var favoritesFragment: UniversalFragment? = null

    private val VERB_GROUPS = arrayOf(TYPE_WEAK, TYPE_STRONG, TYPE_MIXED, TYPE_ALL)
    private val verbSelection = intArrayOf(3)
    private val verbGroup = arrayOf(VERB_GROUPS[verbSelection[0]]) // current verb type list in screen

    private val SORT_TYPES = arrayOf(ALPHABET, COLOR, TYPE)
    private val sortSelection = intArrayOf(0)
    private val sortType = arrayOf(SORT_TYPES[sortSelection[0]]) // current sort type list in screen

    private val COMMON_TYPES = arrayOf(MOST_COMMON_25, MOST_COMMON_50, MOST_COMMON_100,
            MOST_COMMON_250, MOST_COMMON_500, MOST_COMMON_ALL)
    private val commonSelection = intArrayOf(3)
    private val commonType = arrayOf(COMMON_TYPES[commonSelection[0]]) // current most common type list in screen

    private var page: String = PAGE_VERBS // Current page
    var tts: TextToSpeech? = null
        private set

    private var isFirstRun = false
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    private var mAdView: AdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        // create AdMob banner
        val listener = LogAdListener(mFirebaseAnalytics!!, MAIN_ACTIVITY)
        mAdView = ActivityUtils.createAdMobBanner(this, listener)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        isFirstRun = checkFirstRun(this, mFirebaseAnalytics!!)
        configureTextToSpeech()
        createFragments()
    }

    private fun createFragments() {
        // Save name of activity, in case of calling SettingsActivity
        ActivityUtils.saveStringToPreferences(applicationContext, LAST_ACTIVITY, MAIN_ACTIVITY)

        val prefs = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE)
        val currentPage = prefs.getString(CURRENT_PAGE, PAGE_VERBS)
        if (currentPage != null) {
            page = currentPage
        }
        // read verb Type
        verbGroup[0] = prefs.getString(DISPLAY_VERB_TYPE, TYPE_ALL)
        for (i in VERB_GROUPS.indices) {
            if (verbGroup[0].contentEquals(VERB_GROUPS[i])) {
                verbSelection[0] = i
                break
            }
        }
        sortType[0] = prefs.getString(DISPLAY_SORT_TYPE, ALPHABET)
        for (i in SORT_TYPES.indices) {
            if (sortType[0].contentEquals(SORT_TYPES[i])) {
                sortSelection[0] = i
                break
            }
        }
        commonType[0] = prefs.getString(DISPLAY_COMMON_TYPE, MOST_COMMON_ALL)
        for (i in COMMON_TYPES.indices) {
            if (commonType[0].contentEquals(COMMON_TYPES[i])) {
                commonSelection[0] = i
                break
            }
        }

        verbsFragment = createUniversalFragment(verbGroup[0], LIST, sortType[0], commonType[0])
        cardsFragment = createUniversalFragment(verbGroup[0], CARD, sortType[0], commonType[0])
        favoritesFragment = createUniversalFragment(FAVORITES,
                ActivityUtils.getPreferenceFavoritesMode(applicationContext), sortType[0],
                MOST_COMMON_ALL)

        showPage(page)
        assignCheckedItem(page)
    }

    /** Called when returning from startActivityForResult  */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ACT_CHECK_TTS_DATA) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // data exists, so we instantiate the TTS engine
                configureTextToSpeech()
            } else {
                if (LOG) {
                    Log.e("TTS", "TTS is not installed! Voice data is missing")
                }
            }
        }
    }

    private fun configureTextToSpeech() {
        tts = TextToSpeech(this, TextToSpeech.OnInitListener { status ->
            if (isFirstRun) {
                ActivityUtils.setPreferredTextToSpeechLocale(applicationContext, tts, status)
            } else {
                val localStr = ActivityUtils.getPreferenceTextToSpeechLocale(applicationContext)
                val local = ActivityUtils.getTextToSpeechLocale(localStr)
                ActivityUtils.configureTextToSpeechLanguage(tts, status, local)
            }
        })
    }

    /** Called when leaving the activity  */
    public override fun onPause() {
        mAdView?.pause()
        super.onPause()
    }

    /** Called when returning to the activity
     *  Guaranteed to be called after the Activity has been restored to its original state.
     *  See https://www.androiddesignpatterns.com/2013/08/fragment-transaction-commit-state-loss.html
     * */
    public override fun onPostResume() {
        super.onPostResume()
        mAdView?.resume()
    }

    /** Called before the activity is destroyed  */
    public override fun onDestroy() {
        mAdView?.destroy()
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_change_group -> {
                changeVerbGroup()
                return true
            }

            R.id.action_sort -> {
                sortVerbs()
                return true
            }

            R.id.action_most_common -> {
                showMostCommon()
                return true
            }

            R.id.action_search -> {
                ActivityUtils.launchSearchActivity(applicationContext)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * Creates a Fragment.
     * @param verbGroup Type of verbs to display TYPE_WEAK, TYPE_STRONG, TYPE_MIXED, ALL_GROUPS
     * @param itemType Display mode LIST, CARD
     * @param sortType Alphabet, groups
     * @param commonType Display Top 25, Top 50, Top 100, Top 300, Top 500
     * @return fragment
     */
    private fun createUniversalFragment(verbGroup: String, itemType: String,
                                        sortType: String, commonType: String): UniversalFragment {
        val fragment = UniversalFragment()
        val bundle = Bundle()
        bundle.putString(VERB_TYPE, verbGroup)
        bundle.putString(ITEM_TYPE, itemType)
        bundle.putString(SORT_TYPE, sortType)
        bundle.putString(COMMON_TYPE, commonType)
        fragment.arguments = bundle
        return fragment
    }

    /**
     * Changes the list according to the selected type (1er, 2nd, 3rd, all).
     */
    private fun changeVerbGroup() {
        val options = arrayOf<CharSequence>(getString(R.string.type_weak), getString(R.string.type_strong), getString(R.string.type_mixed), getString(R.string.all))

        val builder = AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
        builder.setTitle(getString(R.string.select_show_verbs))
        builder.setSingleChoiceItems(options, verbSelection[0]) { dialog, item ->
            // save the selected verb type
            verbSelection[0] = item
            verbGroup[0] = VERB_GROUPS[item]
        }
        builder.setPositiveButton(android.R.string.ok) { dialog, id ->
            // Change the selection.
            when (verbGroup[0]) {
                TYPE_WEAK, TYPE_STRONG, TYPE_MIXED, TYPE_ALL -> {
                    ActivityUtils.saveStringToPreferences(
                            applicationContext, DISPLAY_VERB_TYPE, verbGroup[0])
                    changeFragmentsDisplay()
                }

                else -> {
                }
            }
        }
        builder.show()
    }

    /**
     * Changes the sort order.
     */
    private fun sortVerbs() {
        val options = arrayOf<CharSequence>(getString(R.string.alphabet), getString(R.string.color), getString(R.string.type))

        val builder = AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
        builder.setTitle(getString(R.string.select_type_of_sort))
        builder.setSingleChoiceItems(options, sortSelection[0]) { dialog, item ->
            // save the selected verb type
            sortSelection[0] = item
            sortType[0] = SORT_TYPES[item]
        }
        builder.setPositiveButton(android.R.string.ok) { dialog, id ->
            // Change the selection.
            when (sortType[0]) {
                ALPHABET, COLOR, TYPE -> {
                    ActivityUtils.saveStringToPreferences(
                            applicationContext, DISPLAY_SORT_TYPE, sortType[0])
                    changeFragmentsDisplay()
                }

                else -> {
                }
            }
        }
        builder.show()
    }

    /**
     * Creates if needed a new fragment with the new display configurations.
     */
    private fun changeFragmentsDisplay() {
        if (!verbsFragment!!.verbGroup.contentEquals(verbGroup[0])
                || !verbsFragment!!.sortType.contentEquals(sortType[0])
                || !verbsFragment!!.commonType.contentEquals(commonType[0])) {
            verbsFragment = createUniversalFragment(verbGroup[0], LIST, sortType[0], commonType[0])
            if (page.contentEquals(PAGE_VERBS)) {
                launchFragment(PAGE_VERBS)
            }
        }
        if (!cardsFragment!!.verbGroup.contentEquals(verbGroup[0])
                || !cardsFragment!!.sortType.contentEquals(sortType[0])
                || !cardsFragment!!.commonType.contentEquals(commonType[0])) {
            cardsFragment = createUniversalFragment(verbGroup[0], CARD, sortType[0], commonType[0])
            if (page.contentEquals(PAGE_CARDS)) {
                launchFragment(PAGE_CARDS)
            }
        }
        if (!favoritesFragment!!.sortType.contentEquals(sortType[0])) {
            // Only allow to change sort order
            favoritesFragment = createUniversalFragment(FAVORITES,
                    ActivityUtils.getPreferenceFavoritesMode(applicationContext), sortType[0],
                    MOST_COMMON_ALL)
            if (page.contentEquals(PAGE_FAVORITES)) {
                launchFragment(PAGE_FAVORITES)
            }
        }
    }

    /**
     * Shows the most common verbs according to selection.
     */
    private fun showMostCommon() {
        val options = arrayOf<CharSequence>(
                getString(R.string.most_common_25), getString(R.string.most_common_50),
                getString(R.string.most_common_100), getString(R.string.most_common_300),
                getString(R.string.most_common_500), getString(R.string.all))

        val builder = AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
        builder.setTitle(getString(R.string.select_show_verbs))
        builder.setSingleChoiceItems(options, commonSelection[0]) { dialog, item ->
            // save the selected verb type
            commonSelection[0] = item
            commonType[0] = COMMON_TYPES[item]
        }
        builder.setPositiveButton(android.R.string.ok) { dialog, id ->
            // Change the selection.
            when (commonType[0]) {
                MOST_COMMON_25, MOST_COMMON_50, MOST_COMMON_100, MOST_COMMON_250,
                MOST_COMMON_500, MOST_COMMON_ALL -> {
                    ActivityUtils.saveStringToPreferences(
                            applicationContext, DISPLAY_COMMON_TYPE, commonType[0])
                    changeFragmentsDisplay()
                }

                else -> {
                }
            }
        }
        builder.show()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId

        when (id) {
            R.id.nav_verbs -> {
                page = PAGE_VERBS

                supportActionBar!!.setTitle(R.string.verbs)
                ActivityUtils.saveStringToPreferences(applicationContext, CURRENT_PAGE, PAGE_VERBS)
                launchFragment(PAGE_VERBS)
            }
            R.id.nav_cards -> {
                page = PAGE_CARDS

                supportActionBar!!.setTitle(R.string.cards)
                ActivityUtils.saveStringToPreferences(applicationContext, CURRENT_PAGE, PAGE_CARDS)
                launchFragment(PAGE_CARDS)
            }
            R.id.nav_favorites -> {
                page = PAGE_FAVORITES

                supportActionBar!!.setTitle(R.string.favorites)
                ActivityUtils.saveStringToPreferences(applicationContext, CURRENT_PAGE, PAGE_FAVORITES)
                launchFragment(PAGE_FAVORITES)
            }
            R.id.nav_settings -> ActivityUtils.launchSettingsActivity(applicationContext)
            R.id.nav_help -> ActivityUtils.launchHelpActivity(applicationContext)
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * Launches the selected fragment.
     * @param category The type of search
     */
    private fun launchFragment(category: String) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        when (category) {
            PAGE_VERBS -> {
                fragmentTransaction.replace(R.id.fragment_container, verbsFragment)
                fragmentTransaction.addToBackStack(null)
                fragmentTransaction.commit()
            }
            PAGE_CARDS -> {
                fragmentTransaction.replace(R.id.fragment_container, cardsFragment)
                fragmentTransaction.addToBackStack(null)
                fragmentTransaction.commit()
            }
            PAGE_FAVORITES -> {
                fragmentTransaction.replace(R.id.fragment_container, favoritesFragment)
                fragmentTransaction.addToBackStack(null)
                fragmentTransaction.commit()
            }
        }
    }

    /***
     * Shows the correct page on screen.
     * @param page name of page
     */
    private fun showPage(page: String) {
        when (page) {
            PAGE_VERBS -> {

                supportActionBar!!.setTitle(R.string.verbs)
                ActivityUtils.saveStringToPreferences(applicationContext, CURRENT_PAGE, PAGE_VERBS)
                ActivityUtils.firebaseAnalyticsLogEventSelectContent(
                        mFirebaseAnalytics!!, PAGE_VERBS, PAGE_VERBS, TYPE_PAGE)
                launchFragment(PAGE_VERBS)
            }
            PAGE_CARDS -> {

                supportActionBar!!.setTitle(R.string.verbs)
                ActivityUtils.saveStringToPreferences(applicationContext, CURRENT_PAGE, PAGE_CARDS)
                ActivityUtils.firebaseAnalyticsLogEventSelectContent(
                        mFirebaseAnalytics!!, PAGE_CARDS, PAGE_CARDS, TYPE_PAGE)
                launchFragment(PAGE_CARDS)
            }
            PAGE_FAVORITES -> {

                supportActionBar!!.setTitle(R.string.favorites)
                ActivityUtils.saveStringToPreferences(applicationContext, CURRENT_PAGE, PAGE_FAVORITES)
                ActivityUtils.firebaseAnalyticsLogEventSelectContent(
                        mFirebaseAnalytics!!, PAGE_FAVORITES, PAGE_FAVORITES, TYPE_PAGE)
                launchFragment(PAGE_FAVORITES)
            }
        }
    }

    private fun assignCheckedItem(page: String) {
        // set selected
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        when (page) {
            PAGE_VERBS -> navigationView.setCheckedItem(R.id.nav_verbs)
            PAGE_CARDS -> navigationView.setCheckedItem(R.id.nav_cards)
            PAGE_FAVORITES -> navigationView.setCheckedItem(R.id.nav_favorites)
        }
    }
}
