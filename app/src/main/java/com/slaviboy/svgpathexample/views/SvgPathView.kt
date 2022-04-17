/*
* Copyright (C) 2022 Stanislav Georgiev
* https://github.com/slaviboy
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.slaviboy.svgpathexample.views

import android.content.Context
import android.graphics.*
import android.graphics.Paint.Align
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import com.slaviboy.svgpath.SvgPath
import com.slaviboy.svgpath.SvgPath.RenderProperties
import com.slaviboy.svgpathexample.R
import com.slaviboy.svgpathexample.extensions.center

/**
 * Simple view, that demonstrates single path drawing from svg path data passed as string
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

    private val bound: RectF = RectF()
    private var svgPath: SvgPath
    private val paint: Paint = Paint().apply {
        isAntiAlias = true
    }

    init {

        // create SvgPath object and pass the raw path data
        val data = resources.getString(R.string.bulgaria)
        svgPath = SvgPath(data, RenderProperties().apply {
            fillColor = Color.GREEN
            opacity = 0.2f
        })

        // set onDraw method that will be called when drawing is required
        svgPath.onDraw { canvas, paint, path ->

            path.apply {
                transform(svgPath.matrix)
                computeBounds(bound, false)
            }

            // fill path with WHITE->GREEN->RED linear gradient
            paint.apply {
                clearShadowLayer()
                alpha = 255
                shader = LinearGradient(
                    0.0f, bound.top, 0.0f, bound.bottom,
                    intArrayOf(Color.WHITE, Color.parseColor("#1fdb44"), Color.parseColor("#ff2e2e")),
                    floatArrayOf(0.25f, 0.5f, 0.7f),
                    Shader.TileMode.CLAMP
                )
                style = Paint.Style.FILL
            }
            canvas.drawPath(path, paint)

            // stroke path
            paint.apply {
                shader = null
                style = Paint.Style.STROKE
                color = Color.BLACK
            }
            canvas.drawPath(path, paint)

            // draw centered text
            paint.apply {
                color = Color.WHITE
                style = Paint.Style.FILL
                setShadowLayer(1.0f, -width / 170f, width / 170f, Color.BLACK)
            }
            drawCenteredText(canvas, paint, "BULGARIA", PointF(-width / 50f, 0.0f))
        }

        // called after final measured is made and width/height for view are available
        this.afterMeasured {

            // get the center of the bound
            val bound = svgPath.bound
            val center = bound.center()
            val boundWidth = bound.width()

            svgPath.matrix.apply {
                postTranslate(-center.x, -center.y)
                //postRotate(180.0)
                postScale(width / (boundWidth * 1.5f), width / (boundWidth * 1.5f))
                postTranslate(width / 2.0f, height / 2.0f)
            }

            paint.textSize = width / 12f
        }
    }

    /**
     * Simple method for drawing centered text on canvas
     * @param canvas canvas where the tet will be drawn
     * @param paint paint with properties for the drawing
     * @param text text value
     */
    private fun drawCenteredText(canvas: Canvas, paint: Paint, text: String = "Ups..", offset: PointF) {

        // get canvas bound
        val canvasRect = Rect()
        canvas.getClipBounds(canvasRect)

        // get text bound
        val textRect = Rect()
        paint.textAlign = Align.LEFT
        paint.getTextBounds(text, 0, text.length, textRect)

        val x: Float = canvasRect.width() / 2f - textRect.width() / 2f - textRect.left
        val y: Float = canvasRect.height() / 2f + textRect.height() / 2f - textRect.bottom
        canvas.drawText(text, x + offset.x, y + offset.y, paint)
    }

    override fun onDraw(canvas: Canvas) {

        // draw the path
        svgPath.draw(canvas, paint)
    }
}