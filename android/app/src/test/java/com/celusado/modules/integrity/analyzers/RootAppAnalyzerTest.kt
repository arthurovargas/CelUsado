package com.celusado.modules.integrity.analyzers

import com.celusado.modules.integrity.models.IntegrityIndicatorType
import com.celusado.modules.integrity.models.IntegrityStatus
import com.celusado.modules.packageanalyzer.models.*
import org.junit.Assert.*
import org.junit.Test

class RootAppAnalyzerTest {

    private val analyzer = RootAppAnalyzer()

    private fun createPackage(
        packageName: String,
        label: String = "Test App",
        indicators: List<String> = emptyList()
    ): NormalizedPackage {
        return NormalizedPackage(
            packageName = packageName,
            label = label,
            version = PackageVersion(name = "1.0", code = 1),
            flags = PackageFlags(system = false, updatedSystem = false, debuggable = false),
            discoverySources = mutableSetOf(DiscoverySource.LAUNCHER),
            visibility = PackageVisibility.DETECTED,
            installerPackageName = null,
            firstInstallTime = 0L,
            lastUpdateTime = 0L,
            classification = ClassificationResult(category = AppClassification.USER, confidence = 0.7, evidence = emptyList()),
            declaredPermissions = mutableListOf(),
            services = mutableListOf(),
            receivers = mutableListOf(),
            activities = mutableListOf(),
            providers = mutableListOf(),
            signing = null,
            indicators = indicators.toMutableList()
        )
    }

    @Test
    fun `analyze with no root apps returns NOT_DETECTED`() {
        val packages = listOf(
            createPackage("com.example.normalapp", "Normal App")
        )
        val indicators = analyzer.analyze(packages)
        val rootAppIndicator = indicators.find { it.type == IntegrityIndicatorType.ROOT_MANAGEMENT_APP }
        assertNotNull(rootAppIndicator)
        assertEquals(IntegrityStatus.NOT_DETECTED, rootAppIndicator!!.status)
    }

    @Test
    fun `analyze detects Magisk`() {
        val packages = listOf(
            createPackage("com.topjohnwu.magisk", "Magisk")
        )
        val indicators = analyzer.analyze(packages)
        val rootAppIndicator = indicators.find { it.type == IntegrityIndicatorType.ROOT_MANAGEMENT_APP }
        assertNotNull(rootAppIndicator)
        assertEquals(IntegrityStatus.DETECTED, rootAppIndicator!!.status)
        assertEquals("com.topjohnwu.magisk", rootAppIndicator!!.value)
        assertTrue(rootAppIndicator!!.confidence >= 0.9)
    }

    @Test
    fun `analyze detects SuperSU`() {
        val packages = listOf(
            createPackage("eu.chainfire.supersu", "SuperSU")
        )
        val indicators = analyzer.analyze(packages)
        val rootAppIndicator = indicators.find { it.type == IntegrityIndicatorType.ROOT_MANAGEMENT_APP }
        assertNotNull(rootAppIndicator)
        assertEquals(IntegrityStatus.DETECTED, rootAppIndicator!!.status)
    }

    @Test
    fun `analyze detects KingRoot`() {
        val packages = listOf(
            createPackage("com.kingroot.kinguser", "KingRoot")
        )
        val indicators = analyzer.analyze(packages)
        val rootAppIndicator = indicators.find { it.type == IntegrityIndicatorType.ROOT_MANAGEMENT_APP }
        assertNotNull(rootAppIndicator)
        assertEquals(IntegrityStatus.DETECTED, rootAppIndicator!!.status)
    }

    @Test
    fun `root app indicator includes limitation about not confirming root`() {
        val packages = listOf(
            createPackage("com.topjohnwu.magisk", "Magisk")
        )
        val indicators = analyzer.analyze(packages)
        val rootAppIndicator = indicators.find { it.type == IntegrityIndicatorType.ROOT_MANAGEMENT_APP }
        assertNotNull(rootAppIndicator)
        assertTrue(
            rootAppIndicator!!.limitations.any {
                it.contains("does not confirm") || it.contains("not confirm")
            }
        )
    }

    @Test
    fun `root app indicator has evidence`() {
        val packages = listOf(
            createPackage("com.topjohnwu.magisk", "Magisk")
        )
        val indicators = analyzer.analyze(packages)
        val rootAppIndicator = indicators.find { it.type == IntegrityIndicatorType.ROOT_MANAGEMENT_APP }
        assertNotNull(rootAppIndicator)
        assertTrue(rootAppIndicator!!.evidence.isNotEmpty())
    }

    @Test
    fun `multiple root apps produce multiple indicators`() {
        val packages = listOf(
            createPackage("com.topjohnwu.magisk", "Magisk"),
            createPackage("eu.chainfire.supersu", "SuperSU"),
            createPackage("com.kingroot.kinguser", "KingRoot")
        )
        val indicators = analyzer.analyze(packages)
        val rootAppIndicators = indicators.filter { it.type == IntegrityIndicatorType.ROOT_MANAGEMENT_APP && it.status == IntegrityStatus.DETECTED }
        assertEquals(3, rootAppIndicators.size)
    }

    @Test
    fun `normal apps are not flagged as root`() {
        val packages = listOf(
            createPackage("com.google.chrome", "Chrome"),
            createPackage("com.whatsapp", "WhatsApp"),
            createPackage("com.instagram.android", "Instagram")
        )
        val indicators = analyzer.analyze(packages)
        val detectedRootApps = indicators.filter {
            it.type == IntegrityIndicatorType.ROOT_MANAGEMENT_APP && it.status == IntegrityStatus.DETECTED
        }
        assertEquals(0, detectedRootApps.size)
    }

    @Test
    fun `root app does not claim ROOT_CONFIRMED`() {
        val packages = listOf(
            createPackage("com.topjohnwu.magisk", "Magisk")
        )
        val indicators = analyzer.analyze(packages)
        for (indicator in indicators) {
            assertNotEquals(
                "Should never claim ROOT_CONFIRMED",
                "ROOT_CONFIRMED",
                indicator.type.name
            )
        }
    }

    @Test
    fun `indicators list includes limitation about package visibility`() {
        val packages = emptyList<NormalizedPackage>()
        val indicators = analyzer.analyze(packages)
        val notDetectedIndicator = indicators.find { it.status == IntegrityStatus.NOT_DETECTED }
        assertNotNull(notDetectedIndicator)
        assertTrue(
            notDetectedIndicator!!.limitations.any {
                it.contains("visibility") || it.contains("visible")
            }
        )
    }
}
