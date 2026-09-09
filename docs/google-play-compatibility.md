# Google Play Compatibility - cel-usado

## Permissions Analysis

### Declared Permissions

| Permission | Justification | Risk |
|---|---|---|
| `INTERNET` | Required for future backend integration (Phase 8) | Low |

### Not Used (Good)

- `QUERY_ALL_PACKAGES` — Not used. Uses specific `<queries>` instead.
- Camera, Contacts, Location, Storage — Not required for core functionality.
- `READ_PHONE_STATE` — Not required.

## Query Declarations

### Launcher Discovery (Required)
```xml
<intent>
    <action android:name="android.intent.action.MAIN" />
    <category android:name="android.intent.category.LAUNCHER" />
</intent>
```

### Settings Intents (Required for UI navigation)
```xml
<intent>
    <action android:name="android.app.action.DEVICE_ADMIN_SETTINGS" />
</intent>
<intent>
    <action android:name="android.settings.ACCESSIBILITY_SETTINGS" />
</intent>
<intent>
    <action android:name="android.net.vpn.SETTINGS" />
</intent>
```

### Known MDM/Management Packages (Required for detection)
```xml
<package android:name="com.samsung.android.knox" />
<package android:name="com.samsung.android.knox.ddar" />
<package android:name="com.samsung.android.knox.containermanager" />
<package android:name="com.google.android.apps.work.oobconfig" />
<package android:name="com.google.android.apps.work.clc" />
<package android:name="com.microsoft.windowsintune.companyportal" />
<package android:name="com.vmware.workspaceone" />
<package android:name="com.mobileiron" />
<package android:name="com.mobileiron.circulate" />
<package android:name="com.lookout" />
<package android:name="com.lookout.android" />
<package android:name="com.zimperium" />
<package android:name="com.zimperium.zipservice" />
<package android:name="com.android.managedprovisioning" />
<package android:name="com.android.packageinstaller" />
```

## Policy Compliance Notes

### Data Collection
- App collects device information locally only
- No personal data sent to servers (no backend yet)
- No analytics or telemetry

### Device Admin APIs
- App does NOT request Device Admin privileges
- App only DETECTS if other apps are Device Admins
- This is a read-only check, not an admin action

### Privacy
- `android:allowBackup="false"` — Prevents data extraction via backup
- No access to personal content (messages, photos, contacts)
- Logs do not contain serial numbers or IMEI

## Potential Issues

1. **QUERY_ALL_PACKAGES justification**: If Google asks, the app needs to list all packages to detect potentially malicious or administrative applications. This is the core purpose of the app.

2. **Samsung Knox APIs**: Uses reflection to check Knox status. May need Samsung Developer account for production.

3. **Device Admin detection**: Uses `DevicePolicyManager` APIs which are public but restricted. The app only checks status, does not request admin.

## Recommendation

The app is currently compliant with Google Play policies. No changes needed for initial release.
