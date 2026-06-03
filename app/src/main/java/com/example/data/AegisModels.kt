package com.example.data

data class SensorRequirement(
    val useAccelerometer: Boolean = false,
    val useGyroscope: Boolean = false,
    val useMagnetometer: Boolean = false,
    val useCameraPose: Boolean = false,
    val useCameraFace: Boolean = false,
    val useMicrophone: Boolean = false,
    val usePedometer: Boolean = false,
    val useGPS: Boolean = false,
    val useTouchscreen: Boolean = false
)

data class VerificationCondition(
    val description: String,
    val check: (sensorValues: Map<String, Double>) -> Boolean
)

data class ExerciseVerificationProfile(
    val exerciseType: String,
    val sensors: SensorRequirement,
    val sensorWeights: Map<String, Double>,
    val conditions: List<String> // Descriptions of conditions for Arabic localized display
)

data class PurityResult(
    val score: Int, // 0-100
    val level: String, // 'pure', 'acceptable', 'suspicious', 'cheat'
    val violations: List<String>
)

data class ShameRecord(
    val timestamp: Long,
    val exerciseType: String,
    val cheatMethod: String,
    val penaltyApplied: String
)
