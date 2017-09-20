package com.koshkama.holdtickview

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * @author Aleksandr Pavlov
 */
open class HoldTickView : View {

    private companion object {
        const val PHASE_VISIBLE = 0f
        const val PHASE_INVISIBLE = 1f
        const val START_ANGLE = 315f
        const val DEGREES = 360f
    }

    /**
     * A color in the checked state. By default 0xFF43A047.
     */
    var checkedColor: Int = (0xFF43A047).toInt()
    /**
     * A color in the unchecked state. By default 0xFF757575.
     */
    var uncheckedColor: Int = (0xFF757575).toInt()

    var shadowColor: Int = Color.BLACK
    /**
     * Time to change state in milliseconds. By default 1000ms.
     */
    var switchingTime: Long = 1000L

    var shadowRadius: Float = 16f

    var isChecked: Boolean = false

    private val tickRatio = 8f

    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private var tickPhase = PHASE_INVISIBLE
    private val tickPath = Path()
    private var tickWidth = 0f
    private var tickLength = 0f
    private val pathCoords = arrayOf(PointF(0f, 0.3f), PointF(0.4f, 0.7f), PointF(1f, 0f))
    private var tickAnimator = ValueAnimator()

    private val circleRatio = 10f
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
                checkedColor = typedArray.getColor(R.styleable.HoldTickView_checkedColor, checkedColor)
                uncheckedColor = typedArray.getColor(R.styleable.HoldTickView_uncheckedColor, uncheckedColor)
                switchingTime = typedArray.getInteger(R.styleable.HoldTickView_switchingTime, switchingTime.toInt()).toLong()
            } finally {
                typedArray.recycle()
            }
        }
        tickPhase = if (isChecked) PHASE_VISIBLE else PHASE_INVISIBLE
        setLayerType(LAYER_TYPE_SOFTWARE, tickPaint)
        setLayerType(LAYER_TYPE_SOFTWARE, circlePaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean = when (event.action) {
        MotionEvent.ACTION_DOWN -> {
            val callback = {
                isChecked = !isChecked
                stopCircleAnimation()
                startTickAnimation()
            }
            handler.postDelayed(callback, switchingTime)
            startCircleAnimation()
            true
        }
        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
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
        tickPaint.color = checkedColor
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
        circleWidth = viewSize / circleRatio
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
        tickWidth = viewSize / tickRatio
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
        tickAnimator.duration = 200
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

}
