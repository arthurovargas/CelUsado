package com.celusado.modules.integrity.analyzers

import com.celusado.modules.integrity.models.IntegrityIndicatorType
import com.celusado.modules.integrity.models.IntegrityStatus
import org.junit.Assert.*
import org.junit.Test

class KeyAttestationAnalyzerTest {

    private val analyzer = KeyAttestationAnalyzer()

    @Test
    fun `analyze returns at least one indicator`() {
        val indicators = analyzer.analyze()
        assertNotNull(indicators)
        assertTrue(indicators.isNotEmpty())
    }

    @Test
    fun `analyze includes KEY_ATTESTATION indicator`() {
        val indicators = analyzer.analyze()
        val keyAttestationIndicator = indicators.find { it.type == IntegrityIndicatorType.KEY_ATTESTATION }
        assertNotNull(keyAttestationIndicator)
    }

    @Test
    fun `key attestation is NOT_SUPPORTED or NOT_AVAILABLE`() {
        val indicators = analyzer.analyze()
        val keyAttestationIndicator = indicators.find { it.type == IntegrityIndicatorType.KEY_ATTESTATION }
        if (keyAttestationIndicator != null) {
            assertTrue(
                keyAttestationIndicator.status in listOf(
                    IntegrityStatus.NOT_SUPPORTED,
                    IntegrityStatus.NOT_AVAILABLE
                )
            )
        }
    }

    @Test
    fun `key attestation has limitations`() {
        val indicators = analyzer.analyze()
        val keyAttestationIndicator = indicators.find { it.type == IntegrityIndicatorType.KEY_ATTESTATION }
        if (keyAttestationIndicator != null) {
            assertTrue(keyAttestationIndicator.limitations.isNotEmpty())
        }
    }

    @Test
    fun `key attestation does not claim DETECTED status`() {
        val indicators = analyzer.analyze()
        val keyAttestationIndicator = indicators.find { it.type == IntegrityIndicatorType.KEY_ATTESTATION }
        if (keyAttestationIndicator != null) {
            assertNotEquals(
                "Should not claim verification without backend",
                IntegrityStatus.DETECTED,
                keyAttestationIndicator.status
            )
        }
    }

    @Test
    fun `key attestation confidence is 1_0`() {
        val indicators = analyzer.analyze()
        val keyAttestationIndicator = indicators.find { it.type == IntegrityIndicatorType.KEY_ATTESTATION }
        if (keyAttestationIndicator != null) {
            assertEquals(1.0, keyAttestationIndicator.confidence, 0.001)
        }
    }
}
