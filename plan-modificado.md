# Plan Modificado: Rediseño a pantalla única

## Concepto

La app se reduce a **2 pantallas**: una lista principal y un detalle. El análisis se ejecuta automáticamente al abrir la app.

## Estructura final

```
App.tsx
  └── AppNavigator (2 pantallas)
        ├── MainScreen (lista de apps + header de dispositivo)
        └── AppDetailScreen (detalle de una app seleccionada)
```

---

## Pantalla 1: MainScreen

### Flujo

```
App abre
  → Se ejecutan los 4 módulos nativos en paralelo
  → Se muestra progreso paso a paso:
      ○ Información del equipo
      ○ Análisis de aplicaciones
      ○ Análisis de administración
      ○ Análisis de integridad
  → Se calcula score por app
  → Se muestra listado ordenado por score (mayor → menor)
```

### Layout

```
┌─────────────────────────────────────┐
│  Samsung Galaxy A56                 │
│  Android 14 · Parche: 2025-01-01   │
├─────────────────────────────────────┤
│  [Todas] [Sistema] [OEM] [Google]  │  ← filtros
├─────────────────────────────────────┤
│  ● Samsung Knox          85  ALTO   │
│    Device Owner (+40), Policy (+20) │
│    Control total del dispositivo    │
├─────────────────────────────────────┤
│  ● Magisk                70  ALTO   │
│    Root Binary (+35), Framework(+35)│
│    Acceso root al sistema           │
├─────────────────────────────────────┤
│  ● WhatsApp               5  BAJO   │
│    Overlay (+10)                    │
│    Permisos estándar                │
├─────────────────────────────────────┤
│  ... más apps                       │
└─────────────────────────────────────┘
```

### Componentes

- **DeviceHeader**: modelo, Android, parche de seguridad
- **ProgressSteps**: progreso durante el análisis (4 pasos)
- **CategoryFilters**: botones de filtro (Todas, Sistema, OEM, Google, Operador, Usuario)
- **AppScoreCard**: card de cada app con score, nivel, factores, razón

---

## Pantalla 2: AppDetailScreen

### Se accede

Al tocar una app del listado

### Layout

```
┌─────────────────────────────────────┐
│  ← Samsung Knox                     │
├─────────────────────────────────────┤
│                                     │
│  Score: 85/100                      │
│  Nivel: ALTO                        │
│                                     │
│  CAPACIDADES DETECTADAS             │
│  ✓ Device Owner      +40  Confirmado│
│  ✓ Admin Policy      +20  Confirmado│
│  ✓ Persistent Svc    +10  Detectado │
│                                     │
│  RAZÓN                               │
│  Control total del dispositivo.     │
│  Puede borrar, bloquear y restringir│
│  funciones del dispositivo.         │
│                                     │
│  INFORMACIÓN                        │
│  Package: com.samsung.android.knox  │
│  Versión: 4.0.0                     │
│  Instalador: com.sec.android.app... │
│  Categoría: OEM                     │
│                                     │
│  PERMISOS DECLARADOS                │
│  android.permission.BIND_DEVICE...  │
│  android.permission.BIND_ACCESS...  │
│  ...                                │
│                                     │
│  EVIDENCIA                          │
│  Fuente: DevicePolicyManager        │
│  Confianza: 95%                     │
│  Limitaciones: API accesible        │
└─────────────────────────────────────┘
```

---

## Score por app: fórmula

```typescript
function calculateAppScore(app, controlIndicators, integrityIndicators):
  score = 0
  factors = []

  // 1. Control de administración
  if (DEVICE_OWNER)    → score += 40, factors.push("Device Owner (+40)")
  if (PROFILE_OWNER)   → score += 30, factors.push("Profile Owner (+30)")
  if (DEVICE_ADMIN)    → score += 20, factors.push("Device Admin (+20)")

  // 2. Servicios peligrosos
  if (ACCESSIBILITY)   → score += 15, factors.push("Accessibility (+15)")
  if (VPN_SERVICE)     → score += 10, factors.push("VPN (+10)")
  if (OVERLAY)         → score += 10, factors.push("Overlay (+10)")

  // 3. Capacidades destructivas
  if (FORCE_LOCK)      → score += 15, factors.push("Force Lock (+15)")
  if (WIPE_CAPABILITY) → score += 20, factors.push("Wipe (+20)")

  // 4. Persistencia
  if (BOOT_RECEIVER)   → score += 5,  factors.push("Boot Receiver (+5)")
  if (PERSISTENT)      → score += 10, factors.push("Persistente (+10)")

  // 5. Root / Modificación
  if (ROOT_BINARY)     → score += 35, factors.push("Root Binary (+35)")
  if (ROOT_APP)        → score += 35, factors.push("Root App (+35)")
  if (MOD_FRAMEWORK)   → score += 35, factors.push("Mod Framework (+35)")

  return { score: min(score, 100), factors, reason: getReason(factors) }
```

### Niveles

| Score | Nivel |
|---|---|
| 0 | SIN CAPACIDADES |
| 1-14 | BAJO |
| 15-39 | MEDIO |
| 40-69 | ALTO |
| 70-100 | MUY ALTO |

---

## Archivos a crear

| Archivo | Descripción |
|---|---|
| `src/domain/apps/AppScoreCalculator.ts` | Calcula score individual por app |
| `src/domain/apps/AppScoreTypes.ts` | Tipos: `AppScore`, `AppFactor`, `AppLevel` |
| `src/domain/apps/index.ts` | Barrel exports |
| `src/screens/Main/MainScreen.tsx` | Pantalla principal (lista + header) |
| `src/screens/Main/DeviceHeader.tsx` | Header con info del dispositivo |
| `src/screens/Main/ProgressSteps.tsx` | Progreso paso a paso |
| `src/screens/Main/CategoryFilters.tsx` | Filtros de categoría |
| `src/screens/Main/AppScoreCard.tsx` | Card de cada app |
| `src/screens/AppDetail/AppDetailScreen.tsx` | Pantalla de detalle |

## Archivos a modificar

| Archivo | Cambio |
|---|---|
| `src/types/navigation.ts` | Reemplazar rutas: solo `Main` y `AppDetail` |
| `src/navigation/AppNavigator.tsx` | Reemplazar pantallas |
| `App.tsx` | Sin cambios (mantiene AnalysisProvider) |

## Archivos a eliminar (screens)

| Archivo | Razón |
|---|---|
| `src/screens/Home/HomeScreen.tsx` | Reemplazada por MainScreen |
| `src/screens/Scan/ScanScreen.tsx` | Análisis integrado en MainScreen |
| `src/screens/Applications/ApplicationsScreen.tsx` | Reemplazada por MainScreen |
| `src/screens/ApplicationDetail/ApplicationDetailScreen.tsx` | Reemplazada por AppDetailScreen |
| `src/screens/Device/DeviceScreen.tsx` | Info integrada en header |
| `src/screens/Report/ReportScreen.tsx` | Eliminada |
| `src/screens/RiskyApps/RiskyAppsScreen.tsx` | Eliminada |
| `src/screens/RiskOverview/RiskOverviewScreen.tsx` | Eliminada |

## Archivos que se mantienen sin cambios

| Archivo | Razón |
|---|---|
| `src/context/AnalysisContext.tsx` | State management se mantiene |
| `src/modules/android/*` | Bridge nativo se mantiene |
| `src/domain/device/*` | Servicio de dispositivo se mantiene |
| `src/domain/rules/*` | RulesEngine se mantiene (score global opcional) |
| `src/domain/report/*` | ReportExporter se mantiene |
| `src/services/DiagnosticsHistory.ts` | Se mantiene |
| `src/utils/*` | Utilidades se mantienen |
| `src/rules/*.json` | Datos de reglas se mantienen |
| `android/` | Código nativo sin cambios |

---

## Orden de implementación

1. Crear `src/domain/apps/AppScoreTypes.ts`
2. Crear `src/domain/apps/AppScoreCalculator.ts`
3. Crear `src/domain/apps/index.ts`
4. Crear `src/screens/Main/DeviceHeader.tsx`
5. Crear `src/screens/Main/ProgressSteps.tsx`
6. Crear `src/screens/Main/CategoryFilters.tsx`
7. Crear `src/screens/Main/AppScoreCard.tsx`
8. Crear `src/screens/Main/MainScreen.tsx`
9. Crear `src/screens/AppDetail/AppDetailScreen.tsx`
10. Actualizar `src/types/navigation.ts`
11. Actualizar `src/navigation/AppNavigator.tsx`
12. Eliminar pantallas antiguas
13. Verificar TypeScript compila
