package com.tello.controltello

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tello.controltello.ui.theme.ControlTelloTheme

class EnterActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ControlTelloTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Row {
                            Image(painter = painterResource(id = R.drawable.logotello),
                                contentDescription = "",
                                modifier = Modifier
                                    .width(300.dp)
                                    .height(150.dp))
                        }
                        Row {
                            FloatingActionButton(onClick = {
                                val intent = Intent(this@EnterActivity, MainActivity::class.java)
                                startActivity(intent)
                            },
                                modifier = Modifier
                                    .width(280.dp)
                                    .height(50.dp)
                                    .border(
                                        1.dp,
                                        color = Color(R.color.primary_blue),
                                        shape = CircleShape
                                    ),
                                shape = CircleShape
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "GO CONTROL YOU DRONE", fontSize = 14.sp)
                                }

                            }
                        }
                    }
                    
                }
            }
        }
    }
}