package com.celusado.modules.integrity.analyzers

import com.celusado.modules.integrity.models.IntegrityEvidenceItem
import com.celusado.modules.integrity.models.IntegrityEvidenceSource
import com.celusado.modules.integrity.models.IntegrityIndicator
import com.celusado.modules.integrity.models.IntegrityIndicatorType
import com.celusado.modules.integrity.models.IntegrityStatus
import com.celusado.modules.packageanalyzer.models.NormalizedPackage

class RootAppAnalyzer {

    fun analyze(packages: List<NormalizedPackage>): List<IntegrityIndicator> {
        val indicators = mutableListOf<IntegrityIndicator>()

        for (pkg in packages) {
            val rootCategory = classifyRootApp(pkg)
            if (rootCategory != null) {
                indicators.add(
                    IntegrityIndicator(
                        type = IntegrityIndicatorType.ROOT_MANAGEMENT_APP,
                        status = IntegrityStatus.DETECTED,
                        confidence = rootCategory.confidence,
                        source = "PACKAGE_MANAGER",
                        value = pkg.packageName,
                        evidence = listOf(
                            IntegrityEvidenceItem(
                                type = "ROOT_APP",
                                source = IntegrityEvidenceSource.PACKAGE_MANAGER,
                                field = pkg.packageName,
                                value = rootCategory.category,
                                description = "Root-related application detected: ${pkg.label}"
                            )
                        ),
                        limitations = listOf(
                            "Application presence does not confirm the device is currently rooted"
                        )
                    )
                )
            }
        }

        if (indicators.isEmpty()) {
            indicators.add(
                IntegrityIndicator(
                    type = IntegrityIndicatorType.ROOT_MANAGEMENT_APP,
                    status = IntegrityStatus.NOT_DETECTED,
                    confidence = 0.70,
                    source = "PACKAGE_MANAGER",
                    value = null,
                    evidence = emptyList(),
                    limitations = listOf(
                        "Root-related apps may not be visible due to package visibility restrictions"
                    )
                )
            )
        }

        return indicators
    }

    private fun classifyRootApp(pkg: NormalizedPackage): RootAppCategory? {
        val packageName = pkg.packageName.lowercase()

        for ((pattern, category) in ROOT_APP_PATTERNS) {
            if (packageName.contains(pattern)) {
                return category
            }
        }

        for (indicator in pkg.indicators) {
            val indicatorLower = indicator.lowercase()
            if (indicatorLower.contains("root") || indicatorLower.contains("su:") ||
                indicatorLower.contains("superuser") || indicatorLower.contains("magisk")
            ) {
                return RootAppCategory("ROOT_TOOL", 0.85)
            }
        }

        return null
    }

    data class RootAppCategory(val category: String, val confidence: Double)

    companion object {
        private val ROOT_APP_PATTERNS = mapOf(
            "com.topjohnwu.magisk" to RootAppCategory("MAGISK", 0.95),
            "com.koushikdutta.superuser" to RootAppCategory("SUPERUSER", 0.90),
            "com.thirdparty.superuser" to RootAppCategory("SUPERUSER", 0.90),
            "com.noshufou.android.su" to RootAppCategory("SU_MANAGER", 0.90),
            "eu.chainfire.supersu" to RootAppCategory("SUPERSU", 0.90),
            "com.devadvance.rootcloak" to RootAppCategory("ROOT_CLOAK", 0.85),
            "com.devadvance.rootcloakplus" to RootAppCategory("ROOT_CLOAK", 0.85),
            "com.saurik.substrate" to RootAppCategory("SUBSTRATE", 0.85),
            "me.phh.superuser" to RootAppCategory("SUPERUSER", 0.90),
            "com.kingroot.kinguser" to RootAppCategory("KINGROOT", 0.90),
            "com.kingo.root" to RootAppCategory("KINGO_ROOT", 0.90),
            "com.dianxinos.optimizer" to RootAppCategory("ROOT_TOOL", 0.75),
            "com_advancedtool_autoroot" to RootAppCategory("ROOT_TOOL", 0.85),
            "com.amphoras.hidemyroot" to RootAppCategory("ROOT_HIDER", 0.85),
            "com.fahrbot Plaint.root" to RootAppCategory("ROOT_TOOL", 0.80),
            "com.z4mod.xposed" to RootAppCategory("XPOSED", 0.85),
            "org.meowmu.edxposed" to RootAppCategory("EDXPOSED", 0.85),
            "com.topjohnwu.magisk.manager" to RootAppCategory("MAGISK_MANAGER", 0.95),
            "com.rxsuite.rxsu" to RootAppCategory("RXSU", 0.85),
            "com(Packet Florence) root" to RootAppCategory("ROOT_TOOL", 0.80)
        )
    }
}
