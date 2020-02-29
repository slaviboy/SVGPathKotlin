package com.slaviboy.svgpath

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.Log
import androidx.annotation.Nullable
import com.slaviboy.svgpath.Command.Companion.TYPE_M
import com.slaviboy.svgpath.CommandOperations.absolutize
import com.slaviboy.svgpath.CommandOperations.normalize
import com.slaviboy.svgpath.CommandOperations.parse
import java.lang.IllegalArgumentException
import java.util.*
import javax.security.auth.callback.Callback
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
 * Class that gets svg path commands data, and translate it to canvas commands,
 * that can be used to draw svg path on Android Canvas.
 */
class SvgPath(var data: String) {

    private var initialPathCommands: ArrayList<Command>          // the initial path commands that are set from the data string
    private var pathCommands: ArrayList<Command>                 // the initial path commands that are normalized(converted to C commands, that are draw with cubicTo curve)
    private var savedPathProperties: ArrayList<PathProperties>   // array queue with all saved path properties
    private var renderProperties: RenderProperties               // rendered properties for the path
    private var onDrawListener: (                                // reference to the callback for the onDraw
        (canvas: Canvas, paint: Paint, path: Path) -> Unit
    )? = null

    // the center point of the path
    var center: PointD = PointD()
        get() {
            val b = bound
            field = PointD((b.left + b.right) / 2.0, (b.top + b.bottom) / 2.0)
            return field
        }

    // the bound surronding the path
    var bound: Bound = Bound()
        get() {
            var boundsOut = Bound()
            if (pathCommands.size > 0) {
                val bounds = Bound(
                    Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    Double.NEGATIVE_INFINITY,
                    Double.NEGATIVE_INFINITY
                )

                // loop through all commands and set the min and max values
                for (i in pathCommands.indices) {
                    val points = pathCommands[i].points
                    for (j in points.indices) {

                        // set min X for left, max X for right
                        if (points[j].x < bounds.left) bounds.left = points[j].x
                        if (points[j].x > bounds.right) bounds.right = points[j].x

                        // set min Y for top, max Y for bottom
                        if (points[j].y < bounds.top) bounds.top = points[j].y
                        if (points[j].y > bounds.bottom) bounds.bottom = points[j].y
                    }
                }
                boundsOut = bounds
            }

            field = boundsOut
            return field
        }

    init {
        val commands = parse(data)
        initialPathCommands = absolutize(commands)
        pathCommands = normalize(initialPathCommands)
        savedPathProperties = ArrayList()
        renderProperties = RenderProperties()
    }

    /**
     * Save current path properties, in the array queue, that includes
     * the transformed points. So before applying any transformation it
     * is preferable to first save the current state of the path
     */
    fun save(): SvgPath {

        // clone the array with commands for the path
        val commandsCopy = ArrayList<Command>()
        pathCommands.forEach {
            commandsCopy.add(it.clone())
        }

        savedPathProperties.add(
            PathProperties(
                pathCommands = commandsCopy,
                bounds = bound.clone(),
                renderProperties = renderProperties.clone()
            )
        )
        return this
    }

    /**
     * Restore the last saved path properties from the array queue and set it as
     * current, and also remove it from the queue.
     */
    fun restore(): SvgPath {
        if (savedPathProperties.size > 0) {
            val paths = savedPathProperties.removeAt(1)
            this.pathCommands = paths.pathCommands
            this.bound = paths.bounds
            this.renderProperties = paths.renderProperties
        }
        return this
    }

    /**
     * Transform all commands coordinates value using the 2D transform matrix
     * tha is passed as argument, applied directly to all points.
     * @param matrix matrix with the transformation that will be applied
     */
    fun transform(matrix: Matrix): SvgPath {

        bound = Bound()
        pathCommands.forEach {

            // apply the transformation for each point
            for (i in it.points.indices) {
                it.points[i] = matrix.transformPoint(it.points[i])
            }
        }
        return this
    }


    fun rotate(degree: Double): SvgPath {
        val matrix = Matrix().rotate(degree)
        return transform(matrix)
    }

    fun translate(x: Double = 0.0, y: Double = 0.0): SvgPath {
        val matrix = Matrix().translate(x, y)
        return transform(matrix)
    }

    fun translate(xy: Double = 0.0): SvgPath {
        return translate(xy, xy)
    }

    fun translate(xy: PointD = PointD()): SvgPath {
        return translate(xy.x, xy.y)
    }

    fun scale(x: Double = 0.0, y: Double = 0.0): SvgPath {
        val matrix = Matrix().scale(x, y)
        return transform(matrix)
    }

    fun scale(xy: Double = 0.0): SvgPath {
        return scale(xy, xy)
    }

    fun scale(xy: PointD = PointD()): SvgPath {
        return scale(xy.x, xy.y)
    }

    fun skew(degX: Double = 0.0, degY: Double = 0.0): SvgPath {
        val matrix = Matrix().skew(degX, degY)
        return transform(matrix)
    }

    fun skew(degXY: Double = 0.0): SvgPath {
        return skew(degXY, degXY)
    }

    fun skew(degXY: PointD = PointD()): SvgPath {
        return skew(degXY.x, degXY.y)
    }

    fun flipHorizontal(): SvgPath {
        val matrix = Matrix.flipHorizontal()
        return transform(matrix)
    }

    fun flipVertical(): SvgPath {
        val matrix = Matrix.flipVertical()
        return transform(matrix)
    }

    fun flipCentral(): SvgPath {
        val matrix = Matrix.flipCentral()
        return transform(matrix)
    }

    fun opacity(opacity: Double): SvgPath {
        renderProperties.opacity = opacity
        return this
    }

    fun strokeStyle(color: Int): SvgPath {
        renderProperties.strokeStyle = color
        return this
    }

    fun fillStyle(color: Int): SvgPath {
        renderProperties.fillStyle = color
        return this
    }

    fun strokeWidth(value: Double): SvgPath {
        renderProperties.strokeWidth = value
        return this
    }

    /**
     * Line cap: BUTT,ROUND and SQUARE
     */
    fun strokeCap(value: String): SvgPath {
        renderProperties.strokeCap = when (value.toLowerCase(Locale.ROOT)) {
            "butt" -> Paint.Cap.BUTT
            "round" -> Paint.Cap.ROUND
            "square" -> Paint.Cap.SQUARE
            else -> Paint.Cap.BUTT
        }
        return this
    }

    fun strokeCap(value: Paint.Cap): SvgPath {
        renderProperties.strokeCap = value
        return this
    }

    /**
     * Line join: BEVEL, MITER and ROUND
     */
    fun strokeJoin(value: String): SvgPath {

        renderProperties.strokeJoin = when (value.toLowerCase(Locale.ROOT)) {
            "round" -> Paint.Join.ROUND
            "bevel" -> Paint.Join.BEVEL
            "miter" -> Paint.Join.MITER
            else -> Paint.Join.MITER
        }
        return this
    }

    fun strokeJoin(value: Paint.Join): SvgPath {
        renderProperties.strokeJoin = value
        return this
    }

    /**
     * Check whether the path is closed
     */
    fun isClosed(): Boolean {
        // check if last command type is Z for closed path
        val part = initialPathCommands[initialPathCommands.size - 1]
        return part.type == 'Z'
    }

    /**
     * Generate the graphic path commands, using the svg path data
     * @param path android graphic path
     */
    fun generatePath(path: Path = Path()): Path {

        if (this.pathCommands.size > 0) {

            // for each command
            for (command in this.pathCommands) {
                val type = command.type
                val cords = command.points

                if (type == 'M') {
                    path.moveTo(cords[0].x.toFloat(), cords[0].y.toFloat())
                } else if (type == 'C') {
                    // analogue to a bezier curve
                    path.cubicTo(
                        cords[0].x.toFloat(), cords[0].y.toFloat(),
                        cords[1].x.toFloat(), cords[1].y.toFloat(),
                        cords[2].x.toFloat(), cords[2].y.toFloat()
                    )
                }
            }

            // close path
            if (isClosed()) {
                path.close()
            }
        }
        return path
    }

    /**
     * Draw given path or generate new path, with the command points
     * and draw it on given canvas with given paint object
     */
    fun draw(canvas: Canvas, paint: Paint = Paint(), path: Path = generatePath()) {

        // it will trigger the callback instead of continuing the drawing from this method
        if (onDrawListener != null) {
            onDrawListener?.invoke(canvas, paint, path)
            return
        }

        // set the render properties for the path
        val rp = this.renderProperties
        paint.strokeWidth = rp.strokeWidth.toFloat()
        paint.strokeJoin = rp.strokeJoin
        paint.strokeCap = rp.strokeCap
        paint.shader = null

        // fill path
        if (rp.fillStyle != Color.TRANSPARENT) {
            paint.style = Paint.Style.FILL
            paint.color = rp.fillStyle
            if (rp.opacity >= 0) {
                paint.alpha = (rp.opacity * 255).toInt()
            }
            canvas.drawPath(path, paint)
        }

        // stroke path
        if (rp.strokeStyle != Color.TRANSPARENT) {
            paint.style = Paint.Style.STROKE
            paint.color = rp.strokeStyle
            if (rp.opacity >= 0) {
                paint.alpha = (rp.opacity * 255).toInt()
            }
            canvas.drawPath(path, paint)
        }
    }

    /**
     * Method used for custom drawing, allowing more robust using
     * of the canvas, paint and path objects
     */
    fun onDraw(callback: (canvas: Canvas, paint: Paint, path: Path) -> Unit) {
        onDrawListener = callback
    }
}

