package com.slaviboy.svgpath

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.slaviboy.graphics.RectD
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*
import kotlin.collections.ArrayList

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class SvgPathUnitTest {

    /**
     * Class that holds the expected commands as array list of commands and as array list with coordinates
     * @param data raw path data as string
     * @param initial_commands array list with initial commands extracted directly from the raw path data string 'v', 'S', 't', ..
     * @param absolutized_commands array list with absolutized commands, 'v' -> 'V', 'S' -> 'S', 't' -> 'T',...
     * @param normalized_commands array list with normalized commands, 'V' -> 'C', 'S' -> 'C', 'T' -> 'C', ...
     * @param initial_commands_coordinates array list with the coordinates extracted from all initial commands
     * @param absolutized_commands_coordinates array list with the coordinates extracted from all absolutized commands
     * @param normalized_commands_coordinates array list with the coordinates extracted from all normalized commands
     * @param transformed_commands_coordinates array list with the coordinates transformed using the current transformation matrix from all normalized commands
     */
    class CommandOperationsData(
        var data: String, var initial_commands: ArrayList<Command>,
        var absolutized_commands: ArrayList<Command>, var normalized_commands: ArrayList<Command>,
        var initial_commands_coordinates: ArrayList<DoubleArray>, var absolutized_commands_coordinates: ArrayList<DoubleArray>,
        var normalized_commands_coordinates: ArrayList<DoubleArray>, var transformed_commands_coordinates: ArrayList<DoubleArray>
    )

    @Test
    fun MainTest() {

        val gson = Gson()
        val context: Context = ApplicationProvider.getApplicationContext()

        // load json file with expected test values
        val jsonCommandOperationsData = CommandOperationsUnitTest.loadStringFromRawResource(context.resources, R.raw.svg_path_test_data)
        val commandOperationsData = gson.fromJson(jsonCommandOperationsData, CommandOperationsData::class.java)

        val svgPath = SvgPath(commandOperationsData.data)
        assertThat(svgPath.data).isEqualTo(commandOperationsData.data)
        assertThat(svgPath.initialCommands).isEqualTo(commandOperationsData.initial_commands)
        assertThat(svgPath.absolutizedCommands).isEqualTo(commandOperationsData.absolutized_commands)
        assertThat(svgPath.normalizedCommands).isEqualTo(commandOperationsData.normalized_commands)

        // get the bound of the path
        var bound = svgPath.bound
        assertThat(bound).isEqualTo(RectD(18.742656707763672, 4.999999523162842, 450.0, 450.0))

        // apply matrix transformations and get bound of the path, make sure you set {isUpdated=true} to force calculating the new bound
        svgPath.matrix.apply {
            postTranslate(72.3, 123.4)
            postRotate(5.2)
            postScale(2.1, 1.6)
            postSkew(0.1, 0.32)
            postRotate(3.31, 2.6, 33.2)
            postScale(4.2, 1.4, 77.4, 23.1)
            postSkew(0.23, 0.24, 66.6, 1.3)
        }
        svgPath.isUpdated = true
        bound = svgPath.bound
        assertThat(bound).isEqualTo(RectD(567.7666625976562, 731.096435546875, 4421.9111328125, 2840.2587890625))

        val initialCoordinates = svgPath.getInitialCoordinates()
        assertThat(Arrays.deepEquals(initialCoordinates.toArray(), commandOperationsData.initial_commands_coordinates.toArray())).isTrue()

        val absolutizedCoordinates = svgPath.getAbsolutizedCoordinates()
        assertThat(absolutizedCoordinates.toArray()).isEqualTo(commandOperationsData.absolutized_commands_coordinates.toArray())

        val normalizedCoordinates = svgPath.getNormalizedCoordinates()
        assertThat(normalizedCoordinates.toArray()).isEqualTo(commandOperationsData.normalized_commands_coordinates.toArray())

        val transformedCoordinates = svgPath.getTransformedCoordinates()
        assertThat(transformedCoordinates.toArray()).isEqualTo(commandOperationsData.transformed_commands_coordinates.toArray())

    }
}