package com.xengar.android.deutscheverben.ui

import android.app.LoaderManager
import android.content.ContentValues
import android.content.CursorLoader
import android.content.Loader
import android.database.Cursor
import android.os.AsyncTask
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import com.android.colorpicker.ColorPickerPalette
import com.android.colorpicker.ColorPickerSwatch
import com.github.amlcurran.showcaseview.ShowcaseView
import com.github.amlcurran.showcaseview.targets.ViewTarget
import com.google.android.gms.ads.AdView
import com.google.firebase.analytics.FirebaseAnalytics
import com.xengar.android.deutscheverben.R
import com.xengar.android.deutscheverben.data.Conjugation
import com.xengar.android.deutscheverben.data.Verb
import com.xengar.android.deutscheverben.utils.ActivityUtils
import com.xengar.android.deutscheverben.utils.LogAdListener

import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_COLOR
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_ID
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.CONTENT_CONJUGATIONS_URI
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.CONTENT_FAVORITES_URI
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.CONTENT_VERBS_URI
import com.xengar.android.deutscheverben.utils.Constants.CONJUGATION_ID
import com.xengar.android.deutscheverben.utils.Constants.DEMO_MODE
import com.xengar.android.deutscheverben.utils.Constants.DETAILS_ACTIVITY
import com.xengar.android.deutscheverben.utils.Constants.DRAWABLE
import com.xengar.android.deutscheverben.utils.Constants.ER
import com.xengar.android.deutscheverben.utils.Constants.SIE
import com.xengar.android.deutscheverben.utils.Constants.ICH
import com.xengar.android.deutscheverben.utils.Constants.LOG
import com.xengar.android.deutscheverben.utils.Constants.WIR
import com.xengar.android.deutscheverben.utils.Constants.PAGE_VERB_DETAILS
import com.xengar.android.deutscheverben.utils.Constants.DU
import com.xengar.android.deutscheverben.utils.Constants.TYPE_ADD_FAV
import com.xengar.android.deutscheverben.utils.Constants.TYPE_DEL_FAV
import com.xengar.android.deutscheverben.utils.Constants.TYPE_PAGE
import com.xengar.android.deutscheverben.utils.Constants.TYPE_SHARE
import com.xengar.android.deutscheverben.utils.Constants.VERB
import com.xengar.android.deutscheverben.utils.Constants.VERBS
import com.xengar.android.deutscheverben.utils.Constants.VERB_ID
import com.xengar.android.deutscheverben.utils.Constants.VERB_NAME
import com.xengar.android.deutscheverben.utils.Constants.IHR

/**
 * DetailsActivity
 */
class DetailsActivity
    : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    companion object {

        private val TAG = DetailsActivity::class.java.simpleName
        private val VERB_LOADER = 0
        private val CONJUGATION_LOADER = 1
    }


    private var fabAdd:FloatingActionButton? = null
    private var fabDel:FloatingActionButton? = null
    private var verbName = ""
    private var verbID:Long = -1
    private var conjugationID:Long = -1
    private var verb:Verb? = null
    private var conjugation:Conjugation? = null
    private var tts:TextToSpeech? = null
    private var infinitive:TextView? = null
    private var group:TextView? = null
    private var definition:TextView? = null
    private var translation:TextView? = null
    private var principalParts:TextView? = null
    private var sample1:TextView? = null
    private var sample2:TextView? = null
    private var sample3:TextView? = null

    private var mFirebaseAnalytics:FirebaseAnalytics? = null
    private var mAdView:AdView? = null

    // Demo
    private var showcaseView:ShowcaseView? = null
    private var scrollView:NestedScrollView? = null
    private var demo = false
    private var counter = 0


    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        ActivityUtils.firebaseAnalyticsLogEventSelectContent(
                mFirebaseAnalytics!!, PAGE_VERB_DETAILS, PAGE_VERB_DETAILS, TYPE_PAGE)

        // create AdMob banner
        val listener = LogAdListener(mFirebaseAnalytics!!, DETAILS_ACTIVITY)
        mAdView = ActivityUtils.createAdMobBanner(this, listener)

        // changing status bar color
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = this.resources.getColor(R.color.colorPrimaryDark)
        }
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val bundle = intent.extras
        if (bundle != null) {
            demo = bundle.getBoolean(DEMO_MODE, false)
            verbName = bundle.getString(VERB_NAME, "")
            verbID = bundle.getLong(VERB_ID, -1)
            conjugationID = bundle.getLong(CONJUGATION_ID, -1)
        }
        else {
            if (LOG) {
                Log.e(TAG, "bundle is null! This should not happen. verbId needed")
            }
        }

        // define click listeners
        setClickListeners()

        infinitive = findViewById(R.id.infinitive)
        group = findViewById(R.id.groupe)
        definition = findViewById(R.id.definition)
        translation = findViewById(R.id.translation)
        principalParts = findViewById(R.id.principalParts)
        sample1 = findViewById(R.id.sample1)
        sample2 = findViewById(R.id.sample2)
        sample3 = findViewById(R.id.sample3)
        scrollView = findViewById(R.id.scroll)

        // Initialize a loader to read the verb data from the database and display it
        loaderManager.initLoader(VERB_LOADER, null, this)
        showFavoriteButtons()

        if (demo) {
            defineDemoMode()
        }
        configureTask().execute()
    }

    inner class configureTask : AsyncTask<Int?, Int, Boolean>() {

        override fun doInBackground(vararg params: Int?): Boolean {
            // Assume we have TTS voice data
            configureTextToSpeech()
            return true
        }

        override fun onPostExecute(param: Boolean) {
            super.onPostExecute(param)
        }
    }

    private fun configureTextToSpeech() {
        tts = TextToSpeech(this, TextToSpeech.OnInitListener { status ->
            val localStr = ActivityUtils.getPreferenceTextToSpeechLocale(applicationContext)
            val local = ActivityUtils.getTextToSpeechLocale(localStr)
            ActivityUtils.configureTextToSpeechLanguage(tts, status, local) })
    }

    /**
     * Set click listeners to this object.
     */
    private fun setClickListeners() {
        findViewById<View>(R.id.play_infinitive).setOnClickListener(this)
        findViewById<View>(R.id.play_definition).setOnClickListener(this)
        findViewById<View>(R.id.play_sample1).setOnClickListener(this)
        findViewById<View>(R.id.play_sample2).setOnClickListener(this)
        findViewById<View>(R.id.play_sample3).setOnClickListener(this)
        
        findViewById<View>(R.id.play_indikativ_prasens_ich).setOnClickListener(this)
        findViewById<View>(R.id.play_indikativ_prasens_du).setOnClickListener(this)
        findViewById<View>(R.id.play_indikativ_prasens_er).setOnClickListener(this)
        findViewById<View>(R.id.play_indikativ_prasens_wir).setOnClickListener(this)
        findViewById<View>(R.id.play_indikativ_prasens_ihr).setOnClickListener(this)
        findViewById<View>(R.id.play_indikativ_prasens_sie).setOnClickListener(this)
        findViewById<View>(R.id.play_indikativ_prateritum_ich).setOnClickListener(this)
        findViewById<View>(R.id.play_indikativ_prateritum_du).setOnClickListener(this)
        findViewById<View>(R.id.play_indikativ_prateritum_er).setOnClickListener(this)
        findViewById<View>(R.id.play_indikativ_prateritum_wir).setOnClickListener(this)
        findViewById<View>(R.id.play_indikativ_prateritum_ihr).setOnClickListener(this)
        findViewById<View>(R.id.play_indikativ_prateritum_sie).setOnClickListener(this)
        findViewById<View>(R.id.play_indikativ_perfekt_ich).setOnClickListener(this)
        findViewById<View>(R.id.play_indikativ_perfekt_du).setOnClickListener(this)
        findViewById<View>(R.id.play_indikativ_perfekt_er).setOnClickListener(this)
        findViewById<View>(R.id.play_indikativ_perfekt_wir).setOnClickListener(this)
        findViewById<View>(R.id.play_indikativ_perfekt_ihr).setOnClickListener(this)
        findViewById<View>(R.id.play_indikativ_perfekt_sie).setOnClickListener(this)
        findViewById<View>(R.id.play_indikativ_plusquamperfekt_ich).setOnClickListener(this)
        findViewById<View>(R.id.play_indikativ_plusquamperfekt_du).setOnClickListener(this)
        findViewById<View>(R.id.play_indikativ_plusquamperfekt_er).setOnClickListener(this)
        findViewById<View>(R.id.play_indikativ_plusquamperfekt_wir).setOnClickListener(this)
        findViewById<View>(R.id.play_indikativ_plusquamperfekt_ihr).setOnClickListener(this)
        findViewById<View>(R.id.play_indikativ_plusquamperfekt_sie).setOnClickListener(this)
        findViewById<View>(R.id.play_indikativ_futur1_ich).setOnClickListener(this)
        findViewById<View>(R.id.play_indikativ_futur1_du).setOnClickListener(this)
        findViewById<View>(R.id.play_indikativ_futur1_er).setOnClickListener(this)
        findViewById<View>(R.id.play_indikativ_futur1_wir).setOnClickListener(this)
        findViewById<View>(R.id.play_indikativ_futur1_ihr).setOnClickListener(this)
        findViewById<View>(R.id.play_indikativ_futur1_sie).setOnClickListener(this)
        findViewById<View>(R.id.play_indikativ_futur2_ich).setOnClickListener(this)
        findViewById<View>(R.id.play_indikativ_futur2_du).setOnClickListener(this)
        findViewById<View>(R.id.play_indikativ_futur2_er).setOnClickListener(this)
        findViewById<View>(R.id.play_indikativ_futur2_wir).setOnClickListener(this)
        findViewById<View>(R.id.play_indikativ_futur2_ihr).setOnClickListener(this)
        findViewById<View>(R.id.play_indikativ_futur2_sie).setOnClickListener(this)

        findViewById<View>(R.id.play_konjunktiv1_prasens_ich).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv1_prasens_du).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv1_prasens_er).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv1_prasens_wir).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv1_prasens_ihr).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv1_prasens_sie).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv1_perfekt_ich).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv1_perfekt_du).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv1_perfekt_er).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv1_perfekt_wir).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv1_perfekt_ihr).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv1_perfekt_sie).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv1_futur1_ich).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv1_futur1_du).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv1_futur1_er).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv1_futur1_wir).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv1_futur1_ihr).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv1_futur1_sie).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv1_futur2_ich).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv1_futur2_du).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv1_futur2_er).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv1_futur2_wir).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv1_futur2_ihr).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv1_futur2_sie).setOnClickListener(this)

        findViewById<View>(R.id.play_konjunktiv2_prateritum_ich).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv2_prateritum_du).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv2_prateritum_er).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv2_prateritum_wir).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv2_prateritum_ihr).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv2_prateritum_sie).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv2_plusquamperfekt_ich).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv2_plusquamperfekt_du).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv2_plusquamperfekt_er).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv2_plusquamperfekt_wir).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv2_plusquamperfekt_ihr).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv2_plusquamperfekt_sie).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv2_futur1_ich).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv2_futur1_du).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv2_futur1_er).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv2_futur1_wir).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv2_futur1_ihr).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv2_futur1_sie).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv2_futur2_ich).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv2_futur2_du).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv2_futur2_er).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv2_futur2_wir).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv2_futur2_ihr).setOnClickListener(this)
        findViewById<View>(R.id.play_konjunktiv2_futur2_sie).setOnClickListener(this)

        findViewById<View>(R.id.play_imperativ_prasens_du).setOnClickListener(this)
        findViewById<View>(R.id.play_imperativ_prasens_ihr).setOnClickListener(this)
        findViewById<View>(R.id.play_imperativ_prasens_sie).setOnClickListener(this)

        findViewById<View>(R.id.play_infinitiv_prasens).setOnClickListener(this)
        findViewById<View>(R.id.play_infinitiv_perfekt).setOnClickListener(this)
        findViewById<View>(R.id.play_partizip_prasens).setOnClickListener(this)
        findViewById<View>(R.id.play_partizip_perfekt).setOnClickListener(this)
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

    override fun onCreateOptionsMenu(menu:Menu):Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.details, menu)
        return true
    }

    override fun onOptionsItemSelected(item:MenuItem):Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        when (id) {
            R.id.action_change_color -> {
                changeColorDialog()
                return true
            }

            R.id.action_search -> {
                ActivityUtils.launchSearchActivity(applicationContext)
                return true
            }

            R.id.action_share -> {
                ActivityUtils.launchShareText(this, createShareText())
                val verbName = if (verb != null) verb!!.infinitive else "verb name not available"
                ActivityUtils.firebaseAnalyticsLogEventSelectContent(mFirebaseAnalytics!!,
                        "Verb: $verbName, VerbId: $verbID", TYPE_SHARE, TYPE_SHARE)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * Create the text to share.
     * @return String
     */
    private fun createShareText():String {
        var text = ""
        if (verb != null)
        {
            text = (getString(R.string.verbe) + ": " + verb!!.infinitive
                    + "\n\n" + getString(R.string.infinitive) + " " + getString(R.string.prasens) + ":"
                    + "\n" + conjugation!!.infinitivPrasens
                    + "\n\n" + getString(R.string.infinitive) + " " + getString(R.string.perfekt) + ":"
                    + "\n" + conjugation!!.infinitivPerfekt
                    + "\n\n" + getString(R.string.partizip) + " " + getString(R.string.prasens) + ":"
                    + "\n" + conjugation!!.partizipPrasens
                    + "\n\n" + getString(R.string.partizip) + " " + getString(R.string.perfekt) + ":"
                    + "\n" + conjugation!!.partizipPerfekt
                    + "\n" + getString(R.string.group) + ": " + verb!!.group
                    + "\n\n" + getString(R.string.definition) + ":\n" + verb!!.definition
                    + "\n\n" + getString(R.string.examples) + ":\n" + verb!!.sample1
                    + "\n" + verb!!.sample2
                    + "\n" + verb!!.sample3
                    + "\n\n" + getString(R.string.imperativ) + ":"
                    + "\n" + conjugation!!.imperativDu
                    + "\n" + conjugation!!.imperativIhr
                    + "\n" + conjugation!!.imperativSie
                    + "\n\n" + getString(R.string.indikativ) + " " + getString(R.string.prasens) + ":"
                    + "\n" + conjugation!!.indikativPrasensIch
                    + "\n" + conjugation!!.indikativPrasensDu
                    + "\n" + conjugation!!.indikativPrasensEr
                    + "\n" + conjugation!!.indikativPrasensWir
                    + "\n" + conjugation!!.indikativPrasensIhr
                    + "\n" + conjugation!!.indikativPrasensSie
                    + "\n\n" + getString(R.string.indikativ) + " " + getString(R.string.prateritum) + ":"
                    + "\n" + conjugation!!.indikativPrateritumIch
                    + "\n" + conjugation!!.indikativPrateritumDu
                    + "\n" + conjugation!!.indikativPrateritumEr
                    + "\n" + conjugation!!.indikativPrateritumWir
                    + "\n" + conjugation!!.indikativPrateritumIhr
                    + "\n" + conjugation!!.indikativPrateritumSie
                    + "\n\n" + getString(R.string.indikativ) + " " + getString(R.string.perfekt) + ":"
                    + "\n" + conjugation!!.indikativPerfektIch
                    + "\n" + conjugation!!.indikativPerfektDu
                    + "\n" + conjugation!!.indikativPerfektEr
                    + "\n" + conjugation!!.indikativPerfektWir
                    + "\n" + conjugation!!.indikativPerfektIhr
                    + "\n" + conjugation!!.indikativPerfektSie
                    + "\n\n" + getString(R.string.indikativ) + " " + getString(R.string.plusquamperfekt) + ":"
                    + "\n" + conjugation!!.indikativPlusquamperfektIch
                    + "\n" + conjugation!!.indikativPlusquamperfektDu
                    + "\n" + conjugation!!.indikativPlusquamperfektEr
                    + "\n" + conjugation!!.indikativPlusquamperfektWir
                    + "\n" + conjugation!!.indikativPlusquamperfektIhr
                    + "\n" + conjugation!!.indikativPlusquamperfektSie
                    + "\n\n" + getString(R.string.indikativ) + " " + getString(R.string.futur1) + ":"
                    + "\n" + conjugation!!.indikativFutur1Ich
                    + "\n" + conjugation!!.indikativFutur1Du
                    + "\n" + conjugation!!.indikativFutur1Er
                    + "\n" + conjugation!!.indikativFutur1Wir
                    + "\n" + conjugation!!.indikativFutur1Ihr
                    + "\n" + conjugation!!.indikativFutur1Sie
                    + "\n\n" + getString(R.string.indikativ) + " " + getString(R.string.futur2) + ":"
                    + "\n" + conjugation!!.indikativFutur2Ich
                    + "\n" + conjugation!!.indikativFutur2Du
                    + "\n" + conjugation!!.indikativFutur2Er
                    + "\n" + conjugation!!.indikativFutur2Wir
                    + "\n" + conjugation!!.indikativFutur2Ihr
                    + "\n" + conjugation!!.indikativFutur2Sie
                    + "\n\n" + getString(R.string.konjunktiv1) + " " + getString(R.string.prasens) + ":"
                    + "\n" + conjugation!!.konjunktiv1PrasensIch
                    + "\n" + conjugation!!.konjunktiv1PrasensDu
                    + "\n" + conjugation!!.konjunktiv1PrasensEr
                    + "\n" + conjugation!!.konjunktiv1PrasensWir
                    + "\n" + conjugation!!.konjunktiv1PrasensIhr
                    + "\n" + conjugation!!.konjunktiv1PrasensSie
                    + "\n\n" + getString(R.string.konjunktiv1) + " " + getString(R.string.perfekt) + ":"
                    + "\n" + conjugation!!.konjunktiv1PerfektIch
                    + "\n" + conjugation!!.konjunktiv1PerfektDu
                    + "\n" + conjugation!!.konjunktiv1PerfektEr
                    + "\n" + conjugation!!.konjunktiv1PerfektWir
                    + "\n" + conjugation!!.konjunktiv1PerfektIhr
                    + "\n" + conjugation!!.konjunktiv1PerfektSie
                    + "\n\n" + getString(R.string.konjunktiv1) + " " + getString(R.string.futur1) + ":"
                    + "\n" + conjugation!!.konjunktiv1Futur1Ich
                    + "\n" + conjugation!!.konjunktiv1Futur1Du
                    + "\n" + conjugation!!.konjunktiv1Futur1Er
                    + "\n" + conjugation!!.konjunktiv1Futur1Wir
                    + "\n" + conjugation!!.konjunktiv1Futur1Ihr
                    + "\n" + conjugation!!.konjunktiv1Futur1Sie
                    + "\n\n" + getString(R.string.konjunktiv1) + " " + getString(R.string.futur2) + ":"
                    + "\n" + conjugation!!.konjunktiv1Futur2Ich
                    + "\n" + conjugation!!.konjunktiv1Futur2Du
                    + "\n" + conjugation!!.konjunktiv1Futur2Er
                    + "\n" + conjugation!!.konjunktiv1Futur2Wir
                    + "\n" + conjugation!!.konjunktiv1Futur2Ihr
                    + "\n" + conjugation!!.konjunktiv1Futur2Sie
                    + "\n\n" + getString(R.string.konjunktiv2) + " " + getString(R.string.prateritum) + ":"
                    + "\n" + conjugation!!.konjunktiv2PrateritumIch
                    + "\n" + conjugation!!.konjunktiv2PrateritumDu
                    + "\n" + conjugation!!.konjunktiv2PrateritumEr
                    + "\n" + conjugation!!.konjunktiv2PrateritumWir
                    + "\n" + conjugation!!.konjunktiv2PrateritumIhr
                    + "\n" + conjugation!!.konjunktiv2PrateritumSie
                    + "\n\n" + getString(R.string.konjunktiv2) + " " + getString(R.string.plusquamperfekt) + ":"
                    + "\n" + conjugation!!.konjunktiv2PlusquamperfektIch
                    + "\n" + conjugation!!.konjunktiv2PlusquamperfektDu
                    + "\n" + conjugation!!.konjunktiv2PlusquamperfektEr
                    + "\n" + conjugation!!.konjunktiv2PlusquamperfektWir
                    + "\n" + conjugation!!.konjunktiv2PlusquamperfektIhr
                    + "\n" + conjugation!!.konjunktiv2PlusquamperfektSie
                    + "\n\n" + getString(R.string.konjunktiv2) + " " + getString(R.string.futur1) + ":"
                    + "\n" + conjugation!!.konjunktiv2Futur1Ich
                    + "\n" + conjugation!!.konjunktiv2Futur1Du
                    + "\n" + conjugation!!.konjunktiv2Futur1Er
                    + "\n" + conjugation!!.konjunktiv2Futur1Wir
                    + "\n" + conjugation!!.konjunktiv2Futur1Ihr
                    + "\n" + conjugation!!.konjunktiv2Futur1Sie
                    + "\n\n" + getString(R.string.konjunktiv2) + " " + getString(R.string.futur2) + ":"
                    + "\n" + conjugation!!.konjunktiv2Futur2Ich
                    + "\n" + conjugation!!.konjunktiv2Futur2Du
                    + "\n" + conjugation!!.konjunktiv2Futur2Er
                    + "\n" + conjugation!!.konjunktiv2Futur2Wir
                    + "\n" + conjugation!!.konjunktiv2Futur2Ihr
                    + "\n" + conjugation!!.konjunktiv2Futur2Sie)
        }
        return text
    }

    /**
     * Changes the color
     */
    private fun changeColorDialog() {
        val colors = intArrayOf(
                ContextCompat.getColor(applicationContext, R.color.colorBlack),
                ContextCompat.getColor(applicationContext, R.color.colorRed),
                ContextCompat.getColor(applicationContext, R.color.colorGreen),
                ContextCompat.getColor(applicationContext, R.color.colorBlue),
                ContextCompat.getColor(applicationContext, R.color.colorPink),
                ContextCompat.getColor(applicationContext, R.color.colorPurple),
                ContextCompat.getColor(applicationContext, R.color.colorDeepPurple),
                ContextCompat.getColor(applicationContext, R.color.colorIndigo),
                ContextCompat.getColor(applicationContext, R.color.colorOrange),
                ContextCompat.getColor(applicationContext, R.color.colorDeepOrange),
                ContextCompat.getColor(applicationContext, R.color.colorBrown),
                ContextCompat.getColor(applicationContext, R.color.colorBlueGray))

        val selectedColor = intArrayOf(colors[0])
        if (verb != null) {
            selectedColor[0] = verb!!.color
        }
        val layoutInflater = LayoutInflater.from(applicationContext)
        val colorPickerPalette = layoutInflater.inflate(R.layout.custom_picker, null) as ColorPickerPalette

        val listener = ColorPickerSwatch.OnColorSelectedListener { color ->
            selectedColor[0] = color
            colorPickerPalette.drawPalette(colors, selectedColor[0])
        }

        colorPickerPalette.init(colors.size, 4, listener)
        colorPickerPalette.drawPalette(colors, selectedColor[0])

        val alert = AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                .setTitle(R.string.select_color)
                .setPositiveButton(android.R.string.ok ){ dialog, which ->
                    // Save changes
                    saveColor(selectedColor[0])
                    setVerbColor(selectedColor[0])
                    verb!!.color = selectedColor[0]
                }
                .setView(colorPickerPalette)
                .create()
        alert.show()
    }

    /**
     * Save the color to database.
     * @param color Color
     */
    private fun saveColor(color:Int) {
        val values = ContentValues()
        values.put(COLUMN_COLOR, "" + color)
        val rowsAffected = contentResolver.update(CONTENT_VERBS_URI, values,
                "$COLUMN_ID = ?", arrayOf(java.lang.Long.toString(verbID)))

        // Show a toast message depending on whether or not the update was successful.
        if (rowsAffected == 0) {
            // If no rows were affected, then there was an error with the update.
            if (LOG) {
                Log.e(TAG, "Failed to change color to verb!")
            }
        }
    }

    /**
     * Defines if add or remove from Favorites should be initially visible for this movieId.
     */
    private fun showFavoriteButtons() {
        fabAdd = findViewById(R.id.fab_add)
        fabDel = findViewById(R.id.fab_minus)

        val cursor = contentResolver.query(CONTENT_FAVORITES_URI, arrayOf(COLUMN_ID), //select
                "$COLUMN_ID = ?", // where
                arrayOf(java.lang.Long.toString(verbID)), null)//whereArgs
        if (cursor != null && cursor.count != 0) {
            fabDel!!.visibility = View.VISIBLE
        }
        else {
            fabAdd!!.visibility = View.VISIBLE
        }
        cursor?.close()
    }

    /**
     * Defines what to do when click on add/remove from Favorites buttons.
     */
    private fun defineClickFavoriteButtons() {
        val DURATION = 1000

        fabAdd!!.setOnClickListener{ view ->
            Snackbar.make(view, getString(R.string.favorites_add_message), DURATION)
                    .setAction("Action", null).show()
            val values = ContentValues()
            values.put(COLUMN_ID, verbID)
            contentResolver.insert(CONTENT_FAVORITES_URI, values)

            fabAdd!!.visibility = View.INVISIBLE
            fabDel!!.visibility = View.VISIBLE
            val verbName = if (verb != null) verb!!.infinitive else "verb name not available"
            ActivityUtils.firebaseAnalyticsLogEventSelectContent(mFirebaseAnalytics!!,
                    "$VERB_ID $verbID", verbName, TYPE_ADD_FAV)
        }

        fabDel!!.setOnClickListener{ view ->
            Snackbar.make(view, getString(R.string.favorites_del_message), DURATION)
                    .setAction("Action", null).show()
            contentResolver.delete(CONTENT_FAVORITES_URI,
                    "$COLUMN_ID = ?",
                    arrayOf(java.lang.Long.toString(verbID)))

            fabAdd!!.visibility = View.VISIBLE
            fabDel!!.visibility = View.INVISIBLE
            val verbName = if (verb != null) verb!!.infinitive else "verb name not available"
            ActivityUtils.firebaseAnalyticsLogEventSelectContent(mFirebaseAnalytics!!,
                    "$VERB_ID $verbID", verbName, TYPE_DEL_FAV)
        }
    }


    override fun onCreateLoader(id:Int, args:Bundle?):Loader<Cursor>? {
        var cursorLoader:CursorLoader? = null
        when (id) {
            CONJUGATION_LOADER -> cursorLoader = CursorLoader(applicationContext,
                    CONTENT_CONJUGATIONS_URI, ActivityUtils.allConjugationColumns(), // Columns in the resulting Cursor
                    "$COLUMN_ID = ?", // selection clause
                    arrayOf(java.lang.Long.toString(conjugationID)), null)// selection arguments
        // Default sort order

            VERB_LOADER -> cursorLoader = CursorLoader(applicationContext, // Parent activity context
                    CONTENT_VERBS_URI, ActivityUtils.allVerbColumns(), // Columns in the resulting Cursor
                    "$COLUMN_ID = ?", // selection clause
                    arrayOf(java.lang.Long.toString(verbID)), null)// selection arguments
        // Default sort order
            else -> {}
        }
        return cursorLoader
    }

    override fun onLoadFinished(loader:Loader<Cursor>, cursor:Cursor?) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.count < 1) {
            //finish();
            return   // the verb or conjugation doesn't exist, this should not happen.
        }

        when (loader.id) {
            CONJUGATION_LOADER -> if (cursor.moveToFirst()) {
                conjugation = ActivityUtils.conjugationFromCursor(cursor)
                processConjugation(conjugation!!)
                fillConjugationDetails(conjugation!!)
            }

            VERB_LOADER -> {
                // Proceed with moving to the first row of the cursor and reading data from it
                // (This should be the only row in the cursor)
                if (cursor.moveToFirst()) {
                    verb = ActivityUtils.verbFromCursor(cursor)
                    supportActionBar?.title = verb?.infinitive
                    setVerbColor(verb!!.color)
                    fillVerbDetails(verb!!)
                    defineClickFavoriteButtons()
                }
                loaderManager.initLoader(CONJUGATION_LOADER, null, this)
            }
            else -> {}
        }
    }

    /**
     * Handles the conjugation of the verb.
     * @param c Conjugation
     */
    private fun processConjugation(c:Conjugation) {

        if (c.infinitivPrasens.isNotEmpty() && !c.infinitivPrasens.contentEquals(verbName)) {
            // if we need, conjugate the verb model.
            var changePPwFI = false
            if (!verb!!.notes.isEmpty()) {
                val arrayNotes = verb!!.notes.split(", ".toRegex()).dropLastWhile{ it.isEmpty() }.toTypedArray()
                for (note in arrayNotes) {
                    if (note.contentEquals("changePPwFI")) {
                        // Past Participle in parentheses when immediately preceded by another infinitive
                        changePPwFI = true
                    }
                }
            }

            // TODO: Conjugate verb
            //conjugateVerb(c, verbName)

            if (changePPwFI) {
                c.partizipPerfekt = verb!!.pastParticiple
            }
        }

        addPronouns(c)
    }

    override fun onLoaderReset(loader:Loader<Cursor>) {

    }

    /**
     * Fill verb details.
     * @param verb Verb
     */
    private fun fillVerbDetails(verb:Verb) {
        // Update the views on the screen with the values from the database
        infinitive?.text = verb.infinitive
        when (verb.group) {
            1 -> group?.text = getString(R.string.group1)
            2 -> group?.text = getString(R.string.group2)
            3 -> group?.text = getString(R.string.group3)
        }

        principalParts?.text = verb.pastHe + ", " + verb.pastParticiple + ", " + verb.presentHe
        definition?.text = verb.definition
        sample1?.text = verb.sample1
        sample2?.text = verb.sample2
        sample3?.text = verb.sample3

        val fontSize = Integer.parseInt(ActivityUtils.getPreferenceFontSize(applicationContext))
        (findViewById<View>(R.id.groupe) as TextView).setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
        (findViewById<View>(R.id.definition_title) as TextView).setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
        (findViewById<View>(R.id.examples_title) as TextView).setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
        principalParts?.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
        definition?.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
        sample1?.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
        sample2?.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
        sample3?.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())

        ActivityUtils.setTranslation(applicationContext, translation, verb)

        // Try to put the verb image
        val imageVerb = findViewById<ImageView>(R.id.verb_image)
        val imageId = resources.getIdentifier(VERB + "lehren"/*verb.image*/, DRAWABLE, packageName)
        if (imageId != 0) {
            ActivityUtils.setImage(applicationContext, imageVerb, imageId)
        }

        ActivityUtils.firebaseAnalyticsLogEventViewItem(
                mFirebaseAnalytics!!, "" + verbID, verb.infinitive, VERBS)
    }


    /**
     * Conjugates the verb according to the model.
     * @param c Conjugation conjugation
     * @param isPronominal boolean
     * @param verbInfinitive String
     *//*
    private fun conjugateVerb(c:Conjugation, verbInfinitive:String, isPronominal:Boolean) {
        // Generate verb radicals for each time and person based on the radical's model.
        val modelRadicals = ArrayList<String>()
        val verbRadicals = ArrayList<String>()
        val modelRs = c.radicals
        if (modelRs.isNotEmpty()) {
            val arrayModelRs = modelRs.split(", ".toRegex()).dropLastWhile{ it.isEmpty() }.toTypedArray()
            for (modelR in arrayModelRs) {
                modelRadicals.add(modelR)
                val verbR = generateRadical(verbInfinitive, modelR, c.id.toInt(), isPronominal)
                verbRadicals.add(verbR)
            }
            replaceRadicals(c, modelRadicals, verbRadicals)
        }

        // Exceptions to the conjugation model
        replaceParticipePasse(c, verbInfinitive)
    }*/


    /**
     * Known Exceptions for the participe passe in conjugation model.
     * @param c Conjugation
     * @param verbInfinitive infinitive
     */
    private fun replaceParticipePasse(c:Conjugation, verbInfinitive:String) {
        /*
        var old = ""
        var new = ""

        when (c.id) {
            20L -> // finir
                if (verbInfinitive.contains("maudire")) {
                    old = "maudi"
                    new = "maudit"
                    c.partizipPerfekt = "maudit (e)"
                }
            46L -> // mouvoir
                if (verbInfinitive.contains("promouvoir")) {
                    old = "promû"
                    new = "promu"
                    c.partizipPerfekt = "promu (u, ue, us, ues)"
                } else if (verbInfinitive.contains("émouvoir")) {
                    old = "émû"
                    new = "ému"
                    c.partizipPerfekt = "ému (u, ue, us, ues)"
                }
            77L -> // conclure
                if (verbInfinitive.contains("inclure")) {
                    old = "inclu"
                    new = "inclus"
                    c.partizipPerfekt = "inclus (e, es)"
                } else if (verbInfinitive.contains("occlure")) {
                    old = "occlu"
                    new = "occlus"
                    c.partizipPerfekt = "occlus (e, es)"
                }
            78L -> // absoudre
                if (verbInfinitive.contains("résoudre")) {
                    old = "résous"
                    new = "résolu"
                    c.partizipPerfekt = "résolu (u, ue, us, ues)"
                }
            87L -> // confire
                if (verbInfinitive.contains("circoncire")) {
                    old = "circoncit"
                    new = "circoncis"
                    c.partizipPerfekt = "circoncis"
                } else if (verbInfinitive.contains("suffire")) {
                    old = "suffit"
                    new = "suffi"
                    c.partizipPerfekt = "suffi"
                }
        }

        if (old.isNotEmpty() && new.isNotEmpty()) {
            replaceParticipePasse(c, old, new)
        }*/
    }

    /*
    private fun replaceParticipePasse(c:Conjugation, old:String, new:String) {
        c.infinitivPerfekt = c.infinitivPerfekt.replace(old, new)

        c.participePasse2 = c.participePasse2.replace(old, new)
        c.gerondifPasse = c.gerondifPasse.replace(old, new)

        c.imperatifPasseDu = c.imperatifPasseDu.replace(old, new)
        c.imperatifPasseWir = c.imperatifPasseWir.replace(old, new)
        c.imperatifPasseIhr = c.imperatifPasseIhr.replace(old, new)

        c.indikativPrateritumIch = c.indikativPrateritumIch.replace(old, new)
        c.indikativPrateritumDu = c.indikativPrateritumDu.replace(old, new)
        c.indikativPrateritumEr = c.indikativPrateritumEr.replace(old, new)
        c.indikativPrateritumWir = c.indikativPrateritumWir.replace(old, new)
        c.indikativPrateritumIhr = c.indikativPrateritumIhr.replace(old, new)
        c.indikativPrateritumSie = c.indikativPrateritumSie.replace(old, new)

        c.indikativPlusquamperfektIch = c.indikativPlusquamperfektIch.replace(old, new)
        c.indikativPlusquamperfektDu = c.indikativPlusquamperfektDu.replace(old, new)
        c.indikativPlusquamperfektEr = c.indikativPlusquamperfektEr.replace(old, new)
        c.indikativPlusquamperfektWir = c.indikativPlusquamperfektWir.replace(old, new)
        c.indikativPlusquamperfektIhr = c.indikativPlusquamperfektIhr.replace(old, new)
        c.indikativPlusquamperfektSie = c.indikativPlusquamperfektSie.replace(old, new)

        c.indikativFutur2Ich = c.indikativFutur2Ich.replace(old, new)
        c.indikativFutur2Du = c.indikativFutur2Du.replace(old, new)
        c.indikativFutur2Er = c.indikativFutur2Er.replace(old, new)
        c.indikativFutur2Wir = c.indikativFutur2Wir.replace(old, new)
        c.indikativFutur2Ihr = c.indikativFutur2Ihr.replace(old, new)
        c.indikativFutur2Sie = c.indikativFutur2Sie.replace(old, new)

        c.konjunktiv1Futur2Ich = c.konjunktiv1Futur2Ich.replace(old, new)
        c.konjunktiv1Futur2Du = c.konjunktiv1Futur2Du.replace(old, new)
        c.konjunktiv1Futur2Er = c.konjunktiv1Futur2Er.replace(old, new)
        c.konjunktiv1Futur2Wir = c.konjunktiv1Futur2Wir.replace(old, new)
        c.konjunktiv1Futur2Ihr = c.konjunktiv1Futur2Ihr.replace(old, new)
        c.konjunktiv1Futur2Sie = c.konjunktiv1Futur2Sie.replace(old, new)

        c.konjunktiv1PerfektIch = c.konjunktiv1PerfektIch.replace(old, new)
        c.konjunktiv1PerfektDu = c.konjunktiv1PerfektDu.replace(old, new)
        c.konjunktiv1PerfektEr = c.konjunktiv1PerfektEr.replace(old, new)
        c.konjunktiv1PerfektWir = c.konjunktiv1PerfektWir.replace(old, new)
        c.konjunktiv1PerfektIhr = c.konjunktiv1PerfektIhr.replace(old, new)
        c.konjunktiv1PerfektSie = c.konjunktiv1PerfektSie.replace(old, new)

        c.konjunktiv2PlusquamperfektIch = c.konjunktiv2PlusquamperfektIch.replace(old, new)
        c.konjunktiv2PlusquamperfektDu = c.konjunktiv2PlusquamperfektDu.replace(old, new)
        c.konjunktiv2PlusquamperfektEr = c.konjunktiv2PlusquamperfektEr.replace(old, new)
        c.konjunktiv2PlusquamperfektWir = c.konjunktiv2PlusquamperfektWir.replace(old, new)
        c.konjunktiv2PlusquamperfektIhr = c.konjunktiv2PlusquamperfektIhr.replace(old, new)
        c.konjunktiv2PlusquamperfektSie = c.konjunktiv2PlusquamperfektSie.replace(old, new)

        c.konjunktiv2Futur2Ich = c.konjunktiv2Futur2Ich.replace(old, new)
        c.konjunktiv2Futur2Du = c.konjunktiv2Futur2Du.replace(old, new)
        c.konjunktiv2Futur2Er = c.konjunktiv2Futur2Er.replace(old, new)
        c.konjunktiv2Futur2Wir = c.konjunktiv2Futur2Wir.replace(old, new)
        c.konjunktiv2Futur2Ihr = c.konjunktiv2Futur2Ihr.replace(old, new)
        c.konjunktiv2Futur2Sie = c.konjunktiv2Futur2Sie.replace(old, new)
    }*/

    /**
     * Generates the verb radical based on the model.
     * @param infinitive String verb
     * @param modelR String radical of the model
     * @param id int model id
     * @param isPronominal boolean
     * @return list of radicals
     *//*
    private fun generateRadical(infinitive:String, modelR:String, id:Int, isPronominal:Boolean):String {
        var verbR = infinitive
        // remove termination
        when {
            infinitive.endsWith("er") -> verbR = infinitive.substring(0, infinitive.length - 2)
            infinitive.endsWith("ir") -> verbR = infinitive.substring(0, infinitive.length - 2)
            infinitive.endsWith("re") -> verbR = infinitive.substring(0, infinitive.length - 2)
        }

        // TODO: Check all models (after 40+)
        // know models
        when (id) {
            8 -> // placer, plaçer : verbes en -cer
                if (modelR.contains("ç")) {
                    verbR = if (infinitive.endsWith("cer")) infinitive.replace("cer", "ç") else verbR
                }
            10 -> // peser, pèser : verbes ayant un e muet à l'avant dèrniere syllabe de l'infinitif: verbes en e(.)er
                if (modelR.contains("è")) {
                    val last = if (verbR.contains("e")) verbR.lastIndexOf("e") else -1
                    if (last > -1) {
                        verbR = verbR.substring(0, last) + "è" + verbR.substring(last + 1, verbR.length)
                    }
                }
            11 -> // céder, cède : verbes ayant un e muet à l'avant dèrniere syllabe de l'infinitif: verbes en é(.)er
                if (modelR.contains("è")) {
                    val last = if (verbR.contains("é")) verbR.lastIndexOf("é") else -1
                    if (last > -1) {
                        verbR = verbR.substring(0, last) + "è" + verbR.substring(last + 1, verbR.length)
                    }
                }
            12 -> // jeter, jetter : verbes en -eler ou -eter, doublant 1 ou t devant e muet
                if (modelR.contains("tt")) {
                    verbR = if (verbR.endsWith("l")) verbR + "l" else verbR
                    verbR = if (verbR.endsWith("t")) verbR + "t" else verbR
                }
            13 -> // model, modèl : verbes en -eler ou -eter, changeant e en è devant syllabe muette
                if (modelR.contains("è")) {
                    verbR = if (infinitive.endsWith("eler")) infinitive.replace("eler", "èl") else verbR
                    verbR = if (infinitive.endsWith("eter")) infinitive.replace("eter", "èt") else verbR
                }
            15 -> // assiéger, assiège : verbes en -éger
                if (modelR.contains("è")) {
                    val last = if (verbR.contains("é")) verbR.lastIndexOf("é") else -1
                    if (last > -1) {
                        verbR = verbR.substring(0, last) + "è" + verbR.substring(last + 1, verbR.length)
                    }
                }
            17 -> // paie / paye : verbes en -ayer
                if (modelR.contains("i")) {
                    verbR = if (infinitive.endsWith("ayer")) infinitive.replace("ayer", "ai") else verbR
                }
            18 -> // broyer, broie : verbes en -oyer, -uyer
                if (modelR.contains("i")) {
                    verbR = if (infinitive.endsWith("oyer")) infinitive.replace("oyer", "oi") else verbR
                    verbR = if (infinitive.endsWith("uyer")) infinitive.replace("uyer", "ui") else verbR
                }
            19 -> // envoyer, envoie, enverra : all verbes, envoyer, renvoyer, s'envoyer, se renvoyer, avoyer
                if (modelR.contains("i")) {
                    verbR = if (infinitive.endsWith("yer")) infinitive.replace("yer", "i") else verbR
                } else if (modelR.contains("enverr")) {
                    // 2 special cases for enverr
                    verbR = if (infinitive.endsWith("envoyer")) infinitive.replace("envoyer", "enverr") else verbR
                    verbR = if (infinitive.endsWith("avoyer")) infinitive.replace("avoyer", "avoier") else verbR
                }
            20 -> // finir: all verbes.  known exceptions: s'amuïr, maudire
                if (modelR.contains("fin")) {
                    verbR = if (infinitive.endsWith("amuïr")) infinitive.replace("amuïr", "amu") else verbR
                    verbR = if (infinitive.endsWith("maudire")) infinitive.replace("maudire", "maud") else verbR
                }
            21 -> // haïr est le seul verbe
                if (modelR.contains("ha")) {
                    verbR = if (infinitive.endsWith("haïr")) infinitive.replace("haïr", "ha") else verbR
                }
            24 -> // tenir, tiens, tinsse, tînt : verbes -enir
                when {
                    modelR.contains("ten") -> verbR = if (infinitive.endsWith("enir")) infinitive.replace("enir", "en") else verbR
                    modelR.contains("tien") -> verbR = if (infinitive.endsWith("enir")) infinitive.replace("enir", "ien") else verbR
                    modelR.contains("tin") -> verbR = if (infinitive.endsWith("enir")) infinitive.replace("enir", "in") else verbR
                    modelR.contains("tîn") -> verbR = if (infinitive.endsWith("enir")) infinitive.replace("enir", "în") else verbR
                }
            25 -> // acquerir : verbes en -érir
                if (modelR.contains("acqu")) {
                    verbR = if (infinitive.endsWith("érir")) infinitive.replace("érir", "") else verbR
                }
            26 -> // sentir : verbes eb -tir
                if (modelR == "sen") {
                    verbR = if (infinitive.endsWith("tir")) infinitive.replace("tir", "") else verbR
                } else if (modelR == "senti") {
                    verbR = if (infinitive.endsWith("tir")) infinitive.replace("tir", "ti") else verbR
                }
            28 -> // souffrir, souffert : verbes en -vrir, frir
                if (modelR.contains("couve")) {
                    verbR = if (infinitive.endsWith("vrir")) infinitive.replace("vrir", "ve") else verbR
                    verbR = if (infinitive.endsWith("frir")) infinitive.replace("frir", "fe") else verbR
                }
            32 -> // bouillir, bous : all verbes, known: bouillir, debouillir, racabouillir
                if (modelR.contentEquals("bou")) {
                    verbR = if (infinitive.endsWith("bouillir")) infinitive.replace("bouillir", "bou") else verbR
                }
            33 -> // dormir, dors : all verbes, known: dormir, endormir, rendormir
                if (modelR.contentEquals("dor")) {
                    verbR = if (infinitive.endsWith("dormir")) infinitive.replace("dormir", "dor") else verbR
                }
            35 -> // mourir, meurs : all verbes, known: mourir, se mourir
                if (modelR.contains("meur")) {
                    verbR = if (infinitive.endsWith("mourir")) infinitive.replace("mourir", "meur") else verbR
                }
            36 -> // servir, sers : all verbes, known: servir, desservir, reservir
                if (modelR.contentEquals("ser")) {
                    verbR = if (infinitive.endsWith("servir")) infinitive.replace("servir", "ser") else verbR
                }
            40 -> // recevoir : verbes en -cevoir, all known: recevoir, apercevoir, concevoir, decevoir, percevoir
                when {
                    modelR.contentEquals("re") -> verbR = if (infinitive.endsWith("cevoir")) infinitive.replace("cevoir", "") else verbR
                    modelR.contentEquals("reçu") -> verbR = if (infinitive.endsWith("cevoir")) infinitive.replace("cevoir", "çu") else verbR
                    modelR.contentEquals("rece") -> verbR = if (infinitive.endsWith("cevoir")) infinitive.replace("cevoir", "ce") else verbR
                }
            41 -> // voir, vu : all verbes, known: voir, entrevoir, prevoir, revoir
                if (modelR.contentEquals("voi")) {
                    verbR = if (infinitive.endsWith("voir")) infinitive.replace("voir", "voi") else verbR
                }
                else if (modelR.contentEquals("voy")) {
                    verbR = if (infinitive.endsWith("voir")) infinitive.replace("voir", "voy") else verbR
                }
                else if (modelR.contentEquals("vi")) {
                    verbR = if (infinitive.endsWith("voir")) infinitive.replace("voir", "vi") else verbR
                }
                else if (modelR.contentEquals("vî")) {
                    verbR = if (infinitive.endsWith("voir")) infinitive.replace("voir", "vî") else verbR
                }
                else if (modelR.contentEquals("verr")) {
                    verbR = if (infinitive.contentEquals("prévoir")) { // this is an exception
                        "prévoir" // Futur Simple and Conditionnel Present
                    } else {
                        if (infinitive.endsWith("voir")) infinitive.replace("voir", "verr") else verbR
                    }
                }
                else if (modelR.contentEquals("vu")) {
                    verbR = if (infinitive.endsWith("voir")) infinitive.replace("voir", "vu") else verbR
                }
            42 -> // pourvoir, pourvu : all verbes, known: pourvoir, depourvoir
                if (modelR.contentEquals("pourv")) {
                    verbR = if (infinitive.endsWith("pourvoir")) infinitive.replace("pourvoir", "pourv") else verbR
                }
            44 -> // devoir, dois : all verbes, known: devoir, redevoir
                if (modelR.contentEquals("d")) {
                    verbR = if (infinitive.endsWith("devoir")) infinitive.replace("devoir", "d") else verbR
                }
            45 -> // pouvoir, pu : all verbes, known: pouvoir
                if (modelR.contentEquals("p")) {
                    verbR = if (infinitive.endsWith("pouvoir")) infinitive.replace("pouvoir", "p") else verbR
                }
            46 -> // mouvoir, mu : all verbes, known: mouvoir, émouvoir, promouvoir
                if (modelR.contentEquals("m")) {
                    verbR = if (infinitive.endsWith("mouvoir")) infinitive.replace("mouvoir", "m") else verbR
                }
            47 -> // pleuvoir, plu : all verbes, known: pleuvoir, repleuvoir
                if (modelR.contentEquals("pl")) {
                    verbR = if (infinitive.endsWith("pleuvoir")) infinitive.replace("pleuvoir", "pl") else verbR
                }
            49 -> // valoir, valu : all verbes, known: valoir, équivaloir, prévaloir, revaloir
                if (modelR.contentEquals("va")) {
                    verbR = if (infinitive.endsWith("valoir")) infinitive.replace("valoir", "va") else verbR
                }
            50 -> // vouloir, veux : all verbes, known: vouloir, revouloir
                when {
                    modelR.contentEquals("veu") -> verbR = if (infinitive.endsWith("vouloir")) infinitive.replace("vouloir", "veu") else verbR
                    modelR.contentEquals("voul") -> verbR = if (infinitive.endsWith("vouloir")) infinitive.replace("vouloir", "voul") else verbR
                    modelR.contentEquals("voud") -> verbR = if (infinitive.endsWith("vouloir")) infinitive.replace("vouloir", "voud") else verbR
                }
            51 -> // asseoir : all verbes, known: asseoir, rasseoir,
                if (modelR.contentEquals("ass")) {
                    verbR = if (infinitive.endsWith("asseoir")) infinitive.replace("asseoir", "ass") else verbR
                }
            59 -> // prendre, pris : all verbes,
                // known: prendre, apprendre, comprendre, déprendre, désapprendre, entreprendre, s'éprendre,
                //        se méprendre, rapprendre, reapprendre, reprendre, surprendre
                if (modelR.contentEquals("pr")) {
                    verbR = if (infinitive.endsWith("prendre")) infinitive.replace("prendre", "pr") else verbR
                }
            60 -> // battre, battu : all verbes,
                // known: battre, abattre, combattre, contrebattre, debattre, ebattre, embattre, rabattre, rebattre
                if (modelR.contentEquals("bat")) {
                    verbR = if (infinitive.endsWith("battre")) infinitive.replace("battre", "bat") else verbR
                }
            61 -> // mettre, mis : all verbes,
                // known: mettre, admettre, commettre, compromettre, demettre, emettre, (.)mettre
                when {
                    modelR.contentEquals("met") -> verbR = if (infinitive.endsWith("mettre")) infinitive.replace("mettre", "met") else verbR
                    modelR.contentEquals("mi") -> verbR = if (infinitive.endsWith("mettre")) infinitive.replace("mettre", "mi") else verbR
                    modelR.contentEquals("mî") -> verbR = if (infinitive.endsWith("mettre")) infinitive.replace("mettre", "mî") else verbR
                }
            62 -> // peindre, peignez : all verbes,
                // known: peindre, astreindre, ceindre,  (.)eindre
                if (modelR.contentEquals("pei")) {
                    verbR = if (infinitive.endsWith("eindre")) infinitive.replace("eindre", "ei") else verbR
                }
            63 -> // joindre, joins : all verbes,
                // known: joindre, adjoindre, conjoindre, disjoindre, enjoindre, rejoindre, oindre, poindre
                if (modelR.contentEquals("joi")) {
                    verbR = if (infinitive.endsWith("oindre")) infinitive.replace("oindre", "oi") else verbR
                }
            64 -> // craindre, craint : all verbes,
                // known: craindre, contraindre, plaindre
                if (modelR.contentEquals("crai")) {
                    verbR = if (infinitive.endsWith("aindre")) infinitive.replace("aindre", "ai") else verbR
                }
            65 -> // vaincre, vaincu : all verbes,
                // known: vaincre, convaincre
                if (modelR.contentEquals("vain")) {
                    verbR = if (infinitive.endsWith("vaincre")) infinitive.replace("vaincre", "vain") else verbR
                }
            66 -> // traire, trait : all verbes,
                // known: traire, abstraire, distraire, extraire, retraire, raire, soustraire, braire
                if (modelR.contentEquals("tra")) {
                    verbR = if (infinitive.endsWith("raire")) infinitive.replace("raire", "ra") else verbR
                }
            67 -> // faire, fait : all verbes,
                // known: faire, contrefaire, defaire, forfaire, malfaire, mefaire, parfaire, redefaire,
                //        refaire, satisfaire, surfaire
                if (modelR.contentEquals("f")) {
                    verbR = if (infinitive.endsWith("faire")) infinitive.replace("faire", "f") else verbR
                }
            68 -> // plaire, plait : all verbes,
                // known: plaire, complaire, déplaire, taire
                if (modelR.contentEquals("pl")) {
                    verbR = if (infinitive.endsWith("aire")) infinitive.replace("aire", "") else verbR
                }
            69 -> // connaître, connu : all verbes,
                // known: connaître, méconnaître, reconnaître, paraître, apparaître, comparaître,
                //        disparaître, réapparaître, recomparaître, reparaître, transparaître
                if (modelR.contentEquals("conn")) {
                    verbR = if (infinitive.endsWith("aître")) infinitive.replace("aître", "") else verbR
                }
            70 -> // naître, né : all verbes, known: naître, renaître
                if (modelR.contentEquals("na")) {
                    verbR = if (infinitive.endsWith("naître")) infinitive.replace("naître", "na") else verbR
                } else if (modelR.contentEquals("né")) {
                    verbR = if (infinitive.endsWith("naître")) infinitive.replace("naître", "né") else verbR
                }
            73 -> // croître, crû : all verbes, known: croître, accroître, décroître, recroître
                if (modelR.contentEquals("cr")) {
                    verbR = if (infinitive.endsWith("croître")) infinitive.replace("croître", "cr") else verbR
                }
            74 -> // croire, cru : all verbes, known: croire, accroire
                if (modelR.contentEquals("cr")) {
                    verbR = if (infinitive.endsWith("croire")) infinitive.replace("croire", "cr") else verbR
                }
            75 -> // boire, bu : all verbes, known: boire, emboire
                if (modelR.contentEquals("b")) {
                    verbR = if (infinitive.endsWith("boire")) infinitive.replace("boire", "b") else verbR
                }
            76 -> // clore, clos : all verbes, known: clore, déclore, éclore, enclore, forclore
                if (modelR.contentEquals("cl")) {
                    verbR = if (infinitive.endsWith("clore")) infinitive.replace("clore", "cl") else verbR
                }
            77 -> // conclure, conclu : all verbes, known: conclure, exclure, inclure, occlure, reclure
                if (modelR.contentEquals("con")) {
                    verbR = if (infinitive.endsWith("clure")) infinitive.replace("clure", "") else verbR
                }
            78 -> // absoudre, absous : all verbes, known: absoudre, dissoudre, résoudre
                if (modelR.contentEquals("abso")) {
                    verbR = if (infinitive.endsWith("soudre")) infinitive.replace("soudre", "so") else verbR
                }
            79 -> // coudre, cousu : all verbes, known: coudre, découdre, recoudre
                if (modelR.contentEquals("cou")) {
                    verbR = if (infinitive.endsWith("coudre")) infinitive.replace("coudre", "cou") else verbR
                }
            80 -> // moudre, moulu : all verbes, known: moudre, émoudre, remoudre
                if (modelR.contentEquals("mou")) {
                    verbR = if (infinitive.endsWith("moudre")) infinitive.replace("moudre", "mou") else verbR
                }
            81 -> // suivre, suivi : all verbes, known: suivre, ensuivre, poursuivre
                if (modelR.contentEquals("sui")) {
                    verbR = if (infinitive.endsWith("suivre")) infinitive.replace("suivre", "sui") else verbR
                }
            82 -> // vivre, vécu : all verbes, known: vivre, revivre, survivre
                when {
                    modelR.contentEquals("viv") -> verbR = if (infinitive.endsWith("vivre")) infinitive.replace("vivre", "viv") else verbR
                    modelR.contentEquals("vi") -> verbR = if (infinitive.endsWith("vivre")) infinitive.replace("vivre", "vi") else verbR
                    modelR.contentEquals("véc") -> verbR = if (infinitive.endsWith("vivre")) infinitive.replace("vivre", "véc") else verbR
                }
            83 -> // lire, lu : all verbes, known: lire, élire, réélire, relire
                if (modelR.contentEquals("l")) {
                    verbR = if (infinitive.endsWith("lire")) infinitive.replace("lire", "l") else verbR
                }
            84 -> // dire, dit : all verbes, known: dire, contredire, dédire, interdire, médire, prédire, redire
                if (modelR.contentEquals("d")) {
                    verbR = if (infinitive.endsWith("dire")) infinitive.replace("dire", "d") else verbR
                }
            85 -> // rire, ri : all verbes, known: rire, sourire
                if (modelR.contentEquals("rir")) {
                    verbR = if (infinitive.endsWith("rire")) infinitive.replace("rire", "rir") else verbR
                } else if (modelR.contentEquals("r")) {
                    verbR = if (infinitive.endsWith("rire")) infinitive.replace("rire", "r") else verbR
                }
            86 -> // écrire, écrit : all verbes,
                // known: écrire, circonscrire, décrire, inscrire, prescrire, proscrire, récrire,
                //        réinscrire, retranscrire, souscrire, transcrire
                if (modelR.contentEquals("écri")) {
                    verbR = if (infinitive.endsWith("crire")) infinitive.replace("crire", "cri") else verbR
                }
            87 -> // confire, confit : all verbes,
                // known: confire, déconfire, circoncire, frire, suffire
                if (modelR.contentEquals("conf")) {
                    verbR = if (infinitive.endsWith("ire")) infinitive.replace("ire", "") else verbR
                }
            88 -> // cuire, cuit : all verbes,
                // known: cuire, recuire, conduire, deduire, econduire, enduire, introduire, produire, (.)uire
                if (modelR.contentEquals("cui")) {
                    verbR = if (infinitive.endsWith("uire")) infinitive.replace("uire", "ui") else verbR
                }
        }

        //remove se or s'
        if (isPronominal) {
            verbR = if (verbR.startsWith("s'")) verbR.replaceFirst("s'".toRegex(), "") else verbR
            verbR = if (verbR.startsWith("se ")) verbR.replaceFirst("se ".toRegex(), "") else verbR
        }
        return verbR
    }*/

    /**
     * Replaces the radicals with the ones from the verb.
     * @param c Conjugation
     * @param modelR  List of model radicals
     * @param verbR  List of verb radicals
     *//*
    private fun replaceRadicals(c:Conjugation, modelR:List<String>, verbR:List<String>) {

        c.infinitivPrasens = verbName
        c.partizipPrasens = replaceRadical(c.partizipPrasens, modelR, verbR)
        c.gerondifPresent = replaceRadical(c.gerondifPresent, modelR, verbR)

        c.imperativDu = replaceRadical(c.imperativDu, modelR, verbR)
        c.imperativIhr = replaceRadical(c.imperativIhr, modelR, verbR)
        c.imperativSie = replaceRadical(c.imperativSie, modelR, verbR)

        c.indikativPrasensIch = replaceRadical(c.indikativPrasensIch, modelR, verbR)
        c.indikativPrasensDu = replaceRadical(c.indikativPrasensDu, modelR, verbR)
        c.indikativPrasensEr = replaceRadical(c.indikativPrasensEr, modelR, verbR)
        c.indikativPrasensWir = replaceRadical(c.indikativPrasensWir, modelR, verbR)
        c.indikativPrasensIhr = replaceRadical(c.indikativPrasensIhr, modelR, verbR)
        c.indikativPrasensSie = replaceRadical(c.indikativPrasensSie, modelR, verbR)

        c.indikativPerfektIch = replaceRadical(c.indikativPerfektIch, modelR, verbR)
        c.indikativPerfektDu = replaceRadical(c.indikativPerfektDu, modelR, verbR)
        c.indikativPerfektEr = replaceRadical(c.indikativPerfektEr, modelR, verbR)
        c.indikativPerfektWir = replaceRadical(c.indikativPerfektWir, modelR, verbR)
        c.indikativPerfektIhr = replaceRadical(c.indikativPerfektIhr, modelR, verbR)
        c.indikativPerfektSie = replaceRadical(c.indikativPerfektSie, modelR, verbR)

        c.indikativFutur1Ich = replaceRadical(c.indikativFutur1Ich, modelR, verbR)
        c.indikativFutur1Du = replaceRadical(c.indikativFutur1Du, modelR, verbR)
        c.indikativFutur1Er = replaceRadical(c.indikativFutur1Er, modelR, verbR)
        c.indikativFutur1Wir = replaceRadical(c.indikativFutur1Wir, modelR, verbR)
        c.indikativFutur1Ihr = replaceRadical(c.indikativFutur1Ihr, modelR, verbR)
        c.indikativFutur1Sie = replaceRadical(c.indikativFutur1Sie, modelR, verbR)

        c.konjunktiv1Futur1Ich = replaceRadical(c.konjunktiv1Futur1Ich, modelR, verbR)
        c.konjunktiv1Futur1Du = replaceRadical(c.konjunktiv1Futur1Du, modelR, verbR)
        c.konjunktiv1Futur1Er = replaceRadical(c.konjunktiv1Futur1Er, modelR, verbR)
        c.konjunktiv1Futur1Wir = replaceRadical(c.konjunktiv1Futur1Wir, modelR, verbR)
        c.konjunktiv1Futur1Ihr = replaceRadical(c.konjunktiv1Futur1Ihr, modelR, verbR)
        c.konjunktiv1Futur1Sie = replaceRadical(c.konjunktiv1Futur1Sie, modelR, verbR)

        c.konjunktiv1PrasensIch = replaceRadical(c.konjunktiv1PrasensIch, modelR, verbR)
        c.konjunktiv1PrasensDu = replaceRadical(c.konjunktiv1PrasensDu, modelR, verbR)
        c.konjunktiv1PrasensEr = replaceRadical(c.konjunktiv1PrasensEr, modelR, verbR)
        c.konjunktiv1PrasensWir = replaceRadical(c.konjunktiv1PrasensWir, modelR, verbR)
        c.konjunktiv1PrasensIhr = replaceRadical(c.konjunktiv1PrasensIhr, modelR, verbR)
        c.konjunktiv1PrasensSie = replaceRadical(c.konjunktiv1PrasensSie, modelR, verbR)

        c.konjunktiv2PrateritumIch = replaceRadical(c.konjunktiv2PrateritumIch, modelR, verbR)
        c.konjunktiv2PrateritumDu = replaceRadical(c.konjunktiv2PrateritumDu, modelR, verbR)
        c.konjunktiv2PrateritumEr = replaceRadical(c.konjunktiv2PrateritumEr, modelR, verbR)
        c.konjunktiv2PrateritumWir = replaceRadical(c.konjunktiv2PrateritumWir, modelR, verbR)
        c.konjunktiv2PrateritumIhr = replaceRadical(c.konjunktiv2PrateritumIhr, modelR, verbR)
        c.konjunktiv2PrateritumSie = replaceRadical(c.konjunktiv2PrateritumSie, modelR, verbR)

        c.konjunktiv2Futur1Ich = replaceRadical(c.konjunktiv2Futur1Ich, modelR, verbR)
        c.konjunktiv2Futur1Du = replaceRadical(c.konjunktiv2Futur1Du, modelR, verbR)
        c.konjunktiv2Futur1Er = replaceRadical(c.konjunktiv2Futur1Er, modelR, verbR)
        c.konjunktiv2Futur1Wir = replaceRadical(c.konjunktiv2Futur1Wir, modelR, verbR)
        c.konjunktiv2Futur1Ihr = replaceRadical(c.konjunktiv2Futur1Ihr, modelR, verbR)
        c.konjunktiv2Futur1Sie = replaceRadical(c.konjunktiv2Futur1Sie, modelR, verbR)


        // Calculate and replace participe passe only, instead of radicals for composed tenses
        var pp = c.partizipPerfekt.split(" ")
        val old = if (!pp[0].contentEquals("-")) pp[0] else ""

        c.partizipPerfekt = replaceRadical(c.partizipPerfekt, modelR, verbR)

        pp = c.partizipPerfekt.split(" ")
        val new = if (!pp[0].contentEquals("-")) pp[0] else ""

        if (old.isNotEmpty() && new.isNotEmpty()) {
            replaceParticipePasse(c, old, new)
        }
    }*/

    /**
     * Replaces the radical in the conjugation form.
     * @param text  verb conjugation
     * @param modelR List of model radicals
     * @param verbR List of verb radicals
     * @return radical
     */
    private fun replaceRadical(text:String, modelR:List<String>, verbR:List<String>) : String {
        var newText = text
        var radicalM:String
        var radicalV:String
        for (i in modelR.indices)
        {
            radicalM = modelR[i]
            radicalV = verbR[i]

            if (!radicalM.isEmpty() && !radicalV.isEmpty() && text.contains(radicalM)) {
                if (newText.contains(" / ")) {
                    // if there is 2 conjugations. Like model 51 asseoir. je rassieds / rassois
                    newText = newText.replace(radicalM, radicalV)
                } else {
                    newText = newText.replaceFirst(radicalM, radicalV)
                }

                // if it's just one form, if it's a double form (like Ich pay / paye) continue
                if (!text.contains("/")) {
                    break
                }
            }
        }
        return newText
    }

    /**
     * Ads the pronoms
     * @param c Conjugation
     */
    private fun addPronouns(c:Conjugation) {
        // Add pronoms
        // TODO: Show pronoms in different color
        addPronounsIndikativPrasens(c)
        addPronounsIndikativPrateritum(c)
        addPronounsIndikativPerfekt(c)
        addPronounsIndikativPlusquamperfekt(c)
        addPronounsIndikativFutur1(c)
        addPronounsIndikativFutur2(c)

        addPronounsKonjunktiv1Prasens(c)
        addPronounsKonjunktiv1Perfekt(c)
        addPronounsKonjunktiv1Futur1(c)
        addPronounsKonjunktiv1Futur2(c)

        addPronounsKonjunktiv2Prateritum(c)
        addPronounsKonjunktiv2Plusquamperfekt(c)
        addPronounsKonjunktiv2Futur1(c)
        addPronounsKonjunktiv2Futur2(c)
    }

    private fun addPronounsIndikativPrasens(c: Conjugation) {
        if (!c.indikativPrasensIch.contentEquals("-")) {
            c.indikativPrasensIch = ICH + c.indikativPrasensIch
        }
        if (!c.indikativPrasensDu.contentEquals("-")) {
            c.indikativPrasensDu = DU + c.indikativPrasensDu
        }
        if (!c.indikativPrasensEr.contentEquals("-")) {
            c.indikativPrasensEr = ER + c.indikativPrasensEr
        }
        if (!c.indikativPrasensWir.contentEquals("-")) {
            c.indikativPrasensWir = WIR + c.indikativPrasensWir
        }
        if (!c.indikativPrasensIhr.contentEquals("-")) {
            c.indikativPrasensIhr = IHR + c.indikativPrasensIhr
        }
        if (!c.indikativPrasensSie.contentEquals("-")) {
            c.indikativPrasensSie = SIE + c.indikativPrasensSie
        }
    }

    private fun addPronounsIndikativPrateritum(c: Conjugation) {
        if (!c.indikativPrateritumIch.contentEquals("-")) {
            c.indikativPrateritumIch = ICH + c.indikativPrateritumIch
        }
        if (!c.indikativPrateritumDu.contentEquals("-")) {
            c.indikativPrateritumDu = DU + c.indikativPrateritumDu
        }
        if (!c.indikativPrateritumEr.contentEquals("-")) {
            c.indikativPrateritumEr = ER + c.indikativPrateritumEr
        }
        if (!c.indikativPrateritumWir.contentEquals("-")) {
            c.indikativPrateritumWir = WIR + c.indikativPrateritumWir
        }
        if (!c.indikativPrateritumIhr.contentEquals("-")) {
            c.indikativPrateritumIhr = IHR + c.indikativPrateritumIhr
        }
        if (!c.indikativPrateritumSie.contentEquals("-")) {
            c.indikativPrateritumSie = SIE + c.indikativPrateritumSie
        }
    }

    private fun addPronounsIndikativPerfekt(c: Conjugation) {
        if (!c.indikativPerfektIch.contentEquals("-")) {
            c.indikativPerfektIch = ICH + c.indikativPerfektIch
        }
        if (!c.indikativPerfektDu.contentEquals("-")) {
            c.indikativPerfektDu = DU + c.indikativPerfektDu
        }
        if (!c.indikativPerfektEr.contentEquals("-")) {
            c.indikativPerfektEr = ER + c.indikativPerfektEr
        }
        if (!c.indikativPerfektWir.contentEquals("-")) {
            c.indikativPerfektWir = WIR + c.indikativPerfektWir
        }
        if (!c.indikativPerfektIhr.contentEquals("-")) {
            c.indikativPerfektIhr = IHR + c.indikativPerfektIhr
        }
        if (!c.indikativPerfektSie.contentEquals("-")) {
            c.indikativPerfektSie = SIE + c.indikativPerfektSie
        }
    }

    private fun addPronounsIndikativPlusquamperfekt(c: Conjugation) {
        if (!c.indikativPlusquamperfektIch.contentEquals("-")) {
            c.indikativPlusquamperfektIch = ICH + c.indikativPlusquamperfektIch
        }
        if (!c.indikativPlusquamperfektDu.contentEquals("-")) {
            c.indikativPlusquamperfektDu = DU + c.indikativPlusquamperfektDu
        }
        if (!c.indikativPlusquamperfektEr.contentEquals("-")) {
            c.indikativPlusquamperfektEr = ER + c.indikativPlusquamperfektEr
        }
        if (!c.indikativPlusquamperfektWir.contentEquals("-")) {
            c.indikativPlusquamperfektWir = WIR + c.indikativPlusquamperfektWir
        }
        if (!c.indikativPlusquamperfektIhr.contentEquals("-")) {
            c.indikativPlusquamperfektIhr = IHR + c.indikativPlusquamperfektIhr
        }
        if (!c.indikativPlusquamperfektSie.contentEquals("-")) {
            c.indikativPlusquamperfektSie = SIE + c.indikativPlusquamperfektSie
        }
    }

    private fun addPronounsIndikativFutur1(c: Conjugation) {
        if (!c.indikativFutur1Ich.contentEquals("-")) {
            c.indikativFutur1Ich = ICH + c.indikativFutur1Ich
        }
        if (!c.indikativFutur1Du.contentEquals("-")) {
            c.indikativFutur1Du = DU + c.indikativFutur1Du
        }
        if (!c.indikativFutur1Er.contentEquals("-")) {
            c.indikativFutur1Er = ER + c.indikativFutur1Er
        }
        if (!c.indikativFutur1Wir.contentEquals("-")) {
            c.indikativFutur1Wir = WIR + c.indikativFutur1Wir
        }
        if (!c.indikativFutur1Ihr.contentEquals("-")) {
            c.indikativFutur1Ihr = IHR + c.indikativFutur1Ihr
        }
        if (!c.indikativFutur1Sie.contentEquals("-")) {
            c.indikativFutur1Sie = SIE + c.indikativFutur1Sie
        }
    }

    private fun addPronounsIndikativFutur2(c: Conjugation) {
        if (!c.indikativFutur2Ich.contentEquals("-")) {
            c.indikativFutur2Ich = ICH + c.indikativFutur2Ich
        }
        if (!c.indikativFutur2Du.contentEquals("-")) {
            c.indikativFutur2Du = DU + c.indikativFutur2Du
        }
        if (!c.indikativFutur2Er.contentEquals("-")) {
            c.indikativFutur2Er = ER + c.indikativFutur2Er
        }
        if (!c.indikativFutur2Wir.contentEquals("-")) {
            c.indikativFutur2Wir = WIR + c.indikativFutur2Wir
        }
        if (!c.indikativFutur2Ihr.contentEquals("-")) {
            c.indikativFutur2Ihr = IHR + c.indikativFutur2Ihr
        }
        if (!c.indikativFutur2Sie.contentEquals("-")) {
            c.indikativFutur2Sie = SIE + c.indikativFutur2Sie
        }
    }

    private fun addPronounsKonjunktiv1Prasens(c: Conjugation) {
        if (!c.konjunktiv1PrasensIch.contentEquals("-")) {
            c.konjunktiv1PrasensIch = ICH + c.konjunktiv1PrasensIch
        }
        if (!c.konjunktiv1PrasensDu.contentEquals("-")) {
            c.konjunktiv1PrasensDu = DU + c.konjunktiv1PrasensDu
        }
        if (!c.konjunktiv1PrasensEr.contentEquals("-")) {
            c.konjunktiv1PrasensEr = ER + c.konjunktiv1PrasensEr
        }
        if (!c.konjunktiv1PrasensWir.contentEquals("-")) {
            c.konjunktiv1PrasensWir = WIR + c.konjunktiv1PrasensWir
        }
        if (!c.konjunktiv1PrasensIhr.contentEquals("-")) {
            c.konjunktiv1PrasensIhr = IHR + c.konjunktiv1PrasensIhr
        }
        if (!c.konjunktiv1PrasensSie.contentEquals("-")) {
            c.konjunktiv1PrasensSie = SIE + c.konjunktiv1PrasensSie
        }
    }

    private fun addPronounsKonjunktiv1Perfekt(c: Conjugation) {
        if (!c.konjunktiv1PerfektIch.contentEquals("-")) {
            c.konjunktiv1PerfektIch = ICH + c.konjunktiv1PerfektIch
        }
        if (!c.konjunktiv1PerfektDu.contentEquals("-")) {
            c.konjunktiv1PerfektDu = DU + c.konjunktiv1PerfektDu
        }
        if (!c.konjunktiv1PerfektEr.contentEquals("-")) {
            c.konjunktiv1PerfektEr = ER + c.konjunktiv1PerfektEr
        }
        if (!c.konjunktiv1PerfektWir.contentEquals("-")) {
            c.konjunktiv1PerfektWir = WIR + c.konjunktiv1PerfektWir
        }
        if (!c.konjunktiv1PerfektIhr.contentEquals("-")) {
            c.konjunktiv1PerfektIhr = IHR + c.konjunktiv1PerfektIhr
        }
        if (!c.konjunktiv1PerfektSie.contentEquals("-")) {
            c.konjunktiv1PerfektSie = SIE + c.konjunktiv1PerfektSie
        }
    }

    private fun addPronounsKonjunktiv1Futur1(c: Conjugation) {
        if (!c.konjunktiv1Futur1Ich.contentEquals("-")) {
            c.konjunktiv1Futur1Ich = ICH + c.konjunktiv1Futur1Ich
        }
        if (!c.konjunktiv1Futur1Du.contentEquals("-")) {
            c.konjunktiv1Futur1Du = DU + c.konjunktiv1Futur1Du
        }
        if (!c.konjunktiv1Futur1Er.contentEquals("-")) {
            c.konjunktiv1Futur1Er = ER + c.konjunktiv1Futur1Er
        }
        if (!c.konjunktiv1Futur1Wir.contentEquals("-")) {
            c.konjunktiv1Futur1Wir = WIR + c.konjunktiv1Futur1Wir
        }
        if (!c.konjunktiv1Futur1Ihr.contentEquals("-")) {
            c.konjunktiv1Futur1Ihr = IHR + c.konjunktiv1Futur1Ihr
        }
        if (!c.konjunktiv1Futur1Sie.contentEquals("-")) {
            c.konjunktiv1Futur1Sie = SIE + c.konjunktiv1Futur1Sie
        }
    }

    private fun addPronounsKonjunktiv1Futur2(c: Conjugation) {
        if (!c.konjunktiv1Futur2Ich.contentEquals("-")) {
            c.konjunktiv1Futur2Ich = ICH + c.konjunktiv1Futur2Ich
        }
        if (!c.konjunktiv1Futur2Du.contentEquals("-")) {
            c.konjunktiv1Futur2Du = DU + c.konjunktiv1Futur2Du
        }
        if (!c.konjunktiv1Futur2Er.contentEquals("-")) {
            c.konjunktiv1Futur2Er = ER + c.konjunktiv1Futur2Er
        }
        if (!c.konjunktiv1Futur2Wir.contentEquals("-")) {
            c.konjunktiv1Futur2Wir = WIR + c.konjunktiv1Futur2Wir
        }
        if (!c.konjunktiv1Futur2Ihr.contentEquals("-")) {
            c.konjunktiv1Futur2Ihr = IHR + c.konjunktiv1Futur2Ihr
        }
        if (!c.konjunktiv1Futur2Sie.contentEquals("-")) {
            c.konjunktiv1Futur2Sie = SIE + c.konjunktiv1Futur2Sie
        }
    }

    private fun addPronounsKonjunktiv2Prateritum(c: Conjugation) {
        if (!c.konjunktiv2PrateritumIch.contentEquals("-")) {
            c.konjunktiv2PrateritumIch = ICH + c.konjunktiv2PrateritumIch
        }
        if (!c.konjunktiv2PrateritumDu.contentEquals("-")) {
            c.konjunktiv2PrateritumDu = DU + c.konjunktiv2PrateritumDu
        }
        if (!c.konjunktiv2PrateritumEr.contentEquals("-")) {
            c.konjunktiv2PrateritumEr = ER + c.konjunktiv2PrateritumEr
        }
        if (!c.konjunktiv2PrateritumWir.contentEquals("-")) {
            c.konjunktiv2PrateritumWir = WIR + c.konjunktiv2PrateritumWir
        }
        if (!c.konjunktiv2PrateritumIhr.contentEquals("-")) {
            c.konjunktiv2PrateritumIhr = IHR + c.konjunktiv2PrateritumIhr
        }
        if (!c.konjunktiv2PrateritumSie.contentEquals("-")) {
            c.konjunktiv2PrateritumSie = SIE + c.konjunktiv2PrateritumSie
        }
    }

    private fun addPronounsKonjunktiv2Plusquamperfekt(c: Conjugation) {
        if (!c.konjunktiv2PlusquamperfektIch.contentEquals("-")) {
            c.konjunktiv2PlusquamperfektIch = ICH + c.konjunktiv2PlusquamperfektIch
        }
        if (!c.konjunktiv2PlusquamperfektDu.contentEquals("-")) {
            c.konjunktiv2PlusquamperfektDu = DU + c.konjunktiv2PlusquamperfektDu
        }
        if (!c.konjunktiv2PlusquamperfektEr.contentEquals("-")) {
            c.konjunktiv2PlusquamperfektEr = ER + c.konjunktiv2PlusquamperfektEr
        }
        if (!c.konjunktiv2PlusquamperfektWir.contentEquals("-")) {
            c.konjunktiv2PlusquamperfektWir = WIR + c.konjunktiv2PlusquamperfektWir
        }
        if (!c.konjunktiv2PlusquamperfektIhr.contentEquals("-")) {
            c.konjunktiv2PlusquamperfektIhr = IHR + c.konjunktiv2PlusquamperfektIhr
        }
        if (!c.konjunktiv2PlusquamperfektSie.contentEquals("-")) {
            c.konjunktiv2PlusquamperfektSie = SIE + c.konjunktiv2PlusquamperfektSie
        }
    }

    private fun addPronounsKonjunktiv2Futur1(c: Conjugation) {
        if (!c.konjunktiv2Futur1Ich.contentEquals("-")) {
            c.konjunktiv2Futur1Ich = ICH + c.konjunktiv2Futur1Ich
        }
        if (!c.konjunktiv2Futur1Du.contentEquals("-")) {
            c.konjunktiv2Futur1Du = DU + c.konjunktiv2Futur1Du
        }
        if (!c.konjunktiv2Futur1Er.contentEquals("-")) {
            c.konjunktiv2Futur1Er = ER + c.konjunktiv2Futur1Er
        }
        if (!c.konjunktiv2Futur1Wir.contentEquals("-")) {
            c.konjunktiv2Futur1Wir = WIR + c.konjunktiv2Futur1Wir
        }
        if (!c.konjunktiv2Futur1Ihr.contentEquals("-")) {
            c.konjunktiv2Futur1Ihr = IHR + c.konjunktiv2Futur1Ihr
        }
        if (!c.konjunktiv2Futur1Sie.contentEquals("-")) {
            c.konjunktiv2Futur1Sie = SIE + c.konjunktiv2Futur1Sie
        }
    }

    private fun addPronounsKonjunktiv2Futur2(c: Conjugation) {
        if (!c.konjunktiv2Futur2Ich.contentEquals("-")) {
            c.konjunktiv2Futur2Ich = ICH + c.konjunktiv2Futur2Ich
        }
        if (!c.konjunktiv2Futur2Du.contentEquals("-")) {
            c.konjunktiv2Futur2Du = DU + c.konjunktiv2Futur2Du
        }
        if (!c.konjunktiv2Futur2Er.contentEquals("-")) {
            c.konjunktiv2Futur2Er = ER + c.konjunktiv2Futur2Er
        }
        if (!c.konjunktiv2Futur2Wir.contentEquals("-")) {
            c.konjunktiv2Futur2Wir = WIR + c.konjunktiv2Futur2Wir
        }
        if (!c.konjunktiv2Futur2Ihr.contentEquals("-")) {
            c.konjunktiv2Futur2Ihr = IHR + c.konjunktiv2Futur2Ihr
        }
        if (!c.konjunktiv2Futur2Sie.contentEquals("-")) {
            c.konjunktiv2Futur2Sie = SIE + c.konjunktiv2Futur2Sie
        }
    }


    /**
     * Fills the conjugation section.
     * @param c Conjugation ready to display
     */
    private fun fillConjugationDetails(c: Conjugation) {
        val fontSize = Integer.parseInt(ActivityUtils.getPreferenceFontSize(applicationContext))
        changeTextFontInConjugation(fontSize)
        
        (findViewById<View>(R.id.infinitiv_prasens) as TextView).text = c.infinitivPrasens
        (findViewById<View>(R.id.infinitiv_perfekt) as TextView).text = c.infinitivPerfekt
        (findViewById<View>(R.id.partizip_prasens) as TextView).text = c.partizipPrasens
        (findViewById<View>(R.id.partizip_perfekt) as TextView).text = c.partizipPerfekt

        (findViewById<View>(R.id.imperativ_prasens_du) as TextView).text = c.imperativDu
        (findViewById<View>(R.id.imperativ_prasens_ihr) as TextView).text = c.imperativIhr
        (findViewById<View>(R.id.imperativ_prasens_sie) as TextView).text = c.imperativSie

        (findViewById<View>(R.id.indikativ_prasens_ich) as TextView).text = c.indikativPrasensIch
        (findViewById<View>(R.id.indikativ_prasens_du) as TextView).text = c.indikativPrasensDu
        (findViewById<View>(R.id.indikativ_prasens_er) as TextView).text = c.indikativPrasensEr
        (findViewById<View>(R.id.indikativ_prasens_wir) as TextView).text = c.indikativPrasensWir
        (findViewById<View>(R.id.indikativ_prasens_ihr) as TextView).text = c.indikativPrasensIhr
        (findViewById<View>(R.id.indikativ_prasens_sie) as TextView).text = c.indikativPrasensSie
        (findViewById<View>(R.id.indikativ_prateritum_ich) as TextView).text = c.indikativPrateritumIch
        (findViewById<View>(R.id.indikativ_prateritum_du) as TextView).text = c.indikativPrateritumDu
        (findViewById<View>(R.id.indikativ_prateritum_er) as TextView).text = c.indikativPrateritumEr
        (findViewById<View>(R.id.indikativ_prateritum_wir) as TextView).text = c.indikativPrateritumWir
        (findViewById<View>(R.id.indikativ_prateritum_ihr) as TextView).text = c.indikativPrateritumIhr
        (findViewById<View>(R.id.indikativ_prateritum_sie) as TextView).text = c.indikativPrateritumSie
        (findViewById<View>(R.id.indikativ_perfekt_ich) as TextView).text = c.indikativPerfektIch
        (findViewById<View>(R.id.indikativ_perfekt_du) as TextView).text = c.indikativPerfektDu
        (findViewById<View>(R.id.indikativ_perfekt_er) as TextView).text = c.indikativPerfektEr
        (findViewById<View>(R.id.indikativ_perfekt_wir) as TextView).text = c.indikativPerfektWir
        (findViewById<View>(R.id.indikativ_perfekt_ihr) as TextView).text = c.indikativPerfektIhr
        (findViewById<View>(R.id.indikativ_perfekt_sie) as TextView).text = c.indikativPerfektSie
        (findViewById<View>(R.id.indikativ_plusquamperfekt_ich) as TextView).text = c.indikativPlusquamperfektIch
        (findViewById<View>(R.id.indikativ_plusquamperfekt_du) as TextView).text = c.indikativPlusquamperfektDu
        (findViewById<View>(R.id.indikativ_plusquamperfekt_er) as TextView).text = c.indikativPlusquamperfektEr
        (findViewById<View>(R.id.indikativ_plusquamperfekt_wir) as TextView).text = c.indikativPlusquamperfektWir
        (findViewById<View>(R.id.indikativ_plusquamperfekt_ihr) as TextView).text = c.indikativPlusquamperfektIhr
        (findViewById<View>(R.id.indikativ_plusquamperfekt_sie) as TextView).text = c.indikativPlusquamperfektSie
        (findViewById<View>(R.id.indikativ_futur1_ich) as TextView).text = c.indikativFutur1Ich
        (findViewById<View>(R.id.indikativ_futur1_du) as TextView).text = c.indikativFutur1Du
        (findViewById<View>(R.id.indikativ_futur1_er) as TextView).text = c.indikativFutur1Er
        (findViewById<View>(R.id.indikativ_futur1_wir) as TextView).text = c.indikativFutur1Wir
        (findViewById<View>(R.id.indikativ_futur1_ihr) as TextView).text = c.indikativFutur1Ihr
        (findViewById<View>(R.id.indikativ_futur1_sie) as TextView).text = c.indikativFutur1Sie
        (findViewById<View>(R.id.indikativ_futur2_ich) as TextView).text = c.indikativFutur2Ich
        (findViewById<View>(R.id.indikativ_futur2_du) as TextView).text = c.indikativFutur2Du
        (findViewById<View>(R.id.indikativ_futur2_er) as TextView).text = c.indikativFutur2Er
        (findViewById<View>(R.id.indikativ_futur2_wir) as TextView).text = c.indikativFutur2Wir
        (findViewById<View>(R.id.indikativ_futur2_ihr) as TextView).text = c.indikativFutur2Ihr
        (findViewById<View>(R.id.indikativ_futur2_sie) as TextView).text = c.indikativFutur2Sie
        
        (findViewById<View>(R.id.konjunktiv1_prasens_ich) as TextView).text = c.konjunktiv1PrasensIch
        (findViewById<View>(R.id.konjunktiv1_prasens_du) as TextView).text = c.konjunktiv1PrasensDu
        (findViewById<View>(R.id.konjunktiv1_prasens_er) as TextView).text = c.konjunktiv1PrasensEr
        (findViewById<View>(R.id.konjunktiv1_prasens_wir) as TextView).text = c.konjunktiv1PrasensWir
        (findViewById<View>(R.id.konjunktiv1_prasens_ihr) as TextView).text = c.konjunktiv1PrasensIhr
        (findViewById<View>(R.id.konjunktiv1_prasens_sie) as TextView).text = c.konjunktiv1PrasensSie
        (findViewById<View>(R.id.konjunktiv1_perfekt_ich) as TextView).text = c.konjunktiv1PerfektIch
        (findViewById<View>(R.id.konjunktiv1_perfekt_du) as TextView).text = c.konjunktiv1PerfektDu
        (findViewById<View>(R.id.konjunktiv1_perfekt_er) as TextView).text = c.konjunktiv1PerfektEr
        (findViewById<View>(R.id.konjunktiv1_perfekt_wir) as TextView).text = c.konjunktiv1PerfektWir
        (findViewById<View>(R.id.konjunktiv1_perfekt_ihr) as TextView).text = c.konjunktiv1PerfektIhr
        (findViewById<View>(R.id.konjunktiv1_perfekt_sie) as TextView).text = c.konjunktiv1PerfektSie
        (findViewById<View>(R.id.konjunktiv1_futur1_ich) as TextView).text = c.konjunktiv1Futur1Ich
        (findViewById<View>(R.id.konjunktiv1_futur1_du) as TextView).text = c.konjunktiv1Futur1Du
        (findViewById<View>(R.id.konjunktiv1_futur1_er) as TextView).text = c.konjunktiv1Futur1Er
        (findViewById<View>(R.id.konjunktiv1_futur1_wir) as TextView).text = c.konjunktiv1Futur1Wir
        (findViewById<View>(R.id.konjunktiv1_futur1_ihr) as TextView).text = c.konjunktiv1Futur1Ihr
        (findViewById<View>(R.id.konjunktiv1_futur1_sie) as TextView).text = c.konjunktiv1Futur1Sie
        (findViewById<View>(R.id.konjunktiv1_futur2_ich) as TextView).text = c.konjunktiv1Futur2Ich
        (findViewById<View>(R.id.konjunktiv1_futur2_du) as TextView).text = c.konjunktiv1Futur2Du
        (findViewById<View>(R.id.konjunktiv1_futur2_er) as TextView).text = c.konjunktiv1Futur2Er
        (findViewById<View>(R.id.konjunktiv1_futur2_wir) as TextView).text = c.konjunktiv1Futur2Wir
        (findViewById<View>(R.id.konjunktiv1_futur2_ihr) as TextView).text = c.konjunktiv1Futur2Ihr
        (findViewById<View>(R.id.konjunktiv1_futur2_sie) as TextView).text = c.konjunktiv1Futur2Sie

        (findViewById<View>(R.id.konjunktiv2_prateritum_ich) as TextView).text = c.konjunktiv2PrateritumIch
        (findViewById<View>(R.id.konjunktiv2_prateritum_du) as TextView).text = c.konjunktiv2PrateritumDu
        (findViewById<View>(R.id.konjunktiv2_prateritum_er) as TextView).text = c.konjunktiv2PrateritumEr
        (findViewById<View>(R.id.konjunktiv2_prateritum_wir) as TextView).text = c.konjunktiv2PrateritumWir
        (findViewById<View>(R.id.konjunktiv2_prateritum_ihr) as TextView).text = c.konjunktiv2PrateritumIhr
        (findViewById<View>(R.id.konjunktiv2_prateritum_sie) as TextView).text = c.konjunktiv2PrateritumSie
        (findViewById<View>(R.id.konjunktiv2_plusquamperfekt_ich) as TextView).text = c.konjunktiv2PlusquamperfektIch
        (findViewById<View>(R.id.konjunktiv2_plusquamperfekt_du) as TextView).text = c.konjunktiv2PlusquamperfektDu
        (findViewById<View>(R.id.konjunktiv2_plusquamperfekt_er) as TextView).text = c.konjunktiv2PlusquamperfektEr
        (findViewById<View>(R.id.konjunktiv2_plusquamperfekt_wir) as TextView).text = c.konjunktiv2PlusquamperfektWir
        (findViewById<View>(R.id.konjunktiv2_plusquamperfekt_ihr) as TextView).text = c.konjunktiv2PlusquamperfektIhr
        (findViewById<View>(R.id.konjunktiv2_plusquamperfekt_sie) as TextView).text = c.konjunktiv2PlusquamperfektSie
        (findViewById<View>(R.id.konjunktiv2_futur1_ich) as TextView).text = c.konjunktiv2Futur1Ich
        (findViewById<View>(R.id.konjunktiv2_futur1_du) as TextView).text = c.konjunktiv2Futur1Du
        (findViewById<View>(R.id.konjunktiv2_futur1_er) as TextView).text = c.konjunktiv2Futur1Er
        (findViewById<View>(R.id.konjunktiv2_futur1_wir) as TextView).text = c.konjunktiv2Futur1Wir
        (findViewById<View>(R.id.konjunktiv2_futur1_ihr) as TextView).text = c.konjunktiv2Futur1Ihr
        (findViewById<View>(R.id.konjunktiv2_futur1_sie) as TextView).text = c.konjunktiv2Futur1Sie
        (findViewById<View>(R.id.konjunktiv2_futur2_ich) as TextView).text = c.konjunktiv2Futur2Ich
        (findViewById<View>(R.id.konjunktiv2_futur2_du) as TextView).text = c.konjunktiv2Futur2Du
        (findViewById<View>(R.id.konjunktiv2_futur2_er) as TextView).text = c.konjunktiv2Futur2Er
        (findViewById<View>(R.id.konjunktiv2_futur2_wir) as TextView).text = c.konjunktiv2Futur2Wir
        (findViewById<View>(R.id.konjunktiv2_futur2_ihr) as TextView).text = c.konjunktiv2Futur2Ihr
        (findViewById<View>(R.id.konjunktiv2_futur2_sie) as TextView).text = c.konjunktiv2Futur2Sie
    }


    /**
     * Changes text font size.
     * @param fontSize int
     */
    private fun changeTextFontInConjugation(fontSize: Int) {
        val unit = TypedValue.COMPLEX_UNIT_SP
        (findViewById<View>(R.id.indikativ_prasens_ich) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indikativ_prasens_du) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indikativ_prasens_er) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indikativ_prasens_wir) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indikativ_prasens_ihr) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indikativ_prasens_sie) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indikativ_prateritum_ich) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indikativ_prateritum_du) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indikativ_prateritum_er) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indikativ_prateritum_wir) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indikativ_prateritum_ihr) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indikativ_prateritum_sie) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indikativ_perfekt_ich) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indikativ_perfekt_du) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indikativ_perfekt_er) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indikativ_perfekt_wir) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indikativ_perfekt_ihr) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indikativ_perfekt_sie) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indikativ_plusquamperfekt_ich) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indikativ_plusquamperfekt_du) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indikativ_plusquamperfekt_er) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indikativ_plusquamperfekt_wir) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indikativ_plusquamperfekt_ihr) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indikativ_plusquamperfekt_sie) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indikativ_futur1_ich) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indikativ_futur1_du) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indikativ_futur1_er) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indikativ_futur1_wir) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indikativ_futur1_ihr) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indikativ_futur1_sie) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indikativ_futur2_ich) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indikativ_futur2_du) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indikativ_futur2_er) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indikativ_futur2_wir) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indikativ_futur2_ihr) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indikativ_futur2_sie) as TextView).setTextSize(unit, fontSize.toFloat())

        (findViewById<View>(R.id.konjunktiv1_prasens_ich) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv1_prasens_du) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv1_prasens_er) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv1_prasens_wir) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv1_prasens_ihr) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv1_prasens_sie) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv1_perfekt_ich) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv1_perfekt_du) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv1_perfekt_er) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv1_perfekt_wir) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv1_perfekt_ihr) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv1_perfekt_sie) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv1_futur1_ich) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv1_futur1_du) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv1_futur1_er) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv1_futur1_wir) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv1_futur1_ihr) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv1_futur1_sie) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv1_futur2_ich) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv1_futur2_du) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv1_futur2_er) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv1_futur2_wir) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv1_futur2_ihr) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv1_futur2_sie) as TextView).setTextSize(unit, fontSize.toFloat())

        (findViewById<View>(R.id.konjunktiv2_prateritum_ich) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv2_prateritum_du) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv2_prateritum_er) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv2_prateritum_wir) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv2_prateritum_ihr) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv2_prateritum_sie) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv2_plusquamperfekt_ich) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv2_plusquamperfekt_du) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv2_plusquamperfekt_er) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv2_plusquamperfekt_wir) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv2_plusquamperfekt_ihr) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv2_plusquamperfekt_sie) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv2_futur1_ich) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv2_futur1_du) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv2_futur1_er) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv2_futur1_wir) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv2_futur1_ihr) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv2_futur1_sie) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv2_futur2_ich) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv2_futur2_du) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv2_futur2_er) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv2_futur2_wir) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv2_futur2_ihr) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.konjunktiv2_futur2_sie) as TextView).setTextSize(unit, fontSize.toFloat())

        (findViewById<View>(R.id.imperativ_prasens_du) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.imperativ_prasens_ihr) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.imperativ_prasens_sie) as TextView).setTextSize(unit, fontSize.toFloat())

        (findViewById<View>(R.id.infinitiv_prasens) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.infinitiv_perfekt) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.partizip_prasens) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.partizip_perfekt) as TextView).setTextSize(unit, fontSize.toFloat())
    }

    /**
     * Set the text color.
     * @param color color
     */
    private fun setVerbColor(color: Int) {
        infinitive?.setTextColor(color)
    }

    override fun onClick(view: View) {
        // Play the sounds
        when (view.id) {
            R.id.play_infinitive -> if (verb != null) {
                ActivityUtils.speak(applicationContext, tts, verb?.infinitive)
                Toast.makeText(applicationContext, verb!!.infinitive, Toast.LENGTH_SHORT).show()
            }

            R.id.play_definition -> ActivityUtils.speak(applicationContext, tts, verb?.definition)
            R.id.play_sample1 -> ActivityUtils.speak(applicationContext, tts, verb?.sample1)
            R.id.play_sample2 -> ActivityUtils.speak(applicationContext, tts, verb?.sample2)
            R.id.play_sample3 -> ActivityUtils.speak(applicationContext, tts, verb?.sample3)
            
            R.id.play_indikativ_prasens_ich ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indikativPrasensIch)
            R.id.play_indikativ_prasens_du ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indikativPrasensDu)
            R.id.play_indikativ_prasens_er ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indikativPrasensEr)
            R.id.play_indikativ_prasens_wir ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indikativPrasensWir)
            R.id.play_indikativ_prasens_ihr ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indikativPrasensIhr)
            R.id.play_indikativ_prasens_sie ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indikativPrasensSie)

            R.id.play_indikativ_prateritum_ich ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indikativPrateritumIch)
            R.id.play_indikativ_prateritum_du ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indikativPrateritumDu)
            R.id.play_indikativ_prateritum_er ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indikativPrateritumEr)
            R.id.play_indikativ_prateritum_wir ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indikativPrateritumWir)
            R.id.play_indikativ_prateritum_ihr ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indikativPrateritumIhr)
            R.id.play_indikativ_prateritum_sie ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indikativPrateritumSie)

            R.id.play_indikativ_perfekt_ich ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indikativPerfektIch)
            R.id.play_indikativ_perfekt_du ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indikativPerfektDu)
            R.id.play_indikativ_perfekt_er ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indikativPerfektEr)
            R.id.play_indikativ_perfekt_wir ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indikativPerfektWir)
            R.id.play_indikativ_perfekt_ihr ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indikativPerfektIhr)
            R.id.play_indikativ_perfekt_sie ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indikativPerfektSie)

            R.id.play_indikativ_plusquamperfekt_ich ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indikativPlusquamperfektIch)
            R.id.play_indikativ_plusquamperfekt_du ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indikativPlusquamperfektDu)
            R.id.play_indikativ_plusquamperfekt_er ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indikativPlusquamperfektEr)
            R.id.play_indikativ_plusquamperfekt_wir ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indikativPlusquamperfektWir)
            R.id.play_indikativ_plusquamperfekt_ihr ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indikativPlusquamperfektIhr)
            R.id.play_indikativ_plusquamperfekt_sie ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indikativPlusquamperfektSie)

            R.id.play_indikativ_futur1_ich ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indikativFutur1Ich)
            R.id.play_indikativ_futur1_du ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indikativFutur1Du)
            R.id.play_indikativ_futur1_er ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indikativFutur1Er)
            R.id.play_indikativ_futur1_wir ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indikativFutur1Wir)
            R.id.play_indikativ_futur1_ihr ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indikativFutur1Ihr)
            R.id.play_indikativ_futur1_sie ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indikativFutur1Sie)

            R.id.play_indikativ_futur2_ich ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indikativFutur2Ich)
            R.id.play_indikativ_futur2_du ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indikativFutur2Du)
            R.id.play_indikativ_futur2_er ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indikativFutur2Er)
            R.id.play_indikativ_futur2_wir ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indikativFutur2Wir)
            R.id.play_indikativ_futur2_ihr ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indikativFutur2Ihr)
            R.id.play_indikativ_futur2_sie ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indikativFutur2Sie)

            R.id.play_konjunktiv1_prasens_ich ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv1PrasensIch)
            R.id.play_konjunktiv1_prasens_du ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv1PrasensDu)
            R.id.play_konjunktiv1_prasens_er ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv1PrasensEr)
            R.id.play_konjunktiv1_prasens_wir ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv1PrasensWir)
            R.id.play_konjunktiv1_prasens_ihr ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv1PrasensIhr)
            R.id.play_konjunktiv1_prasens_sie ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv1PrasensSie)

            R.id.play_konjunktiv1_perfekt_ich ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv1PerfektIch)
            R.id.play_konjunktiv1_perfekt_du ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv1PerfektDu)
            R.id.play_konjunktiv1_perfekt_er ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv1PerfektEr)
            R.id.play_konjunktiv1_perfekt_wir ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv1PerfektWir)
            R.id.play_konjunktiv1_perfekt_ihr ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv1PerfektIhr)
            R.id.play_konjunktiv1_perfekt_sie ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv1PerfektSie)

            R.id.play_konjunktiv1_futur1_ich ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv1Futur1Ich)
            R.id.play_konjunktiv1_futur1_du ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv1Futur1Du)
            R.id.play_konjunktiv1_futur1_er ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv1Futur1Er)
            R.id.play_konjunktiv1_futur1_wir ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv1Futur1Wir)
            R.id.play_konjunktiv1_futur1_ihr ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv1Futur1Ihr)
            R.id.play_konjunktiv1_futur1_sie ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv1Futur1Sie)

            R.id.play_konjunktiv1_futur2_ich ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv1Futur2Ich)
            R.id.play_konjunktiv1_futur2_du ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv1Futur2Du)
            R.id.play_konjunktiv1_futur2_er ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv1Futur2Er)
            R.id.play_konjunktiv1_futur2_wir ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv1Futur2Wir)
            R.id.play_konjunktiv1_futur2_ihr ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv1Futur2Ihr)
            R.id.play_konjunktiv1_futur2_sie ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv1Futur2Sie)

            R.id.play_konjunktiv2_prateritum_ich ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv2PrateritumIch)
            R.id.play_konjunktiv2_prateritum_du ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv2PrateritumDu)
            R.id.play_konjunktiv2_prateritum_er ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv2PrateritumEr)
            R.id.play_konjunktiv2_prateritum_wir ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv2PrateritumWir)
            R.id.play_konjunktiv2_prateritum_ihr ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv2PrateritumIhr)
            R.id.play_konjunktiv2_prateritum_sie ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv2PrateritumSie)

            R.id.play_konjunktiv2_plusquamperfekt_ich ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv2PlusquamperfektIch)
            R.id.play_konjunktiv2_plusquamperfekt_du ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv2PlusquamperfektDu)
            R.id.play_konjunktiv2_plusquamperfekt_er ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv2PlusquamperfektEr)
            R.id.play_konjunktiv2_plusquamperfekt_wir ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv2PlusquamperfektWir)
            R.id.play_konjunktiv2_plusquamperfekt_ihr ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv2PlusquamperfektIhr)
            R.id.play_konjunktiv2_plusquamperfekt_sie ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv2PlusquamperfektSie)

            R.id.play_konjunktiv2_futur1_ich ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv2Futur1Ich)
            R.id.play_konjunktiv2_futur1_du ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv2Futur1Du)
            R.id.play_konjunktiv2_futur1_er ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv2Futur1Er)
            R.id.play_konjunktiv2_futur1_wir ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv2Futur1Wir)
            R.id.play_konjunktiv2_futur1_ihr ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv2Futur1Ihr)
            R.id.play_konjunktiv2_futur1_sie ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv2Futur1Sie)

            R.id.play_konjunktiv2_futur2_ich ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv2Futur2Ich)
            R.id.play_konjunktiv2_futur2_du ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv2Futur2Du)
            R.id.play_konjunktiv2_futur2_er ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv2Futur2Er)
            R.id.play_konjunktiv2_futur2_wir ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv2Futur2Wir)
            R.id.play_konjunktiv2_futur2_ihr ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv2Futur2Ihr)
            R.id.play_konjunktiv2_futur2_sie ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.konjunktiv2Futur2Sie)

            R.id.play_imperativ_prasens_du ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.imperativDu)
            R.id.play_imperativ_prasens_ihr ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.imperativIhr)
            R.id.play_imperativ_prasens_sie ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.imperativSie)

            R.id.play_infinitiv_prasens ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.infinitivPrasens)
            R.id.play_infinitiv_perfekt ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.infinitivPerfekt)
            R.id.play_partizip_prasens ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.partizipPrasens)
            R.id.play_partizip_perfekt ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.partizipPerfekt)

            else -> onClickDemo()
        }
    }

    /**
     * Start a show case view for demo mode.
     */
    private fun defineDemoMode() {
        showcaseView = ShowcaseView.Builder(this)
                .withMaterialShowcase()
                .setTarget(ViewTarget(findViewById(R.id.infinitive)))
                .setContentTitle(getString(R.string.details))
                .setContentText(getString(R.string.infinitive))
                .setStyle(R.style.CustomShowcaseTheme2)
                .replaceEndButton(R.layout.view_custom_button)
                .setOnClickListener(this)
                .build()
        showcaseView!!.setButtonText(getString(R.string.next))
    }

    /**
     * Defines what item to show case view for demo mode.
     */
    private fun onClickDemo() {
        if (!demo) return
        when (counter) {
            0 -> {
                showcaseView!!.setShowcase(ViewTarget(findViewById(R.id.groupe)), true)
                showcaseView!!.setContentText(getString(R.string.group))
            }

            1 -> {
                showcaseView!!.setShowcase(ViewTarget(findViewById(R.id.definition)), true)
                showcaseView!!.setContentText(getString(R.string.definition))
            }

            2 -> {
                showcaseView!!.setShowcase(ViewTarget(findViewById(R.id.sample2)), true)
                showcaseView!!.setContentText(getString(R.string.examples))
            }

            3 -> {
                scrollView!!.requestChildFocus(findViewById(R.id.indikativ_prasens_sie), findViewById(R.id.indikativ_prasens_sie))
                scrollView!!.requestChildFocus(findViewById(R.id.indikativ_prasens_ich), findViewById(R.id.indikativ_prasens_ich))
                showcaseView!!.setShowcase(ViewTarget(findViewById(R.id.indikativ_prasens_ich)), true)
                showcaseView!!.setContentTitle(getString(R.string.conjugations))
                showcaseView!!.setContentText(getString(R.string.conjugations_description))
            }

            4 -> {
                showcaseView!!.setShowcase(ViewTarget(fabAdd), true)
                showcaseView!!.setContentTitle(getString(R.string.favorites))
                showcaseView!!.setContentText(getString(R.string.add_remove_from_favorites))
                showcaseView!!.setButtonText(getString(R.string.got_it))
            }

            5 -> {
                showcaseView!!.hide()
                demo = false
            }
        }
        counter++
    }
}
