package com.celusado.modules.packageanalyzer

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.celusado.modules.packageanalyzer.discovery.PackageDiscoveryManager
import com.celusado.modules.packageanalyzer.models.CoverageInfo
import com.celusado.modules.packageanalyzer.models.NormalizedPackage
import com.celusado.modules.packageanalyzer.models.PackageAnalysisResult
import com.celusado.modules.packageanalyzer.models.PackageVisibility

class PackageAnalyzer(private val context: Context) {

    private val discoveryManager = PackageDiscoveryManager()
    private val metadataReader = PackageMetadataReader(context)
    private val componentAnalyzer = ComponentAnalyzer(context)
    private val permissionAnalyzer = PermissionAnalyzer(context)
    private val signatureAnalyzer = SignatureAnalyzer()
    private val evidenceBuilder = EvidenceBuilder()
    private val packageClassifier = PackageClassifier()

    fun analyzeAll(): PackageAnalysisResult {
        // Phase 1: Discovery
        val discoveredPackages = discoveryManager.discoverAll(context)

        val normalizedPackages = mutableListOf<NormalizedPackage>()

        // Phase 2: For each discovered package, read metadata and analyze
        for ((packageName, sources) in discoveredPackages) {
            val normalized = metadataReader.readMetadata(packageName)
            if (normalized != null) {
                normalized.discoverySources.addAll(sources)
                normalizedPackages.add(normalized)
            }
        }

        // Phase 3: Analyze each package
        for (normalized in normalizedPackages) {
            try {
                val packageInfo = context.packageManager.getPackageInfo(
                    normalized.packageName,
                    getFlags()
                )

                // Components
                componentAnalyzer.analyze(packageInfo, normalized)

                // Permissions
                permissionAnalyzer.analyze(packageInfo, normalized)

                // Signatures
                signatureAnalyzer.analyze(packageInfo, normalized)

                // Classification
                val classification = packageClassifier.classify(normalized)
                normalized.classification = classification

                // Evidence
                evidenceBuilder.buildIndicators(normalized)

            } catch (e: PackageManager.NameNotFoundException) {
                normalized.visibility = PackageVisibility.NOT_VISIBLE
            } catch (e: Exception) {
                normalized.visibility = PackageVisibility.UNKNOWN
            }
        }

        // Phase 4: Build coverage
        val coverage = CoverageInfo(
            status = if (discoveredPackages.isNotEmpty()) "PARTIAL" else "LIMITED",
            packagesDiscovered = discoveredPackages.size,
            packagesAnalyzed = normalizedPackages.count { it.visibility == PackageVisibility.DETECTED },
            visibilityLimitations = true,
            limitations = listOf("PACKAGE_VISIBILITY")
        )

        return PackageAnalysisResult(
            packages = normalizedPackages,
            coverage = coverage
        )
    }

    private fun getFlags(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.content.pm.PackageManager.PackageInfoFlags.of(
                android.content.pm.PackageManager.GET_META_DATA.toLong() or
                android.content.pm.PackageManager.GET_PERMISSIONS.toLong() or
                android.content.pm.PackageManager.GET_SERVICES.toLong() or
                android.content.pm.PackageManager.GET_RECEIVERS.toLong() or
                android.content.pm.PackageManager.GET_ACTIVITIES.toLong() or
                android.content.pm.PackageManager.GET_PROVIDERS.toLong() or
                android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES.toLong()
            ).value.toInt()
        } else {
            @Suppress("DEPRECATION")
            android.content.pm.PackageManager.GET_META_DATA or
            android.content.pm.PackageManager.GET_PERMISSIONS or
            android.content.pm.PackageManager.GET_SERVICES or
            android.content.pm.PackageManager.GET_RECEIVERS or
            android.content.pm.PackageManager.GET_ACTIVITIES or
            android.content.pm.PackageManager.GET_PROVIDERS or
            android.content.pm.PackageManager.GET_SIGNATURES
        }
    }
}
