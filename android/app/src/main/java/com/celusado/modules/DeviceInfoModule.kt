package com.celusado.modules

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableNativeMap
import java.util.Locale

class DeviceInfoModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String = "AppDeviceInfo"

    @ReactMethod
    fun getDeviceInfo(promise: Promise) {
        try {
            val context = reactApplicationContext
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

            val memoryInfo = ActivityManager.MemoryInfo()
            am.getMemoryInfo(memoryInfo)

            val stat = StatFs(Environment.getDataDirectory().path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalStorage = totalBlocks * blockSize
            val availableStorage = availableBlocks * blockSize

            val batteryStatus = context.registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
            val batteryLevel = batteryStatus?.let {
                val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (scale > 0) (level * 100) / scale else -1
            } ?: -1
            val batteryStatusInt = batteryStatus?.getIntExtra(
                BatteryManager.EXTRA_STATUS, -1
            ) ?: -1
            val batteryHealth = batteryStatus?.getIntExtra(
                BatteryManager.EXTRA_HEALTH, -1
            ) ?: -1
            val batteryTemperature = batteryStatus?.let {
                it.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) / 10.0
            } ?: -1.0
            val isCharging = batteryStatus?.let {
                val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
            } ?: false

            val map = WritableNativeMap().apply {
                // Identification
                putString("manufacturer", Build.MANUFACTURER)
                putString("model", Build.MODEL)
                putString("brand", Build.BRAND)
                putString("device", Build.DEVICE)
                putString("product", Build.PRODUCT)
                putString("hardware", Build.HARDWARE)
                putString("board", Build.BOARD)

                // Android / Build
                putString("androidVersion", Build.VERSION.RELEASE)
                putInt("sdkVersion", Build.VERSION.SDK_INT)
                putString("buildNumber", Build.VERSION.INCREMENTAL)
                putString("securityPatch", Build.VERSION.SECURITY_PATCH ?: "")
                putString("fingerprint", Build.FINGERPRINT)
                putString("bootloader", Build.BOOTLOADER)
                putString("buildTags", Build.TAGS)
                putString("buildType", Build.TYPE)
                putString("buildDisplay", Build.DISPLAY)
                putDouble("buildTime", Build.TIME.toDouble())

                // Hardware
                putInt("processorCount", Runtime.getRuntime().availableProcessors())
                putDouble("maxMemory", Runtime.getRuntime().maxMemory().toDouble())

                // RAM
                putDouble("totalRam", memoryInfo.totalMem.toDouble())
                putDouble("availableRam", memoryInfo.availMem.toDouble())

                // Storage
                putDouble("totalStorage", totalStorage.toDouble())
                putDouble("availableStorage", availableStorage.toDouble())

                // Battery
                putInt("batteryLevel", batteryLevel)
                putInt("batteryStatus", batteryStatusInt)
                putInt("batteryHealth", batteryHealth)
                putDouble("batteryTemperature", batteryTemperature)
                putBoolean("isCharging", isCharging)
            }

            promise.resolve(map)
        } catch (e: Exception) {
            promise.reject("DEVICE_INFO_ERROR", e.message, e)
        }
    }
}
