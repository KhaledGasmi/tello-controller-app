package com.tello.controltello.livecontrol.presentation

data class TelloState(val battery: Int,
                      val connection: Boolean,
                      val steam: Boolean,
                      val speed: Int,
                      val snr: Long,
                      val time: Long,
                      val temperature: Long,
                      val height: Int)
