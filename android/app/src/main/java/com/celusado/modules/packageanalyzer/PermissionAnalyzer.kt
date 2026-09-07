package com.celusado.modules.packageanalyzer

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.celusado.modules.packageanalyzer.models.NormalizedPackage

class PermissionAnalyzer(private val context: Context) {

    private val packageManager = context.packageManager

    private val sensitivePermissions = setOf(
        "android.permission.BIND_DEVICE_ADMIN",
        "android.permission.BIND_ACCESSIBILITY_SERVICE",
        "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE",
        "android.permission.BIND_VPN_SERVICE",
        "android.permission.BIND_INPUT_METHOD",
        "android.permission.BIND_MIDI_DEVICE_SERVICE",
        "android.permission.BIND_printService",
        "android.permission.BINDREAMWORKER",
        "android.permission.BINDREAMSERVICE",
        "android.permission.BIND_CONDITION_PROVIDER_SERVICE",
        "android.permission.BIND_TRUST_AGENT",
        "android.permission.BIND_USAGE_ACCESS",
        "android.permission.BIND_MEDIA_PROJECTION",
        "android.permission.MANAGE_DEVICE_ADMINS",
        "android.permission.FORCE_STOP_PACKAGES",
        "android.permission.DELETE_PACKAGES",
        "android.permission.INSTALL_PACKAGES",
        "android.permission.CLEAR_APP_USER_DATA",
        "android.permission.MASTER_CLEAR",
        "android.permission.REBOOT",
        "android.permission.SHUTDOWN",
        "android.permission.STATUS_BAR",
        "android.permission.MODIFY_PHONE_STATE",
        "android.permission.READ_PHONE_STATE",
        "android.permission.CALL_PHONE",
        "android.permission.READ_SMS",
        "android.permission.SEND_SMS",
        "android.permission.RECEIVE_SMS",
        "android.permission.READ_CONTACTS",
        "android.permission.WRITE_CONTACTS",
        "android.permission.READ_CALENDAR",
        "android.permission.WRITE_CALENDAR",
        "android.permission.READ_CALL_LOG",
        "android.permission.WRITE_CALL_LOG",
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.ACCESS_COARSE_LOCATION",
        "android.permission.ACCESS_BACKGROUND_LOCATION",
        "android.permission.CAMERA",
        "android.permission.RECORD_AUDIO",
        "android.permission.READ_MEDIA_IMAGES",
        "android.permission.READ_MEDIA_VIDEO",
        "android.permission.READ_MEDIA_AUDIO",
        "android.permission.READ_EXTERNAL_STORAGE",
        "android.permission.WRITE_EXTERNAL_STORAGE",
        "android.permission.MANAGE_EXTERNAL_STORAGE",
        "android.permission.SYSTEM_ALERT_WINDOW",
        "android.permission.WRITE_SETTINGS",
        "android.permission.REQUEST_INSTALL_PACKAGES",
        "android.permission.REQUEST_DELETE_PACKAGES",
        "android.permission.QUERY_ALL_PACKAGES",
        "android.permission.ACCESS_NOTIFICATION_POLICY",
        "android.permission.PACKAGE_USAGE_STATS",
        "android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS",
        "android.permission.PROCESS_OUTGOING_CALLS",
        "android.permission.ANSWER_PHONE_CALLS",
        "android.permission.USE_SIP",
        "android.permission.READ_VOICEMAIL",
        "android.permission.WRITE_VOICEMAIL",
        "android.permission.BLUETOOTH_SCAN",
        "android.permission.BLUETOOTH_ADVERTISE",
        "android.permission.BLUETOOTH_CONNECT",
        "android.permission.NEARBY_WIFI_DEVICES",
        "android.permission.UWB_RANGING",
        "android.permission.BODY_SENSORS",
        "android.permission.ACTIVITY_RECOGNITION",
        "android.permission.POST_NOTIFICATIONS",
        "android.permission.READ_PHONE_NUMBERS",
        "android.permission.USE_BIOMETRIC",
        "android.permission.USE_FINGERPRINT"
    )

    fun analyze(packageInfo: PackageInfo, normalized: NormalizedPackage) {
        val requestedPermissions = packageInfo.requestedPermissions
        if (requestedPermissions != null) {
            normalized.declaredPermissions.addAll(requestedPermissions.toList())

            // Identify sensitive permissions
            for (perm in requestedPermissions) {
                if (perm in sensitivePermissions) {
                    normalized.indicators.add("SENSITIVE_PERMISSION:$perm")
                }
            }

            // Check for device admin permission
            if (requestedPermissions.any { it == "android.permission.BIND_DEVICE_ADMIN" }) {
                normalized.indicators.add("DEVICE_ADMIN_PERMISSION")
            }

            // Check for accessibility service
            if (requestedPermissions.any { it == "android.permission.BIND_ACCESSIBILITY_SERVICE" }) {
                normalized.indicators.add("ACCESSIBILITY_SERVICE_PERMISSION")
            }

            // Check for VPN service
            if (requestedPermissions.any { it == "android.permission.BIND_VPN_SERVICE" }) {
                normalized.indicators.add("VPN_SERVICE_PERMISSION")
            }

            // Check for overlay
            if (requestedPermissions.any { it == "android.permission.SYSTEM_ALERT_WINDOW" }) {
                normalized.indicators.add("OVERLAY_PERMISSION")
            }

            // Check for install packages
            if (requestedPermissions.any { it == "android.permission.REQUEST_INSTALL_PACKAGES" }) {
                normalized.indicators.add("INSTALL_PACKAGES_PERMISSION")
            }

            // Check for query all packages
            if (requestedPermissions.any { it == "android.permission.QUERY_ALL_PACKAGES" }) {
                normalized.indicators.add("QUERY_ALL_PACKAGES_PERMISSION")
            }
        }
    }
}
