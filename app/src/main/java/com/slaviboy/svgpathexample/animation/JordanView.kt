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
package com.slaviboy.svgpathexample.animation

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.slaviboy.svgpath.SvgPathGroup
import com.slaviboy.svgpathexample.R
import com.slaviboy.svgpathexample.extensions.center
import com.slaviboy.svgpathexample.views.SvgPathView.Companion.afterMeasured

/**
 * Simple path view that draws Michael Jordan logo and signature,
 * from svg path data. And uses animation for displaying it.
 */
class JordanView : View, View.OnClickListener {

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    private val center: PointF                                   // center of the path group
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

        center = svgPathGroup.bound.center()
        svgPathGroup.matrix.apply {
            // postTranslate(-center.x, -center.y)
        }

        paths = svgPathGroup.generatePaths()
        animators = MutableList<Animator>(paths.size) {
            ObjectAnimator()
        }

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

        this.afterMeasured {

            // get bound values for the group boundary box
            svgPathGroup.isUpdated = true
            val bound = svgPathGroup.bound
            val boundWidth = bound.width()

            // apply transformations to the whole group
            svgPathGroup.matrix.apply {
                postTranslate(width / 2f - bound.centerX(), height / 2f - bound.centerY())
                //postRotate(45.0)
                //postSkew(0.5, 0.0)
                postScale(width / (boundWidth * 2f), width / (boundWidth * 2f), width / 2f, height / 2f)
            }
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

    override fun onDraw(canvas: Canvas) {

        canvas.drawColor(Color.WHITE)

        // translate to center and down scale
        canvas.save()
        canvas.setMatrix(svgPathGroup.matrix)

        for (i in paths.indices) {
            canvas.drawPath(paths[i], paints[i])
        }
        canvas.restore()
    }
}