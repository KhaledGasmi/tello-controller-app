package com.tello.controltello.livecontrol.util

import com.tello.controltello.lib.FlipDirection

fun direction(angle: Float): FlipDirection {
    if (angle<=22.5 && angle>-22.5) return FlipDirection.RIGHT
    if (angle<=67.5 && angle>22.5) return FlipDirection.BACK_RIGHT
    if (angle<=112.5 && angle>67.5) return FlipDirection.BACKWARD
    if (angle<=157.5 && angle>112.5) return FlipDirection.BACK_LEFT
    if (angle<=-22.5 && angle>-67.5) return FlipDirection.FRONT_RIGHT
    if (angle<=-67.5 && angle>-112.5) return FlipDirection.FORWARD
    if (angle<=-112.5 && angle>=-157.5) return FlipDirection.FRONT_LEFT
    return FlipDirection.LEFT
}
