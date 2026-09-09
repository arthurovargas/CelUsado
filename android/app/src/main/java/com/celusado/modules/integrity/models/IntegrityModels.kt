package com.celusado.modules.integrity.models

enum class IntegrityStatus {
    DETECTED,
    NOT_DETECTED,
    NOT_ACCESSIBLE,
    NOT_AVAILABLE,
    NOT_SUPPORTED,
    NOT_DETERMINABLE,
    ERROR
}

enum class IntegrityIndicatorType {
    BUILD_TEST_KEYS,
    BUILD_ENGINEERING_BUILD,
    BUILD_USERDEBUG,
    BUILD_DEBUGGABLE,
    BUILD_NON_STANDARD_FINGERPRINT,
    BUILD_INCONSISTENT_PROPERTIES,
    BUILD_DEVELOPMENT_CONFIG,
    SECURITY_PATCH_LEVEL,
    SECURITY_PATCH_OLD,
    ROOT_BINARY_INDICATOR,
    ROOT_MANAGEMENT_APP,
    ROOT_FRAMEWORK_INDICATOR,
    BOOTLOADER_STATE,
    VERIFIED_BOOT_STATE,
    DEVICE_LOCKED,
    KEY_ATTESTATION,
    PLAY_INTEGRITY,
    KNOWN_MODIFICATION_PACKAGE,
    KNOWN_MODIFICATION_FRAMEWORK,
    CUSTOM_ROM_INDICATOR
}

enum class IntegrityEvidenceSource {
    ANDROID_BUILD,
    SYSTEM_PROPERTY,
    FILE_SYSTEM,
    PACKAGE_MANAGER,
    MANIFEST,
    KNOWN_PACKAGE_DB,
    PLAY_INTEGRITY_API,
    KEY_ATTESTATION_API,
    SYSTEM_STATE,
    INDIRECT_DETECTION,
    CONFIGURATION
}

enum class OverallIntegrityStatus {
    NORMAL,
    INDICATOR,
    MULTIPLE_INDICATORS,
    STRONG_INTEGRITY_EVIDENCE,
    INTEGRITY_COMPROMISED_INDICATOR,
    NOT_DETERMINABLE
}

data class IntegrityEvidenceItem(
    val type: String,
    val source: IntegrityEvidenceSource,
    val field: String? = null,
    val value: String? = null,
    val description: String? = null
)

data class IntegrityIndicator(
    val type: IntegrityIndicatorType,
    val status: IntegrityStatus,
    val confidence: Double,
    val source: String,
    val value: String? = null,
    val evidence: List<IntegrityEvidenceItem> = emptyList(),
    val limitations: List<String> = emptyList()
)

data class IntegrityCoverage(
    val status: String,
    val availableChecks: List<String>,
    val unavailableChecks: List<String>,
    val limitations: List<String>
)

data class IntegrityResult(
    val overallStatus: OverallIntegrityStatus,
    val indicators: List<IntegrityIndicator>,
    val coverage: IntegrityCoverage,
    val limitations: List<String>
)
