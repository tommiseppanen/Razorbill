package com.tomtase.razorbill

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.support.wearable.complications.ComplicationProviderInfo




const val TYPE_PREVIEW_AND_COMPLICATIONS_CONFIG = 0

class ConfigurationRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ComplicationLocation {
        TOP,
        RIGHT,
        BOTTOM,
        LEFT
    }

    private var previewViewHolder: PreviewViewHolder? = null


    fun updateSelectedComplication(complicationProviderInfo: ComplicationProviderInfo) {
        if (previewViewHolder != null && previewViewHolder?.selectedComplicationId!! >= 0) {
            previewViewHolder?.updateComplicationViews(
                previewViewHolder?.selectedComplicationId!!, complicationProviderInfo
            )
        }
    }

    override fun getItemCount(): Int {
        return 1
    }

    override fun getItemViewType(position: Int): Int {
        return TYPE_PREVIEW_AND_COMPLICATIONS_CONFIG
    }


    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (viewHolder.itemViewType) {
            TYPE_PREVIEW_AND_COMPLICATIONS_CONFIG -> {
                val previewAndComplicationsViewHolder = viewHolder as PreviewViewHolder

                //val previewAndComplicationsConfigItem = configItemType as ConfigItem

                //val defaultComplicationResourceId = previewAndComplicationsConfigItem.getDefaultComplicationResourceId()
                previewAndComplicationsViewHolder.setDefaultComplicationDrawable(
                    //defaultComplicationResourceId
                    R.drawable.add_complication
                )

                previewAndComplicationsViewHolder.initializeComplications()
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_PREVIEW_AND_COMPLICATIONS_CONFIG -> {
                previewViewHolder = PreviewViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(
                            R.layout.configuration_layout,
                            parent,
                            false
                        )
                )
                return previewViewHolder as PreviewViewHolder
            }
        }
        return PreviewViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.configuration_layout, parent, false))
    }
}