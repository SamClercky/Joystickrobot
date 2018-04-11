package be.samclercky.gamepadrobot

import be.samclercky.gamepadrobot.input.Controller
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking

val VERSION = ""

fun main(args: Array<String>) = runBlocking<Unit> {
    forloop@ for (arg in args) {
        println(arg)
        when(arg) {
            "--test" -> {
                test()
                break@forloop
            }
            "--list-all" -> {
                listAll()
                break@forloop
            }
            "--help" -> {
                listOptions()
                break@forloop
            }
            else -> {
                App()
                break@forloop
            }
        }
    }
    // if nothing is there -> App()
    App()
}