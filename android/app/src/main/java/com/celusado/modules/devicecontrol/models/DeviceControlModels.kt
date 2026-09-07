package com.celusado.modules.devicecontrol.models

enum class DetectionStatus {
    CONFIRMED,
    ACTIVE,
    DECLARED,
    CAPABLE,
    DETECTED,
    NOT_DETECTED,
    NOT_ACCESSIBLE,
    NOT_AVAILABLE,
    NOT_DETERMINABLE,
    UNKNOWN
}

enum class IndicatorType {
    DEVICE_ADMIN,
    DEVICE_OWNER,
    PROFILE_OWNER,
    DEVICE_POLICY_CONTROLLER,
    ACCESSIBILITY_SERVICE,
    VPN_SERVICE,
    OVERLAY,
    BOOT_RECEIVER,
    PERSISTENT_SERVICE,
    FORCE_LOCK,
    WIPE_CAPABILITY,
    ADMINISTRATIVE_POLICY
}

enum class EvidenceSource {
    MANIFEST,
    PACKAGE_COMPONENT,
    SYSTEM_API,
    INDIRECT_DETECTION,
    KNOWN_PACKAGE,
    PERMISSION,
    INTENT_FILTER,
    SYSTEM_STATE,
    CONFIGURATION
}

data class EvidenceItem(
    val type: String,
    val source: EvidenceSource,
    val value: String? = null,
    val description: String? = null
)

data class DeviceControlIndicator(
    val type: IndicatorType,
    val packageName: String,
    val status: DetectionStatus,
    val confidence: Double,
    val evidence: List<EvidenceItem>,
    val limitations: List<String> = emptyList()
)

data class ControlCoverage(
    val status: String,
    val analyzersExecuted: List<String>,
    val limitations: List<String>
)

data class DeviceControlResult(
    val indicators: List<DeviceControlIndicator>,
    val coverage: ControlCoverage
)
