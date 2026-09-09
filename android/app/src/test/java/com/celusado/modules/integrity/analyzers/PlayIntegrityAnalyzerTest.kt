package com.celusado.modules.integrity.analyzers

import com.celusado.modules.integrity.models.IntegrityIndicatorType
import com.celusado.modules.integrity.models.IntegrityStatus
import org.junit.Assert.*
import org.junit.Test

class PlayIntegrityAnalyzerTest {

    private val analyzer = PlayIntegrityAnalyzer()

    @Test
    fun `analyze returns at least one indicator`() {
        val indicators = analyzer.analyze()
        assertNotNull(indicators)
        assertTrue(indicators.isNotEmpty())
    }

    @Test
    fun `analyze includes PLAY_INTEGRITY indicator`() {
        val indicators = analyzer.analyze()
        val playIntegrityIndicator = indicators.find { it.type == IntegrityIndicatorType.PLAY_INTEGRITY }
        assertNotNull(playIntegrityIndicator)
    }

    @Test
    fun `play integrity is NOT_AVAILABLE without backend`() {
        val indicators = analyzer.analyze()
        val playIntegrityIndicator = indicators.find { it.type == IntegrityIndicatorType.PLAY_INTEGRITY }
        assertNotNull(playIntegrityIndicator)
        assertEquals(IntegrityStatus.NOT_AVAILABLE, playIntegrityIndicator!!.status)
    }

    @Test
    fun `play integrity has limitations about backend requirement`() {
        val indicators = analyzer.analyze()
        val playIntegrityIndicator = indicators.find { it.type == IntegrityIndicatorType.PLAY_INTEGRITY }
        assertNotNull(playIntegrityIndicator)
        assertTrue(playIntegrityIndicator!!.limitations.isNotEmpty())
        assertTrue(
            playIntegrityIndicator.limitations.any {
                it.contains("backend") || it.contains("Backend")
            }
        )
    }

    @Test
    fun `play integrity does not simulate MEETS_DEVICE_INTEGRITY`() {
        val indicators = analyzer.analyze()
        val playIntegrityIndicator = indicators.find { it.type == IntegrityIndicatorType.PLAY_INTEGRITY }
        assertNotNull(playIntegrityIndicator)
        assertNotEquals(
            "Should not claim device integrity without verification",
            IntegrityStatus.DETECTED,
            playIntegrityIndicator!!.status
        )
    }

    @Test
    fun `play integrity does not claim ROOT false`() {
        val indicators = analyzer.analyze()
        for (indicator in indicators) {
            assertNotEquals(
                "Play integrity should never claim ROOT=false",
                "ROOT_FALSE",
                indicator.type.name
            )
        }
    }

    @Test
    fun `play integrity confidence is 1_0`() {
        val indicators = analyzer.analyze()
        val playIntegrityIndicator = indicators.find { it.type == IntegrityIndicatorType.PLAY_INTEGRITY }
        assertNotNull(playIntegrityIndicator)
        assertEquals(1.0, playIntegrityIndicator!!.confidence, 0.001)
    }
}
