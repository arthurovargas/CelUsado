package com.celusado.modules.packageanalyzer

import com.celusado.modules.packageanalyzer.models.NormalizedPackage

class EvidenceBuilder {

    fun buildIndicators(normalized: NormalizedPackage) {
        // Detect common indicators from components
        val serviceNames = normalized.services.map { it.name.lowercase() }
        val receiverNames = normalized.receivers.map { it.name.lowercase() }
        val permissions = normalized.declaredPermissions.map { it.lowercase() }

        // Device Admin indicator
        if (permissions.any { it.contains("device_admin") || it.contains("bind_device_admin") }) {
            normalized.indicators.add("DEVICE_ADMIN_PERMISSION")
        }

        // Accessibility Service indicator
        if (permissions.any { it.contains("bind_accessibility_service") }) {
            normalized.indicators.add("ACCESSIBILITY_SERVICE")
        }

        // Persistent service indicator
        if (serviceNames.any { it.contains("persistent") || it.contains("daemon") }) {
            normalized.indicators.add("PERSISTENT_SERVICE")
        }

        // Boot completed receiver
        if (receiverNames.any { it.contains("boot_completed") || it.contains("boot") }) {
            normalized.indicators.add("BOOT_COMPLETED_RECEIVER")
        }

        // VPN service
        if (serviceNames.any { it.contains("vpn") || it.contains("tunnel") }) {
            normalized.indicators.add("VPN_SERVICE")
        }

        // Overlay capability
        if (permissions.any { it.contains("system_alert_window") }) {
            normalized.indicators.add("OVERLAY_CAPABILITY")
        }

        // Force lock capability
        if (permissions.any { it.contains("force_lock") }) {
            normalized.indicators.add("FORCE_LOCK")
        }
    }
}
