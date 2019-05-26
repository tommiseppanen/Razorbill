package com.tomtase.razorbill

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.wearable.watchface.CanvasWatchFaceService
import android.support.wearable.watchface.WatchFaceService
import android.support.wearable.watchface.WatchFaceStyle
import android.view.SurfaceHolder

import java.lang.ref.WeakReference
import java.util.Calendar
import java.util.TimeZone
import android.support.wearable.complications.rendering.ComplicationDrawable
import android.support.wearable.complications.ComplicationData
import com.tomtase.razorbill.ConfigurationRecyclerViewAdapter.*
import android.util.SparseArray


private const val UPDATE_TIME_MESSAGE_ID = 0
private const val INTERACTIVE_MODE_UPDATE_RATE = 1000

private const val HOUR_STROKE_WIDTH = 3f
private const val MINUTE_STROKE_WIDTH = 3f
private const val SECOND_TICK_STROKE_WIDTH = 2f
private const val CENTER_GAP_AND_CIRCLE_RADIUS = 8f
private const val LONG_TICK_LENGTH = 15
private const val SHORT_TICK_LENGTH = 8


class Razorbill : CanvasWatchFaceService() {


    private val RIGHT_COMPLICATION_ID = 100
    private val BOTTOM_COMPLICATION_ID = 101
    private val LEFT_COMPLICATION_ID = 102

    private val COMPLICATION_IDS = intArrayOf(RIGHT_COMPLICATION_ID, BOTTOM_COMPLICATION_ID, LEFT_COMPLICATION_ID)

    private val SUPPORTED_TYPES = intArrayOf(
        ComplicationData.TYPE_RANGED_VALUE,
        ComplicationData.TYPE_ICON,
        ComplicationData.TYPE_SHORT_TEXT,
        ComplicationData.TYPE_SMALL_IMAGE
    )

    private val COMPLICATION_SUPPORTED_TYPES = arrayOf(
        SUPPORTED_TYPES, SUPPORTED_TYPES, SUPPORTED_TYPES
    )

    fun getComplicationId(
        complicationLocation: ComplicationLocation
    ): Int {
        when (complicationLocation) {
            ComplicationLocation.RIGHT -> return RIGHT_COMPLICATION_ID
            ComplicationLocation.BOTTOM -> return BOTTOM_COMPLICATION_ID
            ComplicationLocation.LEFT -> return LEFT_COMPLICATION_ID
            else -> return -1
        }
    }

    fun getComplicationIds(): IntArray {
        return COMPLICATION_IDS
    }

    fun getSupportedComplicationTypes(
        complicationLocation: ComplicationLocation
    ): IntArray {
        when (complicationLocation) {
            ComplicationLocation.RIGHT -> return COMPLICATION_SUPPORTED_TYPES[0]
            ComplicationLocation.BOTTOM  -> return COMPLICATION_SUPPORTED_TYPES[1]
            ComplicationLocation.LEFT -> return COMPLICATION_SUPPORTED_TYPES[2]
            else -> return intArrayOf()
        }
    }

    override fun onCreateEngine(): RazorbillEngine {
        return RazorbillEngine()
    }

    private class EngineHandler(reference: RazorbillEngine) : Handler() {
        private val mWeakReference: WeakReference<RazorbillEngine> = WeakReference(reference)

        override fun handleMessage(msg: Message) {
            val engine = mWeakReference.get()
            if (engine != null) {
                when (msg.what) {
                    UPDATE_TIME_MESSAGE_ID -> engine.handleUpdateTimeMessage()
                }
            }
        }
    }

    inner class RazorbillEngine : CanvasWatchFaceService.Engine() {

        private lateinit var calendar: Calendar

        private var registeredTimeZoneReceiver = false
        private var muteMode: Boolean = false
        private var centerX: Float = 0F
        private var centerY: Float = 0F

        private var secondHandLength: Float = 0F
        private var minuteHandLength: Float = 0F
        private var hourHandLength: Float = 0F

        private var handColor: Int = Color.WHITE
        private var secondHandColor: Int = Color.RED
        private var tickColor: Int = Color.GRAY

        private lateinit var hourPaint: Paint
        private lateinit var minutePaint: Paint
        private lateinit var secondPaint: Paint
        private lateinit var tickPaint: Paint

        private lateinit var backgroundPaint: Paint

        private var ambient: Boolean = false
        private var lowBitAmbient: Boolean = false
        private var burnInProtection: Boolean = false

        private val updateTimeHandler = EngineHandler(this)

        private val timeZoneReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                calendar.timeZone = TimeZone.getDefault()
                invalidate()
            }
        }

        private var complicationDrawables: SparseArray<ComplicationDrawable>? = null

        override fun onCreate(holder: SurfaceHolder) {
            super.onCreate(holder)

            setWatchFaceStyle(WatchFaceStyle.Builder(this@Razorbill)
                    .setAcceptsTapEvents(true)
                    .build())

            calendar = Calendar.getInstance()
            initializePaints()
            initializeComplications()
        }

        private fun initializePaints() {
            backgroundPaint = Paint().apply {
                color = Color.BLACK
            }

            hourPaint = Paint().apply {
                color = handColor
                strokeWidth = HOUR_STROKE_WIDTH
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
            }

            minutePaint = Paint().apply {
                color = handColor
                strokeWidth = MINUTE_STROKE_WIDTH
                isAntiAlias = true
                strokeCap = Paint.Cap.ROUND
            }

            secondPaint = Paint().apply {
                color = secondHandColor
                strokeWidth = SECOND_TICK_STROKE_WIDTH
                isAntiAlias = true
                strokeCap = Paint.Cap.ROUND
            }

            tickPaint = Paint().apply {
                color = tickColor
                strokeWidth = SECOND_TICK_STROKE_WIDTH
                isAntiAlias = true
                style = Paint.Style.STROKE
            }
        }

        private fun initializeComplications() {
            complicationDrawables = SparseArray(COMPLICATION_IDS.count())

            complicationDrawables?.put(RIGHT_COMPLICATION_ID, ComplicationDrawable(applicationContext))
            complicationDrawables?.put(BOTTOM_COMPLICATION_ID, ComplicationDrawable(applicationContext))
            complicationDrawables?.put(LEFT_COMPLICATION_ID, ComplicationDrawable(applicationContext))

            setActiveComplications(*COMPLICATION_IDS)
        }

        override fun onDestroy() {
            updateTimeHandler.removeMessages(UPDATE_TIME_MESSAGE_ID)
            super.onDestroy()
        }

        override fun onPropertiesChanged(properties: Bundle) {
            super.onPropertiesChanged(properties)
            lowBitAmbient = properties.getBoolean(
                    WatchFaceService.PROPERTY_LOW_BIT_AMBIENT, false)
            burnInProtection = properties.getBoolean(
                    WatchFaceService.PROPERTY_BURN_IN_PROTECTION, false)

            for (id in COMPLICATION_IDS) {
                val complicationDrawable = complicationDrawables?.get(id)
                complicationDrawable?.setLowBitAmbient(lowBitAmbient)
                complicationDrawable?.setBurnInProtection(burnInProtection)
            }
        }

        override fun onComplicationDataUpdate(complicationId: Int, complicationData: ComplicationData?) {
            val complicationDrawable = complicationDrawables?.get(complicationId)
            complicationDrawable?.setComplicationData(complicationData)
            invalidate()
        }

        override fun onTapCommand(tapType: Int, x: Int, y: Int, eventTime: Long) {
            when (tapType) {
                TAP_TYPE_TAP ->
                    for (id in COMPLICATION_IDS) {
                        val complicationDrawable = complicationDrawables?.get(id)
                        val successfulTap = complicationDrawable?.onTap(x, y)
                        if (successfulTap == true) {
                            return
                        }
                    }
            }
        }

        override fun onTimeTick() {
            super.onTimeTick()
            invalidate()
        }

        override fun onAmbientModeChanged(inAmbientMode: Boolean) {
            super.onAmbientModeChanged(inAmbientMode)
            ambient = inAmbientMode

            for (id in COMPLICATION_IDS) {
                val complicationDrawable = complicationDrawables?.get(id)
                complicationDrawable?.setInAmbientMode(ambient)
            }

            updateWatchHandStyle()
            updateTimer()
        }

        private fun updateWatchHandStyle() {
            if (ambient) {
                hourPaint.isAntiAlias = false
                minutePaint.isAntiAlias = false
                secondPaint.isAntiAlias = false
                tickPaint.isAntiAlias = false
                secondPaint.color = handColor

            } else {
                hourPaint.isAntiAlias = true
                minutePaint.isAntiAlias = true
                secondPaint.isAntiAlias = true
                tickPaint.isAntiAlias = true
                secondPaint.color = secondHandColor
            }
        }

        override fun onInterruptionFilterChanged(interruptionFilter: Int) {
            super.onInterruptionFilterChanged(interruptionFilter)
            val inMuteMode = interruptionFilter == WatchFaceService.INTERRUPTION_FILTER_NONE

            if (muteMode != inMuteMode) {
                muteMode = inMuteMode
                hourPaint.alpha = if (inMuteMode) 100 else 255
                minutePaint.alpha = if (inMuteMode) 100 else 255
                secondPaint.alpha = if (inMuteMode) 100 else 255
                tickPaint.alpha = if (inMuteMode) 100 else 255
                invalidate()
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)

            centerX = width / 2f
            centerY = height / 2f

            secondHandLength = centerX
            minuteHandLength = (centerX * 0.98).toFloat()
            hourHandLength = (centerX * 0.65).toFloat()
        }

        override fun onDraw(canvas: Canvas, bounds: Rect) {
            val now = System.currentTimeMillis()
            calendar.timeInMillis = now

            //Clear the background
            canvas.drawColor(Color.BLACK)
            drawComplications(canvas, now);
            drawWatchFace(canvas)
        }

        private fun drawWatchFace(canvas: Canvas) {
            val outerTickRadius = centerX
            for (tickIndex in 0..11) {
                var innerTickRadius = if (tickIndex % 3 == 0) { centerX - LONG_TICK_LENGTH }
                    else {centerX - SHORT_TICK_LENGTH}
                val tickRot = (tickIndex.toDouble() * Math.PI * 2.0 / 12).toFloat()
                val innerX = Math.sin(tickRot.toDouble()).toFloat() * innerTickRadius
                val innerY = (-Math.cos(tickRot.toDouble())).toFloat() * innerTickRadius
                val outerX = Math.sin(tickRot.toDouble()).toFloat() * outerTickRadius
                val outerY = (-Math.cos(tickRot.toDouble())).toFloat() * outerTickRadius
                canvas.drawLine(centerX + innerX, centerY + innerY,
                        centerX + outerX, centerY + outerY, tickPaint)
            }

            val seconds =
                    calendar.get(Calendar.SECOND) + calendar.get(Calendar.MILLISECOND) / 1000f
            val secondsRotation = seconds * 6f
            val minutesRotation = calendar.get(Calendar.MINUTE) * 6f
            val hourHandOffset = calendar.get(Calendar.MINUTE) / 2f
            val hoursRotation = calendar.get(Calendar.HOUR) * 30 + hourHandOffset

            canvas.save()
            canvas.rotate(hoursRotation, centerX, centerY)
            drawHand(canvas, hourHandLength, 7f, hourPaint)
            canvas.rotate(minutesRotation - hoursRotation, centerX, centerY)
            drawHand(canvas, minuteHandLength, 5f, minutePaint)

            if (!ambient) {
                canvas.rotate(secondsRotation - minutesRotation, centerX, centerY)
                canvas.drawLine(
                        centerX,
                        centerY - CENTER_GAP_AND_CIRCLE_RADIUS,
                        centerX,
                        centerY - secondHandLength,
                        secondPaint)

            }
            canvas.drawCircle(
                    centerX,
                    centerY,
                    CENTER_GAP_AND_CIRCLE_RADIUS,
                    secondPaint)

            //Restore the canvas' orientation
            canvas.restore()
        }

        private fun drawHand(canvas: Canvas, handLength: Float, endCapRadius: Float, paint: Paint) {
            canvas.drawRoundRect(
                centerX - endCapRadius,
                centerY - handLength, centerX + endCapRadius,
                centerY + endCapRadius, endCapRadius,
                endCapRadius, paint
            )
        }

        private fun drawComplications(canvas: Canvas, currentTimeMillis: Long) {
            for (id in COMPLICATION_IDS) {
                val complicationDrawable = complicationDrawables?.get(id)
                complicationDrawable?.draw(canvas, currentTimeMillis)
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)

            if (visible) {
                registerTimeZoneReceiver()
                calendar.timeZone = TimeZone.getDefault()
                invalidate()
            } else {
                unregisterTimeZoneReceiver()
            }

            updateTimer()
        }

        private fun registerTimeZoneReceiver() {
            if (registeredTimeZoneReceiver) {
                return
            }
            registeredTimeZoneReceiver = true
            val filter = IntentFilter(Intent.ACTION_TIMEZONE_CHANGED)
            this@Razorbill.registerReceiver(timeZoneReceiver, filter)
        }

        private fun unregisterTimeZoneReceiver() {
            if (!registeredTimeZoneReceiver) {
                return
            }
            registeredTimeZoneReceiver = false
            this@Razorbill.unregisterReceiver(timeZoneReceiver)
        }

        private fun updateTimer() {
            updateTimeHandler.removeMessages(UPDATE_TIME_MESSAGE_ID)
            if (timerEnabled()) {
                updateTimeHandler.sendEmptyMessage(UPDATE_TIME_MESSAGE_ID)
            }
        }

        private fun timerEnabled(): Boolean {
            return isVisible && !ambient
        }

        fun handleUpdateTimeMessage() {
            invalidate()
            if (timerEnabled()) {
                val timeMs = System.currentTimeMillis()
                val delayMs = INTERACTIVE_MODE_UPDATE_RATE - timeMs % INTERACTIVE_MODE_UPDATE_RATE
                updateTimeHandler.sendEmptyMessageDelayed(UPDATE_TIME_MESSAGE_ID, delayMs)
            }
        }
    }
}


