package com.celusado.modules.packageanalyzer.discovery

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.celusado.modules.packageanalyzer.models.DiscoverySource

class DeviceAdminDiscovery : PackageDiscoveryStrategy {
    override val source = DiscoverySource.DEVICE_ADMIN

    override fun discover(context: Context): List<DiscoveredPackage> {
        val intent = Intent("android.app.action.DEVICE_ADMIN_SETTINGS")
        val resolveInfos = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        return resolveInfos.mapNotNull { resolveInfo ->
            resolveInfo.activityInfo?.packageName?.let {
                DiscoveredPackage(it, source)
            }
        }.distinctBy { it.packageName }
    }
}
