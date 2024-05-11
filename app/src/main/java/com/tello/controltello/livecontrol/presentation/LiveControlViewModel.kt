package com.tello.controltello.livecontrol.presentation

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tello.controltello.lib.FlipDirection
import com.tello.controltello.lib.Tello
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.BlockingQueue

class LiveControlViewModel: ViewModel() {
    val tello = Tello()

    val isConnected = MutableLiveData(false)
    val telloStates = MutableLiveData<Map<String, String>>()
    val isStreaming = MutableLiveData(false)

    fun connect() {
        viewModelScope.launch(Dispatchers.IO) {
            tello.connect()
            delay(1500)
            stateConnect()
            if (tello.isConnected) {
                isConnected.postValue(true)
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch(Dispatchers.IO) {
            if (tello.isConnected) {
                tello.disconnect()
                isConnected.postValue(false)
            }
        }
    }

    fun isConnected(): Boolean {
        return tello.isConnected
    }

    fun takeOff() {
        viewModelScope.launch(Dispatchers.IO) {
            tello.takeOff()
        }
    }

    fun land() {
        viewModelScope.launch(Dispatchers.IO) {
            tello.land()
        }
    }

    fun flip(direction: FlipDirection) {
        viewModelScope.launch(Dispatchers.IO) {
            tello.flip(direction)
        }
    }

    fun sendRc(a1: Int, a2: Int, a3: Int, a4: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            tello.sendRc(a1, a2, a3, a4)
        }
    }

    private fun stateConnect() {
        viewModelScope.launch(Dispatchers.IO) {
            while(true) {
                val text = tello.stateConnect()
                val keyValuePairs = text.split(";")
                val sensorData: MutableMap<String, String> = mutableMapOf()
                for (pair in keyValuePairs.indices) {
                    if (pair == keyValuePairs.lastIndex) break
                    val (key, value) = keyValuePairs[pair].split(":")
                    sensorData[key] = value
                }
                telloStates.postValue(sensorData)
                delay(200)
            }
        }
    }

    fun getVideoStream(queue: BlockingQueue<Bitmap>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                tello.streamOn()
                isStreaming.postValue(true)
                tello.receiveStream(queue = queue)
            } catch (e: Exception) {
                Log.d("TAG", "$e")
            }
        }
    }

    fun stopStream() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                tello.streamOff()
                isStreaming.postValue(false)
            } catch (e: Exception) {
                Log.d("TAG", "$e")
            }
        }
    }

}