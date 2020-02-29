package com.slaviboy.svgpathexample

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import com.slaviboy.svgpath.PointD
import com.slaviboy.svgpath.SvgPathGroup
import kotlin.collections.ArrayList

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

/**
 * Simple path view that draws Michael Jordan logo and signature,
 * from svg path data. And uses animation for displaying it.
 */
class JordanView : View, View.OnClickListener {

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    private val center: PointD                                   // center of the path group
    private var svgPathGroup: SvgPathGroup                       // the path group with all svg paths
    private var paths: ArrayList<Path>                           // all graphic paths generated from the svg paths
    private val paints: ArrayList<CustomPaint> = ArrayList()     // each path has its own custom paint object, that is used for the animation
    private var animators: MutableList<Animator>                 // list with animators for each path
    private lateinit var animatorSet: AnimatorSet                // set where all animators are attached nad started

    init {

        // create group from all paths
        svgPathGroup = SvgPathGroup(
            resources.getString(R.string.jordan_logo),
            resources.getString(R.string.jordan_signature_name),
            resources.getString(R.string.jordan_signature_surname)
        )

        center = svgPathGroup.center
        svgPathGroup
            .translate(-center.x, -center.y)

        paths = svgPathGroup.generatePaths()
        animators = MutableList<Animator>(paths.size) { ObjectAnimator() }
        for (i in paths.indices) {

            // set paint properties
            val paint = CustomPaint().apply {
                color = Color.BLACK
                strokeWidth = 8.0f
                style = Paint.Style.STROKE
                isAntiAlias = true
                path = paths[i]
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                onUpdate {
                    this@JordanView.invalidate()
                }
                alpha = 0
            }

            // animators for fade and path phase
            val animator = if (i == 0) {
                paint.style = Paint.Style.FILL
                ObjectAnimator.ofInt(paint, "alpha", 0, 255)
            } else {
                ObjectAnimator.ofFloat(paint, "phase", 1.0f, 0.0f)
            }

            animator.duration = 2000
            animators[i] = animator

            paints.add(paint)
        }

        playAnimation()
        setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        playAnimation()
    }

    fun playAnimation() {
        if (::animatorSet.isInitialized) {
            if (!animatorSet.isPaused) {
                animatorSet.pause()
            } else {
                animatorSet.resume()
            }
        } else {
            animatorSet = AnimatorSet()
            animatorSet.playSequentially(animators)
            animatorSet.start()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.drawColor(Color.WHITE)

        // translate to center and down scale
        canvas?.save()
        canvas?.translate(width / 2.0f, height / 2.0f)
        canvas?.scale(0.35f, 0.35f)

        for (i in paths.indices) {
            canvas?.drawPath(paths[i], paints[i])
        }
        canvas?.restore()
    }
}