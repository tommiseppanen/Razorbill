package com.tomtase.razorbill

import android.support.wearable.complications.ProviderChooserIntent
import android.support.wearable.complications.ComplicationProviderInfo
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.os.Bundle
import android.app.Activity
import kotlinx.android.synthetic.main.configuration_recycler_view.*


class ConfigurationActivity : Activity() {
    private var adapter: ConfigurationRecyclerViewAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.configuration_recycler_view)

        adapter = ConfigurationRecyclerViewAdapter()

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {

        if (requestCode == COMPLICATION_CONFIG_REQUEST_CODE && resultCode == RESULT_OK) {

            val complicationProviderInfo =
                data.getParcelableExtra<ComplicationProviderInfo>(ProviderChooserIntent.EXTRA_PROVIDER_INFO)

            adapter!!.updateSelectedComplication(complicationProviderInfo)

        }
    }

    companion object {
        internal const val COMPLICATION_CONFIG_REQUEST_CODE = 1001
    }
}