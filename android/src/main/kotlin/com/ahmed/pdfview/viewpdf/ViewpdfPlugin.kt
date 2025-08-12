package com.ahmed.pdfview.viewpdf

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.flutter.Log
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding

class ViewpdfPlugin: FlutterPlugin, MethodCallHandler, ActivityAware {
    private lateinit var channel: MethodChannel
    private var currentActivity: Activity? = null

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "viewpdf")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
            }
            "startPDFViewActivity" -> {
                val activity = currentActivity ?: run {
                    result.error("NO_ACTIVITY", "Plugin not attached to an activity.", null)
                    return
                }

                try {
                    val intent = Intent(activity, PdfViewerActivity::class.java)
                    
                    if (call.hasArgument("data")) {
                        val args = call.argument<Map<String, Any>>("data")
                        Log.d("PdfreadxPlugin", "Received data from Flutter: ${call.argument<String>("data")}")
                        args?.let {
                            val message = it["message"] as? String
                            val filePath = it["filePath"] as? String
                            val fileName = it["fileName"] as? String
                            
                            message?.let { intent.putExtra("message_from_flutter", it) }
                            filePath?.let { intent.putExtra("filePath", it) }
                            fileName?.let { intent.putExtra("fileName", it) }
                        }
                    }
                    
                    activity.startActivity(intent)
                    result.success("Native Activity Started from Kotlin")
                } catch (e: Exception) {
                    result.error("START_ACTIVITY_FAILED", "Failed to start native activity: ${e.message}", null)
                }
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    // ActivityAware methods
    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        currentActivity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
        currentActivity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        currentActivity = binding.activity
    }

    override fun onDetachedFromActivity() {
        currentActivity = null
    }
}