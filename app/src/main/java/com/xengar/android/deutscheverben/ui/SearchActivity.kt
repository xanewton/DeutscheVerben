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

import android.app.SearchManager
import android.content.ContentResolver
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable

import com.google.firebase.analytics.FirebaseAnalytics
import com.xengar.android.deutscheverben.R
import com.xengar.android.deutscheverben.adapter.VerbHolder
import com.xengar.android.deutscheverben.data.Verb
import com.xengar.android.deutscheverben.data.VerbContract.VerbEntry
import com.xengar.android.deutscheverben.utils.ActivityUtils
import com.xengar.android.deutscheverben.utils.Constants.LIST
import com.xengar.android.deutscheverben.utils.Constants.LOG
import com.xengar.android.deutscheverben.utils.Constants.PAGE_SEARCH
import com.xengar.android.deutscheverben.utils.Constants.TYPE_PAGE

import java.util.ArrayList

/**
 * SearchActivity
 */
class SearchActivity : AppCompatActivity() {

    private var mToolbar: Toolbar? = null
    private var mVerbs: MutableList<Verb>? = null // all verbs in the database
    private var mSearchView: SearchView? = null
    private var mAdapter: SearchAdapter? = null

    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        mToolbar = findViewById(R.id.toolbar)
        val mRecyclerView = findViewById<RecyclerView>(R.id.search_recycler_view)
        mSearchView = findViewById(R.id.search_view)
        setupActionBar()
        setupSearchView()

        mVerbs = ArrayList()
        mAdapter = SearchAdapter()
        // Get all verbs
        val fetch = FetchVerbs(contentResolver, mVerbs!!, mAdapter!!.verbs)
        fetch.execute()

        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mRecyclerView.adapter = mAdapter

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(applicationContext)
        ActivityUtils.firebaseAnalyticsLogEventSelectContent(mFirebaseAnalytics!!,
                PAGE_SEARCH, PAGE_SEARCH, TYPE_PAGE)
    }

    private fun setupActionBar() {
        setSupportActionBar(mToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupSearchView() {
        val searchManager = getSystemService(SEARCH_SERVICE) as SearchManager
        mSearchView!!.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        mSearchView!!.isIconified = false
        mSearchView!!.queryHint = getString(R.string.action_search)
        mSearchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                mSearchView!!.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isNotEmpty()) {
                    mAdapter!!.filter.filter(newText)
                    return true
                }

                return false
            }
        })
        mSearchView!!.setOnCloseListener {
            finish()
            true
        }
    }

    /**
     * SearchAdapter
     */
    internal inner class SearchAdapter : RecyclerView.Adapter<VerbHolder>(), Filterable {

        val verbs: MutableList<Verb> = ArrayList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerbHolder {
            val inflater = LayoutInflater.from(parent.context)
            val v = inflater.inflate(R.layout.verbs_list_item, parent, false)
            return VerbHolder(v)
        }

        override fun onBindViewHolder(holder: VerbHolder, position: Int) {
            if (position >= verbs.size || position < 0)
                return

            val item = verbs[position]
            holder.bindVerb(item, LIST, null)
        }

        override fun getItemCount(): Int {
            return verbs.size
        }

        override fun getFilter(): Filter {

            return object : Filter() {
                override fun performFiltering(charSequence: CharSequence): Filter.FilterResults {
                    val results = Filter.FilterResults()
                    val language = ActivityUtils.getPreferenceTranslationLanguage(applicationContext)

                    // Iterate though the list and get the verbs that contain the string
                    verbs.clear()
                    mVerbs!!.filterTo(verbs) {
                        (ActivityUtils.getTranslation(it, language).contains(charSequence)
                                || it.infinitive.contains(charSequence)
                                || it.pastHe.contains(charSequence)
                                || it.presentHe.contains(charSequence)
                                || it.pastParticiple.contains(charSequence))
                    }
                    results.values = verbs
                    results.count = verbs.size

                    ActivityUtils.firebaseAnalyticsLogEventSearch(
                            mFirebaseAnalytics!!, charSequence.toString())
                    ActivityUtils.firebaseAnalyticsLogEventViewSearchResults(
                            mFirebaseAnalytics!!, charSequence.toString())

                    return results
                }

                override fun publishResults(charSequence: CharSequence,
                                            filterResults: Filter.FilterResults) {
                    notifyDataSetChanged()
                }
            }
        }
    }

    /**
     * FetchVerbs from the database.
     */
    inner class FetchVerbs// Constructor
    (private val contentResolver: ContentResolver,
     private val verbs: MutableList<Verb>, // list of all verbs
     private val search: MutableList<Verb> // list of search result verbs
    ) : AsyncTask<Void, Void, ArrayList<Verb>>() {

        private val TAG = FetchVerbs::class.java.simpleName

        override fun doInBackground(vararg voids: Void): ArrayList<Verb> {
            // Define a projection that specifies the columns from the table we care about.
            val columns = ActivityUtils.allVerbColumns()
            val sortOrder = VerbEntry.COLUMN_INFINITIV + " ASC"
            val cursor = contentResolver.query(
                    VerbEntry.CONTENT_VERBS_URI, columns, null, null, sortOrder)

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
                search.addAll(list) // begin search with all verbs in screen.
            }
        }
    }

}
