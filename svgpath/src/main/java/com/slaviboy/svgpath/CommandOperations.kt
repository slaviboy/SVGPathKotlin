package com.slaviboy.svgpath

import android.util.Log
import com.slaviboy.svgpath.Command.Companion.numberOfCoordinates
import java.lang.IllegalArgumentException

/**
 * Object tha contain methods for the conversion of all types to the 'C' type
 * that can then be drawn with the cubicTo() method on any canvas. And methods
 * for the string data extraction to the expected commands.
 */
object CommandOperations {

    val TAU = Math.PI * 2.0

    /**
     * Parse the string data, and separate it to the supposing commands
     * tha are then returned as array list
     * @param path string containing the path data
     * @return array list with all corresponding commands
     */
    fun parse(path: String): ArrayList<Command> {

        val data = ArrayList<Command>()
        val splitByLetter = path.split(Regex("(?=[a-zA-Z])"))

        // for each split string
        for (splitText in splitByLetter) {

            if (splitText.isNotEmpty()) {

                // ge the supposing type and coordinates
                val type = splitText[0]
                val coordinates =
                    Command.parseIntsAndFloats(splitText.subSequence(1, splitText.length).toString())

                if (type.toUpperCase() == 'Z') {
                    data.add(Command('Z'))
                } else {
                    val steps = numberOfCoordinates[type.toLowerCase()]

                    require(steps != null) {
                        "Unknown svg path type: $type"
                    }

                    require(coordinates.size % steps == 0) {
                        "Command with type: ${type}, does not match the expected number of parameters: ${numberOfCoordinates[type.toLowerCase()]}"
                    }

                    // generate commands by separating the expected number of coordinates for each type
                    for (i in 0 until coordinates.size step steps) {
                        data.add(Command(type, ArrayList<Double>(coordinates.subList(i, i + steps))))
                    }
                }
            }
        }
        return data
    }


    /**
     * Convert from endpoint to center parameterization
     * @return array list {cx, cy, theta1, deltaTheta}
     */
    fun getArcCenter(
        x1: Double, y1: Double, x2: Double, y2: Double,
        fa: Double, fs: Double, rx: Double, ry: Double,
        sinPhi: Double, cosPhi: Double
    ): ArrayList<Double> {

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
            radicant = 0.0
        }

        radicant /= (rxSq * y1pSq) + (rySq * x1pSq)
        radicant = Math.sqrt(radicant) * (if (fa == fs) -1 else 1)

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

        val theta1 = unitVectorAngle(1.0, 0.0, v1x, v1y)
        var deltaTheta = unitVectorAngle(v1x, v1y, v2x, v2y)

        if (fs == 0.0 && deltaTheta > 0.0) {
            deltaTheta -= TAU
        }
        if (fs == 1.0 && deltaTheta < 0.0) {
            deltaTheta += TAU
        }

        return arrayListOf(cx, cy, theta1, deltaTheta)
    }


    /**
     *  Calculate an angle between two unit vectors, since we measure angle
     *  between radii of circular arcs, we can use simplified math
     *  (without length normalization)
     */
    fun unitVectorAngle(ux: Double, uy: Double, vx: Double, vy: Double): Double {
        val sign = if (ux * vy - uy * vx < 0.0) -1 else 1
        var dot = ux * vx + uy * vy

        // Add this to work with arbitrary vectors:
        // dot /= Math.sqrt(ux * ux + uy * uy) * Math.sqrt(vx * vx + vy * vy);
        // rounding errors, e.g. -1.0000000000000002 can screw up this
        if (dot > 1.0) {
            dot = 1.0
        }
        if (dot < -1.0) {
            dot = -1.0
        }

        return sign * Math.acos(dot)
    }

    // approximate one unit arc segment with bézier curves
    fun approximateUnitArc(theta1: Double, deltaTheta: Double): ArrayList<Double> {
        val alpha = 4.0 / 3.0 * Math.tan(deltaTheta / 4.0)

        val x1 = Math.cos(theta1)
        val y1 = Math.sin(theta1)
        val x2 = Math.cos(theta1 + deltaTheta)
        val y2 = Math.sin(theta1 + deltaTheta)

        return arrayListOf(
            x1,
            y1,
            x1 - y1 * alpha,
            y1 + x1 * alpha,
            x2 + y2 * alpha,
            y2 - x2 * alpha,
            x2,
            y2
        )
    }

    /**
     * Converts elliptical art, which is type 'A' to a curve which is the
     * expected type 'C' after the conversion
     */
    fun ellipticalArcToCurve(
        x1: Double, y1: Double, x2: Double, y2: Double,
        fa: Double, fs: Double, rx: Double, ry: Double, phi: Double
    ): ArrayList<ArrayList<Double>> {

        val sinPhi = Math.sin(phi * TAU / 360.0)
        val cosPhi = Math.cos(phi * TAU / 360.0)

        // make sure radii are valid
        val x1p = cosPhi * (x1 - x2) / 2.0 + sinPhi * (y1 - y2) / 2.0
        val y1p = -sinPhi * (x1 - x2) / 2.0 + cosPhi * (y1 - y2) / 2.0

        if (x1p == 0.0 && y1p == 0.0) {
            // we're asked to draw line to itself
            return arrayListOf()
        }

        if (rx == 0.0 || ry == 0.0) {
            // one of the radii is zero
            return arrayListOf()
        }

        // compensate out-of-range radii
        var _rx = Math.abs(rx)
        var _ry = Math.abs(ry)

        val lambda = (x1p * x1p) / (_rx * _rx) + (y1p * y1p) / (_ry * _ry)
        if (lambda > 1) {
            _rx *= Math.sqrt(lambda)
            _ry *= Math.sqrt(lambda)
        }

        // get center parameters (cx, cy, theta1, deltaTheta)
        val cc = getArcCenter(x1, y1, x2, y2, fa, fs, _rx, _ry, sinPhi, cosPhi);

        val result = arrayListOf<ArrayList<Double>>()
        var theta1 = cc[2]
        var deltaTheta = cc[3]

        // split an arc to multiple segments, so each segment will be less than τ/4 (= 90°)
        var segments = Math.max(Math.ceil(Math.abs(deltaTheta) / (TAU / 4.0)), 1.0)
        deltaTheta /= segments

        for (i in 0 until segments.toInt()) {
            result.add(approximateUnitArc(theta1, deltaTheta))
            theta1 += deltaTheta
        }

        fun innerFunc(curve: ArrayList<Double>): ArrayList<Double> {
            for (i in curve.indices step 2) {
                var x = curve[i + 0]
                var y = curve[i + 1]

                // scale
                x *= _rx
                y *= _ry

                // rotate
                val xp = cosPhi * x - sinPhi * y
                val yp = sinPhi * x + cosPhi * y

                // translate
                curve[i + 0] = xp + cc[0];
                curve[i + 1] = yp + cc[1];
            }
            return curve
        }

        // we have a bezier approximation of a unit circle, now need to transform back to the original ellipse
        return result.customMap {
            innerFunc(it)
        }
    }

    /**
     * Absolutize the coordinates for all commands.
     */
    fun absolutize(commands: ArrayList<Command>): ArrayList<Command> {

        val start = PointD()
        val p = PointD()

        // inner function that is used with the custom map method
        fun absolutizeInner(command: Command, start: PointD, p: PointD): Command {

            val type = command.type
            val cords = command.coordinates
            val typeUpper = type.toUpperCase()

            // for relative command
            if (type != typeUpper) {
                command.type = typeUpper

                when (type) {
                    'a' -> {
                        cords[5] += p.x
                        cords[6] += p.y
                    }
                    'v' -> {
                        cords[0] += p.y
                    }
                    'h' -> {
                        cords[0] += p.x
                    }
                    else -> {
                        var i = 0
                        while (i < cords.size) {
                            cords[i++] += p.x
                            cords[i++] += p.y
                        }
                    }
                }
            }

            // update cursor state
            when (typeUpper) {
                'Z' -> {
                    p.x = start.x
                    p.y = start.y
                }
                'H' -> {
                    p.x = cords[0]
                }
                'V' -> {
                    p.y = cords[0]
                }
                'M' -> {
                    p.x = cords[0]
                    p.y = cords[1]
                    start.x = p.x
                    start.y = p.y
                }
                else -> {
                    p.x = cords[cords.size - 2]
                    p.y = cords[cords.size - 1]
                }
            }

            return command
        }

        return commands.customMap { it ->
            absolutizeInner(it, start, p)
        }
    }

    /**
     * Normalize all commands, by converting them from there original command type to
     * the 'C' type, that can be drawn with the cubicTo method on regular canvas.
     * Exception is the 'M' - move to command, which remains the same, and is only
     * given the proper starting point.
     */
    fun normalize(commands: ArrayList<Command>): ArrayList<Command> {

        // init state
        var previousType = ' '
        var p = PointD()
        val quad = PointD()
        var start = PointD()
        var bezier = PointD()
        val result = ArrayList<Command>()

        loop@ for (i in commands.indices) {

            var command = commands[i]
            val cords = command.coordinates
            val typeBeforeChange = command.type

            // set the new command that is either kept as M or changed to C
            command = when (command.type) {

                // move to
                'M' -> {
                    start = PointD(cords[0], cords[1])
                    Command.fromPoints('M', PointD(start))
                }

                // elliptical arc
                'A' -> {
                    val curves = ellipticalArcToCurve(
                        p.x, p.y,
                        cords[5], cords[6], cords[3],
                        cords[4], cords[0], cords[1], cords[2]
                    )

                    if (curves.isEmpty()) continue@loop

                    var newCommand = Command()
                    for (j in curves.indices) {
                        val c = curves[j]
                        newCommand = Command.fromPoints(
                            'C',
                            PointD(c[2], c[3]),
                            PointD(c[4], c[5]),
                            PointD(c[6], c[7])
                        )
                        if (j < curves.size - 1) {
                            result.add(newCommand)
                        }
                    }
                    newCommand
                }

                // shorthand/smooth curve to
                'S' -> {

                    // default control point
                    val c = PointD(p)
                    if (previousType == 'C' || previousType == 'S') {
                        c.x += c.x - bezier.x // reflect the previous commandType's control
                        c.y += c.y - bezier.y // point relative to the current point
                    }
                    Command.fromPoints(
                        'C',
                        c,
                        PointD(cords[0], cords[1]),
                        PointD(cords[2], cords[3])
                    )
                }

                // shorthand/smooth quadratic Bézier curve to
                'T' -> {

                    Log.i("jojo", "${previousType}")

                    if (previousType == 'Q' || previousType == 'T') {
                        quad.x = p.x * 2 - quad.x // as with 'S' reflect previous control point
                        quad.y = p.y * 2 - quad.y
                    } else {
                        quad.x = p.x
                        quad.y = p.y
                    }
                    Command.fromQuadratic(p, quad, PointD(cords[0], cords[1]))
                }

                // quadratic Bézier curve to
                'Q' -> {
                    quad.x = cords[0]
                    quad.y = cords[1]
                    Command.fromQuadratic(
                        p,
                        PointD(cords[0], cords[1]),
                        PointD(cords[2], cords[3])
                    )
                }

                // line to
                'L' -> {
                    Command.fromLine(p, PointD(cords[0], cords[1]))
                }

                // horizontal line to
                'H' -> {
                    Command.fromLine(p, PointD(cords[0], p.y))
                }

                // vertical line to
                'V' -> {
                    Command.fromLine(p, PointD(p.x, cords[0]))
                }

                // close path
                'Z' -> {
                    Command.fromLine(p, start)
                }

                // curve to
                'C' -> {
                    Command.fromPoints(
                        'C',
                        PointD(cords[0], cords[1]),
                        PointD(cords[2], cords[3]),
                        PointD(cords[4], cords[5])
                    )
                }

                else -> Command()
            }

            // update states for the next loop
            val points = command.points
            previousType = typeBeforeChange
            p = PointD(points[points.size - 1])
            bezier = if (cords.size > 2) {
                PointD(points[points.size - 2])
            } else {
                PointD(p)
            }
            result.add(command)
        }

        return result
    }

    /**
     * Custom map method, attached to array list
     */
    fun <T> ArrayList<T>.customMap(transform: (T) -> T): ArrayList<T> {
        for (i in this.indices) {
            this[i] = transform(this[i])
        }
        return this
    }
}