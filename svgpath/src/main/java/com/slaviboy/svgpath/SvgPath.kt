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
package com.slaviboy.svgpath

import android.graphics.*
import com.slaviboy.graphics.MatrixD
import com.slaviboy.graphics.RectD
import com.slaviboy.svgpath.Command.Companion.TYPE_C
import com.slaviboy.svgpath.Command.Companion.TYPE_M
import com.slaviboy.svgpath.Command.Companion.TYPE_Z
import com.slaviboy.svgpath.CommandOperations.absolutize
import com.slaviboy.svgpath.CommandOperations.normalize
import com.slaviboy.svgpath.CommandOperations.parse
import com.slaviboy.svgpath.CommandOperations.toUpperCase
import kotlin.collections.ArrayList

/**
 * Class that gets svg path commands data, and translate it to canvas commands,
 * that can be used to draw svg path using its data into Android Canvas.
 * @param data raw path data as string
 * @param renderProperties object holding Paint properties for the rendering
 * @param matrix matrix with transformations that will be applied to the path
 */
class SvgPath(
    var data: String,
    var renderProperties: RenderProperties = RenderProperties(),
    var matrix: MatrixD = MatrixD()
) {

    val initialCommands: ArrayList<Command>                                                    // initial path commands that are extracted from the data string
    val absolutizedCommands: ArrayList<Command>                                                // absolutized commands converted from the initial commands, that means commands with lowercase 'v', 'h', 's'.. are converted to absolute command with upper case 'V', 'H', 'S'..
    val normalizedCommands: ArrayList<Command>                                                 // normalized commands converted from the absolutized commands, that means converted from any command 'V', 'H', 'S' to 'C' command
    var isUpdated: Boolean                                                                     // if path is updated and calling the getter for 'bound' for the path should generated again or use previous value
    internal var tempMatrix: MatrixD                                                           // temp matrix object used in the conversion
    internal lateinit var onDrawListener: ((canvas: Canvas, paint: Paint, path: Path) -> Unit) // reference to the callback for the onDraw

    // the boundary box that surrounds the path
    var bound: RectD = RectD()
        get() {
            if (isUpdated) {

                isUpdated = false
                var boundsOut = RectD()
                if (normalizedCommands.size > 0) {

                    val bounds = RectD(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY)

                    // transform the coordinates using current matrix transformation
                    val transformedCoordinates = ArrayList<DoubleArray>()
                    normalizedCommands.forEach {
                        transformedCoordinates.add(it.transform(matrix))
                    }

                    // find the min and max x,y values that will be the bound of the path
                    for (i in transformedCoordinates.indices) {
                        val coordinates = transformedCoordinates[i]
                        for (j in 0 until coordinates.size / 2) {

                            // set min X for left, max X for right
                            if (coordinates[j * 2] < bounds.left) {
                                bounds.left = coordinates[j * 2]
                            }
                            if (coordinates[j * 2] > bounds.right) {
                                bounds.right = coordinates[j * 2]
                            }

                            // set min Y for top, max Y for bottom
                            if (coordinates[j * 2 + 1] < bounds.top) {
                                bounds.top = coordinates[j * 2 + 1]
                            }
                            if (coordinates[j * 2 + 1] > bounds.bottom) {
                                bounds.bottom = coordinates[j * 2 + 1]
                            }
                        }
                    }
                    boundsOut = bounds
                }
                field = boundsOut
            }
            return field
        }

    init {
        initialCommands = parse(data)
        absolutizedCommands = absolutize(initialCommands)
        normalizedCommands = normalize(absolutizedCommands)
        isUpdated = true
        tempMatrix = MatrixD()
    }

    /**
     * Check whether the path is closed
     */
    fun isClosed(): Boolean {
        // check if last command type is Z for closed path
        val part = absolutizedCommands[absolutizedCommands.size - 1]
        return part.type.toUpperCase() == TYPE_Z
    }

    /**
     * Generate the graphic path from the existing normalized commands, that are cubic bezier curves
     * and return it. Each normalized command that is either 'M' or 'C' type.
     * @param path existing android graphic path
     */
    fun generatePath(path: Path = Path()): Path {

        if (this.normalizedCommands.size > 0) {

            // for each normalized command that is either 'M' or 'C' type
            for (command in this.normalizedCommands) {
                val type = command.type.toUpperCase()
                val normalizedCoordinates = command.coordinates

                if (type == TYPE_M) {
                    path.moveTo(normalizedCoordinates[0].toFloat(), normalizedCoordinates[1].toFloat())
                } else if (type == TYPE_C) {
                    // analogue to a bezier curve
                    path.cubicTo(
                        normalizedCoordinates[0].toFloat(), normalizedCoordinates[1].toFloat(),
                        normalizedCoordinates[2].toFloat(), normalizedCoordinates[3].toFloat(),
                        normalizedCoordinates[4].toFloat(), normalizedCoordinates[5].toFloat()
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
     * Draw given path or generate new path, with the command points and draw it on
     * given canvas with given paint object
     * @param canvas canvas where the paths will be drawn
     * @param paint paint object for setting stroke color, fill color...
     * @param path existing path instead of creating new one each time the method is called
     * @param groupMatrix matrix with transformation from the group, in case path is include in group
     * @param groupRenderProperties render properties for the group, in case path is include in group
     */
    fun draw(
        canvas: Canvas, paint: Paint = Paint().apply { isAntiAlias = true },
        path: Path = generatePath(), groupMatrix: MatrixD? = null, groupRenderProperties: RenderProperties? = null
    ) {

        // it will trigger the callback instead of continuing the drawing from this method
        if (::onDrawListener.isInitialized) {
            onDrawListener.invoke(canvas, paint, path)
            return
        }

        // apply transformations directly to the canvas
        if (groupMatrix != null) {
            tempMatrix.setConcat(groupMatrix, matrix)
            canvas.setMatrix(tempMatrix.matrix)
        } else {
            canvas.setMatrix(matrix.matrix)
        }

        // if render property values are set for the SvgPath then get it from there, if nor get it from the group
        //region get render properties
        val newStrokeWidth = when {
            renderProperties.strokeWidth != null -> {
                renderProperties.strokeWidth!!
            }
            groupRenderProperties?.strokeWidth != null -> {
                groupRenderProperties.strokeWidth!!
            }
            else -> {
                1.0
            }
        }

        val newStrokeJoin = when {
            renderProperties.strokeJoin != null -> {
                renderProperties.strokeJoin!!
            }
            groupRenderProperties?.strokeJoin != null -> {
                groupRenderProperties.strokeJoin!!
            }
            else -> {
                Paint.Join.MITER
            }
        }

        val newStrokeCap = when {
            renderProperties.strokeCap != null -> {
                renderProperties.strokeCap!!
            }
            groupRenderProperties?.strokeCap != null -> {
                groupRenderProperties.strokeCap!!
            }
            else -> {
                Paint.Cap.SQUARE
            }
        }

        val newStrokeColor = when {
            renderProperties.strokeColor != null -> {
                renderProperties.strokeColor!!
            }
            groupRenderProperties?.strokeColor != null -> {
                groupRenderProperties.strokeColor!!
            }
            else -> {
                Color.BLACK
            }
        }

        val newFillColor = when {
            renderProperties.fillColor != null -> {
                renderProperties.fillColor!!
            }
            groupRenderProperties?.fillColor != null -> {
                groupRenderProperties.fillColor!!
            }
            else -> {
                Color.TRANSPARENT
            }
        }

        val newOpacity = if (renderProperties.opacity != null) {
            if (groupRenderProperties?.opacity != null) {
                (renderProperties.opacity!! + groupRenderProperties.opacity!!) / 2
            } else {
                renderProperties.opacity!!
            }
        } else {
            if (groupRenderProperties?.opacity != null) {
                groupRenderProperties.opacity!!
            } else {
                1.0
            }
        }
        //endregion

        // set the render properties for the path
        paint.apply {
            strokeWidth = newStrokeWidth.toFloat()
            strokeJoin = newStrokeJoin
            strokeCap = newStrokeCap
            alpha = (newOpacity * 255).toInt()
            shader = null
        }

        // fill path
        if (renderProperties.fillColor != Color.TRANSPARENT) {
            paint.style = Paint.Style.FILL
            paint.color = newFillColor
            canvas.drawPath(path, paint)
        }

        // stroke path
        if (renderProperties.strokeColor != Color.TRANSPARENT) {
            paint.style = Paint.Style.STROKE
            paint.color = newStrokeColor
            canvas.drawPath(path, paint)
        }
    }

    /**
     * Get the coordinates from the array list containing the initial commands
     */
    fun getInitialCoordinates(): ArrayList<DoubleArray> {
        return Command.getCoordinates(initialCommands)
    }

    /**
     * Get the coordinates from the array list containing the absolutized commands
     */
    fun getAbsolutizedCoordinates(): ArrayList<DoubleArray> {
        return Command.getCoordinates(absolutizedCommands)
    }

    /**
     * Get the coordinates from the array list containing the normalized commands
     */
    fun getNormalizedCoordinates(): ArrayList<DoubleArray> {
        return Command.getCoordinates(normalizedCommands)
    }

    /**
     * Get the transformed coordinates from given matrix, if none is give the matrix for current
     * object is used.
     * @param matrix transformation matrix whit the transformation that will be applied to the path
     */
    fun getTransformedCoordinates(matrix: MatrixD = this.matrix): ArrayList<DoubleArray> {

        val transformedCoordinates = ArrayList<DoubleArray>()
        normalizedCommands.forEach {
            transformedCoordinates.add(it.transform(matrix))
        }
        return transformedCoordinates
    }

    /**
     * Method used for custom drawing, allowing more robust using
     * of the canvas, paint and path objects
     */
    fun onDraw(callback: (canvas: Canvas, paint: Paint, path: Path) -> Unit) {
        onDrawListener = callback
    }

    /**
     * Class with the basic render properties, that are applied to the
     * paint object, when drawing.
     */
    data class RenderProperties(
        var strokeJoin: Paint.Join? = Paint.Join.MITER,
        var strokeCap: Paint.Cap? = Paint.Cap.SQUARE,
        var strokeWidth: Double? = 1.0,
        var strokeColor: Int? = Color.BLACK,
        var fillColor: Int? = Color.TRANSPARENT,
        var opacity: Double? = 1.0
    ) {

        constructor(renderProperties: RenderProperties) : this(renderProperties.strokeJoin, renderProperties.strokeCap, renderProperties.strokeWidth, renderProperties.strokeColor, renderProperties.fillColor, renderProperties.opacity)

        /**
         * Method for cloning the render properties
         */
        fun clone(): RenderProperties {
            return RenderProperties(strokeJoin, strokeCap, strokeWidth, strokeColor, fillColor, opacity)
        }

        override fun toString(): String {
            return "RenderProperties(strokeJoin: $strokeJoin, strokeCap: $strokeCap, strokeWidth: $strokeWidth, strokeColor: $strokeColor, fillColor: $fillColor, opacity: $opacity)"
        }
    }
}

