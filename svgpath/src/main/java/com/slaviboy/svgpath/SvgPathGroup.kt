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
package com.slaviboy.svgpath

import android.graphics.*
import com.slaviboy.svgpath.SvgPath.RenderProperties

/**
 * Simple class for collecting multiple SvgPath object into a group, that way
 * it is possible applying transformations to the whole group.
 * @param svgPaths array list with all the SvgPath object in the group
 * @param renderProperties object holding Paint properties for the rendering for the whole group
 * @param matrix matrix with transformation applied to the whole group
 */
class SvgPathGroup(
    var svgPaths: ArrayList<SvgPath> = arrayListOf(),
    var renderProperties: RenderProperties = RenderProperties(),
    var matrix: Matrix = Matrix()
) {

    constructor(vararg paths: SvgPath) : this() {
        this.svgPaths = paths.toCollection(ArrayList())
    }

    constructor(vararg data: String) : this() {

        // create the svg paths, and set values for the render properties to null, so the path use those one from the group
        this.svgPaths = (data.map { it ->
            SvgPath(
                it, RenderProperties(null, null, null, null, null, null)
            )
        }).toCollection(ArrayList())
    }

    // reference to the callback for the onDraw
    lateinit var onDrawListener: ((canvas: Canvas, paint: Paint, paths: ArrayList<SvgPath>) -> Unit)
    var isUpdated: Boolean    // if paths are updated and calling the getter for 'bound' for the path should generated again or use previous value

    // bound surrounding all paths
    var bound: RectF = RectF()
        get() {
            if (isUpdated) {
                isUpdated = false

                val boundTemp = RectF(
                    Float.POSITIVE_INFINITY,
                    Float.POSITIVE_INFINITY,
                    Float.NEGATIVE_INFINITY,
                    Float.NEGATIVE_INFINITY
                )

                val bound = RectF()

                // loop through all path bounds and find the minimum and maximum for X and Y
                svgPaths.forEach {

                    matrix.mapRect(bound, it.bound)

                    // set min X for left, max X for right
                    if (bound.left < boundTemp.left) {
                        boundTemp.left = bound.left
                    }
                    if (bound.right > boundTemp.right) {
                        boundTemp.right = bound.right
                    }

                    // set min Y for top, max Y for bottom
                    if (bound.top < boundTemp.top) {
                        boundTemp.top = bound.top
                    }
                    if (bound.bottom > boundTemp.bottom) {
                        boundTemp.bottom = bound.bottom
                    }
                }

                field = boundTemp
            }
            return field
        }

    init {
        isUpdated = true
    }

    /**
     * Generate array containing all graphic paths from the SvgPath object in the group.
     */
    fun generatePaths(): ArrayList<Path> {

        val graphicPath = ArrayList<Path>()
        svgPaths.forEach {
            val path = it.generatePath()
            graphicPath.add(path)
        }
        return graphicPath
    }

    /**
     * Add another svg path.
     */
    fun add(vararg svgPath: SvgPath) {
        svgPath.forEach {
            svgPaths.add(it)
        }
        isUpdated = true
    }

    /**
     * Clear all svg paths, from the array
     */
    fun clear() {
        svgPaths = arrayListOf()
        isUpdated = true
    }

    /**
     * Remove svg path from the existing list
     * @param index remove element at given index
     */
    fun remove(index: Int) {
        svgPaths.removeAt(index)
        isUpdated = true
    }

    /**
     * Draw all paths data in separate paths on a given canvas, with paint object if present.
     * @param canvas canvas where the paths will be drawn
     * @param paint paint object for setting stroke color, fill color...
     */
    fun draw(canvas: Canvas, paint: Paint = Paint().apply { isAntiAlias = true }) {

        // it will trigger the callback instead of continuing the drawing from this method
        if (::onDrawListener.isInitialized) {
            onDrawListener.invoke(canvas, paint, svgPaths)
            return
        }

        // draw each path
        svgPaths.forEach {
            it.draw(
                canvas = canvas,
                paint = paint,
                groupMatrix = matrix,
                groupRenderProperties = renderProperties
            )
        }
    }

    /**
     * Method used for custom drawing, allowing more robust using
     * of the canvas, paint and path objects
     */
    fun onDraw(callback: (canvas: Canvas, paint: Paint, paths: ArrayList<SvgPath>) -> Unit) {
        onDrawListener = callback
    }
}