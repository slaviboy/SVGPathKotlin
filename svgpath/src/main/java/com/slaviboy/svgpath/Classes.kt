package com.slaviboy.svgpath

import android.graphics.Color
import android.graphics.Paint

/**
 * Simple path properties class, that hold the path command, the bound
 * and the render properties. Used to save the last used properties, and
 * then apply transformations.
 */
class PathProperties(
    var pathCommands: ArrayList<Command>,
    var bounds: Bound,
    var renderProperties: RenderProperties
)

/**
 * Simple bound class, similar to Rect and RectF, but working
 * with double values.
 */
class Bound(
    var left: Double = 0.0,
    var top: Double = 0.0,
    var right: Double = 0.0,
    var bottom: Double = 0.0
) {
    override fun toString(): String {
        return "${left},${top},${right},${bottom}"
    }

    fun clone(): Bound {
        return Bound(left, top, right, bottom)
    }
}

/**
 * Simple point class, same as Point and PointF, but working
 * with double values.
 */
class PointD(var x: Double = 0.0, var y: Double = 0.0) {

    constructor(pointD: PointD) : this(pointD.x, pointD.y)

    override fun toString(): String {
        return "PointD(${x}, ${y})"
    }
}

/**
 * Class with the basinc render properties, that are applied to the
 * paint object, when drawing.
 */
class RenderProperties(
    var strokeJoin: Paint.Join = Paint.Join.MITER,
    var strokeCap: Paint.Cap = Paint.Cap.BUTT,
    var strokeWidth: Double = 1.0,
    var strokeStyle: Int = Color.TRANSPARENT,
    var fillStyle: Int = Color.TRANSPARENT,
    var opacity: Double = -1.0
) {
    fun clone(): RenderProperties {
        return RenderProperties(strokeJoin, strokeCap, strokeWidth, strokeStyle, fillStyle)
    }
}

