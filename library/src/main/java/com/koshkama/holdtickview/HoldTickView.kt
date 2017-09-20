package com.koshkama.holdtickview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

/**
 * @author Aleksandr Pavlov
 */
open class HoldTickView : View {

    private companion object {
        const val PHASE_VISIBLE = 0f
        const val PHASE_INVISIBLE = 1f
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
    var switchingTime: Int = 1000

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
    private var circleRadius = 0f
    private var circleWidth = 0f
    private val circlePosition = PointF()

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
                switchingTime = typedArray.getInteger(R.styleable.HoldTickView_switchingTime, switchingTime)
            } finally {
                typedArray.recycle()
            }
        }
        tickPhase = if (isChecked) PHASE_VISIBLE else PHASE_INVISIBLE
        setLayerType(LAYER_TYPE_SOFTWARE, tickPaint)
        setLayerType(LAYER_TYPE_SOFTWARE, circlePaint)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawCircle(canvas)
        drawTick(canvas)
    }

    private fun drawCircle(canvas: Canvas) {
        circlePaint.color = if (isChecked) checkedColor else uncheckedColor
        circlePaint.strokeWidth = circleWidth
        circlePaint.setShadowLayer(shadowRadius, 0f, 0f, shadowColor)
        canvas.drawCircle(circlePosition.x, circlePosition.y, circleRadius, circlePaint)
    }

    private fun drawTick(canvas: Canvas) {
        applyTickDashEffect(tickPhase)
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

    fun animateTick(toShow: Boolean) {
        if (tickAnimator.isStarted) {
            tickAnimator.removeAllUpdateListeners()
            tickAnimator.cancel()
            tickAnimator = ValueAnimator()
        }
        when {
            toShow -> tickAnimator.setFloatValues(1f, 0f)
            else -> tickAnimator.setFloatValues(0f, 1f)
        }
        tickAnimator.addUpdateListener(this::onTickAnimationUpdate)
        tickAnimator.duration = 200
        tickAnimator.start()
    }

    private fun onTickAnimationUpdate(animator: ValueAnimator) {
        tickPhase = animator.animatedValue as Float
        invalidate()
    }

    private fun applyTickDashEffect(phase: Float) {
        tickPaint.pathEffect = DashPathEffect(floatArrayOf(tickLength, tickLength), phase * tickLength)
    }

}
