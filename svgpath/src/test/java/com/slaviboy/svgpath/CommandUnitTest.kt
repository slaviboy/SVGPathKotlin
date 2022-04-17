package com.slaviboy.svgpath

import android.graphics.PointF
import com.google.common.truth.Truth.assertThat
import com.slaviboy.svgpath.Command.Companion.TYPE_C
import com.slaviboy.svgpath.Command.Companion.TYPE_M
import org.junit.Test

class CommandUnitTest {

    @Test
    fun MainTest() {

        val command1 = Command("M2.3,45.2")
        assertThat(command1.type).isEqualTo(TYPE_M)
        assertThat(command1.coordinates).isEqualTo(floatArrayOf(2.3f, 45.2f))
        assertThat(command1.toString()).isEqualTo("Command(type: 77, coordinates: 2.3,45.2)")

        StaticMethods()
        DataClassTest()
    }

    /**
     * Test for the data properties, since the Command class is 'data class', which is used
     * for checking if two object of the same class are equal(have same values for all properties)
     */
    fun DataClassTest() {

        val command1 = Command("M2.3,45.2")

        // different coordinates
        val command2 = Command("M2.3,45.1")
        assertThat(command1).isNotEqualTo(command2)

        // different type
        val command3 = Command("T2.3,45.2")
        assertThat(command1).isNotEqualTo(command3)

        // same type and coordinates
        val command4 = Command(TYPE_M, floatArrayOf(2.3f, 45.2f))
        assertThat(command1).isEqualTo(command4)

        // test cloning of command
        val command5 = command1.clone()
        assertThat(command5.type).isEqualTo(TYPE_M)
        assertThat(command5.coordinates).isEqualTo(floatArrayOf(2.3f, 45.2f))
        assertThat(command1).isEqualTo(command5)

    }

    /**
     * Test all static methods for the Command class
     */
    fun StaticMethods() {

        var extractDoubleValues = Command.parseIntsAndDoubles("23.3-12.4-43.2-12.2")
        assertThat(extractDoubleValues).isEqualTo(floatArrayOf(23.3f, -12.4f, -43.2f, -12.2f))
        extractDoubleValues = Command.parseIntsAndDoubles("23.3 -12.4 -43.2 -12.2")
        assertThat(extractDoubleValues).isEqualTo(floatArrayOf(23.3f, -12.4f, -43.2f, -12.2f))

        val extractType = Command.generateType("M****")
        assertThat(extractType).isEqualTo(TYPE_M)

        val coordinates = Command.generateCoordinates("M2.3,45.2")
        assertThat(coordinates).isEqualTo(floatArrayOf(2.3f, 45.2f))

        // create command with coordinates
        val command1 = Command("M2.3,45.2")
        val command2 = Command.fromCoordinates(TYPE_M, 2.3f, 45.2f)
        val command3 = Command.fromPoints(TYPE_M, PointF(2.3f, 45.2f))
        assertThat(command1).isEqualTo(command2)
        assertThat(command1).isEqualTo(command3)

        // create command with normalized coordinates
        val command4 = Command(type = TYPE_C, coordinates = floatArrayOf(142.1f, 54.3f, 66.6f, 3.1f))
        val command5 = Command.fromCoordinates(TYPE_C, 142.1f, 54.3f, 66.6f, 3.1f)
        val command6 = Command.fromPoints(TYPE_C, PointF(142.1f, 54.3f), PointF(66.6f, 3.1f))
        assertThat(command4).isEqualTo(command5)
        assertThat(command4).isEqualTo(command6)

        val command7 = Command.fromLine(35.2f, 5.1f, 99.2f, 66.6f)
        assertThat(command7.type).isEqualTo(TYPE_C)
        assertThat(command7.coordinates).isEqualTo(floatArrayOf(35.2f, 5.1f, 99.2f, 66.6f, 99.2f, 66.6f))

        val command8 = Command.fromQuadratic(92.1f, 41.2f, 44.2f, 66.6f, 21.1f, 91.29f)
        assertThat(command8.type).isEqualTo(TYPE_C)
        assertThat(command8.coordinates).isEqualTo(floatArrayOf(60.16666754484177f, 58.13333465655644f, 36.5000008781751f, 74.83000132322312f, 21.1f, 91.29f))

    }
}