package com.slaviboy.svgpathexample

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import com.slaviboy.svgpath.SvgPath
import com.slaviboy.svgpath.SvgPathGroup

// Copyright (C) 2020 Stanislav Georgiev
//  https://github.com/slaviboy
//
//	This program is free software: you can redistribute it and/or modify
//	it under the terms of the GNU Affero General Public License as
//	published by the Free Software Foundation, either version 3 of the
//	License, or (at your option) any later version.
//
//	This program is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU Affero General Public License for more details.
//
//	You should have received a copy of the GNU Affero General Public License
//	along with this program.  If not, see <http://www.gnu.org/licenses/>.

class SvgPathGroupView : View {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var bound: RectF
    private val center: PointF
    private var svgPathGroup: SvgPathGroup
    private val paint = Paint().apply {
        isAntiAlias = true
    }

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

    init {

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
            .translateEach(
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

        bound = svgPathGroup.bound
        center = svgPathGroup.center

        svgPathGroup
            .translate(-center.x, -center.y)
            .scale(15.0f)
            //.rotate(45.0f)
            .translate(center.x, center.y)
            .strokeStyle(Color.BLUE)
            .strokeWidth(10.0f)
            .strokeCap("round")
            .strokeJoin("round")



        this.afterMeasured {

            // translate group to center
            svgPathGroup.translate(
                width / 2 - (bound.right - bound.left) / 2,
                height / 2 - (bound.bottom - bound.top) / 2
            )
        }
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas != null) {
            svgPathGroup.draw(canvas, paint)
        }
    }
}