package com.celusado.modules.integrity.analyzers

import android.os.Build
import com.celusado.modules.integrity.models.IntegrityEvidenceItem
import com.celusado.modules.integrity.models.IntegrityEvidenceSource
import com.celusado.modules.integrity.models.IntegrityIndicator
import com.celusado.modules.integrity.models.IntegrityIndicatorType
import com.celusado.modules.integrity.models.IntegrityStatus
import java.io.File

class RootIndicatorAnalyzer {

    fun analyze(): List<IntegrityIndicator> {
        val indicators = mutableListOf<IntegrityIndicator>()

        indicators.addAll(checkSuBinaries())
        indicators.addAll(checkRootPaths())
        indicators.addAll(checkSystemProperties())
        indicators.addAll(checkDangerousProps())

        return indicators
    }

    private fun checkSuBinaries(): List<IntegrityIndicator> {
        val indicators = mutableListOf<IntegrityIndicator>()
        val suPaths = listOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )

        for (path in suPaths) {
            val file = File(path)
            if (file.exists()) {
                indicators.add(
                    IntegrityIndicator(
                        type = IntegrityIndicatorType.ROOT_BINARY_INDICATOR,
                        status = IntegrityStatus.DETECTED,
                        confidence = 0.90,
                        source = "FILE_SYSTEM",
                        value = path,
                        evidence = listOf(
                            IntegrityEvidenceItem(
                                type = "SU_BINARY",
                                source = IntegrityEvidenceSource.FILE_SYSTEM,
                                field = path,
                                value = path,
                                description = "su binary found at $path"
                            )
                        ),
                        limitations = listOf(
                            "File existence does not confirm the device is currently rooted"
                        )
                    )
                )
            }
        }

        if (indicators.isEmpty()) {
            indicators.add(
                IntegrityIndicator(
                    type = IntegrityIndicatorType.ROOT_BINARY_INDICATOR,
                    status = IntegrityStatus.NOT_DETECTED,
                    confidence = 0.70,
                    source = "FILE_SYSTEM",
                    value = null,
                    evidence = emptyList(),
                    limitations = listOf(
                        "Root binaries may be located in non-standard paths"
                    )
                )
            )
        }

        return indicators
    }

    private fun checkRootPaths(): List<IntegrityIndicator> {
        val indicators = mutableListOf<IntegrityIndicator>()
        val rootPaths = listOf(
            "/system/app/Superuser.apk",
            "/system/app/SuperSU.apk",
            "/data/data/com.topjohnwu.magisk",
            "/data/adb/magisk",
            "/cache/su",
            "/system/lib/libsupol.so",
            "/system/lib64/libsupol.so"
        )

        for (path in rootPaths) {
            val file = File(path)
            if (file.exists()) {
                indicators.add(
                    IntegrityIndicator(
                        type = IntegrityIndicatorType.ROOT_FRAMEWORK_INDICATOR,
                        status = IntegrityStatus.DETECTED,
                        confidence = 0.85,
                        source = "FILE_SYSTEM",
                        value = path,
                        evidence = listOf(
                            IntegrityEvidenceItem(
                                type = "ROOT_PATH",
                                source = IntegrityEvidenceSource.FILE_SYSTEM,
                                field = path,
                                value = path,
                                description = "Root-related path found: $path"
                            )
                        ),
                        limitations = listOf(
                            "File/directory presence does not confirm active root"
                        )
                    )
                )
            }
        }

        return indicators
    }

    private fun checkSystemProperties(): List<IntegrityIndicator> {
        val indicators = mutableListOf<IntegrityIndicator>()

        val roSecure = getSystemProperty("ro.secure", "1")
        if (roSecure == "0") {
            indicators.add(
                IntegrityIndicator(
                    type = IntegrityIndicatorType.ROOT_FRAMEWORK_INDICATOR,
                    status = IntegrityStatus.DETECTED,
                    confidence = 0.85,
                    source = "SYSTEM_PROPERTY",
                    value = roSecure,
                    evidence = listOf(
                        IntegrityEvidenceItem(
                            type = "SYSTEM_PROPERTY",
                            source = IntegrityEvidenceSource.SYSTEM_PROPERTY,
                            field = "ro.secure",
                            value = roSecure,
                            description = "ro.secure=0 indicates non-secure build"
                        )
                    )
                )
            )
        }

        val roDebuggable = getSystemProperty("ro.debuggable", "0")
        if (roDebuggable == "1") {
            indicators.add(
                IntegrityIndicator(
                    type = IntegrityIndicatorType.ROOT_FRAMEWORK_INDICATOR,
                    status = IntegrityStatus.DETECTED,
                    confidence = 0.80,
                    source = "SYSTEM_PROPERTY",
                    value = roDebuggable,
                    evidence = listOf(
                        IntegrityEvidenceItem(
                            type = "SYSTEM_PROPERTY",
                            source = IntegrityEvidenceSource.SYSTEM_PROPERTY,
                            field = "ro.debuggable",
                            value = roDebuggable,
                            description = "ro.debuggable=1 indicates debug build"
                        )
                    )
                )
            )
        }

        return indicators
    }

    private fun checkDangerousProps(): List<IntegrityIndicator> {
        val indicators = mutableListOf<IntegrityIndicator>()

        val props = mapOf(
            "ro.adb.secure" to "0",
            "ro.build.selinux" to "0",
            "persist.sys.usb.config" to "mtp,adb"
        )

        for ((key, dangerousValue) in props) {
            val value = getSystemProperty(key, "")
            if (value == dangerousValue) {
                indicators.add(
                    IntegrityIndicator(
                        type = IntegrityIndicatorType.BUILD_DEVELOPMENT_CONFIG,
                        status = IntegrityStatus.DETECTED,
                        confidence = 0.60,
                        source = "SYSTEM_PROPERTY",
                        value = value,
                        evidence = listOf(
                            IntegrityEvidenceItem(
                                type = "DANGEROUS_PROPERTY",
                                source = IntegrityEvidenceSource.SYSTEM_PROPERTY,
                                field = key,
                                value = value,
                                description = "$key=$value indicates development configuration"
                            )
                        ),
                        limitations = listOf(
                            "This property value may be set intentionally for development purposes"
                        )
                    )
                )
            }
        }

        return indicators
    }

    private fun getSystemProperty(key: String, defaultValue: String): String {
        return try {
            val clazz = Class.forName("android.os.SystemProperties")
            val get = clazz.getMethod("get", String::class.java, String::class.java)
            get.invoke(null, key, defaultValue) as? String ?: defaultValue
        } catch (e: Exception) {
            defaultValue
        }
    }
}
