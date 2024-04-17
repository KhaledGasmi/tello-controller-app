package com.tello.controltello.livecontrol.presentation

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress

class LiveControlViewModel: ViewModel() {

    private var sendCommandSocket: DatagramSocket? = null
    private var receiveStatSocket: DatagramSocket? = null
    private var receiveVideoSocket: DatagramSocket? = null
    private val telloAddress = InetAddress.getByName("192.186.10.1")
    private val telloSendingPort = 8889
    private val telloReceiveStatPort = 8890
    private val telloReceiveVideoPort = 1111
    val TAG = "view model errors"

    val telloStat = MutableLiveData<Map<String, Any>>()

    init {
        startCommunication()
    }

    private fun startCommunication() {
        sendCommandSocket = DatagramSocket(null)
        val command = "command".toByteArray(Charsets.UTF_8)
        val packetToSend = DatagramPacket(command, command.size, telloAddress, telloSendingPort)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (sendCommandSocket!!.isConnected) {
                    sendCommandSocket?.send(packetToSend)
                }
            } catch (e: Exception) {
                Log.d(TAG, "error: $e")
            }
        }
    }

    private fun receiveStat() {
        receiveStatSocket = DatagramSocket(null)
        receiveStatSocket?.reuseAddress = true
        receiveStatSocket?.broadcast = true
        val messageToReceive = ByteArray(3000)
        val packetToReceive = DatagramPacket(messageToReceive, messageToReceive.size)
        viewModelScope.launch {
            try {
                receiveStatSocket?.bind(InetSocketAddress(telloReceiveStatPort))
                receiveStatSocket?.receive(packetToReceive)
                val readyText = String(messageToReceive, 0, packetToReceive.length)
                telloStat.value = TransferFunctions.stringToMap(readyText)
            } catch (e: Exception) {
                Log.d(TAG, "error: $e")
            }
        }
    }

    private fun endCommunication() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (sendCommandSocket!!.isConnected) {
                    sendCommandSocket?.close()
                }
                if (receiveStatSocket!!.isConnected) {
                    receiveStatSocket?.close()
                }
                if (receiveVideoSocket!!.isConnected) {
                    receiveVideoSocket?.close()
                }
            } catch (e: Exception) {
                Log.d(TAG, "error: $e")
            }
        }
    }

    private fun sendCommand(command: String) {
        val commandToSend = command.toByteArray(Charsets.UTF_8)
        val packetToSend = DatagramPacket(commandToSend, commandToSend.size, telloAddress, telloSendingPort)
        viewModelScope.launch {
            try {
                if (sendCommandSocket!!.isConnected) {
                    sendCommandSocket?.send(packetToSend)
                }
            } catch (e: Exception) {
                Log.d(TAG, "error: $e")
            }
        }
    }

    private fun receiveStream() {
        receiveVideoSocket = DatagramSocket(null)
        receiveVideoSocket?.reuseAddress = true
        receiveVideoSocket?.broadcast = true
        val messageToReceive = ByteArray(3000)
        val packetToReceive = DatagramPacket(messageToReceive, messageToReceive.size)
        viewModelScope.launch {
            try {
                receiveVideoSocket?.bind(InetSocketAddress(telloReceiveVideoPort))
                receiveVideoSocket?.receive(packetToReceive)
            } catch (e: Exception) {
                Log.d(TAG, "error: $e")
            }
        }
    }
}