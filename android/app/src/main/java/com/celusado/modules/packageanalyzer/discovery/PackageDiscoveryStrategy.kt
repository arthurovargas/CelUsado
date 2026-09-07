package com.celusado.modules.packageanalyzer.discovery

import android.content.Context
import com.celusado.modules.packageanalyzer.models.DiscoverySource

data class DiscoveredPackage(
    val packageName: String,
    val source: DiscoverySource
)

interface PackageDiscoveryStrategy {
    val source: DiscoverySource
    fun discover(context: Context): List<DiscoveredPackage>
}
