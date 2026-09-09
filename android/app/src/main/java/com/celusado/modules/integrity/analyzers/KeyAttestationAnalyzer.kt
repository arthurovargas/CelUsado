package com.celusado.modules.integrity.analyzers

import android.os.Build
import com.celusado.modules.integrity.models.IntegrityEvidenceItem
import com.celusado.modules.integrity.models.IntegrityEvidenceSource
import com.celusado.modules.integrity.models.IntegrityIndicator
import com.celusado.modules.integrity.models.IntegrityIndicatorType
import com.celusado.modules.integrity.models.IntegrityStatus

class KeyAttestationAnalyzer {

    fun analyze(): List<IntegrityIndicator> {
        val indicators = mutableListOf<IntegrityIndicator>()

        if (Build.VERSION.SDK_INT < KEY_ATTESTATION_MIN_API) {
            indicators.add(
                IntegrityIndicator(
                    type = IntegrityIndicatorType.KEY_ATTESTATION,
                    status = IntegrityStatus.NOT_SUPPORTED,
                    confidence = 1.0,
                    source = "Build.VERSION.SDK_INT",
                    value = Build.VERSION.SDK_INT.toString(),
                    evidence = listOf(
                        IntegrityEvidenceItem(
                            type = "API_LEVEL",
                            source = IntegrityEvidenceSource.ANDROID_BUILD,
                            field = "Build.VERSION.SDK_INT",
                            value = Build.VERSION.SDK_INT.toString(),
                            description = "Key Attestation requires API level $KEY_ATTESTATION_MIN_API or higher"
                        )
                    ),
                    limitations = listOf(
                        "Key Attestation is not available on API level ${Build.VERSION.SDK_INT}"
                    )
                )
            )
            return indicators
        }

        indicators.add(
            IntegrityIndicator(
                type = IntegrityIndicatorType.KEY_ATTESTATION,
                status = IntegrityStatus.NOT_AVAILABLE,
                confidence = 1.0,
                source = "KEY_ATTESTATION_API",
                value = null,
                evidence = listOf(
                    IntegrityEvidenceItem(
                        type = "KEY_ATTESTATION_REQUIRES_BACKEND",
                        source = IntegrityEvidenceSource.KEY_ATTESTATION_API,
                        field = "KeyAttestation",
                        value = null,
                        description = "Full Key Attestation requires backend verification"
                    )
                ),
                limitations = listOf(
                    "Key Attestation is available on API ${Build.VERSION.SDK_INT}+ but requires:",
                    "1. Generation of a key pair with attestation challenge",
                    "2. Retrieval of the attestation certificate chain",
                    "3. Cryptographic validation of the certificate chain (requires backend or Google's attestation service)",
                    "4. Analysis of RootOfTrust in the attestation data",
                    "Local-only implementation would be a false validation. Backend infrastructure is required."
                )
            )
        )

        return indicators
    }

    companion object {
        const val KEY_ATTESTATION_MIN_API = 24
    }
}
