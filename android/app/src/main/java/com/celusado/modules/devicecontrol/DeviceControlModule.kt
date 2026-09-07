package com.celusado.modules.devicecontrol

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableNativeArray
import com.facebook.react.bridge.WritableNativeMap
import com.celusado.modules.packageanalyzer.PackageAnalyzer

class DeviceControlModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String = "AppDeviceControl"

    @ReactMethod
    fun analyzeDeviceControl(promise: Promise) {
        try {
            // First get Phase 3 results
            val packageAnalyzer = PackageAnalyzer(reactApplicationContext)
            val phase3Result = packageAnalyzer.analyzeAll()

            // Then run Phase 4 analysis
            val analyzer = DeviceControlAnalyzer(reactApplicationContext)
            val result = analyzer.analyze(phase3Result.packages)

            // Serialize to React Native
            val resultMap = WritableNativeMap()

            // Indicators array
            val indicatorsArray = WritableNativeArray()
            for (indicator in result.indicators) {
                val indMap = WritableNativeMap()
                indMap.putString("type", indicator.type.name)
                indMap.putString("packageName", indicator.packageName)
                indMap.putString("status", indicator.status.name)
                indMap.putDouble("confidence", indicator.confidence)

                // Evidence
                val evidenceArray = WritableNativeArray()
                for (ev in indicator.evidence) {
                    val evMap = WritableNativeMap()
                    evMap.putString("type", ev.type)
                    evMap.putString("source", ev.source.name)
                    evMap.putString("value", ev.value)
                    evMap.putString("description", ev.description)
                    evidenceArray.pushMap(evMap)
                }
                indMap.putArray("evidence", evidenceArray)

                // Limitations
                val limitsArray = WritableNativeArray()
                for (lim in indicator.limitations) {
                    limitsArray.pushString(lim)
                }
                indMap.putArray("limitations", limitsArray)

                indicatorsArray.pushMap(indMap)
            }
            resultMap.putArray("indicators", indicatorsArray)

            // Coverage
            val coverageMap = WritableNativeMap()
            coverageMap.putString("status", result.coverage.status)
            val analyzersArray = WritableNativeArray()
            for (a in result.coverage.analyzersExecuted) {
                analyzersArray.pushString(a)
            }
            coverageMap.putArray("analyzersExecuted", analyzersArray)
            val limitsArray = WritableNativeArray()
            for (lim in result.coverage.limitations) {
                limitsArray.pushString(lim)
            }
            coverageMap.putArray("limitations", limitsArray)
            resultMap.putMap("coverage", coverageMap)

            promise.resolve(resultMap)
        } catch (e: Exception) {
            promise.reject("DEVICE_CONTROL_ERROR", e.message, e)
        }
    }
}
