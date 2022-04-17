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

import android.graphics.PointF
import com.slaviboy.svgpath.Command.Companion.TYPE_A
import com.slaviboy.svgpath.Command.Companion.TYPE_C
import com.slaviboy.svgpath.Command.Companion.TYPE_H
import com.slaviboy.svgpath.Command.Companion.TYPE_L
import com.slaviboy.svgpath.Command.Companion.TYPE_M
import com.slaviboy.svgpath.Command.Companion.TYPE_NONE
import com.slaviboy.svgpath.Command.Companion.TYPE_Q
import com.slaviboy.svgpath.Command.Companion.TYPE_S
import com.slaviboy.svgpath.Command.Companion.TYPE_T
import com.slaviboy.svgpath.Command.Companion.TYPE_V
import com.slaviboy.svgpath.Command.Companion.TYPE_Z
import com.slaviboy.svgpath.Command.Companion.TYPE_a
import com.slaviboy.svgpath.Command.Companion.TYPE_h
import com.slaviboy.svgpath.Command.Companion.TYPE_v
import com.slaviboy.svgpath.Command.Companion.numberOfCoordinates

/**
 * Object tha contain methods for the conversion of all types to the 'C' type
 * that can then be drawn with the cubicTo() method on any canvas. And methods
 * for the string data extraction to the expected commands.
 */
object CommandOperations {

    internal const val TAU: Float = PI * 2.0f

    // temp points used in the conversion, instead of creating new one each time
    internal val point = PointF()
    internal val quad = PointF()
    internal val start = PointF()
    internal val bezier = PointF()

    /**
     * Parse the string data, and separate it to the supposing commands
     * tha are then returned as array list
     * @param data string containing the path data
     * @return array list with all corresponding commands
     */
    fun parse(data: String): ArrayList<Command> {

        val commands = ArrayList<Command>()
        val splitByLetter = data.split(Regex("(?=[a-zA-Z])"))

        // for each split string
        for (splitText in splitByLetter) {

            if (splitText.isNotEmpty()) {

                // ge the supposing type and coordinates
                val type = splitText[0].code
                val typeUpperCase = splitText[0].uppercaseChar().code
                val coordinates = Command.parseIntsAndDoubles(splitText.subSequence(1, splitText.length).toString())

                if (typeUpperCase == TYPE_Z) {
                    commands.add(Command(TYPE_Z))
                } else {
                    val steps = numberOfCoordinates[typeUpperCase]

                    require(steps != null) {
                        "Unknown svg path type: $type"
                    }

                    require(steps > 0 && coordinates.size % steps == 0) {
                        "Command with type: ${type.toChar()}, does not match the expected number of parameters: ${numberOfCoordinates[typeUpperCase]}"
                    }

                    // generate commands by separating the expected number of coordinates for each type
                    for (i in coordinates.indices step steps) {
                        commands.add(Command(type = type, coordinates = coordinates.copyOfRange(i, i + steps)))
                    }
                }
            }
        }
        return commands
    }

    /**
     * Convert from endpoint to center parameterization
     * @return array list {cx, cy, theta1, deltaTheta}
     */
    fun getArcCenter(
        x1: Float, y1: Float, x2: Float, y2: Float, fa: Float, fs: Float,
        rx: Float, ry: Float, sinPhi: Float, cosPhi: Float
    ): FloatArray {

        /**
         *  moving an ellipse so origin will be the middle point
         *  between our two points. After that, rotate it to line
         *  up ellipse axes with coordinate axes.
         */
        val x1p = cosPhi * (x1 - x2) / 2 + sinPhi * (y1 - y2) / 2
        val y1p = -sinPhi * (x1 - x2) / 2 + cosPhi * (y1 - y2) / 2

        val rxSq = rx * rx
        val rySq = ry * ry
        val x1pSq = x1p * x1p
        val y1pSq = y1p * y1p

        // compute coordinates of the centre of this ellipse (cx', cy') in the new coordinate system.
        var radicant = (rxSq * rySq) - (rxSq * y1pSq) - (rySq * x1pSq)
        if (radicant < 0.0) {
            // due to rounding errors it might be e.g. -1.3877787807814457e-17
            radicant = 0.0f
        }

        radicant /= (rxSq * y1pSq) + (rySq * x1pSq)
        radicant = sqrt(radicant) * (if (fa == fs) -1 else 1)

        val cxp = radicant * rx / ry * y1p
        val cyp = radicant * -ry / rx * x1p

        // transform back to get centre coordinates (cx, cy) in the original coordinate system.
        val cx = cosPhi * cxp - sinPhi * cyp + (x1 + x2) / 2
        val cy = sinPhi * cxp + cosPhi * cyp + (y1 + y2) / 2

        // compute angles (theta1, deltaTheta)
        val v1x = (x1p - cxp) / rx
        val v1y = (y1p - cyp) / ry
        val v2x = (-x1p - cxp) / rx
        val v2y = (-y1p - cyp) / ry

        val theta1 = unitVectorAngle(1.0f, 0.0f, v1x, v1y)
        var deltaTheta = unitVectorAngle(v1x, v1y, v2x, v2y)

        if (fs == 0.0f && deltaTheta > 0.0f) {
            deltaTheta -= TAU
        }
        if (fs == 1.0f && deltaTheta < 0.0f) {
            deltaTheta += TAU
        }

        return floatArrayOf(cx, cy, theta1, deltaTheta)
    }

    /**
     *  Calculate an angle between two unit vectors, since we measure angle
     *  between radii of circular arcs, we can use simplified math
     *  (without length normalization)
     */
    fun unitVectorAngle(ux: Float, uy: Float, vx: Float, vy: Float): Float {

        val sign = if (ux * vy - uy * vx < 0.0) -1 else 1
        var dot = ux * vx + uy * vy

        // add this to work with arbitrary vectors:
        // dot /= sqrt(ux * ux + uy * uy) * sqrt(vx * vx + vy * vy);
        // rounding errors, e.g. -1.0000000000000002 can screw up this
        if (dot > 1.0) {
            dot = 1.0f
        }
        if (dot < -1.0) {
            dot = -1.0f
        }

        return sign * acos(dot)
    }

    /**
     * Approximate one unit arc segment with bézier curves
     */
    fun approximateUnitArc(theta1: Float, deltaTheta: Float): FloatArray {

        val alpha = 4.0f / 3.0f * tan(deltaTheta / 4.0f)

        val x1 = cos(theta1)
        val y1 = sin(theta1)
        val x2 = cos(theta1 + deltaTheta)
        val y2 = sin(theta1 + deltaTheta)

        return floatArrayOf(
            x1, y1,
            x1 - y1 * alpha, y1 + x1 * alpha,
            x2 + y2 * alpha, y2 - x2 * alpha,
            x2, y2
        )
    }

    /**
     * Converts elliptical art, which is type 'A' to a curve which is the
     * expected type 'C' after the conversion
     */
    fun ellipticalArcToCurve(
        x1: Float, y1: Float, x2: Float, y2: Float,
        fa: Float, fs: Float, rx: Float, ry: Float, phi: Float
    ): ArrayList<FloatArray> {

        val sinPhi = sin(phi * TAU / 360.0f)
        val cosPhi = cos(phi * TAU / 360.0f)

        // make sure radii are valid
        val x1p = cosPhi * (x1 - x2) / 2.0f + sinPhi * (y1 - y2) / 2.0f
        val y1p = -sinPhi * (x1 - x2) / 2.0f + cosPhi * (y1 - y2) / 2.0f

        // we're asked to draw line to itself
        if (x1p == 0.0f && y1p == 0.0f) {
            return arrayListOf()
        }

        // one of the radii is zero
        if (rx == 0.0f || ry == 0.0f) {
            return arrayListOf()
        }

        // compensate out-of-range radii
        var rx = abs(rx)
        var ry = abs(ry)

        val lambda = (x1p * x1p) / (rx * rx) + (y1p * y1p) / (ry * ry)
        if (lambda > 1) {
            rx *= sqrt(lambda)
            ry *= sqrt(lambda)
        }

        // get center parameters (cx, cy, theta1, deltaTheta)
        val cc = getArcCenter(x1, y1, x2, y2, fa, fs, rx, ry, sinPhi, cosPhi)

        val result = arrayListOf<FloatArray>()
        var theta1 = cc[2]
        var deltaTheta = cc[3]

        // split an arc to multiple segments, so each segment will be less than τ/4 (= 90°)
        val segments = max(ceil(abs(deltaTheta) / (TAU / 4.0f)), 1.0f)
        deltaTheta /= segments

        for (i in 0 until segments.toInt()) {
            result.add(approximateUnitArc(theta1, deltaTheta))
            theta1 += deltaTheta
        }

        fun innerFunc(curve: FloatArray): FloatArray {
            for (i in curve.indices step 2) {
                var x = curve[i + 0]
                var y = curve[i + 1]

                // scale
                x *= rx
                y *= ry

                // rotate
                val xp = cosPhi * x - sinPhi * y
                val yp = sinPhi * x + cosPhi * y

                // translate
                curve[i + 0] = xp + cc[0]
                curve[i + 1] = yp + cc[1]
            }
            return curve
        }

        // we have a bezier approximation of a unit circle, now need to transform back to the original ellipse
        return result.customMap {
            innerFunc(it)
        }
    }

    /**
     * Extension function that convert a integer value that holds the corresponding char values and
     * try to convert it to upper case. {a = 97} -> {A=65}
     */
    fun Int.toUpperCase(): Int {
        return if (this in 97..122) {
            this - 32
        } else {
            this
        }
    }

    /**
     * Extension function that convert a integer value that holds the corresponding char values and
     * try to convert it to lower case. {A=65} -> {a = 97}
     */
    fun Int.toLowerCase(): Int {
        return if (this in 65..90) {
            this + 32
        } else {
            this
        }
    }

    /**
     * Absolutize the coordinates for all commands, that converts the relative commands to absolute
     * command for example 'v' to 'V', 'h' to 'H'... You can red more about relative and absolute
     * commands here: https://www.w3.org/TR/SVG/paths.html
     * @param commands array list with commands for absolutization
     */
    fun absolutize(commands: ArrayList<Command>): ArrayList<Command> {

        // inner function that is used with the custom map method
        fun absolutizeInner(command: Command, start: PointF, point: PointF): Command {

            val type = command.type
            val typeUpper = type.toUpperCase()
            val newCommand = Command(typeUpper, command.coordinates.clone())

            // for relative command, those that have type with lowercase 'a', 'v', 'h', ...
            if (type != typeUpper) {

                when (type) {
                    TYPE_a -> {
                        newCommand.coordinates[5] += point.x
                        newCommand.coordinates[6] += point.y
                    }
                    TYPE_v -> {
                        newCommand.coordinates[0] += point.y
                    }
                    TYPE_h -> {
                        newCommand.coordinates[0] += point.x
                    }
                    else -> {
                        var i = 0
                        while (i < newCommand.coordinates.size) {
                            newCommand.coordinates[i++] += point.x
                            newCommand.coordinates[i++] += point.y
                        }
                    }
                }
            }

            // update cursor state
            when (typeUpper) {
                TYPE_Z -> {
                    point.x = start.x
                    point.y = start.y
                }
                TYPE_H -> {
                    point.x = newCommand.coordinates[0]
                }
                TYPE_V -> {
                    point.y = newCommand.coordinates[0]
                }
                TYPE_M -> {
                    point.x = newCommand.coordinates[0]
                    point.y = newCommand.coordinates[1]
                    start.x = point.x
                    start.y = point.y
                }
                else -> {
                    point.x = newCommand.coordinates[newCommand.coordinates.size - 2]
                    point.y = newCommand.coordinates[newCommand.coordinates.size - 1]
                }
            }

            return newCommand
        }

        return commands.customMap { it ->
            absolutizeInner(it, start, point)
        }
    }

    /**
     * Normalize all commands, by converting them from there original command type to the 'C' type,
     * that can be drawn with the cubicTo method on regular canvas. Exception is the 'M' - move to
     * command, which remains the same, and is only given the proper starting point. That means all
     * commands with types 'V', 'H', 'S'... are converted to type 'C' command.
     * @param commands array list with commands for normalization
     */
    fun normalize(commands: ArrayList<Command>): ArrayList<Command> {

        // init state
        var previousType = TYPE_NONE
        val newCommands = ArrayList<Command>()

        loop@ for (i in commands.indices) {

            val command = commands[i]
            val coordinates = command.coordinates
            val typeBeforeChange = command.type

            // set the new command that is either kept as M or changed to C
            val normalizedCommand = when (command.type) {

                // move to
                TYPE_M -> {
                    start.x = coordinates[0]
                    start.y = coordinates[1]
                    Command.fromCoordinates(TYPE_M, start.x, start.y)
                }

                // elliptical arc
                TYPE_A -> {
                    val curves = ellipticalArcToCurve(
                        point.x, point.y,
                        coordinates[5], coordinates[6], coordinates[3],
                        coordinates[4], coordinates[0], coordinates[1], coordinates[2]
                    )

                    if (curves.isEmpty()) continue@loop

                    var newCommand = Command()
                    for (j in curves.indices) {
                        val c = curves[j]
                        newCommand = Command.fromCoordinates(TYPE_C, c[2], c[3], c[4], c[5], c[6], c[7])
                        if (j < curves.size - 1) {
                            newCommands.add(newCommand)
                        }
                    }
                    newCommand
                }

                // shorthand/smooth curve to
                TYPE_S -> {

                    // default control point
                    var x = point.x
                    var y = point.y
                    if (previousType == TYPE_C || previousType == TYPE_S) {
                        x += x - bezier.x // reflect the previous commandType's control
                        y += y - bezier.y // point relative to the current point
                    }
                    Command.fromCoordinates(TYPE_C, x, y, coordinates[0], coordinates[1], coordinates[2], coordinates[3])
                }

                // shorthand/smooth quadratic Bézier curve to
                TYPE_T -> {

                    if (previousType == TYPE_Q || previousType == TYPE_T) {
                        quad.x = point.x * 2 - quad.x // as with 'S' reflect previous control point
                        quad.y = point.y * 2 - quad.y
                    } else {
                        quad.x = point.x
                        quad.y = point.y
                    }
                    Command.fromQuadratic(point.x, point.y, quad.x, quad.y, coordinates[0], coordinates[1])
                }

                // quadratic Bézier curve to
                TYPE_Q -> {
                    quad.x = coordinates[0]
                    quad.y = coordinates[1]
                    Command.fromQuadratic(point.x, point.y, coordinates[0], coordinates[1], coordinates[2], coordinates[3])
                }

                // line to
                TYPE_L -> {
                    Command.fromLine(point.x, point.y, coordinates[0], coordinates[1])
                }

                // horizontal line to
                TYPE_H -> {
                    Command.fromLine(point.x, point.y, coordinates[0], point.y)
                }

                // vertical line to
                TYPE_V -> {
                    Command.fromLine(point.x, point.y, point.x, coordinates[0])
                }

                // close path
                TYPE_Z -> {
                    Command.fromLine(point.x, point.y, start.x, start.y)
                }

                // curve to
                TYPE_C -> {
                    Command.fromCoordinates(TYPE_C, coordinates[0], coordinates[1], coordinates[2], coordinates[3], coordinates[4], coordinates[5])
                }

                // empty command, for unrecognized type
                else -> Command()
            }

            // update states for the next loop
            val normalizedCoordinates = normalizedCommand.coordinates
            val size = normalizedCoordinates.size
            previousType = typeBeforeChange

            // get last point that will be start point for next command
            if (size >= 2) {
                point.x = normalizedCoordinates[size - 2]
                point.y = normalizedCoordinates[size - 1]
            }

            if (size >= 4) {
                bezier.x = normalizedCoordinates[size - 4]
                bezier.y = normalizedCoordinates[size - 3]
            } else {
                bezier.x = point.x
                bezier.y = point.y
            }
            newCommands.add(normalizedCommand)
        }

        return newCommands
    }

    /**
     * Custom map method, attached to array list. This extension function allows
     * to call method for array list adn that way mapping the values.
     * @param transform transformation function used for updating values
     */
    fun <T> ArrayList<T>.customMap(transform: (T) -> T): ArrayList<T> {
        for (i in this.indices) {
            this[i] = transform(this[i])
        }
        return this
    }
}