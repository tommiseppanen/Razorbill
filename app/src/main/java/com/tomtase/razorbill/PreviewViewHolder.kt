package com.tomtase.razorbill

import android.support.v7.widget.RecyclerView
import android.support.wearable.complications.ComplicationProviderInfo
import android.widget.ImageButton
import android.support.wearable.complications.ComplicationHelperActivity
import android.content.ComponentName
import com.tomtase.razorbill.ConfigurationRecyclerViewAdapter.ComplicationLocation
import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView


class PreviewViewHolder : RecyclerView.ViewHolder {
    private var mWatchFaceArmsAndTicksView: View? = null
    private var mWatchFaceHighlightPreviewView: View? = null

    private var mLeftComplicationBackground: ImageView? = null
    private var mRightComplicationBackground: ImageView? = null

    private var mLeftComplication: ImageButton? = null
    private var mRightComplication: ImageButton? = null

    private var mDefaultComplicationDrawable: Drawable? = null

    constructor(view: View) : super(view) {

        mWatchFaceArmsAndTicksView = view.findViewById(R.id.watch_face_arms_and_ticks)

        // In our case, just the second arm.
        //mWatchFaceHighlightPreviewView = view.findViewById(R.id.watch_face_highlight)

        // Sets up left complication preview.
        mLeftComplicationBackground = view.findViewById(R.id.left_complication_background) as ImageView
        mLeftComplication = view.findViewById(R.id.left_complication)
        //mLeftComplication?.setOnClickListener(this)

        // Sets up right complication preview.
        mRightComplicationBackground = view.findViewById(R.id.right_complication_background) as ImageView
        mRightComplication = view.findViewById(R.id.right_complication)
        //mRightComplication?.setOnClickListener(this)
    }

    fun onClick(view: View) {
        if (view == mLeftComplication) {
            val currentActivity = view.context as Activity
            launchComplicationHelperActivity(currentActivity, ComplicationLocation.LEFT)

        } else if (view == mRightComplication) {
            val currentActivity = view.context as Activity
            launchComplicationHelperActivity(currentActivity, ComplicationLocation.RIGHT)
        }
    }

    private fun launchComplicationHelperActivity(
        currentActivity: Activity, complicationLocation: ComplicationLocation
    ) {

        val selectedComplicationId = Razorbill.getComplicationId(complicationLocation)

        if (selectedComplicationId >= 0) {

            val supportedTypes = Razorbill.getSupportedComplicationTypes(complicationLocation)

            val watchFace = ComponentName(
                currentActivity, Razorbill::class.java!!
            )

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
        val context = mWatchFaceArmsAndTicksView?.context
        mDefaultComplicationDrawable = context?.getDrawable(resourceId)

        mLeftComplication?.setImageDrawable(mDefaultComplicationDrawable)
        mLeftComplicationBackground?.visibility = View.INVISIBLE

        mRightComplication?.setImageDrawable(mDefaultComplicationDrawable)
        mRightComplicationBackground?.visibility = View.INVISIBLE
    }

    /*fun updateComplicationViews(watchFaceComplicationId: Int, complicationProviderInfo: ComplicationProviderInfo?) {

        if (watchFaceComplicationId == Razorbill.getComplicationId(ComplicationLocation.LEFT)) {
            updateComplicationView(
                complicationProviderInfo, mLeftComplication,
                mLeftComplicationBackground
            )

        } else if (watchFaceComplicationId == Razorbill.getComplicationId(ComplicationLocation.RIGHT)) {
            updateComplicationView(
                complicationProviderInfo, mRightComplication,
                mRightComplicationBackground
            )
        }
    }*/

    private fun updateComplicationView(
        complicationProviderInfo: ComplicationProviderInfo?,
        button: ImageButton, background: ImageView
    ) {
        if (complicationProviderInfo != null) {
            button.setImageIcon(complicationProviderInfo.providerIcon)
            /*button.contentDescription = mContext.getString(
                R.string.edit_complication,
                complicationProviderInfo.appName + " " +
                        complicationProviderInfo.providerName
            )*/
            background.setVisibility(View.VISIBLE)
        } else {
            button.setImageDrawable(mDefaultComplicationDrawable)
            //button.contentDescription = mContext.getString(R.string.add_complication)
            background.setVisibility(View.INVISIBLE)
        }
    }

    fun initializeComplications() {

        /*val complicationIds = Razorbill.getComplicationIds()

        providerInfoRetriever.retrieveProviderInfo(
            object : OnProviderInfoReceivedCallback() {
                override fun onProviderInfoReceived(
                    watchFaceComplicationId: Int,
                    complicationProviderInfo: ComplicationProviderInfo?
                ) {


                    updateComplicationViews(
                        watchFaceComplicationId, complicationProviderInfo
                    )
                }
            },
            mWatchFaceComponentName,
            complicationIds
        )*/
    }
}