package com.xengar.android.deutscheverben.ui


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.xengar.android.deutscheverben.R
import com.xengar.android.deutscheverben.adapter.VerbAdapter
import com.xengar.android.deutscheverben.data.Verb
import com.xengar.android.deutscheverben.sync.FetchVerbs
import com.xengar.android.deutscheverben.utils.Constants.ALPHABET
import com.xengar.android.deutscheverben.utils.Constants.COMMON_TYPE
import com.xengar.android.deutscheverben.utils.Constants.TYPE_ALL
import com.xengar.android.deutscheverben.utils.Constants.ITEM_TYPE
import com.xengar.android.deutscheverben.utils.Constants.LIST
import com.xengar.android.deutscheverben.utils.Constants.MOST_COMMON_ALL
import com.xengar.android.deutscheverben.utils.Constants.SORT_TYPE
import com.xengar.android.deutscheverben.utils.Constants.VERB_TYPE
import com.xengar.android.deutscheverben.utils.CustomErrorView
import com.xengar.android.deutscheverben.utils.FragmentUtils
import fr.castorflex.android.circularprogressbar.CircularProgressBar
import java.util.ArrayList


/**
 * UniversalFragment
 */
class UniversalFragment : Fragment() {

    private var mCustomErrorView: CustomErrorView? = null
    private var mRecyclerView: RecyclerView? = null
    private var progressBar: CircularProgressBar? = null
    private var mAdapter: VerbAdapter? = null
    private var mVerbs: MutableList<Verb>? = null
    var verbGroup = TYPE_ALL
        private set   // 1er type, 2nd type, 3rd type, all groups
    var sortType = ALPHABET
        private set     // alphabet, color, type
    private var itemType = LIST         // card, list
    var commonType = MOST_COMMON_ALL
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        if (arguments != null) {
            verbGroup = arguments!!.getString(VERB_TYPE, TYPE_ALL)
            itemType = arguments!!.getString(ITEM_TYPE, LIST)
            sortType = arguments!!.getString(SORT_TYPE, ALPHABET)
            commonType = arguments!!.getString(COMMON_TYPE, MOST_COMMON_ALL)
        }

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_universal, container, false)

        mCustomErrorView = view.findViewById(R.id.error)
        mRecyclerView = view.findViewById(R.id.recycler)
        progressBar = view.findViewById(R.id.progressBar)
        mVerbs = ArrayList()

        val tts = (activity as MainActivity).tts
        mAdapter = VerbAdapter(mVerbs, itemType, tts)

        return view
    }


    override fun onResume() {
        super.onResume()

        /*
        if (!FragmentUtils.checkInternetConnection(getActivity())) {
            if (LOG) {
                Log.e(TAG, "Network is not available");
            }
            onLoadFailed(new Throwable(getString(R.string.network_not_available_message)));
            return;
        }*/

        mVerbs?.clear()
        fillVerbs()
    }

    private fun onLoadFailed(t: Throwable) {
        mCustomErrorView?.setError(t)
        mCustomErrorView?.visibility = View.VISIBLE
        FragmentUtils.updateProgressBar(progressBar, false)
    }

    private fun fillVerbs() {
        mRecyclerView!!.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL,
                false)
        mRecyclerView!!.adapter = mAdapter
        FragmentUtils.updateProgressBar(progressBar, true)

        val fetch = FetchVerbs(verbGroup, sortType, commonType, mAdapter!!,
                activity!!.contentResolver, mVerbs!!, progressBar!!)
        fetch.execute()
    }

}// Required empty public constructor
