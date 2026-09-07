package com.celusado.modules.devicecontrol.analyzers

import com.celusado.modules.devicecontrol.models.DetectionStatus
import com.celusado.modules.devicecontrol.models.DeviceControlIndicator
import com.celusado.modules.devicecontrol.models.EvidenceItem
import com.celusado.modules.devicecontrol.models.EvidenceSource
import com.celusado.modules.devicecontrol.models.IndicatorType
import com.celusado.modules.packageanalyzer.models.NormalizedPackage

class AdministrativeCapabilityAnalyzer {

    fun analyze(packages: List<NormalizedPackage>): List<DeviceControlIndicator> {
        val indicators = mutableListOf<DeviceControlIndicator>()

        for (pkg in packages) {
            analyzeLockCapability(pkg, indicators)
            analyzeWipeCapability(pkg, indicators)
        }

        return indicators
    }

    private fun analyzeLockCapability(pkg: NormalizedPackage, indicators: MutableList<DeviceControlIndicator>) {
        val evidence = mutableListOf<EvidenceItem>()

        // Check for FORCE_LOCK permission
        val hasForceLock = pkg.declaredPermissions.any {
            it.equals("android.permission.FORCE_LOCK", ignoreCase = true)
        }

        if (hasForceLock) {
            evidence.add(
                EvidenceItem(
                    type = "FORCE_LOCK_PERMISSION",
                    source = EvidenceSource.PERMISSION,
                    value = "android.permission.FORCE_LOCK",
                    description = "App declares FORCE_LOCK permission"
                )
            )
        }

        // Check for device admin related lock capabilities
        val hasDeviceAdmin = pkg.declaredPermissions.any {
            it.equals("android.permission.BIND_DEVICE_ADMIN", ignoreCase = true)
        }

        if (hasDeviceAdmin && hasForceLock) {
            evidence.add(
                EvidenceItem(
                    type = "ADMIN_LOCK_CAPABILITY",
                    source = EvidenceSource.INDIRECT_DETECTION,
                    value = "DEVICE_ADMIN_WITH_FORCE_LOCK",
                    description = "App has both device admin and force lock permissions"
                )
            )
        }

        if (evidence.isNotEmpty()) {
            indicators.add(
                DeviceControlIndicator(
                    type = IndicatorType.FORCE_LOCK,
                    packageName = pkg.packageName,
                    status = DetectionStatus.CAPABLE,
                    confidence = if (hasDeviceAdmin) 0.85 else 0.6,
                    evidence = evidence,
                    limitations = listOf("LOCK_CAPABILITY_NOT_CONFIRMED")
                )
            )
        }
    }

    private fun analyzeWipeCapability(pkg: NormalizedPackage, indicators: MutableList<DeviceControlIndicator>) {
        val evidence = mutableListOf<EvidenceItem>()

        // Check for wipe/reset related permissions
        val wipePermissions = listOf(
            "android.permission.MASTER_CLEAR",
            "android.permission.DELETE_PACKAGES",
            "android.permission.FACTORY_RESET",
            "android.permission.MODIFY_PHONE_STATE"
        )

        val declaredWipePermissions = pkg.declaredPermissions.filter { perm ->
            wipePermissions.any { it.equals(perm, ignoreCase = true) }
        }

        for (perm in declaredWipePermissions) {
            evidence.add(
                EvidenceItem(
                    type = "WIPE_PERMISSION",
                    source = EvidenceSource.PERMISSION,
                    value = perm,
                    description = "App declares wipe/reset related permission: $perm"
                )
            )
        }

        // Check for wipe-related component names
        val hasWipeService = pkg.services.any { svc ->
            val name = svc.name.lowercase()
            name.contains("wipe") ||
            name.contains("reset") ||
            name.contains("erase") ||
            name.contains("factory")
        }

        val hasWipeReceiver = pkg.receivers.any { rcv ->
            val name = rcv.name.lowercase()
            name.contains("wipe") ||
            name.contains("reset") ||
            name.contains("erase") ||
            name.contains("factory")
        }

        if (hasWipeService || hasWipeReceiver) {
            evidence.add(
                EvidenceItem(
                    type = "WIPE_COMPONENT",
                    source = EvidenceSource.PACKAGE_COMPONENT,
                    value = "WIPE_RELATED_COMPONENT",
                    description = "App has component with wipe-related name pattern"
                )
            )
        }

        if (evidence.isNotEmpty()) {
            indicators.add(
                DeviceControlIndicator(
                    type = IndicatorType.WIPE_CAPABILITY,
                    packageName = pkg.packageName,
                    status = DetectionStatus.CAPABLE,
                    confidence = if (declaredWipePermissions.isNotEmpty()) 0.8 else 0.5,
                    evidence = evidence,
                    limitations = listOf("WIPE_CAPABILITY_NOT_CONFIRMED")
                )
            )
        }
    }
}
