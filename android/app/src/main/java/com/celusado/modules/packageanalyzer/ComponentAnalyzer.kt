package com.celusado.modules.packageanalyzer

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.celusado.modules.packageanalyzer.models.NormalizedPackage
import com.celusado.modules.packageanalyzer.models.ServiceInfo
import com.celusado.modules.packageanalyzer.models.ReceiverInfo
import com.celusado.modules.packageanalyzer.models.ActivityInfo
import com.celusado.modules.packageanalyzer.models.ProviderInfo

class ComponentAnalyzer(private val context: Context) {

    fun analyze(packageInfo: PackageInfo, normalized: NormalizedPackage) {
        analyzeServices(packageInfo, normalized)
        analyzeReceivers(packageInfo, normalized)
        analyzeActivities(packageInfo, normalized)
        analyzeProviders(packageInfo, normalized)
    }

    private fun analyzeServices(packageInfo: PackageInfo, normalized: NormalizedPackage) {
        val services = packageInfo.services
        if (services != null) {
            for (service in services) {
                normalized.services.add(
                    ServiceInfo(
                        name = service.name ?: "unknown",
                        permission = service.permission,
                        foregroundServiceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            service.foregroundServiceType?.toString()
                        } else null,
                        isExported = service.exported
                    )
                )

                // Detect exported services
                if (service.exported) {
                    normalized.indicators.add("EXPORTED_SERVICE:${service.name}")
                }

                // Detect services with device admin binding
                if (service.permission == "android.permission.BIND_DEVICE_ADMIN") {
                    normalized.indicators.add("DEVICE_ADMIN_SERVICE:${service.name}")
                }

                // Detect accessibility services
                if (service.permission == "android.permission.BIND_ACCESSIBILITY_SERVICE") {
                    normalized.indicators.add("ACCESSIBILITY_SERVICE:${service.name}")
                }

                // Detect VPN services
                if (service.permission == "android.permission.BIND_VPN_SERVICE") {
                    normalized.indicators.add("VPN_SERVICE:${service.name}")
                }
            }
        }
    }

    private fun analyzeReceivers(packageInfo: PackageInfo, normalized: NormalizedPackage) {
        val receivers = packageInfo.receivers
        if (receivers != null) {
            for (receiver in receivers) {
                normalized.receivers.add(
                    ReceiverInfo(
                        name = receiver.name ?: "unknown",
                        permission = receiver.permission,
                        isExported = receiver.exported
                    )
                )

                // Detect exported receivers
                if (receiver.exported) {
                    normalized.indicators.add("EXPORTED_RECEIVER:${receiver.name}")
                }

                // Detect boot completed receivers
                val receiverName = receiver.name?.lowercase() ?: ""
                if (receiverName.contains("boot") || receiverName.contains("startup")) {
                    normalized.indicators.add("BOOT_RECEIVER:${receiver.name}")
                }
            }
        }
    }

    private fun analyzeActivities(packageInfo: PackageInfo, normalized: NormalizedPackage) {
        val activities = packageInfo.activities
        if (activities != null) {
            for (activity in activities) {
                normalized.activities.add(
                    ActivityInfo(
                        name = activity.name ?: "unknown",
                        permission = activity.permission,
                        isExported = activity.exported
                    )
                )
            }
        }
    }

    private fun analyzeProviders(packageInfo: PackageInfo, normalized: NormalizedPackage) {
        val providers = packageInfo.providers
        if (providers != null) {
            for (provider in providers) {
                normalized.providers.add(
                    ProviderInfo(
                        name = provider.name ?: "unknown",
                        authority = provider.authority,
                        isExported = provider.exported,
                        readPermission = provider.readPermission,
                        writePermission = provider.writePermission
                    )
                )
            }
        }
    }
}
