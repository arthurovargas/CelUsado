package com.celusado.modules.packageanalyzer.discovery

import android.content.Context
import com.celusado.modules.packageanalyzer.models.DiscoverySource

class PackageDiscoveryManager {

    private val strategies: List<PackageDiscoveryStrategy> = listOf(
        LauncherDiscovery(),
        DeviceAdminDiscovery(),
        AccessibilityDiscovery(),
        VPNDiscovery(),
        KnownPackageDiscovery()
    )

    fun discoverAll(context: Context): Map<String, MutableSet<DiscoverySource>> {
        val packageSources = mutableMapOf<String, MutableSet<DiscoverySource>>()

        for (strategy in strategies) {
            try {
                val discovered = strategy.discover(context)
                for (item in discovered) {
                    packageSources.getOrPut(item.packageName) { mutableSetOf() }
                        .add(item.source)
                }
            } catch (e: Exception) {
                // Log and continue with next strategy
            }
        }

        return packageSources
    }
}
