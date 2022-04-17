# SVGPathKotlin
Simple library for drawing [Path](https://developer.android.com/reference/android/graphics/Path) from svg path data written in Kotlin

<p align="center">
    <img src="screens/home.png" alt="Image"   />
</p>
 
## About
SVGPathKotlin is simple library that generate commands that are used for creating [Path](https://developer.android.com/reference/android/graphics/Path) with applied curves, that can later be drawn to Canvas on any View. Commands are extracted from raw path data string that is passed as argument to the constructor of the SvgPath class. If you want to learn more about the library and how it works you check the official [wiki](https://github.com/slaviboy/SVGPathKotlin/wiki) page.

[![Platform](https://img.shields.io/badge/platform-android-green.svg)](http://developer.android.com/index.html)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
[![Download](https://img.shields.io/badge/version-0.3.0-blue)](https://github.com/slaviboy/SVGPathKotlin/releases/tag/v0.3.0)

## Add to your project
Add the jitpack maven repository
```
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
``` 
Add the dependency
```
dependencies { 
  implementation 'com.github.slaviboy:SVGPathKotlin:0.3.0'
}
```
 
### How to use
Create SvgPath object and pass you svg path data as string. To apply any of the available transformations: **rotation**, **scale**, **skew** or **translate** to the path, use the _**matrix**_ variable. To change properties for the Paint object such as: strokeWidth, strokeColor, fillColor, opacity when the path is being drawn to the canvas use the _**renderProperties**_ variable.  
```kotlin

// create SvgPath object using raw path data as string
val svgPath = SvgPath("M 10 80 Q 52.5 10, 95 80 T 180 80")

// change stroke color and width for the path
svgPath.renderProperties.apply { 
     strokeWidth = 5.0f
     strokeColor = Color.GREEN
}
        
// apply transformation to the path using its matrix
svgPath.matrix.apply {
     postRotate(45.0f)
     postSkew(0.5f, 0.0f)
}
```
 
_**To check the available example on creating SvgPath and SvgPathGroup check the classes in the [views](https://github.com/slaviboy/SVGPathKotlin/tree/master/app/src/main/java/com/slaviboy/svgpathexample/views) package.**_
 
