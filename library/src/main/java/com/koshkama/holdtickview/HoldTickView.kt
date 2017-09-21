package com.koshkama.holdtickview

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

/**
 * @author Aleksandr Pavlov (vertex55reg@gmail.com)
 */
open class HoldTickView : View {

    private companion object {
        const val PHASE_VISIBLE = 0f
        const val PHASE_INVISIBLE = 1f
        const val START_ANGLE = 315f
        const val DEGREES = 360f
        const val CIRCLE_RATIO = 0.1f
        const val TICK_RATIO = 0.14f
        const val DEFAULT_SHADOW_RADIUS = 4f // in dp
    }

    var isChecked: Boolean = false
        private set(value) {
            field = value
            onCheckedChangeListener?.invoke(value)
        }
    /**
     * A color in the checked state. By default is 0xFF43A047.
     */
    var checkedColor: Int = (0xFF43A047).toInt()
    /**
     * A color in the unchecked state. By default is 0xFF757575.
     */
    var uncheckedColor: Int = (0xFF757575).toInt()
    /**
     * A shadow color. By default is 0x88000000.
     */
    var shadowColor: Int = (0x88000000).toInt()
    /**
     * Shadow radius. By default is 4 dp.
     */
    var shadowRadius: Float = 0f
    /**
     * Tick color. By default is 0xFF43A047.
     */
    var tickColor: Int = (0xFF43A047).toInt()
    /**
     * Tick animation time in milliseconds. By default 200ms.
     */
    var tickAnimationTime: Long = 200L
    /**
     * Time to change state in milliseconds. By default 1000ms.
     */
    var switchingTime: Long = 1000L

    var onCheckedChangeListener: ((isChecked: Boolean) -> Unit)? = null

    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private var tickPhase = PHASE_INVISIBLE
    private val tickPath = Path()
    private var tickWidth = 0f
    private var tickLength = 0f
    private val pathCoords = arrayOf(PointF(0.2f, 0.3f), PointF(0.5f, 0.6f), PointF(0.95f, 0.05f))
    private var tickAnimator = ValueAnimator()

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val animatedCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private var circleSweepAngle = 0f
    private var circleRadius = 0f
    private var circleWidth = 0f
    private val circlePosition = PointF()
    private val circleBounds = RectF()
    private var circleAnimator = ValueAnimator()

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet? = null) {
        if (attrs != null) {
            val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.HoldTickView, 0, 0)
            try {
                isChecked = typedArray.getBoolean(R.styleable.HoldTickView_android_checked, isChecked)
                isEnabled = typedArray.getBoolean(R.styleable.HoldTickView_android_enabled, isEnabled)
                checkedColor = typedArray.getColor(R.styleable.HoldTickView_checkedColor, checkedColor)
                uncheckedColor = typedArray.getColor(R.styleable.HoldTickView_uncheckedColor, uncheckedColor)
                shadowColor = typedArray.getColor(R.styleable.HoldTickView_shadowColor, shadowColor)
                shadowRadius = typedArray.getDimension(R.styleable.HoldTickView_shadowRadius, dpToPx(DEFAULT_SHADOW_RADIUS))
                tickColor = typedArray.getColor(R.styleable.HoldTickView_tickColor, tickColor)
                tickAnimationTime = typedArray.getInteger(R.styleable.HoldTickView_tickAnimationTime, tickAnimationTime.toInt()).toLong()
                switchingTime = typedArray.getInteger(R.styleable.HoldTickView_switchingTime, switchingTime.toInt()).toLong()
            } finally {
                typedArray.recycle()
            }
        }
        tickPhase = if (isChecked) PHASE_VISIBLE else PHASE_INVISIBLE
        setLayerType(LAYER_TYPE_SOFTWARE, tickPaint)
        setLayerType(LAYER_TYPE_SOFTWARE, circlePaint)
    }

    fun setChecked(isChecked: Boolean, animate: Boolean) {
        this.isChecked = isChecked
        if (animate) {
            startTickAnimation()
        } else {
            tickPhase = if (isChecked) PHASE_VISIBLE else PHASE_INVISIBLE
            invalidate()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean = when {
        !isEnabled -> true
        event.action == MotionEvent.ACTION_DOWN -> {
            val switchCallback = {
                isChecked = !isChecked
                stopCircleAnimation()
                startTickAnimation()
            }
            handler.postDelayed(switchCallback, switchingTime)
            startCircleAnimation()
            true
        }
        event.action == MotionEvent.ACTION_UP -> {
            handler.removeCallbacksAndMessages(null)
            stopCircleAnimation()
            true
        }
        else -> super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawCircle(canvas)
        drawAnimatedCircle(canvas)
        drawTick(canvas)
    }

    private fun drawCircle(canvas: Canvas) {
        circlePaint.color = if (isChecked) checkedColor else uncheckedColor
        circlePaint.strokeWidth = circleWidth
        circlePaint.setShadowLayer(shadowRadius, 0f, 0f, shadowColor)
        canvas.drawCircle(circlePosition.x, circlePosition.y, circleRadius, circlePaint)
    }

    private fun drawAnimatedCircle(canvas: Canvas) {
        if (circleSweepAngle == 0f) {
            return
        }
        animatedCirclePaint.color = if (isChecked) uncheckedColor else checkedColor
        animatedCirclePaint.strokeWidth = circleWidth
        canvas.drawArc(circleBounds, START_ANGLE, -circleSweepAngle, false, animatedCirclePaint)
    }

    private fun drawTick(canvas: Canvas) {
        tickPaint.pathEffect = DashPathEffect(floatArrayOf(tickLength, tickLength), tickPhase * tickLength)
        tickPaint.color = tickColor
        tickPaint.strokeWidth = tickWidth
        tickPaint.setShadowLayer(shadowRadius, 0f, 0f, shadowColor)
        canvas.drawPath(tickPath, tickPaint)
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        val viewSize: Float = Math.min(width, height).toFloat()
        recalculateCircleSize(viewSize)
        recalculateTickSize(viewSize)
    }

    private fun recalculateCircleSize(viewSize: Float) {
        circleWidth = viewSize * CIRCLE_RATIO
        circleRadius = Math.max(viewSize * 0.4f - circleWidth * 0.5f - shadowRadius, 0f)
        circlePosition.apply {
            val center = viewSize * 0.5f
            x = center
            y = center
        }
        circleBounds.apply {
            left = circlePosition.x - circleRadius
            top = circlePosition.y - circleRadius
            right = circlePosition.x + circleRadius
            bottom = circlePosition.y + circleRadius
        }
    }

    private fun recalculateTickSize(viewSize: Float) {
        tickWidth = viewSize * TICK_RATIO
        val padding = tickWidth * 0.5f + shadowRadius
        val croppedSize = Math.max(viewSize - padding * 2f, 0f)
        tickPath.reset()
        val calcPosition = { v: Float -> v * croppedSize + padding }
        val firstPoint = pathCoords.first()
        tickPath.moveTo(calcPosition(firstPoint.x), calcPosition(firstPoint.y))
        pathCoords.forEach { point -> tickPath.lineTo(calcPosition(point.x), calcPosition(point.y)) }
        tickLength = PathMeasure(tickPath, false).length
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // make the view square
        val minMeasureSpec = Math.min(widthMeasureSpec, heightMeasureSpec)
        super.onMeasure(minMeasureSpec, minMeasureSpec)
    }

    private fun startTickAnimation() {
        if (tickAnimator.isStarted) {
            tickAnimator.removeAllUpdateListeners()
            tickAnimator.cancel()
            tickAnimator = ValueAnimator()
        }
        when {
            isChecked -> tickAnimator.setFloatValues(PHASE_INVISIBLE, PHASE_VISIBLE)
            else -> tickAnimator.setFloatValues(PHASE_VISIBLE, -PHASE_INVISIBLE)
        }
        tickAnimator.addUpdateListener {
            tickPhase = tickAnimator.animatedValue as Float
            invalidate()
        }
        tickAnimator.duration = tickAnimationTime
        tickAnimator.start()
    }

    private fun startCircleAnimation() {
        if (circleAnimator.isStarted) {
            stopCircleAnimation()
        }
        circleAnimator.setFloatValues(0f, DEGREES)
        circleAnimator.addUpdateListener({
            circleSweepAngle = circleAnimator.animatedValue as Float
            invalidate()
        })
        circleAnimator.duration = switchingTime
        circleAnimator.start()
    }

    private fun stopCircleAnimation() {
        circleSweepAngle = 0f
        circleAnimator.removeAllUpdateListeners()
        circleAnimator.cancel()
        circleAnimator = ValueAnimator()
        invalidate()
    }

    private fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        savedState.isChecked = isChecked
        savedState.isEnabled = isEnabled
        savedState.checkedColor = checkedColor
        savedState.uncheckedColor = uncheckedColor
        savedState.shadowColor = shadowColor
        savedState.shadowRadius = shadowRadius
        savedState.tickColor = tickColor
        savedState.tickAnimationTime = tickAnimationTime
        savedState.switchingTime = switchingTime
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        state as SavedState
        super.onRestoreInstanceState(state.superState)
        isChecked = state.isChecked
        isEnabled = state.isEnabled
        checkedColor = state.checkedColor
        uncheckedColor = state.uncheckedColor
        shadowColor = state.shadowColor
        shadowRadius = state.shadowRadius
        tickColor = state.tickColor
        tickAnimationTime = state.tickAnimationTime
        switchingTime = state.switchingTime
        tickPhase = if (isChecked) PHASE_VISIBLE else PHASE_INVISIBLE
    }

    private class SavedState : View.BaseSavedState {

        var isChecked: Boolean = false
        var isEnabled: Boolean = false
        var checkedColor: Int = 0
        var uncheckedColor: Int = 0
        var shadowColor: Int = 0
        var shadowRadius: Float = 0f
        var tickColor: Int = 0
        var tickAnimationTime: Long = 0L
        var switchingTime: Long = 0L

        constructor(superState: Parcelable) : super(superState)

        private constructor(parcelIn: Parcel) : super(parcelIn) {
            isChecked = parcelIn.readInt() != 0
            isEnabled = parcelIn.readInt() != 0
            checkedColor = parcelIn.readInt()
            uncheckedColor = parcelIn.readInt()
            shadowColor = parcelIn.readInt()
            shadowRadius = parcelIn.readFloat()
            tickColor = parcelIn.readInt()
            tickAnimationTime = parcelIn.readLong()
            switchingTime = parcelIn.readLong()
        }

        override fun writeToParcel(parcelOut: Parcel, flags: Int) {
            super.writeToParcel(parcelOut, flags)
            parcelOut.writeInt(if (isChecked) 1 else 0)
            parcelOut.writeInt(if (isEnabled) 1 else 0)
            parcelOut.writeInt(checkedColor)
            parcelOut.writeInt(uncheckedColor)
            parcelOut.writeInt(shadowColor)
            parcelOut.writeFloat(shadowRadius)
            parcelOut.writeInt(tickColor)
            parcelOut.writeLong(tickAnimationTime)
            parcelOut.writeLong(switchingTime)
        }

        companion object {
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(parcelIn: Parcel): SavedState {
                    return SavedState(parcelIn)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }

    }

}
