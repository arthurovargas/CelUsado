package com.celusado.modules.packageanalyzer.discovery

import android.content.Context
import android.content.pm.PackageManager
import com.celusado.modules.packageanalyzer.models.DiscoverySource

class KnownPackageDiscovery : PackageDiscoveryStrategy {
    override val source = DiscoverySource.KNOWN_PACKAGE

    private val knownPackages = listOf(
        // Samsung Knox
        "com.samsung.android.knox",
        "com.samsung.android.knox.ddar",
        "com.samsung.android.knox.containermanager",
        "com.samsung.android.app.routines",
        "com.sec.android.app.sbrowser",
        // Google Android Enterprise
        "com.google.android.apps.work.oobconfig",
        "com.google.android.apps.work.clc",
        // Microsoft Intune
        "com.microsoft.windowsintune.companyportal",
        // VMware Workspace ONE
        "com.vmware.workspaceone",
        // MobileIron
        "com.mobileiron",
        "com.mobileiron.circulate",
        // Lookout
        "com.lookout",
        "com.lookout.android",
        // Zimperium
        "com.zimperium",
        "com.zimperium.zipservice",
        // Blue Coat/Symantec
        "com.bluecoat",
        // MDM generic
        "com.google.android.gms",
        "com.android.managedprovisioning",
        "com.android.settings",
        "com.android.packageinstaller",
        "com.android.commands.pm"
    )

    override fun discover(context: Context): List<DiscoveredPackage> {
        val pm = context.packageManager
        return knownPackages.filter { packageName ->
            try {
                pm.getPackageInfo(packageName, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }.map { DiscoveredPackage(it, source) }
    }
}
