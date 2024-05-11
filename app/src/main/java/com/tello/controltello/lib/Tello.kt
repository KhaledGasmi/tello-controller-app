package com.tello.controltello.lib

import android.graphics.Bitmap
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import androidx.lifecycle.MutableLiveData
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.*
import java.nio.ByteBuffer
import java.util.concurrent.BlockingQueue

class Tello {
    companion object {
        val distanceRange = 20..500
        val rotationRange = 1..3600
        val speedRange = 1..100
        val rcRange = -100..100
        var invalidArgString = "Command argument not in range!"
        lateinit var m_codec: MediaCodec
    }

    private lateinit var socket: DatagramSocket
    private lateinit var stateSocket: DatagramSocket
    private lateinit var videoSocket: DatagramSocket
    private var videoBytes = MutableLiveData<ByteArray>()

    private var isImperial: Boolean = false

    val isConnected: Boolean
        get() = socket.isConnected

    @Throws(IOException::class)
    fun read(info: Info) = sendCommand(info.type)

    @Throws(IOException::class)
    fun connect(ip: String = "192.168.10.1", port: Int = 8889) {
        socket = DatagramSocket(port)
        socket.connect(InetAddress.getByName(ip), port)
        sendCommand("command")
    }

    @Throws(IOException::class)
    fun stateConnect(port: Int = 8890): String {
        stateSocket = DatagramSocket(null)
        stateSocket.reuseAddress = true
        stateSocket.broadcast = true
        stateSocket.bind(InetSocketAddress(port))
        val message = ByteArray(1518)
        val statePacket = DatagramPacket(message, message.size)
        stateSocket.receive(statePacket)
        return String(statePacket.data, 0, statePacket.length)
    }

    @Throws(IOException::class)
    fun receiveStream(port: Int = 11111, queue: BlockingQueue<Bitmap>) {

        val startMs: Long
        val headerSps = byteArrayOf(0, 0, 0, 1, 103, 77, 64, 40, -107, -96, 60, 5, -71)
        val headerPps = byteArrayOf(0, 0, 0, 1, 104, -18, 56, -128)
        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 960, 720)
        format.setByteBuffer("csd-0", ByteBuffer.wrap(headerSps))
        format.setByteBuffer("csd-1", ByteBuffer.wrap(headerPps))
        format.setInteger(MediaFormat.KEY_WIDTH, 960)
        format.setInteger(MediaFormat.KEY_HEIGHT, 720)
        format.setInteger(MediaFormat.KEY_CAPTURE_RATE, 30)
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 1080 * 940)

        try {
            m_codec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            m_codec.configure(format, null, null, 0)
            startMs = System.currentTimeMillis()
            m_codec.start()
        } catch (e: IOException) {
            e.printStackTrace()
            // Handle exception
            return
        }
        val output = ByteArrayOutputStream()

        videoSocket = DatagramSocket(null)
        videoSocket.reuseAddress = true
        videoSocket.broadcast = true
        videoSocket.bind(InetSocketAddress(port))
        val message = ByteArray(2048)
        var destPos = 0
        val dataNew = ByteArray(60000)
        while (true) {
            val videoPacket = DatagramPacket(message, message.size)
            videoSocket.receive(videoPacket)
            videoBytes.postValue(videoPacket.data)

            System.arraycopy(videoPacket.data, videoPacket.offset, dataNew, destPos, videoPacket.length)
            destPos += videoPacket.length
            val pacMan = ByteArray(videoPacket.length)
            System.arraycopy(videoPacket.data, videoPacket.offset, pacMan, 0, videoPacket.length)
            val len = videoPacket.length
            output.write(pacMan)
            if (len < 1460) {
                destPos = 0
                val data = output.toByteArray()
                output.reset()
                output.flush()
                output.close()
                val inputIndex = m_codec.dequeueInputBuffer(-1)
                if (inputIndex >= 0) {
                    val buffer = m_codec.getInputBuffer(inputIndex)
                    if (buffer != null) {
                        buffer.clear()
                        buffer.put(data)
                        val presentationTimeUs = System.currentTimeMillis() - startMs
                        m_codec.queueInputBuffer(inputIndex, 0, data.size, presentationTimeUs, 0)
                    }
                }

                val info = MediaCodec.BufferInfo()
                val outputIndex = m_codec.dequeueOutputBuffer(info, 100)
                try {
                    if (outputIndex >= 0) {
                        val image = m_codec.getOutputImage(outputIndex)
                        if (image != null) {
                            val bm = imgToBM(image)
                            try {
                                if (!queue.isEmpty()) {
                                    queue.clear()
                                }
                                queue.put(bm)
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }
                        }
                        m_codec.releaseOutputBuffer(outputIndex, false)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun disconnect() = socket.close()

    @Throws(IOException::class)
    fun takeOff() = sendCommand("takeoff")

    @Throws(IOException::class)
    fun land() = sendCommand("land")

    @Throws(IOException::class)
    fun emergency() = sendCommand("emergency")

    @Throws(IOException::class)
    fun streamOn() = sendCommand("streamon")

    @Throws(IOException::class)
    fun streamOff() = sendCommand("streamoff")

    @Throws(IOException::class)
    fun moveLeft(x: Int) = move("left", x)

    @Throws(IOException::class)
    fun moveRight(x: Int) = move("right", x)

    @Throws(IOException::class)
    fun moveForward(y: Int) = move("forward", y)

    @Throws(IOException::class)
    fun moveBack(y: Int) = move("back", y)

    @Throws(IOException::class)
    fun moveUp(z: Int) = move("up", z)

    @Throws(IOException::class)
    fun moveDown(z: Int) = move("down", z)

    @Throws(IOException::class)
    fun rotateClockwise(degrees: Int) = rotate("cw", degrees)

    @Throws(IOException::class)
    fun rotateCounterClockwise(degrees: Int) = rotate("ccw", degrees)

    @Throws(IOException::class)
    fun flip(direction: FlipDirection) = sendCommand("flip ${direction.direction}")

    fun go(x: Int, y: Int, z: Int, speed: Int) =
        if (arrayListOf(x, y, z).isValidDistance())
            sendCommand("go $x $y $z $speed")
        else
            invalidArgString

    fun curve(x1: Int, x2: Int, y1: Int, y2: Int, z1: Int, z2: Int, speed: Int) =
        when {
            !arrayListOf(x1, x2, y1, y2, z1, z2).isValidDistance() -> invalidArgString
            speed !in 10..60 -> invalidArgString
            else -> sendCommand("curve $x1 $y1 $z1 $x2 $y2 $z2 $speed")
        }

    @Throws(IOException::class)
    fun setSpeed(speed: Int) =
        if (speed.isValidSpeed())
            sendCommand("speed $speed")
        else
            invalidArgString

    @Throws(IOException::class)
    fun setWifiSsidPass(ssid: String, pass: String) = sendCommand("wifi $ssid $pass")

    @Throws(IOException::class)
    fun sendRc(leftRight: Int, forwardBack: Int, upDown: Int, yaw: Int) =
        when {
            !arrayListOf(leftRight, forwardBack, upDown).isValidRc() -> invalidArgString
            else -> sendCommand("rc $leftRight $forwardBack $upDown $yaw")
        }

    @Throws(IOException::class)
    private fun move(command: String, distance: Int) =
        if (distance.isValidDistance())
            sendCommand("$command $distance")
        else
            invalidArgString

    @Throws(IOException::class)
    private fun rotate(command: String, degrees: Int) =
        if (degrees.isValidRotation())
            sendCommand("$command $degrees")
        else
            invalidArgString

    @Throws(IOException::class)
    fun sendCommand(command: String): String {
        try {
            if (command.isEmpty()) return "Empty command."
            if (!socket.isConnected) return "Socket Disconnected."

            val sendData = command.toByteArray()
            val sendPacket = DatagramPacket(sendData, sendData.size, socket.inetAddress, socket.port)
            socket.send(sendPacket)

            val receiveData = ByteArray(1024)
            val receivePacket = DatagramPacket(receiveData, receiveData.size)
            socket.soTimeout = 40
            socket.receive(receivePacket)

            val response = String(receivePacket.data)
            Log.d("in tello lib", "$command: $response")
            return response
        } catch (e: Exception) {
            Log.d("exception in sendCommand", "send command exception $e")
            return "$e"
        }
    }

    private fun Int.toMetric() = if (!isImperial) this else Math.round((this * 2.54).toFloat())
    private fun Int.isValidDistance() = this.toMetric() in distanceRange
    private fun Int.isValidRotation() = this in rotationRange
    private fun Int.isValidSpeed() = this in speedRange
    private fun Int.isValidRc() = this in rcRange
    private fun ArrayList<Int>.isValidDistance() = this.all { it.isValidDistance() }
    private fun ArrayList<Int>.isValidRc() = this.all { it.isValidRc() }
}

enum class Info(val type: String) {
    SPEED("speed?"),
    BATTERY("battery?"),
    TIME("time?"),
    HEIGHT("height?"),
    TEMP("temp?"),
    ATTITUDE("attitude?"),
    BARO("baro?"),
    ACCELERATION("acceleration?"),
    TOF("tof?"),
    WIFI("wifi?")
}

enum class FlipDirection(val direction: String) {
    LEFT("l"),
    RIGHT("r"),
    FORWARD("f"),
    BACKWARD("b"),
    BACK_LEFT("bl"),
    BACK_RIGHT("rb"),
    FRONT_LEFT("fl"),
    FRONT_RIGHT("fr")
}