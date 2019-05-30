package com.tomtase.razorbill

import android.support.v7.widget.RecyclerView
import android.support.wearable.complications.ComplicationProviderInfo
import android.widget.ImageButton
import android.support.wearable.complications.ComplicationHelperActivity
import android.content.ComponentName
import com.tomtase.razorbill.ConfigurationRecyclerViewAdapter.ComplicationLocation
import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.support.wearable.complications.ProviderInfoRetriever
import android.view.View
import android.widget.ImageView
import java.util.concurrent.Executors


class PreviewViewHolder : RecyclerView.ViewHolder, View.OnClickListener {
    var selectedComplicationId: Int = 0

    private var watchFace: View? = null

    private var leftComplicationBackground: ImageView? = null
    private var rightComplicationBackground: ImageView? = null

    private var leftComplication: ImageButton? = null
    private var rightComplication: ImageButton? = null

    private var defaultComplicationDrawable: Drawable? = null

    private var providerInfoRetriever: ProviderInfoRetriever? = null
    private var mWatchFaceComponentName: ComponentName? = null
    //private var mContext: Context? = null

    constructor(view: View) : super(view) {

        watchFace = view.findViewById(R.id.watch_face)

        leftComplicationBackground = view.findViewById(R.id.left_complication_background) as ImageView
        leftComplication = view.findViewById(R.id.left_complication)
        leftComplication?.setOnClickListener(this)

        rightComplicationBackground = view.findViewById(R.id.right_complication_background) as ImageView
        rightComplication = view.findViewById(R.id.right_complication)
        rightComplication?.setOnClickListener(this)

        //mContext = view.context
        mWatchFaceComponentName = ComponentName(view.context, "Razorbill")

        providerInfoRetriever = ProviderInfoRetriever(view.context, Executors.newCachedThreadPool())
        providerInfoRetriever?.init()
    }

    /*override*/ fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        //super@PreviewViewHolder.onDetachedFromRecyclerView(recyclerView)
        // Required to release retriever for active complication data on detach.
        providerInfoRetriever?.release()
    }

    override fun onClick(view: View) {
        if (view == leftComplication) {
            val currentActivity = view.context as Activity
            launchComplicationHelperActivity(currentActivity, ComplicationLocation.LEFT)

        } else if (view == rightComplication) {
            val currentActivity = view.context as Activity
            launchComplicationHelperActivity(currentActivity, ComplicationLocation.RIGHT)
        }
    }

    private fun launchComplicationHelperActivity(currentActivity: Activity,
                                                 complicationLocation: ComplicationLocation) {

        selectedComplicationId = Razorbill.getComplicationId(complicationLocation)

        if (selectedComplicationId >= 0) {

            val supportedTypes = Razorbill.getSupportedComplicationTypes(complicationLocation)
            val watchFace = ComponentName(currentActivity, Razorbill::class.java!!)

            currentActivity.startActivityForResult(
                ComplicationHelperActivity.createProviderChooserHelperIntent(
                    currentActivity,
                    watchFace,
                    selectedComplicationId,
                    *supportedTypes
                ),
                ConfigurationActivity.COMPLICATION_CONFIG_REQUEST_CODE
            )

        }
    }

    fun setDefaultComplicationDrawable(resourceId: Int) {
        val context = watchFace?.context
        defaultComplicationDrawable = context?.getDrawable(resourceId)

        leftComplication?.setImageDrawable(defaultComplicationDrawable)
        leftComplicationBackground?.visibility = View.INVISIBLE

        rightComplication?.setImageDrawable(defaultComplicationDrawable)
        rightComplicationBackground?.visibility = View.INVISIBLE
    }

    fun updateComplicationViews(watchFaceComplicationId: Int, complicationProviderInfo: ComplicationProviderInfo?) {

        if (watchFaceComplicationId == Razorbill.getComplicationId(ComplicationLocation.LEFT)) {
            updateComplicationView(complicationProviderInfo, leftComplication, leftComplicationBackground)
        } else if (watchFaceComplicationId == Razorbill.getComplicationId(ComplicationLocation.RIGHT)) {
            updateComplicationView(complicationProviderInfo, rightComplication, rightComplicationBackground)
        }
    }

    private fun updateComplicationView(complicationProviderInfo: ComplicationProviderInfo?,
        button: ImageButton?, background: ImageView?) {
        if (complicationProviderInfo != null) {
            button?.setImageIcon(complicationProviderInfo.providerIcon)
            /*button.contentDescription = mContext.getString(
                R.string.edit_complication,
                complicationProviderInfo.appName + " " +
                        complicationProviderInfo.providerName
            )*/
            background?.visibility = View.VISIBLE
        } else {
            button?.setImageDrawable(defaultComplicationDrawable)
            //button.contentDescription = mContext.getString(R.string.add_complication)
            background?.visibility = View.INVISIBLE
        }
    }

    fun initializeComplications() {

        val complicationIds = Razorbill.getComplicationIds()

        providerInfoRetriever?.retrieveProviderInfo(
            object : ProviderInfoRetriever.OnProviderInfoReceivedCallback() {
                 override fun onProviderInfoReceived(watchFaceComplicationId: Int,
                                                     complicationProviderInfo: ComplicationProviderInfo?) {
                    updateComplicationViews(watchFaceComplicationId, complicationProviderInfo)
                }
            },
            mWatchFaceComponentName,
            *complicationIds
        )
    }
}