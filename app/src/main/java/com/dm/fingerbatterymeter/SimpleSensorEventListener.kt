package com.dm.fingerbatterymeter

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener

interface SimpleSensorEventListener : SensorEventListener {

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent) {
    }
}