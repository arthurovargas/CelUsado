package com.celusado.modules.packageanalyzer

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.celusado.modules.packageanalyzer.models.NormalizedPackage
import com.celusado.modules.packageanalyzer.models.SigningInfo
import java.security.MessageDigest

class SignatureAnalyzer {

    fun analyze(packageInfo: PackageInfo, normalized: NormalizedPackage) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            analyzeWithSigningInfo(packageInfo, normalized)
        } else {
            @Suppress("DEPRECATION")
            analyzeWithSignatures(packageInfo, normalized)
        }
    }

    private fun analyzeWithSigningInfo(packageInfo: PackageInfo, normalized: NormalizedPackage) {
        val signingInfo = packageInfo.signingInfo ?: return
        val hasMultipleSigners = signingInfo.hasMultipleSigners()
        val hasPastCerts = signingInfo.hasPastSigningCertificates()

        val currentSigners = signingInfo.apkContentsSigners?.map { cert ->
            hashCertificate(cert.toByteArray())
        } ?: emptyList()

        val historicalSigners = if (hasPastCerts) {
            signingInfo.signingCertificateHistory?.map { cert ->
                hashCertificate(cert.toByteArray())
            } ?: emptyList()
        } else {
            emptyList()
        }

        normalized.signing = SigningInfo(
            schemeVersion = 2,
            hasMultipleSigners = hasMultipleSigners,
            hasPastSigningCertificates = hasPastCerts,
            currentSigners = currentSigners,
            historicalSigners = historicalSigners
        )
    }

    @Suppress("DEPRECATION")
    private fun analyzeWithSignatures(packageInfo: PackageInfo, normalized: NormalizedPackage) {
        val signatures = packageInfo.signatures ?: return
        val signers = signatures.map { cert ->
            hashCertificate(cert.toByteArray())
        }

        normalized.signing = SigningInfo(
            schemeVersion = 1,
            hasMultipleSigners = signatures.size > 1,
            hasPastSigningCertificates = false,
            currentSigners = signers,
            historicalSigners = emptyList()
        )
    }

    private fun hashCertificate(certBytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(certBytes)
        return hash.joinToString(":") { "%02X".format(it) }
    }
}
