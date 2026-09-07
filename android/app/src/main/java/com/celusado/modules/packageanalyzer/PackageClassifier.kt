package com.celusado.modules.packageanalyzer

import android.os.Build
import com.celusado.modules.packageanalyzer.models.AppClassification
import com.celusado.modules.packageanalyzer.models.ClassificationResult
import com.celusado.modules.packageanalyzer.models.NormalizedPackage

class PackageClassifier {

    private val googlePrefixes = listOf(
        "com.google.android",
        "com.google.",
        "com.android.vending",
        "com.android.chrome",
        "com.google.gms",
        "com.google.android.gms"
    )

    private val samsungPrefixes = listOf(
        "com.samsung.",
        "com.sec.",
        "com.samsung.android.knox"
    )

    private val carrierPrefixes = listOf(
        "com.att.",
        "com.verizon.",
        "com.tmobile.",
        "com.sprint.",
        "com.orange.",
        "com.vodafone.",
        "com.claro."
    )

    fun classify(normalized: NormalizedPackage): ClassificationResult {
        val evidence = mutableListOf<String>()
        var category = AppClassification.UNKNOWN
        var confidence = 0.0

        // 1. System flag
        if (normalized.flags.system) {
            evidence.add("SYSTEM_FLAG")
            category = AppClassification.SYSTEM
            confidence = 0.5
        }

        // 2. Package name known prefixes
        val pkg = normalized.packageName.lowercase()

        // Google
        if (googlePrefixes.any { pkg.startsWith(it) }) {
            evidence.add("GOOGLE_PACKAGE_PREFIX")
            category = AppClassification.GOOGLE
            confidence = 0.9
        }

        // Samsung/OEM
        if (samsungPrefixes.any { pkg.startsWith(it) }) {
            evidence.add("SAMSUNG_PACKAGE_PREFIX")
            category = AppClassification.OEM
            confidence = 0.9
        }

        // Carrier
        if (carrierPrefixes.any { pkg.startsWith(it) }) {
            evidence.add("CARRIER_PACKAGE_PREFIX")
            category = AppClassification.CARRIER
            confidence = 0.85
        }

        // 3. Manufacturer check
        if (Build.MANUFACTURER.equals("samsung", ignoreCase = true) && pkg.startsWith("com.samsung.")) {
            evidence.add("MANUFACTURER_MATCH")
            category = AppClassification.OEM
            confidence = 0.95
        }

        // 4. If system app but no specific classification
        if (normalized.flags.system && category == AppClassification.SYSTEM) {
            confidence = 0.6
            evidence.add("SYSTEM_APP_DEFAULT")
        }

        // 5. If no classification at all, mark as user or unknown
        if (category == AppClassification.UNKNOWN) {
            if (!normalized.flags.system) {
                category = AppClassification.USER
                confidence = 0.7
                evidence.add("NON_SYSTEM_APP")
            }
        }

        // Ensure confidence is reasonable
        if (confidence == 0.0) {
            confidence = 0.3
            evidence.add("LOW_CONFIDENCE")
        }

        return ClassificationResult(
            category = category,
            confidence = confidence.coerceAtMost(1.0),
            evidence = evidence
        )
    }
}
