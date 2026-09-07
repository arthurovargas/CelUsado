package com.celusado.modules.packageanalyzer.discovery

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.celusado.modules.packageanalyzer.models.DiscoverySource

class AccessibilityDiscovery : PackageDiscoveryStrategy {
    override val source = DiscoverySource.ACCESSIBILITY

    override fun discover(context: Context): List<DiscoveredPackage> {
        val intent = Intent("android.settings.ACCESSIBILITY_SETTINGS")
        val resolveInfos = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        return resolveInfos.mapNotNull { resolveInfo ->
            resolveInfo.activityInfo?.packageName?.let {
                DiscoveredPackage(it, source)
            }
        }.distinctBy { it.packageName }
    }
}
