# Capacidades de Android para Aplicaciones de Diagnóstico

> **Fase 0 — Investigación técnica**
> Versión del documento: 1.0
> Fecha: 2026-09-04
> Versiones objetivo: Android 13 (API 33), Android 14 (API 34), Android 15 (API 35), Android 16 (API 36)

---

## 1. Resumen ejecutivo

Este documento documenta qué información puede obtener una aplicación Android **normal** (sin privilegios de sistema, sin ser Device Admin, sin permisos especiales de Play Store) sobre el dispositivo y otras aplicaciones instaladas.

### Limitación fundamental

Desde Android 11 (API 30), Android implementa **package visibility filtering**: una app solo puede ver un subconjunto de las aplicaciones instaladas. Esto afecta directamente a cualquier herramienta de diagnóstico.

### Estrategia de mitigación

El enfoque principal para nuestro caso de uso es:

1. **Consultas por intents conocidos** — declarar intents en `<queries>` del manifest para descubrir apps relevantes
2. **Paquetes conocidos de MDM/administración** — mantener una base de datos local de paquetes conocidos
3. **Detección indirecta** — usar APIs públicas que no requieren visibilidad completa

---

## 2. Matriz de compatibilidad completa

### 2.1 Visibilidad de paquetes e instalación

| Función | Android 13 | Android 14 | Android 15 | Android 16 | Permiso requerido | Rol requerido | Notas |
|---------|-----------|-----------|-----------|-----------|-------------------|---------------|-------|
| `getInstalledPackages()` | Filtrado | Filtrado | Filtrado | Filtrado | Ninguno | Ninguno | Retorna solo paquetes visibles sin `QUERY_ALL_PACKAGES` |
| `getInstalledApplications()` | Filtrado | Filtrado | Filtrado | Filtrado | Ninguno | Ninguno | Mismo filtrado que arriba |
| `getPackageInfo(pkg)` | Disponible | Disponible | Disponible | Disponible | Ninguno | Paquete visible | Funciona solo para paquetes en la lista de visibilidad |
| `QUERY_ALL_PACKAGES` | Restringido Play Store | Restringido Play Store | Restringido Play Store | Restringido Play Store | `QUERY_ALL_PACKAGES` | Ninguno | Requiere justificación en Play Store; aprobación manual |
| `<queries>` con intents | Disponible | Disponible | Disponible | Disponible | Ninguno | Ninguno | Estrategia principal: declarar intents relevantes |
| Paquetes del instalador | Visible | Visible | Visible | Visible | Ninguno | Ninguno | Siempre visible quién instaló la app |
| Paquetes que interactúan con tu app | Visible | Visible | Visible | Visible | Ninguno | Ninguno | Siempre visibles |

**Estrategia recomendada para package visibility:**

```xml
<manifest>
    <queries>
        <!-- Apps con launcher (cubre ~80-90% de apps de usuario) -->
        <intent>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent>

        <!-- Device Admin apps -->
        <intent>
            <action android:name="android.app.action.DEVICE_ADMIN_ENABLED" />
        </intent>

        <!-- Accessibility Settings -->
        <intent>
            <action android:name="android.settings.ACCESSIBILITY_SETTINGS" />
        </intent>

        <!-- VPN Settings -->
        <intent>
            <action android:name="android.net.vpn.SETTINGS" />
        </intent>

        <!-- Application Details -->
        <intent>
            <action android:name="android.settings.APPLICATION_DETAILS_SETTINGS" />
            <data android:scheme="package" />
        </intent>

        <!-- Paquetes conocidos de MDM -->
        <package android:name="com.samsung.android.knox胗uard" />
        <package android:name="com.samsung.android.knox.maskagent" />
        <package android:name="com.samsung.android.knox.containercore" />
        <package android:name="com.google.android.apps.work.oobconfig" />
        <package android:name="com.microsoft.windowsintune.companyportal" />
        <package android:name="com.vmware.workspaceone" />
        <package android:name="com.mobileiron" />
        <!-- Agregar más paquetes conocidos de MDM -->
    </queries>
</manifest>
```

---

### 2.2 ApplicationInfo y PackageInfo

| Campo/API | Android 13 | Android 14 | Android 15 | Android 16 | Permiso | Rol | Notas |
|-----------|-----------|-----------|-----------|-----------|---------|-----|-------|
| `PackageInfo.packageName` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | |
| `PackageInfo.versionName` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | |
| `PackageInfo.versionCode` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | Usar `PackageInfoCompat.getLongVersionCode()` |
| `PackageInfo.firstInstallTime` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | |
| `PackageInfo.lastUpdateTime` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | |
| `PackageInfo.applicationInfo.flags` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | `FLAG_SYSTEM`, `FLAG_UPDATED_SYSTEM_APP` |
| `PackageInfo.applicationInfo.sourceDir` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | Ruta al APK |
| `PackageInfo.installerPackageName` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | Identifica la tienda que instaló la app |
| `PackageInfo.getSigningInfo()` | ✅ (API 28+) | ✅ | ✅ | ✅ | Ninguno | Paquete visible | `GET_SIGNING_CERTIFICATES` |
| `PackageInfo.signatures` | ✅ (deprecated API 28) | ✅ | ✅ | ✅ | Ninguno | Paquete visible | `GET_SIGNATURES` |
| `PackageInfo.permissions` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | Permisos que la app **declara** |
| `PackageInfo.requestedPermissions` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | Permisos solicitados |
| `PackageInfo.services` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | `GET_SERVICES` flag |
| `PackageInfo.receivers` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | `GET_RECEIVERS` flag |
| `PackageInfo.activities` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | `GET_ACTIVITIES` flag |
| `PackageInfo.providers` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | `GET_PROVIDERS` flag |
| `ApplicationInfo.uid` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | |

**Notas importantes:**
- `GET_SIGNATURES` fue deprecado en API 28. Usar `GET_SIGNING_CERTIFICATES` en su lugar
- `GET_SIGNING_CERTIFICATES` soporta APK Signature Scheme v3 (rotación de certificados)
- Los flags como `GET_SERVICES`, `GET_RECEIVERS`, etc. son acumulables con `|`

---

### 2.3 Permisos declarados por aplicaciones

| Campo/API | Android 13 | Android 14 | Android 15 | Android 16 | Permiso | Rol | Notas |
|-----------|-----------|-----------|-----------|-----------|---------|-----|-------|
| `PackageInfo.requestedPermissions` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | Lista de permisos solicitados en manifest |
| `PackageInfo.permissions` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | Permisos definidos por la app (PermissionInfo) |
| `PackageManager.getPermissionInfo()` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | Detalle de un permiso conocido |
| `PermissionInfo.protectionLevel` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | Nivel de protección del permiso |

**Permisos especialmente relevantes para detectar:**

| Permiso | Nivel | Indicador |
|---------|-------|-----------|
| `android.permission.BIND_DEVICE_ADMIN` | Normal | App potencialmente administrativa |
| `android.permission.MANAGE_DEVICE_ADMINS` | Signature | Control de administradores |
| `android.permission.FORCE_STOP_PACKAGES` | Signature | Capacidad de forzar detención |
| `android.permission.DELETE_PACKAGES` | Normal | Puede eliminar paquetes |
| `android.permission.INSTALL_PACKAGES` | Signature | Puede instalar paquetes |
| `android.permission.INJECT_EVENTS` | Signature | Inyectar eventos de UI |
| `android.permission.BIND_ACCESSIBILITY_SERVICE` | Normal | Servicio de accesibilidad |
| `android.permission.SYSTEM_ALERT_WINDOW` | Special | Dibujar sobre otras apps |
| `android.permission.WRITE_SECURE_SETTINGS` | Signature | Modificar configuración segura |
| `android.permission.READ_PHONE_STATE` | Danger | Leer estado del teléfono |
| `android.permission.RECEIVE_BOOT_COMPLETED` | Normal | Ejecutar al arrancar |
| `android.permission.FOREGROUND_SERVICE` | Normal | Servicio en primer plano |
| `android.permission.REQUEST_INSTALL_PACKAGES` | Normal | Solicitar instalación |
| `android.permission.MANAGE_EXTERNAL_STORAGE` | Special | Acceso a almacenamiento |

---

### 2.4 Servicios, receivers y activities

| Campo/API | Android 13 | Android 14 | Android 15 | Android 16 | Permiso | Rol | Notas |
|-----------|-----------|-----------|-----------|-----------|---------|-----|-------|
| `PackageInfo.services[]` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | Flag: `PackageManager.GET_SERVICES` |
| `ServiceInfo.name` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | Nombre completo del servicio |
| `ServiceInfo.permission` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | Permiso requerido para bindear |
| `ServiceInfo.foregroundServiceType` | ✅ (API 29+) | ✅ | ✅ | ✅ | Ninguno | Paquete visible | Tipo de servicio en primer plano |
| `PackageInfo.receivers[]` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | Flag: `PackageManager.GET_RECEIVERS` |
| `ActivityInfo.name` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | |
| `ActivityInfo.permission` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | Permiso para iniciar la activity |
| `ActivityInfo.exported` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | Si la activity es exportada |
| `PackageInfo.activities[]` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | Flag: `PackageManager.GET_ACTIVITIES` |
| `PackageInfo.providers[]` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | Flag: `PackageManager.GET_PROVIDERS` |
| `ProviderInfo.authority` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | |
| `ProviderInfo.exported` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | |
| `ProviderInfo.readPermission` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | |
| `ProviderInfo.writePermission` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | |

**Componentes especialmente relevantes:**

| Componente | Por qué importa |
|-----------|----------------|
| Servicios con `BIND_ACCESSIBILITY_SERVICE` | Indicador de servicio de accesibilidad |
| Receivers con `BOOT_COMPLETED` | Persistencia tras reinicio |
| Receivers con `MY_PACKAGE_REPLACED` | Persistencia tras actualización |
| Servicios con `FOREGROUND_SERVICE_TYPE_SPECIAL_USE` | Servicios persistentes de uso especial |
| Activities exportadas sin launcher | Pueden ser entry points de administración |
| Providers exportados con permisos débiles | Posible vector de datos |

---

### 2.5 Firmas y certificados

| Campo/API | Android 13 | Android 14 | Android 15 | Android 16 | Permiso | Rol | Notas |
|-----------|-----------|-----------|-----------|-----------|---------|-----|-------|
| `PackageManager.GET_SIGNING_CERTIFICATES` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | API 28+, reemplaza GET_SIGNATURES |
| `SigningInfo.signingCertificateHistory` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | Historial de certificados (rotación) |
| `SigningInfo.apkContentsSigners` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | Certificados actuales del APK |
| `SigningInfo.hasMultipleSigners()` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | |
| `SigningInfo.hasPastSigningCertificates()` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | Rotación de certificados |
| `SigningInfo.getSchemeVersion()` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | Versión del esquema de firma (v1/v2/v3/v4) |
| `PackageManager.GET_SIGNATURES` | ✅ (deprecated) | ✅ | ✅ | ✅ | Ninguno | Paquete visible | API antigua, usar GET_SIGNING_CERTIFICATES |

**Código de ejemplo para obtener firmas:**

```kotlin
fun getPackageSignatures(pm: PackageManager, packageName: String): List<String> {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            val signingInfo = packageInfo.signingInfo ?: return emptyList()
            val signatures = if (signingInfo.hasMultipleSigners()) {
                signingInfo.apkContentsSigners
            } else {
                signingInfo.signingCertificateHistory
            }
            signatures?.map { signature ->
                val digest = MessageDigest.getInstance("SHA-256")
                digest.update(signature.toByteArray())
                digest.digest().joinToString("") { "%02X".format(it) }
            } ?: emptyList()
        } else {
            @Suppress("DEPRECATION")
            val packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            packageInfo.signatures?.map { signature ->
                val digest = MessageDigest.getInstance("SHA-256")
                digest.update(signature.toByteArray())
                digest.digest().joinToString("") { "%02X".format(it) }
            } ?: emptyList()
        }
    } catch (e: Exception) {
        emptyList()
    }
}
```

---

### 2.6 Device Administrator — Detección indirecta

> **Nota crítica:** `DevicePolicyManager.getActiveAdmins()` solo retorna resultados si **nuestra app** es Device Admin. Para detectar otros administradores, se usa detección indirecta.

| Método | Android 13 | Android 14 | Android 15 | Android 16 | Permiso | Rol | Notas |
|--------|-----------|-----------|-----------|-----------|---------|-----|-------|
| `queryBroadcastReceivers(DEVICE_ADMIN_ENABLED)` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | Busca receivers que declaran DEVICE_ADMIN_ENABLED |
| `DevicePolicyManager.getActiveAdmins()` | Solo si somos DA | Solo si somos DA | Solo si somos DA | Solo si somos DA | `BIND_DEVICE_ADMIN` | Device Admin | Solo retorna admins si nuestra app es admin |
| `DevicePolicyManager.isDeviceOwnerApp(pkg)` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | Verificar si paquete es Device Owner |
| `DevicePolicyManager.isProfileOwnerApp(pkg)` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | Verificar si paquete es Profile Owner |
| `DevicePolicyManager.isOrganizationOwnedDeviceWithManagedProfile()` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | Dispositivo con perfil de organización |
| `CrossProfileApps.getTargetUserProfiles()` | ✅ (API 30+) | ✅ | ✅ | ✅ | `INTERACT_ACROSS_PROFILES` | Ninguno | Perfiles gestionados |
| `UserManager.getUserProfiles()` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | IDs de perfiles |
| `PackageManager.queryIntentActivities(DEVICE_ADMIN_ENABLED)` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | Apps que pueden ser Device Admin |

**Método de detección indirecta recomendado:**

```kotlin
data class DeviceAdminInfo(
    val packageName: String,
    val componentName: String,
    val label: String,
    val isActive: Boolean,
    val policies: List<String>
)

fun detectDeviceAdmins(context: Context): List<DeviceAdminInfo> {
    val pm = context.packageManager
    val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val activeAdmins = dpm.activeAdmins?.map { it.packageName }?.toSet() ?: emptySet()

    val intent = Intent("android.app.action.DEVICE_ADMIN_ENABLED")
    val resolveInfos = pm.queryBroadcastReceivers(intent, PackageManager.GET_META_DATA)

    return resolveInfos.map { resolveInfo ->
        val packageName = resolveInfo.activityInfo.packageName
        val componentName = resolveInfo.activityInfo.name
        val label = resolveInfo.loadLabel(pm).toString()

        // Obtener políticas declaradas del metadata
        val policies = mutableListOf<String>()
        try {
            val adminInfo = android.app.admin.DeviceAdminInfo(context, resolveInfo)
            val policyFlags = adminInfo.usedPolicies
            // Mapear flags a nombres legibles
            if (adminInfo.isVisibleInSettings) policies.add("VISIBLE")
            // Agregar más políticas según DeviceAdminInfo.usedPolicies
        } catch (e: Exception) {
            // Algunos receivers pueden no ser parseables como DeviceAdminInfo
        }

        DeviceAdminInfo(
            packageName = packageName,
            componentName = componentName,
            label = label,
            isActive = packageName in activeAdmins,
            policies = policies
        )
    }
}
```

**Limitaciones de la detección indirecta:**

- No se pueden obtener todas las políticas declaradas sin parsear el XML del admin receptor
- Algunos admins pueden no declarar `DEVICE_ADMIN_ENABLED` de forma convencional
- La visibilidad de paquetes puede filtrar algunos resultados

---

### 2.7 Device Owner y Profile Owner

| Campo/API | Android 13 | Android 14 | Android 15 | Android 16 | Permiso | Rol | Notas |
|-----------|-----------|-----------|-----------|-----------|---------|-----|-------|
| `DevicePolicyManager.isDeviceOwnerApp(pkg)` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | Disponibilidad completa sin permisos |
| `DevicePolicyManager.isProfileOwnerApp(pkg)` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | Disponibilidad completa sin permisos |
| `DevicePolicyManager.getDeviceOwner()` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | ComponentName del Device Owner (nullable) |
| `DevicePolicyManager.getProfileOwner(userHandle)` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | ComponentName del Profile Owner |
| `DevicePolicyManager.isOrganizationOwnedDeviceWithManagedProfile()` | ✅ (API 30+) | ✅ | ✅ | ✅ | Ninguno | Ninguno | Perfil de organización en dispositivo propio |
| `DevicePolicyManager.getDeviceOwnerComponent()` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | Nombre del componente del DO |
| `DevicePolicyManager.getDeviceOwnerName()` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | Nombre legible del DO |
| `DevicePolicyManager.getProfileOwnerName(userHandle)` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | Nombre legible del PO |

**Código de ejemplo:**

```kotlin
data class DevicePolicyInfo(
    val hasDeviceOwner: Boolean,
    val deviceOwnerPackage: String?,
    val deviceOwnerLabel: String?,
    val hasProfileOwner: Boolean,
    val profileOwnerPackage: String?,
    val profileOwnerLabel: String?,
    val isOrganizationOwned: Boolean
)

fun getDevicePolicyInfo(context: Context): DevicePolicyInfo {
    val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    val deviceOwnerComponent = dpm.deviceOwnerComponent
    val profileOwnerComponent = dpm.getProfileOwner android.os.Process.myUserHandle()

    return DevicePolicyInfo(
        hasDeviceOwner = deviceOwnerComponent != null,
        deviceOwnerPackage = deviceOwnerComponent?.packageName,
        deviceOwnerLabel = deviceOwnerComponent?.let {
            try { dpm.getDeviceOwnerName() } catch (e: Exception) { null }
        },
        hasProfileOwner = profileOwnerComponent != null,
        profileOwnerPackage = profileOwnerComponent?.packageName,
        profileOwnerLabel = profileOwnerComponent?.let {
            try { dpm.getProfileOwnerName(android.os.Process.myUserHandle()) } catch (e: Exception) { null }
        },
        isOrganizationOwned = dpm.isOrganizationOwnedDeviceWithManagedProfile
    )
}
```

---

### 2.8 Accessibility Services

| Campo/API | Android 13 | Android 14 | Android 15 | Android 16 | Permiso | Rol | Notas |
|-----------|-----------|-----------|-----------|-----------|---------|-----|-------|
| `AccessibilityManager.getEnabledAccessibilityServiceList()` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | Lista de servicios habilitados |
| `AccessibilityManager.getInstalledAccessibilityServiceList()` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | Todos los servicios instalados |
| `Settings.Secure.getString(ENABLED_ACCESSIBILITY_SERVICES)` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | String con servicios habilitados (formato `pkg/svc:pkg/svc`) |
| `AccessibilityServiceInfo.getCapabilities()` | ✅ (API 16+) | ✅ | ✅ | ✅ | Ninguno | Ninguno | Capacidades del servicio |
| `AccessibilityServiceInfo.feedbackType` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | Tipos de feedback soportados |
| `AccessibilityServiceInfo.eventTypes` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | Tipos de eventos que escucha |
| `AccessibilityServiceInfo.packageNames` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | Paquetes monitoreados (null = todos) |
| `AccessibilityServiceInfo.settingsActivityName` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | Activity de configuración del servicio |

**Restricciones importantes:**

- Android 13+: Apps instaladas desde fuentes no verificadas tienen "Restricted Settings" — no pueden habilitarse como servicios de accesibilidad a menos que se instalen desde Play Store o con instalación basada en sesión
- Android 14+: `accessibilityDataSensitive` attribute para proteger views específicas

**Código de ejemplo:**

```kotlin
data class AccessibilityServiceInfo(
    val id: String,
    val packageName: String,
    val serviceName: String,
    val label: String,
    val feedbackType: Int,
    val capabilities: Int,
    val eventTypes: Int,
    val packageNames: Array<String>?,
    val isInstalled: Boolean,
    val isEnabled: Boolean
)

fun detectAccessibilityServices(context: Context): List<AccessibilityServiceInfo> {
    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val pm = context.packageManager

    val enabledIds = am.getEnabledAccessibilityServiceList(AccessibilityManager.FEEDBACK_ALL_MASK)
        .map { it.id }.toSet()

    val installedServices = am.installedAccessibilityServiceList

    return installedServices.map { info ->
        val id = info.id
        val parts = id.split("/")
        val packageName = parts.getOrElse(0) { "" }
        val serviceName = parts.getOrElse(1) { "" }

        AccessibilityServiceInfo(
            id = id,
            packageName = packageName,
            serviceName = serviceName,
            label = info.loadLabel(pm).toString(),
            feedbackType = info.feedbackType,
            capabilities = info.capabilities,
            eventTypes = info.eventTypes,
            packageNames = info.packageNames,
            isInstalled = true,
            isEnabled = id in enabledIds
        )
    }
}
```

**Indicadores de riesgo en servicios de accesibilidad:**

| Indicador | Severidad | Descripción |
|-----------|-----------|-------------|
| `CAPABILITY_CAN_RETRIEVE_WINDOW_CONTENT` | Alta | Puede leer contenido de ventanas |
| `CAPABILITY_CAN_PERFORM_GESTURES` | Alta | Puede ejecutar gestos |
| `CAPABILITY_CAN_REQUEST_TOUCH_EXPLORATION` | Media | Exploración táctil |
| `CAPABILITY_CAN_CONTROL_MAGNIFICATION` | Media | Control de ampliación |
| `eventTypes` incluye `TYPE_WINDOW_STATE_CHANGED` | Media | Monitorea cambios de ventana |
| `eventTypes` incluye `TYPE_WINDOW_CONTENT_CHANGED` | Alta | Monitorea cambios de contenido |
| `eventTypes` incluye `TYPE_VIEW_FOCUSED` | Media | Monitorea foco de vistas |
| `packageNames` es null | Alta | Monitorea TODOS los paquetes |
| No es app del sistema | Alta | Servicio de terceros habilitado |

---

### 2.9 Overlay (SYSTEM_ALERT_WINDOW)

| Campo/API | Android 13 | Android 14 | Android 15 | Android 16 | Permiso | Rol | Notas |
|-----------|-----------|-----------|-----------|-----------|---------|-----|-------|
| `Settings.canDrawOverlays(context)` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | API 23+, verifica si la app puede dibujar overlays |
| `PackageInfo.requestedPermissions` contiene `SYSTEM_ALERT_WINDOW` | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | Verificar si la app solicita el permiso |
| `PackageInfo.applicationInfo` flags | ✅ | ✅ | ✅ | ✅ | Ninguno | Paquete visible | Flags relevantes |

**Notas:**
- `SYSTEM_ALERT_WINDOW` es un permiso especial, no un permiso de runtime estándar
- El usuario debe habilitarlo manualmente desde Configuración
- Desde Android 12, las apps con permiso de overlay en primer plano están más restringidas
- `Settings.canDrawOverlays()` solo verifica el estado del permiso, no lo concede

---

### 2.10 VPN

| Campo/API | Android 13 | Android 14 | Android 15 | Android 16 | Permiso | Rol | Notas |
|-----------|-----------|-----------|-----------|-----------|---------|-----|-------|
| `NetworkCapabilities.hasTransport(TRANSPORT_VPN)` | ✅ | ✅ | ✅ | ✅ | `ACCESS_NETWORK_STATE` | Ninguno | API 21+, detecta VPN en red activa |
| `ConnectivityManager.activeNetwork` + `getNetworkCapabilities()` | ✅ | ✅ | ✅ | ✅ | `ACCESS_NETWORK_STATE` | Ninguno | Verificar si la red activa tiene VPN |
| `ConnectivityManager.allNetworks` | ✅ | ✅ | ✅ | ✅ | `ACCESS_NETWORK_STATE` | Ninguno | Verificar todas las redes (VPN global vs activa) |
| `ConnectivityManager.getNetworkCapabilities(network)` | ✅ | ✅ | ✅ | ✅ | `ACCESS_NETWORK_STATE` | Ninguno | Capacidades de una red específica |
| `NetworkCapabilities.transportInfo` | ✅ (API 29+) | ✅ | ✅ | ✅ | `ACCESS_NETWORK_STATE` | Ninguno | Info adicional (VpnTransportInfo) |
| `LinkProperties.interfaceName` | ✅ | ✅ | ✅ | ✅ | `ACCESS_NETWORK_STATE` | Ninguno | Nombre de interfaz (tun0, wg0, etc.) |

**Código de ejemplo:**

```kotlin
data class VpnStatus(
    val isActiveOnCurrentNetwork: Boolean,
    val isActiveOnAnyNetwork: Boolean,
    val interfaceNames: List<String>
)

fun detectVpn(context: Context): VpnStatus {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val activeNetwork = cm.activeNetwork
    val activeCapabilities = activeNetwork?.let { cm.getNetworkCapabilities(it) }
    val isActiveVpn = activeCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true

    val allVpnNetworks = cm.allNetworks.filter { network ->
        cm.getNetworkCapabilities(network)?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
    }

    val interfaces = allVpnNetworks.mapNotNull { network ->
        cm.getLinkProperties(network)?.interfaceName
    }.filter { it.startsWith("tun") || it.startsWith("wg") || it.startsWith("ppp") || it.startsWith("ipsec") }

    return VpnStatus(
        isActiveOnCurrentNetwork = isActiveVpn,
        isActiveOnAnyNetwork = allVpnNetworks.isNotEmpty(),
        interfaceNames = interfaces
    )
}
```

---

### 2.11 Información del dispositivo

| Campo/API | Android 13 | Android 14 | Android 15 | Android 16 | Permiso | Rol | Notas |
|-----------|-----------|-----------|-----------|-----------|---------|-----|-------|
| `Build.MANUFACTURER` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | |
| `Build.MODEL` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | |
| `Build.BRAND` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | |
| `Build.DEVICE` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | Nombre del dispositivo |
| `Build.PRODUCT` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | |
| `Build.HARDWARE` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | |
| `Build.BOARD` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | |
| `Build.VERSION.RELEASE` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | Versión de Android |
| `Build.VERSION.SDK_INT` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | API level |
| `Build.VERSION.INCREMENTAL` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | Build number del fabricante |
| `Build.VERSION.SECURITY_PATCH` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | Fecha del parche de seguridad |
| `Build.FINGERPRINT` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | Identificador único del build |
| `Build.BOOTLOADER` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | Versión del bootloader |
| `Build.TAGS` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | `test-keys` o `release-keys` |
| `Build.TYPE` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | `user`, `userdebug`, `eng` |
| `Build.TIME` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | Timestamp del build |
| `Build.DISPLAY` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | String de display |
| `Runtime.getRuntime().availableProcessors()` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | Número de cores |
| `Runtime.getRuntime().maxMemory()` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | Memoria máxima |
| `ActivityManager.MemoryInfo.totalMem` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | RAM total |
| `ActivityManager.MemoryInfo.availMem` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | RAM disponible |
| `Environment.getExternalStorageDirectory()` | ✅ (scoped) | ✅ (scoped) | ✅ (scoped) | ✅ (scoped) | Ninguno | Ninguno | Almacenamiento (acceso limitado desde API 30) |
| `BatteryManager` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | Estado de batería |

---

### 2.12 Indicadores de integridad / Root

| Campo/API | Android 13 | Android 14 | Android 15 | Android 16 | Permiso | Rol | Notas |
|-----------|-----------|-----------|-----------|-----------|---------|-----|-------|
| `Build.TAGS` == `test-keys` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | Indicador de build no estándar |
| `Build.TYPE` == `userdebug` o `eng` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | Build de desarrollo |
| Verificar archivos de root (`/system/bin/su`, etc.) | ⚠️ | ⚠️ | ⚠️ | ⚠️ | Ninguno | Ninguno | Requiere acceso al filesystem; puede no ser accesible |
| `Build.getSerial()` | ❌ | ❌ | ❌ | ❌ | `READ_PRIVILEGED_PHONE_STATE` | Device/Profile Owner | No accesible para apps normales desde API 29 |
| `Build.BOARD` / `Build.HARDWARE` sospechosos | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | Indicadores de emulador |
| `fingerprint` contiene `generic`, `sdk`, `unknown` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | Indicador de emulador |
| SELinux status | ⚠️ | ⚠️ | ⚠️ | ⚠️ | Ninguno | Ninguno | `/sys/fs/selinux/enforce` — acceso restringido |
| Verificar paquetes de root conocidos | ✅ (si visibles) | ✅ (si visibles) | ✅ (si visibles) | ✅ (si visibles) | `QUERY_ALL_PACKAGES` | Ninguno | Magisk, SuperSU, etc. — requiere visibilidad |

**Notas importantes sobre detección de root:**
- Muchas técnicas de detección de root requieren acceso a archivos del sistema que no están disponibles para apps normales
- `Build.TAGS` y `Build.TYPE` son indicadores útiles pero no concluyentes
- La presencia de paquetes de root puede verificarse si están en la lista de visibilidad
- No se debe concluir "ROOT DETECTADO" basándose únicamente en `test-keys` — puede ser un build legítimo del fabricante

---

### 2.13 TelephonyManager

| Campo/API | Android 13 | Android 14 | Android 15 | Android 16 | Permiso | Rol | Notas |
|-----------|-----------|-----------|-----------|-----------|---------|-----|-------|
| `TelephonyManager.getImei()` | ❌ | ❌ | ❌ | ❌ | `READ_PRIVILEGED_PHONE_STATE` | Device/Profile Owner o Carrier | SecurityException desde API 29 |
| `TelephonyManager.getDeviceId()` | ❌ | ❌ | ❌ | ❌ | `READ_PRIVILEGED_PHONE_STATE` | Device/Profile Owner o Carrier | Deprecated en API 26 |
| `TelephonyManager.getMeid()` | ❌ | ❌ | ❌ | ❌ | `READ_PRIVILEGED_PHONE_STATE` | Device/Profile Owner o Carrier | |
| `TelephonyManager.getSimSerialNumber()` | ❌ | ❌ | ❌ | ❌ | `READ_PRIVILEGED_PHONE_STATE` | Device/Profile Owner o Carrier | |
| `TelephonyManager.getSubscriberId()` | ❌ | ❌ | ❌ | ❌ | `READ_PRIVILEGED_PHONE_STATE` | Device/Profile Owner o Carrier | IMSI |
| `TelephonyManager.getNetworkOperatorName()` | ✅ | ✅ | ✅ | ✅ | `READ_PHONE_STATE` | Ninguno | Nombre del operador |
| `TelephonyManager.getSimOperatorName()` | ✅ | ✅ | ✅ | ✅ | `READ_PHONE_STATE` | Ninguno | Nombre del operador SIM |
| `TelephonyManager.getNetworkOperator()` | ✅ | ✅ | ✅ | ✅ | `READ_PHONE_STATE` | Ninguno | MCC+MNA del operador |
| `TelephonyManager.getSimOperator()` | ✅ | ✅ | ✅ | ✅ | `READ_PHONE_STATE` | Ninguno | MCC+MNA del SIM |
| `TelephonyManager.getPhoneType()` | ✅ | ✅ | ✅ | ✅ | `READ_PHONE_STATE` | Ninguno | GSM, CDMA, etc. |
| `TelephonyManager.getNetworkType()` | ✅ | ✅ | ✅ | ✅ | `READ_PHONE_STATE` | Ninguno | Tipo de red |
| `TelephonyManager.getDataState()` | ✅ | ✅ | ✅ | ✅ | `READ_PHONE_STATE` | Ninguno | Estado de datos |
| `TelephonyManager.getCallState()` | ✅ | ✅ | ✅ | ✅ | `READ_PHONE_STATE` | Ninguno | Estado de llamada |
| `TelephonyManager.hasCarrierPrivileges()` | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | Privilegios de carrier |

**Resumen:** Para apps normales, la información de telephony está muy limitada. IMEI, MEID, IMSI y serial no son accesibles. Solo información básica del operador y estado de red.

---

### 2.14 Android Keystore / Play Integrity

| Campo/API | Android 13 | Android 14 | Android 15 | Android 16 | Permiso | Rol | Notas |
|-----------|-----------|-----------|-----------|-----------|---------|-----|-------|
| `KeyStore` (Android Keystore) | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | Almacenamiento seguro de claves; no expone info del dispositivo |
| Play Integrity API (basic) | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | Verificación de integridad del dispositivo/aplicación |
| Play Integrity API (device) | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | Requiere integración con backend propio |
| Key Attestation | ✅ | ✅ | ✅ | ✅ | Ninguno | Ninguno | Verificar hardware security; requiere backend |

**Notas:**
- Play Integrity requiere un backend propio para verificar tokens
- Key Attestation es el método más robusto para verificar integridad del hardware
- Ambos son complementarios, no alternativos

---

## 3. Limitaciones conocidas

### 3.1 Lo que NO se puede obtener sin permisos especiales

| Información | Permiso requerido | Alternativa |
|------------|-------------------|-------------|
| IMEI / MEID | `READ_PRIVILEGED_PHONE_STATE` | No hay alternativa; esta información no es accesible |
| Número de serie del dispositivo | `READ_PRIVILEGED_PHONE_STATE` | No hay alternativa |
| IMSI | `READ_PRIVILEGED_PHONE_STATE` | No hay alternativa |
| ICCID | `READ_PRIVILEGED_PHONE_STATE` | No hay alternativa |
| MAC address del Wi-Fi | Restringido desde API 29 | No hay alternativa |
| Lista completa de paquetes instalados | `QUERY_ALL_PACKAGES` | Usar `<queries>` con intents conocidos |
| Content providers de otras apps | Permisos del provider | Solo si el provider es exportado y se tienen permisos |
| Estado del bootloader (verificado) | Root o firmware específico | Indicadores indirectos (Build.TAGS, Build.TYPE) |
| SELinux status | Acceso al filesystem | Indicadores indirectos |
| Paquetes ocultos/sin launcher | `QUERY_ALL_PACKAGES` | Lista de paquetes conocidos de MDM |

### 3.2 Lo que se puede obtener pero con limitaciones

| Información | Limitación |
|------------|-----------|
| Administradores activos | Solo se puede detectar por nombre de paquete, no por políticas exactas |
| Device Owner / Profile Owner | Se puede detectar quién es, pero no todas sus políticas sin permisos especiales |
| Servicios de accesibilidad | Se detectan los habilitados, pero no todos los instalados (los no habilitados pueden estar ocultos) |
| Paquetes visibles | Sin `QUERY_ALL_PACKAGES`, solo se ven ~80-90% de apps (las que tienen launcher o intents declarados) |
| Firmas de paquetes | Solo para paquetes visibles |
| Permisos declarados | Solo para paquetes visibles |

---

## 4. Estrategia de detección para el proyecto

### 4.1 Paquetes conocidos de MDM a mantener en la base de datos local

```
# Samsung Knox
com.samsung.android.knox.guard
com.samsung.android.knox.maskagent
com.samsung.android.knox.containercore
com.samsung.android.knox.appconfig
com.samsung.android.knox.mdm
com.samsung.android.knox.keystore
com.samsung.android.knox.barcode
com.sec.android.agent

# Google Android Enterprise
com.google.android.apps.work.oobconfig
com.google.android.apps.work.clouddpc

# Microsoft Intune
com.microsoft.windowsintune.companyportal
com.microsoft.intune

# VMware Workspace ONE
com.vmware.workspaceone

# MobileIron
com.mobileiron
com.mobileiron.connection

# Other MDM
com.blackberry.bbs.integration
com.zimperium.zim
com.lookout
com.bluecoat
com.airwatch.rm.agent.cloud
net.soti.mobicontrol.androidwork
com.splashtop.sos
com.realvnc.viewer.android
```

### 4.2 Orden de análisis recomendado

```
1. Device Info (Build.*)
       ↓
2. Device Policy (isDeviceOwnerApp, isProfileOwnerApp)
       ↓
3. Device Admin detection (queryBroadcastReceivers)
       ↓
4. Accessibility services (getEnabledAccessibilityServiceList)
       ↓
5. Package enumeration (con <queries>)
       ↓
6. Per analyze package:
   a. ApplicationInfo (flags, system app detection)
   b. PackageInfo (permissions, services, receivers, activities, providers)
   c. SigningInfo (certificates)
   d. Classification (SYSTEM/OEM/GOOGLE/CARRIER/USER/UNKNOWN)
   e. Indicators (MDM packages, admin capabilities, etc.)
       ↓
7. Overlay detection (Settings.canDrawOverlays for known packages)
       ↓
8. VPN detection (NetworkCapabilities.TRANSPORT_VPN)
       ↓
9. Build integrity (Build.TAGS, Build.TYPE)
       ↓
10. Risk scoring and report generation
```

### 4.3 Clasificación de aplicaciones

| Clasificación | Criterios | Confianza |
|--------------|-----------|-----------|
| `SYSTEM` | `FLAG_SYSTEM` activo, sin `FLAG_UPDATED_SYSTEM_APP` | 0.95 |
| `OEM` | `FLAG_SYSTEM` + paquete conocido del fabricante (Samsung, Xiaomi, Huawei, etc.) | 0.90 |
| `GOOGLE` | Paquete com.google.* o certificado conocido de Google | 0.95 |
| `CARRIER` | Paquete de operador conocido o certificado de operador | 0.85 |
| `USER` | Sin `FLAG_SYSTEM`, instalado por el usuario | 0.95 |
| `UNKNOWN` | No se puede determinar la origen | 0.50 |

### 4.4 Estados de resultado

Usar estos estados en lugar de afirmaciones definitivas:

```
DETECTADO        → La evidencia indica presencia del elemento
NO DETECTADO     → No se encontró evidencia
NO DISPONIBLE    → La API no está disponible en esta versión/rol
NO SE PUEDE DETERMINAR → Datos insuficientes para una conclusión
```

---

## 5. Referencias

- [Android Package Visibility](https://developer.android.com/training/package-visibility)
- [QUERY_ALL_PACKAGES Policy](https://support.google.com/googleplay/android-developer/answer/10158779)
- [DevicePolicyManager API](https://developer.android.com/reference/android/app/admin/DevicePolicyManager)
- [AccessibilityManager API](https://developer.android.com/reference/android/view/accessibility/AccessibilityManager)
- [PackageManager API](https://developer.android.com/reference/android/content/pm/PackageManager)
- [PackageInfo API](https://developer.android.com/reference/android/content/pm/PackageInfo)
- [SigningInfo API](https://developer.android.com/reference/android/content/pm/SigningInfo)
- [NetworkCapabilities API](https://developer.android.com/reference/android/net/NetworkCapabilities)
- [TelephonyManager API](https://developer.android.com/reference/android/telephony/TelephonyManager)
- [Build API](https://developer.android.com/reference/android/os/Build)
- [Device Identifiers Policy](https://source.android.com/docs/core/connect/device-identifiers)
- [Immutable Device IDs](https://source.android.com/docs/core/permissions/immutable-device-ids)
