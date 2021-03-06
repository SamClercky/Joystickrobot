package be.samclercky.gamepadrobot.input

import org.lwjgl.glfw.GLFW.*

import be.samclercky.gamepadrobot.utils.iterator

/**
 * Helper class with the lwjgl gamepad
 */
class Controller {
    companion object {
        private var working = false
    }

    private val joysticks = arrayOf(
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

    private var prevData = Array<Event<Int>>(pollData().size, {Event(it, 0)}) // default data

    /**
     * Gives the name of the first recognized controller
     */
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
    /**
     * Gives all the names of the recognized controllers
     */
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
    /**
     * Gives the id of the first recognized gamepad
     */
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
        if (!working) { // make sure this is only called once, so we don't get memory leaks
            if (!glfwInit()) { // sometimes glfw can't initialize, so we have to take care of it
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
    }

    /**
     * Has to be called before program exits!!!
     */
    fun destroy() {
        glfwTerminate()
        println("GWFL terminated")
    }

    /**
     * Gives back an array of all new data
     */
    fun pollData(): Array<Event<Int>> {
        val results: ArrayList<Event<Int>> = arrayListOf()

        glfwPollEvents()
        val joys = glfwGetJoystickAxes(firstControllerId)
        val btns = glfwGetJoystickButtons(firstControllerId)

        if (joys != null && btns != null) {
            var index = 0
            for (joy in joys) {
                results.add(Event(index, joy, true))
                index++
            }
            index = 0
            for (btn in btns) {
                results.add(Event(index, btn))
                index++
            }
        }
        return results.toTypedArray()
    }

    /**
     * Gives back an array of all data whitout the unchanged data from the previous call
     */
    fun pollNewData(): Array<Event<Int>> {
        val newData = pollData()
        val diffData = getDiff<Int>(prevData, newData)
        prevData = newData

        return diffData
    }

    /**
     * Searches all the differences between the 2 arrays
     * @param oldData The oldest data
     * @param newData The newest data
     * @return All the differences between oldData and newData. If the size of both arrays aren't equal, the data is equal to the newData
     */
    private fun <T> getDiff(oldData: Array<Event<T>>, newData: Array<Event<T>>): Array<Event<T>> {
        var result: ArrayList<Event<T>> = arrayListOf()

        if (oldData.size != newData.size && newData.size > 0) { // return data based on newData
            for (i in 0..(newData.size-1)) {
                result.add(Event(i, newData[i].value, newData[i].analog))
            }
        } else {
            for (i in 0..(oldData.size-1)) {
                if (oldData[i] != newData[i]) {
                    result.add(Event(i, newData[i].value, newData[i].analog))
                }
            }
        }

        return result.toTypedArray()
    }
}

/**
 * @param key Position in incoming array-data (starting with 0)
 * @param value The value of the data
 */
data class Event<T>(val key: Int, val value: T, val analog: Boolean = false)