package com.slaviboy.svgpath

import android.util.Log
import java.lang.IllegalArgumentException
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Command object for each command type from the svg path data
 * @param type command type
 * @param coordinates array with coordinates
 * @param points array with points transformed from original command to 'C'
 * @example
 * Command('M', arrayListOf(23.6,-12.4))
 *      => M is the type (move to)
 *      => 23.6,-12.4 are the coordinates x and y for the move to command
 */
class Command(
    var type: Char = TYPE_M.toChar(),
    var coordinates: ArrayList<Double> = ArrayList(),
    var points: ArrayList<PointD> = ArrayList()
) {

    /**
     * Generate command by svg path data string, for the specific command
     * @param commandString string with the command from the path data
     *
     * @example
     * Command("M23.6,-12.4")
     *      => M is the type (move to)
     *      => 23.6,-12.4 are the coordinates that are converted to double array list
     */
    constructor(commandString: String) : this(
        generateType(commandString),
        generateCoordinates(commandString)
    )

    /**
     * Usually used for command with already converted points, from the previous command
     * type to 'C' command type. But since the 'M' command can also be used the
     * char type is acquires.
     * @param type command type 'M' or 'C'
     * @param points array with points for the path command
     */
    constructor(type: Char, vararg points: PointD) : this(type) {
        this.points = points.toCollection(ArrayList())
    }

    companion object {

        /**
         * Generate 'C' command from line, by two given points, usually used for
         * commands like: lineTo, horizontalLintTo...
         * @param p1 first point
         * @param p2 second point
         */
        fun fromLine(p1: PointD, p2: PointD): Command {
            return Command('C', PointD(p1), PointD(p2), PointD(p2))
        }

        /**
         * Generate 'C" command from quadratic points, usually used for
         * quadratic bezier curve
         * @param p1 start point
         * @param c control point
         * @param p2 end point
         */
        fun fromQuadratic(p1: PointD, c: PointD, p2: PointD): Command {
            return Command(
                'C',
                PointD(p1.x / 3.0 + (2.0 / 3.0) * c.x, p1.y / 3.0 + (2.0 / 3.0) * c.y),
                PointD(p2.x / 3.0 + (2.0 / 3.0) * c.x, p2.y / 3.0 + (2.0 / 3.0) * c.y),
                PointD(p2.x, p2.y)
            )
        }

        /**
         * Generate command from points, passed as multiple arguments instead of
         * array list.
         * @param type command type
         * @param points points object as arguments
         */
        fun fromPoints(type: Char, vararg points: PointD): Command {
            // use the spread operator *, to send the varargs
            return Command(type, *points)
        }

        /**
         * Generate command from coordinates, passed as multiple arguments instead of
         * array list.
         */
        fun fromCoordinates(type: Char, vararg coordinates: Double): Command {
            // use the spread operator *, to send the varargs
            return Command(type, coordinates.toCollection(ArrayList<Double>()))
        }

        /**
         * Get the type, its the first character from the command string
         * @param commandString string containing the path data
         */
        private fun generateType(commandString: String): Char {
            return commandString[0]
        }

        /**
         * Get the coordinates, the values after the type
         * @param commandString string containing the path data
         */
        private fun generateCoordinates(commandString: String): ArrayList<Double> {

            val type = commandString[0]

            // get all coordinates as double array list
            val coordinates =
                parseIntsAndFloats(commandString.subSequence(1, commandString.length).toString())

            // check if the expected number of coordinates for the command is acquired
            if (coordinates.size != numberOfCoordinates[type.toLowerCase()]) {
                throw IllegalArgumentException("Command with type: ${type}, does not match the expected number of parameters: ${numberOfCoordinates[type.toLowerCase()]}")
            }

            return coordinates
        }

        /**
         * Extract double values positive and negative from string,
         * and return them as a double array
         * @param raw raw string containing double values
         */
         fun parseIntsAndFloats(raw: String): ArrayList<Double> {
            val listBuffer = ArrayList<Double>()
            val p = Pattern.compile("[-]?[0-9]*\\.?[0-9]+")
            val m: Matcher = p.matcher(raw)
            while (m.find()) {
                listBuffer.add(m.group().toDouble())
            }
            return listBuffer
        }

        // expected number of coordinates for each command type
        val numberOfCoordinates = mapOf(
            'a' to 7,
            'c' to 6,
            'h' to 1,
            'l' to 2,
            'm' to 2,
            'q' to 4,
            's' to 4,
            't' to 2,
            'v' to 1,
            'z' to 0
        )

        val TYPE_UNDEFINED: Int = 0

        // elliptical arc
        val TYPE_a: Int = 'a'.toInt()
        val TYPE_A: Int = 'A'.toInt()

        // curve to
        val TYPE_c: Int = 'c'.toInt()
        val TYPE_C: Int = 'C'.toInt()

        // horizontal line to
        val TYPE_h: Int = 'h'.toInt()
        val TYPE_H: Int = 'H'.toInt()

        // line to
        val TYPE_l: Int = 'l'.toInt()
        val TYPE_L: Int = 'L'.toInt()

        // move to
        val TYPE_m: Int = 'm'.toInt()
        val TYPE_M: Int = 'M'.toInt()

        // quadratic Bézier curve to
        val TYPE_q: Int = 'q'.toInt()
        val TYPE_Q: Int = 'Q'.toInt()

        // shorthand/smooth curve to
        val TYPE_s: Int = 's'.toInt()
        val TYPE_S: Int = 'S'.toInt()

        // shorthand/smooth quadratic Bézier curve to
        val TYPE_t: Int = 't'.toInt()
        val TYPE_T: Int = 'T'.toInt()

        // vertical line to
        val TYPE_v: Int = 'v'.toInt()
        val TYPE_V: Int = 'V'.toInt()

        // close path
        val TYPE_z: Int = 'z'.toInt()
        val TYPE_Z: Int = 'Z'.toInt()
    }

    /**
     * Add coordinates to the array with coordinates
     */
    fun addCoordinates(vararg coordinates: Double) {
        for (coordinate in coordinates) {
            this.coordinates.add(coordinate)
        }
    }

    /**
     * Clone command object
     */
    fun clone(): Command {
        return Command(type, ArrayList(coordinates), ArrayList(points))
    }

    override fun toString(): String {
        return "Command(type: $type coordinates: ${coordinates.joinToString(",")})"
    }

    fun compactString(): String {
        return "${type},${coordinates.joinToString(",")}"
    }
}