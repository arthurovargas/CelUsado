package com.celusado.modules.devicecontrol.analyzers

import android.content.Context
import com.celusado.modules.devicecontrol.models.DetectionStatus
import com.celusado.modules.devicecontrol.models.DeviceControlIndicator
import com.celusado.modules.devicecontrol.models.EvidenceItem
import com.celusado.modules.devicecontrol.models.EvidenceSource
import com.celusado.modules.devicecontrol.models.IndicatorType
import com.celusado.modules.packageanalyzer.models.NormalizedPackage

class AdministrationAnalyzer(private val context: Context) {

    fun analyze(packages: List<NormalizedPackage>): List<DeviceControlIndicator> {
        val indicators = mutableListOf<DeviceControlIndicator>()

        for (pkg in packages) {
            // Device Admin detection
            analyzeDeviceAdmin(pkg, indicators)

            // Device Owner detection (limited without active admin)
            analyzeDeviceOwner(pkg, indicators)

            // Profile Owner detection (limited without active admin)
            analyzeProfileOwner(pkg, indicators)

            // Device Policy Controller detection
            analyzeDPC(pkg, indicators)
        }

        return indicators
    }

    private fun analyzeDeviceAdmin(pkg: NormalizedPackage, indicators: MutableList<DeviceControlIndicator>) {
        val evidence = mutableListOf<EvidenceItem>()

        // Check for DeviceAdminReceiver in receivers
        val hasDeviceAdminReceiver = pkg.receivers.any { receiver ->
            receiver.name.lowercase().contains("admin") ||
            receiver.permission?.lowercase()?.contains("bind_device_admin") == true
        }

        if (hasDeviceAdminReceiver) {
            val adminReceivers = pkg.receivers.filter {
                it.name.lowercase().contains("admin") ||
                it.permission?.lowercase()?.contains("bind_device_admin") == true
            }
            for (rcv in adminReceivers) {
                evidence.add(
                    EvidenceItem(
                        type = "DEVICE_ADMIN_RECEIVER",
                        source = EvidenceSource.PACKAGE_COMPONENT,
                        value = rcv.name,
                        description = "Receiver with device admin capability"
                    )
                )
            }
        }

        // Check for BIND_DEVICE_ADMIN permission
        val hasBindDeviceAdmin = pkg.declaredPermissions.any {
            it.equals("android.permission.BIND_DEVICE_ADMIN", ignoreCase = true)
        }

        if (hasBindDeviceAdmin) {
            evidence.add(
                EvidenceItem(
                    type = "BIND_DEVICE_ADMIN_PERMISSION",
                    source = EvidenceSource.PERMISSION,
                    value = "android.permission.BIND_DEVICE_ADMIN",
                    description = "App declares BIND_DEVICE_ADMIN permission"
                )
            )
        }

        // Check for MANAGE_DEVICE_ADMINS permission
        val hasManageDeviceAdmins = pkg.declaredPermissions.any {
            it.equals("android.permission.MANAGE_DEVICE_ADMINS", ignoreCase = true)
        }

        if (hasManageDeviceAdmins) {
            evidence.add(
                EvidenceItem(
                    type = "MANAGE_DEVICE_ADMINS_PERMISSION",
                    source = EvidenceSource.PERMISSION,
                    value = "android.permission.MANAGE_DEVICE_ADMINS",
                    description = "App declares MANAGE_DEVICE_ADMINS permission"
                )
            )
        }

        if (evidence.isNotEmpty()) {
            indicators.add(
                DeviceControlIndicator(
                    type = IndicatorType.DEVICE_ADMIN,
                    packageName = pkg.packageName,
                    status = DetectionStatus.DECLARED,
                    confidence = if (hasDeviceAdminReceiver) 0.95 else 0.7,
                    evidence = evidence,
                    limitations = listOf("ACTIVE_STATE_NOT_ACCESSIBLE")
                )
            )
        }
    }

    private fun analyzeDeviceOwner(pkg: NormalizedPackage, indicators: MutableList<DeviceControlIndicator>) {
        // DeviceOwner cannot be confirmed without DevicePolicyManager.isDeviceOwnerApp()
        // which requires our app to be a device admin itself
        // We can only detect indirect evidence

        val evidence = mutableListOf<EvidenceItem>()

        // Check if package is in known DPC list
        val knownDpcPackages = listOf(
            "com.google.android.apps.work.oobconfig",
            "com.google.android.apps.work.clc",
            "com.samsung.android.knox",
            "com.microsoft.windowsintune.companyportal",
            "com.vmware.workspaceone"
        )

        if (pkg.packageName in knownDpcPackages) {
            evidence.add(
                EvidenceItem(
                    type = "KNOWN_DPC_PACKAGE",
                    source = EvidenceSource.KNOWN_PACKAGE,
                    value = pkg.packageName,
                    description = "Package is known Device Policy Controller"
                )
            )
        }

        if (evidence.isNotEmpty()) {
            indicators.add(
                DeviceControlIndicator(
                    type = IndicatorType.DEVICE_OWNER,
                    packageName = pkg.packageName,
                    status = DetectionStatus.NOT_DETERMINABLE,
                    confidence = 0.3,
                    evidence = evidence,
                    limitations = listOf("OWNER_STATE_NOT_ACCESSIBLE", "REQUIRES_DEVICE_ADMIN_ROLE")
                )
            )
        }
    }

    private fun analyzeProfileOwner(pkg: NormalizedPackage, indicators: MutableList<DeviceControlIndicator>) {
        // Profile Owner state is not accessible without DevicePolicyManager
        // Only indirect evidence can be collected

        // If package has admin capabilities and is not a known DPC, it might be a profile owner
        val hasAdminIndicators = pkg.declaredPermissions.any {
            it.equals("android.permission.BIND_DEVICE_ADMIN", ignoreCase = true)
        } || pkg.receivers.any { rcv ->
            rcv.permission?.lowercase()?.contains("bind_device_admin") == true
        }

        if (hasAdminIndicators) {
            indicators.add(
                DeviceControlIndicator(
                    type = IndicatorType.PROFILE_OWNER,
                    packageName = pkg.packageName,
                    status = DetectionStatus.NOT_DETERMINABLE,
                    confidence = 0.2,
                    evidence = listOf(
                        EvidenceItem(
                            type = "INDIRECT_EVIDENCE",
                            source = EvidenceSource.INDIRECT_DETECTION,
                            value = "ADMIN_COMPONENTS_PRESENT",
                            description = "App has admin components but profile owner state unknown"
                        )
                    ),
                    limitations = listOf("PROFILE_OWNER_STATE_NOT_ACCESSIBLE")
                )
            )
        }
    }

    private fun analyzeDPC(pkg: NormalizedPackage, indicators: MutableList<DeviceControlIndicator>) {
        // DPC detection combines admin evidence + known package + owner evidence
        // This is handled in the unified analysis, but we check for DPC-specific patterns

        val hasAdminReceiver = pkg.receivers.any { rcv ->
            rcv.permission?.lowercase()?.contains("bind_device_admin") == true
        }

        val hasKnownDpc = pkg.packageName in listOf(
            "com.google.android.apps.work.oobconfig",
            "com.google.android.apps.work.clc",
            "com.samsung.android.knox",
            "com.microsoft.windowsintune.companyportal",
            "com.vmware.workspaceone",
            "com.mobileiron",
            "com.lookout",
            "com.zimperium"
        )

        if (hasAdminReceiver || hasKnownDpc) {
            indicators.add(
                DeviceControlIndicator(
                    type = IndicatorType.DEVICE_POLICY_CONTROLLER,
                    packageName = pkg.packageName,
                    status = if (hasKnownDpc) DetectionStatus.DETECTED else DetectionStatus.DECLARED,
                    confidence = if (hasKnownDpc) 0.8 else 0.6,
                    evidence = buildList {
                        if (hasAdminReceiver) {
                            add(EvidenceItem("ADMIN_RECEIVER", EvidenceSource.PACKAGE_COMPONENT, "Device admin receiver found"))
                        }
                        if (hasKnownDpc) {
                            add(EvidenceItem("KNOWN_DPC_PACKAGE", EvidenceSource.KNOWN_PACKAGE, pkg.packageName))
                        }
                    },
                    limitations = if (!hasKnownDpc) listOf("DPC_IDENTITY_NOT_CONFIRMED") else emptyList()
                )
            )
        }
    }
}
