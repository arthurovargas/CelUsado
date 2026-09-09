package com.celusado.modules.integrity.analyzers

import com.celusado.modules.integrity.models.IntegrityIndicatorType
import com.celusado.modules.integrity.models.IntegrityStatus
import org.junit.Assert.*
import org.junit.Test

class SecurityPatchAnalyzerTest {

    private val analyzer = SecurityPatchAnalyzer()

    @Test
    fun `analyze returns at least one indicator`() {
        val indicators = analyzer.analyze()
        assertNotNull(indicators)
        assertTrue(indicators.isNotEmpty())
    }

    @Test
    fun `analyze includes SECURITY_PATCH_LEVEL indicator`() {
        val indicators = analyzer.analyze()
        val patchIndicator = indicators.find { it.type == IntegrityIndicatorType.SECURITY_PATCH_LEVEL }
        assertNotNull(patchIndicator)
    }

    @Test
    fun `patch level indicator has valid status`() {
        val indicators = analyzer.analyze()
        val patchIndicator = indicators.find { it.type == IntegrityIndicatorType.SECURITY_PATCH_LEVEL }
        assertNotNull(patchIndicator)
        assertTrue(
            patchIndicator!!.status in listOf(
                IntegrityStatus.DETECTED,
                IntegrityStatus.NOT_AVAILABLE
            )
        )
    }

    @Test
    fun `when patch is available, value is not null`() {
        val indicators = analyzer.analyze()
        val patchIndicator = indicators.find { it.type == IntegrityIndicatorType.SECURITY_PATCH_LEVEL }
        if (patchIndicator?.status == IntegrityStatus.DETECTED) {
            assertNotNull(patchIndicator.value)
            assertTrue(patchIndicator.value!!.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
        }
    }

    @Test
    fun `old patch produces SECURITY_PATCH_OLD indicator`() {
        val indicators = analyzer.analyze()
        val oldPatchIndicator = indicators.find { it.type == IntegrityIndicatorType.SECURITY_PATCH_OLD }
        if (oldPatchIndicator != null) {
            assertEquals(IntegrityStatus.DETECTED, oldPatchIndicator.status)
            assertTrue(oldPatchIndicator.confidence in 0.0..1.0)
            assertTrue(oldPatchIndicator.limitations.isNotEmpty())
        }
    }

    @Test
    fun `patch level indicator confidence is 1_0 when detected`() {
        val indicators = analyzer.analyze()
        val patchIndicator = indicators.find { it.type == IntegrityIndicatorType.SECURITY_PATCH_LEVEL && it.status == IntegrityStatus.DETECTED }
        if (patchIndicator != null) {
            assertEquals(1.0, patchIndicator.confidence, 0.001)
        }
    }

    @Test
    fun `all indicators have evidence when status is DETECTED`() {
        val indicators = analyzer.analyze()
        for (indicator in indicators.filter { it.status == IntegrityStatus.DETECTED }) {
            assertTrue(
                "Indicator ${indicator.type} should have evidence when DETECTED",
                indicator.evidence.isNotEmpty()
            )
        }
    }

    @Test
    fun `old patch indicator includes limitations`() {
        val indicators = analyzer.analyze()
        val oldPatchIndicator = indicators.find { it.type == IntegrityIndicatorType.SECURITY_PATCH_OLD }
        if (oldPatchIndicator != null) {
            assertTrue(oldPatchIndicator.limitations.isNotEmpty())
            assertTrue(oldPatchIndicator.limitations.any { it.contains("versioned") || it.contains("Rules") })
        }
    }

    @Test
    fun `analyzer does not declare device insecure`() {
        val indicators = analyzer.analyze()
        for (indicator in indicators) {
            assertNotEquals(
                "Security patch should never declare device insecure",
                "INSECURE",
                indicator.type.name
            )
        }
    }
}
