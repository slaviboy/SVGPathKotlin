package com.slaviboy.svgpath

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import org.junit.Test
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Simple instrumented test, for testing if the generated bitmap by the draw() method
 * matches the expected one.
 */
class SvgPathInstrumentedTest {

    lateinit var context: Context

    /**
     * Class that holds the pixel data of the SvgPath, when it is drawn on to 100x100 bitmap. And it also have
     * the raw path data string.
     * @param data raw path data as string
     * @param pixel_data the expected pixel data that will be generated when the path is drawn on to canvas
     */
    class PixelData(var data: String, var pixel_data: IntArray)

    /**
     * Class that holds each PixelData object for all vector editor software that was tested, those are
     * Expression Design and Adobe Illustrator
     * @param expression_design expected pixel data for path generated using Expression Design
     * @param adobe_illustrator expected pixel data for path generated using Adobe Illustrator
     */
    class PixelDataPack(var expression_design: PixelData, var adobe_illustrator: PixelData)

    @Test
    fun MainTest() {
        context = InstrumentationRegistry.getInstrumentation().targetContext

        val gson = Gson()
        val context: Context = ApplicationProvider.getApplicationContext()

        // load json file with expected test values
        val jsonCommandOperationsData = loadStringFromRawResource(context.resources, R.raw.svg_path_bitmap_pixel_data)
        val pixelDataPack = gson.fromJson(jsonCommandOperationsData, PixelDataPack::class.java)

        // check pixel data for each editor: Expression Design and Adobe Illustrator
        checkPixelData(pixelDataPack.expression_design)
        checkPixelData(pixelDataPack.adobe_illustrator)
    }

    /**
     * Check the pixel data by drawing SvgPath on to 100x100 bitmap with attached canvas to it.
     * Then after drawing it check if bitmap pixel data matches the expected pixel data.
     * @param pixelData object holding the raw string data for the path, and the bitmap pixel data
     */
    fun checkPixelData(pixelData: PixelData) {
        val svgPath = SvgPath(pixelData.data, SvgPath.RenderProperties(strokeWidth = 5.0, strokeColor = Color.GREEN))
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        svgPath.draw(canvas, Paint().apply { isAntiAlias = true })
        checkBitmap(bitmap, pixelData.pixel_data)
    }

    /**
     * Check if bitmap pixel data matches with the expected one give as a IntArray
     * @param bitmap bitmap with the pixel data
     * @param expectedPixelData expected pixel data
     */
    fun checkBitmap(bitmap: Bitmap, expectedPixelData: IntArray) {

        // get the pixel data
        val width = bitmap.width
        val height = bitmap.height
        val pixelData = IntArray(width * height)
        bitmap.getPixels(pixelData, 0, width, 0, 0, width, height)

        assertThat(pixelData).isEqualTo(expectedPixelData)
    }

    companion object {

        /**
         * Load string from the raw folder using a resource id of the given file.
         * @param resources resource from the context
         * @param resId resource id of the file
         */
        fun loadStringFromRawResource(resources: Resources, resId: Int): String {
            val rawResource = resources.openRawResource(resId)
            val content = streamToString(rawResource)
            try {
                rawResource.close()
            } catch (e: IOException) {
                throw e
            }
            return content
        }

        /**
         * Read the file from the raw folder using input stream
         */
        private fun streamToString(inputStream: InputStream): String {
            var l: String?
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()
            try {
                while (bufferedReader.readLine().also { l = it } != null) {
                    stringBuilder.append(l)
                }
            } catch (e: IOException) {
            }
            return stringBuilder.toString()
        }
    }
}