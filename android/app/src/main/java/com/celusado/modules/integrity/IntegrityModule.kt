package com.celusado.modules.integrity

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableNativeArray
import com.facebook.react.bridge.WritableNativeMap
import com.celusado.modules.integrity.models.IntegrityStatus
import com.celusado.modules.packageanalyzer.PackageAnalyzer

class IntegrityModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String = "AppIntegrity"

    @ReactMethod
    fun analyzeIntegrity(promise: Promise) {
        try {
            val packageAnalyzer = PackageAnalyzer(reactApplicationContext)
            val phase3Result = packageAnalyzer.analyzeAll()

            val analyzer = IntegrityAnalyzer(reactApplicationContext)
            val result = analyzer.analyze(phase3Result.packages)

            val resultMap = WritableNativeMap()

            resultMap.putString("overallStatus", result.overallStatus.name)

            val indicatorsArray = WritableNativeArray()
            for (indicator in result.indicators) {
                val indMap = WritableNativeMap()
                indMap.putString("type", indicator.type.name)
                indMap.putString("status", indicator.status.name)
                indMap.putDouble("confidence", indicator.confidence)
                indMap.putString("source", indicator.source)
                indMap.putString("value", indicator.value)

                val evidenceArray = WritableNativeArray()
                for (ev in indicator.evidence) {
                    val evMap = WritableNativeMap()
                    evMap.putString("type", ev.type)
                    evMap.putString("source", ev.source.name)
                    evMap.putString("field", ev.field)
                    evMap.putString("value", ev.value)
                    evMap.putString("description", ev.description)
                    evidenceArray.pushMap(evMap)
                }
                indMap.putArray("evidence", evidenceArray)

                val limitsArray = WritableNativeArray()
                for (lim in indicator.limitations) {
                    limitsArray.pushString(lim)
                }
                indMap.putArray("limitations", limitsArray)

                indicatorsArray.pushMap(indMap)
            }
            resultMap.putArray("indicators", indicatorsArray)

            val coverageMap = WritableNativeMap()
            coverageMap.putString("status", result.coverage.status)
            val availableArray = WritableNativeArray()
            for (check in result.coverage.availableChecks) {
                availableArray.pushString(check)
            }
            coverageMap.putArray("availableChecks", availableArray)
            val unavailableArray = WritableNativeArray()
            for (check in result.coverage.unavailableChecks) {
                unavailableArray.pushString(check)
            }
            coverageMap.putArray("unavailableChecks", unavailableArray)
            val limitsArray = WritableNativeArray()
            for (lim in result.coverage.limitations) {
                limitsArray.pushString(lim)
            }
            coverageMap.putArray("limitations", limitsArray)
            resultMap.putMap("coverage", coverageMap)

            val globalLimitsArray = WritableNativeArray()
            for (lim in result.limitations) {
                globalLimitsArray.pushString(lim)
            }
            resultMap.putArray("limitations", globalLimitsArray)

            promise.resolve(resultMap)
        } catch (e: Exception) {
            promise.reject("INTEGRITY_ERROR", e.message, e)
        }
    }
}
