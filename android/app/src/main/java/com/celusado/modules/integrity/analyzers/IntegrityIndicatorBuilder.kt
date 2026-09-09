package com.celusado.modules.integrity.analyzers

import com.celusado.modules.integrity.models.IntegrityCoverage
import com.celusado.modules.integrity.models.IntegrityIndicator
import com.celusado.modules.integrity.models.IntegrityResult
import com.celusado.modules.integrity.models.OverallIntegrityStatus

class IntegrityIndicatorBuilder {

    fun build(
        allIndicators: List<IntegrityIndicator>,
        executedAnalyzers: List<String>,
        unavailableAnalyzers: List<String>,
        globalLimitations: List<String>
    ): IntegrityResult {
        val deduplicated = deduplicateIndicators(allIndicators)
        val overallStatus = determineOverallStatus(deduplicated)

        val availableChecks = executedAnalyzers
        val unavailableChecks = unavailableAnalyzers

        val coverageStatus = when {
            unavailableChecks.isEmpty() && globalLimitations.isEmpty() -> "COMPLETE"
            unavailableChecks.size <= 2 -> "PARTIAL"
            else -> "LIMITED"
        }

        val coverage = IntegrityCoverage(
            status = coverageStatus,
            availableChecks = availableChecks,
            unavailableChecks = unavailableChecks,
            limitations = globalLimitations
        )

        return IntegrityResult(
            overallStatus = overallStatus,
            indicators = deduplicated,
            coverage = coverage,
            limitations = globalLimitations
        )
    }

    private fun deduplicateIndicators(indicators: List<IntegrityIndicator>): List<IntegrityIndicator> {
        val grouped = indicators.groupBy { "${it.type.name}:${it.source}" }
        return grouped.map { (_, group) ->
            if (group.size == 1) {
                group.first()
            } else {
                val mergedEvidence = group.flatMap { it.evidence }
                    .distinctBy { "${it.type}:${it.source}:${it.field}:${it.value}" }
                val mergedLimitations = group.flatMap { it.limitations }.distinct()
                val bestStatus = group.maxByOrNull { statusPriority(it.status) }?.status
                    ?: group.first().status
                val maxConfidence = group.maxOfOrNull { it.confidence } ?: 0.0

                IntegrityIndicator(
                    type = group.first().type,
                    status = bestStatus,
                    confidence = maxConfidence,
                    source = group.first().source,
                    value = group.first().value,
                    evidence = mergedEvidence,
                    limitations = mergedLimitations
                )
            }
        }
    }

    private fun determineOverallStatus(indicators: List<IntegrityIndicator>): OverallIntegrityStatus {
        val detectedIndicators = indicators.filter {
            it.status == com.celusado.modules.integrity.models.IntegrityStatus.DETECTED
        }

        val hasStrongEvidence = indicators.any {
            it.type == com.celusado.modules.integrity.models.IntegrityIndicatorType.VERIFIED_BOOT_STATE &&
                it.value?.lowercase() == "verified" &&
                it.status == com.celusado.modules.integrity.models.IntegrityStatus.DETECTED
        }

        if (detectedIndicators.isEmpty()) {
            return if (hasStrongEvidence) {
                OverallIntegrityStatus.STRONG_INTEGRITY_EVIDENCE
            } else {
                OverallIntegrityStatus.NORMAL
            }
        }

        if (hasStrongEvidence && detectedIndicators.size <= 1) {
            return OverallIntegrityStatus.STRONG_INTEGRITY_EVIDENCE
        }

        if (detectedIndicators.size == 1) {
            return OverallIntegrityStatus.INDICATOR
        }

        return OverallIntegrityStatus.MULTIPLE_INDICATORS
    }

    private fun statusPriority(status: com.celusado.modules.integrity.models.IntegrityStatus): Int {
        return when (status) {
            com.celusado.modules.integrity.models.IntegrityStatus.DETECTED -> 5
            com.celusado.modules.integrity.models.IntegrityStatus.NOT_DETECTED -> 4
            com.celusado.modules.integrity.models.IntegrityStatus.NOT_DETERMINABLE -> 3
            com.celusado.modules.integrity.models.IntegrityStatus.NOT_ACCESSIBLE -> 2
            com.celusado.modules.integrity.models.IntegrityStatus.NOT_AVAILABLE -> 2
            com.celusado.modules.integrity.models.IntegrityStatus.NOT_SUPPORTED -> 2
            com.celusado.modules.integrity.models.IntegrityStatus.ERROR -> 1
        }
    }
}
