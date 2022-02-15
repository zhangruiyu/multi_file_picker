package com.mhz.multi_file_picker

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Handler
import android.os.Looper
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener
import me.rosuh.filepicker.bean.FileItemBeanImpl
import me.rosuh.filepicker.config.AbstractFileFilter
import me.rosuh.filepicker.config.FilePickerManager.REQUEST_CODE
import me.rosuh.filepicker.config.FilePickerManager.from
import me.rosuh.filepicker.config.FilePickerManager.obtainData
import java.lang.ref.WeakReference
import java.util.*

/**
 * MultiFilePickerPlugin
 */
class MultiFilePickerPlugin : FlutterPlugin, MethodCallHandler, ActivityResultListener, ActivityAware {
    private var methodResult: MethodChannel.Result? = null
    private var mMethodChannel: MethodChannel? = null
    private var mApplication: Application? = null
    private var mActivity: WeakReference<Activity>? = null
    override fun onAttachedToEngine(flutterPluginBinding: FlutterPluginBinding) {
        mMethodChannel = MethodChannel(flutterPluginBinding.binaryMessenger, "multi_file_picker")
        mApplication = flutterPluginBinding.applicationContext as Application
        mMethodChannel!!.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(binding: FlutterPluginBinding) {
        mMethodChannel!!.setMethodCallHandler(null)
        mMethodChannel = null
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        methodResult = MethodResultWrapper(result)
        if (mActivity == null) {
            methodResult?.error("no_activity", "image_picker plugin requires a foreground activity.", null)
            return
        }
        if (call.method == "select") {
            // in Java
            val type = call.argument<List<String>>("type")!!
            val customAudioFileType = CustomAudioFileType(type)
            val aFilter: AbstractFileFilter = object : AbstractFileFilter() {
                override fun doFilter(arrayList: ArrayList<FileItemBeanImpl>): ArrayList<FileItemBeanImpl> {
                    val fileItemBeans = ArrayList<FileItemBeanImpl>()
                    for (fileItemBean in arrayList) {
                        if (fileItemBean.isDir || fileItemBean.isChecked() || customAudioFileType.verify(fileItemBean.fileName)) {
                            fileItemBeans.add(fileItemBean)
                        }
                    }
                    return fileItemBeans
                }
            }
            from(mActivity!!.get()!!).filter(aFilter)
                    .forResult(REQUEST_CODE)
        } else {
            result.notImplemented()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val list: List<String> = obtainData()
                // do your work
                if (methodResult != null) {
                    methodResult!!.success(list)
                    methodResult = null
                }
            } else {
                if (methodResult != null) {
                    methodResult!!.success(ArrayList<Any?>())
                    methodResult = null
                }
            }
            return true
        }
        return false
    }

    override fun onAttachedToActivity(activityBinding: ActivityPluginBinding) {
        mActivity = WeakReference(activityBinding.activity)
        activityBinding.addActivityResultListener(this)
    }

    override fun onDetachedFromActivityForConfigChanges() {}
    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {}
    override fun onDetachedFromActivity() {
        mActivity = null
    }
}

internal class MethodResultWrapper(private val methodResult: MethodChannel.Result) : MethodChannel.Result {
    private val handler: Handler = Handler(Looper.getMainLooper())
    override fun success(result: Any?) {
        handler.post { methodResult.success(result) }
    }

    override fun error(
            errorCode: String, errorMessage: String?, errorDetails: Any?) {
        handler.post { methodResult.error(errorCode, errorMessage, errorDetails) }
    }

    override fun notImplemented() {
        handler.post { methodResult.notImplemented() }
    }

}