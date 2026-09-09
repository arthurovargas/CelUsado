package com.celusado.modules.integrity.analyzers

import com.celusado.modules.integrity.models.*
import org.junit.Assert.*
import org.junit.Test

class IntegrityIndicatorBuilderTest {

    private val builder = IntegrityIndicatorBuilder()

    private fun createIndicator(
        type: IntegrityIndicatorType,
        status: IntegrityStatus,
        confidence: Double = 0.8,
        source: String = "TEST",
        value: String? = null
    ): IntegrityIndicator {
        return IntegrityIndicator(
            type = type,
            status = status,
            confidence = confidence,
            source = source,
            value = value,
            evidence = listOf(
                IntegrityEvidenceItem(
                    type = "TEST_EVIDENCE",
                    source = IntegrityEvidenceSource.ANDROID_BUILD,
                    field = "test",
                    value = "test"
                )
            )
        )
    }

    @Test
    fun `build with no indicators produces NORMAL status`() {
        val result = builder.build(
            allIndicators = emptyList(),
            executedAnalyzers = listOf("BUILD_INTEGRITY"),
            unavailableAnalyzers = emptyList(),
            globalLimitations = emptyList()
        )
        assertEquals(OverallIntegrityStatus.NORMAL, result.overallStatus)
        assertTrue(result.indicators.isEmpty())
    }

    @Test
    fun `build with single detected indicator produces INDICATOR status`() {
        val indicators = listOf(
            createIndicator(IntegrityIndicatorType.BUILD_TEST_KEYS, IntegrityStatus.DETECTED)
        )
        val result = builder.build(
            allIndicators = indicators,
            executedAnalyzers = listOf("BUILD_INTEGRITY"),
            unavailableAnalyzers = emptyList(),
            globalLimitations = emptyList()
        )
        assertEquals(OverallIntegrityStatus.INDICATOR, result.overallStatus)
    }

    @Test
    fun `build with multiple detected indicators produces MULTIPLE_INDICATORS status`() {
        val indicators = listOf(
            createIndicator(IntegrityIndicatorType.BUILD_TEST_KEYS, IntegrityStatus.DETECTED),
            createIndicator(IntegrityIndicatorType.ROOT_MANAGEMENT_APP, IntegrityStatus.DETECTED)
        )
        val result = builder.build(
            allIndicators = indicators,
            executedAnalyzers = listOf("BUILD_INTEGRITY", "ROOT_APPS"),
            unavailableAnalyzers = emptyList(),
            globalLimitations = emptyList()
        )
        assertEquals(OverallIntegrityStatus.MULTIPLE_INDICATORS, result.overallStatus)
    }

    @Test
    fun `build with verified boot verified value produces STRONG_INTEGRITY_EVIDENCE`() {
        val indicators = listOf(
            createIndicator(
                IntegrityIndicatorType.VERIFIED_BOOT_STATE,
                IntegrityStatus.DETECTED,
                source = "ro.boot.verifiedbootstate",
                value = "verified"
            )
        )
        val result = builder.build(
            allIndicators = indicators,
            executedAnalyzers = listOf("BOOT_INTEGRITY"),
            unavailableAnalyzers = emptyList(),
            globalLimitations = emptyList()
        )
        assertEquals(OverallIntegrityStatus.STRONG_INTEGRITY_EVIDENCE, result.overallStatus)
    }

    @Test
    fun `coverage is COMPLETE when no limitations`() {
        val result = builder.build(
            allIndicators = emptyList(),
            executedAnalyzers = listOf("BUILD_INTEGRITY", "SECURITY_PATCH"),
            unavailableAnalyzers = emptyList(),
            globalLimitations = emptyList()
        )
        assertEquals("COMPLETE", result.coverage.status)
    }

    @Test
    fun `coverage is PARTIAL with some unavailable analyzers`() {
        val result = builder.build(
            allIndicators = emptyList(),
            executedAnalyzers = listOf("BUILD_INTEGRITY"),
            unavailableAnalyzers = listOf("KEY_ATTESTATION"),
            globalLimitations = emptyList()
        )
        assertEquals("PARTIAL", result.coverage.status)
    }

    @Test
    fun `coverage is LIMITED with many unavailable analyzers`() {
        val result = builder.build(
            allIndicators = emptyList(),
            executedAnalyzers = listOf("BUILD_INTEGRITY"),
            unavailableAnalyzers = listOf("KEY_ATTESTATION", "PLAY_INTEGRITY", "BOOT_INTEGRITY"),
            globalLimitations = emptyList()
        )
        assertEquals("LIMITED", result.coverage.status)
    }

    @Test
    fun `deduplication merges evidence from same type and source`() {
        val indicators = listOf(
            createIndicator(IntegrityIndicatorType.BUILD_TEST_KEYS, IntegrityStatus.DETECTED, 0.9, "Build.TAGS"),
            createIndicator(IntegrityIndicatorType.BUILD_TEST_KEYS, IntegrityStatus.DETECTED, 0.95, "Build.TAGS")
        )
        val result = builder.build(
            allIndicators = indicators,
            executedAnalyzers = listOf("BUILD_INTEGRITY"),
            unavailableAnalyzers = emptyList(),
            globalLimitations = emptyList()
        )
        val testKeysIndicators = result.indicators.filter { it.type == IntegrityIndicatorType.BUILD_TEST_KEYS }
        assertEquals(1, testKeysIndicators.size)
        assertEquals(0.95, testKeysIndicators.first().confidence, 0.001)
    }

    @Test
    fun `global limitations are included in result`() {
        val limitations = listOf("Test limitation 1", "Test limitation 2")
        val result = builder.build(
            allIndicators = emptyList(),
            executedAnalyzers = listOf("BUILD_INTEGRITY"),
            unavailableAnalyzers = emptyList(),
            globalLimitations = limitations
        )
        assertEquals(limitations, result.limitations)
    }

    @Test
    fun `indicator confidence is preserved`() {
        val indicators = listOf(
            createIndicator(IntegrityIndicatorType.BUILD_TEST_KEYS, IntegrityStatus.DETECTED, 0.98)
        )
        val result = builder.build(
            allIndicators = indicators,
            executedAnalyzers = listOf("BUILD_INTEGRITY"),
            unavailableAnalyzers = emptyList(),
            globalLimitations = emptyList()
        )
        assertEquals(0.98, result.indicators.first().confidence, 0.001)
    }
}
