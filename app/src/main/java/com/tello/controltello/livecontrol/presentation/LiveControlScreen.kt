package com.tello.controltello.livecontrol.presentation

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import com.tello.controltello.R
import com.tello.controltello.livecontrol.presentation.components.FlipJoyStick
import com.tello.controltello.livecontrol.presentation.components.LeftJoyStick
import com.tello.controltello.livecontrol.presentation.components.RightJoyStick

class LiveControlScreen {

    val rightJotStick = RightJoyStick()
    val leftJoyStick = LeftJoyStick()
    val flipJoyStick = FlipJoyStick()
    val radiusStrength = MutableLiveData<Long>()
    val angleValue = MutableLiveData<Long>()

    @SuppressLint("NotConstructor")
    @Composable
    fun LiveControlScreen() {
        Column(verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()) {
            Row(modifier = Modifier
                .fillMaxWidth()) {
                Box(modifier = Modifier
                    .fillMaxWidth()) {

                    Row(modifier = Modifier
                        .align(Alignment.TopStart)) {
                        Image(imageVector = ImageVector.vectorResource(id = R.drawable.disconnected), contentDescription = "")
                        Image(imageVector = ImageVector.vectorResource(id = R.drawable.streamoff), contentDescription = "")
                    }
                    Row(modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(start = 15.dp)) {
                        FloatingActionButton(onClick = { /*TODO*/ },
                            modifier = Modifier
                                .width(60.dp)
                                .height(60.dp)
                                .border(
                                    1.dp,
                                    color = Color(R.color.primary_green),
                                    shape = CircleShape
                                ),
                            shape = CircleShape) {
                            Image(imageVector = ImageVector.vectorResource(R.drawable.onicon),
                                contentDescription = "",
                                modifier = Modifier.padding(3.dp))
                        }
                    }
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .padding(start = 150.dp)) {
                        flipJoyStick.Joystick(joyStickWidth = 120, joyStickHeight = 100) { r, angle ->
                            
                        }
                    }
                    Box(modifier = Modifier
                        .align(Alignment.TopCenter)) {
                        Image(imageVector = ImageVector.vectorResource(R.drawable.dash), contentDescription = "")
                        Text(text ="100" , modifier = Modifier.align(Alignment.TopStart).padding(start = 14.dp, top = 14.dp), fontSize = 12.sp)
                        Text(text ="50 cm/s" , modifier = Modifier.align(Alignment.TopEnd).padding(end = 20.dp, top = 9.dp), fontSize = 9.sp)
                        Text(text ="70 C" , modifier = Modifier.align(Alignment.CenterEnd).padding(end = 20.dp), fontSize = 9.sp)
                        Text(text ="120 cm" , modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 9.dp), fontSize = 9.sp)
                    }
                    Box(modifier = Modifier
                        .width(180.dp)
                        .height(150.dp)
                        .align(Alignment.CenterEnd)) {
                        Column(modifier = Modifier
                            .align(Alignment.CenterStart)) {
                            Image(imageVector = ImageVector.vectorResource(R.drawable.streamicon),
                                contentDescription = "",
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(60.dp))
                        }
                        Column( horizontalAlignment = Alignment.End,
                            modifier = Modifier
                                .padding(end = 15.dp)
                                .align(Alignment.CenterEnd)) {
                            Row {
                                FloatingActionButton(onClick = { /*TODO*/ },
                                    modifier = Modifier
                                        .width(70.dp)
                                        .height(70.dp)
                                        .padding(5.dp)
                                        .border(
                                            1.dp,
                                            color = Color(R.color.primary_blue),
                                            shape = CircleShape
                                        ),
                                    shape = CircleShape) {
                                    Image(imageVector = ImageVector.vectorResource(R.drawable.updrone),
                                        contentDescription = "",
                                        modifier = Modifier
                                            .padding(3.dp))
                                }
                            }
                            Row {
                                FloatingActionButton(onClick = { /*TODO*/ },
                                    modifier = Modifier
                                        .width(70.dp)
                                        .height(70.dp)
                                        .padding(5.dp)
                                        .border(
                                            1.dp,
                                            color = Color(R.color.primary_blue),
                                            shape = CircleShape
                                        ),
                                    shape = CircleShape) {
                                    Image(imageVector = ImageVector.vectorResource(R.drawable.downdrone),
                                        contentDescription = "",
                                        modifier = Modifier.padding(3.dp))
                                }
                            }
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .padding(15.dp)
                    .fillMaxWidth()) {
                leftJoyStick.Joystick(joyStickSize = 180) { r, angle ->

                }
                rightJotStick.Joystick(joyStickSize = 180) { r, angle ->

                }
            }
        }
    }
    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        LiveControlScreen()
    }
}