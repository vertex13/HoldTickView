package com.koshkama.holdcheckbox

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * @author Aleksandr Pavlov
 */
open class HoldCheckBox : View {

    /**
     * A color in the checked state. By default [Color.GREEN].
     */
    var checkedColor: Int = Color.GREEN
    /**
     * A color in the unchecked state. By default [Color.GRAY].
     */
    var uncheckedColor: Int = Color.GRAY
    /**
     * Time to change state in milliseconds. By default 1000ms.
     */
    var switchingTime: Int = 1000

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

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

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet? = null) {
        if (attrs == null) {
            return
        }
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.HoldCheckBox, 0, 0)
        try {
            checkedColor = typedArray.getColor(R.styleable.HoldCheckBox_checkedColor, checkedColor)
            uncheckedColor = typedArray.getColor(R.styleable.HoldCheckBox_uncheckedColor, uncheckedColor)
            switchingTime = typedArray.getInteger(R.styleable.HoldCheckBox_switchingTime, switchingTime)
        } finally {
            typedArray.recycle()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // This view should be always square.
        val minMeasureSpec = Math.min(widthMeasureSpec, heightMeasureSpec)
        super.onMeasure(minMeasureSpec, minMeasureSpec)
    }

}
