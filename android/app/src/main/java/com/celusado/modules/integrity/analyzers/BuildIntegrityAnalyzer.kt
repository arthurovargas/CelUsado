package com.celusado.modules.integrity.analyzers

import android.os.Build
import com.celusado.modules.integrity.models.IntegrityEvidenceItem
import com.celusado.modules.integrity.models.IntegrityEvidenceSource
import com.celusado.modules.integrity.models.IntegrityIndicator
import com.celusado.modules.integrity.models.IntegrityIndicatorType
import com.celusado.modules.integrity.models.IntegrityStatus

class BuildIntegrityAnalyzer {

    fun analyze(): List<IntegrityIndicator> {
        val indicators = mutableListOf<IntegrityIndicator>()

        indicators.addAll(analyzeTags())
        indicators.addAll(analyzeType())
        indicators.addAll(analyzeFingerprint())
        indicators.addAll(analyzeDisplay())
        indicators.addAll(analyzeHardware())
        indicators.addAll(analyzeBuildProperties())

        return indicators
    }

    private fun analyzeTags(): List<IntegrityIndicator> {
        val indicators = mutableListOf<IntegrityIndicator>()
        val tags: String = Build.TAGS ?: return indicators

        if (tags == "test-keys") {
            indicators.add(
                IntegrityIndicator(
                    type = IntegrityIndicatorType.BUILD_TEST_KEYS,
                    status = IntegrityStatus.DETECTED,
                    confidence = 0.98,
                    source = "Build.TAGS",
                    value = tags,
                    evidence = listOf(
                        IntegrityEvidenceItem(
                            type = "BUILD_TAG",
                            source = IntegrityEvidenceSource.ANDROID_BUILD,
                            field = "Build.TAGS",
                            value = tags
                        )
                    )
                )
            )
        } else if (tags == "release-keys") {
            indicators.add(
                IntegrityIndicator(
                    type = IntegrityIndicatorType.BUILD_TEST_KEYS,
                    status = IntegrityStatus.NOT_DETECTED,
                    confidence = 0.95,
                    source = "Build.TAGS",
                    value = tags,
                    evidence = listOf(
                        IntegrityEvidenceItem(
                            type = "BUILD_TAG",
                            source = IntegrityEvidenceSource.ANDROID_BUILD,
                            field = "Build.TAGS",
                            value = tags
                        )
                    )
                )
            )
        } else {
            indicators.add(
                IntegrityIndicator(
                    type = IntegrityIndicatorType.BUILD_TEST_KEYS,
                    status = IntegrityStatus.NOT_DETERMINABLE,
                    confidence = 0.3,
                    source = "Build.TAGS",
                    value = tags,
                    evidence = listOf(
                        IntegrityEvidenceItem(
                            type = "BUILD_TAG",
                            source = IntegrityEvidenceSource.ANDROID_BUILD,
                            field = "Build.TAGS",
                            value = tags
                        )
                    ),
                    limitations = listOf("Unexpected TAGS value: $tags")
                )
            )
        }

        return indicators
    }

    private fun analyzeType(): List<IntegrityIndicator> {
        val indicators = mutableListOf<IntegrityIndicator>()
        val type: String = Build.TYPE ?: return indicators

        if (type == "userdebug") {
            indicators.add(
                IntegrityIndicator(
                    type = IntegrityIndicatorType.BUILD_USERDEBUG,
                    status = IntegrityStatus.DETECTED,
                    confidence = 0.95,
                    source = "Build.TYPE",
                    value = type,
                    evidence = listOf(
                        IntegrityEvidenceItem(
                            type = "BUILD_TYPE",
                            source = IntegrityEvidenceSource.ANDROID_BUILD,
                            field = "Build.TYPE",
                            value = type
                        )
                    )
                )
            )
        } else if (type == "eng") {
            indicators.add(
                IntegrityIndicator(
                    type = IntegrityIndicatorType.BUILD_ENGINEERING_BUILD,
                    status = IntegrityStatus.DETECTED,
                    confidence = 0.98,
                    source = "Build.TYPE",
                    value = type,
                    evidence = listOf(
                        IntegrityEvidenceItem(
                            type = "BUILD_TYPE",
                            source = IntegrityEvidenceSource.ANDROID_BUILD,
                            field = "Build.TYPE",
                            value = type
                        )
                    )
                )
            )
        }

        return indicators
    }

    private fun analyzeFingerprint(): List<IntegrityIndicator> {
        val indicators = mutableListOf<IntegrityIndicator>()
        val fingerprint: String = Build.FINGERPRINT ?: return indicators

        val isNonStandard = !fingerprint.startsWith("${Build.MANUFACTURER}/") &&
            !fingerprint.startsWith("${Build.BRAND}/") &&
            !fingerprint.startsWith("generic/") &&
            !fingerprint.startsWith("unknown/")

        if (isNonStandard) {
            indicators.add(
                IntegrityIndicator(
                    type = IntegrityIndicatorType.BUILD_NON_STANDARD_FINGERPRINT,
                    status = IntegrityStatus.DETECTED,
                    confidence = 0.7,
                    source = "Build.FINGERPRINT",
                    value = fingerprint,
                    evidence = listOf(
                        IntegrityEvidenceItem(
                            type = "FINGERPRINT_PATTERN",
                            source = IntegrityEvidenceSource.ANDROID_BUILD,
                            field = "Build.FINGERPRINT",
                            value = fingerprint
                        )
                    ),
                    limitations = listOf("Fingerprint pattern does not match standard OEM format")
                )
            )
        }

        return indicators
    }

    private fun analyzeDisplay(): List<IntegrityIndicator> {
        val indicators = mutableListOf<IntegrityIndicator>()
        val display: String = Build.DISPLAY ?: return indicators

        if (display.contains("debug", ignoreCase = true) ||
            display.contains("test", ignoreCase = true) ||
            display.contains("engineering", ignoreCase = true)
        ) {
            indicators.add(
                IntegrityIndicator(
                    type = IntegrityIndicatorType.BUILD_DEVELOPMENT_CONFIG,
                    status = IntegrityStatus.DETECTED,
                    confidence = 0.85,
                    source = "Build.DISPLAY",
                    value = display,
                    evidence = listOf(
                        IntegrityEvidenceItem(
                            type = "DISPLAY_PATTERN",
                            source = IntegrityEvidenceSource.ANDROID_BUILD,
                            field = "Build.DISPLAY",
                            value = display
                        )
                    )
                )
            )
        }

        return indicators
    }

    private fun analyzeHardware(): List<IntegrityIndicator> {
        val indicators = mutableListOf<IntegrityIndicator>()
        val hardware: String = Build.HARDWARE ?: return indicators

        if (hardware == "goldfish" || hardware == "ranchu" || hardware == "generic") {
            indicators.add(
                IntegrityIndicator(
                    type = IntegrityIndicatorType.BUILD_DEVELOPMENT_CONFIG,
                    status = IntegrityStatus.DETECTED,
                    confidence = 0.90,
                    source = "Build.HARDWARE",
                    value = hardware,
                    evidence = listOf(
                        IntegrityEvidenceItem(
                            type = "HARDWARE_EMULATOR",
                            source = IntegrityEvidenceSource.ANDROID_BUILD,
                            field = "Build.HARDWARE",
                            value = hardware
                        )
                    ),
                    limitations = listOf("Emulator hardware detected")
                )
            )
        }

        return indicators
    }

    private fun analyzeBuildProperties(): List<IntegrityIndicator> {
        val indicators = mutableListOf<IntegrityIndicator>()

        val roDebuggable = getSystemProperty("ro.debuggable", "0")
        if (roDebuggable == "1") {
            indicators.add(
                IntegrityIndicator(
                    type = IntegrityIndicatorType.BUILD_DEBUGGABLE,
                    status = IntegrityStatus.DETECTED,
                    confidence = 0.95,
                    source = "ro.debuggable",
                    value = roDebuggable,
                    evidence = listOf(
                        IntegrityEvidenceItem(
                            type = "SYSTEM_PROPERTY",
                            source = IntegrityEvidenceSource.SYSTEM_PROPERTY,
                            field = "ro.debuggable",
                            value = roDebuggable
                        )
                    )
                )
            )
        }

        return indicators
    }

    private fun getSystemProperty(key: String, defaultValue: String): String {
        return try {
            val clazz = Class.forName("android.os.SystemProperties")
            val get = clazz.getMethod("get", String::class.java, String::class.java)
            get.invoke(null, key, defaultValue) as? String ?: defaultValue
        } catch (e: Exception) {
            defaultValue
        }
    }
}
