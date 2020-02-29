package com.slaviboy.svgpath

import android.graphics.Color
import android.graphics.Paint

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

