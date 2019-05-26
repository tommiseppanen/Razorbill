package com.tomtase.razorbill

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.R
import android.view.LayoutInflater


const val TYPE_PREVIEW_AND_COMPLICATIONS_CONFIG = 0

class ConfigurationRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ComplicationLocation {
        TOP,
        RIGHT,
        BOTTOM,
        LEFT
    }

    //private val settings: ArrayList<ConfigItemType>? = null
    private var previewViewHolder: PreviewViewHolder? = null

    override fun getItemCount(): Int {
        /*if (settings != null) {
            return settings.count()
        }
        return 0*/
        return 1
    }

    override fun getItemViewType(position: Int): Int {
        //val configItemType = settings.get(position)
        //return configItemType.getConfigType()
        return TYPE_PREVIEW_AND_COMPLICATIONS_CONFIG
    }


    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        //val configItemType = settings.get(position)

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
                            R.layout.config_list_preview,
                            parent,
                            false
                        )
                )
                return previewViewHolder as PreviewViewHolder
            }
        }
        return null;
    }
}