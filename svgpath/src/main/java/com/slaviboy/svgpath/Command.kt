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

import android.graphics.Matrix
import android.graphics.PointF
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Command object for each command type from the svg path data
 * @param type command type of the path 'M', 'H', 'S',...
 * @param coordinates coordinates for the command, used by initial, absolutized and normalized coordinates
 * @example Command('M'.toInt(), floatArrayOf(23.6,-12.4))
 *    => M is the type (move to)
 *    => 23.6,-12.4 are the coordinates x and y for the move to command
 */
data class Command(
    var type: Int = TYPE_M,
    var coordinates: FloatArray = floatArrayOf()
) {

    /**
     * Generate command by svg path data string, for the specific command
     * @param data raw path data as string
     * @example Command("M23.6,-12.4")
     *    => M is the type (move to)
     *    => 23.6,-12.4 are the coordinates that are converted to Float array list
     */
    constructor(data: String) : this(
        generateType(data),
        generateCoordinates(data)
    )

    /**
     * Transform the existing coordinates using the transformation matrix that is passed
     * as argument and return array with transformed coordinates.
     * @param matrix transformation matrix, that will apply the transformation to the existing coordinates
     */
    fun transform(matrix: Matrix): FloatArray {
        val transformedCoordinates = FloatArray(coordinates.size)
        matrix.mapPoints(transformedCoordinates, coordinates)
        return transformedCoordinates
    }

    /**
     * Clone command object
     */
    fun clone(): Command {
        return Command(type, coordinates.clone())
    }

    /**
     * Method that return values of the available properties as string.
     */
    override fun toString(): String {
        return "Command(type: $type, coordinates: ${coordinates.joinToString(",")})"
    }

    /**
     * Method used by the data class, to check if two object from the same class have
     * equal values for the public properties.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Command

        if (type != other.type) return false
        if (!coordinates.contentEquals(other.coordinates)) return false

        return true
    }

    /**
     * Method used by the data class, to check if two object from the same class have
     * equal values for the public properties.
     */
    override fun hashCode(): Int {
        var result = type
        result = 31 * result + coordinates.contentHashCode()
        return result
    }

    companion object {

        /**
         * Extension function that allows for array list with points to be converted to
         * FloatArray.
         */
        fun Array<PointF>.toFloatArray(): FloatArray {
            val array = FloatArray(this.size * 2)
            for (i in this.indices) {
                array[i * 2] = this[i].x
                array[i * 2 + 1] = this[i].y
            }
            return array
        }

        /**
         * Generate 'C' command from line, by two given points, usually used for
         * commands like: lineTo, horizontalLintTo...
         * @param x1 x coordinate from the first point of the line
         * @param y1 y coordinate from the first point of the line
         * @param x2 x coordinate from the second point of the line
         * @param y2 y coordinate from the second point of the line
         */
        fun fromLine(x1: Float, y1: Float, x2: Float, y2: Float): Command {
            return fromCoordinates(TYPE_C, x1, y1, x2, y2, x2, y2)
        }

        /**
         * Generate 'C" command from quadratic points, usually used for
         * quadratic bezier curve
         * @param x1 start point x coordinate
         * @param y1 start point y coordinate
         * @param x control point x coordinate
         * @param y control point y coordinate
         * @param x2 end point x coordinate
         * @param y2 end point y coordinate
         */
        fun fromQuadratic(x1: Float, y1: Float, x: Float, y: Float, x2: Float, y2: Float): Command {
            return fromCoordinates(
                TYPE_C,
                x1 / 3.0f + (2.0f / 3.0f) * x,
                y1 / 3.0f + (2.0f / 3.0f) * y,
                x2 / 3.0f + (2.0f / 3.0f) * x,
                y2 / 3.0f + (2.0f / 3.0f) * y,
                x2, y2
            )
        }

        /**
         * Generate command from coordinates, passed as multiple arguments instead of array.
         * @param type command type as integer representation of the char 'M', 'C',...
         * @param coordinates initial coordinates passes as varargs
         */
        fun fromCoordinates(type: Int, vararg coordinates: Float): Command {
            return Command(type = type, coordinates = coordinates)
        }

        /**
         * Generate command from points, passed as multiple arguments instead of array.
         * @param type command type as integer representation of the char 'M', 'C',...
         * @param points points containing the coordinates for the command passes as varargs
         */
        fun fromPoints(type: Int, vararg points: PointF): Command {
            return fromCoordinates(type, *(points as Array<PointF>).toFloatArray())
        }

        /**
         * Get the coordinates for each command in given array list as FloatArray
         * @param commands array list containing the commands
         */
        fun getCoordinates(commands: ArrayList<Command>): ArrayList<FloatArray> {
            val coordinates = ArrayList<FloatArray>()
            commands.forEach {
                coordinates.add(it.coordinates)
            }
            return coordinates
        }

        /**
         * Get the type, its always the first character from the command string
         * @param commandString string containing the path data
         */
        internal fun generateType(commandString: String): Int {
            return commandString[0].code
        }

        /**
         * Get the coordinates, the values after the type
         * @param commandString string containing the path data
         */
        internal fun generateCoordinates(commandString: String): FloatArray {

            val type = commandString[0].uppercaseChar().code

            // get all coordinates as Float array list
            val coordinates = parseIntsAndDoubles(commandString.subSequence(1, commandString.length).toString())

            // check if the expected number of coordinates for the command is acquired
            if (coordinates.size != numberOfCoordinates[type]) {
                throw IllegalArgumentException("Command with type: ${type}, does not match the expected number of parameters: ${numberOfCoordinates[type]}")
            }

            return coordinates
        }

        /**
         * Extract Float values positive and negative from string,
         * and return them as a Float array
         * @param raw raw string containing Float values
         */
        fun parseIntsAndDoubles(raw: String): FloatArray {
            val listBuffer = ArrayList<Float>()
            val p = Pattern.compile("[-]?[0-9]*\\.?[0-9]+")
            val m: Matcher = p.matcher(raw)
            while (m.find()) {
                listBuffer.add(m.group().toFloat())
            }
            return listBuffer.toFloatArray()
        }

        const val TYPE_NONE: Int = ' '.code

        // elliptical arc
        const val TYPE_a: Int = 'a'.code
        const val TYPE_A: Int = 'A'.code

        // curve to
        const val TYPE_c: Int = 'c'.code
        const val TYPE_C: Int = 'C'.code

        // horizontal line to
        const val TYPE_h: Int = 'h'.code
        const val TYPE_H: Int = 'H'.code

        // line to
        const val TYPE_l: Int = 'l'.code
        const val TYPE_L: Int = 'L'.code

        // move to
        const val TYPE_m: Int = 'm'.code
        const val TYPE_M: Int = 'M'.code

        // quadratic Bézier curve to
        const val TYPE_q: Int = 'q'.code
        const val TYPE_Q: Int = 'Q'.code

        // shorthand/smooth curve to
        const val TYPE_s: Int = 's'.code
        const val TYPE_S: Int = 'S'.code

        // shorthand/smooth quadratic Bézier curve to
        const val TYPE_t: Int = 't'.code
        const val TYPE_T: Int = 'T'.code

        // vertical line to
        const val TYPE_v: Int = 'v'.code
        const val TYPE_V: Int = 'V'.code

        // close path
        const val TYPE_z: Int = 'z'.code
        const val TYPE_Z: Int = 'Z'.code

        // expected number of coordinates for each command type
        val numberOfCoordinates: Map<Int, Int> = mapOf(
            TYPE_A to 7,
            TYPE_C to 6,
            TYPE_H to 1,
            TYPE_L to 2,
            TYPE_M to 2,
            TYPE_Q to 4,
            TYPE_S to 4,
            TYPE_T to 2,
            TYPE_V to 1,
            TYPE_Z to 0
        )
    }
}