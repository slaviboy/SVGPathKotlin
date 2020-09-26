package com.slaviboy.svgpath

import android.content.Context
import android.content.res.Resources
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.slaviboy.svgpath.CommandOperations.toLowerCase
import com.slaviboy.svgpath.CommandOperations.toUpperCase
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class CommandOperationsUnitTest {

    /**
     * Class that holds the raw string data for the command operations and the expected values
     * for the generated commands, absolutized commands and normalized commands.
     * @param data raw path data as string
     * @param initial_commands array list with initial commands extracted directly from the raw path data string 'v', 'S', 't', ..
     * @param absolutized_commands array list with absolutized commands, 'v' -> 'V', 'S' -> 'S', 't' -> 'T',...
     * @param normalized_commands array list with normalized commands, 'V' -> 'C', 'S' -> 'C', 'T' -> 'C', ...
     */
    class CommandOperationsData(
        var data: String,
        var initial_commands: ArrayList<Command>,
        var absolutized_commands: ArrayList<Command>,
        var normalized_commands: ArrayList<Command>
    )

    /**
     * Class that holds each CommandOperationsData object for all vector editor software that was tested, those are
     * Expression Design and Adobe Illustrator and multiple: generated manually
     * @param multiple expected commands for path that was generated by hand
     * @param expression_design expected commands for path that was generated using Expression Design
     * @param adobe_illustrator expected commands for path that was generated using Adobe Illustrator
     */
    class CommandOperationDataPack(
        var multiple: CommandOperationsData,
        var expression_design: CommandOperationsData,
        var adobe_illustrator: CommandOperationsData
    )

    @Test
    fun MainTest() {

        UpperLowerCase()
        ParseData()
    }


    /**
     * Method for testing the raw string data parse from the test.json file. That file holds
     * the raw input string data and the expected values for the generated commands,
     * absolutized commands and normalized commands.
     */
    fun ParseData() {

        val gson = Gson()
        val context: Context = ApplicationProvider.getApplicationContext()

        // load json file with expected test values
        val jsonCommandOperationsData = loadStringFromRawResource(context.resources, R.raw.command_operations_test_data)
        val commandOperationsDataPack = gson.fromJson(jsonCommandOperationsData, CommandOperationDataPack::class.java)

        // test multiple commands, that were generated manually
        TestCommands(commandOperationsDataPack.multiple)

        // test commands parse from Microsoft Expression Design
        TestCommands(commandOperationsDataPack.expression_design)

        // test commands parse from Adobe Illustrator
        TestCommands(commandOperationsDataPack.adobe_illustrator)
    }

    /**
     * Test the commands by passing raw string, and check if commands matches with the generating the commands,
     * absolutized commands and normalized commands.
     * @param commandOperationsData object holding the expected command values
     */
    fun TestCommands(commandOperationsData: CommandOperationsData) {

        val data: String = commandOperationsData.data
        val initialCommands: ArrayList<Command> = commandOperationsData.initial_commands
        val absolutizedCommands: ArrayList<Command> = commandOperationsData.absolutized_commands
        val normalizedCommands: ArrayList<Command> = commandOperationsData.normalized_commands

        val newCommands = CommandOperations.parse(data)
        val newAbsolutizedCommands = CommandOperations.absolutize(newCommands)
        val newNormalizedCommands = CommandOperations.normalize(newAbsolutizedCommands)

        assertThat(newCommands).isEqualTo(initialCommands)
        assertThat(newAbsolutizedCommands).isEqualTo(absolutizedCommands)
        assertThat(newNormalizedCommands).isEqualTo(normalizedCommands)
    }

    /**
     * Test the extension function for Integer types, for lower and upper case
     * to its corresponding char type.
     */
    fun UpperLowerCase() {

        // check for first character A
        val a = 97
        val A = 65
        assertThat(a.toUpperCase()).isEqualTo(A)
        assertThat(A.toUpperCase()).isEqualTo(A)
        assertThat(a.toLowerCase()).isEqualTo(a)
        assertThat(A.toLowerCase()).isEqualTo(a)

        // check for last character Z
        val z = 122
        val Z = 90
        assertThat(z.toUpperCase()).isEqualTo(Z)
        assertThat(Z.toUpperCase()).isEqualTo(Z)
        assertThat(z.toLowerCase()).isEqualTo(z)
        assertThat(Z.toLowerCase()).isEqualTo(z)
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