package com.celusado.modules.packageanalyzer

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.celusado.modules.packageanalyzer.models.NormalizedPackage
import com.celusado.modules.packageanalyzer.models.PackageFlags
import com.celusado.modules.packageanalyzer.models.PackageVisibility
import com.celusado.modules.packageanalyzer.models.PackageVersion

class PackageMetadataReader(private val context: Context) {

    private val packageManager = context.packageManager

    fun readMetadata(packageName: String): NormalizedPackage? {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(
                        PackageManager.GET_META_DATA.toLong() or
                        PackageManager.GET_PERMISSIONS.toLong() or
                        PackageManager.GET_SERVICES.toLong() or
                        PackageManager.GET_RECEIVERS.toLong() or
                        PackageManager.GET_ACTIVITIES.toLong() or
                        PackageManager.GET_PROVIDERS.toLong() or
                        PackageManager.GET_SIGNING_CERTIFICATES.toLong()
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_META_DATA or
                    PackageManager.GET_PERMISSIONS or
                    PackageManager.GET_SERVICES or
                    PackageManager.GET_RECEIVERS or
                    PackageManager.GET_ACTIVITIES or
                    PackageManager.GET_PROVIDERS or
                    PackageManager.GET_SIGNATURES
                )
            }

            val appInfo = packageInfo.applicationInfo
            val label = appInfo?.loadLabel(packageManager)?.toString() ?: packageName

            val installerPackageName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                packageManager.getInstallSourceInfo(packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstallerPackageName(packageName)
            }

            NormalizedPackage(
                packageName = packageName,
                label = label,
                version = PackageVersion(
                    name = packageInfo.versionName ?: "unknown",
                    code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        packageInfo.longVersionCode
                    } else {
                        @Suppress("DEPRECATION")
                        packageInfo.versionCode.toLong()
                    }
                ),
                flags = PackageFlags(
                    system = (appInfo?.flags ?: 0) and android.content.pm.ApplicationInfo.FLAG_SYSTEM != 0,
                    updatedSystem = (appInfo?.flags ?: 0) and android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0,
                    debuggable = (appInfo?.flags ?: 0) and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0
                ),
                discoverySources = mutableSetOf(),
                visibility = PackageVisibility.DETECTED,
                installerPackageName = installerPackageName,
                firstInstallTime = packageInfo.firstInstallTime,
                lastUpdateTime = packageInfo.lastUpdateTime,
                classification = null,
                declaredPermissions = mutableListOf(),
                services = mutableListOf(),
                receivers = mutableListOf(),
                activities = mutableListOf(),
                providers = mutableListOf(),
                signing = null,
                indicators = mutableListOf()
            )
        } catch (e: PackageManager.NameNotFoundException) {
            // Package exists in discovery but metadata cannot be read
            // This is a visibility limitation on Android 11+
            null
        }
    }

    fun checkPackageInstalled(packageName: String): PackageVisibility {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0)
            }
            PackageVisibility.DETECTED
        } catch (e: PackageManager.NameNotFoundException) {
            PackageVisibility.NOT_AVAILABLE
        } catch (e: Exception) {
            PackageVisibility.UNKNOWN
        }
    }
}
