package com.koshkama.holdcheckbox

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

/**
 * @author Aleksandr Pavlov
 */
open class HoldCheckBox : View {

    private companion object {
        const val PHASE_VISIBLE = 0f
        const val PHASE_INVISIBLE = 1f
    }

    /**
     * A color in the checked state. By default [Color.GREEN].
     */
    var checkedColor: Int = Color.GREEN
    /**
     * A color in the unchecked state. By default [Color.GRAY].
     */
    var uncheckedColor: Int = Color.GRAY

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
    private var tickLength: Float = 0f
    private val pathCoords = arrayOf(PointF(0f, 0.6f), PointF(0.3f, 0.9f), PointF(1f, 0.2f))
    private var tickAnimator = ValueAnimator()

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
            val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.HoldCheckBox, 0, 0)
            try {
                checkedColor = typedArray.getColor(R.styleable.HoldCheckBox_checkedColor, checkedColor)
                uncheckedColor = typedArray.getColor(R.styleable.HoldCheckBox_uncheckedColor, uncheckedColor)
                switchingTime = typedArray.getInteger(R.styleable.HoldCheckBox_switchingTime, switchingTime)
            } finally {
                typedArray.recycle()
            }
        }
        tickPhase = if (isChecked) PHASE_VISIBLE else PHASE_INVISIBLE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // draw circle

        // draw tick
        applyTickDashEffect(tickPhase)
        tickPaint.color = checkedColor
        tickPaint.strokeWidth = tickWidth
        tickPaint.setShadowLayer(shadowRadius, 0f, 0f, shadowColor)
        canvas.drawPath(tickPath, tickPaint)
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)

        // recalculate tick path
        val size: Float = Math.min(width, height).toFloat()
        tickWidth = size / tickRatio
        val padding = tickWidth * 0.5f + shadowRadius * 0.5f
        val croppedSize = Math.max(size - padding * 2f, 0f)
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
        tickAnimator.duration = 500
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
