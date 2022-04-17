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

const val PI = Math.PI.toFloat()

fun sin(float: Float): Float {
    return Math.sin(float.toDouble()).toFloat()
}

fun cos(float: Float): Float {
    return Math.cos(float.toDouble()).toFloat()
}

fun acos(float: Float): Float {
    return Math.acos(float.toDouble()).toFloat()
}

fun tan(float: Float): Float {
    return Math.tan(float.toDouble()).toFloat()
}

fun atan(float: Float): Float {
    return Math.atan(float.toDouble()).toFloat()
}

fun abs(float: Float): Float {
    return Math.abs(float)
}

fun sqrt(float: Float): Float {
    return Math.sqrt(float.toDouble()).toFloat()
}

fun ceil(float: Float): Float {
    return Math.ceil(float.toDouble()).toFloat()
}

fun max(a: Float, b: Float): Float {
    return Math.max(a, b)
}