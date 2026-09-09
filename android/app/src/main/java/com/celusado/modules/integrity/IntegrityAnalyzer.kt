package com.celusado.modules.integrity

import android.content.Context
import com.celusado.modules.integrity.analyzers.BuildIntegrityAnalyzer
import com.celusado.modules.integrity.analyzers.KeyAttestationAnalyzer
import com.celusado.modules.integrity.analyzers.KnownModificationAnalyzer
import com.celusado.modules.integrity.analyzers.PlayIntegrityAnalyzer
import com.celusado.modules.integrity.analyzers.RootAppAnalyzer
import com.celusado.modules.integrity.analyzers.RootIndicatorAnalyzer
import com.celusado.modules.integrity.analyzers.BootIntegrityAnalyzer
import com.celusado.modules.integrity.analyzers.IntegrityIndicatorBuilder
import com.celusado.modules.integrity.analyzers.SecurityPatchAnalyzer
import com.celusado.modules.integrity.models.IntegrityIndicator
import com.celusado.modules.integrity.models.IntegrityResult
import com.celusado.modules.packageanalyzer.models.NormalizedPackage

class IntegrityAnalyzer(private val context: Context) {

    private val buildIntegrityAnalyzer = BuildIntegrityAnalyzer()
    private val securityPatchAnalyzer = SecurityPatchAnalyzer()
    private val rootIndicatorAnalyzer = RootIndicatorAnalyzer()
    private val rootAppAnalyzer = RootAppAnalyzer()
    private val bootIntegrityAnalyzer = BootIntegrityAnalyzer()
    private val keyAttestationAnalyzer = KeyAttestationAnalyzer()
    private val playIntegrityAnalyzer = PlayIntegrityAnalyzer()
    private val knownModificationAnalyzer = KnownModificationAnalyzer()
    private val indicatorBuilder = IntegrityIndicatorBuilder()

    fun analyze(packages: List<NormalizedPackage>): IntegrityResult {
        val allIndicators = mutableListOf<IntegrityIndicator>()
        val executedAnalyzers = mutableListOf<String>()
        val unavailableAnalyzers = mutableListOf<String>()
        val globalLimitations = mutableListOf<String>()

        runAnalyzer("BUILD_INTEGRITY") {
            allIndicators.addAll(buildIntegrityAnalyzer.analyze())
            executedAnalyzers.add("BUILD_INTEGRITY")
        }

        runAnalyzer("SECURITY_PATCH") {
            allIndicators.addAll(securityPatchAnalyzer.analyze())
            executedAnalyzers.add("SECURITY_PATCH")
        }

        runAnalyzer("ROOT_INDICATORS") {
            allIndicators.addAll(rootIndicatorAnalyzer.analyze())
            executedAnalyzers.add("ROOT_INDICATORS")
        }

        runAnalyzer("ROOT_APPS") {
            allIndicators.addAll(rootAppAnalyzer.analyze(packages))
            executedAnalyzers.add("ROOT_APPS")
        }

        runAnalyzer("BOOT_INTEGRITY") {
            allIndicators.addAll(bootIntegrityAnalyzer.analyze())
            executedAnalyzers.add("BOOT_INTEGRITY")
        }

        runAnalyzer("KEY_ATTESTATION") {
            val indicators = keyAttestationAnalyzer.analyze()
            allIndicators.addAll(indicators)
            val notSupported = indicators.any {
                it.status == com.celusado.modules.integrity.models.IntegrityStatus.NOT_SUPPORTED
            }
            if (notSupported) {
                unavailableAnalyzers.add("KEY_ATTESTATION")
            } else {
                executedAnalyzers.add("KEY_ATTESTATION")
            }
        }

        runAnalyzer("PLAY_INTEGRITY") {
            val indicators = playIntegrityAnalyzer.analyze()
            allIndicators.addAll(indicators)
            val notAvailable = indicators.any {
                it.status == com.celusado.modules.integrity.models.IntegrityStatus.NOT_AVAILABLE
            }
            if (notAvailable) {
                unavailableAnalyzers.add("PLAY_INTEGRITY")
            } else {
                executedAnalyzers.add("PLAY_INTEGRITY")
            }
        }

        runAnalyzer("KNOWN_MODIFICATIONS") {
            allIndicators.addAll(knownModificationAnalyzer.analyze(packages))
            executedAnalyzers.add("KNOWN_MODIFICATIONS")
        }

        return indicatorBuilder.build(
            allIndicators = allIndicators,
            executedAnalyzers = executedAnalyzers,
            unavailableAnalyzers = unavailableAnalyzers,
            globalLimitations = globalLimitations
        )
    }

    @Suppress("UNUSED_PARAMETER")
    private fun runAnalyzer(name: String, block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            // Analyzer failed but should not crash the whole integrity check
        }
    }
}
