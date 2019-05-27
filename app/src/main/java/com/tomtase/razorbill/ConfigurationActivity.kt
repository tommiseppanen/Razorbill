package com.tomtase.razorbill

import android.support.wearable.complications.ProviderChooserIntent
import android.support.wearable.complications.ComplicationProviderInfo
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.R
import android.support.wearable.view.WearableRecyclerView
import android.os.Bundle
import android.app.Activity


class ConfigurationActivity : Activity() {

    private var mWearableRecyclerView: WearableRecyclerView? = null
    private var mAdapter: ConfigurationRecyclerViewAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.complication_config)

        mAdapter = ConfigurationRecyclerViewAdapter()

        mWearableRecyclerView = findViewById(R.id.wearable_recycler_view) as WearableRecyclerView

        //mWearableRecyclerView!!.setEdgeItemsCenteringEnabled(true)

        mWearableRecyclerView!!.layoutManager = LinearLayoutManager(this)
        mWearableRecyclerView!!.setHasFixedSize(true)
        mWearableRecyclerView!!.adapter = mAdapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {

        if (requestCode == COMPLICATION_CONFIG_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            val complicationProviderInfo =
                data.getParcelableExtra<ComplicationProviderInfo>(ProviderChooserIntent.EXTRA_PROVIDER_INFO)

            //mAdapter!!.updateSelectedComplication(complicationProviderInfo)

        }
    }

    companion object {

        private val TAG = ConfigurationActivity::class.java.simpleName

        internal val COMPLICATION_CONFIG_REQUEST_CODE = 1001
    }
}