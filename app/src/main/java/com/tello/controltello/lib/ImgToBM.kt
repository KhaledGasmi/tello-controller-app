package com.tello.controltello.lib

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import java.io.ByteArrayOutputStream

fun imgToBM(image: Image): Bitmap {
    val p = image.planes
    val y = p[0].buffer
    val u = p[1].buffer
    val v = p[2].buffer
    val ySz = y.remaining()
    val uSz = u.remaining()
    val vSz = v.remaining()
    val jm8 = ByteArray(ySz + uSz + vSz)
    y.get(jm8, 0, ySz)
    v.get(jm8, ySz, vSz)
    u.get(jm8, ySz + vSz, uSz)
    val yuvImage = YuvImage(jm8, ImageFormat.NV21, image.width, image.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 75, out)
    val imgBytes = out.toByteArray()
    return BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.size)
}

