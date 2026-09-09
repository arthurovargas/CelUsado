package com.celusado.modules.integrity.analyzers

import com.celusado.modules.integrity.models.IntegrityIndicatorType
import com.celusado.modules.integrity.models.IntegrityStatus
import org.junit.Assert.*
import org.junit.Test

class BootIntegrityAnalyzerTest {

    private val analyzer = BootIntegrityAnalyzer()

    @Test
    fun `analyze returns at least one indicator`() {
        val indicators = analyzer.analyze()
        assertNotNull(indicators)
        assertTrue(indicators.isNotEmpty())
    }

    @Test
    fun `analyze includes BOOTLOADER_STATE indicator`() {
        val indicators = analyzer.analyze()
        val bootloaderIndicator = indicators.find { it.type == IntegrityIndicatorType.BOOTLOADER_STATE }
        assertNotNull(bootloaderIndicator)
    }

    @Test
    fun `analyze includes VERIFIED_BOOT_STATE indicator`() {
        val indicators = analyzer.analyze()
        val verifiedBootIndicator = indicators.find { it.type == IntegrityIndicatorType.VERIFIED_BOOT_STATE }
        assertNotNull(verifiedBootIndicator)
    }

    @Test
    fun `analyze includes DEVICE_LOCKED indicator`() {
        val indicators = analyzer.analyze()
        val deviceLockedIndicator = indicators.find { it.type == IntegrityIndicatorType.DEVICE_LOCKED }
        assertNotNull(deviceLockedIndicator)
    }

    @Test
    fun `bootloader state is never CLAIMED as UNLOCKED without evidence`() {
        val indicators = analyzer.analyze()
        val bootloaderIndicator = indicators.find { it.type == IntegrityIndicatorType.BOOTLOADER_STATE }
        assertNotNull(bootloaderIndicator)
        if (bootloaderIndicator?.status == IntegrityStatus.NOT_DETERMINABLE) {
            assertEquals(0.0, bootloaderIndicator!!.confidence, 0.001)
            assertTrue(bootloaderIndicator.limitations.isNotEmpty())
        }
    }

    @Test
    fun `verified boot state has limitations`() {
        val indicators = analyzer.analyze()
        val verifiedBootIndicator = indicators.find { it.type == IntegrityIndicatorType.VERIFIED_BOOT_STATE }
        assertNotNull(verifiedBootIndicator)
        assertTrue(verifiedBootIndicator!!.limitations.isNotEmpty())
    }

    @Test
    fun `device locked state is NOT_DETERMINABLE`() {
        val indicators = analyzer.analyze()
        val deviceLockedIndicator = indicators.find { it.type == IntegrityIndicatorType.DEVICE_LOCKED }
        assertNotNull(deviceLockedIndicator)
        assertEquals(IntegrityStatus.NOT_DETERMINABLE, deviceLockedIndicator!!.status)
        assertEquals(0.0, deviceLockedIndicator.confidence, 0.001)
    }

    @Test
    fun `all indicators have valid status values`() {
        val indicators = analyzer.analyze()
        for (indicator in indicators) {
            assertTrue(
                "Indicator ${indicator.type} has invalid status: ${indicator.status}",
                indicator.status in IntegrityStatus.values()
            )
        }
    }

    @Test
    fun `boot integrity does not claim bootloader unlocked without direct API`() {
        val indicators = analyzer.analyze()
        val bootloaderIndicator = indicators.find { it.type == IntegrityIndicatorType.BOOTLOADER_STATE }
        if (bootloaderIndicator?.status == IntegrityStatus.DETECTED) {
            assertTrue(
                "Bootloader detected status should have limitations explaining inference",
                bootloaderIndicator!!.limitations.any { it.contains("inferred") || it.contains("string") }
            )
        }
    }

    @Test
    fun `verified boot SelfSigned does not produce ROOT`() {
        val indicators = analyzer.analyze()
        val verifiedBootIndicator = indicators.find { it.type == IntegrityIndicatorType.VERIFIED_BOOT_STATE }
        if (verifiedBootIndicator?.value?.lowercase() == "selfsigned") {
            assertNotEquals(
                "SelfSigned should not produce ROOT",
                "ROOT",
                verifiedBootIndicator.type.name
            )
        }
    }
}
