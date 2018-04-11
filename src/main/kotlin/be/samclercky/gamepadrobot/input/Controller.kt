package be.samclercky.gamepadrobot.input

import org.lwjgl.glfw.GLFW.*
import java.nio.FloatBuffer

import be.samclercky.gamepadrobot.utils.iterator
import java.nio.ByteBuffer

class Controller {
    companion object {
        private var working = false
    }

    val joysticks = arrayOf(
            GLFW_JOYSTICK_1,
            GLFW_JOYSTICK_2,
            GLFW_JOYSTICK_3,
            GLFW_JOYSTICK_4,
            GLFW_JOYSTICK_5,
            GLFW_JOYSTICK_6,
            GLFW_JOYSTICK_7,
            GLFW_JOYSTICK_8,
            GLFW_JOYSTICK_9,
            GLFW_JOYSTICK_10,
            GLFW_JOYSTICK_11,
            GLFW_JOYSTICK_12,
            GLFW_JOYSTICK_13,
            GLFW_JOYSTICK_14,
            GLFW_JOYSTICK_15,
            GLFW_JOYSTICK_16
    )

    var prevData = Array<Int>(pollData().size, {0}) // default data

    val firstControllerName: String
        get() {
            for (joystick in joysticks) {
                val exits = glfwJoystickPresent(joystick)
                val name = glfwGetJoystickName(joystick)

                if (exits && name != null) {
                    return name
                }
            }
            return "[no name]"
        }
    val allControllerNames: Array<String>
        get() {
            val result: ArrayList<String> = ArrayList()

            for (joystick in joysticks) {
                val name = glfwGetJoystickName(joystick)
                if (name != null) {
                    result.add(name)
                }
            }

            return result.toTypedArray()
        }
    val firstControllerId: Int
        get() {
            for (joystick in joysticks) {
                val name = glfwJoystickPresent(joystick)
                if (name != null) {
                    return joystick
                }
            }
            return -1 // nothing was found
        }

    init {
        if (!glfwInit()) {
            throw IllegalStateException("GLFW couldn't be initialized")
        }
        glfwPollEvents()

        working = true

        Runtime.getRuntime().addShutdownHook(Thread {
            if (working) {
                destroy()
                working = false
            }
        })
    }

    /**
     * Has to be called before program exits!!!
     */
    fun destroy() {
        glfwTerminate()
        println("destroing gwfl")
    }

    /**
     * Gives back an array of all new data
     */
    fun pollData(): Array<Int> {
        val results: ArrayList<Int> = arrayListOf()

        glfwPollEvents()
        val joys = glfwGetJoystickAxes(firstControllerId)
        val btns = glfwGetJoystickButtons(firstControllerId)

        if (joys != null && btns != null) {
            for (joy in joys) {
                results.add(joy)
            }
            for (btn in btns) {
                results.add(btn)
            }
        }
        return results.toTypedArray()
    }

    fun pollNewData(): Array<DiffEvents<Int>> {
        val newData = pollData()
        val diffData = getDiff<Int>(prevData, newData)
        prevData = newData

        return diffData
    }

    private fun <T> getDiff(oldData: Array<T>, newData: Array<T>): Array<DiffEvents<T>> {
        var result: ArrayList<DiffEvents<T>> = arrayListOf()

        if (oldData.size != newData.size && newData.size > 0) { // return data based on newData
            for (i in 0..(newData.size-1)) {
                result.add(DiffEvents(i, newData[i]))
            }
        } else {
            for (i in 0..(oldData.size-1)) {
                if (oldData[i] != newData[i]) {
                    result.add(DiffEvents(i, newData[i]))
                }
            }
        }

        return result.toTypedArray()
    }
}

data class DiffEvents<T>(val key: Int, val value: T)