package com.tello.controltello

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.tello.controltello.lib.FlipDirection
import com.tello.controltello.livecontrol.presentation.LiveControlViewModel
import com.tello.controltello.livecontrol.presentation.components.FlipJoyStick
import com.tello.controltello.livecontrol.presentation.components.LeftJoyStick
import com.tello.controltello.livecontrol.presentation.components.RightJoyStick
import com.tello.controltello.livecontrol.util.direction
import com.tello.controltello.ui.theme.ControlTelloTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {

    private val rightJotStick = RightJoyStick()
    private val leftJoyStick = LeftJoyStick()
    private val flipJoyStick = FlipJoyStick()
    private var rcs = intArrayOf(0, 0, 0, 0)
    private var flipDirection: FlipDirection? = null
    private val controlViewModel: LiveControlViewModel by viewModels()
    private val isDropdownMenuExpanded = mutableStateOf(false)
    private val menuItems = listOf(R.drawable.streamon, R.drawable.streamoff)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ControlTelloTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    val videoQueue = remember { ArrayBlockingQueue<Bitmap>(1) }
                    Box {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            VideoScreen(controlViewModel, videoQueue = videoQueue)
                        }

                        Column(
                            verticalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            val isConnected =
                                observeLiveData(controlViewModel.isConnected).value ?: false
                            val isStreaming =
                                observeLiveData(controlViewModel.isStreaming).value ?: false
                            var selectedItem: Int? = remember { null }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {

                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                    ) {
                                        Image(
                                            imageVector = if (!isConnected) ImageVector.vectorResource(
                                                id = R.drawable.disconnected
                                            ) else ImageVector.vectorResource(id = R.drawable.connected),
                                            contentDescription = ""
                                        )
                                        Image(
                                            imageVector = if (isStreaming && isConnected) ImageVector.vectorResource(
                                                id = R.drawable.streamon
                                            ) else ImageVector.vectorResource(id = R.drawable.streamoff),
                                            contentDescription = ""
                                        )
                                    }
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .fillMaxWidth()
                                            .padding(start = 15.dp)
                                    ) {
                                        FloatingActionButton(
                                            onClick = {
                                                if (!isConnected) {
                                                    controlViewModel.connect()
                                                } else {
                                                    controlViewModel.disconnect()
                                                }
                                            },
                                            modifier = Modifier
                                                .width(60.dp)
                                                .height(60.dp)
                                                .border(
                                                    1.dp,
                                                    color = if (isConnected) Color(R.color.primary_red) else Color(
                                                        R.color.primary_green
                                                    ),
                                                    shape = CircleShape
                                                ),
                                            shape = CircleShape
                                        ) {
                                            if (isConnected) {
                                                Image(
                                                    imageVector = ImageVector.vectorResource(R.drawable.officon),
                                                    contentDescription = "",
                                                    modifier = Modifier.padding(3.dp)
                                                )
                                            } else {
                                                Image(
                                                    imageVector = ImageVector.vectorResource(R.drawable.onicon),
                                                    contentDescription = "",
                                                    modifier = Modifier.padding(3.dp)
                                                )
                                            }
                                        }
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.BottomStart)
                                            .padding(start = 150.dp)
                                    ) {
                                        flipJoyStick.Joystick(
                                            joyStickWidth = 120,
                                            joyStickHeight = 100
                                        ) { x, y ->
                                            val r = sqrt(x.toFloat().pow(2) + y.toFloat().pow(2))
                                            val radAngle = atan2(y.toFloat(), x.toFloat())
                                            val angle = radAngle * 180 / PI

                                            flipDirection = direction(angle.toFloat())
                                            controlViewModel.flip(flipDirection!!)
                                            flipDirection = null
                                            Log.d("leftjoystick", "r= $r, angle= $angle")
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopCenter)
                                    ) {
                                        Image(
                                            imageVector = ImageVector.vectorResource(R.drawable.dash),
                                            contentDescription = ""
                                        )
                                        val stateTello =
                                            observeLiveData(controlViewModel.telloStates)
                                        Text(
                                            text = if (isConnected) (stateTello.value?.get("bat") + "%") else "-- %",
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .padding(start = 14.dp, top = 14.dp),
                                            fontSize = 12.sp
                                        )

                                        Text(
                                            text = if (isConnected) (stateTello.value?.get("vgx") + " cm/s") else "-- cm/s",
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(end = 20.dp, top = 9.dp), fontSize = 9.sp
                                        )
                                        Text(
                                            text = if (isConnected) (stateTello.value?.get("temph") + " °C") else "-- °C",
                                            modifier = Modifier
                                                .align(Alignment.CenterEnd)
                                                .padding(end = 20.dp), fontSize = 9.sp
                                        )
                                        Text(
                                            text = if (isConnected) (stateTello.value?.get("h") + " cm") else "-- cm",
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(end = 20.dp, bottom = 9.dp),
                                            fontSize = 9.sp
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .width(210.dp)
                                            .height(150.dp)
                                            .align(Alignment.CenterEnd)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .align(Alignment.CenterStart)
                                        ) {
                                            Image(
                                                imageVector = ImageVector.vectorResource(R.drawable.streamicon),
                                                contentDescription = "",
                                                modifier = Modifier
                                                    .clickable {
                                                        isDropdownMenuExpanded.value =
                                                            !isDropdownMenuExpanded.value
                                                    }
                                                    .width(60.dp)
                                                    .height(60.dp)
                                            )
                                            DropdownMenu(
                                                isOpen = isDropdownMenuExpanded.value,
                                                items = menuItems,
                                                queue = videoQueue
                                            ) { selectedItem = it }

                                            if (selectedItem != null) {
                                                Text("$selectedItem")
                                            }
                                        }
                                        Column(
                                            horizontalAlignment = Alignment.End,
                                            modifier = Modifier
                                                .padding(end = 15.dp)
                                                .align(Alignment.CenterEnd)
                                        ) {
                                            FloatingActionButton(
                                                onClick = {
                                                    try {
                                                        controlViewModel.takeOff()
                                                    } catch (e: Exception) {
                                                        Log.d(
                                                            "connecting to view model",
                                                            "error found connecting to view model function : $e"
                                                        )
                                                    }
                                                },
                                                modifier = Modifier
                                                    .width(70.dp)
                                                    .height(70.dp)
                                                    .padding(5.dp)
                                                    .border(
                                                        1.dp,
                                                        color = Color(R.color.primary_blue),
                                                        shape = CircleShape
                                                    ),
                                                shape = CircleShape
                                            ) {
                                                Image(
                                                    imageVector = ImageVector.vectorResource(R.drawable.updrone),
                                                    contentDescription = "",
                                                    modifier = Modifier
                                                        .padding(3.dp)
                                                )
                                            }
                                            FloatingActionButton(
                                                onClick = {
                                                    try {
                                                        controlViewModel.land()
                                                    } catch (e: Exception) {
                                                        Log.d(
                                                            "connecting to view model",
                                                            "error found connecting to view model function : $e"
                                                        )
                                                    }
                                                },
                                                modifier = Modifier
                                                    .width(70.dp)
                                                    .height(70.dp)
                                                    .padding(5.dp)
                                                    .border(
                                                        1.dp,
                                                        color = Color(R.color.primary_blue),
                                                        shape = CircleShape
                                                    ),
                                                shape = CircleShape
                                            ) {
                                                Image(
                                                    imageVector = ImageVector.vectorResource(R.drawable.downdrone),
                                                    contentDescription = "",
                                                    modifier = Modifier.padding(3.dp)
                                                )
                                            }

                                        }
                                    }
                                }
                            }
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom,
                                modifier = Modifier
                                    .padding(15.dp)
                                    .fillMaxWidth()
                            ) {
                                leftJoyStick.Joystick(joyStickSize = 180) { x, y ->
                                    val r = sqrt(x.toFloat().pow(2) + y.toFloat().pow(2))
                                    val radAngle = atan2(y.toFloat(), x.toFloat())
                                    val angle = radAngle * 180 / PI

                                    if (angle <= 45 && angle > -45) rcs[3] =
                                        (r * cos(radAngle) * 0.5).toInt()
                                    if (angle <= 135 && angle > 45) rcs[2] =
                                        (-r * sin(radAngle) * 0.5).toInt()
                                    if (angle <= -45 && angle > -135) rcs[2] =
                                        (-r * sin(radAngle) * 0.5).toInt()
                                    if (angle <= -135 || angle > 135) rcs[3] =
                                        (r * cos(radAngle) * 0.5).toInt()

                                    Log.d("testUI", "${rcs[0]}  ${rcs[1]}  ${rcs[2]}  ${rcs[3]}")
                                    try {
                                        controlViewModel.sendRc(rcs[0], rcs[1], rcs[2], rcs[3])
                                    } catch (e: Exception) {
                                        Log.d(
                                            "connecting to view model",
                                            "error found connecting to view model function "
                                        )
                                    }
                                    rcs.fill(0)
                                    Log.d("leftjoystick", "r= $r, angle= $angle")

                                }
                                rightJotStick.Joystick(joyStickSize = 180) { x, y ->
                                    val r = sqrt(x.toFloat().pow(2) + y.toFloat().pow(2))
                                    val radAngle = atan2(y.toFloat(), x.toFloat())
                                    val angle = radAngle * 180 / PI

                                    if (angle <= 45 && angle > -45) rcs[0] =
                                        (r * cos(radAngle) * 0.5).toInt()
                                    if (angle <= 135 && angle > 45) rcs[1] =
                                        (-r * sin(radAngle) * 0.5).toInt()
                                    if (angle <= -45 && angle > -135) rcs[1] =
                                        (-r * sin(radAngle) * 0.5).toInt()
                                    if (angle <= -135 || angle > 135) rcs[0] =
                                        (r * cos(radAngle) * 0.5).toInt()

                                    Log.d("testUI", "${rcs[0]}  ${rcs[1]}  ${rcs[2]}  ${rcs[3]}")
                                    try {
                                        controlViewModel.sendRc(rcs[0], rcs[1], rcs[2], rcs[3])
                                    } catch (e: Exception) {
                                        Log.d(
                                            "connecting to view model",
                                            "error found connecting to view model function "
                                        )
                                    }
                                    rcs.fill(0)
                                    Log.d("leftjoystick", "r= $r, angle= $angle")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun DropdownMenu(isOpen: Boolean, items: List<Int>, queue: BlockingQueue<Bitmap>, onItemSelected: (Int) -> Unit) {
        if (isOpen) {
            Column(modifier = Modifier.fillMaxWidth()) {
                for (item in items) {
                    Image(imageVector = ImageVector.vectorResource(item),
                        contentDescription = "",
                        modifier = Modifier
                            .clickable {
                                when(item) {
                                    R.drawable.streamon -> controlViewModel.getVideoStream(queue)
                                    else -> controlViewModel.stopStream()
                                }
                                onItemSelected(item)
                                isDropdownMenuExpanded.value = false
                            })
                }
            }
        }
    }

    @Composable
    fun <T> observeLiveData(liveData: LiveData<T>): MutableState<T?> {
        val observedState = remember { mutableStateOf(liveData.value) }

        DisposableEffect(liveData) {
            val observer = Observer<T> { value ->
                observedState.value = value
            }
            liveData.observeForever(observer)
            onDispose {
                liveData.removeObserver(observer)
            }
        }
        return observedState
    }

    @Composable
    fun VideoScreen(viewModel: LiveControlViewModel, videoQueue: ArrayBlockingQueue<Bitmap>) {
        var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

        LaunchedEffect(viewModel) {
            launch(Dispatchers.IO) {
                while (true) {
                    val bitmap = videoQueue.take()
                    imageBitmap = bitmap.asImageBitmap()
                }
            }
        }

        if (viewModel.isStreaming.value == true) {
            imageBitmap?.let {
                Image(
                    bitmap = it,
                    contentDescription = "Video Stream",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            // If streaming is off, display a transparent box
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // You can customize the transparency by changing the alpha value
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color.Transparent)
                )
            }
        }
    }

}
