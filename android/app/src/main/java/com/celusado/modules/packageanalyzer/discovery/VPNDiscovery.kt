package com.celusado.modules.packageanalyzer.discovery

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.celusado.modules.packageanalyzer.models.DiscoverySource

class VPNDiscovery : PackageDiscoveryStrategy {
    override val source = DiscoverySource.VPN

    override fun discover(context: Context): List<DiscoveredPackage> {
        val intent = Intent("android.net.vpn.SETTINGS")
        val resolveInfos = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        return resolveInfos.mapNotNull { resolveInfo ->
            resolveInfo.activityInfo?.packageName?.let {
                DiscoveredPackage(it, source)
            }
        }.distinctBy { it.packageName }
    }
}
