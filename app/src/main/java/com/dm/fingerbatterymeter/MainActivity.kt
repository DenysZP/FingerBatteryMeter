package com.dm.fingerbatterymeter

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlin.math.absoluteValue


class MainActivity : AppCompatActivity(), SimpleSensorEventListener {

    companion object {
        private const val EVENT_DELAY = 200
        private const val DATA_SET_SIZE = 25
    }

    private var sensorManager: SensorManager? = null
    private var xMagneticValue = 0.0
    private var yMagneticValue = 0.0
    private var zMagneticValue = 0.0
    private var xCalibrator = 0.0
    private var yCalibrator = 0.0
    private var zCalibrator = 0.0
    private var calibratedMagX = 0.0
    private var calibratedMagY = 0.0
    private var calibratedMagZ = 0.0
    private var isDataCollecting = false
    private var sensorDataList = mutableListOf<Triple<Double, Double, Double>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        calibrateButton.setOnClickListener {
            xCalibrator = xMagneticValue
            yCalibrator = yMagneticValue
            zCalibrator = zMagneticValue
        }
        dataCollectionButton.setOnClickListener {
            if (!isDataCollecting) {
                sensorDataList.clear()
            }
            isDataCollecting = !isDataCollecting
            changeUiState()
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager?.let {
            it.registerListener(
                this,
                it.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL,
                EVENT_DELAY
            )
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        xMagneticValue = event.values[0].toDouble()
        yMagneticValue = event.values[1].toDouble()
        zMagneticValue = event.values[2].toDouble()

        calibratedMagX = (xMagneticValue - xCalibrator).absoluteValue
        calibratedMagY = (yMagneticValue - yCalibrator).absoluteValue
        calibratedMagZ = (zMagneticValue - zCalibrator).absoluteValue

        sensorData.text =
            getString(R.string.data_pattern, calibratedMagX, calibratedMagY, calibratedMagZ)

        if (isDataCollecting) {
            collectData(calibratedMagX, calibratedMagY, calibratedMagZ)
        }
    }

    private fun changeUiState() {
        calibrateButton.isEnabled = !isDataCollecting
        dataCollectionButton.setText(
            if (isDataCollecting) R.string.stop_data_collection
            else R.string.start_data_collection
        )
    }

    private fun collectData(x: Double, y: Double, z: Double) {
        val currentDataSize = sensorDataList.size
        if (currentDataSize < DATA_SET_SIZE) {
            sensorDataList.add(Triple(x, y, z))
        } else {
            Toast.makeText(this, R.string.data_collection_is_complete, Toast.LENGTH_LONG).show()
            isDataCollecting = false
            changeUiState()
            toJson()
        }
        progressBar.progress = ((currentDataSize.toFloat() / DATA_SET_SIZE) * 100).toInt()
    }

    private fun toJson() {
        try {
            val jsonSensorData = JSONArray()
            sensorDataList.forEach {
                val jsonSensorObject = JSONObject()
                val (x, y, z) = it
                jsonSensorObject.put("x", x)
                jsonSensorObject.put("y", y)
                jsonSensorObject.put("z", z)
                jsonSensorData.put(jsonSensorObject)
            }
            Log.d("TEST", jsonSensorData.toString(4))
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}
