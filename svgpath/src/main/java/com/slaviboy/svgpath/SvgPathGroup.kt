package com.slaviboy.svgpath

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.Log
import java.lang.IllegalArgumentException
import java.util.*
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

class SvgPathGroup() {

    constructor(vararg paths: SvgPath) : this() {
        this.paths = paths.toCollection(ArrayList()) as ArrayList<SvgPath>
    }

    constructor(vararg data: String) : this() {
        paths = (data.map { it -> SvgPath(it) }).toCollection(ArrayList()) as ArrayList<SvgPath>
    }

    private var paths: ArrayList<SvgPath> = arrayListOf()        // array with all svg paths used in this group
    private var onDrawListener: (                                // reference to the callback for the onDraw
        (canvas: Canvas, paint: Paint, paths: ArrayList<SvgPath>) -> Unit
    )? = null

    // bound surrounding all paths
    var bound: Bound = Bound()
        get() {

            val boundTemp = Bound(
                Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY,
                Double.NEGATIVE_INFINITY
            )

            // loop through all path bounds and find the minimum and maximum for X and Y
            paths.forEach {

                val b = it.bound
                // set min X for left, max X for right
                if (b.left < boundTemp.left) boundTemp.left = b.left
                if (b.right > boundTemp.right) boundTemp.right = b.right

                // set min Y for top, max Y for bottom
                if (b.top < boundTemp.top) boundTemp.top = b.top
                if (b.bottom > boundTemp.bottom) boundTemp.bottom = b.bottom
            }

            field = boundTemp
            return field
        }

    // center of the bound surrounding all paths
    var center: PointD = PointD()
        get() {

            // return center point from the bound
            val b = bound
            field = PointD(
                (b.left + b.right) / 2.0,
                (b.top + b.bottom) / 2.0
            )
            return field
        }


    /**
     * Generate array contatining all graphic paths from the
     * SvgPath object in the group.
     */
    fun generatePaths(): ArrayList<Path> {

        val gPaths = ArrayList<Path>()
        paths.forEach {
            val path = it.generatePath()
            gPaths.add(path)
        }
        return gPaths
    }

    /**
     * Add another svg path.
     */
    fun add(vararg svgPath: SvgPath) {
        svgPath.forEach {
            paths.add(it)
        }
    }

    /**
     * Clear all svg paths, from the array
     */
    fun clear() {
        paths = arrayListOf<SvgPath>()
    }

    /**
     * Remove svg path from the existing list
     * @param index remove element at given index
     */
    fun remove(index: Int) {
        paths.removeAt(index)
    }

    /**
     * Set opacity for the whole group, that means all paths simultaneously
     * @param opacity opacity as value between [0,1]
     */
    fun opacity(opacity: Double): SvgPathGroup {
        paths.forEach {
            it.opacity(opacity)
        }
        return this
    }

    /**
     * Rotate the whole group, that means all paths simultaneously
     * @param deg rotational degree
     */
    fun rotate(deg: Double): SvgPathGroup {
        paths.forEach {
            it.rotate(deg)
        }
        return this
    }

    /**
     * Translate the whole group, that means all paths simultaneously
     * @param x horizontally
     * @param y vertically
     */
    fun translate(x: Double = 0.0, y: Double = 0.0): SvgPathGroup {
        paths.forEach {
            it.translate(x, y)
        }
        return this
    }

    /**
     * Translate the whole group, that means all paths simultaneously
     * @param xy horizontally and vertically
     */
    fun translate(xy: Double = 0.0): SvgPathGroup {
        return this.translate(xy, xy)
    }

    /**
     * Scale the whole group, that means all paths simultaneously
     * @param x horizontally
     * @param y vertically
     */
    fun scale(x: Double = 0.0, y: Double = 0.0): SvgPathGroup {
        paths.forEach {
            it.scale(x, y)
        }
        return this
    }

    /**
     * Scale the whole group, that means all paths simultaneously
     * @param xy horizontally and vertically
     */
    fun scale(xy: Double = 0.0): SvgPathGroup {
        return this.scale(xy, xy)
    }

    /**
     * Skew the whole group, that means all paths simultaneously
     * @param degX degree fro horizontal direction
     * @param degY degree fro horizontal vertical
     */
    fun skew(degX: Double = 0.0, degY: Double = 0.0): SvgPathGroup {
        paths.forEach {
            it.skew(degX, degY)
        }
        return this
    }

    /**
     * Skew the whole group, that means all paths simultaneously
     * @param degXY degree fro both horizontal and vertical direction
     */
    fun skew(degXY: Double = 0.0): SvgPathGroup {
        return this.skew(degXY, degXY)
    }

    /**
     * Flip the whole group horizontally, that means all paths simultaneously
     */
    fun flipHorizontal(): SvgPathGroup {
        paths.forEach {
            it.flipHorizontal()
        }
        return this
    }

    /**
     * Flip the whole group vertically, that means all paths simultaneously
     */
    fun flipVertical(): SvgPathGroup {
        paths.forEach {
            it.flipVertical()
        }
        return this
    }

    /**
     * Flip the whole group centrally, that means all paths simultaneously
     */
    fun flipCentral(): SvgPathGroup {
        paths.forEach {
            it.flipCentral()
        }
        return this
    }

    /**
     * Set stroke style for the whole group
     * @param color integer representation of a color
     */
    fun strokeStyle(color: Int): SvgPathGroup {
        paths.forEach {
            it.strokeStyle(color)
        }
        return this
    }

    /**
     * Set the fill style for the whole group
     * @param color integer representation of a color
     */
    fun fillStyle(color: Int): SvgPathGroup {
        paths.forEach {
            it.fillStyle(color)
        }
        return this
    }

    /**
     * Set stroke width for the whole group
     * @param value stroke width in pixels
     */
    fun strokeWidth(value: Double): SvgPathGroup {
        paths.forEach {
            it.strokeWidth(value)
        }
        return this
    }

    /**
     * Set stroke cap for the whole group
     * @param value string representation "butt", "round" or "square"
     */
    fun strokeCap(value: String): SvgPathGroup {
        paths.forEach {
            val strokeCap = when (value.toLowerCase(Locale.ROOT)) {
                "butt" -> Paint.Cap.BUTT
                "round" -> Paint.Cap.ROUND
                "square" -> Paint.Cap.SQUARE
                else -> Paint.Cap.BUTT
            }
            it.strokeCap(strokeCap)
        }
        return this
    }

    /**
     * Set stroke cap style for the whole, group, that means all paths simultaneously
     * @param value as paint cap representation Paint.Cap.BUTT,
     * Paint.Cap.ROUND or Paint.Cap.SQUARE
     */
    fun strokeCap(value: Paint.Cap): SvgPathGroup {
        paths.forEach {
            it.strokeCap(value)
        }
        return this
    }


    /**
     * Set stroke join style for the whole, group, that means all paths simultaneously
     * @param value as string "round", "bevel" or "miter"
     */
    fun strokeJoin(value: String): SvgPathGroup {
        paths.forEach {
            val strokeJoin = when (value.toLowerCase(Locale.ROOT)) {
                "round" -> Paint.Join.ROUND
                "bevel" -> Paint.Join.BEVEL
                "miter" -> Paint.Join.MITER
                else -> Paint.Join.MITER
            }
            it.strokeJoin(strokeJoin)
        }
        return this
    }

    /**
     * Set stroke join style for the whole, group, that means all paths simultaneously
     * @param value as paint cap representation Paint.Join.ROUND,
     * Paint.Join.BEVEL or Paint.Join.MITER
     */
    fun strokeJoin(value: Paint.Join): SvgPathGroup {
        paths.forEach {
            it.strokeJoin(value)
        }
        return this
    }

    /**
     * Draw all paths data in separate paths on a given canvas,
     * with paint object if presented
     */
    fun draw(canvas: Canvas, paint: Paint = Paint().apply { isAntiAlias = true }) {

        // it will trigger the callback instead of continuing the drawing from this method
        if (onDrawListener != null) {
            onDrawListener?.invoke(canvas, paint, paths)
            return
        }

        paths.forEach {
            it.draw(canvas, paint)
        }
    }

    /**
     * Translate each path separately with coordinate, two coordinates per path x and y
     * for n number of paths, they all follow the pattern: x0,y0, x1,y1, x2,y2, ....xn,yn
     * @param translations translations as list of coordinate for each path
     */
    fun translateEach(vararg translations: Double): SvgPathGroup {

        require(translations.size / 2 == paths.size) {
            "Total number of translation: ${translations.size / 2} does not math the number of paths ${paths.size}!"
        }

        for (i in paths.indices) {
            paths[i].translate(translations[i * 2], translations[i * 2 + 1])
        }
        return this
    }


    /**
     * Translate each path separately with points for n number of paths, they all
     * have the following pattern: p1, p2, p3, ... pn {where p is representation of a point}
     * @param translations translations as list of points for each path
     */
    fun translateEach(vararg translations: PointD): SvgPathGroup {

        require(translations.size == paths.size) {
            "Total number of translation: ${translations.size} does not math the number of paths ${paths.size}!"
        }

        for (i in paths.indices) {
            paths[i].translate(translations[i].x, translations[i].y)
        }
        return this
    }

    /**
     * Rotate each path separately with degree for each path, the following
     * pattern is in place: d1, d2, d3, ... dn {where d is representation of a degree}
     * @param degrees rotation as list of degrees for each path
     */
    fun rotateEach(vararg degrees: Double): SvgPathGroup {

        require(degrees.size == paths.size) {
            "Total number of rotations: ${degrees.size} does not math the number of paths ${paths.size}!"
        }

        for (i in paths.indices) {
            paths[i].rotate(degrees[i])
        }
        return this
    }

    /**
     * Scale each path separately with coordinate, two coordinates per path x and y
     * for n number of paths, they all follow the pattern: x0,y0, x1,y1, x2,y2, ....xn,yn
     * @param scales scales as coordinate list for each path
     */
    fun scaleEach(vararg scales: Double): SvgPathGroup {

        require(scales.size / 2 == paths.size) {
            "Total number of scales: ${scales.size / 2} does not math the number of paths ${paths.size}!"
        }

        for (i in paths.indices) {
            paths[i].scale(scales[i * 2], scales[i * 2 + 1])
        }
        return this
    }

    /**
     * Scale each path separately with points for n number of paths, following the
     * pattern: p1, p2, p3, ... pn {where p is representation of a point}
     * @param scales scales as list of points for each path
     */
    fun scaleEach(vararg scales: PointD): SvgPathGroup {

        require(scales.size == paths.size) {
            "Total number of scales: ${scales.size} does not math the number of paths ${paths.size}!"
        }

        for (i in paths.indices) {
            paths[i].scale(scales[i].x, scales[i].y)
        }
        return this
    }

    /**
     * Skew each path separately with coordinate, two coordinates per path x and y
     * for n number of paths, they all follow the pattern: x0,y0, x1,y1, x2,y2, ....xn,yn
     * @param degrees degrees as coordinate list for each path
     */
    fun skewEach(vararg degrees: Double): SvgPathGroup {

        require(degrees.size / 2 == paths.size) {
            "Total number of skews: ${degrees.size / 2} does not math the number of paths ${paths.size}!"
        }

        for (i in paths.indices) {
            paths[i].skew(degrees[i * 2], degrees[i * 2 + 1])
        }
        return this
    }

    /**
     * Skew each path separately with points for n number of paths, following the
     * pattern: p1, p2, p3, ... pn {where p is representation of a point}
     * @param degrees degrees as list of points for each path
     */
    fun skewEach(vararg degrees: PointD): SvgPathGroup {

        require(degrees.size == paths.size) {
            "Total number of skews: ${degrees.size} does not math the number of paths ${paths.size}!"
        }

        for (i in paths.indices) {
            paths[i].skew(degrees[i].x, degrees[i].y)
        }
        return this
    }

    /**
     * Method used for custom drawing, allowing more robust using
     * of the canvas, paint and path objects
     */
    fun onDraw(callback: (canvas: Canvas, paint: Paint, paths: ArrayList<SvgPath>) -> Unit) {
        onDrawListener = callback
    }
}