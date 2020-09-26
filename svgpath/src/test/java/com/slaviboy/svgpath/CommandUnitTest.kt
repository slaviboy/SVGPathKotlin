package com.slaviboy.svgpath

import com.google.common.truth.Truth.assertThat
import com.slaviboy.graphics.PointD
import com.slaviboy.svgpath.Command.Companion.TYPE_C
import com.slaviboy.svgpath.Command.Companion.TYPE_M
import org.junit.Test

class CommandUnitTest {

    @Test
    fun MainTest() {

        val command1 = Command("M2.3,45.2")
        assertThat(command1.type).isEqualTo(TYPE_M)
        assertThat(command1.coordinates).isEqualTo(doubleArrayOf(2.3, 45.2))
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
        val command4 = Command(TYPE_M, doubleArrayOf(2.3, 45.2))
        assertThat(command1).isEqualTo(command4)

        // test cloning of command
        val command5 = command1.clone()
        assertThat(command5.type).isEqualTo(TYPE_M)
        assertThat(command5.coordinates).isEqualTo(doubleArrayOf(2.3, 45.2))
        assertThat(command1).isEqualTo(command5)

    }

    /**
     * Test all static methods for the Command class
     */
    fun StaticMethods() {

        var extractDoubleValues = Command.parseIntsAndDoubles("23.3-12.4-43.2-12.2")
        assertThat(extractDoubleValues).isEqualTo(doubleArrayOf(23.3, -12.4, -43.2, -12.2))
        extractDoubleValues = Command.parseIntsAndDoubles("23.3 -12.4 -43.2 -12.2")
        assertThat(extractDoubleValues).isEqualTo(doubleArrayOf(23.3, -12.4, -43.2, -12.2))

        val extractType = Command.generateType("M****")
        assertThat(extractType).isEqualTo(TYPE_M)

        val coordinates = Command.generateCoordinates("M2.3,45.2")
        assertThat(coordinates).isEqualTo(doubleArrayOf(2.3, 45.2))

        // create command with coordinates
        val command1 = Command("M2.3,45.2")
        val command2 = Command.fromCoordinates(TYPE_M, 2.3, 45.2)
        val command3 = Command.fromPoints(TYPE_M, PointD(2.3, 45.2))
        assertThat(command1).isEqualTo(command2)
        assertThat(command1).isEqualTo(command3)

        // create command with normalized coordinates
        val command4 = Command(type = TYPE_C, coordinates = doubleArrayOf(142.1, 54.3, 66.6, 3.1))
        val command5 = Command.fromCoordinates(TYPE_C, 142.1, 54.3, 66.6, 3.1)
        val command6 = Command.fromPoints(TYPE_C, PointD(142.1, 54.3), PointD(66.6, 3.1))
        assertThat(command4).isEqualTo(command5)
        assertThat(command4).isEqualTo(command6)

        val command7 = Command.fromLine(35.2, 5.1, 99.2, 66.6)
        assertThat(command7.type).isEqualTo(TYPE_C)
        assertThat(command7.coordinates).isEqualTo(doubleArrayOf(35.2, 5.1, 99.2, 66.6, 99.2, 66.6))

        val command8 = Command.fromQuadratic(92.1, 41.2, 44.2, 66.6, 21.1, 91.29)
        assertThat(command8.type).isEqualTo(TYPE_C)
        assertThat(command8.coordinates).isEqualTo(doubleArrayOf(60.16666754484177, 58.13333465655644, 36.5000008781751, 74.83000132322312, 21.1, 91.29))

    }
}