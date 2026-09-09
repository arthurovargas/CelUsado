package com.celusado.modules.integrity.analyzers

import android.os.Build
import com.celusado.modules.integrity.models.IntegrityEvidenceItem
import com.celusado.modules.integrity.models.IntegrityEvidenceSource
import com.celusado.modules.integrity.models.IntegrityIndicator
import com.celusado.modules.integrity.models.IntegrityIndicatorType
import com.celusado.modules.integrity.models.IntegrityStatus
import com.celusado.modules.packageanalyzer.models.NormalizedPackage

class KnownModificationAnalyzer {

    fun analyze(packages: List<NormalizedPackage>): List<IntegrityIndicator> {
        val indicators = mutableListOf<IntegrityIndicator>()

        indicators.addAll(checkKnownModificationPackages(packages))
        indicators.addAll(checkBuildPatterns())

        return indicators
    }

    private fun checkKnownModificationPackages(packages: List<NormalizedPackage>): List<IntegrityIndicator> {
        val indicators = mutableListOf<IntegrityIndicator>()

        for (pkg in packages) {
            val packageName = pkg.packageName.lowercase()

            for ((pattern, description) in KNOWN_MODIFICATION_PACKAGES) {
                if (packageName.contains(pattern)) {
                    indicators.add(
                        IntegrityIndicator(
                            type = IntegrityIndicatorType.KNOWN_MODIFICATION_PACKAGE,
                            status = IntegrityStatus.DETECTED,
                            confidence = 0.85,
                            source = "PACKAGE_MANAGER",
                            value = pkg.packageName,
                            evidence = listOf(
                                IntegrityEvidenceItem(
                                    type = "KNOWN_MODIFICATION_APP",
                                    source = IntegrityEvidenceSource.KNOWN_PACKAGE_DB,
                                    field = pkg.packageName,
                                    value = description,
                                    description = "Known modification-related application: ${pkg.label}"
                                )
                            ),
                            limitations = listOf(
                                "Application presence does not confirm system modification"
                            )
                        )
                    )
                }
            }
        }

        return indicators
    }

    private fun checkBuildPatterns(): List<IntegrityIndicator> {
        val indicators = mutableListOf<IntegrityIndicator>()

        val fingerprint: String = Build.FINGERPRINT ?: return indicators
        val display: String = Build.DISPLAY ?: return indicators

        for ((pattern, description) in KNOWN_BUILD_PATTERNS) {
            if (fingerprint.contains(pattern, ignoreCase = true) ||
                display.contains(pattern, ignoreCase = true)
            ) {
                indicators.add(
                    IntegrityIndicator(
                        type = IntegrityIndicatorType.CUSTOM_ROM_INDICATOR,
                        status = IntegrityStatus.DETECTED,
                        confidence = 0.80,
                        source = "Build.FINGERPRINT",
                        value = pattern,
                        evidence = listOf(
                            IntegrityEvidenceItem(
                                type = "BUILD_PATTERN",
                                source = IntegrityEvidenceSource.ANDROID_BUILD,
                                field = "Build.FINGERPRINT",
                                value = pattern,
                                description = "Known build pattern detected: $description"
                            )
                        ),
                        limitations = listOf(
                            "Custom ROM pattern detection is based on string matching"
                        )
                    )
                )
            }
        }

        return indicators
    }

    companion object {
        private val KNOWN_MODIFICATION_PACKAGES = mapOf(
            "magisk" to "Magisk root framework",
            "supersu" to "SuperSU root management",
            "superuser" to "Superuser management",
            "kingroot" to "KingRoot",
            "kingo" to "Kingo Root",
            "towelroot" to "TowelRoot",
            "cf.auto.exploit" to "CF Auto Root",
            "substrate" to "Cydia Substrate",
            "xposed" to "Xposed Framework",
            "edxposed" to "EdXposed",
            "lsposed" to "LSPosed",
            "riru" to "Riru",
            "zygisk" to "Zygisk",
            "shamiko" to "Shamiko (root hider)",
            "safetynet" to "SafetyNet bypass",
            "play.integrity" to "Play Integrity bypass",
            "hide.my.root" to "Root hider",
            "rootcloak" to "Root Cloak",
            "busybox" to "BusyBox (root tool)"
        )

        private val KNOWN_BUILD_PATTERNS = mapOf(
            "lineage" to "LineageOS",
            "cyanogenmod" to "CyanogenMod",
            "paranoid" to "Paranoid Android",
            "resurrection" to "Resurrection Remix",
            "crdroid" to "crDroid",
            "pixel" to "PixelExperience",
            "arrowos" to "ArrowOS",
            "aosp" to "AOSP-based ROM",
            "havoc" to "Havoc-OS",
            "evolution" to "Evolution-X",
            "validus" to "Validus",
            "aosip" to "AOSiP",
            "xyz" to "Custom ROM indicator"
        )
    }
}
