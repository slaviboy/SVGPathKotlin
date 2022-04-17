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
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.slaviboy.svgpath.SvgPathGroup
import com.slaviboy.svgpathexample.extensions.center
import com.slaviboy.svgpathexample.views.SvgPathView.Companion.afterMeasured
import kotlin.concurrent.fixedRateTimer

/**
 * Simple view that demonstrates how to add random color for each values of the group
 */
class SVGPathGroupRandomView : View {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var svgPathGroup: SvgPathGroup
    private val paint: Paint = Paint().apply {
        isAntiAlias = true
    }

    init {

        // create multiple paths combined in a group
        svgPathGroup = SvgPathGroup(
            "M2,2 L8,8",
            "M2,8 L5,2 L8,8",
            "M2,2 Q8,2 8,8",
            "M2,5 C2,8 8,8 8,5",
            "M2,2 L8,2 L2,5 L8,5 L2,8 L8,8",
            "M2,5 A 5 25 0 0 1 8 8",
            "M2,5 S2,-2 4,5 S7,8 8,4",
            "M5,2 Q 2,5 5,8",
            "M2,2 Q5,2 5,5 T8,8"
        )

        // set properties to all paths in the group
        svgPathGroup.renderProperties.apply {
            strokeWidth = 0.2f
            strokeColor = Color.BLACK
        }

        // values to translate each path separately
        val translateValues = floatArrayOf(
            -8.0f, 0.0f,
            0.0f, 0.0f,
            8.0f, 0.0f,
            -8.0f, 8.0f,
            0.0f, 8.0f,
            8.0f, 8.0f,
            -8.0f, 16.0f,
            0.0f, 16.0f,
            8.0f, 16.0f
        )

        svgPathGroup.svgPaths.forEachIndexed { index, svgPath ->

            // translate each path separately
            svgPath.matrix.postTranslate(translateValues[index * 2], translateValues[index * 2 + 1])

            // generate random color for each path separately
            svgPath.renderProperties.strokeColor = getRandomColor()
        }

        this.afterMeasured {

            svgPathGroup.matrix.apply {
                //postRotate(45.0)
                //postSkew(0.5, 0.0)
            }

            // get bound values for the group boundary box
            svgPathGroup.isUpdated = true
            val bound = svgPathGroup.bound
            val boundCenter = bound.center()
            val boundWidth = bound.width()
            val boundHeight = bound.height()

            // apply transformations to the whole group
            svgPathGroup.matrix.apply {
                postTranslate(-boundCenter.x, -boundCenter.y)
                postScale(width / (boundWidth * 2), width / (boundWidth * 2))
                postTranslate(width / 2 - boundWidth / 2, height / 2 - boundHeight / 2)
            }
        }

        // start infinite timer that changes the color for each path separately
        fixedRateTimer(name = "infinite-timer", daemon = true, initialDelay = 0, period = 1000) {
            svgPathGroup.svgPaths.forEachIndexed { index, svgPath ->

                // generate random color for each path separately
                svgPath.renderProperties.strokeColor = getRandomColor()
            }
            invalidate()
        }
    }

    /**
     * Method that generates random color represented as integer value
     */
    fun getRandomColor(): Int {
        val range = (1..255)
        return Color.argb(
            255,
            range.random(),
            range.random(),
            range.random()
        )
    }

    override fun onDraw(canvas: Canvas) {

        // draw all paths in the group
        svgPathGroup.draw(canvas, paint)
    }
}
