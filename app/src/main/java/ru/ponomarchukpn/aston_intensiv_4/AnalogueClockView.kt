package ru.ponomarchukpn.aston_intensiv_4

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

class AnalogueClockView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var calendar: Calendar? = null

    private var baseColor: Int = Color.BLACK
    private var hourColor: Int = Color.BLACK
    private var minuteColor: Int = Color.BLACK
    private var secondColor: Int = Color.BLACK
    private var hourPercent: Float = DEFAULT_HAND_LENGTH_PERCENT
    private var minutePercent: Float = DEFAULT_HAND_LENGTH_PERCENT
    private var secondPercent: Float = DEFAULT_HAND_LENGTH_PERCENT

    init {
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.AnalogueClockView, defStyleAttr, 0)
        baseColor = typedArray.getColor(R.styleable.AnalogueClockView_base_color, Color.BLACK)
        hourColor = typedArray.getColor(R.styleable.AnalogueClockView_hour_color, Color.BLACK)
        minuteColor = typedArray.getColor(R.styleable.AnalogueClockView_minute_color, Color.BLACK)
        secondColor = typedArray.getColor(R.styleable.AnalogueClockView_second_color, Color.BLACK)
        hourPercent = typedArray.getFloatPercent(R.styleable.AnalogueClockView_hour_percent)
        minutePercent = typedArray.getFloatPercent(R.styleable.AnalogueClockView_minute_percent)
        secondPercent = typedArray.getFloatPercent(R.styleable.AnalogueClockView_second_percent)
        typedArray.recycle()
    }

    private val centerX by lazy { width.toFloat() / 2 }
    private val centerY by lazy { height.toFloat() / 2 }

    private val clockRadius by lazy {
        if (width > height) {
            height.toFloat() / 2 * CLOCK_RADIUS_RATIO
        } else {
            width.toFloat() / 2 * CLOCK_RADIUS_RATIO
        }
    }

    private val textSizePx by lazy { clockRadius * TEXT_SIZE_RATIO }
    private val hourPointRadius by lazy { clockRadius * HOUR_POINT_RADIUS_RATIO }
    private val pointsRadius by lazy { clockRadius * POINTS_RADIUS_RATIO }
    private val hourHandWidth by lazy { clockRadius * HOUR_HAND_WIDTH_RATIO }
    private val minuteHandWidth by lazy { clockRadius * MINUTE_HAND_WIDTH_RATIO }
    private val secondHandWidth by lazy { clockRadius * SECOND_HAND_WIDTH_RATIO }
    private val borderWidth by lazy { clockRadius * BORDER_WIDTH_RATIO }

    private val borderPaint by lazy {
        Paint().apply {
            color = baseColor
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = borderWidth
        }
    }

    private val hourHandPaint by lazy {
        Paint().apply {
            color = hourColor
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = hourHandWidth
            strokeCap = Paint.Cap.ROUND
        }
    }

    private val minuteHandPaint by lazy {
        Paint().apply {
            color = minuteColor
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = minuteHandWidth
            strokeCap = Paint.Cap.ROUND
        }
    }

    private val secondHandPaint by lazy {
        Paint().apply {
            color = secondColor
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = secondHandWidth
            strokeCap = Paint.Cap.ROUND
        }
    }

    private val hourPointsPaint by lazy {
        Paint().apply {
            color = baseColor
            isAntiAlias = true
            style = Paint.Style.FILL
        }
    }

    private val digitLabelsPaint by lazy {
        Paint().apply {
            color = baseColor
            isAntiAlias = true
            style = Paint.Style.FILL
            textSize = textSizePx
        }
    }

    fun submitTime(newCalendar: Calendar) {
        calendar = newCalendar
        invalidate()
    }

    fun submitHourPercent(lengthPercent: Float) {
        hourPercent = validatedPercent(lengthPercent)
        invalidate()
    }

    fun submitMinutePercent(lengthPercent: Float) {
        minutePercent = validatedPercent(lengthPercent)
        invalidate()
    }

    fun submitSecondPercent(lengthPercent: Float) {
        secondPercent = validatedPercent(lengthPercent)
        invalidate()
    }

    private fun validatedPercent(lengthPercent: Float): Float {
        return if (lengthPercent > 0 && lengthPercent <= 1) {
            lengthPercent
        } else {
            throw RuntimeException("Value is out of range from 0 to 1: $lengthPercent")
        }
    }

    fun submitHourColor(@ColorInt color: Int) {
        hourColor = color
        hourHandPaint.color = color
        invalidate()
    }

    fun submitMinuteColor(@ColorInt color: Int) {
        minuteColor = color
        minuteHandPaint.color = color
        invalidate()
    }

    fun submitSecondColor(@ColorInt color: Int) {
        secondColor = color
        secondHandPaint.color = color
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawClockBorder(canvas)
        drawHourPoints(canvas)
        drawDigitLabels(canvas)
        drawHands(canvas)
    }

    override fun onSaveInstanceState(): Parcelable {
        return Bundle().apply {
            putParcelable(KEY_INSTANCE_STATE, super.onSaveInstanceState())
            putInt(KEY_HOUR_COLOR, hourColor)
            putInt(KEY_MINUTE_COLOR, minuteColor)
            putInt(KEY_SECOND_COLOR, secondColor)
            putFloat(KEY_HOUR_PERCENT, hourPercent)
            putFloat(KEY_MINUTE_PERCENT, minutePercent)
            putFloat(KEY_SECOND_PERCENT, secondPercent)
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            hourColor = state.getInt(KEY_HOUR_COLOR, Color.BLACK)
            minuteColor = state.getInt(KEY_MINUTE_COLOR, Color.BLACK)
            secondColor = state.getInt(KEY_SECOND_COLOR, Color.BLACK)
            hourPercent = state.getFloat(KEY_HOUR_PERCENT, DEFAULT_HAND_LENGTH_PERCENT)
            minutePercent = state.getFloat(KEY_MINUTE_PERCENT, DEFAULT_HAND_LENGTH_PERCENT)
            secondPercent = state.getFloat(KEY_SECOND_PERCENT, DEFAULT_HAND_LENGTH_PERCENT)
            super.onRestoreInstanceState(state.parcelable(KEY_INSTANCE_STATE))
        }
    }

    private fun drawHands(canvas: Canvas?) {
        val calendarToApply = calendar ?: Calendar.getInstance()
        val hours = calendarToApply.get(Calendar.HOUR)
        val minutes = calendarToApply.get(Calendar.MINUTE)
        val seconds = calendarToApply.get(Calendar.SECOND)
        val hoursAngle = (hours.toDouble() + minutes.toDouble() / MINUTES_IN_HOUR) * HOUR_ANGLE
        val minutesAngle = minutes * STEP_ANGLE
        val secondsAngle = seconds * STEP_ANGLE

        drawHand(canvas, hoursAngle, hourPercent, hourHandPaint)
        drawHand(canvas, minutesAngle, minutePercent, minuteHandPaint)
        drawHand(canvas, secondsAngle, secondPercent, secondHandPaint)
    }

    private fun drawHand(canvas: Canvas?, angle: Double, lengthPercent: Float, paint: Paint) {
        val radius = pointsRadius * lengthPercent
        val endX = centerX + radius * cos(Math.toRadians(angle - RIGHT_ANGLE)).toFloat()
        val endY = centerY + radius * sin(Math.toRadians(angle - RIGHT_ANGLE)).toFloat()
        canvas?.drawLine(centerX, centerY, endX, endY, paint)
    }

    private fun drawClockBorder(canvas: Canvas?) {
        canvas?.drawCircle(centerX, centerY, clockRadius, borderPaint)
    }

    private fun drawHourPoints(canvas: Canvas?) {
        IntRange(1, 12).forEach {
            drawHourPoint(it, canvas)
        }
    }

    private fun drawHourPoint(number: Int, canvas: Canvas?) {
        val angle = number * HOUR_ANGLE
        val pointX = centerX + pointsRadius * cos(Math.toRadians(angle - RIGHT_ANGLE)).toFloat()
        val pointY = centerY + pointsRadius * sin(Math.toRadians(angle - RIGHT_ANGLE)).toFloat()
        canvas?.drawCircle(pointX, pointY, hourPointRadius, hourPointsPaint)
    }

    private fun drawDigitLabels(canvas: Canvas?) {
        IntRange(1, 12).forEach {
            drawDigitLabel(it, canvas)
        }
    }

    private fun drawDigitLabel(number: Int, canvas: Canvas?) {
        val angle = number * HOUR_ANGLE
        val digitsRadius = clockRadius * DIGITS_RADIUS_RATIO
        val pointX = centerX + digitsRadius * cos(Math.toRadians(angle - RIGHT_ANGLE)).toFloat()
        val pointY = centerY + digitsRadius * sin(Math.toRadians(angle - RIGHT_ANGLE)).toFloat()
        val numberStr = number.toString()
        val half = textSizePx / 2
        val quarter = textSizePx / 4

        when (number) {
            1, 2 -> canvas?.drawText(numberStr, pointX, pointY, digitLabelsPaint)
            3 -> canvas?.drawText(numberStr, pointX, pointY + quarter, digitLabelsPaint)
            4, 5 -> canvas?.drawText(numberStr, pointX, pointY + half, digitLabelsPaint)
            6 -> canvas?.drawText(numberStr, pointX - quarter, pointY + half, digitLabelsPaint)
            7, 8 -> canvas?.drawText(numberStr, pointX - half, pointY + half, digitLabelsPaint)
            9 -> canvas?.drawText(numberStr, pointX - half, pointY + quarter, digitLabelsPaint)
            10, 11 -> canvas?.drawText(numberStr, pointX - half, pointY, digitLabelsPaint)
            12 -> canvas?.drawText(numberStr, pointX - half, pointY, digitLabelsPaint)
            else -> throw RuntimeException("Unknown number: $number")
        }
    }

    companion object {

        private const val DEFAULT_HAND_LENGTH_PERCENT = 1F
        private const val CLOCK_RADIUS_RATIO = 0.9F
        private const val TEXT_SIZE_RATIO = 0.15F
        private const val HOUR_POINT_RADIUS_RATIO = 0.03F
        private const val POINTS_RADIUS_RATIO = 0.7F
        private const val HOUR_HAND_WIDTH_RATIO = 0.1F
        private const val MINUTE_HAND_WIDTH_RATIO = 0.05F
        private const val SECOND_HAND_WIDTH_RATIO = 0.025F
        private const val BORDER_WIDTH_RATIO = 0.05F
        private const val DIGITS_RADIUS_RATIO = 0.8F
        private const val MINUTES_IN_HOUR = 60
        private const val HOUR_ANGLE = 30.0
        private const val RIGHT_ANGLE = 90.0
        private const val STEP_ANGLE = 6.0

        private const val KEY_HOUR_COLOR = "hourColor"
        private const val KEY_MINUTE_COLOR = "minuteColor"
        private const val KEY_SECOND_COLOR = "secondColor"
        private const val KEY_HOUR_PERCENT = "hourPercent"
        private const val KEY_MINUTE_PERCENT = "minutePercent"
        private const val KEY_SECOND_PERCENT = "secondPercent"
        private const val KEY_INSTANCE_STATE = "instanceState"
    }
}
