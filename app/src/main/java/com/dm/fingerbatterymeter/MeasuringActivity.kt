package com.dm.fingerbatterymeter

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_measuring.*
import kotlin.math.absoluteValue

class MeasuringActivity : AppCompatActivity(), SimpleSensorEventListener {

    companion object {
        private const val FILE_NAME = "template_data.json"
        private const val ANIMATION_DURATION = 300L
        private const val N_NEIGHBORS = 5
        private const val EVENT_DELAY = 500
        private const val ZERO = 0
        private const val LOW = 1
        private const val BELOW_AVERAGE = 2
        private const val AVERAGE = 3
        private const val ABOVE_AVERAGE = 4
        private const val HIGH = 5
        private const val VERY_HIGH = 6
    }

    private lateinit var classifier: KNeighborsClassifier

    private var sensorManager: SensorManager? = null
    private var xMagneticValue = 0.0
    private var yMagneticValue = 0.0
    private var xCalibrator = 0.0
    private var yCalibrator = 0.0
    private var calibratedMagX = 0.0
    private var calibratedMagY = 0.0
    private var isCalibrated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_measuring)

        classifier = KNeighborsClassifier(this, FILE_NAME, N_NEIGHBORS)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager

        showCalibrateInstruction()
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_measuring, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.calibrateItem) {
            xCalibrator = xMagneticValue
            yCalibrator = yMagneticValue
            isCalibrated = true
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        xMagneticValue = event.values[0].toDouble()
        yMagneticValue = event.values[1].toDouble()

        calibratedMagX = (xMagneticValue - xCalibrator).absoluteValue
        calibratedMagY = (yMagneticValue - yCalibrator).absoluteValue

        if (isCalibrated) {
            changeBatteryLevel(
                classifier.predict(
                    doubleArrayOf(
                        calibratedMagX,
                        calibratedMagY
                    )
                )
            )
        }
    }

    private fun changeBatteryLevel(batteryLevel: Int) {
        if (batteryLevel != ZERO) {
            hideAllInstructions()
        }

        val resource = when (batteryLevel) {
            ZERO -> {
                showPlaceInstruction()
                R.drawable.ic_battery_0
            }
            LOW -> R.drawable.ic_battery_1
            BELOW_AVERAGE -> R.drawable.ic_battery_2
            AVERAGE -> R.drawable.ic_battery_3
            ABOVE_AVERAGE -> R.drawable.ic_battery_4
            HIGH -> R.drawable.ic_battery_5
            VERY_HIGH -> R.drawable.ic_battery_6
            else -> null
        }

        resource?.let {
            batteryImageView.setImageResource(resource)
        }
    }

    private fun showCalibrateInstruction() {
        instructionTextView.visibility = VISIBLE
        instructionTextView.setText(R.string.calibration_required)
        calibrateArrow.visibility = VISIBLE
        calibrateArrow.showBlinkAnimation(ANIMATION_DURATION)
        batteryPlace.visibility = GONE
        batteryPlaceArrow.visibility = GONE
        batteryPlaceArrow.clearAnimation()
    }

    private fun showPlaceInstruction() {
        if (batteryPlaceArrow.visibility != VISIBLE || instructionTextView.visibility != VISIBLE) {
            instructionTextView.visibility = VISIBLE
            batteryPlace.visibility = VISIBLE
            calibrateArrow.visibility = GONE
            calibrateArrow.clearAnimation()
            instructionTextView.setText(R.string.put_battery_here)
            batteryPlaceArrow.visibility = VISIBLE
            batteryPlaceArrow.showBlinkAnimation(ANIMATION_DURATION)
        }
    }

    private fun hideAllInstructions() {
        calibrateArrow.visibility = GONE
        calibrateArrow.clearAnimation()
        batteryPlaceArrow.visibility = GONE
        batteryPlaceArrow.clearAnimation()
        instructionTextView.visibility = GONE
    }
}