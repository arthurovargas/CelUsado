package com.celusado.modules.integrity.analyzers

import com.celusado.modules.integrity.models.IntegrityIndicatorType
import com.celusado.modules.integrity.models.IntegrityStatus
import org.junit.Assert.*
import org.junit.Test

class BuildIntegrityAnalyzerTest {

    private val analyzer = BuildIntegrityAnalyzer()

    @Test
    fun `analyze returns a list`() {
        val indicators = analyzer.analyze()
        assertNotNull(indicators)
    }

    @Test
    fun `all returned indicators have valid types`() {
        val indicators = analyzer.analyze()
        for (indicator in indicators) {
            assertTrue(
                "Indicator type ${indicator.type} should be a valid IntegrityIndicatorType",
                indicator.type in IntegrityIndicatorType.values()
            )
        }
    }

    @Test
    fun `all returned indicators have valid status`() {
        val indicators = analyzer.analyze()
        for (indicator in indicators) {
            assertTrue(
                "Indicator ${indicator.type} has invalid status: ${indicator.status}",
                indicator.status in IntegrityStatus.values()
            )
        }
    }

    @Test
    fun `all returned indicators have confidence between 0 and 1`() {
        val indicators = analyzer.analyze()
        for (indicator in indicators) {
            assertTrue(
                "Indicator ${indicator.type} has invalid confidence: ${indicator.confidence}",
                indicator.confidence in 0.0..1.0
            )
        }
    }

    @Test
    fun `all returned indicators have evidence`() {
        val indicators = analyzer.analyze()
        for (indicator in indicators) {
            assertTrue(
                "Indicator ${indicator.type} should have evidence",
                indicator.evidence.isNotEmpty()
            )
        }
    }

    @Test
    fun `no indicator should have ROOT type`() {
        val indicators = analyzer.analyze()
        for (indicator in indicators) {
            assertTrue(
                "Build integrity indicator type should not be ROOT_MANAGEMENT_APP",
                indicator.type != IntegrityIndicatorType.ROOT_MANAGEMENT_APP
            )
        }
    }

    @Test
    fun `test keys indicator has correct source when present`() {
        val indicators = analyzer.analyze()
        val testKeysIndicator = indicators.find { it.type == IntegrityIndicatorType.BUILD_TEST_KEYS }
        if (testKeysIndicator != null) {
            assertEquals("Build.TAGS", testKeysIndicator.source)
        }
    }

    @Test
    fun `userdebug indicator has correct source when present`() {
        val indicators = analyzer.analyze()
        val userdebugIndicator = indicators.find { it.type == IntegrityIndicatorType.BUILD_USERDEBUG }
        if (userdebugIndicator != null) {
            assertEquals("Build.TYPE", userdebugIndicator.source)
        }
    }

    @Test
    fun `engineering indicator has correct source when present`() {
        val indicators = analyzer.analyze()
        val engIndicator = indicators.find { it.type == IntegrityIndicatorType.BUILD_ENGINEERING_BUILD }
        if (engIndicator != null) {
            assertEquals("Build.TYPE", engIndicator.source)
        }
    }

    @Test
    fun `indicator without test-keys has NOT_DETECTED or NOT_DETERMINABLE status`() {
        val indicators = analyzer.analyze()
        val testKeysIndicator = indicators.find { it.type == IntegrityIndicatorType.BUILD_TEST_KEYS }
        if (testKeysIndicator != null && testKeysIndicator.status != IntegrityStatus.DETECTED) {
            assertTrue(
                testKeysIndicator.status in listOf(
                    IntegrityStatus.NOT_DETECTED,
                    IntegrityStatus.NOT_DETERMINABLE
                )
            )
        }
    }
}
