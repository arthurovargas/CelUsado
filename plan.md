# Plan de desarrollo — App de diagnóstico y verificación de dispositivos Android

## 1. Objetivo

Construir una aplicación Android orientada a la revisión de teléfonos usados antes de una compra, capaz de:

- Identificar el dispositivo y su información básica.
- Analizar las aplicaciones instaladas.
- Diferenciar, con niveles de confianza, aplicaciones del sistema/fabricante, Google, operador y terceros.
- Detectar aplicaciones con capacidades administrativas o de control del dispositivo.
- Detectar indicadores relacionados con `Device Administrator`, `Device Owner`, `Profile Owner`, `Accessibility`, `FORCE_LOCK`, servicios persistentes, receivers de arranque y otros permisos/capacidades relevantes.
- Generar un informe comprensible con indicadores, evidencias y nivel de riesgo.
- Evitar afirmar que un equipo está financiado, bloqueado o reportado como robado cuando los datos disponibles solamente constituyen indicios.

## 2. Principios del proyecto

1. **Privacidad primero:** recopilar únicamente los datos necesarios para el diagnóstico.
2. **Análisis pasivo:** la app debe detectar capacidades e indicadores, no intentar bloquear, modificar o desactivar otras aplicaciones.
3. **Evidencia antes que conclusiones:** cada alerta debe indicar qué evidencia la produjo.
4. **Separar capacidad de asociación:** una app con capacidad de bloqueo no implica que pertenezca a una financiera.
5. **Separar financiación de reporte de robo:** un equipo financiado no debe clasificarse automáticamente como robado.
6. **Compatibilidad Android:** asumir que algunas APIs están restringidas según versión, fabricante, rol de la aplicación y privilegios.
7. **Google Play:** diseñar desde el principio considerando las restricciones de visibilidad de paquetes y permisos.
8. **Resultados con incertidumbre:** usar estados como `DETECTADO`, `NO DETECTADO`, `NO DISPONIBLE` y `NO SE PUEDE DETERMINAR`.
9. **Offline-first:** la app debe funcionar completamente sin conexión. El backend es complementario, no requerido.

---

## 2b. Compatibilidad OEM

Cada fabricante tiene comportamientos y apps de sistema propias. Mantener una matriz de compatibilidad:

| Fabricante | Paquetes de sistema relevantes | Notas |
|------------|-------------------------------|-------|
| Samsung | `com.samsung.android.knox.*`, `com.sec.*` | Knox tiene su propio framework de administración |
| Xiaomi | `com.miui.*`, `com.xiaomi.*` | MIUI tiene permisos adicionales no estándar |
| Huawei | `com.huawei.*`, `com.hicore.*` | EMUI/HarmonyOS puede ocultar info |
| OnePlus | `com.oneplus.*` | Cerca de stock Android |
| Motorola | `com.motorola.*` | Poco stock, comportamiento estándar |
| LG | `com.lge.*` | Puede tener APIs propias |
| Pixel | `com.google.*` | Referencia AOSP, más predecible |

**Consideraciones:**

- Algunos fabricantes restringen APIs adicionales más allá de AOSP
- Las apps de sistema varían significativamente entre fabricantes
- La clasificación OEM/Google/Carrier puede requerir bases de datos específicas por fabricante
- Probar en al menos 2-3 fabricantes antes de generalizar resultados

---

## 3. Arquitectura tecnológica

### Cliente

- React Native
- TypeScript
- React Native New Architecture
- Jetpack/Android APIs mediante módulos nativos Kotlin
- Jetpack Compose solo si posteriormente se decide implementar pantallas nativas específicas
- SQLite para cache/base local

### Integración nativa

Crear módulos Kotlin mediante Turbo Native Modules.

Módulos iniciales:

- `PackageAnalyzer`
- `PermissionAnalyzer`
- `DeviceAdminAnalyzer`
- `DevicePolicyAnalyzer`
- `AccessibilityAnalyzer`
- `SystemAppAnalyzer`
- `IntegrityAnalyzer`
- `TelephonyAnalyzer`
- `DeviceInfoAnalyzer`

### Backend

Inicialmente puede omitirse para acelerar el MVP.

Cuando sea necesario:

- NestJS
- PostgreSQL
- API REST
- Sistema de actualización de reglas
- Registro de versiones de reglas

---

## 4. Arquitectura general

```text
┌────────────────────────────────────────────┐
│              React Native                  │
│                                            │
│ UI / navegación / estado / informes        │
│ TypeScript                                 │
└──────────────────────┬─────────────────────┘
                       │
              Turbo Native Modules
                       │
┌──────────────────────▼─────────────────────┐
│                 Kotlin                     │
│                                            │
│ PackageAnalyzer                            │
│ PermissionAnalyzer                         │
│ DeviceAdminAnalyzer                        │
│ DevicePolicyAnalyzer                       │
│ AccessibilityAnalyzer                     │
│ SystemAppAnalyzer                          │
│ IntegrityAnalyzer                          │
│ TelephonyAnalyzer                          │
│ DeviceInfoAnalyzer                         │
└──────────────────────┬─────────────────────┘
                       │
                       ▼
             Normalización de datos
                       │
                       ▼
               Motor de diagnóstico
                       │
             ┌─────────┴─────────┐
             ▼                   ▼
        Reglas locales       Backend/API
             │                   │
             └─────────┬─────────┘
                       ▼
                  Informe final
```

---

# 5. Fase 0 — Investigación técnica

## Objetivo

Determinar exactamente qué información puede obtener una aplicación Android normal en las versiones objetivo.

### Investigar

- `PackageManager`
- Package visibility
- `QUERY_ALL_PACKAGES`
- `ApplicationInfo`
- `PackageInfo`
- permisos declarados
- servicios
- broadcast receivers
- activities
- content providers
- firmas/certificados
- `DevicePolicyManager`
- Device Administrator
- Device Owner
- Profile Owner
- Accessibility Services
- Usage/overlay/VPN capabilities
- `TelephonyManager`
- Android Keystore
- Play Integrity
- bootloader/root indicators

### Versiones objetivo

Inicialmente:

- Android 13
- Android 14
- Android 15
- Android 16

Mantener una matriz de compatibilidad:

```text
Función | Android | Permiso | Rol requerido | Resultado
```

### Entregable

`docs/android-capabilities.md`

---

# 6. Fase 1 — Inicialización del proyecto

## Objetivo

Crear la base React Native.

### Tareas

- Crear proyecto React Native + TypeScript.
- Activar New Architecture.
- Configurar Android.
- Configurar ESLint.
- Configurar Prettier.
- Configurar TypeScript estricto.
- Configurar Git.
- Configurar Jest (unit testing).
- Configurar Detox (E2E testing).
- Crear estructura de carpetas.
- Crear sistema básico de navegación.
- Crear pantalla inicial.
- Crear sistema de logs.
- Crear manejo centralizado de errores.

### Estructura propuesta

```text
src/
├── components/
├── screens/
│   ├── Home/
│   ├── Scan/
│   ├── Applications/
│   ├── ApplicationDetail/
│   ├── Device/
│   └── Report/
├── modules/
│   └── android/
├── domain/
│   ├── device/
│   ├── applications/
│   ├── security/
│   └── financing/
├── services/
├── rules/
├── storage/
├── utils/
└── types/
```

---

# 7. Fase 2 — Módulo de información del dispositivo

## Objetivo

Obtener información básica.

### Datos

- fabricante
- modelo
- marca
- Android
- SDK
- versión/build
- hardware
- SoC cuando esté disponible
- RAM
- almacenamiento
- arquitectura CPU
- estado de batería
- operador cuando esté disponible

### Resultado

```json
{
  "manufacturer": "Samsung",
  "model": "SM-A566B",
  "androidVersion": "16",
  "sdk": 36
}
```

---

# 8. Fase 3 — Analizador de aplicaciones

## Objetivo

Construir el núcleo del diagnóstico.

### Por cada aplicación intentar obtener

- package name
- nombre visible
- versión
- version code
- instalación/actualización cuando esté disponible
- aplicación del sistema
- aplicación del sistema actualizada
- instalador
- permisos declarados
- servicios
- receivers
- activities relevantes
- proveedores
- firmas/certificados
- componentes administrativos
- accessibility service
- otros indicadores disponibles

### Clasificación

Cada aplicación debe clasificarse como:

```text
SYSTEM
OEM
GOOGLE
CARRIER
USER
UNKNOWN
```

Con un `confidence`:

```text
0.00 — 1.00
```

No asumir que `FLAG_SYSTEM` equivale automáticamente a "app del fabricante".

### Estrategia de visibilidad de paquetes (Android 11+)

Desde Android 11, `QUERY_ALL_PACKAGES` está restringido en Google Play. Usar estrategia alternativa:

**Enfoque principal — consulta por intents conocidos:**

```kotlin
val intents = listOf(
    Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
    Intent("android.app.action.DEVICE_ADMIN_ENABLED"),
    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS),
    Intent(Settings.ACTION_VPN_SETTINGS),
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
)

val packages = mutableSetOf<ResolveInfo>()
for (intent in intents) {
    packages.addAll(packageManager.queryIntentActivities(intent, 0))
}
```

**Enfoque complementario — paquetes conocidos de MDM/administración:**

Mantener una lista local de paquetes conocidos que se actualice desde el motor de reglas.

**Resultado esperado:**

- Apps con launcher: visibles siempre
- Apps de administración/MDM: visibles por intents específicos
- Apps sin launcher ni intents relevantes: podrían no aparecer (documentar esta limitación)

> **Importante:** La app debe indicar claramente cuántas aplicaciones total Android permite enumerar y cuántas quedan fuera de alcance por restricciones del sistema.

---

# 9. Fase 4 — Detector de capacidades de control

## Objetivo

Detectar aplicaciones que poseen o podrían poseer capacidades relevantes para administrar/restringir el equipo.

### Indicadores

- Device Administrator
- políticas administrativas
- `FORCE_LOCK`
- wipe/reset relacionado con administración
- Device Owner
- Profile Owner
- Device Policy Controller
- Accessibility Service
- servicios persistentes
- `BOOT_COMPLETED`
- overlay
- VPN
- permisos especialmente sensibles
- componentes privilegiados
- aplicación del sistema
- certificados conocidos

### Limitación de detección directa

`DevicePolicyManager.getActiveAdmins()` solo retorna resultados si **nuestra app** es Device Admin. Para detectar otros administradores sin requerir permisos especiales, usar detección indirecta:

**Métodos de detección indirecta:**

- Verificar si existen paquetes conocidos de MDM (Samsung Knox, Google Android Enterprise, Microsoft Intune, VMware Workspace ONE, etc.)
- Buscar paquetes que declaren el permiso `android.permission.BIND_DEVICE_ADMIN`
- Buscar paquetes que declaren `android.app.device_admin` en su manifest
- Verificar si existen paquetes con `android:sharedUserId="android.uid.system"` que no sean del fabricante conocido
- Revisar si hay apps con `MANAGE_DEVICE_ADMINS` declarado

**Paquetes conocidos de MDM a verificar:**

```text
com.samsung.android.knox.* (Samsung Knox)
com.google.android.apps.work.oobconfig (Android Enterprise)
com.microsoft.windowsintune.companyportal (Intune)
com.vmware.workspaceone (Workspace ONE)
com.mambo.knox.* (Knox Cloud)
com.mobileiron.* (MobileIron)
com.blue Coat.* (Blue Coat/Symantec)
com.zimperium.* (Zimperium)
com.lookout.* (Lookout)
```

> Nota: Esta lista debe mantenerse actualizada en el motor de reglas.

### Resultado por aplicación

```json
{
  "packageName": "com.example.app",
  "indicators": [
    {
      "type": "DEVICE_ADMIN",
      "severity": "HIGH",
      "evidence": "..."
    },
    {
      "type": "FORCE_LOCK",
      "severity": "CRITICAL",
      "evidence": "..."
    }
  ]
}
```

---

# 10. Fase 5 — Integridad del dispositivo

## Objetivo

Añadir señales de modificación del sistema.

### Investigar/detectar

- root
- bootloader
- Play Integrity
- build/test keys
- modificaciones conocidas
- aplicaciones de root
- estado de Verified Boot cuando sea accesible

### Importante

Una señal individual no debe considerarse prueba definitiva.

Ejemplo:

```text
user/test-keys
```

debe aparecer como:

```text
⚠ Indicador de configuración/build no estándar
```

y no automáticamente como:

```text
ROOT DETECTADO
```

---

# 11. Fase 6 — Interfaz

## Pantallas

### Inicio

```text
┌─────────────────────────┐
│   ANALIZAR EQUIPO       │
│                         │
│   Samsung A56           │
│                         │
│   Estado: Sin analizar  │
└─────────────────────────┘
```

### Análisis

Mostrar progreso:

```text
✓ Información del equipo
✓ Aplicaciones
✓ Permisos
✓ Administración
✓ Integridad
```

### Resultado

```text
RIESGO GENERAL

       🟠 ALTO

Motivos:
• Administrador activo
• FORCE_LOCK
• Aplicación asociada a plataforma conocida
```

### Detalle

Cada alerta debe permitir ver:

- aplicación
- package name
- indicador
- permiso/política
- evidencia
- nivel
- explicación
- fuente, cuando exista

---

# 12. Fase 7 — Informe

Crear informe exportable.

### Contenido

1. Identificación
2. Hardware
3. Software
4. Aplicaciones
5. Capacidades administrativas
6. Indicadores de financiación
7. Integridad
8. Riesgos
9. Recomendaciones
10. Fecha/hora
11. versión de la aplicación
12. versión de reglas

El informe debe diferenciar claramente:

```text
HECHO
INDICIO
NO DETERMINABLE
```

---

# 13. Fase 8 — Backend

Implementar cuando el MVP local esté funcionando.

### Servicios

```text
GET /rules/version
GET /apps/{package}
GET /device-models/{tac}
```

### Seguridad

- HTTPS
- autenticación cuando sea necesaria
- rate limiting
- logs mínimos
- anonimización cuando sea posible

---

# 14. Base de datos

Tablas iniciales:

```text
applications
application_certificates
application_indicators
device_models
tac_ranges
rule_versions
evidence_sources
```

### Principio

Toda regla externa debe ser versionada.

```text
rule_version
created_at
updated_at
source
confidence
```

---

# 15. Pruebas

Crear un laboratorio con dispositivos/emuladores que representen:

### Caso A

Teléfono normal sin software especial.

Esperado:

```text
LOW
```

### Caso B

Teléfono con Device Administrator legítimo.

Esperado:

```text
HIGH/RELEVANT
```

### Caso C

Teléfono administrado por MDM.

Esperado:

```text
HIGH
```

### Caso D

Aplicación financiera + capacidades administrativas.

Esperado:

```text
HIGH
```

---

# 16. Seguridad y privacidad

La aplicación puede manejar datos extremadamente sensibles del dispositivo.

### Reglas

- No enviar datos al backend por defecto.
- Mostrar qué datos serán enviados antes de una consulta externa.
- Cifrar información local sensible.
- No registrar números seriales en logs.
- Minimizar telemetría.
- Permitir eliminar el historial.
- No recolectar contenido personal de las aplicaciones.
- No inspeccionar mensajes, fotos, contactos ni archivos personales.

---

# 17. Compatibilidad con Google Play

Antes de publicar:

- Revisar política de visibilidad de paquetes.
- Justificar cualquier uso de `QUERY_ALL_PACKAGES`.
- Revisar permisos sensibles.
- Revisar APIs de administración.
- Revisar política de datos personales.
- Crear declaración de privacidad.
- Minimizar permisos.
- Evaluar si algunas funciones deben quedar en una versión distribuida fuera de Play.

No implementar funcionalidades únicamente porque técnicamente sean posibles si la distribución elegida no las permite.

---

# 18. MVP

La primera versión debe ser pequeña.

## MVP-1

**Esfuerzo estimado:** 3-4 semanas (1 desarrollador)

**Incluye:**

- Fase 0: Investigación técnica (3-5 días)
- Fase 1: Setup proyecto + Jest + Detox (2-3 días)
- Fase 2: DeviceInfoAnalyzer (2-3 días)
- Fase 3: PackageAnalyzer (4-5 días)
- Fase 4: DeviceAdminAnalyzer + detección indirecta (3-4 días)
- Fase 6: UI básica de resultados (3-4 días)
- Fase 7: Informe local (2-3 días)
- PoC en dispositivo real (2-3 días)

Implementar solamente:

```text
✓ Información del dispositivo
✓ Lista de aplicaciones visibles
✓ System/OEM/Google/User classification
✓ Package name
✓ Permisos
✓ Servicios
✓ Receivers
✓ Device Administrator
✓ Indicadores FORCE_LOCK
✓ Accessibility
✓ Device Owner/Profile Owner cuando sea accesible
✓ Informe local
```

No incluir inicialmente:

```text
✗ Backend complejo
✗ OCR
✗ Sistema avanzado de scoring
```

El objetivo del MVP es demostrar que el **análisis técnico local funciona correctamente**.

---

# 19. MVP-2

**Esfuerzo estimado:** 2-3 semanas (1 desarrollador)

Añadir:

```text
✓ Motor de reglas
✓ Risk Score
✓ Análisis de integridad
✓ Informe PDF/JSON
```

---

# 20. MVP-3

**Esfuerzo estimado:** 3-4 semanas (1-2 desarrolladores)

Añadir:

```text
✓ Backend
✓ OCR
✓ Historial de diagnósticos
✓ Comparación de equipos
```

---

# 21. Estructura de repositorio

```text
android-device-checker/
│
├── android/
│   └── app/
│       └── src/main/java/
│           └── com/devicechecker/
│               ├── PackageAnalyzer.kt
│               ├── PermissionAnalyzer.kt
│               ├── DeviceAdminAnalyzer.kt
│               ├── DevicePolicyAnalyzer.kt
│               ├── AccessibilityAnalyzer.kt
│               ├── SystemAppAnalyzer.kt
│               ├── IntegrityAnalyzer.kt
│               ├── TelephonyAnalyzer.kt
│               └── DeviceInfoAnalyzer.kt
│
├── src/
│   ├── components/
│   ├── screens/
│   ├── modules/
│   ├── domain/
│   ├── rules/
│   ├── services/
│   ├── storage/
│   ├── types/
│   └── utils/
│
├── docs/
│   ├── android-capabilities.md
│   ├── architecture.md
│   ├── privacy.md
│   └── threat-model.md
│
├── tests/
│
├── package.json
├── tsconfig.json
└── README.md
```

---

# 22. Orden recomendado de implementación

```text
1. Crear proyecto React Native
        ↓
2. Crear primer Turbo Native Module
        ↓
3. Obtener información del dispositivo
        ↓
4. Obtener aplicaciones
        ↓
5. Analizar permisos
        ↓
6. Analizar servicios/receivers
        ↓
7. Analizar Device Administrator
        ↓
8. Analizar Device Policy
        ↓
9. Analizar Accessibility
        ↓
10. Crear modelo de datos unificado
        ↓
11. Crear motor de reglas
        ↓
12. Crear UI de resultados
        ↓
13. Crear informe
        ↓
14. Añadir integridad
        ↓
15. Crear backend
        ↓
16. Pruebas con dispositivos reales
        ↓
17. Publicación
```

---

# 23. Criterios de éxito del MVP

El MVP se considera funcional cuando puede:

1. Identificar correctamente un dispositivo.
2. Enumerar las aplicaciones que Android permite visualizar.
3. Identificar package names.
4. Clasificar aplicaciones del sistema y terceros con una confianza explícita.
5. Mostrar permisos relevantes.
6. Detectar administradores activos cuando las APIs lo permitan.
7. Identificar indicadores de `FORCE_LOCK`.
8. Identificar servicios de accesibilidad relevantes.
9. Detectar Device Owner/Profile Owner cuando sea posible.
10. Mostrar claramente las limitaciones de Android.
11. Generar un informe reproducible con evidencias.
12. No modificar ni bloquear el dispositivo analizado.

---

# 24. Regla fundamental del producto

El objetivo de la aplicación no debe ser:

> "Encontrar una app de una financiera."

Debe ser:

> **"Determinar qué mecanismos de administración, restricción o control existen en el dispositivo y evaluar si existen indicadores compatibles con financiación o bloqueo."**

Esta diferencia será fundamental para que el diagnóstico sea técnicamente sólido y evite conclusiones incorrectas.

---

# 25. Próximo paso

Antes de escribir una gran cantidad de código, realizar una **Prueba de Concepto (PoC)** de 2 módulos:

**Esfuerzo estimado PoC:** 3-5 días (1 desarrollador)

```text
PoC 1
PackageAnalyzer
    ↓
Lista completa de información disponible
de las aplicaciones.

PoC 2
DeviceAdminAnalyzer
    ↓
Detectar administradores y capacidades
administrativas disponibles (directa e indirectamente).

```

Si estas dos PoC funcionan correctamente en un dispositivo físico, construir el resto del MVP sobre ellas.

---

# 26. Resultado final esperado

La aplicación debe terminar ofreciendo un diagnóstico como:

```text
════════════════════════════════════
       INFORME DE DIAGNÓSTICO
════════════════════════════════════

DISPOSITIVO
Samsung Galaxy A56 5G
Android 16

INTEGRIDAD
✓ Sin indicadores concluyentes de root
⚠ Build/configuración no estándar detectada

ADMINISTRACIÓN
🔴 Device Administrator detectado
🔴 FORCE_LOCK disponible

APLICACIONES
124 analizadas
98 sistema/OEM
18 Google
8 terceros relevantes

Aplicación:
com.example.devicecontrol

Evidencia:
• Administrador activo
• FORCE_LOCK
• Servicio persistente
• Asociación conocida con plataforma

CONCLUSIÓN
🟠 RIESGO ELEVADO

La información disponible presenta indicadores
compatibles con administración
del dispositivo. Se recomienda verificar con el
vendedor la liberación/cancelación del equipo.

════════════════════════════════════
```

La conclusión **nunca debe decir "equipo robado" o "equipo financiado" sin una fuente que permita establecerlo**.
