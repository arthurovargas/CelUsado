package com.celusado.modules.packageanalyzer.discovery

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.celusado.modules.packageanalyzer.models.DiscoverySource

class LauncherDiscovery : PackageDiscoveryStrategy {
    override val source = DiscoverySource.LAUNCHER

    override fun discover(context: Context): List<DiscoveredPackage> {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        return resolveInfos.mapNotNull { resolveInfo ->
            resolveInfo.activityInfo?.packageName?.let {
                DiscoveredPackage(it, source)
            }
        }.distinctBy { it.packageName }
    }
}
