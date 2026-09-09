package com.celusado.modules.integrity.analyzers

import android.os.Build
import com.celusado.modules.integrity.models.IntegrityEvidenceItem
import com.celusado.modules.integrity.models.IntegrityEvidenceSource
import com.celusado.modules.integrity.models.IntegrityIndicator
import com.celusado.modules.integrity.models.IntegrityIndicatorType
import com.celusado.modules.integrity.models.IntegrityStatus
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class SecurityPatchAnalyzer {

    fun analyze(): List<IntegrityIndicator> {
        val indicators = mutableListOf<IntegrityIndicator>()
        val patchLevel: String? = Build.VERSION.SECURITY_PATCH

        if (patchLevel.isNullOrEmpty()) {
            indicators.add(
                IntegrityIndicator(
                    type = IntegrityIndicatorType.SECURITY_PATCH_LEVEL,
                    status = IntegrityStatus.NOT_AVAILABLE,
                    confidence = 1.0,
                    source = "Build.VERSION.SECURITY_PATCH",
                    value = null,
                    evidence = listOf(
                        IntegrityEvidenceItem(
                            type = "SECURITY_PATCH_MISSING",
                            source = IntegrityEvidenceSource.ANDROID_BUILD,
                            field = "Build.VERSION.SECURITY_PATCH",
                            value = null
                        )
                    ),
                    limitations = listOf("Security patch level is not available on this device")
                )
            )
            return indicators
        }

        indicators.add(
            IntegrityIndicator(
                type = IntegrityIndicatorType.SECURITY_PATCH_LEVEL,
                status = IntegrityStatus.DETECTED,
                confidence = 1.0,
                source = "Build.VERSION.SECURITY_PATCH",
                value = patchLevel,
                evidence = listOf(
                    IntegrityEvidenceItem(
                        type = "SECURITY_PATCH_LEVEL",
                        source = IntegrityEvidenceSource.ANDROID_BUILD,
                        field = "Build.VERSION.SECURITY_PATCH",
                        value = patchLevel
                    )
                )
            )
        )

        val isOld = isPatchOld(patchLevel)
        if (isOld) {
            indicators.add(
                IntegrityIndicator(
                    type = IntegrityIndicatorType.SECURITY_PATCH_OLD,
                    status = IntegrityStatus.DETECTED,
                    confidence = 0.95,
                    source = "Build.VERSION.SECURITY_PATCH",
                    value = patchLevel,
                    evidence = listOf(
                        IntegrityEvidenceItem(
                            type = "SECURITY_PATCH_AGE",
                            source = IntegrityEvidenceSource.ANDROID_BUILD,
                            field = "Build.VERSION.SECURITY_PATCH",
                            value = patchLevel,
                            description = "Security patch is older than 90 days"
                        )
                    ),
                    limitations = listOf(
                        "Security patch age is based on comparison with current date rules. " +
                        "Rules should be versioned and updated externally."
                    )
                )
            )
        }

        return indicators
    }

    private fun isPatchOld(patchLevel: String): Boolean {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val patchDate = formatter.parse(patchLevel) ?: return false
            val now = System.currentTimeMillis()
            val diffDays = TimeUnit.MILLISECONDS.toDays(now - patchDate.time)
            diffDays > SECURITY_PATCH_THRESHOLD_DAYS
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        const val SECURITY_PATCH_THRESHOLD_DAYS = 90L
    }
}
