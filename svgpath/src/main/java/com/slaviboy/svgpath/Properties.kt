package com.slaviboy.svgpath

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

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
    var bounds: RectF,
    var renderProperties: RenderProperties
)

/**
 * Class with the basinc render properties, that are applied to the
 * paint object, when drawing.
 */
class RenderProperties(
    var strokeJoin: Paint.Join = Paint.Join.MITER,
    var strokeCap: Paint.Cap = Paint.Cap.BUTT,
    var strokeWidth: Float = 1.0f,
    var strokeStyle: Int = Color.TRANSPARENT,
    var fillStyle: Int = Color.TRANSPARENT,
    var opacity: Float = -1.0f
) {
    fun clone(): RenderProperties {
        return RenderProperties(strokeJoin, strokeCap, strokeWidth, strokeStyle, fillStyle)
    }
}

