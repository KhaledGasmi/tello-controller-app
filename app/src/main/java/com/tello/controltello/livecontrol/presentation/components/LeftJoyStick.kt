package com.tello.controltello.livecontrol.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tello.controltello.R
import kotlin.math.pow

class LeftJoyStick {

    @Composable
    fun Joystick(
        joyStickSize: Int,
        onPositionChanged: (Long, Long) -> Unit
    ) {
        var knobPosition by remember { mutableStateOf(Offset.Zero) }
        var isDragging by remember { mutableStateOf(false)  }

        val knobImage = ImageBitmap.imageResource(R.drawable.handicon)

        Box(modifier = Modifier
            .width(joyStickSize.dp)
            .height(joyStickSize.dp)) {
            Image(painter = painterResource(id = R.drawable.leftjoystickbackground),
                contentDescription = "",
                modifier = Modifier
                    .width(joyStickSize.dp)
                    .height(joyStickSize.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                isDragging = true
                            },
                            onDragEnd = {
                                isDragging = false
                                // Reset knob position when drag ends
                                knobPosition = Offset.Zero
                                onPositionChanged(0, 0)
                            }
                        ) { change, dragAmount ->
                            val newPosition = knobPosition + dragAmount
                            knobPosition = limitPosition(newPosition)
                            onPositionChanged(
                                knobPosition.x.toLong(),
                                knobPosition.y.toLong()
                            )
                            if (change.positionChange() != Offset.Zero) change.consume()
                        }
                    }
            ) {
                drawJoystickKnob(knobPosition, knobImage, joyStickSize)
            }
        }
    }

    private fun DrawScope.drawJoystickKnob(position: Offset, knob: ImageBitmap, joyStickSize: Int) {
        drawImage(knob, Offset(joyStickSize/2f, joyStickSize/2f) +position)
    }

    private fun limitPosition(position: Offset): Offset {
        val radius = 180f
        val centerX = 0f
        val centerY = 0f
        val distance = position.getDistance(Offset(centerX, centerY))
        return if (distance <= radius) {
            position
        } else {
            val angle = position.getAngle(Offset(centerX, centerY))
            val limitedX = centerX + radius * kotlin.math.cos(angle)
            val limitedY = centerY + radius * kotlin.math.sin(angle)
            Offset(limitedX, limitedY)
        }
    }

    private fun Offset.getDistance(other: Offset): Float {
        return kotlin.math.sqrt((x - other.x).pow(2) + (y - other.y).pow(2))
    }

    private fun Offset.getAngle(other: Offset): Float {
        return kotlin.math.atan2((y - other.y).toDouble(), (x - other.x).toDouble()).toFloat()
    }
}
