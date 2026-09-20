package com.offerpro.launcher.offerpro_launcher

import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.rayole.offerpro.sdk.OfferProSdk
import com.rayole.offerpro.sdk.SdkConfig
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.util.concurrent.Executors

class OfferproLauncherPlugin : FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware {
    private lateinit var channel: MethodChannel
    private lateinit var context: android.content.Context
    private var activity: Activity? = null
    private val main = Handler(Looper.getMainLooper())
    private var worker = Executors.newSingleThreadExecutor()
    private var attached = false
    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        context = binding.applicationContext
        if (worker.isShutdown) worker = Executors.newSingleThreadExecutor()
        attached = true
        channel = MethodChannel(binding.binaryMessenger, "offerpro_sdk")
        channel.setMethodCallHandler(this)
    }
    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        attached = false
        channel.setMethodCallHandler(null)
        worker.shutdownNow()
    }
    override fun onAttachedToActivity(binding: ActivityPluginBinding) { activity = binding.activity }
    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) { activity = binding.activity }
    override fun onDetachedFromActivityForConfigChanges() { activity = null }
    override fun onDetachedFromActivity() { activity = null }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        val sdk = OfferProSdk.getInstance()
        fun text(key: String) = call.argument<String>(key).orEmpty()
        fun millis(key: String) = call.argument<Number>(key)?.toLong()
            ?: throw IllegalArgumentException("$key is required")
        fun perform(): Any? = when (call.method) {
            "initialize" -> {
                val raw = call.argument<Any>("appId")
                val appId = if (raw is Number) raw.toInt() else raw?.toString()?.toIntOrNull() ?: 0
                sdk.initialize(context, SdkConfig.Builder().appId(appId)
                    .userEmail(text("userEmail")).userId(text("userId"))
                    .userCountry(text("userCountry")).encKey(text("encKey"))
                    .advertisingId(text("advertisingId")).deviceId(text("deviceId")).build())
                null
            }
            "openWall" -> { sdk.openWall(activity ?: error("No foreground Activity")); null }
            "openUrl", "openMegaWall", "showLinkOMagic" -> {
                sdk.openUrl(activity ?: error("No foreground Activity"), text("url")); null
            }
            "hasUsageAccess" -> sdk.hasUsageAccess()
            "openUsageAccessSettings" -> { sdk.openUsageAccessSettings(); null }
            "getUsageTimeMs" -> sdk.getUsageTimeMs(text("packageName"), millis("fromMs"), millis("toMs"))
            "isInstalled" -> sdk.isInstalled(text("packageName"))
            "validateInstall" -> sdk.validateInstall(text("packageName"))
            "validateAppUsage" -> sdk.validateAppUsage(text("packageName"), millis("fromMs"), millis("toMs"))
            else -> throw UnsupportedOperationException()
        }
        try {
            when (call.method) {
                "fetchMegaOffer" -> sdk.fetchMegaOffer { if (attached) result.success(it?.toMap()) }
                "fetchLinkOMagic" -> sdk.fetchLinkOMagic { if (attached) result.success(it?.toMap()) }
                "getUsageTimeMs", "validateAppUsage", "isInstalled", "validateInstall" -> worker.execute {
                    try {
                        val value = perform()
                        main.post { if (attached) result.success(value) }
                    } catch (e: Exception) {
                        main.post { if (attached) result.error("SDK_ERROR", e.message, null) }
                    }
                }
                else -> result.success(perform())
            }
        } catch (e: UnsupportedOperationException) { result.notImplemented() }
        catch (e: Exception) { result.error("SDK_ERROR", e.message, null) }
    }
}
