package com.slaviboy.svgpath

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
 * Simple transformation matrix of a 2D plane implemented in kotlin, with some basic methods
 * It represents matrix(a, b, c, d, tx, ty) which is a shorthand for
 * matrix3d(a, b, 0, 0, c, d, 0, 0, 0, 0, 1, 0, tx, ty, 0, 1)
 *
 * Default matrix is
 * (1, 0, 0)
 * (0, 1, 0)
 */
class Matrix(var m: ArrayList<Double> = arrayListOf(1.0, 0.0, 0.0, 1.0, 0.0, 0.0)) {

    constructor(vararg mElements: Double) : this(mElements.toCollection(ArrayList<Double>()))

    companion object {

        /**
         * Set as the horizontal flip matrix
         */
        fun flipHorizontal(): Matrix {
            return Matrix(-1.0, 0.0, 0.0, 1.0, 0.0, 0.0)
        }

        /**
         * Set as rhe vertical flip matrix
         */
        fun flipVertical(): Matrix {
            return Matrix(1.0, 0.0, 0.0, -1.0, 0.0, 0.0)
        }

        /**
         * Set as the central flip matrix
         */
        fun flipCentral(): Matrix {
            return Matrix(-1.0, 0.0, 0.0, -1.0, 0.0, 0.0)
        }

    }

    /**
     * Reset current matrix with the default values and
     * return it
     */
    fun reset(): Matrix {
        m = arrayListOf(1.0, 0.0, 0.0, 1.0, 0.0, 0.00)
        return this
    }

    /**
     * Multiply current matrix with another one, with
     * same size.
     * @param m the second matrix
     */
    fun multiply(m: ArrayList<Double>): Matrix {
        val m1 = this.m
        val m2 = m

        val m11 = m1[0] * m2[0] + m1[2] * m2[1]
        val m12 = m1[1] * m2[0] + m1[3] * m2[1]
        val m21 = m1[0] * m2[2] + m1[2] * m2[3]
        val m22 = m1[1] * m2[2] + m1[3] * m2[3]

        val dx = m1[0] * m2[4] + m1[2] * m2[5] + m1[4]
        val dy = m1[1] * m2[4] + m1[3] * m2[5] + m1[5]

        m1[0] = m11
        m1[1] = m12
        m1[2] = m21
        m1[3] = m22
        m1[4] = dx
        m1[5] = dy

        return this
    }

    fun multiply(matrix: Matrix): Matrix {
        return multiply(matrix.m)
    }

    fun multiply(vararg m: Double): Matrix {
        return multiply(m.toCollection(ArrayList<Double>()))
    }

    /**
     * Inverse the current matrix, and return it
     */
    fun inverse(): Matrix {

        val inv = Matrix(this.m)
        val invm = inv.m

        val d = 1 / (invm[0] * invm[3] - invm[1] * invm[2])
        val m0 = invm[3] * d
        val m1 = -invm[1] * d
        val m2 = -invm[2] * d
        val m3 = invm[0] * d
        val m4 = d * (invm[2] * invm[5] - invm[3] * invm[4])
        val m5 = d * (invm[1] * invm[4] - invm[0] * invm[5])

        invm[0] = m0
        invm[1] = m1
        invm[2] = m2
        invm[3] = m3
        invm[4] = m4
        invm[5] = m5

        return inv
    }

    /**
     * Set rotation value by given degree represented as
     * double value.
     * (cos, -sin, 0)
     * (sin,  cos, 0)
     * @param degree rotational degree
     */
    fun rotate(degree: Double): Matrix {
        val rad = degree * Math.PI / 180
        val c = Math.cos(rad)
        val s = Math.sin(rad)
        return this.multiply(c, s, -s, c, 0.0, 0.0)
    }

    /**
     * Set translation values for vertical and horizontal
     * direction, using separate double values.
     * (1, 0, sx)
     * (0, 1, sy)
     * @param x translation value for horizontal direction
     * @param y translation value for vertical direction
     */
    fun translate(x: Double = 0.0, y: Double = 0.0): Matrix {
        return this.multiply(1.0, 0.0, 0.0, 1.0, x, y)
    }

    fun translate(xy: PointD = PointD()): Matrix {
        return this.translate(xy.x, xy.y)
    }

    /**
     * Set skew values by given degrees for each direction
     * (vertical and horizontal) as double values.
     * (1, tx, 0)
     * (ty, 1, 0)
     * @param degreeX skew value for horizontal direction
     * @param degreeY skew value for vertical direction
     */
    fun skew(degreeX: Double = 0.0, degreeY: Double = 0.0): Matrix {

        // convert to radians
        val radX = degreeX * Math.PI / 180
        val radY = degreeY * Math.PI / 180

        val tx = Math.tan(radX)
        val ty = Math.tan(radY)
        return this.multiply(1.0, ty, tx, 1.0, 0.0, 0.0)
    }

    fun skew(degreeXY: PointD = PointD()): Matrix {
        return this.skew(degreeXY.x, degreeXY.y)
    }

    /**
     * Set scale values for each direction (vertical and horizontal)
     * as double values.
     * (sx, 0, 0)
     * (0, sy, 0)
     * @param x scale value for horizontal direction
     * @param y scale value for vertical direction
     */
    fun scale(x: Double = 0.0, y: Double = 0.0): Matrix {
        return this.multiply(x, 0.0, 0.0, y, 0.0, 0.0)
    }

    fun scale(xy: PointD = PointD()): Matrix {
        return this.scale(xy.x, xy.y)
    }

    /**
     * Apply the transformation on point coordinates and return the
     * result point with all applied transformations.
     */
    fun transformPoint(x: Double = 0.0, y: Double = 0.0): PointD {
        return PointD(
            x * this.m[0] + y * this.m[2] + this.m[4],
            x * this.m[1] + y * this.m[3] + this.m[5]
        )
    }

    fun transformPoint(p: PointD): PointD {
        return transformPoint(p.x, p.y)
    }

    fun transformVector(x: Double = 0.0, y: Double = 0.0): PointD {
        return PointD(
            x * this.m[0] + y * this.m[2],
            x * this.m[1] + y * this.m[3]
        )
    }

    fun transformVector(xy: PointD = PointD()): PointD {
        return transformVector(xy.x, xy.y)
    }

    override fun toString(): String {
        return m.joinToString(",")
    }
}