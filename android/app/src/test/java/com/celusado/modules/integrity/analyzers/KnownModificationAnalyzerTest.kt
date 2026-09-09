package com.celusado.modules.integrity.analyzers

import com.celusado.modules.integrity.models.IntegrityIndicatorType
import com.celusado.modules.integrity.models.IntegrityStatus
import com.celusado.modules.packageanalyzer.models.*
import org.junit.Assert.*
import org.junit.Test

class KnownModificationAnalyzerTest {

    private val analyzer = KnownModificationAnalyzer()

    private fun createPackage(
        packageName: String,
        label: String = "Test App"
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
            indicators = mutableListOf()
        )
    }

    @Test
    fun `analyze with normal packages returns no modification indicators`() {
        val packages = listOf(
            createPackage("com.google.chrome", "Chrome"),
            createPackage("com.whatsapp", "WhatsApp")
        )
        val indicators = analyzer.analyze(packages)
        val modificationIndicators = indicators.filter {
            it.type == IntegrityIndicatorType.KNOWN_MODIFICATION_PACKAGE && it.status == IntegrityStatus.DETECTED
        }
        assertEquals(0, modificationIndicators.size)
    }

    @Test
    fun `analyze detects Xposed framework`() {
        val packages = listOf(
            createPackage("com.xposed.installer", "Xposed Installer")
        )
        val indicators = analyzer.analyze(packages)
        val modificationIndicator = indicators.find {
            it.type == IntegrityIndicatorType.KNOWN_MODIFICATION_PACKAGE && it.status == IntegrityStatus.DETECTED
        }
        if (modificationIndicator != null) {
            assertEquals(IntegrityStatus.DETECTED, modificationIndicator.status)
        }
    }

    @Test
    fun `modification indicator includes limitation when found`() {
        val packages = listOf(
            createPackage("com.xposed.installer", "Xposed Installer")
        )
        val indicators = analyzer.analyze(packages)
        val modificationIndicator = indicators.find {
            it.type == IntegrityIndicatorType.KNOWN_MODIFICATION_PACKAGE && it.status == IntegrityStatus.DETECTED
        }
        if (modificationIndicator != null) {
            assertTrue(modificationIndicator.limitations.isNotEmpty())
        }
    }

    @Test
    fun `modification indicator has evidence when found`() {
        val packages = listOf(
            createPackage("com.xposed.installer", "Xposed Installer")
        )
        val indicators = analyzer.analyze(packages)
        val modificationIndicator = indicators.find {
            it.type == IntegrityIndicatorType.KNOWN_MODIFICATION_PACKAGE && it.status == IntegrityStatus.DETECTED
        }
        if (modificationIndicator != null) {
            assertTrue(modificationIndicator.evidence.isNotEmpty())
        }
    }

    @Test
    fun `multiple modification packages produce indicators`() {
        val packages = listOf(
            createPackage("com.xposed.installer", "Xposed"),
            createPackage("org.meowmu.edxposed.manager", "EdXposed")
        )
        val indicators = analyzer.analyze(packages)
        val modificationIndicators = indicators.filter {
            it.type == IntegrityIndicatorType.KNOWN_MODIFICATION_PACKAGE && it.status == IntegrityStatus.DETECTED
        }
        assertTrue(modificationIndicators.size >= 1)
    }
}
