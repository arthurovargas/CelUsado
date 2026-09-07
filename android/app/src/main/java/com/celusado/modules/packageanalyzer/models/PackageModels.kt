package com.celusado.modules.packageanalyzer.models

enum class DiscoverySource {
    LAUNCHER,
    DEVICE_ADMIN,
    ACCESSIBILITY,
    VPN,
    KNOWN_PACKAGE
}

enum class PackageVisibility {
    DETECTED,
    NOT_DETECTED,
    NOT_VISIBLE,
    NOT_AVAILABLE,
    UNKNOWN
}

enum class AppClassification {
    SYSTEM,
    OEM,
    GOOGLE,
    CARRIER,
    USER,
    UNKNOWN
}

data class PackageVersion(
    val name: String,
    val code: Long
)

data class PackageFlags(
    val system: Boolean,
    val updatedSystem: Boolean,
    val debuggable: Boolean
)

data class ServiceInfo(
    val name: String,
    val permission: String?,
    val foregroundServiceType: String?,
    val isExported: Boolean
)

data class ReceiverInfo(
    val name: String,
    val permission: String?,
    val isExported: Boolean
)

data class ActivityInfo(
    val name: String,
    val permission: String?,
    val isExported: Boolean
)

data class ProviderInfo(
    val name: String,
    val authority: String?,
    val isExported: Boolean,
    val readPermission: String?,
    val writePermission: String?
)

data class SigningInfo(
    val schemeVersion: Int,
    val hasMultipleSigners: Boolean,
    val hasPastSigningCertificates: Boolean,
    val currentSigners: List<String>,
    val historicalSigners: List<String>
)

data class ClassificationResult(
    val category: AppClassification,
    val confidence: Double,
    val evidence: List<String>
)

data class NormalizedPackage(
    val packageName: String,
    val label: String,
    val version: PackageVersion,
    val flags: PackageFlags,
    val discoverySources: MutableSet<DiscoverySource>,
    var visibility: PackageVisibility,
    var installerPackageName: String?,
    var firstInstallTime: Long,
    var lastUpdateTime: Long,
    var classification: ClassificationResult?,
    val declaredPermissions: MutableList<String>,
    val services: MutableList<ServiceInfo>,
    val receivers: MutableList<ReceiverInfo>,
    val activities: MutableList<ActivityInfo>,
    val providers: MutableList<ProviderInfo>,
    var signing: SigningInfo?,
    val indicators: MutableList<String>
)

data class CoverageInfo(
    val status: String,
    val packagesDiscovered: Int,
    val packagesAnalyzed: Int,
    val visibilityLimitations: Boolean,
    val limitations: List<String>
)

data class PackageAnalysisResult(
    val packages: List<NormalizedPackage>,
    val coverage: CoverageInfo
)
