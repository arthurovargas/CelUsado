package com.celusado.modules.integrity.analyzers

import android.os.Build
import com.celusado.modules.integrity.models.IntegrityEvidenceItem
import com.celusado.modules.integrity.models.IntegrityEvidenceSource
import com.celusado.modules.integrity.models.IntegrityIndicator
import com.celusado.modules.integrity.models.IntegrityIndicatorType
import com.celusado.modules.integrity.models.IntegrityStatus

class BootIntegrityAnalyzer {

    fun analyze(): List<IntegrityIndicator> {
        val indicators = mutableListOf<IntegrityIndicator>()

        indicators.addAll(analyzeBootloaderState())
        indicators.addAll(analyzeVerifiedBoot())
        indicators.addAll(analyzeDeviceLocked())

        return indicators
    }

    private fun analyzeBootloaderState(): List<IntegrityIndicator> {
        val bootloader: String = Build.BOOTLOADER ?: return listOf(
            IntegrityIndicator(
                type = IntegrityIndicatorType.BOOTLOADER_STATE,
                status = IntegrityStatus.NOT_DETERMINABLE,
                confidence = 0.0,
                source = "Build.BOOTLOADER",
                value = null,
                evidence = emptyList(),
                limitations = listOf(
                    "Build.BOOTLOADER is null or empty"
                )
            )
        )

        if (bootloader.isEmpty()) {
            return listOf(
                IntegrityIndicator(
                    type = IntegrityIndicatorType.BOOTLOADER_STATE,
                    status = IntegrityStatus.NOT_DETERMINABLE,
                    confidence = 0.0,
                    source = "Build.BOOTLOADER",
                    value = null,
                    evidence = emptyList(),
                    limitations = listOf(
                        "Build.BOOTLOADER is null or empty"
                    )
                )
            )
        }

        val isUnlocked = bootloader.contains("unlocked", ignoreCase = true) ||
            bootloader.contains("dev", ignoreCase = true)

        if (isUnlocked) {
            return listOf(
                IntegrityIndicator(
                    type = IntegrityIndicatorType.BOOTLOADER_STATE,
                    status = IntegrityStatus.DETECTED,
                    confidence = 0.70,
                    source = "Build.BOOTLOADER",
                    value = bootloader,
                    evidence = listOf(
                        IntegrityEvidenceItem(
                            type = "BOOTLOADER_STRING",
                            source = IntegrityEvidenceSource.ANDROID_BUILD,
                            field = "Build.BOOTLOADER",
                            value = bootloader,
                            description = "Bootloader string suggests unlocked state"
                        )
                    ),
                    limitations = listOf(
                        "Bootloader state inferred from Build.BOOTLOADER string, not directly verified"
                    )
                )
            )
        }

        return listOf(
            IntegrityIndicator(
                type = IntegrityIndicatorType.BOOTLOADER_STATE,
                status = IntegrityStatus.NOT_DETERMINABLE,
                confidence = 0.30,
                source = "Build.BOOTLOADER",
                value = bootloader,
                evidence = listOf(
                    IntegrityEvidenceItem(
                        type = "BOOTLOADER_STRING",
                        source = IntegrityEvidenceSource.ANDROID_BUILD,
                        field = "Build.BOOTLOADER",
                        value = bootloader
                    )
                ),
                limitations = listOf(
                    "No reliable public API to directly determine bootloader lock state from a normal application",
                    "Build.BOOTLOADER value does not definitively indicate lock state"
                )
            )
        )
    }

    private fun analyzeVerifiedBoot(): List<IntegrityIndicator> {
        val verifiedBootState: String? = getVerifiedBootState()

        if (verifiedBootState == null) {
            return listOf(
                IntegrityIndicator(
                    type = IntegrityIndicatorType.VERIFIED_BOOT_STATE,
                    status = IntegrityStatus.NOT_DETERMINABLE,
                    confidence = 0.0,
                    source = "ro.boot.verifiedbootstate",
                    value = null,
                    evidence = emptyList(),
                    limitations = listOf(
                        "Verified boot state is not reliably readable from a normal application"
                    )
                )
            )
        }

        val status = when (verifiedBootState.lowercase()) {
            "verified" -> IntegrityStatus.DETECTED
            "selfsigned" -> IntegrityStatus.DETECTED
            "unverified" -> IntegrityStatus.DETECTED
            "failed" -> IntegrityStatus.DETECTED
            else -> IntegrityStatus.NOT_DETERMINABLE
        }

        val description = when (verifiedBootState.lowercase()) {
            "verified" -> "Verified Boot chain is intact"
            "selfsigned" -> "Device is using self-signed boot image"
            "unverified" -> "Verified Boot chain is not verified"
            "failed" -> "Verified Boot chain verification failed"
            else -> "Unknown verified boot state"
        }

        return listOf(
            IntegrityIndicator(
                type = IntegrityIndicatorType.VERIFIED_BOOT_STATE,
                status = status,
                confidence = 0.75,
                source = "ro.boot.verifiedbootstate",
                value = verifiedBootState,
                evidence = listOf(
                    IntegrityEvidenceItem(
                        type = "VERIFIED_BOOT_STATE",
                        source = IntegrityEvidenceSource.SYSTEM_PROPERTY,
                        field = "ro.boot.verifiedbootstate",
                        value = verifiedBootState,
                        description = description
                    )
                ),
                limitations = listOf(
                    "Verified boot state read from system property may not be available on all devices",
                    "SelfSigned or Unverified state does not automatically indicate root"
                )
            )
        )
    }

    private fun analyzeDeviceLocked(): List<IntegrityIndicator> {
        return listOf(
            IntegrityIndicator(
                type = IntegrityIndicatorType.DEVICE_LOCKED,
                status = IntegrityStatus.NOT_DETERMINABLE,
                confidence = 0.0,
                source = "UNAVAILABLE",
                value = null,
                evidence = emptyList(),
                limitations = listOf(
                    "Device locked state cannot be reliably determined from a normal application"
                )
            )
        )
    }

    private fun getVerifiedBootState(): String? {
        return try {
            val clazz = Class.forName("android.os.SystemProperties")
            val get = clazz.getMethod("get", String::class.java)
            val state = get.invoke(null, "ro.boot.verifiedbootstate") as? String
            if (state.isNullOrEmpty()) null else state
        } catch (e: Exception) {
            null
        }
    }
}
