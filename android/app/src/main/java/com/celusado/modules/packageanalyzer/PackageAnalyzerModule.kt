package com.celusado.modules.packageanalyzer

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableNativeArray
import com.facebook.react.bridge.WritableNativeMap

class PackageAnalyzerModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String = "AppPackageAnalyzer"

    @ReactMethod
    fun analyzeAllPackages(promise: Promise) {
        try {
            val analyzer = PackageAnalyzer(reactApplicationContext)
            val result = analyzer.analyzeAll()

            val resultMap = WritableNativeMap()

            // Packages array
            val packagesArray = WritableNativeArray()
            for (pkg in result.packages) {
                val pkgMap = WritableNativeMap()
                pkgMap.putString("packageName", pkg.packageName)
                pkgMap.putString("label", pkg.label)

                // Version
                val versionMap = WritableNativeMap()
                versionMap.putString("name", pkg.version.name)
                versionMap.putDouble("code", pkg.version.code.toDouble())
                pkgMap.putMap("version", versionMap)

                // Flags
                val flagsMap = WritableNativeMap()
                flagsMap.putBoolean("system", pkg.flags.system)
                flagsMap.putBoolean("updatedSystem", pkg.flags.updatedSystem)
                flagsMap.putBoolean("debuggable", pkg.flags.debuggable)
                pkgMap.putMap("flags", flagsMap)

                // Discovery Sources
                val sourcesArray = WritableNativeArray()
                for (source in pkg.discoverySources) {
                    sourcesArray.pushString(source.name)
                }
                pkgMap.putArray("discoverySources", sourcesArray)

                // Visibility
                pkgMap.putString("visibility", pkg.visibility.name)

                // Classification
                pkg.classification?.let { cls ->
                    val classMap = WritableNativeMap()
                    classMap.putString("category", cls.category.name)
                    classMap.putDouble("confidence", cls.confidence)
                    val evidenceArray = WritableNativeArray()
                    for (e in cls.evidence) {
                        evidenceArray.pushString(e)
                    }
                    classMap.putArray("evidence", evidenceArray)
                    pkgMap.putMap("classification", classMap)
                }

                // Installer
                pkgMap.putString("installerPackageName", pkg.installerPackageName)

                // Install times
                pkgMap.putDouble("firstInstallTime", pkg.firstInstallTime.toDouble())
                pkgMap.putDouble("lastUpdateTime", pkg.lastUpdateTime.toDouble())

                // Permissions
                val permsArray = WritableNativeArray()
                for (perm in pkg.declaredPermissions) {
                    permsArray.pushString(perm)
                }
                pkgMap.putArray("declaredPermissions", permsArray)

                // Services
                val servicesArray = WritableNativeArray()
                for (svc in pkg.services) {
                    val svcMap = WritableNativeMap()
                    svcMap.putString("name", svc.name)
                    svcMap.putString("permission", svc.permission)
                    svcMap.putString("foregroundServiceType", svc.foregroundServiceType)
                    svcMap.putBoolean("isExported", svc.isExported)
                    servicesArray.pushMap(svcMap)
                }
                pkgMap.putArray("services", servicesArray)

                // Receivers
                val receiversArray = WritableNativeArray()
                for (rcv in pkg.receivers) {
                    val rcvMap = WritableNativeMap()
                    rcvMap.putString("name", rcv.name)
                    rcvMap.putString("permission", rcv.permission)
                    rcvMap.putBoolean("isExported", rcv.isExported)
                    receiversArray.pushMap(rcvMap)
                }
                pkgMap.putArray("receivers", receiversArray)

                // Activities
                val activitiesArray = WritableNativeArray()
                for (act in pkg.activities) {
                    val actMap = WritableNativeMap()
                    actMap.putString("name", act.name)
                    actMap.putString("permission", act.permission)
                    actMap.putBoolean("isExported", act.isExported)
                    activitiesArray.pushMap(actMap)
                }
                pkgMap.putArray("activities", activitiesArray)

                // Providers
                val providersArray = WritableNativeArray()
                for (prov in pkg.providers) {
                    val provMap = WritableNativeMap()
                    provMap.putString("name", prov.name)
                    provMap.putString("authority", prov.authority)
                    provMap.putBoolean("isExported", prov.isExported)
                    provMap.putString("readPermission", prov.readPermission)
                    provMap.putString("writePermission", prov.writePermission)
                    providersArray.pushMap(provMap)
                }
                pkgMap.putArray("providers", providersArray)

                // Signing
                pkg.signing?.let { sign ->
                    val signMap = WritableNativeMap()
                    signMap.putInt("schemeVersion", sign.schemeVersion)
                    signMap.putBoolean("hasMultipleSigners", sign.hasMultipleSigners)
                    signMap.putBoolean("hasPastSigningCertificates", sign.hasPastSigningCertificates)
                    val currentArray = WritableNativeArray()
                    for (s in sign.currentSigners) {
                        currentArray.pushString(s)
                    }
                    signMap.putArray("currentSigners", currentArray)
                    val historicalArray = WritableNativeArray()
                    for (s in sign.historicalSigners) {
                        historicalArray.pushString(s)
                    }
                    signMap.putArray("historicalSigners", historicalArray)
                    pkgMap.putMap("signing", signMap)
                }

                // Indicators
                val indicatorsArray = WritableNativeArray()
                for (ind in pkg.indicators) {
                    indicatorsArray.pushString(ind)
                }
                pkgMap.putArray("indicators", indicatorsArray)

                packagesArray.pushMap(pkgMap)
            }

            resultMap.putArray("packages", packagesArray)

            // Coverage
            val coverageMap = WritableNativeMap()
            coverageMap.putString("status", result.coverage.status)
            coverageMap.putInt("packagesDiscovered", result.coverage.packagesDiscovered)
            coverageMap.putInt("packagesAnalyzed", result.coverage.packagesAnalyzed)
            coverageMap.putBoolean("visibilityLimitations", result.coverage.visibilityLimitations)
            val limitsArray = WritableNativeArray()
            for (lim in result.coverage.limitations) {
                limitsArray.pushString(lim)
            }
            coverageMap.putArray("limitations", limitsArray)
            resultMap.putMap("coverage", coverageMap)

            promise.resolve(resultMap)
        } catch (e: Exception) {
            promise.reject("PACKAGE_ANALYZER_ERROR", e.message, e)
        }
    }
}
