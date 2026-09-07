package com.celusado.modules.devicecontrol.analyzers

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.celusado.modules.devicecontrol.models.DetectionStatus
import com.celusado.modules.devicecontrol.models.DeviceControlIndicator
import com.celusado.modules.devicecontrol.models.EvidenceItem
import com.celusado.modules.devicecontrol.models.EvidenceSource
import com.celusado.modules.devicecontrol.models.IndicatorType
import com.celusado.modules.packageanalyzer.models.NormalizedPackage

class ControlCapabilityAnalyzer(private val context: Context) {

    fun analyze(packages: List<NormalizedPackage>): List<DeviceControlIndicator> {
        val indicators = mutableListOf<DeviceControlIndicator>()

        for (pkg in packages) {
            analyzeAccessibility(pkg, indicators)
            analyzeVPN(pkg, indicators)
            analyzeOverlay(pkg, indicators)
        }

        return indicators
    }

    private fun analyzeAccessibility(pkg: NormalizedPackage, indicators: MutableList<DeviceControlIndicator>) {
        val evidence = mutableListOf<EvidenceItem>()
        var hasAccessibilityService = false

        // Check for accessibility service declaration in services
        for (service in pkg.services) {
            if (service.permission?.equals("android.permission.BIND_ACCESSIBILITY_SERVICE", ignoreCase = true) == true) {
                hasAccessibilityService = true
                evidence.add(
                    EvidenceItem(
                        type = "ACCESSIBILITY_SERVICE_DECLARED",
                        source = EvidenceSource.PACKAGE_COMPONENT,
                        value = service.name,
                        description = "Service with BIND_ACCESSIBILITY_SERVICE permission"
                    )
                )
            }
        }

        // Check for accessibility permission
        val hasAccessibilityPermission = pkg.declaredPermissions.any {
            it.equals("android.permission.BIND_ACCESSIBILITY_SERVICE", ignoreCase = true)
        }

        if (hasAccessibilityPermission && !hasAccessibilityService) {
            hasAccessibilityService = true
            evidence.add(
                EvidenceItem(
                    type = "ACCESSIBILITY_PERMISSION_DECLARED",
                    source = EvidenceSource.PERMISSION,
                    value = "android.permission.BIND_ACCESSIBILITY_SERVICE",
                    description = "App declares BIND_ACCESSIBILITY_SERVICE permission"
                )
            )
        }

        if (hasAccessibilityService) {
            // Try to check if accessibility service is actually enabled
            val isActive = checkAccessibilityServiceActive(pkg.packageName)

            indicators.add(
                DeviceControlIndicator(
                    type = IndicatorType.ACCESSIBILITY_SERVICE,
                    packageName = pkg.packageName,
                    status = if (isActive == true) DetectionStatus.ACTIVE else DetectionStatus.DECLARED,
                    confidence = if (isActive == true) 1.0 else 0.9,
                    evidence = evidence,
                    limitations = if (isActive == null) listOf("ACCESSIBILITY_STATE_NOT_ACCESSIBLE") else emptyList()
                )
            )
        }
    }

    private fun checkAccessibilityServiceActive(packageName: String): Boolean? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val enabledServices = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                ) ?: return null

                enabledServices.split(":").any { service ->
                    service.startsWith(packageName)
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun analyzeVPN(pkg: NormalizedPackage, indicators: MutableList<DeviceControlIndicator>) {
        val evidence = mutableListOf<EvidenceItem>()
        var hasVpnService = false

        // Check for VPN service
        for (service in pkg.services) {
            if (service.permission?.equals("android.permission.BIND_VPN_SERVICE", ignoreCase = true) == true) {
                hasVpnService = true
                evidence.add(
                    EvidenceItem(
                        type = "VPN_SERVICE_DECLARED",
                        source = EvidenceSource.PACKAGE_COMPONENT,
                        value = service.name,
                        description = "Service with BIND_VPN_SERVICE permission"
                    )
                )
            }
        }

        // Check for VPN permission
        val hasVpnPermission = pkg.declaredPermissions.any {
            it.equals("android.permission.BIND_VPN_SERVICE", ignoreCase = true)
        }

        if (hasVpnPermission && !hasVpnService) {
            hasVpnService = true
            evidence.add(
                EvidenceItem(
                    type = "VPN_PERMISSION_DECLARED",
                    source = EvidenceSource.PERMISSION,
                    value = "android.permission.BIND_VPN_SERVICE",
                    description = "App declares BIND_VPN_SERVICE permission"
                )
            )
        }

        if (hasVpnService) {
            indicators.add(
                DeviceControlIndicator(
                    type = IndicatorType.VPN_SERVICE,
                    packageName = pkg.packageName,
                    status = DetectionStatus.DECLARED,
                    confidence = 0.85,
                    evidence = evidence,
                    limitations = listOf("VPN_ACTIVE_STATE_NOT_ACCESSIBLE")
                )
            )
        }
    }

    private fun analyzeOverlay(pkg: NormalizedPackage, indicators: MutableList<DeviceControlIndicator>) {
        val hasOverlayPermission = pkg.declaredPermissions.any {
            it.equals("android.permission.SYSTEM_ALERT_WINDOW", ignoreCase = true)
        }

        if (hasOverlayPermission) {
            val isGranted = checkOverlayGranted(pkg.packageName)

            indicators.add(
                DeviceControlIndicator(
                    type = IndicatorType.OVERLAY,
                    packageName = pkg.packageName,
                    status = when (isGranted) {
                        true -> DetectionStatus.ACTIVE
                        false -> DetectionStatus.DECLARED
                        null -> DetectionStatus.NOT_DETERMINABLE
                    },
                    confidence = when (isGranted) {
                        true -> 1.0
                        false -> 0.9
                        null -> 0.5
                    },
                    evidence = listOf(
                        EvidenceItem(
                            type = "SYSTEM_ALERT_WINDOW_PERMISSION",
                            source = EvidenceSource.PERMISSION,
                            value = "android.permission.SYSTEM_ALERT_WINDOW",
                            description = "App declares SYSTEM_ALERT_WINDOW permission"
                        )
                    ),
                    limitations = if (isGranted == null) listOf("OVERLAY_STATE_NOT_ACCESSIBLE") else emptyList()
                )
            )
        }
    }

    private fun checkOverlayGranted(packageName: String): Boolean? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Settings.canDrawOverlays requires a context with the package
                // We can't directly check for other packages without special permissions
                // This will be NOT_DETERMINABLE for most cases
                null
            } else {
                // Before API 23, all apps could draw overlays
                true
            }
        } catch (e: Exception) {
            null
        }
    }
}
