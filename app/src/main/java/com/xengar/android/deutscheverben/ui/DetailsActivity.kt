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
//import com.google.android.gms.ads.AdView
//import com.google.firebase.analytics.FirebaseAnalytics
import com.xengar.android.deutscheverben.R
import com.xengar.android.deutscheverben.data.Conjugation
import com.xengar.android.deutscheverben.data.Verb
import com.xengar.android.deutscheverben.utils.ActivityUtils
//import com.xengar.android.deutscheverben.utils.LogAdListener

import java.util.ArrayList

import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_COLOR
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.COLUMN_ID
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.CONTENT_CONJUGATIONS_URI
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.CONTENT_FAVORITES_URI
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry.Companion.CONTENT_VERBS_URI
import com.xengar.android.deutscheverben.utils.Constants.CONJUGATION_ID
import com.xengar.android.deutscheverben.utils.Constants.DEMO_MODE
import com.xengar.android.deutscheverben.utils.Constants.DETAILS_ACTIVITY
import com.xengar.android.deutscheverben.utils.Constants.DRAWABLE
import com.xengar.android.deutscheverben.utils.Constants.IL
import com.xengar.android.deutscheverben.utils.Constants.ILS
import com.xengar.android.deutscheverben.utils.Constants.JE
import com.xengar.android.deutscheverben.utils.Constants.JEA
import com.xengar.android.deutscheverben.utils.Constants.LOG
import com.xengar.android.deutscheverben.utils.Constants.ME
import com.xengar.android.deutscheverben.utils.Constants.MEA
import com.xengar.android.deutscheverben.utils.Constants.NOUS
import com.xengar.android.deutscheverben.utils.Constants.PAGE_VERB_DETAILS
import com.xengar.android.deutscheverben.utils.Constants.QUE
import com.xengar.android.deutscheverben.utils.Constants.QUEA
import com.xengar.android.deutscheverben.utils.Constants.SE
import com.xengar.android.deutscheverben.utils.Constants.SEA
import com.xengar.android.deutscheverben.utils.Constants.TE
import com.xengar.android.deutscheverben.utils.Constants.TEA
import com.xengar.android.deutscheverben.utils.Constants.TU
import com.xengar.android.deutscheverben.utils.Constants.TYPE_ADD_FAV
import com.xengar.android.deutscheverben.utils.Constants.TYPE_DEL_FAV
import com.xengar.android.deutscheverben.utils.Constants.TYPE_PAGE
import com.xengar.android.deutscheverben.utils.Constants.TYPE_SHARE
import com.xengar.android.deutscheverben.utils.Constants.VERB
import com.xengar.android.deutscheverben.utils.Constants.VERBS
import com.xengar.android.deutscheverben.utils.Constants.VERB_ID
import com.xengar.android.deutscheverben.utils.Constants.VERB_NAME
import com.xengar.android.deutscheverben.utils.Constants.VOUS

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
    private var sample1:TextView? = null
    private var sample2:TextView? = null
    private var sample3:TextView? = null

    //private var mFirebaseAnalytics:FirebaseAnalytics? = null
    //private var mAdView:AdView? = null

    // Demo
    private var showcaseView:ShowcaseView? = null
    private var scrollView:NestedScrollView? = null
    private var demo = false
    private var counter = 0


    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        // Obtain the FirebaseAnalytics instance.
        //mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        //ActivityUtils.firebaseAnalyticsLogEventSelectContent(
        //        mFirebaseAnalytics!!, PAGE_VERB_DETAILS, PAGE_VERB_DETAILS, TYPE_PAGE)

        // create AdMob banner
        //val listener = LogAdListener(mFirebaseAnalytics!!, DETAILS_ACTIVITY)
        //mAdView = ActivityUtils.createAdMobBanner(this, listener)

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

        findViewById<View>(R.id.play_indicative_present_je).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_present_tu).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_present_il).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_present_nous).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_present_vous).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_present_ils).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_passe_compose_je).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_passe_compose_tu).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_passe_compose_il).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_passe_compose_nous).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_passe_compose_vous).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_passe_compose_ils).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_imperfait_je).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_imperfait_tu).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_imperfait_il).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_imperfait_nous).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_imperfait_vous).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_imperfait_ils).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_plus_que_parfait_je).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_plus_que_parfait_tu).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_plus_que_parfait_il).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_plus_que_parfait_nous).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_plus_que_parfait_vous).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_plus_que_parfait_ils).setOnClickListener(this)

        findViewById<View>(R.id.play_indicative_passe_simple_je).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_passe_simple_tu).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_passe_simple_il).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_passe_simple_nous).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_passe_simple_vous).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_passe_simple_ils).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_passe_anterieur_je).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_passe_anterieur_tu).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_passe_anterieur_il).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_passe_anterieur_nous).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_passe_anterieur_vous).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_passe_anterieur_ils).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_futur_simple_je).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_futur_simple_tu).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_futur_simple_il).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_futur_simple_nous).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_futur_simple_vous).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_futur_simple_ils).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_futur_anterieur_je).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_futur_anterieur_tu).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_futur_anterieur_il).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_futur_anterieur_nous).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_futur_anterieur_vous).setOnClickListener(this)
        findViewById<View>(R.id.play_indicative_futur_anterieur_ils).setOnClickListener(this)

        findViewById<View>(R.id.play_conditionnel_present_je).setOnClickListener(this)
        findViewById<View>(R.id.play_conditionnel_present_tu).setOnClickListener(this)
        findViewById<View>(R.id.play_conditionnel_present_il).setOnClickListener(this)
        findViewById<View>(R.id.play_conditionnel_present_nous).setOnClickListener(this)
        findViewById<View>(R.id.play_conditionnel_present_vous).setOnClickListener(this)
        findViewById<View>(R.id.play_conditionnel_present_ils).setOnClickListener(this)
        findViewById<View>(R.id.play_conditionnel_passe_je).setOnClickListener(this)
        findViewById<View>(R.id.play_conditionnel_passe_tu).setOnClickListener(this)
        findViewById<View>(R.id.play_conditionnel_passe_il).setOnClickListener(this)
        findViewById<View>(R.id.play_conditionnel_passe_nous).setOnClickListener(this)
        findViewById<View>(R.id.play_conditionnel_passe_vous).setOnClickListener(this)
        findViewById<View>(R.id.play_conditionnel_passe_ils).setOnClickListener(this)

        findViewById<View>(R.id.play_subjonctif_present_je).setOnClickListener(this)
        findViewById<View>(R.id.play_subjonctif_present_tu).setOnClickListener(this)
        findViewById<View>(R.id.play_subjonctif_present_il).setOnClickListener(this)
        findViewById<View>(R.id.play_subjonctif_present_nous).setOnClickListener(this)
        findViewById<View>(R.id.play_subjonctif_present_vous).setOnClickListener(this)
        findViewById<View>(R.id.play_subjonctif_present_ils).setOnClickListener(this)
        findViewById<View>(R.id.play_subjonctif_passe_je).setOnClickListener(this)
        findViewById<View>(R.id.play_subjonctif_passe_tu).setOnClickListener(this)
        findViewById<View>(R.id.play_subjonctif_passe_il).setOnClickListener(this)
        findViewById<View>(R.id.play_subjonctif_passe_nous).setOnClickListener(this)
        findViewById<View>(R.id.play_subjonctif_passe_vous).setOnClickListener(this)
        findViewById<View>(R.id.play_subjonctif_passe_ils).setOnClickListener(this)
        findViewById<View>(R.id.play_subjonctif_imperfait_je).setOnClickListener(this)
        findViewById<View>(R.id.play_subjonctif_imperfait_tu).setOnClickListener(this)
        findViewById<View>(R.id.play_subjonctif_imperfait_il).setOnClickListener(this)
        findViewById<View>(R.id.play_subjonctif_imperfait_nous).setOnClickListener(this)
        findViewById<View>(R.id.play_subjonctif_imperfait_vous).setOnClickListener(this)
        findViewById<View>(R.id.play_subjonctif_imperfait_ils).setOnClickListener(this)
        findViewById<View>(R.id.play_subjonctif_plus_que_parfait_je).setOnClickListener(this)
        findViewById<View>(R.id.play_subjonctif_plus_que_parfait_tu).setOnClickListener(this)
        findViewById<View>(R.id.play_subjonctif_plus_que_parfait_il).setOnClickListener(this)
        findViewById<View>(R.id.play_subjonctif_plus_que_parfait_nous).setOnClickListener(this)
        findViewById<View>(R.id.play_subjonctif_plus_que_parfait_vous).setOnClickListener(this)
        findViewById<View>(R.id.play_subjonctif_plus_que_parfait_ils).setOnClickListener(this)

        findViewById<View>(R.id.play_imperatif_present_tu).setOnClickListener(this)
        findViewById<View>(R.id.play_imperatif_present_nous).setOnClickListener(this)
        findViewById<View>(R.id.play_imperatif_present_vous).setOnClickListener(this)
        findViewById<View>(R.id.play_imperatif_passe_tu).setOnClickListener(this)
        findViewById<View>(R.id.play_imperatif_passe_nous).setOnClickListener(this)
        findViewById<View>(R.id.play_imperatif_passe_vous).setOnClickListener(this)

        findViewById<View>(R.id.play_infinitive_present).setOnClickListener(this)
        findViewById<View>(R.id.play_infinitive_passe).setOnClickListener(this)
        findViewById<View>(R.id.play_participe_present).setOnClickListener(this)
        findViewById<View>(R.id.play_participe_passe1).setOnClickListener(this)
        findViewById<View>(R.id.play_participe_passe2).setOnClickListener(this)
        findViewById<View>(R.id.play_gerondif_present).setOnClickListener(this)
        findViewById<View>(R.id.play_gerondif_passe).setOnClickListener(this)
    }

    /** Called when leaving the activity  */
    public override fun onPause() {
        //mAdView?.pause()
        super.onPause()
    }

    /** Called when returning to the activity
     *  Guaranteed to be called after the Activity has been restored to its original state.
     *  See https://www.androiddesignpatterns.com/2013/08/fragment-transaction-commit-state-loss.html
     * */
    public override fun onPostResume() {
        super.onPostResume()
        //mAdView?.resume()
    }

    /** Called before the activity is destroyed  */
    public override fun onDestroy() {
        //mAdView?.destroy()
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
                //ActivityUtils.launchSearchActivity(applicationContext)
                return true
            }

            R.id.action_share -> {
                ActivityUtils.launchShareText(this, createShareText())
                val verbName = if (verb != null) verb!!.infinitive else "verb name not available"
                //ActivityUtils.firebaseAnalyticsLogEventSelectContent(mFirebaseAnalytics!!,
                //        "Verb: $verbName, VerbId: $verbID", TYPE_SHARE, TYPE_SHARE)
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
            text = ("Verb: " + verb!!.infinitive
                    + "\n" + getString(R.string.group) + ": " + verb!!.group
                    + "\n\n" + getString(R.string.definition) + ":\n" + verb!!.definition
                    + "\n\n" + getString(R.string.examples) + ":\n" + verb!!.sample1
                    + "\n" + verb!!.sample2
                    + "\n" + verb!!.sample3
                    + "\n\n" + getString(R.string.indicatif) + " " + getString(R.string.present) + ":"
                    + "\n" + conjugation!!.indicatifPresentJe
                    + "\n" + conjugation!!.indicatifPresentTu
                    + "\n" + conjugation!!.indicatifPresentIl
                    + "\n" + conjugation!!.indicatifPresentNous
                    + "\n" + conjugation!!.indicatifPresentVous
                    + "\n" + conjugation!!.indicatifPresentIls
                    + "\n\n" + getString(R.string.indicatif) + " " + getString(R.string.passe_compose) + ":"
                    + "\n" + conjugation!!.indicatifPasseComposeJe
                    + "\n" + conjugation!!.indicatifPasseComposeTu
                    + "\n" + conjugation!!.indicatifPasseComposeIl
                    + "\n" + conjugation!!.indicatifPasseComposeNous
                    + "\n" + conjugation!!.indicatifPasseComposeVous
                    + "\n" + conjugation!!.indicatifPasseComposeIls
                    + "\n\n" + getString(R.string.indicatif) + " " + getString(R.string.imperfait) + ":"
                    + "\n" + conjugation!!.indicatifImperfaitJe
                    + "\n" + conjugation!!.indicatifImperfaitTu
                    + "\n" + conjugation!!.indicatifImperfaitIl
                    + "\n" + conjugation!!.indicatifImperfaitNous
                    + "\n" + conjugation!!.indicatifImperfaitVous
                    + "\n" + conjugation!!.indicatifImperfaitIls
                    + "\n\n" + getString(R.string.indicatif) + " " + getString(R.string.plus_que_parfait) + ":"
                    + "\n" + conjugation!!.indicatifPlusQueParfaitJe
                    + "\n" + conjugation!!.indicatifPlusQueParfaitTu
                    + "\n" + conjugation!!.indicatifPlusQueParfaitIl
                    + "\n" + conjugation!!.indicatifPlusQueParfaitNous
                    + "\n" + conjugation!!.indicatifPlusQueParfaitVous
                    + "\n" + conjugation!!.indicatifPlusQueParfaitIls
                    + "\n\n" + getString(R.string.indicatif) + " " + getString(R.string.passe_simple) + ":"
                    + "\n" + conjugation!!.indicatifPasseSimpleJe
                    + "\n" + conjugation!!.indicatifPasseSimpleTu
                    + "\n" + conjugation!!.indicatifPasseSimpleIl
                    + "\n" + conjugation!!.indicatifPasseSimpleNous
                    + "\n" + conjugation!!.indicatifPasseSimpleVous
                    + "\n" + conjugation!!.indicatifPasseSimpleIls
                    + "\n\n" + getString(R.string.indicatif) + " " + getString(R.string.passe_anterieur) + ":"
                    + "\n" + conjugation!!.indicatifPasseAnterieurJe
                    + "\n" + conjugation!!.indicatifPasseAnterieurTu
                    + "\n" + conjugation!!.indicatifPasseAnterieurIl
                    + "\n" + conjugation!!.indicatifPasseAnterieurNous
                    + "\n" + conjugation!!.indicatifPasseAnterieurVous
                    + "\n" + conjugation!!.indicatifPasseAnterieurIls
                    + "\n\n" + getString(R.string.indicatif) + " " + getString(R.string.futur_simple) + ":"
                    + "\n" + conjugation!!.indicatifFuturSimpleJe
                    + "\n" + conjugation!!.indicatifFuturSimpleTu
                    + "\n" + conjugation!!.indicatifFuturSimpleIl
                    + "\n" + conjugation!!.indicatifFuturSimpleNous
                    + "\n" + conjugation!!.indicatifFuturSimpleVous
                    + "\n" + conjugation!!.indicatifFuturSimpleIls
                    + "\n\n" + getString(R.string.indicatif) + " " + getString(R.string.futur_anterieur) + ":"
                    + "\n" + conjugation!!.indicatifFuturAnterieurJe
                    + "\n" + conjugation!!.indicatifFuturAnterieurTu
                    + "\n" + conjugation!!.indicatifFuturAnterieurIl
                    + "\n" + conjugation!!.indicatifFuturAnterieurNous
                    + "\n" + conjugation!!.indicatifFuturAnterieurVous
                    + "\n" + conjugation!!.indicatifFuturAnterieurIls
                    + "\n\n" + getString(R.string.conditionnel) + " " + getString(R.string.present) + ":"
                    + "\n" + conjugation!!.conditionnelPresentJe
                    + "\n" + conjugation!!.conditionnelPresentTu
                    + "\n" + conjugation!!.conditionnelPresentIl
                    + "\n" + conjugation!!.conditionnelPresentNous
                    + "\n" + conjugation!!.conditionnelPresentVous
                    + "\n" + conjugation!!.conditionnelPresentIls
                    + "\n\n" + getString(R.string.conditionnel) + " " + getString(R.string.passe) + ":"
                    + "\n" + conjugation!!.conditionnelPasseJe
                    + "\n" + conjugation!!.conditionnelPasseTu
                    + "\n" + conjugation!!.conditionnelPasseIl
                    + "\n" + conjugation!!.conditionnelPasseNous
                    + "\n" + conjugation!!.conditionnelPasseVous
                    + "\n" + conjugation!!.conditionnelPasseIls
                    + "\n\n" + getString(R.string.subjonctif) + " " + getString(R.string.present) + ":"
                    + "\n" + conjugation!!.subjonctifPresentJe
                    + "\n" + conjugation!!.subjonctifPresentTu
                    + "\n" + conjugation!!.subjonctifPresentIl
                    + "\n" + conjugation!!.subjonctifPresentNous
                    + "\n" + conjugation!!.subjonctifPresentVous
                    + "\n" + conjugation!!.subjonctifPresentIls
                    + "\n\n" + getString(R.string.subjonctif) + " " + getString(R.string.passe) + ":"
                    + "\n" + conjugation!!.subjonctifPasseJe
                    + "\n" + conjugation!!.subjonctifPasseTu
                    + "\n" + conjugation!!.subjonctifPasseIl
                    + "\n" + conjugation!!.subjonctifPasseNous
                    + "\n" + conjugation!!.subjonctifPasseVous
                    + "\n" + conjugation!!.subjonctifPasseIls
                    + "\n\n" + getString(R.string.subjonctif) + " " + getString(R.string.imperfait) + ":"
                    + "\n" + conjugation!!.subjonctifImperfaitJe
                    + "\n" + conjugation!!.subjonctifImperfaitTu
                    + "\n" + conjugation!!.subjonctifImperfaitIl
                    + "\n" + conjugation!!.subjonctifImperfaitNous
                    + "\n" + conjugation!!.subjonctifImperfaitVous
                    + "\n" + conjugation!!.subjonctifImperfaitIls
                    + "\n\n" + getString(R.string.subjonctif) + " " + getString(R.string.plus_que_parfait) + ":"
                    + "\n" + conjugation!!.subjonctifPlusQueParfaitJe
                    + "\n" + conjugation!!.subjonctifPlusQueParfaitTu
                    + "\n" + conjugation!!.subjonctifPlusQueParfaitIl
                    + "\n" + conjugation!!.subjonctifPlusQueParfaitNous
                    + "\n" + conjugation!!.subjonctifPlusQueParfaitVous
                    + "\n" + conjugation!!.subjonctifPlusQueParfaitIls
                    + "\n\n" + getString(R.string.imperatif) + " " + getString(R.string.present) + ":"
                    + "\n" + conjugation!!.imperatifPresentTu
                    + "\n" + conjugation!!.imperatifPresentNous
                    + "\n" + conjugation!!.imperatifPresentVous
                    + "\n\n" + getString(R.string.imperatif) + " " + getString(R.string.passe) + ":"
                    + "\n" + conjugation!!.imperatifPasseTu
                    + "\n" + conjugation!!.imperatifPasseNous
                    + "\n" + conjugation!!.imperatifPasseVous
                    + "\n\n" + getString(R.string.infinitive) + " " + getString(R.string.present) + ":"
                    + "\n" + conjugation!!.infinitivePresent
                    + "\n\n" + getString(R.string.infinitive) + " " + getString(R.string.passe) + ":"
                    + "\n" + conjugation!!.infinitivePasse
                    + "\n\n" + getString(R.string.participe) + " " + getString(R.string.present) + ":"
                    + "\n" + conjugation!!.participePresent
                    + "\n\n" + getString(R.string.participe) + " " + getString(R.string.passe) + ":"
                    + "\n" + conjugation!!.participePasse1
                    + "\n" + conjugation!!.participePasse2
                    + "\n\n" + getString(R.string.gerondif) + " " + getString(R.string.present) + ":"
                    + "\n" + conjugation!!.gerondifPresent
                    + "\n\n" + getString(R.string.gerondif) + " " + getString(R.string.passe) + ":"
                    + "\n" + conjugation!!.gerondifPasse)
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
            //ActivityUtils.firebaseAnalyticsLogEventSelectContent(mFirebaseAnalytics!!,
            //        "$VERB_ID $verbID", verbName, TYPE_ADD_FAV)
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
            //ActivityUtils.firebaseAnalyticsLogEventSelectContent(mFirebaseAnalytics!!,
            //        "$VERB_ID $verbID", verbName, TYPE_DEL_FAV)
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

        var isOnlyInfinitive = false
        var isOnlyIlIls = false
        var isOnlyIlIlsIndicatif = false
        var isRestrictedLikeFrire = false
        var isImpersonnel = false

        if (c.infinitivePresent.isNotEmpty() && !c.infinitivePresent.contentEquals(verbName)) {
            // if we need, conjugate the verb model.

            var isEtre = false
            var isAvoir = false
            var isPronominal = false
            var isParticipePasseInvariable = false

            if (!verb!!.notes.isEmpty()) {
                val arrayNotes = verb!!.notes.split(", ".toRegex()).dropLastWhile{ it.isEmpty() }.toTypedArray()
                for (note in arrayNotes) {
                    if (note.contentEquals("être ou avoir")) {
                        isAvoir = true     // uses both auxiliars
                        isEtre = true
                    }
                    else if (note.contentEquals("avoir")) {
                        isAvoir = true     // uses auxiliar avoir
                    }
                    else if (note.contentEquals("être")) {
                        isEtre = true      // uses auxiliar être
                    }
                    else if (note.contentEquals("P")) {
                        isPronominal = true
                    }
                    else if (note.contentEquals("I")
                            || note.contentEquals("Ti")) {
                        isParticipePasseInvariable = true // do not add accords in pronominals
                    }
                    else if (note.contentEquals("seulement a l'infinitif")) {
                        isOnlyInfinitive = true
                    }
                    else if (note.contentEquals("seulement a l'inf. au part. présent et aux 3es pers.")
                            || note.contentEquals("seulement a l'inf. et a la 3e personne")) {
                        isOnlyIlIls = true
                    }
                    else if (note.contentEquals("seulement aux 3es pers. de l'indicatif")) {
                        isOnlyIlIlsIndicatif = true
                    }
                    else if (note.contentEquals("seulement a l'inf. au part. passé au singulier de l'ind. présent et futur du cond. de l'impératif et aux temps composés")) {
                        isRestrictedLikeFrire = true
                    }
                    else if (note.contentEquals("imp.")) {
                        isImpersonnel = true
                    }
                }
            }
            // all pronominals conjugate with auxiliar être
            if (isPronominal) {
                isEtre = true
            }

            conjugateVerb(c, verbName, isPronominal)
            // check if the verb uses other auxiliar verb and replace it. Like partir, mourir, s'ecrier
            reviewAuxiliar(c, isEtre, isAvoir)
            if (isPronominal) {
                addReflexive(c, isParticipePasseInvariable)
            }
            if (isParticipePasseInvariable) {
                setParticipePasseAsInvariable(c)
            }
            // TODO: Optional - Add accord de participe with persons. Like disparaître
        }
        addPronoms(c)

        if (isOnlyInfinitive) {
            ignoreConjugations(c)
        }
        else if (isOnlyIlIls) {
            // Seulement a la 3e personne du singulier et du pluriel. Like advenir, s'ensuivre
            ignoreAllPersonsExceptIlAndIls(c)
        }
        else if (isOnlyIlIlsIndicatif) {
            ignoreAllPersonsExceptIlAndIlsIndicatif(c)
        }
        else if (isImpersonnel) {
            ignoreAllPersonsExceptIl(c)
        }
        else if (isRestrictedLikeFrire) {
            ignoreIndicatifPresentNousVousIls(c)
            ignoreIndicatifImperfait(c)
            ignoreIndicatifPasseSimple(c)
            ignoreSubjonctifPresent(c)
            ignoreSubjonctifImperfait(c)
            ignoreImperatifNousVous(c)
        }
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

        definition?.text = verb.definition
        sample1?.text = verb.sample1
        sample2?.text = verb.sample2
        sample3?.text = verb.sample3

        val fontSize = Integer.parseInt(ActivityUtils.getPreferenceFontSize(applicationContext))
        (findViewById<View>(R.id.groupe) as TextView).setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
        (findViewById<View>(R.id.definition_title) as TextView).setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
        (findViewById<View>(R.id.examples_title) as TextView).setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
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

        //ActivityUtils.firebaseAnalyticsLogEventViewItem(
        //        mFirebaseAnalytics!!, "" + verbID, verb.infinitive, VERBS)
    }


    /**
     * Conjugates the verb according to the model.
     * @param c Conjugation conjugation
     * @param isPronominal boolean
     * @param verbInfinitive String
     */
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
        replaceAccents(c, verbInfinitive)
        replaceConjugationModel(c, verbInfinitive)
        replaceParticipePasse(c, verbInfinitive)
    }

    private fun setParticipePasseAsInvariable(c:Conjugation) {
        // TODO: Ensure conjugations are invariable
        if (c.participePasse1.contains("(") ) {
            val aux = c.participePasse1.substringBefore("(")
            c.participePasse1 = "$aux(invar.)"
        } else {
            c.participePasse1 = c.participePasse1 + " (invar.)"
        }
    }

    /**
     * Known Exceptions for the participe passe in conjugation model.
     * @param c Conjugation
     * @param verbInfinitive infinitive
     */
    private fun replaceParticipePasse(c:Conjugation, verbInfinitive:String) {

        var old = ""
        var new = ""

        when (c.id) {
            20L -> // finir
                if (verbInfinitive.contains("maudire")) {
                    old = "maudi"
                    new = "maudit"
                    c.participePasse1 = "maudit (e)"
                }
            46L -> // mouvoir
                if (verbInfinitive.contains("promouvoir")) {
                    old = "promû"
                    new = "promu"
                    c.participePasse1 = "promu (u, ue, us, ues)"
                } else if (verbInfinitive.contains("émouvoir")) {
                    old = "émû"
                    new = "ému"
                    c.participePasse1 = "ému (u, ue, us, ues)"
                }
            77L -> // conclure
                if (verbInfinitive.contains("inclure")) {
                    old = "inclu"
                    new = "inclus"
                    c.participePasse1 = "inclus (e, es)"
                } else if (verbInfinitive.contains("occlure")) {
                    old = "occlu"
                    new = "occlus"
                    c.participePasse1 = "occlus (e, es)"
                }
            78L -> // absoudre
                if (verbInfinitive.contains("résoudre")) {
                    old = "résous"
                    new = "résolu"
                    c.participePasse1 = "résolu (u, ue, us, ues)"
                }
            87L -> // confire
                if (verbInfinitive.contains("circoncire")) {
                    old = "circoncit"
                    new = "circoncis"
                    c.participePasse1 = "circoncis"
                } else if (verbInfinitive.contains("suffire")) {
                    old = "suffit"
                    new = "suffi"
                    c.participePasse1 = "suffi"
                }
        }

        if (old.isNotEmpty() && new.isNotEmpty()) {
            replaceParticipePasse(c, old, new)
        }
    }

    private fun replaceParticipePasse(c:Conjugation, old:String, new:String) {
        c.infinitivePasse = c.infinitivePasse.replace(old, new)

        c.participePasse2 = c.participePasse2.replace(old, new)
        c.gerondifPasse = c.gerondifPasse.replace(old, new)

        c.imperatifPasseTu = c.imperatifPasseTu.replace(old, new)
        c.imperatifPasseNous = c.imperatifPasseNous.replace(old, new)
        c.imperatifPasseVous = c.imperatifPasseVous.replace(old, new)

        c.indicatifPasseComposeJe = c.indicatifPasseComposeJe.replace(old, new)
        c.indicatifPasseComposeTu = c.indicatifPasseComposeTu.replace(old, new)
        c.indicatifPasseComposeIl = c.indicatifPasseComposeIl.replace(old, new)
        c.indicatifPasseComposeNous = c.indicatifPasseComposeNous.replace(old, new)
        c.indicatifPasseComposeVous = c.indicatifPasseComposeVous.replace(old, new)
        c.indicatifPasseComposeIls = c.indicatifPasseComposeIls.replace(old, new)

        c.indicatifPlusQueParfaitJe = c.indicatifPlusQueParfaitJe.replace(old, new)
        c.indicatifPlusQueParfaitTu = c.indicatifPlusQueParfaitTu.replace(old, new)
        c.indicatifPlusQueParfaitIl = c.indicatifPlusQueParfaitIl.replace(old, new)
        c.indicatifPlusQueParfaitNous = c.indicatifPlusQueParfaitNous.replace(old, new)
        c.indicatifPlusQueParfaitVous = c.indicatifPlusQueParfaitVous.replace(old, new)
        c.indicatifPlusQueParfaitIls = c.indicatifPlusQueParfaitIls.replace(old, new)

        c.indicatifPasseAnterieurJe = c.indicatifPasseAnterieurJe.replace(old, new)
        c.indicatifPasseAnterieurTu = c.indicatifPasseAnterieurTu.replace(old, new)
        c.indicatifPasseAnterieurIl = c.indicatifPasseAnterieurIl.replace(old, new)
        c.indicatifPasseAnterieurNous = c.indicatifPasseAnterieurNous.replace(old, new)
        c.indicatifPasseAnterieurVous = c.indicatifPasseAnterieurVous.replace(old, new)
        c.indicatifPasseAnterieurIls = c.indicatifPasseAnterieurIls.replace(old, new)

        c.indicatifFuturAnterieurJe = c.indicatifFuturAnterieurJe.replace(old, new)
        c.indicatifFuturAnterieurTu = c.indicatifFuturAnterieurTu.replace(old, new)
        c.indicatifFuturAnterieurIl = c.indicatifFuturAnterieurIl.replace(old, new)
        c.indicatifFuturAnterieurNous = c.indicatifFuturAnterieurNous.replace(old, new)
        c.indicatifFuturAnterieurVous = c.indicatifFuturAnterieurVous.replace(old, new)
        c.indicatifFuturAnterieurIls = c.indicatifFuturAnterieurIls.replace(old, new)

        c.conditionnelPasseJe = c.conditionnelPasseJe.replace(old, new)
        c.conditionnelPasseTu = c.conditionnelPasseTu.replace(old, new)
        c.conditionnelPasseIl = c.conditionnelPasseIl.replace(old, new)
        c.conditionnelPasseNous = c.conditionnelPasseNous.replace(old, new)
        c.conditionnelPasseVous = c.conditionnelPasseVous.replace(old, new)
        c.conditionnelPasseIls = c.conditionnelPasseIls.replace(old, new)

        c.subjonctifPasseJe = c.subjonctifPasseJe.replace(old, new)
        c.subjonctifPasseTu = c.subjonctifPasseTu.replace(old, new)
        c.subjonctifPasseIl = c.subjonctifPasseIl.replace(old, new)
        c.subjonctifPasseNous = c.subjonctifPasseNous.replace(old, new)
        c.subjonctifPasseVous = c.subjonctifPasseVous.replace(old, new)
        c.subjonctifPasseIls = c.subjonctifPasseIls.replace(old, new)

        c.subjonctifPlusQueParfaitJe = c.subjonctifPlusQueParfaitJe.replace(old, new)
        c.subjonctifPlusQueParfaitTu = c.subjonctifPlusQueParfaitTu.replace(old, new)
        c.subjonctifPlusQueParfaitIl = c.subjonctifPlusQueParfaitIl.replace(old, new)
        c.subjonctifPlusQueParfaitNous = c.subjonctifPlusQueParfaitNous.replace(old, new)
        c.subjonctifPlusQueParfaitVous = c.subjonctifPlusQueParfaitVous.replace(old, new)
        c.subjonctifPlusQueParfaitIls = c.subjonctifPlusQueParfaitIls.replace(old, new)
    }

    /**
     * Exceptions for the conjugation model. Cases when the model changes by rule.
     * @param c Conjugation
     * @param verbInfinitive infinitive
     */
    private fun replaceConjugationModel(c:Conjugation, verbInfinitive:String) {
        if (c.id == 84L) { // dire
            if (verbInfinitive.contains("contredire") || verbInfinitive.contains("dédire")
                    || verbInfinitive.contains("interdire") || verbInfinitive.contains("médire")
                    || verbInfinitive.contains("prédire")) {
                c.indicatifPresentVous = c.indicatifPresentVous.replace("dites", "disez")
            }
        }
    }

    /**
     * Known Exceptions for the accents in conjugation model.
     * @param c Conjugation
     * @param verbInfinitive infinitive
     */
    private fun replaceAccents(c:Conjugation, verbInfinitive:String) {
        if (c.id == 73L && !verbInfinitive.contentEquals("croître")) {
            // NOTE: all verbes, except croître : accroître, décroître, recroître
            // conjugate without some accents
            c.indicatifPresentJe = c.indicatifPresentJe.replace("î", "i")
            c.indicatifPresentTu = c.indicatifPresentTu.replace("î", "i")

            c.indicatifPasseSimpleJe = c.indicatifPasseSimpleJe.replace("û", "u")
            c.indicatifPasseSimpleTu = c.indicatifPasseSimpleTu.replace("û", "u")
            c.indicatifPasseSimpleIl = c.indicatifPasseSimpleIl.replace("û", "u")
            c.indicatifPasseSimpleIls = c.indicatifPasseSimpleIls.replace("û", "u")

            c.indicatifImperfaitJe = c.indicatifImperfaitJe.replace("û", "u")
            c.indicatifImperfaitTu = c.indicatifImperfaitTu.replace("û", "u")
            c.indicatifImperfaitNous = c.indicatifImperfaitNous.replace("û", "u")
            c.indicatifImperfaitVous = c.indicatifImperfaitVous.replace("û", "u")
            c.indicatifImperfaitIls = c.indicatifImperfaitIls.replace("û", "u")

            c.imperatifPresentTu = c.imperatifPresentTu.replace("î", "i")
            c.imperatifPasseTu = c.imperatifPasseTu.replace("crû", "cru")
            c.imperatifPasseNous = c.imperatifPasseNous.replace("crû", "cru")
            c.imperatifPasseVous = c.imperatifPasseVous.replace("crû", "cru")

            c.infinitivePasse = c.infinitivePasse.replace("crû", "cru")
            c.participePasse1 = c.participePasse1.replace("crû", "cru")
            c.participePasse2 = c.participePasse2.replace("crû", "cru")
            c.gerondifPasse = c.gerondifPasse.replace("crû", "cru")

            c.indicatifPasseComposeJe = c.indicatifPasseComposeJe.replace("crû", "cru")
            c.indicatifPasseComposeTu = c.indicatifPasseComposeTu.replace("crû", "cru")
            c.indicatifPasseComposeIl = c.indicatifPasseComposeIl.replace("crû", "cru")
            c.indicatifPasseComposeNous = c.indicatifPasseComposeNous.replace("crû", "cru")
            c.indicatifPasseComposeVous = c.indicatifPasseComposeVous.replace("crû", "cru")
            c.indicatifPasseComposeIls = c.indicatifPasseComposeIls.replace("crû", "cru")

            c.indicatifPlusQueParfaitJe = c.indicatifPlusQueParfaitJe.replace("crû", "cru")
            c.indicatifPlusQueParfaitTu = c.indicatifPlusQueParfaitTu.replace("crû", "cru")
            c.indicatifPlusQueParfaitIl = c.indicatifPlusQueParfaitIl.replace("crû", "cru")
            c.indicatifPlusQueParfaitNous = c.indicatifPlusQueParfaitNous.replace("crû", "cru")
            c.indicatifPlusQueParfaitVous = c.indicatifPlusQueParfaitVous.replace("crû", "cru")
            c.indicatifPlusQueParfaitIls = c.indicatifPlusQueParfaitIls.replace("crû", "cru")

            c.indicatifPasseAnterieurJe = c.indicatifPasseAnterieurJe.replace("crû", "cru")
            c.indicatifPasseAnterieurTu = c.indicatifPasseAnterieurTu.replace("crû", "cru")
            c.indicatifPasseAnterieurIl = c.indicatifPasseAnterieurIl.replace("crû", "cru")
            c.indicatifPasseAnterieurNous = c.indicatifPasseAnterieurNous.replace("crû", "cru")
            c.indicatifPasseAnterieurVous = c.indicatifPasseAnterieurVous.replace("crû", "cru")
            c.indicatifPasseAnterieurIls = c.indicatifPasseAnterieurIls.replace("crû", "cru")

            c.indicatifFuturAnterieurJe = c.indicatifFuturAnterieurJe.replace("crû", "cru")
            c.indicatifFuturAnterieurTu = c.indicatifFuturAnterieurTu.replace("crû", "cru")
            c.indicatifFuturAnterieurIl = c.indicatifFuturAnterieurIl.replace("crû", "cru")
            c.indicatifFuturAnterieurNous = c.indicatifFuturAnterieurNous.replace("crû", "cru")
            c.indicatifFuturAnterieurVous = c.indicatifFuturAnterieurVous.replace("crû", "cru")
            c.indicatifFuturAnterieurIls = c.indicatifFuturAnterieurIls.replace("crû", "cru")

            c.conditionnelPasseJe = c.conditionnelPasseJe.replace("crû", "cru")
            c.conditionnelPasseTu = c.conditionnelPasseTu.replace("crû", "cru")
            c.conditionnelPasseIl = c.conditionnelPasseIl.replace("crû", "cru")
            c.conditionnelPasseNous = c.conditionnelPasseNous.replace("crû", "cru")
            c.conditionnelPasseVous = c.conditionnelPasseVous.replace("crû", "cru")
            c.conditionnelPasseIls = c.conditionnelPasseIls.replace("crû", "cru")

            c.subjonctifImperfaitJe = c.subjonctifImperfaitJe.replace("crû", "cru")
            c.subjonctifImperfaitTu = c.subjonctifImperfaitTu.replace("crû", "cru")
            c.subjonctifImperfaitNous = c.subjonctifImperfaitNous.replace("crû", "cru")
            c.subjonctifImperfaitVous = c.subjonctifImperfaitVous.replace("crû", "cru")
            c.subjonctifImperfaitIls = c.subjonctifImperfaitIls.replace("crû", "cru")

            c.subjonctifPasseJe = c.subjonctifPasseJe.replace("crû", "cru")
            c.subjonctifPasseTu = c.subjonctifPasseTu.replace("crû", "cru")
            c.subjonctifPasseIl = c.subjonctifPasseIl.replace("crû", "cru")
            c.subjonctifPasseNous = c.subjonctifPasseNous.replace("crû", "cru")
            c.subjonctifPasseVous = c.subjonctifPasseVous.replace("crû", "cru")
            c.subjonctifPasseIls = c.subjonctifPasseIls.replace("crû", "cru")

            c.subjonctifPlusQueParfaitJe = c.subjonctifPlusQueParfaitJe.replace("crû", "cru")
            c.subjonctifPlusQueParfaitTu = c.subjonctifPlusQueParfaitTu.replace("crû", "cru")
            c.subjonctifPlusQueParfaitIl = c.subjonctifPlusQueParfaitIl.replace("crû", "cru")
            c.subjonctifPlusQueParfaitNous = c.subjonctifPlusQueParfaitNous.replace("crû", "cru")
            c.subjonctifPlusQueParfaitVous = c.subjonctifPlusQueParfaitVous.replace("crû", "cru")
            c.subjonctifPlusQueParfaitIls = c.subjonctifPlusQueParfaitIls.replace("crû", "cru")
        }
        else if (c.id == 20L && verbInfinitive.contains("amuïr")) {
            // Restore circunflex accent
            c.indicatifPresentJe = c.indicatifPresentJe.replace("amui", "amuï")
            c.indicatifPresentTu = c.indicatifPresentTu.replace("amui", "amuï")
            c.indicatifPresentIl = c.indicatifPresentIl.replace("amui", "amuï")
            c.indicatifPresentNous = c.indicatifPresentNous.replace("amui", "amuï")
            c.indicatifPresentVous = c.indicatifPresentVous.replace("amui", "amuï")
            c.indicatifPresentIls = c.indicatifPresentIls.replace("amui", "amuï")

            c.indicatifPasseComposeJe = c.indicatifPasseComposeJe.replace("amui", "amuï")
            c.indicatifPasseComposeTu = c.indicatifPasseComposeTu.replace("amui", "amuï")
            c.indicatifPasseComposeIl = c.indicatifPasseComposeIl.replace("amui", "amuï")
            c.indicatifPasseComposeNous = c.indicatifPasseComposeNous.replace("amui", "amuï")
            c.indicatifPasseComposeVous = c.indicatifPasseComposeVous.replace("amui", "amuï")
            c.indicatifPasseComposeIls = c.indicatifPasseComposeIls.replace("amui", "amuï")

            c.indicatifImperfaitJe = c.indicatifImperfaitJe.replace("amui", "amuï")
            c.indicatifImperfaitTu = c.indicatifImperfaitTu.replace("amui", "amuï")
            c.indicatifImperfaitIl = c.indicatifImperfaitIl.replace("amui", "amuï")
            c.indicatifImperfaitNous = c.indicatifImperfaitNous.replace("amui", "amuï")
            c.indicatifImperfaitVous = c.indicatifImperfaitVous.replace("amui", "amuï")
            c.indicatifImperfaitIls = c.indicatifImperfaitIls.replace("amui", "amuï")

            c.indicatifPlusQueParfaitJe = c.indicatifPlusQueParfaitJe.replace("amui", "amuï")
            c.indicatifPlusQueParfaitTu = c.indicatifPlusQueParfaitTu.replace("amui", "amuï")
            c.indicatifPlusQueParfaitIl = c.indicatifPlusQueParfaitIl.replace("amui", "amuï")
            c.indicatifPlusQueParfaitNous = c.indicatifPlusQueParfaitNous.replace("amui", "amuï")
            c.indicatifPlusQueParfaitVous = c.indicatifPlusQueParfaitVous.replace("amui", "amuï")
            c.indicatifPlusQueParfaitIls = c.indicatifPlusQueParfaitIls.replace("amui", "amuï")

            c.indicatifPasseSimpleJe = c.indicatifPasseSimpleJe.replace("amui", "amuï")
            c.indicatifPasseSimpleTu = c.indicatifPasseSimpleTu.replace("amui", "amuï")
            c.indicatifPasseSimpleIl = c.indicatifPasseSimpleIl.replace("amui", "amuï")
            c.indicatifPasseSimpleIls = c.indicatifPasseSimpleIls.replace("amui", "amuï")

            c.indicatifPasseAnterieurJe = c.indicatifPasseAnterieurJe.replace("amui", "amuï")
            c.indicatifPasseAnterieurTu = c.indicatifPasseAnterieurTu.replace("amui", "amuï")
            c.indicatifPasseAnterieurIl = c.indicatifPasseAnterieurIl.replace("amui", "amuï")
            c.indicatifPasseAnterieurNous = c.indicatifPasseAnterieurNous.replace("amui", "amuï")
            c.indicatifPasseAnterieurVous = c.indicatifPasseAnterieurVous.replace("amui", "amuï")
            c.indicatifPasseAnterieurIls = c.indicatifPasseAnterieurIls.replace("amui", "amuï")

            c.indicatifFuturSimpleJe = c.indicatifFuturSimpleJe.replace("amui", "amuï")
            c.indicatifFuturSimpleTu = c.indicatifFuturSimpleTu.replace("amui", "amuï")
            c.indicatifFuturSimpleIl = c.indicatifFuturSimpleIl.replace("amui", "amuï")
            c.indicatifFuturSimpleNous = c.indicatifFuturSimpleNous.replace("amui", "amuï")
            c.indicatifFuturSimpleVous = c.indicatifFuturSimpleVous.replace("amui", "amuï")
            c.indicatifFuturSimpleIls = c.indicatifFuturSimpleIls.replace("amui", "amuï")

            c.indicatifFuturAnterieurJe = c.indicatifFuturAnterieurJe.replace("amui", "amuï")
            c.indicatifFuturAnterieurTu = c.indicatifFuturAnterieurTu.replace("amui", "amuï")
            c.indicatifFuturAnterieurIl = c.indicatifFuturAnterieurIl.replace("amui", "amuï")
            c.indicatifFuturAnterieurNous = c.indicatifFuturAnterieurNous.replace("amui", "amuï")
            c.indicatifFuturAnterieurVous = c.indicatifFuturAnterieurVous.replace("amui", "amuï")
            c.indicatifFuturAnterieurIls = c.indicatifFuturAnterieurIls.replace("amui", "amuï")

            c.conditionnelPresentJe = c.conditionnelPresentJe.replace("amui", "amuï")
            c.conditionnelPresentTu = c.conditionnelPresentTu.replace("amui", "amuï")
            c.conditionnelPresentIl = c.conditionnelPresentIl.replace("amui", "amuï")
            c.conditionnelPresentNous = c.conditionnelPresentNous.replace("amui", "amuï")
            c.conditionnelPresentVous = c.conditionnelPresentVous.replace("amui", "amuï")
            c.conditionnelPresentIls = c.conditionnelPresentIls.replace("amui", "amuï")

            c.conditionnelPasseJe = c.conditionnelPasseJe.replace("amui", "amuï")
            c.conditionnelPasseTu = c.conditionnelPasseTu.replace("amui", "amuï")
            c.conditionnelPasseIl = c.conditionnelPasseIl.replace("amui", "amuï")
            c.conditionnelPasseNous = c.conditionnelPasseNous.replace("amui", "amuï")
            c.conditionnelPasseVous = c.conditionnelPasseVous.replace("amui", "amuï")
            c.conditionnelPasseIls = c.conditionnelPasseIls.replace("amui", "amuï")

            c.subjonctifPresentJe = c.subjonctifPresentJe.replace("amui", "amuï")
            c.subjonctifPresentTu = c.subjonctifPresentTu.replace("amui", "amuï")
            c.subjonctifPresentIl = c.subjonctifPresentIl.replace("amui", "amuï")
            c.subjonctifPresentNous = c.subjonctifPresentNous.replace("amui", "amuï")
            c.subjonctifPresentVous = c.subjonctifPresentVous.replace("amui", "amuï")
            c.subjonctifPresentIls = c.subjonctifPresentIls.replace("amui", "amuï")

            c.subjonctifPasseJe = c.subjonctifPasseJe.replace("amui", "amuï")
            c.subjonctifPasseTu = c.subjonctifPasseTu.replace("amui", "amuï")
            c.subjonctifPasseIl = c.subjonctifPasseIl.replace("amui", "amuï")
            c.subjonctifPasseNous = c.subjonctifPasseNous.replace("amui", "amuï")
            c.subjonctifPasseVous = c.subjonctifPasseVous.replace("amui", "amuï")
            c.subjonctifPasseIls = c.subjonctifPasseIls.replace("amui", "amuï")

            c.subjonctifImperfaitJe = c.subjonctifImperfaitJe.replace("amui", "amuï")
            c.subjonctifImperfaitTu = c.subjonctifImperfaitTu.replace("amui", "amuï")
            c.subjonctifImperfaitIl = c.subjonctifImperfaitIl.replace("amui", "amuï")
            c.subjonctifImperfaitNous = c.subjonctifImperfaitNous.replace("amui", "amuï")
            c.subjonctifImperfaitVous = c.subjonctifImperfaitVous.replace("amui", "amuï")
            c.subjonctifImperfaitIls = c.subjonctifImperfaitIls.replace("amui", "amuï")

            c.subjonctifPlusQueParfaitJe = c.subjonctifPlusQueParfaitJe.replace("amui", "amuï")
            c.subjonctifPlusQueParfaitTu = c.subjonctifPlusQueParfaitTu.replace("amui", "amuï")
            c.subjonctifPlusQueParfaitIl = c.subjonctifPlusQueParfaitIl.replace("amui", "amuï")
            c.subjonctifPlusQueParfaitNous = c.subjonctifPlusQueParfaitNous.replace("amui", "amuï")
            c.subjonctifPlusQueParfaitVous = c.subjonctifPlusQueParfaitVous.replace("amui", "amuï")
            c.subjonctifPlusQueParfaitIls = c.subjonctifPlusQueParfaitIls.replace("amui", "amuï")

            c.imperatifPresentTu = c.imperatifPresentTu.replace("amui", "amuï")
            c.imperatifPresentNous = c.imperatifPresentNous.replace("amui", "amuï")
            c.imperatifPresentVous = c.imperatifPresentVous.replace("amui", "amuï")
            c.imperatifPasseTu = c.imperatifPasseTu.replace("amui", "amuï")
            c.imperatifPasseNous = c.imperatifPasseNous.replace("amui", "amuï")
            c.imperatifPasseVous = c.imperatifPasseVous.replace("amui", "amuï")

            c.infinitivePasse = c.infinitivePasse.replace("amui", "amuï")
            c.participePresent = c.participePresent.replace("amui", "amuï")
            c.participePasse1 = c.participePasse1.replace("amui", "amuï")
            c.participePasse2 = c.participePasse2.replace("amui", "amuï")
            c.gerondifPresent = c.gerondifPresent.replace("amui", "amuï")
            c.gerondifPasse = c.gerondifPasse.replace("amui", "amuï")
        }
    }

    /**
     * Generates the verb radical based on the model.
     * @param infinitive String verb
     * @param modelR String radical of the model
     * @param id int model id
     * @param isPronominal boolean
     * @return list of radicals
     */
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
    }

    /**
     * Replaces the radicals with the ones from the verb.
     * @param c Conjugation
     * @param modelR  List of model radicals
     * @param verbR  List of verb radicals
     */
    private fun replaceRadicals(c:Conjugation, modelR:List<String>, verbR:List<String>) {

        c.infinitivePresent = verbName
        c.participePresent = replaceRadical(c.participePresent, modelR, verbR)
        c.gerondifPresent = replaceRadical(c.gerondifPresent, modelR, verbR)

        c.imperatifPresentTu = replaceRadical(c.imperatifPresentTu, modelR, verbR)
        c.imperatifPresentNous = replaceRadical(c.imperatifPresentNous, modelR, verbR)
        c.imperatifPresentVous = replaceRadical(c.imperatifPresentVous, modelR, verbR)

        c.indicatifPresentJe = replaceRadical(c.indicatifPresentJe, modelR, verbR)
        c.indicatifPresentTu = replaceRadical(c.indicatifPresentTu, modelR, verbR)
        c.indicatifPresentIl = replaceRadical(c.indicatifPresentIl, modelR, verbR)
        c.indicatifPresentNous = replaceRadical(c.indicatifPresentNous, modelR, verbR)
        c.indicatifPresentVous = replaceRadical(c.indicatifPresentVous, modelR, verbR)
        c.indicatifPresentIls = replaceRadical(c.indicatifPresentIls, modelR, verbR)

        c.indicatifImperfaitJe = replaceRadical(c.indicatifImperfaitJe, modelR, verbR)
        c.indicatifImperfaitTu = replaceRadical(c.indicatifImperfaitTu, modelR, verbR)
        c.indicatifImperfaitIl = replaceRadical(c.indicatifImperfaitIl, modelR, verbR)
        c.indicatifImperfaitNous = replaceRadical(c.indicatifImperfaitNous, modelR, verbR)
        c.indicatifImperfaitVous = replaceRadical(c.indicatifImperfaitVous, modelR, verbR)
        c.indicatifImperfaitIls = replaceRadical(c.indicatifImperfaitIls, modelR, verbR)

        c.indicatifPasseSimpleJe = replaceRadical(c.indicatifPasseSimpleJe, modelR, verbR)
        c.indicatifPasseSimpleTu = replaceRadical(c.indicatifPasseSimpleTu, modelR, verbR)
        c.indicatifPasseSimpleIl = replaceRadical(c.indicatifPasseSimpleIl, modelR, verbR)
        c.indicatifPasseSimpleNous = replaceRadical(c.indicatifPasseSimpleNous, modelR, verbR)
        c.indicatifPasseSimpleVous = replaceRadical(c.indicatifPasseSimpleVous, modelR, verbR)
        c.indicatifPasseSimpleIls = replaceRadical(c.indicatifPasseSimpleIls, modelR, verbR)

        c.indicatifFuturSimpleJe = replaceRadical(c.indicatifFuturSimpleJe, modelR, verbR)
        c.indicatifFuturSimpleTu = replaceRadical(c.indicatifFuturSimpleTu, modelR, verbR)
        c.indicatifFuturSimpleIl = replaceRadical(c.indicatifFuturSimpleIl, modelR, verbR)
        c.indicatifFuturSimpleNous = replaceRadical(c.indicatifFuturSimpleNous, modelR, verbR)
        c.indicatifFuturSimpleVous = replaceRadical(c.indicatifFuturSimpleVous, modelR, verbR)
        c.indicatifFuturSimpleIls = replaceRadical(c.indicatifFuturSimpleIls, modelR, verbR)

        c.conditionnelPresentJe = replaceRadical(c.conditionnelPresentJe, modelR, verbR)
        c.conditionnelPresentTu = replaceRadical(c.conditionnelPresentTu, modelR, verbR)
        c.conditionnelPresentIl = replaceRadical(c.conditionnelPresentIl, modelR, verbR)
        c.conditionnelPresentNous = replaceRadical(c.conditionnelPresentNous, modelR, verbR)
        c.conditionnelPresentVous = replaceRadical(c.conditionnelPresentVous, modelR, verbR)
        c.conditionnelPresentIls = replaceRadical(c.conditionnelPresentIls, modelR, verbR)

        c.subjonctifPresentJe = replaceRadical(c.subjonctifPresentJe, modelR, verbR)
        c.subjonctifPresentTu = replaceRadical(c.subjonctifPresentTu, modelR, verbR)
        c.subjonctifPresentIl = replaceRadical(c.subjonctifPresentIl, modelR, verbR)
        c.subjonctifPresentNous = replaceRadical(c.subjonctifPresentNous, modelR, verbR)
        c.subjonctifPresentVous = replaceRadical(c.subjonctifPresentVous, modelR, verbR)
        c.subjonctifPresentIls = replaceRadical(c.subjonctifPresentIls, modelR, verbR)

        c.subjonctifImperfaitJe = replaceRadical(c.subjonctifImperfaitJe, modelR, verbR)
        c.subjonctifImperfaitTu = replaceRadical(c.subjonctifImperfaitTu, modelR, verbR)
        c.subjonctifImperfaitIl = replaceRadical(c.subjonctifImperfaitIl, modelR, verbR)
        c.subjonctifImperfaitNous = replaceRadical(c.subjonctifImperfaitNous, modelR, verbR)
        c.subjonctifImperfaitVous = replaceRadical(c.subjonctifImperfaitVous, modelR, verbR)
        c.subjonctifImperfaitIls = replaceRadical(c.subjonctifImperfaitIls, modelR, verbR)


        // Calculate and replace participe passe only, instead of radicals for composed tenses
        var pp = c.participePasse1.split(" ")
        val old = if (!pp[0].contentEquals("-")) pp[0] else ""

        c.participePasse1 = replaceRadical(c.participePasse1, modelR, verbR)

        pp = c.participePasse1.split(" ")
        val new = if (!pp[0].contentEquals("-")) pp[0] else ""

        if (old.isNotEmpty() && new.isNotEmpty()) {
            replaceParticipePasse(c, old, new)
        }
    }

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

                // if it's just one form, if it's a double form (like Je pay / paye) continue
                if (!text.contains("/")) {
                    break
                }
            }
        }
        return newText
    }

    /**
     * Checks if the verb uses other auxiliar verb and replace it.
     * @param c Conjugation
     * @param isEtre boolean The verb uses the auxiliar être
     * @param isAvoir boolean The verb uses the auxiliar avoir
     */
    private fun reviewAuxiliar(c:Conjugation, isEtre:Boolean, isAvoir:Boolean) {

        if (!isEtre && !isAvoir) return

        val wordsAvoir = arrayOf("avoir", "ayant", "ayant",
                "aie", "ayons", "ayez", "ai", "as", "a", "avons", "avez", "ont", // IndicatifPasseCompose
                "avais", "avais", "avait", "avions", "aviez", "avaient", // IndicatifPlusQueParfait
                "eus", "eus", "eut", "eûmes", "eûtes", "eurent", // IndicatifPasseAnterieur
                "aurai", "auras", "aura", "aurons", "aurez", "auront", // IndicatifFuturAnterieur
                "aurais", "aurais", "aurait", "aurions", "auriez", "auraient", // ConditionnelPasse
                "aie", "aies", "ait", "ayons", "ayez", "aient", // SubjonctifPasse
                "eusse", "eusses", "eût", "eussions", "eussiez", "eussent")// SubjonctifPlusQueParfait
        val wordsEtre = arrayOf("être", "étant", "étant",
                "sois", "soyons", "soyez", "suis", "es", "est", "sommes", "êtes", "sont", // IndicatifPasseCompose
                "étais", "étais", "était", "étions", "étiez", "étaient", // IndicatifPlusQueParfait
                "fus", "fus", "fut", "fûmes", "fûtes", "furent", // IndicatifPasseAnterieur
                "serai", "seras", "sera", "serons", "serez", "seront", // IndicatifFuturAnterieur
                "serais", "serais", "serait", "serions", "seriez", "seraient", // ConditionnelPasse
                "sois", "sois", "soit", "soyons", "soyez", "soient", // SubjonctifPasse
                "fusse", "fusses", "fût", "fussions", "fussiez", "fussent")// SubjonctifPlusQueParfait
        val wordsEtreAvoir = arrayOf("être ou avoir", "étant ou ayant", "étant ou ayant",
                "sois ou aie", "soyons ou ayons", "soyez ou ayez",
                "suis ou ai", "es ou as", "est ou a", "sommes ou avons", "êtes ou avez", "sont ou ont", // IndicatifPasseCompose
                "étais ou avais", "étais ou avais", "était ou avait", "étions ou avions", "étiez ou aviez", "étaient ou avaient", // IndicatifPlusQueParfait
                "fus ou eus", "fus ou eus", "fut ou eut", "fûmes ou eûmes", "fûtes ou eûtes", "furent ou eurent", // IndicatifPasseAnterieur
                "serai ou aurai", "seras ou auras", "sera ou aura", "serons ou aurons", "serez ou aurez", "seront ou auront", // IndicatifFuturAnterieur
                "serais ou aurais", "serais ou aurais", "serait ou aurait", "serions ou aurions", "seriez ou auriez", "seraient ou auraient", // ConditionnelPasse
                "sois ou aie", "sois ou aies", "soit ou ait", "soyons ou ayons", "soyez ou ayez", "soient ou aient", // SubjonctifPasse
                "fusse ou eusse", "fusses ou eusses", "fût ou eût", "fussions ou eussions", "fussiez ou eussiez", "fussent ou eussent")// SubjonctifPlusQueParfait

        // change auxiliar verb
        if (isAvoir && isEtre) {
            if (c.infinitivePasse.contains("avoir")) {
                //  Like: sortir, renter
                replaceAuxiliar(c, wordsAvoir, wordsEtreAvoir)
            } else if (c.infinitivePasse.contains("être")) {
                replaceAuxiliar(c, wordsEtre, wordsEtreAvoir)
            }
        } else if (c.infinitivePasse.contains("avoir") && isEtre && !isAvoir) {
            //  Like: partir, mourir, s'ecrier
            replaceAuxiliar(c, wordsAvoir, wordsEtre)
        } else if (c.infinitivePasse.contains("être") && !isEtre && isAvoir) {
            replaceAuxiliar(c, wordsEtre, wordsAvoir)
        }
    }

    /**
     * Replaces a list of strings with another list.
     * Both list should refer to the same conjugation item in the same order.
     * @param c Conjugation
     * @param words string to replace
     * @param replaces replace string
     */
    private fun replaceAuxiliar(c:Conjugation, words:Array<String>, replaces:Array<String>) {
        // NOTE: Items to replace must come in the same order
        for (i in words.indices) {
            val word = words[i]
            val replace = replaces[i]

            when (i) {
                0 -> c.infinitivePasse = c.infinitivePasse.replaceFirst(word.toRegex(), replace)
                1 -> c.participePasse2 = c.participePasse2.replaceFirst(word.toRegex(), replace)
                2 -> c.gerondifPasse = c.gerondifPasse.replaceFirst(word.toRegex(), replace)
                3 -> c.imperatifPasseTu = c.imperatifPasseTu.replaceFirst(word.toRegex(), replace)
                4 -> c.imperatifPasseNous = c.imperatifPasseNous.replaceFirst(word.toRegex(), replace)
                5 -> c.imperatifPasseVous = c.imperatifPasseVous.replaceFirst(word.toRegex(), replace)

                6 -> c.indicatifPasseComposeJe = c.indicatifPasseComposeJe.replaceFirst(word.toRegex(), replace)
                7 -> c.indicatifPasseComposeTu = c.indicatifPasseComposeTu.replaceFirst(word.toRegex(), replace)
                8 -> c.indicatifPasseComposeIl = c.indicatifPasseComposeIl.replaceFirst(word.toRegex(), replace)
                9 -> c.indicatifPasseComposeNous = c.indicatifPasseComposeNous.replaceFirst(word.toRegex(), replace)
                10 -> c.indicatifPasseComposeVous = c.indicatifPasseComposeVous.replaceFirst(word.toRegex(), replace)
                11 -> c.indicatifPasseComposeIls = c.indicatifPasseComposeIls.replaceFirst(word.toRegex(), replace)

                12 -> c.indicatifPlusQueParfaitJe = c.indicatifPlusQueParfaitJe.replaceFirst(word.toRegex(), replace)
                13 -> c.indicatifPlusQueParfaitTu = c.indicatifPlusQueParfaitTu.replaceFirst(word.toRegex(), replace)
                14 -> c.indicatifPlusQueParfaitIl = c.indicatifPlusQueParfaitIl.replaceFirst(word.toRegex(), replace)
                15 -> c.indicatifPlusQueParfaitNous = c.indicatifPlusQueParfaitNous.replaceFirst(word.toRegex(), replace)
                16 -> c.indicatifPlusQueParfaitVous = c.indicatifPlusQueParfaitVous.replaceFirst(word.toRegex(), replace)
                17 -> c.indicatifPlusQueParfaitIls = c.indicatifPlusQueParfaitIls.replaceFirst(word.toRegex(), replace)

                18 -> c.indicatifPasseAnterieurJe = c.indicatifPasseAnterieurJe.replaceFirst(word.toRegex(), replace)
                19 -> c.indicatifPasseAnterieurTu = c.indicatifPasseAnterieurTu.replaceFirst(word.toRegex(), replace)
                20 -> c.indicatifPasseAnterieurIl = c.indicatifPasseAnterieurIl.replaceFirst(word.toRegex(), replace)
                21 -> c.indicatifPasseAnterieurNous = c.indicatifPasseAnterieurNous.replaceFirst(word.toRegex(), replace)
                22 -> c.indicatifPasseAnterieurVous = c.indicatifPasseAnterieurVous.replaceFirst(word.toRegex(), replace)
                23 -> c.indicatifPasseAnterieurIls = c.indicatifPasseAnterieurIls.replaceFirst(word.toRegex(), replace)

                24 -> c.indicatifFuturAnterieurJe = c.indicatifFuturAnterieurJe.replaceFirst(word.toRegex(), replace)
                25 -> c.indicatifFuturAnterieurTu = c.indicatifFuturAnterieurTu.replaceFirst(word.toRegex(), replace)
                26 -> c.indicatifFuturAnterieurIl = c.indicatifFuturAnterieurIl.replaceFirst(word.toRegex(), replace)
                27 -> c.indicatifFuturAnterieurNous = c.indicatifFuturAnterieurNous.replaceFirst(word.toRegex(), replace)
                28 -> c.indicatifFuturAnterieurVous = c.indicatifFuturAnterieurVous.replaceFirst(word.toRegex(), replace)
                29 -> c.indicatifFuturAnterieurIls = c.indicatifFuturAnterieurIls.replaceFirst(word.toRegex(), replace)

                30 -> c.conditionnelPasseJe = c.conditionnelPasseJe.replaceFirst(word.toRegex(), replace)
                31 -> c.conditionnelPasseTu = c.conditionnelPasseTu.replaceFirst(word.toRegex(), replace)
                32 -> c.conditionnelPasseIl = c.conditionnelPasseIl.replaceFirst(word.toRegex(), replace)
                33 -> c.conditionnelPasseNous = c.conditionnelPasseNous.replaceFirst(word.toRegex(), replace)
                34 -> c.conditionnelPasseVous = c.conditionnelPasseVous.replaceFirst(word.toRegex(), replace)
                35 -> c.conditionnelPasseIls = c.conditionnelPasseIls.replaceFirst(word.toRegex(), replace)

                36 -> c.subjonctifPasseJe = c.subjonctifPasseJe.replaceFirst(word.toRegex(), replace)
                37 -> c.subjonctifPasseTu = c.subjonctifPasseTu.replaceFirst(word.toRegex(), replace)
                38 -> c.subjonctifPasseIl = c.subjonctifPasseIl.replaceFirst(word.toRegex(), replace)
                39 -> c.subjonctifPasseNous = c.subjonctifPasseNous.replaceFirst(word.toRegex(), replace)
                40 -> c.subjonctifPasseVous = c.subjonctifPasseVous.replaceFirst(word.toRegex(), replace)
                41 -> c.subjonctifPasseIls = c.subjonctifPasseIls.replaceFirst(word.toRegex(), replace)

                42 -> c.subjonctifPlusQueParfaitJe = c.subjonctifPlusQueParfaitJe.replaceFirst(word.toRegex(), replace)
                43 -> c.subjonctifPlusQueParfaitTu = c.subjonctifPlusQueParfaitTu.replaceFirst(word.toRegex(), replace)
                44 -> c.subjonctifPlusQueParfaitIl = c.subjonctifPlusQueParfaitIl.replaceFirst(word.toRegex(), replace)
                45 -> c.subjonctifPlusQueParfaitNous = c.subjonctifPlusQueParfaitNous.replaceFirst(word.toRegex(), replace)
                46 -> c.subjonctifPlusQueParfaitVous = c.subjonctifPlusQueParfaitVous.replaceFirst(word.toRegex(), replace)
                47 -> c.subjonctifPlusQueParfaitIls = c.subjonctifPlusQueParfaitIls.replaceFirst(word.toRegex(), replace)
            }
        }
    }

    /**
     * Marks the conjugations as "-"
     * @param c Conjugation
     */
    private fun ignoreConjugations(c:Conjugation) {
        ignoreIndicatifPresent(c)
        ignoreIndicatifPasseCompose(c)
        ignoreIndicatifImperfait(c)
        ignoreIndicatifPlusQueParfait(c)
        ignoreIndicatifPasseSimple(c)
        ignoreIndicatifPasseAnterieur(c)
        ignoreIndicatifFuturSimple(c)
        ignoreIndicatifFuturAnterieur(c)
        ignoreConditionnelPresent(c)
        ignoreConditionnelPasse(c)
        ignoreSubjonctifPresent(c)
        ignoreSubjonctifPasse(c)
        ignoreSubjonctifImperfait(c)
        ignoreSubjonctifPlusQueParfait(c)

        ignoreImperatif(c)
        c.infinitivePasse = "-"
        c.participePresent = "-"
        c.participePasse1 = "-"
        c.participePasse2 = "-"
        c.gerondifPresent = "-"
        c.gerondifPasse = "-"
    }

    private fun ignoreIndicatifPresent(c:Conjugation) {
        c.indicatifPresentJe = "-"
        c.indicatifPresentTu = "-"
        c.indicatifPresentIl = "-"
        c.indicatifPresentNous = "-"
        c.indicatifPresentVous = "-"
        c.indicatifPresentIls = "-"
    }

    private fun ignoreIndicatifPresentNousVousIls(c:Conjugation) {
        c.indicatifPresentNous = "-"
        c.indicatifPresentVous = "-"
        c.indicatifPresentIls = "-"
    }

    private fun ignoreIndicatifPasseCompose(c:Conjugation) {
        c.indicatifPasseComposeJe = "-"
        c.indicatifPasseComposeTu = "-"
        c.indicatifPasseComposeIl = "-"
        c.indicatifPasseComposeNous = "-"
        c.indicatifPasseComposeVous = "-"
        c.indicatifPasseComposeIls = "-"
    }

    private fun ignoreIndicatifImperfait(c:Conjugation) {
        c.indicatifImperfaitJe = "-"
        c.indicatifImperfaitTu = "-"
        c.indicatifImperfaitIl = "-"
        c.indicatifImperfaitNous = "-"
        c.indicatifImperfaitVous = "-"
        c.indicatifImperfaitIls = "-"
    }

    private fun ignoreIndicatifPlusQueParfait(c:Conjugation) {
        c.indicatifPlusQueParfaitJe = "-"
        c.indicatifPlusQueParfaitTu = "-"
        c.indicatifPlusQueParfaitIl = "-"
        c.indicatifPlusQueParfaitNous = "-"
        c.indicatifPlusQueParfaitVous = "-"
        c.indicatifPlusQueParfaitIls = "-"
    }

    private fun ignoreIndicatifPasseSimple(c:Conjugation) {
        c.indicatifPasseSimpleJe = "-"
        c.indicatifPasseSimpleTu = "-"
        c.indicatifPasseSimpleIl = "-"
        c.indicatifPasseSimpleNous = "-"
        c.indicatifPasseSimpleVous = "-"
        c.indicatifPasseSimpleIls = "-"
    }

    private fun ignoreIndicatifPasseAnterieur(c:Conjugation) {
        c.indicatifPasseAnterieurJe = "-"
        c.indicatifPasseAnterieurTu = "-"
        c.indicatifPasseAnterieurIl = "-"
        c.indicatifPasseAnterieurNous = "-"
        c.indicatifPasseAnterieurVous = "-"
        c.indicatifPasseAnterieurIls = "-"
    }

    private fun ignoreIndicatifFuturSimple(c:Conjugation) {
        c.indicatifFuturSimpleJe = "-"
        c.indicatifFuturSimpleTu = "-"
        c.indicatifFuturSimpleIl = "-"
        c.indicatifFuturSimpleNous = "-"
        c.indicatifFuturSimpleVous = "-"
        c.indicatifFuturSimpleIls = "-"
    }

    private fun ignoreIndicatifFuturAnterieur(c:Conjugation) {
        c.indicatifFuturAnterieurJe = "-"
        c.indicatifFuturAnterieurTu = "-"
        c.indicatifFuturAnterieurIl = "-"
        c.indicatifFuturAnterieurNous = "-"
        c.indicatifFuturAnterieurVous = "-"
        c.indicatifFuturAnterieurIls = "-"
    }

    private fun ignoreConditionnelPresent(c:Conjugation) {
        c.conditionnelPresentJe = "-"
        c.conditionnelPresentTu = "-"
        c.conditionnelPresentIl = "-"
        c.conditionnelPresentNous = "-"
        c.conditionnelPresentVous = "-"
        c.conditionnelPresentIls = "-"
    }

    private fun ignoreConditionnelPasse(c:Conjugation) {
        c.conditionnelPasseJe = "-"
        c.conditionnelPasseTu = "-"
        c.conditionnelPasseIl = "-"
        c.conditionnelPasseNous = "-"
        c.conditionnelPasseVous = "-"
        c.conditionnelPasseIls = "-"
    }

    private fun ignoreSubjonctifPresent(c:Conjugation) {
        c.subjonctifPresentJe = "-"
        c.subjonctifPresentTu = "-"
        c.subjonctifPresentIl = "-"
        c.subjonctifPresentNous = "-"
        c.subjonctifPresentVous = "-"
        c.subjonctifPresentIls = "-"
    }

    private fun ignoreSubjonctifPasse(c:Conjugation) {
        c.subjonctifPasseJe = "-"
        c.subjonctifPasseTu = "-"
        c.subjonctifPasseIl = "-"
        c.subjonctifPasseNous = "-"
        c.subjonctifPasseVous = "-"
        c.subjonctifPasseIls = "-"
    }

    private fun ignoreSubjonctifImperfait(c:Conjugation) {
        c.subjonctifImperfaitJe = "-"
        c.subjonctifImperfaitTu = "-"
        c.subjonctifImperfaitIl = "-"
        c.subjonctifImperfaitNous = "-"
        c.subjonctifImperfaitVous = "-"
        c.subjonctifImperfaitIls = "-"
    }

    private fun ignoreSubjonctifPlusQueParfait(c:Conjugation) {
        c.subjonctifPlusQueParfaitJe = "-"
        c.subjonctifPlusQueParfaitTu = "-"
        c.subjonctifPlusQueParfaitIl = "-"
        c.subjonctifPlusQueParfaitNous = "-"
        c.subjonctifPlusQueParfaitVous = "-"
        c.subjonctifPlusQueParfaitIls = "-"
    }

    private fun ignoreImperatif(c:Conjugation) {
        c.imperatifPresentTu = "-"
        c.imperatifPresentNous = "-"
        c.imperatifPresentVous = "-"
        c.imperatifPasseTu = "-"
        c.imperatifPasseNous = "-"
        c.imperatifPasseVous = "-"
    }

    private fun ignoreImperatifNousVous(c:Conjugation) {
        c.imperatifPresentNous = "-"
        c.imperatifPresentVous = "-"
    }

    /**
     * Marks the conjugations as "-" for all persons except il
     * @param c Conjugation
     */
    private fun ignoreAllPersonsExceptIl(c:Conjugation) {
        c.indicatifPresentJe = "-"
        c.indicatifPresentTu = "-"
        c.indicatifPresentNous = "-"
        c.indicatifPresentVous = "-"
        c.indicatifPresentIls = "-"

        c.indicatifPasseComposeJe = "-"
        c.indicatifPasseComposeTu = "-"
        c.indicatifPasseComposeNous = "-"
        c.indicatifPasseComposeVous = "-"
        c.indicatifPasseComposeIls = "-"

        c.indicatifImperfaitJe = "-"
        c.indicatifImperfaitTu = "-"
        c.indicatifImperfaitNous = "-"
        c.indicatifImperfaitVous = "-"
        c.indicatifImperfaitIls = "-"

        c.indicatifPlusQueParfaitJe = "-"
        c.indicatifPlusQueParfaitTu = "-"
        c.indicatifPlusQueParfaitNous = "-"
        c.indicatifPlusQueParfaitVous = "-"
        c.indicatifPlusQueParfaitIls = "-"

        c.indicatifPasseSimpleJe = "-"
        c.indicatifPasseSimpleTu = "-"
        c.indicatifPasseSimpleNous = "-"
        c.indicatifPasseSimpleVous = "-"
        c.indicatifPasseSimpleIls = "-"

        c.indicatifPasseAnterieurJe = "-"
        c.indicatifPasseAnterieurTu = "-"
        c.indicatifPasseAnterieurNous = "-"
        c.indicatifPasseAnterieurVous = "-"
        c.indicatifPasseAnterieurIls = "-"

        c.indicatifFuturSimpleJe = "-"
        c.indicatifFuturSimpleTu = "-"
        c.indicatifFuturSimpleNous = "-"
        c.indicatifFuturSimpleVous = "-"
        c.indicatifFuturSimpleIls = "-"

        c.indicatifFuturAnterieurJe = "-"
        c.indicatifFuturAnterieurTu = "-"
        c.indicatifFuturAnterieurNous = "-"
        c.indicatifFuturAnterieurVous = "-"
        c.indicatifFuturAnterieurIls = "-"

        c.conditionnelPresentJe = "-"
        c.conditionnelPresentTu = "-"
        c.conditionnelPresentNous = "-"
        c.conditionnelPresentVous = "-"
        c.conditionnelPresentIls = "-"

        c.conditionnelPasseJe = "-"
        c.conditionnelPasseTu = "-"
        c.conditionnelPasseNous = "-"
        c.conditionnelPasseVous = "-"
        c.conditionnelPasseIls = "-"

        c.subjonctifPresentJe = "-"
        c.subjonctifPresentTu = "-"
        c.subjonctifPresentNous = "-"
        c.subjonctifPresentVous = "-"
        c.subjonctifPresentIls = "-"

        c.subjonctifPasseJe = "-"
        c.subjonctifPasseTu = "-"
        c.subjonctifPasseNous = "-"
        c.subjonctifPasseVous = "-"
        c.subjonctifPasseIls = "-"

        c.subjonctifImperfaitJe = "-"
        c.subjonctifImperfaitTu = "-"
        c.subjonctifImperfaitNous = "-"
        c.subjonctifImperfaitVous = "-"
        c.subjonctifImperfaitIls = "-"

        c.subjonctifPlusQueParfaitJe = "-"
        c.subjonctifPlusQueParfaitTu = "-"
        c.subjonctifPlusQueParfaitNous = "-"
        c.subjonctifPlusQueParfaitVous = "-"
        c.subjonctifPlusQueParfaitIls = "-"

        ignoreImperatif(c)
    }

    /**
     * Marks the conjugations as "-" for all persons except il and ils
     * @param c Conjugation
     */
    private fun ignoreAllPersonsExceptIlAndIls(c:Conjugation) {
        c.indicatifPresentJe = "-"
        c.indicatifPresentTu = "-"
        c.indicatifPresentNous = "-"
        c.indicatifPresentVous = "-"

        c.indicatifPasseComposeJe = "-"
        c.indicatifPasseComposeTu = "-"
        c.indicatifPasseComposeNous = "-"
        c.indicatifPasseComposeVous = "-"

        c.indicatifImperfaitJe = "-"
        c.indicatifImperfaitTu = "-"
        c.indicatifImperfaitNous = "-"
        c.indicatifImperfaitVous = "-"

        c.indicatifPlusQueParfaitJe = "-"
        c.indicatifPlusQueParfaitTu = "-"
        c.indicatifPlusQueParfaitNous = "-"
        c.indicatifPlusQueParfaitVous = "-"

        c.indicatifPasseSimpleJe = "-"
        c.indicatifPasseSimpleTu = "-"
        c.indicatifPasseSimpleNous = "-"
        c.indicatifPasseSimpleVous = "-"

        c.indicatifPasseAnterieurJe = "-"
        c.indicatifPasseAnterieurTu = "-"
        c.indicatifPasseAnterieurNous = "-"
        c.indicatifPasseAnterieurVous = "-"

        c.indicatifFuturSimpleJe = "-"
        c.indicatifFuturSimpleTu = "-"
        c.indicatifFuturSimpleNous = "-"
        c.indicatifFuturSimpleVous = "-"

        c.indicatifFuturAnterieurJe = "-"
        c.indicatifFuturAnterieurTu = "-"
        c.indicatifFuturAnterieurNous = "-"
        c.indicatifFuturAnterieurVous = "-"

        c.conditionnelPresentJe = "-"
        c.conditionnelPresentTu = "-"
        c.conditionnelPresentNous = "-"
        c.conditionnelPresentVous = "-"

        c.conditionnelPasseJe = "-"
        c.conditionnelPasseTu = "-"
        c.conditionnelPasseNous = "-"
        c.conditionnelPasseVous = "-"

        c.subjonctifPresentJe = "-"
        c.subjonctifPresentTu = "-"
        c.subjonctifPresentNous = "-"
        c.subjonctifPresentVous = "-"

        c.subjonctifPasseJe = "-"
        c.subjonctifPasseTu = "-"
        c.subjonctifPasseNous = "-"
        c.subjonctifPasseVous = "-"

        c.subjonctifImperfaitJe = "-"
        c.subjonctifImperfaitTu = "-"
        c.subjonctifImperfaitNous = "-"
        c.subjonctifImperfaitVous = "-"

        c.subjonctifPlusQueParfaitJe = "-"
        c.subjonctifPlusQueParfaitTu = "-"
        c.subjonctifPlusQueParfaitNous = "-"
        c.subjonctifPlusQueParfaitVous = "-"

        ignoreImperatif(c)
    }

    private fun ignoreAllPersonsExceptIlAndIlsIndicatif(c:Conjugation) {
        c.indicatifPresentJe = "-"
        c.indicatifPresentTu = "-"
        c.indicatifPresentNous = "-"
        c.indicatifPresentVous = "-"

        ignoreIndicatifPasseCompose(c)

        c.indicatifImperfaitJe = "-"
        c.indicatifImperfaitTu = "-"
        c.indicatifImperfaitNous = "-"
        c.indicatifImperfaitVous = "-"

        ignoreIndicatifPlusQueParfait(c)

        c.indicatifPasseSimpleJe = "-"
        c.indicatifPasseSimpleTu = "-"
        c.indicatifPasseSimpleNous = "-"
        c.indicatifPasseSimpleVous = "-"

        ignoreIndicatifPasseAnterieur(c)

        c.indicatifFuturSimpleJe = "-"
        c.indicatifFuturSimpleTu = "-"
        c.indicatifFuturSimpleNous = "-"
        c.indicatifFuturSimpleVous = "-"

        ignoreIndicatifFuturAnterieur(c)

        c.conditionnelPresentJe = "-"
        c.conditionnelPresentTu = "-"
        c.conditionnelPresentNous = "-"
        c.conditionnelPresentVous = "-"

        ignoreConditionnelPasse(c)
        ignoreSubjonctifPresent(c)
        ignoreSubjonctifPasse(c)
        ignoreSubjonctifImperfait(c)
        ignoreSubjonctifPlusQueParfait(c)
        ignoreImperatif(c)
    }

    /**
     * Ads the pronoms
     * @param c Conjugation
     */
    private fun addPronoms(c:Conjugation) {
        // Add pronoms
        // TODO: Show pronoms in different color
        addPronomsIndicatifPresent(c)
        addPronomsIndicatifPasseCompose(c)
        addPronomsIndicatifImperfait(c)
        addPronomsIndicatifPlusQueParfait(c)
        addPronomsIndicatifPasseSimple(c)
        addPronomsIndicatifPasseAnterieur(c)
        addPronomsIndicatifFuturSimple(c)
        addPronomsIndicatifFuturAnterieur(c)
        addPronomsConditionnelPresent(c)
        addPronomsConditionnelPasse(c)
        addPronomsSubjonctifPresent(c)
        addPronomsSubjonctifPasse(c)
        addPronomsSubjonctifImperfait(c)
        addPronomsSubjonctifPlusQueParfait(c)
    }

    private fun addPronomsSubjonctifPlusQueParfait(c:Conjugation) {
        val text:String = c.subjonctifPlusQueParfaitJe
        if (!text.contentEquals("-")) {
            c.subjonctifPlusQueParfaitJe = if (ActivityUtils.useApostrophe(text)) QUE + JEA + text else QUE + JE + text
        }
        if (!c.subjonctifPlusQueParfaitTu.contentEquals("-")) {
            c.subjonctifPlusQueParfaitTu = QUE + TU + c.subjonctifPlusQueParfaitTu
        }
        if (!c.subjonctifPlusQueParfaitIl.contentEquals("-")) {
            c.subjonctifPlusQueParfaitIl = QUEA + IL + c.subjonctifPlusQueParfaitIl
        }
        if (!c.subjonctifPlusQueParfaitNous.contentEquals("-")) {
            c.subjonctifPlusQueParfaitNous = QUE + NOUS + c.subjonctifPlusQueParfaitNous
        }
        if (!c.subjonctifPlusQueParfaitVous.contentEquals("-")) {
            c.subjonctifPlusQueParfaitVous = QUE + VOUS + c.subjonctifPlusQueParfaitVous
        }
        if (!c.subjonctifPlusQueParfaitIls.contentEquals("-")) {
            c.subjonctifPlusQueParfaitIls = QUEA + ILS + c.subjonctifPlusQueParfaitIls
        }
    }

    private fun addPronomsSubjonctifImperfait(c:Conjugation) {
        val text:String = c.subjonctifImperfaitJe
        if (!text.contentEquals("-")) {
            c.subjonctifImperfaitJe = if (ActivityUtils.useApostrophe(text)) QUE + JEA + text else QUE + JE + text
        }
        if (!c.subjonctifImperfaitTu.contentEquals("-")) {
            c.subjonctifImperfaitTu = QUE + TU + c.subjonctifImperfaitTu
        }
        if (!c.subjonctifImperfaitIl.contentEquals("-")) {
            c.subjonctifImperfaitIl = QUEA + IL + c.subjonctifImperfaitIl
        }
        if (!c.subjonctifImperfaitNous.contentEquals("-")) {
            c.subjonctifImperfaitNous = QUE + NOUS + c.subjonctifImperfaitNous
        }
        if (!c.subjonctifImperfaitVous.contentEquals("-")) {
            c.subjonctifImperfaitVous = QUE + VOUS + c.subjonctifImperfaitVous
        }
        if (!c.subjonctifImperfaitIls.contentEquals("-")) {
            c.subjonctifImperfaitIls = QUEA + ILS + c.subjonctifImperfaitIls
        }
    }

    private fun addPronomsSubjonctifPasse(c:Conjugation) {
        val text:String = c.subjonctifPasseJe
        if (!text.contentEquals("-")) {
            c.subjonctifPasseJe = if (ActivityUtils.useApostrophe(text)) QUE + JEA + text else QUE + JE + text
        }
        if (!c.subjonctifPasseTu.contentEquals("-")) {
            c.subjonctifPasseTu = QUE + TU + c.subjonctifPasseTu
        }
        if (!c.subjonctifPasseIl.contentEquals("-")) {
            c.subjonctifPasseIl = QUEA + IL + c.subjonctifPasseIl
        }
        if (!c.subjonctifPasseNous.contentEquals("-")) {
            c.subjonctifPasseNous = QUE + NOUS + c.subjonctifPasseNous
        }
        if (!c.subjonctifPasseVous.contentEquals("-")) {
            c.subjonctifPasseVous = QUE + VOUS + c.subjonctifPasseVous
        }
        if (!c.subjonctifPasseIls.contentEquals("-")) {
            c.subjonctifPasseIls = QUEA + ILS + c.subjonctifPasseIls
        }
    }

    private fun addPronomsSubjonctifPresent(c:Conjugation) {
        val text:String = c.subjonctifPresentJe
        if (!text.contentEquals("-")) {
            c.subjonctifPresentJe = if (ActivityUtils.useApostrophe(text)) QUE + JEA + text else QUE + JE + text
        }
        if (!c.subjonctifPresentTu.contentEquals("-")) {
            c.subjonctifPresentTu = QUE + TU + c.subjonctifPresentTu
        }
        if (!c.subjonctifPresentIl.contentEquals("-")) {
            c.subjonctifPresentIl = QUEA + IL + c.subjonctifPresentIl
        }
        if (!c.subjonctifPresentNous.contentEquals("-")) {
            c.subjonctifPresentNous = QUE + NOUS + c.subjonctifPresentNous
        }
        if (!c.subjonctifPresentVous.contentEquals("-")) {
            c.subjonctifPresentVous = QUE + VOUS + c.subjonctifPresentVous
        }
        if (!c.subjonctifPresentIls.contentEquals("-")) {
            c.subjonctifPresentIls = QUEA + ILS + c.subjonctifPresentIls
        }
    }

    private fun addPronomsConditionnelPasse(c: Conjugation) {
        val text: String = c.conditionnelPasseJe
        if (!text.contentEquals("-")) {
            c.conditionnelPasseJe = if (ActivityUtils.useApostrophe(text)) JEA + text else JE + text
        }
        if (!c.conditionnelPasseTu.contentEquals("-")) {
            c.conditionnelPasseTu = TU + c.conditionnelPasseTu
        }
        if (!c.conditionnelPasseIl.contentEquals("-")) {
            c.conditionnelPasseIl = IL + c.conditionnelPasseIl
        }
        if (!c.conditionnelPasseNous.contentEquals("-")) {
            c.conditionnelPasseNous = NOUS + c.conditionnelPasseNous
        }
        if (!c.conditionnelPasseVous.contentEquals("-")) {
            c.conditionnelPasseVous = VOUS + c.conditionnelPasseVous
        }
        if (!c.conditionnelPasseIls.contentEquals("-")) {
            c.conditionnelPasseIls = ILS + c.conditionnelPasseIls
        }
    }

    private fun addPronomsConditionnelPresent(c: Conjugation) {
        val text: String = c.conditionnelPresentJe
        if (!text.contentEquals("-")) {
            c.conditionnelPresentJe = if (ActivityUtils.useApostrophe(text)) JEA + text else JE + text
        }
        if (!c.conditionnelPresentTu.contentEquals("-")) {
            c.conditionnelPresentTu = TU + c.conditionnelPresentTu
        }
        if (!c.conditionnelPresentIl.contentEquals("-")) {
            c.conditionnelPresentIl = IL + c.conditionnelPresentIl
        }
        if (!c.conditionnelPresentNous.contentEquals("-")) {
            c.conditionnelPresentNous = NOUS + c.conditionnelPresentNous
        }
        if (!c.conditionnelPresentVous.contentEquals("-")) {
            c.conditionnelPresentVous = VOUS + c.conditionnelPresentVous
        }
        if (!c.conditionnelPresentIls.contentEquals("-")) {
            c.conditionnelPresentIls = ILS + c.conditionnelPresentIls
        }
    }

    private fun addPronomsIndicatifFuturAnterieur(c: Conjugation) {
        val text: String = c.indicatifFuturAnterieurJe
        if (!text.contentEquals("-")) {
            c.indicatifFuturAnterieurJe = if (ActivityUtils.useApostrophe(text)) JEA + text else JE + text
        }
        if (!c.indicatifFuturAnterieurTu.contentEquals("-")) {
            c.indicatifFuturAnterieurTu = TU + c.indicatifFuturAnterieurTu
        }
        if (!c.indicatifFuturAnterieurIl.contentEquals("-")) {
            c.indicatifFuturAnterieurIl = IL + c.indicatifFuturAnterieurIl
        }
        if (!c.indicatifFuturAnterieurNous.contentEquals("-")) {
            c.indicatifFuturAnterieurNous = NOUS + c.indicatifFuturAnterieurNous
        }
        if (!c.indicatifFuturAnterieurVous.contentEquals("-")) {
            c.indicatifFuturAnterieurVous = VOUS + c.indicatifFuturAnterieurVous
        }
        if (!c.indicatifFuturAnterieurIls.contentEquals("-")) {
            c.indicatifFuturAnterieurIls = ILS + c.indicatifFuturAnterieurIls
        }
    }

    private fun addPronomsIndicatifFuturSimple(c: Conjugation) {
        val text: String = c.indicatifFuturSimpleJe
        if (!text.contentEquals("-")) {
            c.indicatifFuturSimpleJe = if (ActivityUtils.useApostrophe(text)) JEA + text else JE + text
        }
        if (!c.indicatifFuturSimpleTu.contentEquals("-")) {
            c.indicatifFuturSimpleTu = TU + c.indicatifFuturSimpleTu
        }
        if (!c.indicatifFuturSimpleIl.contentEquals("-")) {
            c.indicatifFuturSimpleIl = IL + c.indicatifFuturSimpleIl
        }
        if (!c.indicatifFuturSimpleNous.contentEquals("-")) {
            c.indicatifFuturSimpleNous = NOUS + c.indicatifFuturSimpleNous
        }
        if (!c.indicatifFuturSimpleVous.contentEquals("-")) {
            c.indicatifFuturSimpleVous = VOUS + c.indicatifFuturSimpleVous
        }
        if (!c.indicatifFuturSimpleIls.contentEquals("-")) {
            c.indicatifFuturSimpleIls = ILS + c.indicatifFuturSimpleIls
        }
    }

    private fun addPronomsIndicatifPasseAnterieur(c: Conjugation) {
        val text: String = c.indicatifPasseAnterieurJe
        if (!text.contentEquals("-")) {
            c.indicatifPasseAnterieurJe = if (ActivityUtils.useApostrophe(text)) JEA + text else JE + text
        }
        if (!c.indicatifPasseAnterieurTu.contentEquals("-")) {
            c.indicatifPasseAnterieurTu = TU + c.indicatifPasseAnterieurTu
        }
        if (!c.indicatifPasseAnterieurIl.contentEquals("-")) {
            c.indicatifPasseAnterieurIl = IL + c.indicatifPasseAnterieurIl
        }
        if (!c.indicatifPasseAnterieurNous.contentEquals("-")) {
            c.indicatifPasseAnterieurNous = NOUS + c.indicatifPasseAnterieurNous
        }
        if (!c.indicatifPasseAnterieurVous.contentEquals("-")) {
            c.indicatifPasseAnterieurVous = VOUS + c.indicatifPasseAnterieurVous
        }
        if (!c.indicatifPasseAnterieurIls.contentEquals("-")) {
            c.indicatifPasseAnterieurIls = ILS + c.indicatifPasseAnterieurIls
        }
    }

    private fun addPronomsIndicatifPasseSimple(c: Conjugation) {
        val text: String = c.indicatifPasseSimpleJe
        if (!text.contentEquals("-")) {
            c.indicatifPasseSimpleJe = if (ActivityUtils.useApostrophe(text)) JEA + text else JE + text
        }
        if (!c.indicatifPasseSimpleTu.contentEquals("-")) {
            c.indicatifPasseSimpleTu = TU + c.indicatifPasseSimpleTu
        }
        if (!c.indicatifPasseSimpleIl.contentEquals("-")) {
            c.indicatifPasseSimpleIl = IL + c.indicatifPasseSimpleIl
        }
        if (!c.indicatifPasseSimpleNous.contentEquals("-")) {
            c.indicatifPasseSimpleNous = NOUS + c.indicatifPasseSimpleNous
        }
        if (!c.indicatifPasseSimpleVous.contentEquals("-")) {
            c.indicatifPasseSimpleVous = VOUS + c.indicatifPasseSimpleVous
        }
        if (!c.indicatifPasseSimpleIls.contentEquals("-")) {
            c.indicatifPasseSimpleIls = ILS + c.indicatifPasseSimpleIls
        }
    }

    private fun addPronomsIndicatifPlusQueParfait(c: Conjugation) {
        val text: String = c.indicatifPlusQueParfaitJe
        if (!text.contentEquals("-")) {
            c.indicatifPlusQueParfaitJe = if (ActivityUtils.useApostrophe(text)) JEA + text else JE + text
        }
        if (!c.indicatifPlusQueParfaitTu.contentEquals("-")) {
            c.indicatifPlusQueParfaitTu = TU + c.indicatifPlusQueParfaitTu
        }
        if (!c.indicatifPlusQueParfaitIl.contentEquals("-")) {
            c.indicatifPlusQueParfaitIl = IL + c.indicatifPlusQueParfaitIl
        }
        if (!c.indicatifPlusQueParfaitNous.contentEquals("-")) {
            c.indicatifPlusQueParfaitNous = NOUS + c.indicatifPlusQueParfaitNous
        }
        if (!c.indicatifPlusQueParfaitVous.contentEquals("-")) {
            c.indicatifPlusQueParfaitVous = VOUS + c.indicatifPlusQueParfaitVous
        }
        if (!c.indicatifPlusQueParfaitIls.contentEquals("-")) {
            c.indicatifPlusQueParfaitIls = ILS + c.indicatifPlusQueParfaitIls
        }
    }

    private fun addPronomsIndicatifImperfait(c: Conjugation) {
        val text: String = c.indicatifImperfaitJe
        if (!text.contentEquals("-")) {
            c.indicatifImperfaitJe = if (ActivityUtils.useApostrophe(text)) JEA + text else JE + text
        }
        if (!c.indicatifImperfaitTu.contentEquals("-")) {
            c.indicatifImperfaitTu = TU + c.indicatifImperfaitTu
        }
        if (!c.indicatifImperfaitIl.contentEquals("-")) {
            c.indicatifImperfaitIl = IL + c.indicatifImperfaitIl
        }
        if (!c.indicatifImperfaitNous.contentEquals("-")) {
            c.indicatifImperfaitNous = NOUS + c.indicatifImperfaitNous
        }
        if (!c.indicatifImperfaitVous.contentEquals("-")) {
            c.indicatifImperfaitVous = VOUS + c.indicatifImperfaitVous
        }
        if (!c.indicatifImperfaitIls.contentEquals("-")) {
            c.indicatifImperfaitIls = ILS + c.indicatifImperfaitIls
        }
    }

    private fun addPronomsIndicatifPasseCompose(c: Conjugation) {
        val text: String = c.indicatifPasseComposeJe
        if (!text.contentEquals("-")) {
            c.indicatifPasseComposeJe = if (ActivityUtils.useApostrophe(text)) JEA + text else JE + text
        }
        if (!c.indicatifPasseComposeTu.contentEquals("-")) {
            c.indicatifPasseComposeTu = TU + c.indicatifPasseComposeTu
        }
        if (!c.indicatifPasseComposeIl.contentEquals("-")) {
            c.indicatifPasseComposeIl = IL + c.indicatifPasseComposeIl
        }
        if (!c.indicatifPasseComposeNous.contentEquals("-")) {
            c.indicatifPasseComposeNous = NOUS + c.indicatifPasseComposeNous
        }
        if (!c.indicatifPasseComposeVous.contentEquals("-")) {
            c.indicatifPasseComposeVous = VOUS + c.indicatifPasseComposeVous
        }
        if (!c.indicatifPasseComposeIls.contentEquals("-")) {
            c.indicatifPasseComposeIls = ILS + c.indicatifPasseComposeIls
        }
    }

    private fun addPronomsIndicatifPresent(c: Conjugation) {
        val text = c.indicatifPresentJe
        if (!text.contentEquals("-")) {
            c.indicatifPresentJe = if (ActivityUtils.useApostrophe(text)) JEA + text else JE + text
        }
        if (!c.indicatifPresentTu.contentEquals("-")) {
            c.indicatifPresentTu = TU + c.indicatifPresentTu
        }
        if (!c.indicatifPresentIl.contentEquals("-")) {
            c.indicatifPresentIl = IL + c.indicatifPresentIl
        }
        if (!c.indicatifPresentNous.contentEquals("-")) {
            c.indicatifPresentNous = NOUS + c.indicatifPresentNous
        }
        if (!c.indicatifPresentVous.contentEquals("-")) {
            c.indicatifPresentVous = VOUS + c.indicatifPresentVous
        }
        if (!c.indicatifPresentIls.contentEquals("-")) {
            c.indicatifPresentIls = ILS + c.indicatifPresentIls
        }
    }

    /**
     * Ads the reflexive pronoms and accord of participe passe
     * @param c Conjugation
     * @param ppInv boolean is Participe Passe invariable
     */
    private fun addReflexive(c: Conjugation, ppInv: Boolean) {
        // Add pronoms
        // TODO: Show pronoms in different color
        addReflexiveIndicatifPresent(c)
        addReflexiveIndicatifPasseCompose(c, ppInv)
        addReflexiveIndicatifImperfait(c)
        addReflexiveIndicatifPlusQueParfait(c, ppInv)
        addReflexiveIndicatifPasseSimple(c)
        addReflexiveIndicatifPasseAnterieur(c, ppInv)
        addReflexiveIndicatifFuturSimple(c)
        addReflexiveIndicatifFuturAnterieur(c, ppInv)
        addReflexiveConditionnelPresent(c)
        addReflexiveConditionnelPasse(c, ppInv)
        addReflexiveSubjonctifPresent(c)
        addReflexiveSubjonctifPasse(c, ppInv)
        addReflexiveSubjonctifImperfait(c)
        addReflexiveSubjonctifPlusQueParfait(c, ppInv)
        addReflexiveImperatif(c, ppInv)
        addReflexiveInfinitive(c, ppInv)
        addReflexiveParticipe(c, ppInv)
        addReflexiveGerondif(c, ppInv)
    }

    private fun addReflexiveGerondif(c: Conjugation, ppInv: Boolean) {
        var text: String

        text = c.gerondifPresent.replace("en ", "")
        if (!text.contentEquals("-")) {
            c.gerondifPresent = if (ActivityUtils.useApostrophe(text)) "en $SEA$text" else "en $SE$text"
        }
        text = c.gerondifPasse.replace("en ", "")
        if (!text.contentEquals("-")) {
            text = if (ActivityUtils.useApostrophe(text)) "en $SEA$text" else "en $SE$text"
            c.gerondifPasse = if (ppInv) text else "$text(e)(s)"
        }
    }

    private fun addReflexiveParticipe(c: Conjugation, ppInv: Boolean) {
        var text: String = c.participePresent
        if (!text.contentEquals("-")) {
            c.participePresent = if (ActivityUtils.useApostrophe(text)) SEA + text else SE + text
        }
        text = c.participePasse1
        if (!text.contentEquals("-")) {
            c.participePasse1 = if (ActivityUtils.useApostrophe(text)) SEA + text else SE + text
        }
        text = c.participePasse2
        if (!text.contentEquals("-")) {
            c.participePasse2 = if (ppInv) text else "$text(e)(s)"
        }
    }

    private fun addReflexiveInfinitive(c: Conjugation, ppInv: Boolean) {
        var text = c.infinitivePasse
        if (!text.contentEquals("-")) {
            text = if (ActivityUtils.useApostrophe(text)) SEA + text else SE + text
            c.infinitivePasse = if (ppInv) text else "$text(e)(s)"
        }
    }

    private fun addReflexiveImperatif(c: Conjugation, ppInv: Boolean) {
        if (!c.imperatifPresentTu.contentEquals("-")) {
            c.imperatifPresentTu = c.imperatifPresentTu + "-toi"
        }
        if (!c.imperatifPresentNous.contentEquals("-")) {
            c.imperatifPresentNous = c.imperatifPresentNous + "-nous"
        }
        if (!c.imperatifPresentVous.contentEquals("-")) {
            c.imperatifPresentVous = c.imperatifPresentVous + "-vous"
        }

        var text = c.imperatifPasseTu
        if (!text.contentEquals("-")) {
            c.imperatifPasseTu = if (ppInv) text else "$text(e)"
        }
        text = c.imperatifPasseNous
        if (!text.contentEquals("-")) {
            c.imperatifPasseNous = if (ppInv) text else "$text(e)s"
        }
        text = c.imperatifPasseVous
        if (!text.contentEquals("-")) {
            c.imperatifPasseVous = if (ppInv) text else "$text(e)s"
        }
    }

    private fun addReflexiveSubjonctifPlusQueParfait(c: Conjugation, ppInv: Boolean) {
        var text: String = c.subjonctifPlusQueParfaitJe
        if (!text.contentEquals("-")) {
            text = if (ActivityUtils.useApostrophe(text)) MEA + text else ME + text
            c.subjonctifPlusQueParfaitJe = if (ppInv) text else "$text(e)"
        }
        text = c.subjonctifPlusQueParfaitTu
        if (!text.contentEquals("-")) {
            text = if (ActivityUtils.useApostrophe(text)) TEA + text else TE + text
            c.subjonctifPlusQueParfaitTu = if (ppInv) text else "$text(e)"
        }
        text = c.subjonctifPlusQueParfaitIl
        if (!text.contentEquals("-")) {
            text = if (ActivityUtils.useApostrophe(text)) SEA + text else SE + text
            c.subjonctifPlusQueParfaitIl = if (ppInv) text else "$text(e)"
        }
        if (!c.subjonctifPlusQueParfaitNous.contentEquals("-")) {
            text = NOUS + c.subjonctifPlusQueParfaitNous
            c.subjonctifPlusQueParfaitNous = if (ppInv) text else "$text(e)s"
        }
        if (!c.subjonctifPlusQueParfaitVous.contentEquals("-")) {
            text = VOUS + c.subjonctifPlusQueParfaitVous
            c.subjonctifPlusQueParfaitVous = if (ppInv) text else "$text(e)s"
        }
        text = c.subjonctifPlusQueParfaitIls
        if (!text.contentEquals("-")) {
            text = if (ActivityUtils.useApostrophe(text)) SEA + text else SE + text
            c.subjonctifPlusQueParfaitIls = if (ppInv) text else "$text(e)s"
        }
    }

    private fun addReflexiveSubjonctifImperfait(c: Conjugation) {
        var text: String = c.subjonctifImperfaitJe
        if (!text.contentEquals("-")) {
            c.subjonctifImperfaitJe = if (ActivityUtils.useApostrophe(text)) MEA + text else ME + text
        }
        text = c.subjonctifImperfaitTu
        if (!text.contentEquals("-")) {
            c.subjonctifImperfaitTu = if (ActivityUtils.useApostrophe(text)) TEA + text else TE + text
        }
        text = c.subjonctifImperfaitIl
        if (!text.contentEquals("-")) {
            c.subjonctifImperfaitIl = if (ActivityUtils.useApostrophe(text)) SEA + text else SE + text
        }
        if (!c.subjonctifImperfaitNous.contentEquals("-")) {
            c.subjonctifImperfaitNous = NOUS + c.subjonctifImperfaitNous
        }
        if (!c.subjonctifImperfaitVous.contentEquals("-")) {
            c.subjonctifImperfaitVous = VOUS + c.subjonctifImperfaitVous
        }
        text = c.subjonctifImperfaitIls
        if (!text.contentEquals("-")) {
            c.subjonctifImperfaitIls = if (ActivityUtils.useApostrophe(text)) SEA + text else SE + text
        }
    }

    private fun addReflexiveSubjonctifPasse(c: Conjugation, ppInv: Boolean) {
        var text: String = c.subjonctifPasseJe
        if (!text.contentEquals("-")) {
            text = if (ActivityUtils.useApostrophe(text)) MEA + text else ME + text
            c.subjonctifPasseJe = if (ppInv) text else "$text(e)"
        }
        text = c.subjonctifPasseTu
        if (!text.contentEquals("-")) {
            text = if (ActivityUtils.useApostrophe(text)) TEA + text else TE + text
            c.subjonctifPasseTu = if (ppInv) text else "$text(e)"
        }
        text = c.subjonctifPasseIl
        if (!text.contentEquals("-")) {
            text = if (ActivityUtils.useApostrophe(text)) SEA + text else SE + text
            c.subjonctifPasseIl = if (ppInv) text else "$text(e)"
        }
        if (!c.subjonctifPasseNous.contentEquals("-")) {
            text = NOUS + c.subjonctifPasseNous
            c.subjonctifPasseNous = if (ppInv) text else "$text(e)s"
        }
        if (!c.subjonctifPasseVous.contentEquals("-")) {
            text = VOUS + c.subjonctifPasseVous
            c.subjonctifPasseVous = if (ppInv) text else "$text(e)s"
        }
        text = c.subjonctifPasseIls
        if (!text.contentEquals("-")) {
            text = if (ActivityUtils.useApostrophe(text)) SEA + text else SE + text
            c.subjonctifPasseIls = if (ppInv) text else "$text(e)s"
        }
    }

    private fun addReflexiveSubjonctifPresent(c: Conjugation) {
        var text: String = c.subjonctifPresentJe
        if (!text.contentEquals("-")) {
            c.subjonctifPresentJe = if (ActivityUtils.useApostrophe(text)) MEA + text else ME + text
        }
        text = c.subjonctifPresentTu
        if (!text.contentEquals("-")) {
            c.subjonctifPresentTu = if (ActivityUtils.useApostrophe(text)) TEA + text else TE + text
        }
        text = c.subjonctifPresentIl
        if (!text.contentEquals("-")) {
            c.subjonctifPresentIl = if (ActivityUtils.useApostrophe(text)) SEA + text else SE + text
        }
        if (!c.subjonctifPresentNous.contentEquals("-")) {
            c.subjonctifPresentNous = NOUS + c.subjonctifPresentNous
        }
        if (!c.subjonctifPresentVous.contentEquals("-")) {
            c.subjonctifPresentVous = VOUS + c.subjonctifPresentVous
        }
        text = c.subjonctifPresentIls
        if (!text.contentEquals("-")) {
            c.subjonctifPresentIls = if (ActivityUtils.useApostrophe(text)) SEA + text else SE + text
        }
    }

    private fun addReflexiveConditionnelPasse(c: Conjugation, ppInv: Boolean) {
        var text: String = c.conditionnelPasseJe
        if (!text.contentEquals("-")) {
            text = if (ActivityUtils.useApostrophe(text)) MEA + text else ME + text
            c.conditionnelPasseJe = if (ppInv) text else "$text(e)"
        }
        text = c.conditionnelPasseTu
        if (!text.contentEquals("-")) {
            text = if (ActivityUtils.useApostrophe(text)) TEA + text else TE + text
            c.conditionnelPasseTu = if (ppInv) text else "$text(e)"
        }
        text = c.conditionnelPasseIl
        if (!text.contentEquals("-")) {
            text = if (ActivityUtils.useApostrophe(text)) SEA + text else SE + text
            c.conditionnelPasseIl = if (ppInv) text else "$text(e)"
        }
        if (!c.conditionnelPasseNous.contentEquals("-")) {
            text = NOUS + c.conditionnelPasseNous
            c.conditionnelPasseNous = if (ppInv) text else "$text(e)s"
        }
        if (!c.conditionnelPasseVous.contentEquals("-")) {
            text = VOUS + c.conditionnelPasseVous
            c.conditionnelPasseVous = if (ppInv) text else "$text(e)s"
        }
        text = c.conditionnelPasseIls
        if (!text.contentEquals("-")) {
            text = if (ActivityUtils.useApostrophe(text)) SEA + text else SE + text
            c.conditionnelPasseIls = if (ppInv) text else "$text(e)s"
        }
    }

    private fun addReflexiveConditionnelPresent(c: Conjugation) {
        var text: String = c.conditionnelPresentJe
        if (!text.contentEquals("-")) {
            c.conditionnelPresentJe = if (ActivityUtils.useApostrophe(text)) MEA + text else ME + text
        }
        text = c.conditionnelPresentTu
        if (!text.contentEquals("-")) {
            c.conditionnelPresentTu = if (ActivityUtils.useApostrophe(text)) TEA + text else TE + text
        }
        text = c.conditionnelPresentIl
        if (!text.contentEquals("-")) {
            c.conditionnelPresentIl = if (ActivityUtils.useApostrophe(text)) SEA + text else SE + text
        }
        if (!c.conditionnelPresentNous.contentEquals("-")) {
            c.conditionnelPresentNous = NOUS + c.conditionnelPresentNous
        }
        if (!c.conditionnelPresentVous.contentEquals("-")) {
            c.conditionnelPresentVous = VOUS + c.conditionnelPresentVous
        }
        text = c.conditionnelPresentIls
        if (!text.contentEquals("-")) {
            c.conditionnelPresentIls = if (ActivityUtils.useApostrophe(text)) SEA + text else SE + text
        }
    }

    private fun addReflexiveIndicatifFuturAnterieur(c: Conjugation, ppInv: Boolean) {
        var text: String = c.indicatifFuturAnterieurJe
        if (!text.contentEquals("-")) {
            text = if (ActivityUtils.useApostrophe(text)) MEA + text else ME + text
            c.indicatifFuturAnterieurJe = if (ppInv) text else "$text(e)"
        }
        text = c.indicatifFuturAnterieurTu
        if (!text.contentEquals("-")) {
            text = if (ActivityUtils.useApostrophe(text)) TEA + text else TE + text
            c.indicatifFuturAnterieurTu = if (ppInv) text else "$text(e)"
        }
        text = c.indicatifFuturAnterieurIl
        if (!text.contentEquals("-")) {
            text = if (ActivityUtils.useApostrophe(text)) SEA + text else SE + text
            c.indicatifFuturAnterieurIl = if (ppInv) text else "$text(e)"
        }
        if (!c.indicatifFuturAnterieurNous.contentEquals("-")) {
            text = NOUS + c.indicatifFuturAnterieurNous
            c.indicatifFuturAnterieurNous = if (ppInv) text else "$text(e)s"
        }
        if (!c.indicatifFuturAnterieurVous.contentEquals("-")) {
            text = VOUS + c.indicatifFuturAnterieurVous
            c.indicatifFuturAnterieurVous = if (ppInv) text else "$text(e)s"
        }
        text = c.indicatifFuturAnterieurIls
        if (!text.contentEquals("-")) {
            text = if (ActivityUtils.useApostrophe(text)) SEA + text else SE + text
            c.indicatifFuturAnterieurIls = if (ppInv) text else "$text(e)s"
        }
    }

    private fun addReflexiveIndicatifFuturSimple(c: Conjugation) {
        var text: String = c.indicatifFuturSimpleJe
        if (!text.contentEquals("-")) {
            c.indicatifFuturSimpleJe = if (ActivityUtils.useApostrophe(text)) MEA + text else ME + text
        }
        text = c.indicatifFuturSimpleTu
        if (!text.contentEquals("-")) {
            c.indicatifFuturSimpleTu = if (ActivityUtils.useApostrophe(text)) TEA + text else TE + text
        }
        text = c.indicatifFuturSimpleIl
        if (!text.contentEquals("-")) {
            c.indicatifFuturSimpleIl = if (ActivityUtils.useApostrophe(text)) SEA + text else SE + text
        }
        if (!c.indicatifFuturSimpleNous.contentEquals("-")) {
            c.indicatifFuturSimpleNous = NOUS + c.indicatifFuturSimpleNous
        }
        if (!c.indicatifFuturSimpleVous.contentEquals("-")) {
            c.indicatifFuturSimpleVous = VOUS + c.indicatifFuturSimpleVous
        }
        text = c.indicatifFuturSimpleIls
        if (!text.contentEquals("-")) {
            c.indicatifFuturSimpleIls = if (ActivityUtils.useApostrophe(text)) SEA + text else SE + text
        }
    }

    private fun addReflexiveIndicatifPasseAnterieur(c: Conjugation, ppInv: Boolean) {
        var text: String = c.indicatifPasseAnterieurJe
        if (!text.contentEquals("-")) {
            text = if (ActivityUtils.useApostrophe(text)) MEA + text else ME + text
            c.indicatifPasseAnterieurJe = if (ppInv) text else "$text(e)"
        }
        text = c.indicatifPasseAnterieurTu
        if (!text.contentEquals("-")) {
            text = if (ActivityUtils.useApostrophe(text)) TEA + text else TE + text
            c.indicatifPasseAnterieurTu = if (ppInv) text else "$text(e)"
        }
        text = c.indicatifPasseAnterieurIl
        if (!text.contentEquals("-")) {
            text = if (ActivityUtils.useApostrophe(text)) SEA + text else SE + text
            c.indicatifPasseAnterieurIl = if (ppInv) text else "$text(e)"
        }
        if (!c.indicatifPasseAnterieurNous.contentEquals("-")) {
            text = NOUS + c.indicatifPasseAnterieurNous
            c.indicatifPasseAnterieurNous = if (ppInv) text else "$text(e)s"
        }
        if (!c.indicatifPasseAnterieurVous.contentEquals("-")) {
            text = VOUS + c.indicatifPasseAnterieurVous
            c.indicatifPasseAnterieurVous = if (ppInv) text else "$text(e)s"
        }
        text = c.indicatifPasseAnterieurIls
        if (!text.contentEquals("-")) {
            text = if (ActivityUtils.useApostrophe(text)) SEA + text else SE + text
            c.indicatifPasseAnterieurIls = if (ppInv) text else "$text(e)s"
        }
    }

    private fun addReflexiveIndicatifPasseSimple(c: Conjugation) {
        var text: String = c.indicatifPasseSimpleJe
        if (!text.contentEquals("-")) {
            c.indicatifPasseSimpleJe = if (ActivityUtils.useApostrophe(text)) MEA + text else ME + text
        }
        text = c.indicatifPasseSimpleTu
        if (!text.contentEquals("-")) {
            c.indicatifPasseSimpleTu = if (ActivityUtils.useApostrophe(text)) TEA + text else TE + text
        }
        text = c.indicatifPasseSimpleIl
        if (!text.contentEquals("-")) {
            c.indicatifPasseSimpleIl = if (ActivityUtils.useApostrophe(text)) SEA + text else SE + text
        }
        if (!c.indicatifPasseSimpleNous.contentEquals("-")) {
            c.indicatifPasseSimpleNous = NOUS + c.indicatifPasseSimpleNous
        }
        if (!c.indicatifPasseSimpleVous.contentEquals("-")) {
            c.indicatifPasseSimpleVous = VOUS + c.indicatifPasseSimpleVous
        }
        text = c.indicatifPasseSimpleIls
        if (!text.contentEquals("-")) {
            c.indicatifPasseSimpleIls = if (ActivityUtils.useApostrophe(text)) SEA + text else SE + text
        }
    }

    private fun addReflexiveIndicatifPlusQueParfait(c: Conjugation, ppInv: Boolean) {
        var text: String = c.indicatifPlusQueParfaitJe
        if (!text.contentEquals("-")) {
            text = if (ActivityUtils.useApostrophe(text)) MEA + text else ME + text
            c.indicatifPlusQueParfaitJe = if (ppInv) text else "$text(e)"
        }
        text = c.indicatifPlusQueParfaitTu
        if (!text.contentEquals("-")) {
            text = if (ActivityUtils.useApostrophe(text)) TEA + text else TE + text
            c.indicatifPlusQueParfaitTu = if (ppInv) text else "$text(e)"
        }
        text = c.indicatifPlusQueParfaitIl
        if (!text.contentEquals("-")) {
            text = if (ActivityUtils.useApostrophe(text)) SEA + text else SE + text
            c.indicatifPlusQueParfaitIl = if (ppInv) text else "$text(e)"
        }
        if (!c.indicatifPlusQueParfaitNous.contentEquals("-")) {
            text = NOUS + c.indicatifPlusQueParfaitNous
            c.indicatifPlusQueParfaitNous = if (ppInv) text else "$text(e)s"
        }
        if (!c.indicatifPlusQueParfaitVous.contentEquals("-")) {
            text = VOUS + c.indicatifPlusQueParfaitVous
            c.indicatifPlusQueParfaitVous = if (ppInv) text else "$text(e)s"
        }
        text = c.indicatifPlusQueParfaitIls
        if (!text.contentEquals("-")) {
            text = if (ActivityUtils.useApostrophe(text)) SEA + text else SE + text
            c.indicatifPlusQueParfaitIls = if (ppInv) text else "$text(e)s"
        }
    }

    private fun addReflexiveIndicatifImperfait(c: Conjugation) {
        var text: String = c.indicatifImperfaitJe
        if (!text.contentEquals("-")) {
            c.indicatifImperfaitJe = if (ActivityUtils.useApostrophe(text)) MEA + text else ME + text
        }
        text = c.indicatifImperfaitTu
        if (!text.contentEquals("-")) {
            c.indicatifImperfaitTu = if (ActivityUtils.useApostrophe(text)) TEA + text else TE + text
        }
        text = c.indicatifImperfaitIl
        if (!text.contentEquals("-")) {
            c.indicatifImperfaitIl = if (ActivityUtils.useApostrophe(text)) SEA + text else SE + text
        }
        if (!c.indicatifImperfaitNous.contentEquals("-")) {
            c.indicatifImperfaitNous = NOUS + c.indicatifImperfaitNous
        }
        if (!c.indicatifImperfaitVous.contentEquals("-")) {
            c.indicatifImperfaitVous = VOUS + c.indicatifImperfaitVous
        }
        text = c.indicatifImperfaitIls
        if (!text.contentEquals("-")) {
            c.indicatifImperfaitIls = if (ActivityUtils.useApostrophe(text)) SEA + text else SE + text
        }
    }

    private fun addReflexiveIndicatifPasseCompose(c: Conjugation, ppInv: Boolean) {
        var text: String = c.indicatifPasseComposeJe
        if (!text.contentEquals("-")) {
            text = if (ActivityUtils.useApostrophe(text)) MEA + text else ME + text
            c.indicatifPasseComposeJe = if (ppInv) text else "$text(e)"
        }
        text = c.indicatifPasseComposeTu
        if (!text.contentEquals("-")) {
            text = if (ActivityUtils.useApostrophe(text)) TEA + text else TE + text
            c.indicatifPasseComposeTu = if (ppInv) text else "$text(e)"
        }
        text = c.indicatifPasseComposeIl
        if (!text.contentEquals("-")) {
            text = if (ActivityUtils.useApostrophe(text)) SEA + text else SE + text
            c.indicatifPasseComposeIl = if (ppInv) text else "$text(e)"
        }
        if (!c.indicatifPasseComposeNous.contentEquals("-")) {
            text = NOUS + c.indicatifPasseComposeNous
            c.indicatifPasseComposeNous = if (ppInv) text else "$text(e)s"
        }
        if (!c.indicatifPasseComposeVous.contentEquals("-")) {
            text = VOUS + c.indicatifPasseComposeVous
            c.indicatifPasseComposeVous = if (ppInv) text else "$text(e)s"
        }
        text = c.indicatifPasseComposeIls
        if (!text.contentEquals("-")) {
            text = if (ActivityUtils.useApostrophe(text)) SEA + text else SE + text
            c.indicatifPasseComposeIls = if (ppInv) text else "$text(e)s"
        }
    }

    private fun addReflexiveIndicatifPresent(c: Conjugation) {
        var text = c.indicatifPresentJe
        if (!text.contentEquals("-")) {
            c.indicatifPresentJe = if (ActivityUtils.useApostrophe(text)) MEA + text else ME + text
        }
        text = c.indicatifPresentTu
        if (!text.contentEquals("-")) {
            c.indicatifPresentTu = if (ActivityUtils.useApostrophe(text)) TEA + text else TE + text
        }
        text = c.indicatifPresentIl
        if (!text.contentEquals("-")) {
            c.indicatifPresentIl = if (ActivityUtils.useApostrophe(text)) SEA + text else SE + text
        }
        if (!c.indicatifPresentNous.contentEquals("-")) {
            c.indicatifPresentNous = NOUS + c.indicatifPresentNous
        }
        if (!c.indicatifPresentVous.contentEquals("-")) {
            c.indicatifPresentVous = VOUS + c.indicatifPresentVous
        }
        text = c.indicatifPresentIls
        if (!text.contentEquals("-")) {
            c.indicatifPresentIls = if (ActivityUtils.useApostrophe(text)) SEA + text else SE + text
        }
    }


    /**
     * Fills the conjugation section.
     * @param c Conjugation ready to display
     */
    private fun fillConjugationDetails(c: Conjugation) {
        val fontSize = Integer.parseInt(ActivityUtils.getPreferenceFontSize(applicationContext))
        changeTextFontInConjugation(fontSize)

        (findViewById<View>(R.id.infinitive_present) as TextView).text = c.infinitivePresent
        (findViewById<View>(R.id.infinitive_passe) as TextView).text = c.infinitivePasse
        (findViewById<View>(R.id.participe_present) as TextView).text = c.participePresent
        (findViewById<View>(R.id.participe_passe1) as TextView).text = c.participePasse1
        (findViewById<View>(R.id.participe_passe2) as TextView).text = c.participePasse2
        (findViewById<View>(R.id.gerondif_present) as TextView).text = c.gerondifPresent
        (findViewById<View>(R.id.gerondif_passe) as TextView).text = c.gerondifPasse

        (findViewById<View>(R.id.imperatif_present_tu) as TextView).text = c.imperatifPresentTu
        (findViewById<View>(R.id.imperatif_present_nous) as TextView).text = c.imperatifPresentNous
        (findViewById<View>(R.id.imperatif_present_vous) as TextView).text = c.imperatifPresentVous
        (findViewById<View>(R.id.imperatif_passe_tu) as TextView).text = c.imperatifPasseTu
        (findViewById<View>(R.id.imperatif_passe_nous) as TextView).text = c.imperatifPasseNous
        (findViewById<View>(R.id.imperatif_passe_vous) as TextView).text = c.imperatifPasseVous

        (findViewById<View>(R.id.indicative_present_je) as TextView).text = c.indicatifPresentJe
        (findViewById<View>(R.id.indicative_present_tu) as TextView).text = c.indicatifPresentTu
        (findViewById<View>(R.id.indicative_present_il) as TextView).text = c.indicatifPresentIl
        (findViewById<View>(R.id.indicative_present_nous) as TextView).text = c.indicatifPresentNous
        (findViewById<View>(R.id.indicative_present_vous) as TextView).text = c.indicatifPresentVous
        (findViewById<View>(R.id.indicative_present_ils) as TextView).text = c.indicatifPresentIls
        (findViewById<View>(R.id.indicative_passe_compose_je) as TextView).text = c.indicatifPasseComposeJe
        (findViewById<View>(R.id.indicative_passe_compose_tu) as TextView).text = c.indicatifPasseComposeTu
        (findViewById<View>(R.id.indicative_passe_compose_il) as TextView).text = c.indicatifPasseComposeIl
        (findViewById<View>(R.id.indicative_passe_compose_nous) as TextView).text = c.indicatifPasseComposeNous
        (findViewById<View>(R.id.indicative_passe_compose_vous) as TextView).text = c.indicatifPasseComposeVous
        (findViewById<View>(R.id.indicative_passe_compose_ils) as TextView).text = c.indicatifPasseComposeIls
        (findViewById<View>(R.id.indicative_imperfait_je) as TextView).text = c.indicatifImperfaitJe
        (findViewById<View>(R.id.indicative_imperfait_tu) as TextView).text = c.indicatifImperfaitTu
        (findViewById<View>(R.id.indicative_imperfait_il) as TextView).text = c.indicatifImperfaitIl
        (findViewById<View>(R.id.indicative_imperfait_nous) as TextView).text = c.indicatifImperfaitNous
        (findViewById<View>(R.id.indicative_imperfait_vous) as TextView).text = c.indicatifImperfaitVous
        (findViewById<View>(R.id.indicative_imperfait_ils) as TextView).text = c.indicatifImperfaitIls
        (findViewById<View>(R.id.indicative_plus_que_parfait_je) as TextView).text = c.indicatifPlusQueParfaitJe
        (findViewById<View>(R.id.indicative_plus_que_parfait_tu) as TextView).text = c.indicatifPlusQueParfaitTu
        (findViewById<View>(R.id.indicative_plus_que_parfait_il) as TextView).text = c.indicatifPlusQueParfaitIl
        (findViewById<View>(R.id.indicative_plus_que_parfait_nous) as TextView).text = c.indicatifPlusQueParfaitNous
        (findViewById<View>(R.id.indicative_plus_que_parfait_vous) as TextView).text = c.indicatifPlusQueParfaitVous
        (findViewById<View>(R.id.indicative_plus_que_parfait_ils) as TextView).text = c.indicatifPlusQueParfaitIls
        (findViewById<View>(R.id.indicative_passe_simple_je) as TextView).text = c.indicatifPasseSimpleJe
        (findViewById<View>(R.id.indicative_passe_simple_tu) as TextView).text = c.indicatifPasseSimpleTu
        (findViewById<View>(R.id.indicative_passe_simple_il) as TextView).text = c.indicatifPasseSimpleIl
        (findViewById<View>(R.id.indicative_passe_simple_nous) as TextView).text = c.indicatifPasseSimpleNous
        (findViewById<View>(R.id.indicative_passe_simple_vous) as TextView).text = c.indicatifPasseSimpleVous
        (findViewById<View>(R.id.indicative_passe_simple_ils) as TextView).text = c.indicatifPasseSimpleIls
        (findViewById<View>(R.id.indicative_passe_anterieur_je) as TextView).text = c.indicatifPasseAnterieurJe
        (findViewById<View>(R.id.indicative_passe_anterieur_tu) as TextView).text = c.indicatifPasseAnterieurTu
        (findViewById<View>(R.id.indicative_passe_anterieur_il) as TextView).text = c.indicatifPasseAnterieurIl
        (findViewById<View>(R.id.indicative_passe_anterieur_nous) as TextView).text = c.indicatifPasseAnterieurNous
        (findViewById<View>(R.id.indicative_passe_anterieur_vous) as TextView).text = c.indicatifPasseAnterieurVous
        (findViewById<View>(R.id.indicative_passe_anterieur_ils) as TextView).text = c.indicatifPasseAnterieurIls
        (findViewById<View>(R.id.indicative_futur_simple_je) as TextView).text = c.indicatifFuturSimpleJe
        (findViewById<View>(R.id.indicative_futur_simple_tu) as TextView).text = c.indicatifFuturSimpleTu
        (findViewById<View>(R.id.indicative_futur_simple_il) as TextView).text = c.indicatifFuturSimpleIl
        (findViewById<View>(R.id.indicative_futur_simple_nous) as TextView).text = c.indicatifFuturSimpleNous
        (findViewById<View>(R.id.indicative_futur_simple_vous) as TextView).text = c.indicatifFuturSimpleVous
        (findViewById<View>(R.id.indicative_futur_simple_ils) as TextView).text = c.indicatifFuturSimpleIls
        (findViewById<View>(R.id.indicative_futur_anterieur_je) as TextView).text = c.indicatifFuturAnterieurJe
        (findViewById<View>(R.id.indicative_futur_anterieur_tu) as TextView).text = c.indicatifFuturAnterieurTu
        (findViewById<View>(R.id.indicative_futur_anterieur_il) as TextView).text = c.indicatifFuturAnterieurIl
        (findViewById<View>(R.id.indicative_futur_anterieur_nous) as TextView).text = c.indicatifFuturAnterieurNous
        (findViewById<View>(R.id.indicative_futur_anterieur_vous) as TextView).text = c.indicatifFuturAnterieurVous
        (findViewById<View>(R.id.indicative_futur_anterieur_ils) as TextView).text = c.indicatifFuturAnterieurIls

        (findViewById<View>(R.id.conditionnel_present_je) as TextView).text = c.conditionnelPresentJe
        (findViewById<View>(R.id.conditionnel_present_tu) as TextView).text = c.conditionnelPresentTu
        (findViewById<View>(R.id.conditionnel_present_il) as TextView).text = c.conditionnelPresentIl
        (findViewById<View>(R.id.conditionnel_present_nous) as TextView).text = c.conditionnelPresentNous
        (findViewById<View>(R.id.conditionnel_present_vous) as TextView).text = c.conditionnelPresentVous
        (findViewById<View>(R.id.conditionnel_present_ils) as TextView).text = c.conditionnelPresentIls
        (findViewById<View>(R.id.conditionnel_passe_je) as TextView).text = c.conditionnelPasseJe
        (findViewById<View>(R.id.conditionnel_passe_tu) as TextView).text = c.conditionnelPasseTu
        (findViewById<View>(R.id.conditionnel_passe_il) as TextView).text = c.conditionnelPasseIl
        (findViewById<View>(R.id.conditionnel_passe_nous) as TextView).text = c.conditionnelPasseNous
        (findViewById<View>(R.id.conditionnel_passe_vous) as TextView).text = c.conditionnelPasseVous
        (findViewById<View>(R.id.conditionnel_passe_ils) as TextView).text = c.conditionnelPasseIls

        (findViewById<View>(R.id.subjonctif_present_je) as TextView).text = c.subjonctifPresentJe
        (findViewById<View>(R.id.subjonctif_present_tu) as TextView).text = c.subjonctifPresentTu
        (findViewById<View>(R.id.subjonctif_present_il) as TextView).text = c.subjonctifPresentIl
        (findViewById<View>(R.id.subjonctif_present_nous) as TextView).text = c.subjonctifPresentNous
        (findViewById<View>(R.id.subjonctif_present_vous) as TextView).text = c.subjonctifPresentVous
        (findViewById<View>(R.id.subjonctif_present_ils) as TextView).text = c.subjonctifPresentIls
        (findViewById<View>(R.id.subjonctif_passe_je) as TextView).text = c.subjonctifPasseJe
        (findViewById<View>(R.id.subjonctif_passe_tu) as TextView).text = c.subjonctifPasseTu
        (findViewById<View>(R.id.subjonctif_passe_il) as TextView).text = c.subjonctifPasseIl
        (findViewById<View>(R.id.subjonctif_passe_nous) as TextView).text = c.subjonctifPasseNous
        (findViewById<View>(R.id.subjonctif_passe_vous) as TextView).text = c.subjonctifPasseVous
        (findViewById<View>(R.id.subjonctif_passe_ils) as TextView).text = c.subjonctifPasseIls
        (findViewById<View>(R.id.subjonctif_imperfait_je) as TextView).text = c.subjonctifImperfaitJe
        (findViewById<View>(R.id.subjonctif_imperfait_tu) as TextView).text = c.subjonctifImperfaitTu
        (findViewById<View>(R.id.subjonctif_imperfait_il) as TextView).text = c.subjonctifImperfaitIl
        (findViewById<View>(R.id.subjonctif_imperfait_nous) as TextView).text = c.subjonctifImperfaitNous
        (findViewById<View>(R.id.subjonctif_imperfait_vous) as TextView).text = c.subjonctifImperfaitVous
        (findViewById<View>(R.id.subjonctif_imperfait_ils) as TextView).text = c.subjonctifImperfaitIls
        (findViewById<View>(R.id.subjonctif_plus_que_parfait_je) as TextView).text = c.subjonctifPlusQueParfaitJe
        (findViewById<View>(R.id.subjonctif_plus_que_parfait_tu) as TextView).text = c.subjonctifPlusQueParfaitTu
        (findViewById<View>(R.id.subjonctif_plus_que_parfait_il) as TextView).text = c.subjonctifPlusQueParfaitIl
        (findViewById<View>(R.id.subjonctif_plus_que_parfait_nous) as TextView).text = c.subjonctifPlusQueParfaitNous
        (findViewById<View>(R.id.subjonctif_plus_que_parfait_vous) as TextView).text = c.subjonctifPlusQueParfaitVous
        (findViewById<View>(R.id.subjonctif_plus_que_parfait_ils) as TextView).text = c.subjonctifPlusQueParfaitIls
    }


    /**
     * Changes text font size.
     * @param fontSize int
     */
    private fun changeTextFontInConjugation(fontSize: Int) {
        val unit = TypedValue.COMPLEX_UNIT_SP
        (findViewById<View>(R.id.indicative_present_je) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_present_tu) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_present_il) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_present_nous) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_present_vous) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_present_ils) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_passe_compose_je) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_passe_compose_tu) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_passe_compose_il) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_passe_compose_nous) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_passe_compose_vous) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_passe_compose_ils) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_imperfait_je) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_imperfait_tu) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_imperfait_il) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_imperfait_nous) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_imperfait_vous) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_imperfait_ils) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_plus_que_parfait_je) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_plus_que_parfait_tu) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_plus_que_parfait_il) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_plus_que_parfait_nous) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_plus_que_parfait_vous) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_plus_que_parfait_ils) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_passe_simple_je) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_passe_simple_tu) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_passe_simple_il) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_passe_simple_nous) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_passe_simple_vous) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_passe_simple_ils) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_passe_anterieur_je) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_passe_anterieur_tu) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_passe_anterieur_il) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_passe_anterieur_nous) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_passe_anterieur_vous) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_passe_anterieur_ils) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_futur_simple_je) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_futur_simple_tu) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_futur_simple_il) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_futur_simple_nous) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_futur_simple_vous) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_futur_simple_ils) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_futur_anterieur_je) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_futur_anterieur_tu) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_futur_anterieur_il) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_futur_anterieur_nous) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_futur_anterieur_vous) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.indicative_futur_anterieur_ils) as TextView).setTextSize(unit, fontSize.toFloat())

        (findViewById<View>(R.id.conditionnel_present_je) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.conditionnel_present_tu) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.conditionnel_present_il) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.conditionnel_present_nous) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.conditionnel_present_vous) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.conditionnel_present_ils) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.conditionnel_passe_je) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.conditionnel_passe_tu) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.conditionnel_passe_il) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.conditionnel_passe_nous) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.conditionnel_passe_vous) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.conditionnel_passe_ils) as TextView).setTextSize(unit, fontSize.toFloat())

        (findViewById<View>(R.id.subjonctif_present_je) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.subjonctif_present_tu) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.subjonctif_present_il) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.subjonctif_present_nous) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.subjonctif_present_vous) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.subjonctif_present_ils) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.subjonctif_passe_je) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.subjonctif_passe_tu) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.subjonctif_passe_il) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.subjonctif_passe_nous) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.subjonctif_passe_vous) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.subjonctif_passe_ils) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.subjonctif_imperfait_je) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.subjonctif_imperfait_tu) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.subjonctif_imperfait_il) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.subjonctif_imperfait_nous) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.subjonctif_imperfait_vous) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.subjonctif_imperfait_ils) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.subjonctif_plus_que_parfait_je) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.subjonctif_plus_que_parfait_tu) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.subjonctif_plus_que_parfait_il) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.subjonctif_plus_que_parfait_nous) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.subjonctif_plus_que_parfait_vous) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.subjonctif_plus_que_parfait_ils) as TextView).setTextSize(unit, fontSize.toFloat())

        (findViewById<View>(R.id.imperatif_present_tu) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.imperatif_present_nous) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.imperatif_present_vous) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.imperatif_passe_tu) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.imperatif_passe_nous) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.imperatif_passe_vous) as TextView).setTextSize(unit, fontSize.toFloat())

        (findViewById<View>(R.id.infinitive_present) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.infinitive_passe) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.participe_present) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.participe_passe1) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.participe_passe2) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.gerondif_present) as TextView).setTextSize(unit, fontSize.toFloat())
        (findViewById<View>(R.id.gerondif_passe) as TextView).setTextSize(unit, fontSize.toFloat())
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

            R.id.play_indicative_present_je ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifPresentJe)
            R.id.play_indicative_present_tu ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifPresentTu)
            R.id.play_indicative_present_il ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifPresentIl)
            R.id.play_indicative_present_nous ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifPresentNous)
            R.id.play_indicative_present_vous ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifPresentVous)
            R.id.play_indicative_present_ils ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifPresentIls)

            R.id.play_indicative_passe_compose_je ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifPasseComposeJe)
            R.id.play_indicative_passe_compose_tu ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifPasseComposeTu)
            R.id.play_indicative_passe_compose_il ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifPasseComposeIl)
            R.id.play_indicative_passe_compose_nous ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifPasseComposeNous)
            R.id.play_indicative_passe_compose_vous ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifPasseComposeVous)
            R.id.play_indicative_passe_compose_ils ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifPasseComposeIls)

            R.id.play_indicative_imperfait_je ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifImperfaitJe)
            R.id.play_indicative_imperfait_tu ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifImperfaitTu)
            R.id.play_indicative_imperfait_il ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifImperfaitIl)
            R.id.play_indicative_imperfait_nous ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifImperfaitNous)
            R.id.play_indicative_imperfait_vous ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifImperfaitVous)
            R.id.play_indicative_imperfait_ils ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifImperfaitIls)

            R.id.play_indicative_plus_que_parfait_je ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifPlusQueParfaitJe)
            R.id.play_indicative_plus_que_parfait_tu ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifPlusQueParfaitTu)
            R.id.play_indicative_plus_que_parfait_il ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifPlusQueParfaitIl)
            R.id.play_indicative_plus_que_parfait_nous ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifPlusQueParfaitNous)
            R.id.play_indicative_plus_que_parfait_vous ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifPlusQueParfaitVous)
            R.id.play_indicative_plus_que_parfait_ils ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifPlusQueParfaitIls)

            R.id.play_indicative_passe_simple_je ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifPasseSimpleJe)
            R.id.play_indicative_passe_simple_tu ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifPasseSimpleTu)
            R.id.play_indicative_passe_simple_il ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifPasseSimpleIl)
            R.id.play_indicative_passe_simple_nous ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifPasseSimpleNous)
            R.id.play_indicative_passe_simple_vous ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifPasseSimpleVous)
            R.id.play_indicative_passe_simple_ils ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifPasseSimpleIls)

            R.id.play_indicative_passe_anterieur_je ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifPasseAnterieurJe)
            R.id.play_indicative_passe_anterieur_tu ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifPasseAnterieurTu)
            R.id.play_indicative_passe_anterieur_il ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifPasseAnterieurIl)
            R.id.play_indicative_passe_anterieur_nous ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifPasseAnterieurNous)
            R.id.play_indicative_passe_anterieur_vous ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifPasseAnterieurVous)
            R.id.play_indicative_passe_anterieur_ils ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifPasseAnterieurIls)

            R.id.play_indicative_futur_simple_je ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifFuturSimpleJe)
            R.id.play_indicative_futur_simple_tu ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifFuturSimpleTu)
            R.id.play_indicative_futur_simple_il ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifFuturSimpleIl)
            R.id.play_indicative_futur_simple_nous ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifFuturSimpleNous)
            R.id.play_indicative_futur_simple_vous ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifFuturSimpleVous)
            R.id.play_indicative_futur_simple_ils ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifFuturSimpleIls)

            R.id.play_indicative_futur_anterieur_je ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifFuturAnterieurJe)
            R.id.play_indicative_futur_anterieur_tu ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifFuturAnterieurTu)
            R.id.play_indicative_futur_anterieur_il ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifFuturAnterieurIl)
            R.id.play_indicative_futur_anterieur_nous ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifFuturAnterieurNous)
            R.id.play_indicative_futur_anterieur_vous ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifFuturAnterieurVous)
            R.id.play_indicative_futur_anterieur_ils ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.indicatifFuturAnterieurIls)

            R.id.play_conditionnel_present_je ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.conditionnelPresentJe)
            R.id.play_conditionnel_present_tu ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.conditionnelPresentTu)
            R.id.play_conditionnel_present_il ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.conditionnelPresentIl)
            R.id.play_conditionnel_present_nous ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.conditionnelPresentNous)
            R.id.play_conditionnel_present_vous ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.conditionnelPresentVous)
            R.id.play_conditionnel_present_ils ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.conditionnelPresentIls)

            R.id.play_conditionnel_passe_je ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.conditionnelPasseJe)
            R.id.play_conditionnel_passe_tu ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.conditionnelPasseTu)
            R.id.play_conditionnel_passe_il ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.conditionnelPasseIl)
            R.id.play_conditionnel_passe_nous ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.conditionnelPasseNous)
            R.id.play_conditionnel_passe_vous ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.conditionnelPasseVous)
            R.id.play_conditionnel_passe_ils ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.conditionnelPasseIls)

            R.id.play_subjonctif_present_je ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.subjonctifPresentJe)
            R.id.play_subjonctif_present_tu ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.subjonctifPresentTu)
            R.id.play_subjonctif_present_il ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.subjonctifPresentIl)
            R.id.play_subjonctif_present_nous ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.subjonctifPresentNous)
            R.id.play_subjonctif_present_vous ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.subjonctifPresentVous)
            R.id.play_subjonctif_present_ils ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.subjonctifPresentIls)

            R.id.play_subjonctif_passe_je ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.subjonctifPasseJe)
            R.id.play_subjonctif_passe_tu ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.subjonctifPasseTu)
            R.id.play_subjonctif_passe_il ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.subjonctifPasseIl)
            R.id.play_subjonctif_passe_nous ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.subjonctifPasseNous)
            R.id.play_subjonctif_passe_vous ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.subjonctifPasseVous)
            R.id.play_subjonctif_passe_ils ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.subjonctifPasseIls)

            R.id.play_subjonctif_imperfait_je ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.subjonctifImperfaitJe)
            R.id.play_subjonctif_imperfait_tu ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.subjonctifImperfaitTu)
            R.id.play_subjonctif_imperfait_il ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.subjonctifImperfaitIl)
            R.id.play_subjonctif_imperfait_nous ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.subjonctifImperfaitNous)
            R.id.play_subjonctif_imperfait_vous ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.subjonctifImperfaitVous)
            R.id.play_subjonctif_imperfait_ils ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.subjonctifImperfaitIls)

            R.id.play_subjonctif_plus_que_parfait_je ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.subjonctifPlusQueParfaitJe)
            R.id.play_subjonctif_plus_que_parfait_tu ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.subjonctifPlusQueParfaitTu)
            R.id.play_subjonctif_plus_que_parfait_il ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.subjonctifPlusQueParfaitIl)
            R.id.play_subjonctif_plus_que_parfait_nous ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.subjonctifPlusQueParfaitNous)
            R.id.play_subjonctif_plus_que_parfait_vous ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.subjonctifPlusQueParfaitVous)
            R.id.play_subjonctif_plus_que_parfait_ils ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.subjonctifPlusQueParfaitIls)

            R.id.play_imperatif_present_tu ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.imperatifPresentTu)
            R.id.play_imperatif_present_nous ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.imperatifPresentNous)
            R.id.play_imperatif_present_vous ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.imperatifPresentVous)
            R.id.play_imperatif_passe_tu ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.imperatifPasseTu)
            R.id.play_imperatif_passe_nous ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.imperatifPasseNous)
            R.id.play_imperatif_passe_vous ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.imperatifPasseVous)

            R.id.play_infinitive_present ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.infinitivePresent)
            R.id.play_infinitive_passe ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.infinitivePasse)
            R.id.play_participe_present ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.participePresent)
            R.id.play_participe_passe1 ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.participePasse1)
            R.id.play_participe_passe2 ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.participePasse2)
            R.id.play_gerondif_present ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.gerondifPresent)
            R.id.play_gerondif_passe ->
                ActivityUtils.speak(applicationContext, tts, conjugation?.gerondifPasse)

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
                scrollView!!.requestChildFocus(findViewById(R.id.indicative_present_ils), findViewById(R.id.indicative_present_ils))
                scrollView!!.requestChildFocus(findViewById(R.id.indicative_present_je), findViewById(R.id.indicative_present_je))
                showcaseView!!.setShowcase(ViewTarget(findViewById(R.id.indicative_present_je)), true)
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
