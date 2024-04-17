package com.tello.controltello.livecontrol.presentation

class TransferFunctions {

    companion object {

        fun stringToMap(text: String): Map<String, Any> {
            val regex = Regex("([a-zA-Z]+):([-\\d.]+)")
            val matches = regex.findAll(text)

            val map = mutableMapOf<String, Any>()
            matches.forEach { matchResult ->
                val (key, value) = matchResult.destructured
                map[key] = if (value.contains(".")) value.toDouble() else value.toInt()
            }
            return map
        }

    }
}