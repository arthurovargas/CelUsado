package com.celusado.modules.devicecontrol

import android.content.Context
import com.celusado.modules.devicecontrol.analyzers.AdministrationAnalyzer
import com.celusado.modules.devicecontrol.analyzers.ControlCapabilityAnalyzer
import com.celusado.modules.devicecontrol.analyzers.PersistenceAnalyzer
import com.celusado.modules.devicecontrol.analyzers.AdministrativeCapabilityAnalyzer
import com.celusado.modules.devicecontrol.models.ControlCoverage
import com.celusado.modules.devicecontrol.models.DeviceControlIndicator
import com.celusado.modules.devicecontrol.models.DeviceControlResult
import com.celusado.modules.packageanalyzer.models.NormalizedPackage

class DeviceControlAnalyzer(private val context: Context) {

    private val administrationAnalyzer = AdministrationAnalyzer(context)
    private val controlCapabilityAnalyzer = ControlCapabilityAnalyzer(context)
    private val persistenceAnalyzer = PersistenceAnalyzer()
    private val administrativeCapabilityAnalyzer = AdministrativeCapabilityAnalyzer()

    fun analyze(packages: List<NormalizedPackage>): DeviceControlResult {
        val allIndicators = mutableListOf<DeviceControlIndicator>()
        val analyzersExecuted = mutableListOf<String>()
        val limitations = mutableListOf<String>()

        // 1. Administration analysis (Device Admin, Device Owner, Profile Owner, DPC)
        try {
            val adminIndicators = administrationAnalyzer.analyze(packages)
            allIndicators.addAll(adminIndicators)
            analyzersExecuted.add("ADMINISTRATION")
        } catch (e: Exception) {
            limitations.add("ADMINISTRATION_ANALYSIS_FAILED:${e.message}")
        }

        // 2. Control capabilities (Accessibility, VPN, Overlay)
        try {
            val controlIndicators = controlCapabilityAnalyzer.analyze(packages)
            allIndicators.addAll(controlIndicators)
            analyzersExecuted.add("CONTROL_CAPABILITY")
        } catch (e: Exception) {
            limitations.add("CONTROL_CAPABILITY_ANALYSIS_FAILED:${e.message}")
        }

        // 3. Persistence (Boot receivers, persistent services)
        try {
            val persistenceIndicators = persistenceAnalyzer.analyze(packages)
            allIndicators.addAll(persistenceIndicators)
            analyzersExecuted.add("PERSISTENCE")
        } catch (e: Exception) {
            limitations.add("PERSISTENCE_ANALYSIS_FAILED:${e.message}")
        }

        // 4. Administrative capabilities (Lock, Wipe)
        try {
            val adminCapIndicators = administrativeCapabilityAnalyzer.analyze(packages)
            allIndicators.addAll(adminCapIndicators)
            analyzersExecuted.add("ADMINISTRATIVE_CAPABILITY")
        } catch (e: Exception) {
            limitations.add("ADMINISTRATIVE_CAPABILITY_ANALYSIS_FAILED:${e.message}")
        }

        // Deduplicate indicators by type + packageName, merging evidence
        val deduplicated = deduplicateIndicators(allIndicators)

        val coverage = ControlCoverage(
            status = when {
                limitations.isEmpty() -> "COMPLETE"
                limitations.size <= 2 -> "PARTIAL"
                else -> "LIMITED"
            },
            analyzersExecuted = analyzersExecuted,
            limitations = limitations
        )

        return DeviceControlResult(
            indicators = deduplicated,
            coverage = coverage
        )
    }

    private fun deduplicateIndicators(indicators: List<DeviceControlIndicator>): List<DeviceControlIndicator> {
        val grouped = indicators.groupBy { "${it.type.name}:${it.packageName}" }
        return grouped.map { (_, group) ->
            if (group.size == 1) {
                group.first()
            } else {
                // Merge evidence from all sources, keep highest confidence
                val mergedEvidence = group.flatMap { it.evidence }.distinctBy { "${it.type}:${it.source}:${it.value}" }
                val mergedLimitations = group.flatMap { it.limitations }.distinct()
                val bestStatus = group.maxByOrNull { statusPriority(it.status) }?.status ?: group.first().status
                val maxConfidence = group.maxOfOrNull { it.confidence } ?: 0.0

                DeviceControlIndicator(
                    type = group.first().type,
                    packageName = group.first().packageName,
                    status = bestStatus,
                    confidence = maxConfidence,
                    evidence = mergedEvidence,
                    limitations = mergedLimitations
                )
            }
        }
    }

    private fun statusPriority(status: com.celusado.modules.devicecontrol.models.DetectionStatus): Int {
        return when (status) {
            com.celusado.modules.devicecontrol.models.DetectionStatus.CONFIRMED -> 10
            com.celusado.modules.devicecontrol.models.DetectionStatus.ACTIVE -> 9
            com.celusado.modules.devicecontrol.models.DetectionStatus.DECLARED -> 7
            com.celusado.modules.devicecontrol.models.DetectionStatus.CAPABLE -> 6
            com.celusado.modules.devicecontrol.models.DetectionStatus.DETECTED -> 5
            com.celusado.modules.devicecontrol.models.DetectionStatus.NOT_DETECTED -> 2
            com.celusado.modules.devicecontrol.models.DetectionStatus.NOT_ACCESSIBLE -> 1
            com.celusado.modules.devicecontrol.models.DetectionStatus.NOT_AVAILABLE -> 1
            com.celusado.modules.devicecontrol.models.DetectionStatus.NOT_DETERMINABLE -> 0
            com.celusado.modules.devicecontrol.models.DetectionStatus.UNKNOWN -> 0
        }
    }
}
