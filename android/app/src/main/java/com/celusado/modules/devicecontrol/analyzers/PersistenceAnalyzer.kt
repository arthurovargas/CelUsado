package com.celusado.modules.devicecontrol.analyzers

import com.celusado.modules.devicecontrol.models.DetectionStatus
import com.celusado.modules.devicecontrol.models.DeviceControlIndicator
import com.celusado.modules.devicecontrol.models.EvidenceItem
import com.celusado.modules.devicecontrol.models.EvidenceSource
import com.celusado.modules.devicecontrol.models.IndicatorType
import com.celusado.modules.packageanalyzer.models.NormalizedPackage

class PersistenceAnalyzer {

    fun analyze(packages: List<NormalizedPackage>): List<DeviceControlIndicator> {
        val indicators = mutableListOf<DeviceControlIndicator>()

        for (pkg in packages) {
            analyzeBootReceivers(pkg, indicators)
            analyzePersistentServices(pkg, indicators)
        }

        return indicators
    }

    private fun analyzeBootReceivers(pkg: NormalizedPackage, indicators: MutableList<DeviceControlIndicator>) {
        val evidence = mutableListOf<EvidenceItem>()

        // Check receivers for boot-related intent filters (via name pattern)
        val bootReceivers = pkg.receivers.filter { receiver ->
            val name = receiver.name.lowercase()
            name.contains("boot") ||
            name.contains("startup") ||
            name.contains("launch") ||
            name.contains("init")
        }

        for (rcv in bootReceivers) {
            evidence.add(
                EvidenceItem(
                    type = "BOOT_RECEIVER_DECLARED",
                    source = EvidenceSource.PACKAGE_COMPONENT,
                    value = rcv.name,
                    description = "Receiver with boot-related name pattern"
                )
            )
        }

        // Also check for common boot receiver patterns
        val hasBootCompletedReceiver = pkg.receivers.any { rcv ->
            rcv.name.lowercase().contains("bootcompleted") ||
            rcv.name.lowercase().contains("boot_completed") ||
            rcv.name.lowercase().contains("onboot")
        }

        if (hasBootCompletedReceiver) {
            evidence.add(
                EvidenceItem(
                    type = "BOOT_COMPLETED_PATTERN",
                    source = EvidenceSource.PACKAGE_COMPONENT,
                    value = "BOOT_COMPLETED_RECEIVER",
                    description = "Receiver matches BOOT_COMPLETED pattern"
                )
            )
        }

        if (evidence.isNotEmpty()) {
            indicators.add(
                DeviceControlIndicator(
                    type = IndicatorType.BOOT_RECEIVER,
                    packageName = pkg.packageName,
                    status = DetectionStatus.DECLARED,
                    confidence = 0.9,
                    evidence = evidence,
                    limitations = listOf("BOOT_EXECUTION_NOT_CONFIRMED")
                )
            )
        }
    }

    private fun analyzePersistentServices(pkg: NormalizedPackage, indicators: MutableList<DeviceControlIndicator>) {
        val evidence = mutableListOf<EvidenceItem>()

        // Check for foreground services
        val foregroundServices = pkg.services.filter { svc ->
            svc.foregroundServiceType != null
        }

        for (svc in foregroundServices) {
            evidence.add(
                EvidenceItem(
                    type = "FOREGROUND_SERVICE_DECLARED",
                    source = EvidenceSource.PACKAGE_COMPONENT,
                    value = svc.name,
                    description = "Foreground service with type: ${svc.foregroundServiceType}"
                )
            )
        }

        // Check for services with persistent-like names
        val persistentServices = pkg.services.filter { svc ->
            val name = svc.name.lowercase()
            name.contains("persistent") ||
            name.contains("daemon") ||
            name.contains("background") ||
            name.contains("always")
        }

        for (svc in persistentServices) {
            evidence.add(
                EvidenceItem(
                    type = "PERSISTENT_SERVICE_PATTERN",
                    source = EvidenceSource.PACKAGE_COMPONENT,
                    value = svc.name,
                    description = "Service with persistent-like name pattern"
                )
            )
        }

        // Check for services that are exported (potential external control)
        val exportedServices = pkg.services.filter { it.isExported }

        for (svc in exportedServices) {
            evidence.add(
                EvidenceItem(
                    type = "EXPORTED_SERVICE",
                    source = EvidenceSource.PACKAGE_COMPONENT,
                    value = svc.name,
                    description = "Exported service that can be invoked externally"
                )
            )
        }

        if (evidence.isNotEmpty()) {
            indicators.add(
                DeviceControlIndicator(
                    type = IndicatorType.PERSISTENT_SERVICE,
                    packageName = pkg.packageName,
                    status = DetectionStatus.DECLARED,
                    confidence = 0.75,
                    evidence = evidence,
                    limitations = listOf("RUNTIME_STATE_NOT_CONFIRMED")
                )
            )
        }
    }
}
