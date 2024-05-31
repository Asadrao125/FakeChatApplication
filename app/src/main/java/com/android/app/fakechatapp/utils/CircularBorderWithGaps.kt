package com.android.app.fakechatapp.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.android.app.fakechatapp.R
import kotlin.math.min

class CircularBorderWithGaps : View {
    private var borderPaint: Paint? = null
    private var borderColors = intArrayOf()
    private var borderWidth = 0
    private var gapWidth = 0

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        borderPaint = Paint()
        borderPaint!!.style = Paint.Style.STROKE
        borderColors = intArrayOf(
            ContextCompat.getColor(context, R.color.black),
            ContextCompat.getColor(context, R.color.yellow),
            ContextCompat.getColor(context, R.color.green)
        )
        borderWidth = 10
        gapWidth = 10
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width
        val height = height
        val radius = min(width, height) / borderColors.size
        borderPaint!!.strokeWidth = borderWidth.toFloat()

        val segmentAngle = 360f / borderColors.size
        var startAngle = 0f

        for ((colorIndex) in (borderColors.indices).withIndex()) {
            borderPaint!!.color =
                borderColors[colorIndex % borderColors.size]
            val rect = RectF(
                (width / 2 - radius).toFloat(),
                (height / 2 - radius).toFloat(),
                (width / 2 + radius).toFloat(),
                (height / 2 + radius).toFloat()
            )
            canvas.drawArc(rect, startAngle, segmentAngle - gapWidth, false, borderPaint!!)
            startAngle += segmentAngle
        }
    }
}