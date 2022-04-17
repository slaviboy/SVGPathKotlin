package com.slaviboy.svgpathexample.extensions

import android.graphics.PointF
import android.graphics.RectF

fun RectF.center(): PointF {
    return PointF(centerX(), centerY())
}