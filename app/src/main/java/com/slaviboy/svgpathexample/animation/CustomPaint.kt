/*
* Copyright (C) 2020 Stanislav Georgiev
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

import android.graphics.*

/**
 * Custom paint class, that is customized to work with separate path,
 * and support the animation: fadeIn, fadeOut and phase (change the
 * stroke path effect to simulate path drawing)
 */
class CustomPaint : Paint {

    constructor() : super()
    constructor(flags: Int) : super(flags)
    constructor(paint: Paint?) : super(paint)

    var pathLength: Float = 0.0f
    var path: Path = Path()
        set(value) {
            val measure = PathMeasure(value, false)
            pathLength = measure.length
            field = value
        }

    /**
     * Update the stroke phase, to produce the draw path animation
     */
    fun setPhase(phase: Float) {
        alpha = 255
        pathEffect = createPathEffect(pathLength, phase, 0.0f)
        onUpdateListener?.invoke()
    }

    /**
     * Override the set alpha so update invoke can be done, for
     * smooth fadeIn or fadeOut effect
     */
    override fun setAlpha(a: Int) {
        super.setAlpha(a)
        onUpdateListener?.invoke()
    }

    var onUpdateListener: (() -> Unit)? = null

    fun onUpdate(callback: () -> Unit) {
        onUpdateListener = callback
    }

    companion object {
        private fun createPathEffect(pathLength: Float, phase: Float, offset: Float): PathEffect {
            return DashPathEffect(
                floatArrayOf(pathLength, pathLength),
                Math.max(phase * pathLength, offset)
            )
        }
    }
}