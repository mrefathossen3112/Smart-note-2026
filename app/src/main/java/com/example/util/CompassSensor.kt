package com.example.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

data class CompassData(
    val azimuthDegrees: Float = 0f,
    val cardinalDirection: String = "N",
    val pitchDegrees: Float = 0f,
    val rollDegrees: Float = 0f,
    val accuracy: Int = SensorManager.SENSOR_STATUS_ACCURACY_HIGH
)

class CompassSensor(context: Context) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val rotationVectorSensor =
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometerSensor =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magneticSensor =
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _compassData = MutableStateFlow(CompassData())
    val compassData: StateFlow<CompassData> = _compassData.asStateFlow()

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private val gravityValues = FloatArray(3)
    private val magneticValues = FloatArray(3)
    private var hasGravity = false
    private var hasMagnetic = false

    private var smoothedAzimuth = 0f
    private val alpha = 0.15f // Smoothing factor

    fun startListening() {
        if (rotationVectorSensor != null) {
            sensorManager.registerListener(
                this,
                rotationVectorSensor,
                SensorManager.SENSOR_DELAY_UI
            )
        } else {
            accelerometerSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
            magneticSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            updateOrientation(orientationAngles[0], orientationAngles[1], orientationAngles[2], event.accuracy)
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, gravityValues, 0, 3)
            hasGravity = true
            processFallbackSensors(event.accuracy)
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magneticValues, 0, 3)
            hasMagnetic = true
            processFallbackSensors(event.accuracy)
        }
    }

    private fun processFallbackSensors(accuracy: Int) {
        if (hasGravity && hasMagnetic) {
            val success = SensorManager.getRotationMatrix(
                rotationMatrix,
                null,
                gravityValues,
                magneticValues
            )
            if (success) {
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                updateOrientation(orientationAngles[0], orientationAngles[1], orientationAngles[2], accuracy)
            }
        }
    }

    private fun updateOrientation(azimuthRad: Float, pitchRad: Float, rollRad: Float, accuracy: Int) {
        var degrees = Math.toDegrees(azimuthRad.toDouble()).toFloat()
        if (degrees < 0) {
            degrees += 360f
        }

        // Apply low-pass exponential smoothing
        smoothedAzimuth += alpha * (degrees - smoothedAzimuth)

        val cardinal = getCardinalDirection(smoothedAzimuth)
        _compassData.value = CompassData(
            azimuthDegrees = smoothedAzimuth,
            cardinalDirection = cardinal,
            pitchDegrees = Math.toDegrees(pitchRad.toDouble()).toFloat(),
            rollDegrees = Math.toDegrees(rollRad.toDouble()).toFloat(),
            accuracy = accuracy
        )
    }

    private fun getCardinalDirection(degrees: Float): String {
        val directions = arrayOf(
            "N", "NNE", "NE", "ENE",
            "E", "ESE", "SE", "SSE",
            "S", "SSW", "SW", "WSW",
            "W", "WNW", "NW", "NNW"
        )
        val index = ((degrees + 11.25f) / 22.5f).toInt() % 16
        return directions[index]
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
