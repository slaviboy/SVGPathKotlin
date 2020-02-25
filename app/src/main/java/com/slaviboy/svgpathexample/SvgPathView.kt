package com.slaviboy.svgpathexample

import android.content.Context
import android.graphics.*
import android.graphics.Paint.Align
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import com.slaviboy.svgpath.PointD
import com.slaviboy.svgpath.SvgPath

/**
 * Simple view, that demonstrates single path drawing from svg path data
 */
class SvgPathView : View {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    companion object {

        /**
         * Inline function that is called, when the final measurement is made and
         * the view is about to be draw.
         */
        inline fun View.afterMeasured(crossinline f: View.() -> Unit) {
            viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if (measuredWidth > 0 && measuredHeight > 0) {
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                        f()
                    }
                }
            })
        }
    }

    private var svgPath: SvgPath
    private val paint: Paint

    init {

        paint = Paint()
        paint.isAntiAlias = true

        val data = resources.getString(R.string.bulgaria)
        svgPath = SvgPath(data)

        val center = svgPath.center

        svgPath.save()
            .translate(-center.x, -center.y)
            .scale(0.4)
            //.rotate(180.0)
            //.skew(0.0, 0.0)
            //.flipHorizontal()
            .strokeStyle(Color.BLACK)
            .fillStyle(Color.RED)
            .strokeWidth(2.0)
            .opacity(1.0)

        svgPath.onDraw { canvas, paint, path ->

            val bound = svgPath.bound

            paint.clearShadowLayer()
            paint.alpha = 255
            paint.shader = LinearGradient(
                0.0f, bound.top.toFloat(), 0.0f, bound.bottom.toFloat(),
                intArrayOf(Color.WHITE, Color.parseColor("#1fdb44"), Color.parseColor("#ff2e2e")),
                floatArrayOf(0.25f, 0.5f, 0.7f),
                Shader.TileMode.CLAMP
            )
            paint.style = Paint.Style.FILL
            canvas.drawPath(path, paint)

            paint.shader = null
            paint.style = Paint.Style.STROKE
            paint.color = Color.BLACK
            canvas.drawPath(path, paint)

            paint.textSize = 50.0f
            paint.color = Color.WHITE
            paint.setShadowLayer(1.0f, -2.0f, 2.0f, Color.BLACK);
            drawCenteredText(canvas, paint, "BULGARIA", PointD(-20.0, 0.0))
        }

        this.afterMeasured {

            // translate path to center
            svgPath.translate(
                (width / 2.0),
                (height / 2.0)
            )
        }
    }

    /**
     * Simple method for drawing centered text on canvas
     * @param canvas canvas where the tet will be drawn
     * @param paint paint with properties for the drawing
     * @param text text value
     */
    private fun drawCenteredText(canvas: Canvas, paint: Paint, text: String = "Ups..", offset: PointD) {

        // get canvas bound
        val canvasRect = Rect()
        canvas.getClipBounds(canvasRect)

        // get text bound
        val textRect = Rect()
        paint.textAlign = Align.LEFT
        paint.getTextBounds(text, 0, text.length, textRect)

        val x: Float = canvasRect.width() / 2f - textRect.width() / 2f - textRect.left
        val y: Float = canvasRect.height() / 2f + textRect.height() / 2f - textRect.bottom
        canvas.drawText(text, x + offset.x.toFloat(), y + offset.y.toFloat(), paint)
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas != null) {
            svgPath.draw(canvas, paint)
        }
    }
}