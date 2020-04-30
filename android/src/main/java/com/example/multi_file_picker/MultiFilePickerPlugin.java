package com.example.multi_file_picker;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import me.rosuh.filepicker.bean.FileItemBeanImpl;
import me.rosuh.filepicker.config.AbstractFileFilter;
import me.rosuh.filepicker.config.FilePickerManager;

/**
 * MultiFilePickerPlugin
 */
public class MultiFilePickerPlugin implements FlutterPlugin, MethodCallHandler, PluginRegistry.ActivityResultListener, ActivityAware {
    private MethodChannel.Result methodResult;

    private MethodChannel mMethodChannel;
    private Application mApplication;
    private WeakReference<Activity> mActivity;
    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        mMethodChannel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "multi_file_picker");
        mApplication = (Application) flutterPluginBinding.getApplicationContext();
        mMethodChannel.setMethodCallHandler(this);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        mMethodChannel.setMethodCallHandler(null);
        mMethodChannel = null;
    }


    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        methodResult = new MethodResultWrapper(result);
        if (mActivity == null) {
            methodResult.error("no_activity", "image_picker plugin requires a foreground activity.", null);
            return;
        }
        if (call.method.equals("select")) {
            // in Java
            List<String> type = call.argument("type");
            final CustomAudioFileType customAudioFileType = new CustomAudioFileType(type);
            AbstractFileFilter aFilter = new AbstractFileFilter() {
                @Override
                public ArrayList<FileItemBeanImpl> doFilter(final ArrayList<FileItemBeanImpl> arrayList) {
                    ArrayList<FileItemBeanImpl> fileItemBeans = new ArrayList<>();

                    for (FileItemBeanImpl fileItemBean : arrayList) {
                        if (fileItemBean.isDir() || fileItemBean.isChecked() || customAudioFileType.verify(fileItemBean.getFileName())) {
                            fileItemBeans.add(fileItemBean);
                        }
                    }
                    return fileItemBeans;
                }
            };
            FilePickerManager.INSTANCE
                    .from(mActivity.get()).filter(aFilter)
                    .forResult(FilePickerManager.REQUEST_CODE);
        } else {
            result.notImplemented();
        }
    }


    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FilePickerManager.REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                List<String> list = FilePickerManager.INSTANCE.obtainData();
                // do your work
                if (methodResult != null) {
                    methodResult.success(list);
                    methodResult = null;
                }

            } else {
                if (methodResult != null) {
                    methodResult.success(new ArrayList());
                    methodResult = null;
                }
            }
            return true;
        }
        return false;
    }


    @Override
    public void onAttachedToActivity(ActivityPluginBinding activityBinding) {
        mActivity = new WeakReference<>(activityBinding.getActivity());
        activityBinding.addActivityResultListener(this);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {

    }

    @Override
    public void onDetachedFromActivity() {
        mActivity = null;
    }


}

class MethodResultWrapper implements MethodChannel.Result {
    private MethodChannel.Result methodResult;
    private Handler handler;

    MethodResultWrapper(MethodChannel.Result result) {
        methodResult = result;
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void success(final Object result) {
        handler.post(
                new Runnable() {
                    @Override
                    public void run() {
                        methodResult.success(result);
                    }
                });
    }

    @Override
    public void error(
            final String errorCode, final String errorMessage, final Object errorDetails) {
        handler.post(
                new Runnable() {
                    @Override
                    public void run() {
                        methodResult.error(errorCode, errorMessage, errorDetails);
                    }
                });
    }

    @Override
    public void notImplemented() {
        handler.post(
                new Runnable() {
                    @Override
                    public void run() {
                        methodResult.notImplemented();
                    }
                });
    }
}
